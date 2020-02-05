/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.vn.common.queue;

import com.google.gson.JsonArray;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Address;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.ShutdownListener;
import com.rabbitmq.client.ShutdownSignalException;
import com.smartfoxserver.v2.SmartFoxServer;
import com.smartfoxserver.v2.entities.User;
import com.smartfoxserver.v2.entities.data.SFSObject;
import game.command.SFSAction;
import game.command.SFSCommand;
import game.key.SFSKey;
import game.vn.common.config.QueueConfig;
import game.vn.common.config.ServerConfig;
import game.vn.common.constant.ExtensionConstant;
import game.vn.common.lib.hazelcast.Board;
import game.vn.common.lib.findboard.FindBoardRequest;
import game.vn.common.lib.findboard.FindBoardResponse;
import game.vn.common.lib.ranking.LeaderboardObject;
import game.vn.common.lib.vip.AddMoneyNotifyInfo;
import game.vn.common.lib.vip.UserVipData;
import game.vn.common.lib.vip.VipQueueObj;
import game.vn.common.lib.vip.ZCashoutInfo;
import game.vn.common.lib.vip.ZCashoutResult;
import game.vn.util.GsonUtil;
import game.vn.util.Utils;
import game.vn.util.db.Database;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.msgpack.MessagePack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author hanv
 */
public class QueueService {

    private final static Logger LOGGER = LoggerFactory.getLogger(QueueService.class);
    private static final MessagePack MSG_PACK = new MessagePack();

    public static final String EXCHANGE_TYPE_FANOUT = "fanout";
    public static final String EXCHANGE_TYPE = "direct";

    private static final QueueService _instance = new QueueService();
    private Connection conn;
    private Channel channel;
    private AMQP.BasicProperties prop;
    private final String consumeTag = "EventConsume" + System.currentTimeMillis();

    public static QueueService getInstance() {
        return _instance;
    }

    public void init() {
        try {
            ConnectionFactory factory = new ConnectionFactory();
            factory.setUsername(QueueConfig.getInstance().getQueueUsername());
            factory.setPassword(QueueConfig.getInstance().getQueuePassword());
            factory.setAutomaticRecoveryEnabled(true);

            ExecutorService es = Executors.newFixedThreadPool(QueueConfig.getInstance().getQueuePoolSize());
            conn = factory.newConnection(es, getHosts());
            conn.addShutdownListener(new ShutdownListener() {
                @Override
                public void shutdownCompleted(ShutdownSignalException paramShutdownSignalException) {
                    LOGGER.warn("------------ RabbitMQ Service shutdown complete: " + paramShutdownSignalException);
                }
            });

            int serverId = ServerConfig.getInstance().getServerId();
            String queueFindBoardResponse = QueueConfig.getInstance().getQueueFindBoardResponse() + serverId;
            String keyFindBoardResponse = QueueLogKey.KEY_FIND_BOARD_RESPONSE + serverId;
            Consumer findBoardConsumer = new DefaultConsumer(channel) {
                @Override
                public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties props, byte[] body) throws IOException {
                    processFindBoardResponse(new String(body, "UTF-8"));
                }
            };

            String queueRankingResponse = QueueConfig.getInstance().getQueueRankingResponse() + serverId;
            String queueVipResponse = QueueConfig.getInstance().getQueueVipResponse() + serverId;
            String keyRankingResponse = QueueLogKey.KEY_RESPONSE_USER_RANKING + serverId;
            String keyVipResponse = QueueLogKey.KEY_RESPONSE_USER_VIP + serverId;

            Consumer rankingConsumer = new DefaultConsumer(channel) {
                @Override
                public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties props, byte[] body) throws IOException {
                    processRankingResponse(body);
                }
            };

            Consumer vipConsumer = new DefaultConsumer(channel) {
                @Override
                public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties props, byte[] body) throws IOException {
                    processVipInfoResponse(body);
                }
            };

            String exchangeFindboard = QueueConfig.getInstance().getExchangeFindBoard();
            String exchangeResponseRanking = QueueConfig.getInstance().getExchangeRankingResponse();
            String exchangeRequestVip = QueueConfig.getInstance().getExchangeVipRequest();
            String exchangeResponseVip = QueueConfig.getInstance().getExchangeVipResponse();

            channel = conn.createChannel();
            channel.exchangeDeclare(exchangeFindboard, EXCHANGE_TYPE);
            channel.exchangeDeclare(exchangeResponseRanking, EXCHANGE_TYPE);
            channel.exchangeDeclare(exchangeRequestVip, EXCHANGE_TYPE);
            channel.exchangeDeclare(exchangeResponseVip, EXCHANGE_TYPE);

            channel.queueDeclare(queueFindBoardResponse, true, false, false, null);
            channel.queueDeclare(queueRankingResponse, true, false, false, null);
            channel.queueDeclare(QueueConfig.getInstance().getQueueVipRequest(), true, false, false, null);
            channel.queueDeclare(queueVipResponse, true, false, false, null);

            channel.queueBind(queueFindBoardResponse, exchangeFindboard, keyFindBoardResponse);
            channel.queueBind(QueueConfig.getInstance().getQueueVipRequest(), exchangeRequestVip, QueueLogKey.KEY_REQUEST_USER_VIP);
            channel.queueBind(queueRankingResponse, exchangeResponseRanking, keyRankingResponse);
            channel.queueBind(queueVipResponse, exchangeResponseVip, keyVipResponse);

            channel.basicConsume(queueFindBoardResponse, true, findBoardConsumer);
            channel.basicConsume(queueRankingResponse, true, rankingConsumer);
            channel.basicConsume(queueVipResponse, true, vipConsumer);

            AMQP.BasicProperties.Builder builder = new AMQP.BasicProperties.Builder();
            prop = builder.deliveryMode(2).build();

            LOGGER.info("---------------- QueueService init done, connected to: " + conn.getAddress() + ":" + conn.getPort() + " ----------------");
        } catch (Exception ex) {
            LOGGER.error("QueueService init error", ex);
        }
    }

    private Address[] getHosts() {
        List<String> listHost = Arrays.asList(QueueConfig.getInstance().getQueueHost().split(";"));
        if (listHost.size() > 0) {
            Address[] queueHosts = new Address[listHost.size()];
            for (int i = 0; i < listHost.size(); i++) {
                queueHosts[i] = new Address(listHost.get(i), QueueConfig.getInstance().getQueuePort());
            }
            return queueHosts;
        }
        return null;
    }

    /**
     *
     * @param request
     */
    public void sendFindBoardRequest(FindBoardRequest request) {
        try {
            byte[] bytes = GsonUtil.toJson(request).getBytes(Charset.forName("UTF-8"));
            channel.basicPublish(QueueConfig.getInstance().getExchangeFindBoard(), QueueLogKey.KEY_FIND_BOARD_REQUEST, prop, bytes);
        } catch (Exception e) {
            LOGGER.error("QueueService.sendFindBoardRequest", e);
        }
    }

    /**
     *
     * @param data
     */
    private void processFindBoardResponse(String data) {
        try {
            LOGGER.debug("find board response: " + data);
            FindBoardResponse response = GsonUtil.fromJson(data, FindBoardResponse.class);
            User user = Utils.findUser(response.getUserId());
            if (user == null) {
                LOGGER.info("find board user not found: " + response.getUserId());
                return;
            }
            JsonArray array = new JsonArray();
            List<Board> boards = response.getBoards();
            for (Board board : boards) {
                array.add(GsonUtil.toJsonTree(board));
            }

            SFSObject sfsObj = new SFSObject();
            sfsObj.putInt(SFSKey.ACTION_INCORE, SFSAction.FIND_BOARD);
            sfsObj.putUtfString(SFSKey.BOARDS, array.toString());
            SmartFoxServer.getInstance().getAPIManager().getSFSApi().sendExtensionResponse(SFSCommand.CLIENT_REQUEST,
                    sfsObj, user, null, false);
        } catch (Exception e) {
            LOGGER.error("QueueService.processFindBoardResponse", e);
        }
    }

    /**
     *
     * @param obj
     */
    public void sendRankingRequest(LeaderboardObject obj) {
        try {
            byte[] bytes = MSG_PACK.write(obj);
            channel.basicPublish(QueueConfig.getInstance().getExchangeRankingRequest(), QueueLogKey.KEY_REQUEST_USER_RANKING, prop, bytes);
        } catch (Exception e) {
            LOGGER.error("QueueService.sendRankingRequest", e);
        }
    }

    /**
     *
     * @param obj
     */
    public void sendVipRequest(VipQueueObj obj) {
        try {
            byte[] bytes = MSG_PACK.write(obj);
            channel.basicPublish(QueueConfig.getInstance().getExchangeVipRequest(), QueueLogKey.KEY_REQUEST_USER_VIP, prop, bytes);
        } catch (Exception e) {
            LOGGER.error("QueueService.sendVipRequest", e);
        }
    }
    
    /**
     *
     * @param data
     */
    private void processRankingResponse(byte[] data) {
        try {
            LeaderboardObject obj = MSG_PACK.read(data, LeaderboardObject.class);
            User user = Utils.findUser(obj.getUserid());
            if (user != null) {
                SFSObject sfsObj = new SFSObject();
                sfsObj.putInt(SFSKey.ACTION_INCORE, SFSAction.RANKING_GET_LEADER_BOARD_INFO);
                sfsObj.putInt(SFSKey.COMMAND, obj.getCommand());
                sfsObj.putBool(SFSKey.STATUS, obj.getStatus());
                if (obj.getCommand() != LeaderboardObject.SWITCH_EVENT_JOIN_STATUS) {
                    sfsObj.putInt(SFSKey.SERVICE_ID, obj.getServiceId());
                    sfsObj.putUtfString(SFSKey.DATA, obj.getData());
                }
                SmartFoxServer.getInstance().getAPIManager().getSFSApi().sendExtensionResponse(SFSCommand.CLIENT_REQUEST, sfsObj, user, null, false);
            }
        } catch (Exception e) {
            LOGGER.error("QueueService.processRankingResponse", e);
        }
    }

    public synchronized void basicAck(long deliveryTag) {
        try {
            channel.basicAck(deliveryTag, false);
        } catch (IOException e) {
            LOGGER.error("Channel basicAck fail: " + deliveryTag, e);
        }
    }

    public void shutDown() {
        try {
            if (channel != null && channel.isOpen()) {
                channel.close();
            }
            if (conn != null && conn.isOpen()) {
                conn.close();
            }
            LOGGER.info("------------ RabbitMQ Service stopped-------------");
        } catch (Exception e) {
            LOGGER.error("------------ RabbitMQ Service: ", e);
        }
    }

    /**
     *
     * @param obj
     */
    public void sendVipInfoRequest(LeaderboardObject obj) {
        try {
            byte[] bytes = MSG_PACK.write(obj);
            channel.basicPublish(QueueLogKey.KEY_REQUEST_USER_VIP, QueueLogKey.KEY_REQUEST_USER_VIP, prop, bytes);
        } catch (Exception e) {
            LOGGER.error("QueueService.sendRankingRequest", e);
        }
    }

    /**
     *
     * @param data
     */
    private void processVipInfoResponse(byte[] data) {
        try {
            VipQueueObj obj = MSG_PACK.read(data, VipQueueObj.class);
            LOGGER.debug("vip response data: " + obj.getData());
            User user = Utils.findUser(obj.getUserid());
            if (user == null) {
                LOGGER.info("vip response user not found: " + obj.getUserid());
                return;
            }
            
            SFSObject sfsObj = new SFSObject();
            switch (obj.getCommand()) {
                case VipQueueObj.GET_VIP_INFO_OBJ:

                    sfsObj.putInt(SFSKey.ACTION_INCORE, SFSAction.VERIFY_GG_IAP);
                    sfsObj.putUtfString(SFSKey.VIP_INFO, obj.getData());
                    SmartFoxServer.getInstance().getAPIManager().getSFSApi().sendExtensionResponse(SFSCommand.CLIENT_REQUEST, sfsObj, user, null, false);
                    break;

                case VipQueueObj.GET_USER_VIP_DATA:
                    UserVipData userVipData = GsonUtil.fromJson(obj.getData(), UserVipData.class);

                    sfsObj.putInt(SFSKey.ACTION_INCORE, SFSAction.GET_USER_VIP_INFO);
                    sfsObj.putUtfString(SFSKey.VIP_RANK, userVipData.getCurrentRank());
                    sfsObj.putUtfString(SFSKey.VIP_STEP, userVipData.getCurrentStep());
                    sfsObj.putUtfString(SFSKey.NEXT_RANK, userVipData.getNextRank());
                    sfsObj.putUtfString(SFSKey.NEXT_STEP, userVipData.getNextStep());

                    sfsObj.putInt(SFSKey.CURRENT_POINT, userVipData.getCurrentPoint());
                    sfsObj.putInt(SFSKey.TOTAL_POINT, userVipData.getTotalPoint());
                    sfsObj.putLong(SFSKey.TOTAL_Z, userVipData.getCurrentZ());
                    sfsObj.putUtfString(SFSKey.CURRENT_IMG, userVipData.getCurrent_imgUrl() != null ? userVipData.getCurrent_imgUrl() : "");
                    sfsObj.putUtfString(SFSKey.NEXT_IMG, userVipData.getNext_imgUrl() != null ? userVipData.getNext_imgUrl() : "");
                    sfsObj.putUtfString(SFSKey.CURRENT_IMG_MINI, userVipData.getCurrent_imgUrl_mini() != null ? userVipData.getCurrent_imgUrl_mini() : "");
                    sfsObj.putUtfString(SFSKey.NEXT_IMG_MINI, userVipData.getNext_imgUrl_mini() != null ? userVipData.getNext_imgUrl_mini() : "");
                    SmartFoxServer.getInstance().getAPIManager().getSFSApi().sendExtensionResponse(SFSCommand.CLIENT_REQUEST, sfsObj, user, null, false);
                    break;

                case VipQueueObj.GET_Z_CASHOUT_INFO:
                    ZCashoutInfo zCashoutInfo = GsonUtil.fromJson(obj.getData(), ZCashoutInfo.class);
                    sfsObj.putInt(SFSKey.ACTION_INCORE, SFSAction.GET_CASHOUT_Z_INFO);
                    sfsObj.putDouble(SFSKey.CASHOUT_RATE, zCashoutInfo.getCashoutRate());
                    sfsObj.putInt(SFSKey.CASHOUT_MONTH_QUOTA, zCashoutInfo.getMonthQuota().intValue());
                    sfsObj.putLong(SFSKey.TOTAL_Z, zCashoutInfo.getCurrentZ());
                    sfsObj.putLong(SFSKey.MIN_Z_CASHOUT, zCashoutInfo.getMinCashoutZ());
                    SmartFoxServer.getInstance().getAPIManager().getSFSApi().sendExtensionResponse(SFSCommand.CLIENT_REQUEST, sfsObj, user, null, false);
                    break;

                case VipQueueObj.DO_Z_CASHOUT:

                    ZCashoutResult zCashoutResult = GsonUtil.fromJson(obj.getData(), ZCashoutResult.class);

                    sfsObj.putInt(SFSKey.ACTION_INCORE, SFSAction.CASHOUT_Z_POINT);
                    sfsObj.putInt(SFSKey.CASHOUT_RESULT, zCashoutResult.getCode());
                    sfsObj.putUtfString(SFSKey.CASHOUT_MSG, String.format(zCashoutResult.getMessage()));
                    SmartFoxServer.getInstance().getAPIManager().getSFSApi().sendExtensionResponse(SFSCommand.CLIENT_REQUEST, sfsObj, user, null, false);
                    break;

                case VipQueueObj.PUSH_CASHOUT_BONUS_MONEY:
                    double money = Database.instance.getUserMoney(obj.getUserid());
                    double point = Database.instance.getUserPoint(obj.getUserid());
                    Utils.updateMoneyOfUser(user, money);
                    Utils.updatePointOfUser(user, point);

                    sfsObj.putInt(SFSKey.ACTION_INCORE, SFSAction.NOTIFY_BONUS_MONEY);
                    sfsObj.putUtfString(SFSKey.BONUS_CASH_INFO, obj.getData());
                    SmartFoxServer.getInstance().getAPIManager().getSFSApi().sendExtensionResponse(SFSCommand.CLIENT_REQUEST, sfsObj, user, null, false);

                    try {
                        AddMoneyNotifyInfo info = GsonUtil.fromJson(obj.getData(), AddMoneyNotifyInfo.class);
                        if (info.getUpdateType() == AddMoneyNotifyInfo.UPDATE_TYPE_CHARGE) {
                            if (Database.instance.getPinStatus(obj.getUserid()) == ExtensionConstant.PIN_STATUS_INACTIVE) {
                                Database.instance.updatePinStatus(obj.getUserid(), ExtensionConstant.PIN_STATUS_ACTIVATING);
                                sfsObj.putInt(SFSKey.ACTION_INCORE, SFSAction.FORCE_ACTIVATE_PIN);
                                SmartFoxServer.getInstance().getAPIManager().getSFSApi().sendExtensionResponse(
                                        SFSCommand.CLIENT_REQUEST, sfsObj, user, null, false);
                            }
                        }
                    } catch (Exception e) {
                        LOGGER.error("QueueService.processVipInfoResponse", e);
                    }
                    break;

                case VipQueueObj.PUSH_UP_LEVEL:

                    sfsObj.putInt(SFSKey.ACTION_INCORE, SFSAction.NOTIFY_UP_LEVEL_VIP);
                    sfsObj.putUtfString(SFSKey.UP_LEVEL_INFO, obj.getData());
                    SmartFoxServer.getInstance().getAPIManager().getSFSApi().sendExtensionResponse(SFSCommand.CLIENT_REQUEST, sfsObj, user, null, false);
                    break;

            }

        } catch (Exception e) {
            LOGGER.error("QueueService.processVipInfoResponse", e);
        }
    }
    
    /**
     * 
     * @param data
     */
    public void sendWithdrawTransaction(String data) {
        try {
            byte[] bytes = data.getBytes(Charset.forName("UTF-8"));
            channel.basicPublish(QueueConfig.getInstance().getExchangeTransactionHistory(), QueueLogKey.KEY_TRANSACTION_WITHDRAW, prop, bytes);
        } catch (Exception e) {
            LOGGER.error("QueueService.sendWithdrawTransaction", e);
        }
    }

}
