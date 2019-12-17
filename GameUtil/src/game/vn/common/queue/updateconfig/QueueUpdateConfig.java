/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.vn.common.queue.updateconfig;

import game.vn.common.config.TurnOffGameConfig;
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
import game.vn.common.config.QueueConfig;
import game.vn.common.config.RoomConfig;
import game.vn.common.config.ServerConfig;
import game.vn.common.constant.MoneyContants;
import game.vn.common.constant.Service;
import game.vn.common.lib.poker.spinandgo.GameSpinAndGoConfigInfor;
import game.vn.common.lib.updateconfig.GameConfigInfor;
import game.vn.common.lib.updateconfig.GameInfor;
import game.vn.common.lib.updateconfig.MaintainGame;
import game.vn.common.lib.updateconfig.TurnOffGameList;
import game.vn.common.lib.updateconfig.tournament.GameTournamentConfigInfor;
import game.vn.common.lib.updateconfig.tournament.BonusInfor;
import game.vn.common.queue.QueueLogKey;
import game.vn.common.queue.QueueService;
import game.vn.common.tournament.TournamentManager;
import game.vn.util.GsonUtil;
import game.vn.util.GlobalsUtil;
import game.vn.util.HazelcastUtil;
import game.vn.util.Utils;
import game.vn.util.watchservice.TaiXiuConfig;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author tuanp
 */
public class QueueUpdateConfig {

    private static final Logger LOG = LoggerFactory.getLogger(QueueService.class);
    private static final QueueUpdateConfig INSTANCE = new QueueUpdateConfig();
    private Channel channel;
    private Connection conn;
    private AMQP.BasicProperties prop;
    
    
    public static QueueUpdateConfig instance(){
        return INSTANCE;
    }
    
    public void init(){
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
            
            int serverId = ServerConfig.getInstance().getServerId();
            String exchange = QueueConfig.getInstance().getExchangeUpdateConfig();
            
            channel = conn.createChannel();
            
            channel.exchangeDeclare(exchange, QueueService.EXCHANGE_TYPE_FANOUT);
            //get config game
            channel.queueDeclare(QueueConfig.getInstance().getQueueConfigGame(), true, false, false, null);
            
            channel.queueDeclare(QueueConfig.getInstance().getQueueTournamentConfig(), true, false, false, null);
            
            channel.queueDeclare(QueueConfig.getInstance().getQueueUpdateConfigGame() + serverId, true, false, false, null);
            
            channel.queueDeclare(QueueConfig.getInstance().getQueueUpdateTournament() + serverId, true, false, false, null);
            
            //lấy danh sách game service
            channel.queueDeclare(QueueConfig.getInstance().getQueueServices(), true, false, false, null);
            
            //get turn off game config
            channel.queueDeclare(QueueConfig.getInstance().getQueueTurnOffGame(), true, false, false, null);
            
            channel.queueDeclare(QueueConfig.getInstance().getQueueUpdateTurnOffGame() + serverId, true, false, false, null);

            channel.queueDeclare(QueueConfig.getInstance().getQueueMaintain() + serverId, true, false, false, null);

            
            channel.queueBind(QueueConfig.getInstance().getQueueUpdateConfigGame() + serverId, exchange, QueueLogKey.KEY_UPDATE_CONFIG_GAME);
            channel.queueBind(QueueConfig.getInstance().getQueueUpdateTournament() + serverId, exchange, QueueLogKey.KEY_UPDATE_CONFIG_GAME_TOURNAMENT);
            channel.queueBind(QueueConfig.getInstance().getQueueUpdateTurnOffGame() + serverId, exchange, QueueLogKey.KEY_UPDATE_TURN_OFF_GAME);
            channel.queueBind(QueueConfig.getInstance().getQueueMaintain() + serverId, exchange, QueueLogKey.KEY_MAINTAIN_GAME);
            
            AMQP.BasicProperties.Builder builder = new AMQP.BasicProperties.Builder();
            prop = builder.deliveryMode(2).build();

            LOG.info("---------------- QueueUpdateConfig init done, connected to: " + conn.getAddress() + ":" + conn.getPort() + " ----------------");
        } catch (Exception e) {
            LOG.error("QueueUpdateConfig.init() error: ", e);
        }
    }

    public void consume() {
        
        //update config game
        Consumer consumer1 = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties props, byte[] body) throws IOException {
                try {
                    switch (envelope.getRoutingKey()) {
                        case QueueLogKey.KEY_UPDATE_CONFIG_GAME:
                            updateConfigGame(new String(body, "UTF-8"));
                            break;
                    }
                } catch (Exception e) {
                    LOG.error("update config game error: ", e);
                }
            }
        };
        
        //get config game
        Consumer consumer2 = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties props, byte[] body) throws IOException {
                AMQP.BasicProperties replyProps = new AMQP.BasicProperties
                            .Builder()
                            .correlationId(props.getCorrelationId())
                            .build();
                String response = "";
                try {
                    //thông tin update config game chổ này
                    LOG.debug(" [x] Received '" + envelope + "'");
                    int moneyType =Integer.parseInt(new String(body, "UTF-8")) ;
                    response = GsonUtil.toJson(getInforGameConfig(moneyType));
                } catch (RuntimeException e) {
                    LOG.error("get config game error:", e);
                }finally{
                     channel.basicPublish("", props.getReplyTo(), replyProps, response.getBytes("UTF-8"));
                }
            }
        };
        
        //get list services
        Consumer consumer3 = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties props, byte[] body) throws IOException {
                AMQP.BasicProperties replyProps = new AMQP.BasicProperties
                            .Builder()
                            .correlationId(props.getCorrelationId())
                            .build();
                String response = "";
                try {
                    //thông tin update config game chổ này
                    LOG.debug(" [x] Received '" + envelope + "'");
                     List<GameInfor> infor= getGameInforList();
                    response = GsonUtil.toJson(infor);
                } catch (Exception e) {
                    LOG.error("get list service error:", e);
                }finally{
                    channel.basicPublish("", props.getReplyTo(), replyProps, response.getBytes("UTF-8"));
                }
            }
        };
        // get turn off game infor
        Consumer consumer4 = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties props, byte[] body) throws IOException {
                AMQP.BasicProperties replyProps = new AMQP.BasicProperties
                            .Builder()
                            .correlationId(props.getCorrelationId())
                            .build();
                String response = "";
                try {
                    //thông tin update config game chổ này
                    LOG.debug(" [x] Received '" + envelope + "'");
                    int moneyType =Integer.parseInt(new String(body, "UTF-8")) ;
                    response = GsonUtil.toJson(TurnOffGameConfig.getInstance().getTurnOffGameDetails(moneyType));
                } catch (Exception e) {
                    LOG.error("get get turn off game infor error:", e);
                }finally{
                    channel.basicPublish("", props.getReplyTo(), replyProps, response.getBytes("UTF-8"));
                }
            }
        };
        
         // update turn off game infor
        Consumer consumer5 = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties props, byte[] body) throws IOException {
                try {
                    switch (envelope.getRoutingKey()) {
                        case QueueLogKey.KEY_UPDATE_TURN_OFF_GAME:
                            TurnOffGameList turnOffGameList = GsonUtil.fromJson(new String(body, "UTF-8"), TurnOffGameList.class);
                            if (turnOffGameList == null) {
                                return;
                            }
                            if (turnOffGameList.getPointGameList() != null) {
                                TurnOffGameConfig.getInstance().getListConfig().setPointGameList(turnOffGameList.getPointGameList());
                            }
                            if (turnOffGameList.getMoneyGameList() != null) {
                                TurnOffGameConfig.getInstance().getListConfig().setMoneyGameList(turnOffGameList.getMoneyGameList());
                            }
                            TurnOffGameConfig.getInstance().updateFileGameConfig(GsonUtil.toJson(TurnOffGameConfig.getInstance().getListConfig()));
                            break;
                    }

                } catch (Exception e) {
                    LOG.error("update turn off game infor error:", e);
                }
            }
        };

        //update config game
        Consumer consumer6 = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties props, byte[] body) throws IOException {
                try {
                    switch (envelope.getRoutingKey()) {
                        case QueueLogKey.KEY_MAINTAIN_GAME:
                            updateMaintainConfig(new String(body, "UTF-8"));
                            break;
                    }

                } catch (Exception e) {
                    LOG.error("update config game error: ", e);
                }
            }
        };
        
        //get config tournament game
        Consumer consumer7 = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties props, byte[] body) throws IOException {
                AMQP.BasicProperties replyProps = new AMQP.BasicProperties
                            .Builder()
                            .correlationId(props.getCorrelationId())
                            .build();
                String response = "";
                try {
                    //thông tin update config game chổ này
                    LOG.debug(" [x] Received '" + envelope + "'");
                    response = GsonUtil.toJson(getInforSpinAndGoConfig());
                } catch (RuntimeException e) {
                    LOG.error("get config game tournament error:", e);
                }finally{
                     channel.basicPublish("", props.getReplyTo(), replyProps, response.getBytes("UTF-8"));
                }
            }
        };
        
        //update config tournament game
        Consumer consumer8 = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties props, byte[] body) throws IOException {
                try {
                    switch (envelope.getRoutingKey()) {
                        case QueueLogKey.KEY_UPDATE_CONFIG_GAME_TOURNAMENT:
                            updateConfigGameTournament(new String(body, "UTF-8"));
                            break;
                        case QueueLogKey.KEY_UPDATE_CONFIG_GAME_TOURNAMENT_REWARD:
                            updateConfigGameTournamentReward();
                            break;
                    }
                } catch (Exception e) {
                    LOG.error("update config game error: ", e);
                }
            }
        };

        try {
            int serverId = ServerConfig.getInstance().getServerId();
            channel.basicConsume(QueueConfig.getInstance().getQueueUpdateConfigGame() + serverId, true, consumer1);
            channel.basicConsume(QueueConfig.getInstance().getQueueConfigGame(), true, consumer2);
            channel.basicConsume(QueueConfig.getInstance().getQueueServices(), true, consumer3);
            channel.basicConsume(QueueConfig.getInstance().getQueueTurnOffGame(), true, consumer4);
            channel.basicConsume(QueueConfig.getInstance().getQueueUpdateTurnOffGame() + serverId, true, consumer5);
            channel.basicConsume(QueueConfig.getInstance().getQueueMaintain() + serverId, true, consumer6);
            channel.basicConsume(QueueConfig.getInstance().getQueueTournamentConfig(), true, consumer7);
            channel.basicConsume(QueueConfig.getInstance().getQueueUpdateTournament() + serverId, true, consumer8);

        } catch (Exception ex) {
            LOG.error("QueueUpdateConfig.consume", ex);
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
    
    private List<GameInfor> getGameInforList(){
        List<GameInfor> games= new ArrayList<>();
        //point game
        games.addAll(getGameInforListByMoneyType(MoneyContants.POINT));
        //money game
        games.addAll(getGameInforListByMoneyType(MoneyContants.MONEY));
        return games;
    }
    
    /**
     * Lấy ra danh sách game theo money type
     * @param moneyType
     * @return 
     */
    private List<GameInfor> getGameInforListByMoneyType(int moneyType){
         List<GameInfor> games= new ArrayList<>();
        
        //point game
        String gamesconfig = RoomConfig.getInstance().getPointGames();
        if(moneyType==MoneyContants.MONEY){
            gamesconfig=RoomConfig.getInstance().getMoneyGames();
        }
        if (!gamesconfig.isEmpty()) {
            JsonObject json = GsonUtil.parse(gamesconfig).getAsJsonObject();
            if (json != null) {
                for (Map.Entry<String, JsonElement> entry : json.entrySet()) {
                    if(!entry.getValue().getAsBoolean()){
                        continue;
                    }
                    int serviceId = Utils.getServiceId(entry.getKey());
                    GameInfor gameInfor = new GameInfor();
                    gameInfor.setMoneyType((byte) moneyType);
                    gameInfor.setServiceId(serviceId);
                    gameInfor.setName(Service.getServiceNameByLanguage((byte) serviceId, GlobalsUtil.ENGLISH_LOCALE));
                    gameInfor.setIsActive(entry.getValue().getAsBoolean());
                    games.add(gameInfor);
                }
            }
        }

        GameInfor gameInfor = new GameInfor();
        gameInfor.setMoneyType((byte) moneyType);
        gameInfor.setServiceId(Service.TAI_XIU);
        gameInfor.setName(Service.getServiceNameByLanguage(Service.TAI_XIU, GlobalsUtil.ENGLISH_LOCALE));
        gameInfor.setIsActive(true);
        if (moneyType == MoneyContants.MONEY && TaiXiuConfig.getInstance().isEnable()) {
            games.add(gameInfor);
        }
        if (moneyType == MoneyContants.POINT && TaiXiuConfig.getInstance().isEnablePoint()) {
            games.add(gameInfor);
        }

        return games;
    }

     /**
     * Lấy thông tin config của game không gửi về danh sách tournament
     * @param serviceId
     * @param moneyType
     * @return 
     */
    private List<GameConfigInfor> getInforGameConfig(int moneyType) {
        
        List<GameConfigInfor> gameInforConfigs= new ArrayList<>();
        List<GameInfor> listGame = getGameInforListByMoneyType(moneyType);
        for (GameInfor game : listGame) {
            String nameLobby = Utils.getLobbyName(game.getServiceId(), moneyType);
            if (nameLobby.isEmpty() || RoomConfig.getInstance().getTournamentNameGames().contains(nameLobby)) {
                continue;
            }
            GameConfigInfor gameInfor = new GameConfigInfor();
            gameInfor.setServiceId(game.getServiceId());
            gameInfor.setMoneyType((byte) moneyType);
            gameInfor.setBets(Utils.convertToListDouble(RoomConfig.getInstance().getListBet(nameLobby)));
            gameInfor.setMaxBet(RoomConfig.getInstance().getMaxBetGame(nameLobby));
            gameInfor.setMinJoin(RoomConfig.getInstance().getMinJoinGame(nameLobby));
            gameInfor.setNoPlayer(RoomConfig.getInstance().getNoPlayer(nameLobby));
            gameInfor.setPenalize(RoomConfig.getInstance().getPennalizeFactor(nameLobby));
            gameInfor.setPlayingTime(RoomConfig.getInstance().getPlayingTime(nameLobby));
            gameInfor.setResultTime(RoomConfig.getInstance().getResultTime(nameLobby));
            gameInfor.setWaitingTime(RoomConfig.getInstance().getWaitingTime(nameLobby));
            gameInfor.setTax(RoomConfig.getInstance().getTax(nameLobby));
            gameInfor.setMinJoinOwner(RoomConfig.getInstance().getMinJoinOwner(nameLobby));
            gameInfor.setPriority(RoomConfig.getInstance().getPriority(nameLobby));
            gameInforConfigs.add(gameInfor);
        }

        return gameInforConfigs;

    }
    
    /**
     * Lấy thông tin config của game
     * @param serviceId
     * @param moneyType
     * @return 
     */
    private List<GameTournamentConfigInfor> getInforSpinAndGoConfig() {
        
        String lobbyName = Utils.getLobbyName(Service.TIEN_LEN_TOUR, MoneyContants.MONEY);
        List<GameTournamentConfigInfor> gameTournamentConfigInfors = new ArrayList<>();

        GameTournamentConfigInfor gameInforConfig= new GameTournamentConfigInfor();
        gameInforConfig.setSpinTime(RoomConfig.getInstance().getRotateTime(Service.TIEN_LEN_TOUR));
        gameInforConfig.setPlayingTime(RoomConfig.getInstance().getPlayingTime(lobbyName));
        gameInforConfig.setPriority(RoomConfig.getInstance().getPriority(lobbyName));
        gameInforConfig.setWaitingTime(RoomConfig.getInstance().getWaitingTime(lobbyName));
        gameInforConfig.setResultTime(RoomConfig.getInstance().getResultTime(lobbyName));
        gameInforConfig.setTotalFund(HazelcastUtil.getSumMoneyByBetBoard(Service.TIEN_LEN_TOUR));
        gameInforConfig.setNoPlayer(RoomConfig.getInstance().getNoPlayer(lobbyName));
        gameInforConfig.setWinning(RoomConfig.getInstance().getWinning(Service.TIEN_LEN_TOUR));
        gameInforConfig.setServiceId(Service.TIEN_LEN_TOUR);
        gameTournamentConfigInfors.add(gameInforConfig);

        return gameTournamentConfigInfors;

    }
    
    /**
     * Lấy ra thông tin bonus tournament game
     * @param strValues
     * @return 
     */
    private List<BonusInfor> getInforBonus(String strValues){
        String[] arrValues = strValues.split(";");
        List<BonusInfor> tournamnetList = new ArrayList<>();
        for (String value : arrValues) {
            String[] arrTour = value.split(",");
            if (arrTour.length < 2) {
                continue;
            }
            try {
                int multi = Integer.valueOf(arrTour[0]);
                int rate = Integer.valueOf(arrTour[1]);
                BonusInfor tour = new BonusInfor();
                tour.setRate(rate);
                tour.setMultiply(multi);
                tournamnetList.add(tour);
            } catch (Exception e) {
                LOG.error("getInforBonus error, ", e);
            }
        }
        return tournamnetList;
    }
    
     private synchronized void updateConfigGameTournamentReward(){
         TournamentManager.getInstance().initAllTournaments();
     }
    
    /**
     * Update config trong game tournament
     * @param data 
     */
    private synchronized void updateConfigGameTournament(String data){
        LOG.info("data: " + data);
        GameTournamentConfigInfor response = GsonUtil.fromJson(data, GameTournamentConfigInfor.class);
        if(response==null){
            return ;
        }
    
        String lobbyName = Utils.getLobbyName(Service.TIEN_LEN_TOUR, MoneyContants.MONEY);
        if(lobbyName.isEmpty()){
            return;
        }
        
        try {
            RoomConfig.getInstance().updateProperties(RoomConfig.PLAYING_TIME_NAME_LOBBY + lobbyName, String.valueOf(response.getPlayingTime()));
            RoomConfig.getInstance().updateProperties(RoomConfig.RESULT_TIME_NAME_LOBBY + lobbyName, String.valueOf(response.getResultTime()));
            RoomConfig.getInstance().updateProperties(RoomConfig.WAITING_TIME_NAME_LOBBY + lobbyName, String.valueOf(response.getWaitingTime()));
            RoomConfig.getInstance().updateProperties(RoomConfig.PRIORITY_GAME + lobbyName, String.valueOf(response.getPriority()));
            RoomConfig.getInstance().updateProperties(RoomConfig.NO_PLAYER_NAME_LOBBY + lobbyName, String.valueOf(response.getNoPlayer()));
            RoomConfig.getInstance().updateProperties(RoomConfig.ROTATE_TIME + Service.TIEN_LEN_TOUR, String.valueOf(response.getSpinTime()));
            RoomConfig.getInstance().updateProperties(RoomConfig.GAME_WINNING + + Service.TIEN_LEN_TOUR, String.valueOf(response.getWinning()));
            RoomConfig.getInstance().save();     
            
        } catch (Exception e) {
            LOG.error("updateSpinAndGoConfig error:", e);
        }    
    }
    
    /**
     * Update config trong game
     * @param data 
     */
    private synchronized void updateConfigGame(String data){
        LOG.info("data: " + data);
        GameConfigInfor response = GsonUtil.fromJson(data, GameConfigInfor.class);
        if(response==null){
            return ;
        }
        
        String nameLobby =Utils.getLobbyName(response.getServiceId(), response.getMoneyType());
        
        if(nameLobby.isEmpty() || RoomConfig.getInstance().getTournamentNameGames().contains(nameLobby)){
            return;
        }
        RoomConfig.getInstance().reloadConfig();
        RoomConfig.getInstance().updateProperties(nameLobby,Utils.convertListDoubleToString(response.getBets()));
        RoomConfig.getInstance().updateProperties(RoomConfig.PENNALIZE_LEAVER_NAME_LOBBY+nameLobby, String.valueOf(response.getPenalize()));
        RoomConfig.getInstance().updateProperties(RoomConfig.MAX_BET_NAME_LOBBY+nameLobby, String.valueOf(response.getMaxBet()));
        RoomConfig.getInstance().updateProperties(RoomConfig.GAME_MIN_JOIN_NAME_LOBBY+nameLobby, String.valueOf(response.getMinJoin()));
        RoomConfig.getInstance().updateProperties(RoomConfig.NO_PLAYER_NAME_LOBBY+nameLobby, String.valueOf(response.getNoPlayer()));
        RoomConfig.getInstance().updateProperties(RoomConfig.PLAYING_TIME_NAME_LOBBY+nameLobby, String.valueOf(response.getPlayingTime()));
        RoomConfig.getInstance().updateProperties(RoomConfig.RESULT_TIME_NAME_LOBBY+nameLobby, String.valueOf(response.getResultTime()));
        RoomConfig.getInstance().updateProperties(RoomConfig.WAITING_TIME_NAME_LOBBY+nameLobby, String.valueOf(response.getWaitingTime()));
        RoomConfig.getInstance().updateProperties(RoomConfig.MIN_JOIN_OWNER_NAME_LOBBY+nameLobby, String.valueOf(response.getMinJoinOwner()));
        RoomConfig.getInstance().updateProperties(RoomConfig.TAX_GAME+nameLobby, String.valueOf(response.getTax()));
        RoomConfig.getInstance().updateProperties(RoomConfig.PRIORITY_GAME+nameLobby, String.valueOf(response.getPriority()));
        RoomConfig.getInstance().save();
        
    }
    
    /**
     * Thực hiện chức năng bảo trì của server
     * @param data 
     */
    private void updateMaintainConfig(String data){
        LOG.info("data: " + data);
        MaintainGame response = GsonUtil.fromJson(data, MaintainGame.class);
        if(response==null){
            return ;
        }
        RoomConfig.getInstance().reloadConfig();
        if(response.isIsMaintain()){
            for(int serviceId:response.getServiceIds()){
                RoomConfig.getInstance().updateProperties(RoomConfig.MAINTAIN_GAME+serviceId,"true");
            }
            RoomConfig.getInstance().updateProperties(RoomConfig.MAINTAIN_ALL_GAME,response.isIsAllGame()?"true":"false");
        }else{
            RoomConfig.getInstance().updateProperties(RoomConfig.MAINTAIN_ALL_GAME,"false");
            List<GameInfor> games = getGameInforListByMoneyType(MoneyContants.MONEY);
            for(GameInfor game: games){
                RoomConfig.getInstance().updateProperties(RoomConfig.MAINTAIN_GAME+game.getServiceId(),"false");
            }
        }
        RoomConfig.getInstance().updateProperties(RoomConfig.MAINTAIN_INFOR,response.getMess());
        RoomConfig.getInstance().updateProperties(RoomConfig.MAINTAIN_INFOR_EN,response.getMessEn());
        RoomConfig.getInstance().save();
    }
}
