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
import game.vn.common.lib.event.UserCardsObj;
import game.vn.util.GsonUtil;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author tuanp
 */
public class QueueServiceEvent {
    
    private final static Logger LOGGER = LoggerFactory.getLogger(QueueService.class);
    public static final String EXCHANGE_TYPE = "direct";

    private static final QueueServiceEvent _instance = new QueueServiceEvent();
    private Connection conn;
    private Channel channel;
    private AMQP.BasicProperties prop;
    
    public static QueueServiceEvent getInstance() {
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

            String exchangeUserQuestData = QueueConfig.getInstance().getExchangeUserQuestData();

            channel = conn.createChannel();
            channel.exchangeDeclare(exchangeUserQuestData, EXCHANGE_TYPE);

            channel.queueDeclare(QueueConfig.getInstance().getQueueUserQuestData(), true, false, false, null);

            channel.queueBind(QueueConfig.getInstance().getQueueUserQuestData(), exchangeUserQuestData, QueueLogKey.KEY_EVENT_CARD_DATA);
            channel.queueBind(QueueConfig.getInstance().getQueueUserQuestData(), exchangeUserQuestData, QueueLogKey.KEY_EVENT_VERIFY_USER);

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
     * @param infor
     */
    public void sendEventInfor(UserCardsObj infor) {
        if(infor == null) {
            throw new Error("Data log is empty!");
        }

        try {
            byte[] bytes = GsonUtil.toJson(infor).getBytes(Charset.forName("UTF-8"));
            channel.basicPublish(QueueConfig.getInstance().getExchangeUserQuestData(), QueueLogKey.KEY_EVENT_CARD_DATA, prop, bytes);
        } catch (Exception e) {
            LOGGER.error("QueueService.sendEventInfor() error:", e);
        }
    }

    public void sendUserVerifyInfo(Object obj) {
        try {
            byte[] bytes = GsonUtil.toJson(obj).getBytes(Charset.forName("UTF-8"));
            channel.basicPublish(QueueConfig.getInstance().getExchangeUserQuestData(), QueueLogKey.KEY_EVENT_VERIFY_USER, prop, bytes);
        } catch (Exception e) {
            LOGGER.error("sendUserVerifyInfo", e);
        }
    }
    public void sendUserRegisterInfo(Object obj) {
        try {
            byte[] bytes = GsonUtil.toJson(obj).getBytes(Charset.forName("UTF-8"));
            channel.basicPublish(QueueConfig.getInstance().getExchangeUserQuestData(), QueueLogKey.KEY_EVENT_REGISTER_USER, prop, bytes);
        } catch (Exception e) {
            LOGGER.error("sendUserVerifyInfo", e);
        }
    }
}
