/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.vn.common.queue;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
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
import game.vn.common.config.LeaderBoardConfig;
import game.vn.common.config.QueueConfig;
import game.vn.common.config.ServerConfig;
import game.vn.common.constant.Service;
import game.vn.common.lib.iap.IAPItem;
import game.vn.util.GsonUtil;
import game.vn.util.db.Database;
import java.io.IOException;
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
public class QueueServicePayment {

    private final static Logger LOGGER = LoggerFactory.getLogger(QueueService.class);

    private static final String EXCHANGE_TYPE_FANOUT = "fanout";

    private static final QueueServicePayment INSTANCE = new QueueServicePayment();
    private Connection conn;
    private Channel channel;
    private AMQP.BasicProperties prop;
    private final String consumeTag = "EventConsume" + System.currentTimeMillis();

    public static QueueServicePayment getInstance() {
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
            
            Consumer paymentConsumer = new DefaultConsumer(channel) {
                @Override
                public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties props, byte[] body) throws IOException {
                    AMQP.BasicProperties replyProps = new AMQP.BasicProperties.Builder()
                            .correlationId(props.getCorrelationId())
                            .build();
                    String response = "";
                    
                    try {
                        response = getPaymentConfigData().toString();
                    } catch (RuntimeException e) {
                        LOGGER.error("", e);
                    } finally {
                        channel.basicPublish("", props.getReplyTo(), replyProps, response.getBytes("UTF-8"));
                    }
                    
                }
            };
            
            Consumer updatePointConsumer = new DefaultConsumer(channel) {
                @Override
                public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties props, byte[] body) throws IOException {
                    updatePointConfig(new String(body, "UTF-8"));
                }
            };
            
            Consumer leaderBoardConsumer = new DefaultConsumer(channel) {
                @Override
                public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties props, byte[] body) throws IOException {
                    processLeaderBoardRequest(props);
                }
            };
            
            Consumer updateLeaderBoardConsumer = new DefaultConsumer(channel) {
                @Override
                public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties props, byte[] body) throws IOException {
                    updateLeaderBoardConfig(new String(body, "UTF-8"));
                }
            };
            
            int serverId = ServerConfig.getInstance().getServerId();
            String exchangePoint = QueueConfig.getInstance().getExchangeUpdatePoint();
            String exchangeLeaderBoard = QueueConfig.getInstance().getExchangeUpdateLeaderBoard();

            channel = conn.createChannel();
            channel.exchangeDeclare(exchangePoint, EXCHANGE_TYPE_FANOUT);
            channel.exchangeDeclare(exchangeLeaderBoard, EXCHANGE_TYPE_FANOUT);
            
            channel.queueDeclare(QueueConfig.getInstance().getQueuePaymentConfig(), true, false, false, null);
            channel.queueDeclare(QueueConfig.getInstance().getQueueLeaderboardConfig(), true, false, false, null);
            channel.queueDeclare(QueueConfig.getInstance().getQueueUpdatePoint() + serverId, true, false, false, null);
            channel.queueDeclare(QueueConfig.getInstance().getQueueUpdateLeaderboard() + serverId, true, false, false, null);

            channel.queueBind(QueueConfig.getInstance().getQueueUpdatePoint() + serverId, exchangePoint, "");
            channel.queueBind(QueueConfig.getInstance().getQueueUpdateLeaderboard() + serverId, exchangeLeaderBoard, "");
            
            channel.basicConsume(QueueConfig.getInstance().getQueueLeaderboardConfig(), true, leaderBoardConsumer);
            channel.basicConsume(QueueConfig.getInstance().getQueuePaymentConfig(), true, paymentConsumer);
            channel.basicConsume(QueueConfig.getInstance().getQueueUpdatePoint() + serverId, true, updatePointConsumer);
            channel.basicConsume(QueueConfig.getInstance().getQueueUpdateLeaderboard() + serverId, true, updateLeaderBoardConsumer);
            
            AMQP.BasicProperties.Builder builder = new AMQP.BasicProperties.Builder();
            prop = builder.deliveryMode(2).build();
            
            LOGGER.info("---------------- QueueServicePayment init done, connected to: " + conn.getAddress() + ":" + conn.getPort() + " ----------------");
        } catch (Exception ex) {
            LOGGER.error("QueueServicePayment init error", ex);
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
     * @return
     */
    private JsonObject getPaymentConfigData() {

        JsonObject pointJson = new JsonObject();
        pointJson.addProperty("freeEnable", ServerConfig.getInstance().isPointFreeEnable());
        pointJson.addProperty("freeTime", ServerConfig.getInstance().getTimeReceivePointFree());
        pointJson.addProperty("freePoint", ServerConfig.getInstance().getPointFree());
        pointJson.addProperty("freeCount", ServerConfig.getInstance().getCountFree());
        pointJson.addProperty("videoEnable", ServerConfig.getInstance().isPointVideoEnable());
        pointJson.addProperty("videoTime", ServerConfig.getInstance().getTimeReceivePointVideo());
        pointJson.addProperty("videoPoint", ServerConfig.getInstance().getPointVideo());
        pointJson.addProperty("videoCount", ServerConfig.getInstance().getCountVideo());

        JsonArray androidJson = new JsonArray();
        List<IAPItem> items = Database.instance.getListIAPItem("android");
        if (items != null && !items.isEmpty()) {
            for (IAPItem item : items) {
                JsonObject jsonItem = new JsonObject();
                jsonItem.addProperty("id", item.getId());
                jsonItem.addProperty("money", item.getMoney());
                jsonItem.addProperty("point", item.getPoint());
                jsonItem.addProperty("promotion", item.isSpecial());
                jsonItem.addProperty("enable", item.isEnable());
                jsonItem.addProperty("image", item.getUrl());
                jsonItem.addProperty("image_en", item.getUrlEn());
                androidJson.add(jsonItem);
            }
        }

        JsonArray iosJson = new JsonArray();
        items = Database.instance.getListIAPItem("ios");
        if (items != null && !items.isEmpty()) {
            for (IAPItem item : items) {
                JsonObject jsonItem = new JsonObject();
                jsonItem.addProperty("id", item.getId());
                jsonItem.addProperty("money", item.getMoney());
                jsonItem.addProperty("point", item.getPoint());
                jsonItem.addProperty("promotion", item.isSpecial());
                jsonItem.addProperty("enable", item.isEnable());
                jsonItem.addProperty("image", item.getUrl());
                jsonItem.addProperty("image_en", item.getUrlEn());
                iosJson.add(jsonItem);
            }
        }

        JsonObject json = new JsonObject();
        json.add("pointConfig", pointJson);
        json.add("inappIos", iosJson);
        json.add("inappAndroid", androidJson);
        return json;
    }

    /**
     *
     * @param json
     */
    private void updatePointConfig(String data) {
        try {
            JsonObject json = GsonUtil.parse(data).getAsJsonObject();
            boolean enableFree = json.get("enableFree").getAsBoolean();
            boolean enableVideo = json.get("enableVideo").getAsBoolean();
            int freeTime = json.get("freeTime").getAsInt();
            int freePoint = json.get("freePoint").getAsInt();
            int videoTime = json.get("videoTime").getAsInt();
            int videoPoint = json.get("videoPoint").getAsInt();
            int countFree = json.get("freeCount").getAsInt();
            int countVideo = json.get("videoCount").getAsInt();
            
            ServerConfig.getInstance().updateProperties("point.free.enable", String.valueOf(enableFree));
            ServerConfig.getInstance().updateProperties("point.time.free", String.valueOf(freeTime));
            ServerConfig.getInstance().updateProperties("point.free", String.valueOf(freePoint));
            ServerConfig.getInstance().updateProperties("point.count.free", String.valueOf(countFree));
            ServerConfig.getInstance().updateProperties("point.video.enable", String.valueOf(enableVideo));
            ServerConfig.getInstance().updateProperties("point.time.video", String.valueOf(videoTime));
            ServerConfig.getInstance().updateProperties("point.video", String.valueOf(videoPoint));
            ServerConfig.getInstance().updateProperties("point.count.video", String.valueOf(countVideo));
            

            ServerConfig.getInstance().save();
        } catch (Exception e) {
            LOGGER.error("QueueServicePayment.updatePointConfig", e);
        }
    }

    /**
     *
     * @param bytes
     */
    private void processLeaderBoardRequest(AMQP.BasicProperties props) throws IOException {
        AMQP.BasicProperties replyProps = new AMQP.BasicProperties.Builder()
                .correlationId(props.getCorrelationId())
                .build();
        String response = "";

        try {
            response = getLeaderBoardConfig().toString();
        } catch (RuntimeException e) {
            LOGGER.error("", e);
        } finally {
            channel.basicPublish("", props.getReplyTo(), replyProps, response.getBytes("UTF-8"));
        }
    }

    /**
     *
     * @param json
     */
    private void updateLeaderBoardConfig(String data) {
        try {
            JsonObject json = GsonUtil.parse(data).getAsJsonObject();
            if (json.has("enable")) {
                boolean enable = json.get("enable").getAsBoolean();
                LeaderBoardConfig.getInstance().updateProperties("enable", String.valueOf(enable));
                LeaderBoardConfig.getInstance().save();
                if (!enable) {
                    return;
                }
            }

            JsonArray games = json.get("games").getAsJsonArray();
            for (JsonElement e : games) {
                JsonObject game = e.getAsJsonObject();
                byte gameId = game.get("gameId").getAsByte();
                LeaderBoardConfig.getInstance().updateProperties(String.valueOf(gameId), String.valueOf(game.get("enable").getAsBoolean()));
                LeaderBoardConfig.getInstance().save();
            }
        } catch (Exception e) {
            LOGGER.error("QueueServicePayment.updateLeaderBoardConfig", e);
        }

    }

    /**
     *
     * @return
     */
    private JsonObject getLeaderBoardConfig() {
        JsonObject json = new JsonObject();
        byte[] gameIds = new byte[]{Service.BAI_CAO, Service.TIENLEN, Service.MAUBINH, Service.BLACKJACK, Service.PHOM, Service.SAM, Service.XI_TO, Service.LIENG};
        json.addProperty("enable", LeaderBoardConfig.getInstance().getBooleanAttribute("enable"));
        JsonArray arr = new JsonArray();
        for (byte gameId : gameIds) {
            JsonObject jsonGame = new JsonObject();
            jsonGame.addProperty("gameId", gameId);
            jsonGame.addProperty("name", Service.getSeviceName(gameId));
            jsonGame.addProperty("enable", LeaderBoardConfig.getInstance().getBooleanAttribute(String.valueOf(gameId), true));
            arr.add(jsonGame);
        }
        json.add("games", arr);

        return json;
    }
}
