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
 */
public class QueueHistory {
    
    private static final Logger LOG = LoggerFactory.getLogger(QueueService.class);
    private static final QueueHistory INSTANCE = new QueueHistory();
    
    private Channel channel;
    private Connection conn;
    private AMQP.BasicProperties prop;
    
    public static QueueHistory instance(){
        return INSTANCE;
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
                    LOG.warn("------------ RabbitMQ Service shutdown complete: " + paramShutdownSignalException);
                }
            });

            channel = conn.createChannel();           
            
            String exchangeHistory = QueueConfig.getInstance().getExchangeHistory();
            channel.exchangeDeclare(exchangeHistory, QueueService.EXCHANGE_TYPE);
            
        } catch (IOException e) {
            LOG.error("QueueHistory.init()", e);
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
    
    public void sendData( Object obj) {
        try {
            String data = GsonUtil.toJson(obj);
            LOG.info(data);
            channel.basicPublish(QueueConfig.getInstance().getExchangeHistory(), QueueLogKey.KEY_INSERT_HISTORY, prop, data.getBytes(Charset.forName("UTF-8")));
        } catch (IOException ex) {
            LOG.error("-----QueueHistory sendData() error:", ex);
        }
    }
}
