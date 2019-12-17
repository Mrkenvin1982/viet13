/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.vn.common.queue;

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
import game.vn.common.lang.GameLanguage;
import game.vn.common.lib.taixiu.TaiXiuBetResult;
import game.vn.common.lib.taixiu.TaiXiuBuyTicketResult;
import game.vn.common.lib.taixiu.TaiXiuCommand;
import game.vn.common.lib.taixiu.TaiXiuMatch;
import game.vn.common.lib.taixiu.TaiXiuQueueData;
import game.vn.common.lib.taixiu.TaiXiuUserBet;
import game.vn.common.lib.taixiu.TaiXiuUserOrder;
import game.vn.common.lib.taixiu.TaiXiuUserOrderList;
import game.vn.common.service.BroadcastService;
import game.vn.util.GsonUtil;
import game.vn.util.Utils;
import game.vn.util.db.Database;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author hanv
 */
public class QueueTaiXiu {
    private final static Logger LOGGER = LoggerFactory.getLogger(QueueService.class);

    private static final String EXCHANGE_TYPE_FANOUT = "fanout";
    private static final String EXCHANGE_TYPE_DIRECT = "direct";

    private static final QueueTaiXiu instance = new QueueTaiXiu();
    private Connection conn;
    private Channel channel;
    private AMQP.BasicProperties prop;

    public static QueueTaiXiu getInstance() {
        return instance;
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
            
            channel = conn.createChannel();
            int serverId = ServerConfig.getInstance().getServerId();
            String queueNotify = channel.queueDeclare().getQueue();
            String queueResponse = QueueConfig.getInstance().getQueueTaiXiuResponse() + serverId;
            String exchange = QueueConfig.getInstance().getExchangeTaiXiu();
            String exchangeNotify = QueueConfig.getInstance().getExchangeTaiXiuNotify();
            
            Consumer consumer = new DefaultConsumer(channel) {
                @Override
                public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties props, byte[] body) throws IOException {
                    processResponse(body);
                }
            };
            
            Consumer consumerNotify = new DefaultConsumer(channel) {
                @Override
                public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties props, byte[] body) throws IOException {
                    processNotify(body);
                }
            };
            
            channel.exchangeDeclare(exchange, EXCHANGE_TYPE_DIRECT);
            channel.exchangeDeclare(exchangeNotify, EXCHANGE_TYPE_FANOUT);
            channel.queueDeclare(queueResponse, true, false, false, null);
            channel.queueBind(queueResponse, exchange, QueueLogKey.KEY_TAIXIU_RESPONSE + serverId);
            channel.queueBind(queueNotify, exchangeNotify, "");
            channel.basicConsume(queueResponse, true, consumer);
            channel.basicConsume(queueNotify, true, consumerNotify);
            
            AMQP.BasicProperties.Builder builder = new AMQP.BasicProperties.Builder();
            prop = builder.deliveryMode(2).build();
            
            LOGGER.info("---------------- QueueTaiXiu init done, connected to: " + conn.getAddress() + ":" + conn.getPort() + " ----------------");
        } catch (Exception ex) {
            LOGGER.error("QueueTaiXiu init error", ex);
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
    
    private void processResponse(byte[] body) {
        try {
            String data = new String(body, "UTF-8");
            LOGGER.debug("taixiu response: " + data);
            TaiXiuQueueData queueData = GsonUtil.fromJson(data, TaiXiuQueueData.class);
            User user = Utils.findUser(queueData.getUserId());
            if (user == null) {
                return;
            }
            
            switch (queueData.getCommand()) {
//                case TaiXiuCommand.GET_CURRENT_MATCH_INFO:
//                    TaiXiuGameInfo info = GsonUtil.fromJson(queueData.getData(), TaiXiuGameInfo.class);
//                    BigDecimal maxBet = TaiXiuConfig.getInstance().getMaxBet();
//                    BigDecimal minBet = TaiXiuConfig.getInstance().getMinBet();
//                    double money = user.getVariable(UserInforPropertiesKey.MONEY_USER).getDoubleValue();
//                    if (money < minBet.doubleValue()) {
//                        minBet = maxBet = BigDecimal.ZERO;
//                    }
//                    info.setMaxBet(maxBet);
//                    info.setMinBet(minBet);
//                    queueData.setData(GsonUtil.toJson(info));
//                    break;
                case TaiXiuCommand.BET:
                    TaiXiuBetResult result = GsonUtil.fromJson(queueData.getData(), TaiXiuBetResult.class);
                    switch (result.getCode()) {
                        case TaiXiuBetResult.CODE_FAIL:
                            result.setMessage(GameLanguage.getMessage(GameLanguage.ERROR_TRY_AGAIN, Utils.getUserLocale(user)));
                            break;
                        case TaiXiuBetResult.CODE_FAIL_CONTINUOUSLY:
                            result.setMessage(GameLanguage.getMessage(GameLanguage.WAIT_FOR_NEXT_BET, Utils.getUserLocale(user)));
                            break;
                        case TaiXiuBetResult.CODE_FAIL_OVER_TIME:
                            result.setMessage(GameLanguage.getMessage(GameLanguage.BETTING_TIME_OVER, Utils.getUserLocale(user)));
                            break;
                        case TaiXiuBetResult.CODE_FAIL_BET_TWO_SIDE:
                            result.setMessage(GameLanguage.getMessage(GameLanguage.BET_ONE_SIDE_PER_MATCH, Utils.getUserLocale(user)));
                            break;
                        case TaiXiuBetResult.CODE_FAIL_INVALID_BET_MONEY:
                            result.setMessage(GameLanguage.getMessage(GameLanguage.INVALID_BET_MONEY, Utils.getUserLocale(user)));
                            break;
                    }
                    queueData.setData(GsonUtil.toJson(result));
                    break;
                case TaiXiuCommand.BUY_NOHU_TICKET:
                    TaiXiuBuyTicketResult buyResult = GsonUtil.fromJson(queueData.getData(), TaiXiuBuyTicketResult.class);
                    switch (buyResult.getCode()) {
                        case TaiXiuBuyTicketResult.CODE_FAIL:
                            buyResult.setMessage(GameLanguage.getMessage(GameLanguage.ERROR_TRY_AGAIN, Utils.getUserLocale(user)));
                            break;
                        case TaiXiuBuyTicketResult.CODE_FAIL_OVER_TIME:
                            buyResult.setMessage(GameLanguage.getMessage(GameLanguage.BETTING_TIME_OVER, Utils.getUserLocale(user)));
                            break;
                        case TaiXiuBuyTicketResult.CODE_FAIL_CONTINUOUSLY:
                            buyResult.setMessage(GameLanguage.getMessage(GameLanguage.WAIT_FOR_NEXT_BET, Utils.getUserLocale(user)));
                            break;
                    }
                    queueData.setData(GsonUtil.toJson(buyResult));
                    break;
                case TaiXiuCommand.GET_TOP:
                    List<TaiXiuUserOrder> list = GsonUtil.fromJson(queueData.getData(), TaiXiuUserOrderList.class).getUserOrderList();
                    for (TaiXiuUserOrder order : list) {
                        order.setUsername(Database.instance.getDisplayName(order.getUserId()));
                    }
                    queueData.setData(GsonUtil.toJson(new TaiXiuUserOrderList(list)));
                    break;
                case TaiXiuCommand.GET_MATCH_DETAIL:
                    TaiXiuMatch match = GsonUtil.fromJson(queueData.getData(), TaiXiuMatch.class);
                    List<TaiXiuUserBet> bets = match.getUserBets();
                    for (TaiXiuUserBet bet : bets) {
                        bet.setUsername(Database.instance.getDisplayName(bet.getUserId()));
                    }
                    match.setUserBets(bets);
                    queueData.setData(GsonUtil.toJson(match));
                    break;
            }

            SFSObject sfsObj = new SFSObject();
            sfsObj.putInt(SFSKey.ACTION_INCORE, SFSAction.PLAY_TAIXIU);
            sfsObj.putByte(SFSKey.COMMAND, queueData.getCommand());
            sfsObj.putUtfString(SFSKey.DATA, queueData.getData());
            SmartFoxServer.getInstance().getAPIManager().getSFSApi().sendExtensionResponse(SFSCommand.CLIENT_REQUEST,
                    sfsObj, user, null, false);

        } catch (Exception e) {
            LOGGER.error("", e);
        }
    }
    
    private void processNotify(byte[] body) {
        try {
            String data = new String(body, "UTF-8");
            LOGGER.debug("taixiu notify: " + data);
            TaiXiuQueueData queueData = GsonUtil.fromJson(data, TaiXiuQueueData.class);
            if (queueData.getCommand() == TaiXiuCommand.NOTIFY_NOHU) {
                SFSObject sfsObj = new SFSObject();
                sfsObj.putInt(SFSKey.ACTION_INCORE, SFSAction.PLAY_TAIXIU);
                sfsObj.putByte(SFSKey.COMMAND, TaiXiuCommand.NOTIFY_NOHU);
                sfsObj.putUtfString(SFSKey.DATA, queueData.getData());
                BroadcastService.broadcast(SFSCommand.CLIENT_REQUEST, sfsObj);
                return;
            }
            
            User user = Utils.findUser(queueData.getUserId());
            if (user == null) {
                return;
            }
            
//            switch (queueData.getCommand()) {
//                case TaiXiuCommand.BET:
//                    TaiXiuBetResult result = GsonUtil.fromJson(queueData.getData(), TaiXiuBetResult.class);
//                    if (queueData.getUserId().equals(result.getUserId())) {
//                        double money = Database.instance.getUserMoney(result.getUserId());
//
//                        BigDecimal maxBet = TaiXiuConfig.getInstance().getMaxBet();
//                        BigDecimal minBet = TaiXiuConfig.getInstance().getMinBet();
//                        if (money < maxBet.doubleValue()) {
//                            maxBet = new BigDecimal(money);
//                        }
//                        if (maxBet.compareTo(minBet) < 0) {
//                            maxBet = minBet = BigDecimal.ZERO;
//                        }
//                        result.setMaxBet(maxBet);
//                        result.setMinBet(minBet);
//                    }
//                    queueData.setData(GsonUtil.toJson(result));
//                    break;
//
//                case TaiXiuCommand.UPDATE_CURRENT_MATCH_INFO:
//                    TaiXiuGameInfo info = GsonUtil.fromJson(queueData.getData(), TaiXiuGameInfo.class);
//                    double money = Database.instance.getUserMoney(queueData.getUserId());
//                    BigDecimal maxBet = TaiXiuConfig.getInstance().getMaxBet();
//                    BigDecimal minBet = TaiXiuConfig.getInstance().getMinBet();
//                    if (money < maxBet.doubleValue()) {
//                        maxBet = new BigDecimal(money);
//                    }
//                    if (maxBet.compareTo(minBet) < 0) {
//                        maxBet = minBet = BigDecimal.ZERO;
//                    }
//                    info.setMaxBet(maxBet);
//                    info.setMinBet(minBet);
//                    queueData.setData(GsonUtil.toJson(info));
//                    break;
//                    
//                case TaiXiuCommand.BUY_NOHU_TICKET:
//                    TaiXiuBuyTicketResult buyResult = GsonUtil.fromJson(queueData.getData(), TaiXiuBuyTicketResult.class);
//                    if (queueData.getUserId().equals(buyResult.getUserId())) {
//                        if (buyResult.isSuccess()) {
//                            money = Database.instance.getUserMoney(queueData.getUserId());
//                            maxBet = TaiXiuConfig.getInstance().getMaxBet();
//                            minBet = TaiXiuConfig.getInstance().getMinBet();
//                            if (money < maxBet.doubleValue()) {
//                                maxBet = new BigDecimal(money);
//                            }
//                            if (maxBet.compareTo(minBet) < 0) {
//                               maxBet = minBet = BigDecimal.ZERO;
//                            }
//                            buyResult.setMaxBet(maxBet);
//                            buyResult.setMinBet(minBet);
//                            queueData.setData(GsonUtil.toJson(buyResult));
//                        }
//                    }
//                    break;
//            }

            SFSObject sfsObj = new SFSObject();
            sfsObj.putInt(SFSKey.ACTION_INCORE, SFSAction.PLAY_TAIXIU);
            sfsObj.putByte(SFSKey.COMMAND, queueData.getCommand());
            sfsObj.putUtfString(SFSKey.DATA, queueData.getData());
            SmartFoxServer.getInstance().getAPIManager().getSFSApi().sendExtensionResponse(SFSCommand.CLIENT_REQUEST, sfsObj, user, null, false);

        } catch (Exception e) {
            LOGGER.error("", e);
        }
    }
    
    public void sendRequest(TaiXiuQueueData data) {
        try {
            byte[] bytes = GsonUtil.toJson(data).getBytes(Charset.forName("UTF-8"));
            channel.basicPublish(QueueConfig.getInstance().getExchangeTaiXiu(), QueueLogKey.KEY_TAIXIU_REQUEST, prop, bytes);
        } catch (Exception e) {
            LOGGER.error("QueueTaiXiu.sendRequest", e);
        }
    }
}
