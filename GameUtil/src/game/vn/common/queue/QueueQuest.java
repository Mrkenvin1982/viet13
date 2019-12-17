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
import game.vn.common.lib.event.EventData;
import game.vn.util.GsonUtil;
import game.vn.util.Utils;
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
public class QueueQuest {
    private final static Logger LOGGER = LoggerFactory.getLogger(QueueService.class);
    private static final String EXCHANGE_TYPE = "direct";
    
    private static final QueueQuest INSTANCE = new QueueQuest();
    private Connection conn;
    private Channel channel;
    private AMQP.BasicProperties prop;
    
    public static QueueQuest getInstance() {
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
                    LOGGER.warn("------------ RabbitMQ Service shutdown complete: " + paramShutdownSignalException);
                }
            });
            
            Consumer questConsumer = new DefaultConsumer(channel) {
                @Override
                public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties props, byte[] body) throws IOException {
                    processQuestResponse(new String(body, "UTF-8"));
                }
            };

            int serverId = ServerConfig.getInstance().getServerId();
            String queueQuestRequest = QueueConfig.getInstance().getQueueQuestRequest();
            String queueQuestResponse = QueueConfig.getInstance().getQueueQuestResponse() + serverId;
            String exchangeQuest = QueueConfig.getInstance().getExchangeQuest();
            
            channel = conn.createChannel();
            channel.exchangeDeclare(exchangeQuest, EXCHANGE_TYPE);
            channel.queueDeclare(queueQuestRequest, true, false, false, null);
            channel.queueDeclare(queueQuestResponse, true, false, false, null);
            channel.queueBind(queueQuestRequest, exchangeQuest, QueueLogKey.KEY_QUEST_REQUEST);
            channel.queueBind(queueQuestResponse, exchangeQuest, QueueLogKey.KEY_QUEST_RESPONSE + serverId);
            channel.basicConsume(queueQuestResponse, true, questConsumer);

            AMQP.BasicProperties.Builder builder = new AMQP.BasicProperties.Builder();
            prop = builder.deliveryMode(2).build();

            LOGGER.info("---------------- QueueQuest init done, connected to: " + conn.getAddress() + ":" + conn.getPort() + " ----------------");
        } catch (Exception ex) {
            LOGGER.error("QueueQuest init error", ex);
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

    public void sendQuestRequest(EventData eventData) {
        try {
            byte[] bytes = GsonUtil.toJson(eventData).getBytes(Charset.forName("UTF-8"));
            channel.basicPublish(QueueConfig.getInstance().getExchangeQuest(), QueueLogKey.KEY_QUEST_REQUEST, prop, bytes);
        } catch (Exception e) {
            LOGGER.error("sendQuestRequest", e);
        }
    }

    private void processQuestResponse(String data) {
        try {
            LOGGER.debug("quest response: " + data);
            EventData response = GsonUtil.fromJson(data, EventData.class);
            User user = Utils.findUser(response.getUserId());
            if (user == null) {
                LOGGER.info("quest user not found: " + response.getUserId());
                return;
            }
            SFSObject sfsObj = new SFSObject();
            sfsObj.putInt(SFSKey.ACTION_INCORE, SFSAction.REQUEST_EVENT);
            sfsObj.putInt(SFSKey.COMMAND, response.getCmd());
            sfsObj.putUtfString(SFSKey.DATA, response.getData());
            SmartFoxServer.getInstance().getAPIManager().getSFSApi().sendExtensionResponse(SFSCommand.CLIENT_REQUEST,
                    sfsObj, user, null, false);
        } catch (Exception e) {
            LOGGER.error("processQuestResponse", e);
        }
    }
}
