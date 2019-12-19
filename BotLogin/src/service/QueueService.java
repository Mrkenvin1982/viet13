/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package service;

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
import constant.Constant;
import domain.UserJoinGameInfo;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.Configs;
import util.GsonUtil;

/**
 *
 * @author hanv
 */
public class QueueService {
    private final static Logger LOGGER = LoggerFactory.getLogger(QueueService.class);

    private static final QueueService instance = new QueueService();
    private Connection conn;
    private Channel channel;
    private AMQP.BasicProperties prop;
    private final String consumeTag = "notifyConsume" + System.currentTimeMillis();
    private final String QUEUE_NOTIFY = "QUEUE_NOTIFY_BOT";
    private final String EXCHANGE_NOTIFY = "EXCHANGE_NOTIFY";
    private final String KEY_USER_JOIN_GAME = "KEY_USER_JOIN_GAME";

    public static QueueService getInstance() {
        return instance;
    }
    
    public void init() {
        try {
            ConnectionFactory factory = new ConnectionFactory();
            factory.setUsername(Configs.getInstance().queueUsername());
            factory.setPassword(Configs.getInstance().queuePassword());
            factory.setAutomaticRecoveryEnabled(true);
            
            ExecutorService es = Executors.newFixedThreadPool(Configs.getInstance().queuePoolSize());
            conn = factory.newConnection(es, getHosts());
            conn.addShutdownListener(new ShutdownListener() {
                @Override
                public void shutdownCompleted(ShutdownSignalException paramShutdownSignalException) {
                    LOGGER.warn("------------ RabbitMQ Service shutdown complete: " + paramShutdownSignalException);
                }
            });
            
            Consumer consumer = new DefaultConsumer(channel) {
                @Override
                public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties props, byte[] body) throws IOException {
                    processMessage(envelope.getRoutingKey(), body);
                }
            };
            
            String queue = QUEUE_NOTIFY + Configs.getInstance().queueSuffix();
            channel = conn.createChannel();
            channel.queueDeclare(queue, true, false, false, null);
            channel.queueBind(queue, EXCHANGE_NOTIFY, KEY_USER_JOIN_GAME);
            channel.basicConsume(queue, true, consumer);
            
            AMQP.BasicProperties.Builder builder = new AMQP.BasicProperties.Builder();
            prop = builder.deliveryMode(2).build();
            
            LOGGER.info("---------------- QueueNotify init done, connected to: " + conn.getAddress() + ":" + conn.getPort() + " ----------------");
        } catch (Exception ex) {
            LOGGER.error("QueueNotify init error", ex);
        }
    }

    private Address[] getHosts() {
        List<String> listHost = Arrays.asList(Configs.getInstance().queueHost().split(";"));
        if (listHost.size() > 0) {
            Address[] hosts = new Address[listHost.size()];
            for (int i = 0; i < listHost.size(); i++) {
                hosts[i] = new Address(listHost.get(i), Configs.getInstance().queuePort());
            }
            return hosts;
        }
        return null;
    }

    private void processMessage(String routingKey, byte[] body) {
        try {
            String data = new String(body, "UTF-8");
            switch (routingKey) {
                case KEY_USER_JOIN_GAME:
                    UserJoinGameInfo info = GsonUtil.fromJson(data, UserJoinGameInfo.class);
                    if (info.getUserType() == Constant.USER_TYPE_NORMAL) {
                        BotManager.getInstance().callBot(info.getRoomName(), info.getBetBoard(), info.getMoneyType(), info.getServiceId());
                    }
                    break;
            }
        } catch (Exception e) {
            LOGGER.error("processMessage", e);
        }
    }

}
