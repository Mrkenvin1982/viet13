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
import game.vn.util.GsonUtil;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Queue sử dụng tương tác với TTKT
 * @author tuanp
 */
public class QueueServiceApi {
    private final static Logger LOGGER = LoggerFactory.getLogger(QueueService.class);

    private static final QueueServiceApi _instance = new QueueServiceApi();
    private Connection conn;
    private Channel channel;
    private AMQP.BasicProperties prop;
    public static QueueServiceApi getInstance(){
        return _instance;
    }
    
    public void init() {
        try {
            ConnectionFactory factory = new ConnectionFactory();
            factory.setUsername(QueueConfig.getInstance().getQueueApiUserName());
            factory.setPassword(QueueConfig.getInstance().getQueueApiPassWord());
            factory.setAutomaticRecoveryEnabled(true);
            ExecutorService es = Executors.newFixedThreadPool(QueueConfig.getInstance().getQueueApiPoolSize());
            conn = factory.newConnection(es, getHosts());
            conn.addShutdownListener(new ShutdownListener() {
                @Override
                public void shutdownCompleted(ShutdownSignalException paramShutdownSignalException) {
                    LOGGER.warn("------------ RabbitMQ Service shutdown complete: " + paramShutdownSignalException);
                }
            });
            
            channel = conn.createChannel();
            
            AMQP.BasicProperties.Builder builder = new AMQP.BasicProperties.Builder();
            prop = builder.deliveryMode(2).build();
            
            LOGGER.info("---------------- QueueServiceApi init done, connected to: " + conn.getAddress() + ":" + conn.getPort() + " ----------------");
        } catch (Exception ex) {
            LOGGER.error("QueueServiceApi init error", ex);
        }
    }

    private Address[] getHosts() {
        List<String> listHost = Arrays.asList(QueueConfig.getInstance().getQueueApiHost().split(";"));
        if (listHost.size() > 0) {
            Address[] queueHosts = new Address[listHost.size()];
            for (int i = 0; i < listHost.size(); i++) {
                queueHosts[i] = new Address(listHost.get(i), QueueConfig.getInstance().getQueueApiPort());
            }
            return queueHosts;
        }
        return null;
    }

    public void sendData(String routingKey, boolean isPersistance, Object obj) {
        try {
            String data = GsonUtil.toJson(obj);
            LOGGER.info(data);
            channel.basicPublish(QueueConfig.getInstance().getQueueApiExchange(), routingKey, isPersistance ? prop : null, data.getBytes(Charset.forName("UTF-8")));
        } catch (Exception ex) {
            LOGGER.error("-----RabbitMQ error:", ex);
        }
    }

}
