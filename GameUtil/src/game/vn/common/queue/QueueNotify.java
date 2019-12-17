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
import game.vn.common.constant.ExtensionConstant;
import game.vn.common.lang.GameLanguage;
import game.vn.common.lib.api.UpdateMoneyInfo;
import game.vn.common.lib.api.UpdateUserType;
import game.vn.common.lib.contants.MoneyContants;
import game.vn.common.lib.event.EventData;
import game.vn.common.lib.news.News;
import game.vn.common.message.MessageController;
import game.vn.common.object.UserJoinGameInfo;
import game.vn.common.service.BroadcastService;
import game.vn.util.GsonUtil;
import game.vn.util.HazelcastUtil;
import game.vn.util.Utils;
import game.vn.util.db.Database;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * queue nhận thông tin notify từ backend, money, ranking ...,
 * @author hanv
 */
public class QueueNotify {
    private final static Logger LOGGER = LoggerFactory.getLogger(QueueService.class);

    public static final String EXCHANGE_TYPE_FANOUT = "fanout";
    
    private static final QueueNotify instance = new QueueNotify();
    private Connection conn;
    private Channel channel;

    public static QueueNotify getInstance() {
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
            
            Consumer consumer = new DefaultConsumer(channel) {
                @Override
                public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties props, byte[] body) {
                    processMessage(envelope.getRoutingKey(), body);
                }
            };
            
            int serverId = ServerConfig.getInstance().getServerId();
            String queue = QueueConfig.getInstance().getQueueNotify() + serverId;
            String exchange = QueueConfig.getInstance().getExchangeNotify();
            
            channel = conn.createChannel();
            channel.exchangeDeclare(exchange, EXCHANGE_TYPE_FANOUT);
            channel.queueDeclare(queue, true, false, false, null);
            channel.queueBind(queue, exchange, "");
            channel.basicConsume(queue, true, consumer);
            
            LOGGER.info("---------------- QueueNotify init done, connected to: " + conn.getAddress() + ":" + conn.getPort() + " ----------------");
        } catch (Exception ex) {
            LOGGER.error("QueueNotify init error", ex);
        }
    }

    private Address[] getHosts() {
        List<String> listHost = Arrays.asList(QueueConfig.getInstance().getQueueHost().split(";"));
        if (listHost.size() > 0) {
            Address[] hosts = new Address[listHost.size()];
            for (int i = 0; i < listHost.size(); i++) {
                hosts[i] = new Address(listHost.get(i), QueueConfig.getInstance().getQueuePort());
            }
            return hosts;
        }
        return null;
    }

    private void processMessage(String routingKey, byte[] body) {
        try {
            String data = new String(body, "UTF-8");
            LOGGER.debug("notify data: " + data);
            switch (routingKey) {
                case QueueLogKey.KEY_NOTIFY_NEWS:
                    News news = GsonUtil.fromJson(data, News.class);
                    SFSObject obj = new SFSObject();
                    obj.putInt(SFSKey.ACTION_INCORE, SFSAction.SYSTEM_MESSAGE);
                    obj.putUtfString(SFSKey.MESSAGE, news.getContent());
                    BroadcastService.broadcast(SFSCommand.CLIENT_REQUEST, obj);
                    break;

                case QueueLogKey.KEY_UPDATE_MONEY:
                    UpdateMoneyInfo info = GsonUtil.fromJson(data, UpdateMoneyInfo.class);
                    String userId = info.getUserId();
                    User user = Utils.findUser(userId);
                    if (user == null) {
                        return;
                    }

                    if (info.getMoneyType() == MoneyContants.MONEY) {
                        double money = Database.instance.getUserMoney(userId);
                        Utils.updateMoneyOfUser(user, money);
                    } else {
                        double point = Database.instance.getUserPoint(userId);
                        Utils.updatePointOfUser(user, point);
                    }
                    break;
                    
                   case QueueLogKey.KEY_UPDATE_USER_TYPE:
                    UpdateUserType uut = GsonUtil.fromJson(data, UpdateUserType.class);
                    String uId = uut.getUserId();
                    int userType = uut.getUserType();
                    User u = Utils.findUser(uId);
                    if (u == null) {
                        return;
                    }

                    Utils.updateUserType(u, userType);
                    break;

                case QueueLogKey.KEY_CHARGE_BTC:
                    String[] tmp = data.split(";");
                    userId = tmp[0];
                    user = Utils.findUser(userId);
                    if (user == null) {
                        return;
                    }
                    
                    double money = Database.instance.getUserMoney(userId);
                    Utils.updateMoneyOfUser(user, money);

                    String unit = GameLanguage.getMessage(GameLanguage.NAME_MONEY, Utils.getUserLocale(user));
                    String msg = GameLanguage.getMessage(GameLanguage.BTC_CHARGE, Utils.getUserLocale(user));
                    msg = String.format(msg, tmp[1], unit);
                    SFSObject sfsObj = MessageController.getToastMessage(msg, 0);
                    SmartFoxServer.getInstance().getAPIManager().getSFSApi().sendExtensionResponse(SFSCommand.CLIENT_REQUEST, sfsObj, user, null, false);
                    break;

                case QueueLogKey.KEY_BROADCAST:
                    if (HazelcastUtil.serverType == ExtensionConstant.SERVER_TYPE_LOGIN) {
                        EventData evData = GsonUtil.fromJson(data, EventData.class);
                        obj = new SFSObject();
                        obj.putInt(SFSKey.ACTION_INCORE, SFSAction.BROADCAST);
                        obj.putUtfString(SFSKey.DATA, evData.getData());
                        if (evData.getUserId() == null) {
                            BroadcastService.broadcast(SFSCommand.CLIENT_REQUEST, obj);
                        } else {
                            String[] userIds = evData.getUserId().split(";");
                            for (String id : userIds) {
                                user = Utils.findUser(id);
                                if (user != null) {
                                    SmartFoxServer.getInstance().getAPIManager().getSFSApi().sendExtensionResponse(SFSCommand.CLIENT_REQUEST, obj, user, null, false);
                                }
                            }
                        }
                    }
                    break;
            }
        } catch (Exception e) {
            LOGGER.error("QueueNotify.processMessage", e);
        }
    }

    public void notifyUserLogin(String userId) {
        try {
            channel.basicPublish(QueueConfig.getInstance().getExchangeNotify(), QueueLogKey.KEY_NOTIFY_USER_LOGIN, null, userId.getBytes());
        } catch (Exception e) {
            LOGGER.error("notifyUserLogin", e);
        }
    }
    
    public void notifyUserJoinGame(UserJoinGameInfo user) {
        try {
            byte[] bytes = GsonUtil.toJson(user).getBytes(Charset.forName("UTF-8"));
            channel.basicPublish(QueueConfig.getInstance().getExchangeNotify(), QueueLogKey.KEY_USER_JOIN_GAME, null, bytes);
        } catch (Exception e) {
            LOGGER.error("notifyUserLogin", e);
        }
    }
}
