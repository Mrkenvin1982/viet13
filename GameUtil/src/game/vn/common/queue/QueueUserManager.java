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
import com.smartfoxserver.v2.api.ISFSApi;
import com.smartfoxserver.v2.entities.User;
import com.smartfoxserver.v2.entities.Zone;
import game.vn.common.config.QueueConfig;
import game.vn.common.config.ServerConfig;
import game.vn.common.lib.hazelcast.PlayingBoardManager;
import game.vn.common.lib.hazelcast.UserState;
import game.vn.common.lib.usermanager.BanUserInfor;
import game.vn.common.lib.usermanager.KickUserInfor;
import game.vn.util.GsonUtil;
import game.vn.util.HazelcastUtil;
import game.vn.util.db.Database;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Quản lý user từ tool insight chổ này
 * @author tuanp
 */
public class QueueUserManager {
    
    private static final Logger LOG = LoggerFactory.getLogger(QueueService.class);
    private static final QueueUserManager INSTANCE = new QueueUserManager();
    private Channel channel;
    private Connection conn;
    private AMQP.BasicProperties prop;
    private ISFSApi api;
    private Zone zone;
    
    private static final int BAN_USER=1;
    private static final int UNBAN_USER=0;
       
    public static QueueUserManager instance(){
        return INSTANCE;
    }
    public void init(ISFSApi apiInput, Zone zoneInput){
        try {
            this.api = apiInput;
            this.zone = zoneInput;
            ConnectionFactory factory = new ConnectionFactory();
            factory.setUsername(QueueConfig.getInstance().getQueueUsername());
            factory.setPassword(QueueConfig.getInstance().getQueuePassword());
            factory.setAutomaticRecoveryEnabled(true);
            ExecutorService es = Executors.newFixedThreadPool(QueueConfig.getInstance().getQueuePoolSize());
            conn = factory.newConnection(es, getHosts());
            conn.addShutdownListener(new ShutdownListener() {
                @Override
                public void shutdownCompleted(ShutdownSignalException paramShutdownSignalException) {
                    LOG.warn("------------ RabbitMQ Service shutdown complete: " + paramShutdownSignalException);
                }
            });
            
            int serverId = ServerConfig.getInstance().getServerId();
            String exchange = QueueConfig.getInstance().getExchangeUserManager();
            String queueKickUser = QueueConfig.getInstance().getQueueKickUser() + serverId;
            
            channel = conn.createChannel();            
            channel.exchangeDeclare(exchange, QueueService.EXCHANGE_TYPE_FANOUT);
            
            channel.queueDeclare(queueKickUser, true, false, false, null);
            channel.queueDeclare(QueueConfig.getInstance().getQueueRemoveUserHazelcast(), true, false, false, null);
            
            channel.queueBind(queueKickUser, exchange, QueueLogKey.KEY_KICK_USER);
            channel.queueBind(queueKickUser, exchange, QueueLogKey.KEY_KICK_ALL_USER);
            channel.queueBind(queueKickUser, exchange, QueueLogKey.KEY_BAN_USER);
            
            AMQP.BasicProperties.Builder builder = new AMQP.BasicProperties.Builder();
            prop = builder.deliveryMode(2).build();

            LOG.info("---------------- QueueUserManager init done, connected to: " + conn.getAddress() + ":" + conn.getPort() + " ----------------");
        } catch (Exception e) {
            LOG.error("QueueUserManager init error: ", e);
        }
    }

    public void consume() {

        //kick user
        Consumer consumer1 = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties props, byte[] body) throws IOException {
                try {
                    LOG.debug(" [x] Received '" + envelope + "'");
                    switch (envelope.getRoutingKey()) {
                        case QueueLogKey.KEY_KICK_USER:
                            KickUserInfor kickUserinfor = GsonUtil.fromJson(new String(body, "UTF-8"), KickUserInfor.class);
                            if (kickUserinfor != null) {
                                kickUser(kickUserinfor.getIdDBUser(), kickUserinfor.getReason());
                            }
                            break;

                        case QueueLogKey.KEY_KICK_ALL_USER:
                            kickAllUser();
                            break;
                        case QueueLogKey.KEY_BAN_USER:
                            BanUserInfor banUserinfor = GsonUtil.fromJson(new String(body, "UTF-8"), BanUserInfor.class);
                            if (banUserinfor != null) {
                                banUser(banUserinfor.getIdDBUser(), banUserinfor.getReason(), banUserinfor.getType());
                            }
                            break;
                    }

                } catch (Exception e) {
                    LOG.error("update config game error: ", e);
                }
            }
        };

        //remove hazelcast of user
        Consumer consumer2 = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties props, byte[] body) throws IOException {
                AMQP.BasicProperties replyProps = new AMQP.BasicProperties.Builder()
                        .correlationId(props.getCorrelationId())
                        .build();
                String userId = "";
                try {
                    userId = new String(body, "UTF-8");

                    HazelcastUtil.removeUserInfo(userId);
                    HazelcastUtil.removeUserState(userId);
                    UserState userState = HazelcastUtil.getUserState(userId);
                    if (userState != null && userState.getLoginToken() != null) {
                        HazelcastUtil.removeUserLoginToken(userState.getLoginToken());
                    }

                    PlayingBoardManager playingBoard = HazelcastUtil.getPlayingBoard(userId);
                    if (playingBoard != null) {
                        HazelcastUtil.removePlayingBoard(userId);
                    }
                } catch (UnsupportedEncodingException e) {
                    LOG.error("get list service error:", e);
                } finally {
                    String infor = "Remove user id=" + userId + " successful.";
                    channel.basicPublish("", props.getReplyTo(), replyProps, infor.getBytes("UTF-8"));
                }
            }
        };

        
        try {
            channel.basicConsume(QueueConfig.getInstance().getQueueKickUser() + ServerConfig.getInstance().getServerId(), true, consumer1);
            channel.basicConsume(QueueConfig.getInstance().getQueueRemoveUserHazelcast(), true, consumer2);
            
        } catch (IOException ex) {
            LOG.error("QueueUpdateConfig.consume()", ex);
        }
    }
    
    /**
     * kick tất cả user trong server
     */
    private void kickAllUser() {
        for (User u : this.zone.getUserList()) {
            this.api.kickUser(u, null, "", 1);
        }
    }
    
     /**
     * Kick user khỏi server hiện tại
     * @param IdDBUser
     * @param reason 
     */  
    private void banUser(String IdDBUser, String reason, int type) {
        User user = this.api.getUserByName(IdDBUser);
        if (user != null) {
            this.api.kickUser(user, null, reason, 1);
        }
        Database.instance.banUser(IdDBUser,type);
    }
    
    /**
     * Kick user khỏi server hiện tại
     * @param IdDBUser
     * @param reason 
     */  
    private void kickUser(String IdDBUser, String reason) {
        User user = this.api.getUserByName(IdDBUser);
        if (user != null) {
            this.api.kickUser(user, null, reason, 1);
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
    
    public void sendData(String exchangeName, String routingKey, boolean isPersistance, byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            throw new Error("Data is empty!");
        }
        try {
            channel.basicPublish(exchangeName, routingKey, isPersistance ? prop : null, bytes);
        } catch (Exception ex) {
            LOG.error("-----RabbitMQ error:", ex);
        }
    }


}
