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
import com.rabbitmq.client.ShutdownListener;
import com.rabbitmq.client.ShutdownSignalException;
import game.vn.common.config.QueueConfig;
import game.vn.common.lib.ranking.GameDataObj;
import game.vn.common.lib.vip.UserTaxData;
import java.io.IOException;
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
public class QueueServiceVip {

    private final static Logger LOGGER = LoggerFactory.getLogger(QueueService.class);    
    private static final MessagePack MSG_PACK = new MessagePack();

    public static final String EXCHANGE_TYPE_FANOUT = "fanout";
    public static final String EXCHANGE_TYPE_DIRECT = "direct";

    private static final QueueServiceVip _instance = new QueueServiceVip();
    private Connection conn;
    private Channel channel;
    private AMQP.BasicProperties prop;    

    public static QueueServiceVip getInstance() {
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
            
            channel = conn.createChannel();
            channel.exchangeDeclare(QueueConfig.getInstance().getExchangeVipRequest(), EXCHANGE_TYPE_DIRECT);
            
            channel.queueBind(QueueConfig.getInstance().getQueueVipRequest(), QueueConfig.getInstance().getExchangeVipRequest(), QueueLogKey.KEY_DATA_USER_VIP);
            
            AMQP.BasicProperties.Builder builder = new AMQP.BasicProperties.Builder();
            prop = builder.deliveryMode(2).build();
            
            LOGGER.info("---------------- QueueServiceVip init done, connected to: " + conn.getAddress() + ":" + conn.getPort() + " ----------------");
        } catch (Exception ex) {
            LOGGER.error("QueueServiceVip init error", ex);
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
    
    public void sendVipData(UserTaxData obj) {
        try {
            byte[] bytes = MSG_PACK.write(obj);
            channel.basicPublish(QueueConfig.getInstance().getExchangeVipRequest(), QueueLogKey.KEY_DATA_USER_VIP, prop, bytes);
        } catch (Exception e) {
            LOGGER.error("QueueService.sendVipData", e);
        }
    }

    /**
     * game gui data khi ket thuc van qua queue
     *
     * @param obj
     */
    public void sendRankingData(GameDataObj obj) {
        try {
            byte[] bytes = MSG_PACK.write(obj);
            channel.basicPublish(QueueConfig.getInstance().getExchangeRankingData(), QueueLogKey.KEY_PLAY_GAME_DATA, prop, bytes);
        } catch (Exception e) {
            LOGGER.error("QueueService.sendRankingData", e);
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

}
