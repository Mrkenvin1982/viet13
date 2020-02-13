package game.vn.login;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.smartfoxserver.v2.api.CreateRoomSettings;
import com.smartfoxserver.v2.api.CreateRoomSettings.RoomExtensionSettings;
import com.smartfoxserver.v2.core.SFSEventType;
import com.smartfoxserver.v2.entities.Room;
import com.smartfoxserver.v2.entities.SFSRoomRemoveMode;
import com.smartfoxserver.v2.entities.User;
import com.smartfoxserver.v2.entities.variables.RoomVariable;
import com.smartfoxserver.v2.entities.variables.SFSRoomVariable;
import com.smartfoxserver.v2.extensions.SFSExtension;
import game.command.SFSCommand;
import game.vn.common.config.QueueConfig;
import game.vn.common.config.RoomConfig;
import game.vn.common.config.SFSConfig;
import game.vn.common.config.ServerConfig;
import game.vn.common.constant.ExtensionConstant;
import game.vn.common.constant.MoneyContants;
import game.vn.common.constant.Service;
import game.vn.common.lib.api.BotCCuDetail;
import game.vn.common.lib.api.CCUInfor;
import game.vn.common.lib.api.UserCCuDetail;
import game.vn.common.lib.contants.UserType;
import game.vn.common.lib.hazelcast.PlayingBoardManager;
import game.vn.common.properties.RoomInforPropertiesKey;
import game.vn.common.queue.QueueNotify;
import game.vn.common.queue.QueueQuest;
import game.vn.common.queue.QueueService;
import game.vn.common.queue.QueueServiceApi;
import game.vn.common.queue.QueueServiceEvent;
import game.vn.common.queue.QueueServicePayment;
import game.vn.common.queue.QueueTaiXiu;
import game.vn.common.queue.QueueUserManager;
import game.vn.common.queue.updateconfig.QueueUpdateConfig;
import game.vn.login.handler.AddRoomHandler;
import game.vn.login.handler.DisconnectHandler;
import game.vn.login.handler.JoinZoneEventHandler;
import game.vn.login.handler.LoginHandler;
import game.vn.login.handler.LogoutHandler;
import game.vn.login.handler.ReconnectHandler;
import game.vn.login.handler.RemoveRoomHandler;
import game.vn.login.handler.request.CommonClientRequest;
import game.vn.login.hazelcast.MyEntryListenerLogin;
import game.vn.util.GsonUtil;
import game.vn.util.HazelcastUtil;
import game.vn.util.Utils;
import game.vn.util.db.Database;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author minhhnb
 */
public class LoginExt extends SFSExtension{
    
    @Override
    public void init() {
        trace("LoginExt init...");
        Utils.init(getApi());
        addEventHandler();
        Database.instance.init(getParentZone().getDBManager());
//        initQueue();
        createLobbyRoom();
        SFSConfig.init(getParentZone());
        initHazelcast();
        trace("LoginExt init done.");
    }
    
    @Override
    public void destroy() {
        trace("destroy()!!!!!!!!!!!!!!.");
        super.destroy(); 
    }
    
    private void logCCU() {
        getApi().getSystemScheduler().scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                try {
                    CCUInfor ccu = new CCUInfor();
                    ccu.setConnectionId(ServerConfig.getInstance().getConnectionId());
                    ccu.setCount(getParentZone().getUserCount());
                    List<User> users = (List<User>) getParentZone().getUserList();
                    int countBot = 0;
                    int countBotBCCaiOn = 0;
                    int countBotBCConOn = 0;
                    int countBotXDCaiOn = 0;
                    int countBotXDConOn = 0;
                    int countBotMBOn = 0;
                    int countBotMBConOn = 0;
                    int countBotTLMNOn = 0;
                    int countBotTLMNConOn = 0;
                    int countBotTXOn = 0;
                    int countPointBCCaiOn = 0;
                    int countPointBCConOn = 0;
                    int countPointXDCaiOn = 0;
                    int countPointXDConOn = 0;
                    int countPointMBOn = 0;
                    int countPointMBConOn = 0;
                    int countPointTLMNOn = 0;
                    int countPointTLMNConOn = 0;
                    int countPointTXOn = 0;
                    
                    int countInGameMoneyBC = 0;
                    int countInGamePointBC = 0;
                    int countLobbyMoneyBC = 0;
                    int countLobbyPointBC = 0;
                    int countInGameMoneyXD = 0;
                    int countInGamePointXD = 0;
                    int countLobbyMoneyXD = 0;
                    int countLobbyPointXD = 0;
                    int countInGameMoneyMB = 0;
                    int countInGamePointMB = 0;
                    int countLobbyMoneyMB = 0;
                    int countLobbyPointMB = 0;
                    int countInGameMoneyTL = 0;
                    int countInGamePointTL = 0;
                    int countLobbyMoneyTL = 0;
                    int countLobbyPointTL = 0;

                    Map<Byte, UserCCuDetail> map = new HashMap<>();
                    map.put(Service.BAI_CAO, new UserCCuDetail(Service.BAI_CAO));
                    map.put(Service.XI_TO, new UserCCuDetail(Service.XI_TO));
                    map.put(Service.BLACKJACK, new UserCCuDetail(Service.BLACKJACK));
                    map.put(Service.TIENLEN, new UserCCuDetail(Service.TIENLEN));
                    map.put(Service.PHOM, new UserCCuDetail(Service.PHOM));
                    map.put(Service.MAUBINH, new UserCCuDetail(Service.MAUBINH));
                    map.put(Service.LIENG, new UserCCuDetail(Service.LIENG));
                    map.put(Service.SAM, new UserCCuDetail(Service.SAM));
                    map.put(Service.TIEN_LEN_DEM_LA, new UserCCuDetail(Service.TIEN_LEN_DEM_LA));
                    map.put(Service.TLDL_SOLO, new UserCCuDetail(Service.TLDL_SOLO));
                    map.put(Service.TIEN_LEN_TOUR, new UserCCuDetail(Service.TIEN_LEN_TOUR));
                    map.put(Service.TAI_XIU, new UserCCuDetail(Service.TAI_XIU));
                    
                    List<Room> listRoom;
                    Room room;
                    for (User u : users) {
                        if (Utils.isBot(u)) {
                            countBot++;
                            switch (Utils.getUserType(u)) {
                                case UserType.BOT_BC_CAI_ON:
                                    countBotBCCaiOn++;
                                    break;
                                case UserType.BOT_BC_CON_ON:
                                    countBotBCConOn++;
                                    break;
                                case UserType.BOT_XD_CAI_ON:
                                    countBotXDCaiOn++;
                                    break;
                                case UserType.BOT_XD_CON_ON:
                                    countBotXDConOn++;
                                    break;
                                case UserType.BOT_MB_ON:
                                    countBotMBOn++;
                                    break;
                                case UserType.BOT_MB_CON_ON:
                                    countBotMBConOn++;
                                    break;
                                case UserType.BOT_TLMN_ON:
                                    countBotTLMNOn++;
                                    break;
                                case UserType.BOT_TLMN_CON_ON:
                                    countBotTLMNConOn++;
                                    break;
                                case UserType.BOT_TX_ON:
                                    countBotTXOn++;
                                    break;
                                case UserType.POINT_BOT_BC_CAI_ON:
                                    countPointBCCaiOn++;
                                    break;
                                case UserType.POINT_BOT_BC_CON_ON:
                                    countPointBCConOn++;
                                    break;
                                case UserType.POINT_BOT_XD_CAI_ON:
                                    countPointXDCaiOn++;
                                    break;
                                case UserType.POINT_BOT_XD_CON_ON:
                                    countPointXDConOn++;
                                    break;
                                case UserType.POINT_BOT_MB_ON:
                                    countPointMBOn++;
                                    break;
                                case UserType.POINT_BOT_MB_CON_ON:
                                    countPointMBConOn++;
                                    break;
                                case UserType.POINT_BOT_TLMN_ON:
                                    countPointTLMNOn++;
                                    break;
                                case UserType.POINT_BOT_TLMN_CON_ON:
                                    countPointTLMNConOn++;
                                    break;
                                case UserType.POINT_BOT_TX_ON:
                                    countPointTXOn++;
                                    break;
                            }
                            
                            listRoom = u.getJoinedRooms();
                            if (listRoom == null || listRoom.isEmpty()) {
                                continue;
                            }
                            room = listRoom.get(0);
                            if (room != null) {
                                int moneyType = Utils.getMoneyTypeOfUser(u);
                                byte serviceId = Utils.getServiceId(room.getName());
                                PlayingBoardManager pbm = HazelcastUtil.getPlayingBoard(Utils.getIdDBOfUser(u));

                                switch (serviceId) {
                                    case Service.BAI_CAO:
                                        if (pbm == null) {
                                            if (moneyType == MoneyContants.MONEY) {
                                                countLobbyMoneyBC++;
                                            } else {
                                                countLobbyPointBC++;
                                            }
                                        } else {
                                            if (moneyType == MoneyContants.MONEY) {
                                                countInGameMoneyBC++;
                                            } else {
                                                countInGamePointBC++;
                                            }
                                        }
                                        break;
                                    case Service.BLACKJACK:
                                        if (pbm == null) {
                                            if (moneyType == MoneyContants.MONEY) {
                                                countLobbyMoneyXD++;
                                            } else {
                                                countLobbyPointXD++;
                                            }
                                        } else {
                                            if (moneyType == MoneyContants.MONEY) {
                                                countInGameMoneyXD++;
                                            } else {
                                                countInGamePointXD++;
                                            }
                                        }
                                        break;
                                    case Service.MAUBINH:
                                        if (pbm == null) {
                                            if (moneyType == MoneyContants.MONEY) {
                                                countLobbyMoneyMB++;
                                            } else {
                                                countLobbyPointMB++;
                                            }
                                        } else {
                                            if (moneyType == MoneyContants.MONEY) {
                                                countInGameMoneyMB++;
                                            } else {
                                                countInGamePointMB++;
                                            }
                                        }
                                        break;
                                    case Service.TIENLEN:
                                        if (pbm == null) {
                                            if (moneyType == MoneyContants.MONEY) {
                                                countLobbyMoneyTL++;
                                            } else {
                                                countLobbyPointTL++;
                                            }
                                        } else {
                                            if (moneyType == MoneyContants.MONEY) {
                                                countInGameMoneyTL++;
                                            } else {
                                                countInGamePointTL++;
                                            }
                                        }
                                        break;
                                }
                            }
                        } else {
                            listRoom = u.getJoinedRooms();
                            if (listRoom == null || listRoom.isEmpty()) {
                                continue;
                            }
                            room = listRoom.get(0);
                            if (room != null) {
                                int moneyType = Utils.getMoneyTypeOfUser(u);
                                byte serviceId = Utils.getServiceId(room.getName());
                                PlayingBoardManager pbm = HazelcastUtil.getPlayingBoard(Utils.getIdDBOfUser(u));
                                UserCCuDetail detail = map.get(serviceId);
                                if (pbm == null) {
                                    if (moneyType == MoneyContants.MONEY) {
                                        detail.increaseCountLobbyMoney();
                                    } else {
                                        detail.increaseCountLobbyPoint();
                                    }

                                } else {
                                    if (moneyType == MoneyContants.MONEY) {
                                        detail.increaseCountGameMoney();
                                    } else {
                                        detail.increaseCountGamePoint();
                                    }
                                }
                            }
                        }
                    }
                    ccu.setCountBot(countBot);
                    BotCCuDetail botCCuDetailBC = new BotCCuDetail(Service.BAI_CAO, countBotBCCaiOn, countBotBCConOn, countPointBCCaiOn, countPointBCConOn, countInGameMoneyBC, countInGamePointBC, countLobbyMoneyBC, countLobbyPointBC);
                    BotCCuDetail botCCuDetailXD = new BotCCuDetail(Service.BLACKJACK, countBotXDCaiOn, countBotXDConOn, countPointXDCaiOn, countPointXDConOn, countInGameMoneyXD, countInGamePointXD, countLobbyMoneyXD, countLobbyPointXD);
                    BotCCuDetail botCCuDetailMB = new BotCCuDetail(Service.MAUBINH, countBotMBOn, countBotMBConOn, countPointMBOn, countPointMBConOn, countInGameMoneyMB, countInGamePointMB, countLobbyMoneyMB, countLobbyPointMB);
                    BotCCuDetail botCCuDetailTLMN = new BotCCuDetail(Service.TIENLEN, countBotTLMNOn, countBotTLMNConOn, countPointTLMNOn, countPointTLMNConOn, countInGameMoneyTL, countInGamePointTL, countLobbyMoneyTL, countLobbyPointTL);
                    BotCCuDetail botCCuDetailTX = new BotCCuDetail(Service.TAI_XIU, countBotTXOn, 0, countPointTXOn, 0, 0, 0, 0, 0);

                    List<BotCCuDetail> botCCuDetail = new ArrayList<>();
                    botCCuDetail.add(botCCuDetailBC);
                    botCCuDetail.add(botCCuDetailXD);
                    botCCuDetail.add(botCCuDetailMB);
                    botCCuDetail.add(botCCuDetailTLMN);
                    botCCuDetail.add(botCCuDetailTX);

                    List<UserCCuDetail> listUserCCuDetail = new ArrayList<>(map.values());

                    ccu.setUserDetail(listUserCCuDetail);
                    ccu.setBotDetail(botCCuDetail);
                    ccu.setCreatedAt(System.currentTimeMillis() / 1000);
                    ccu.setRequestId(Utils.md5String(String.valueOf(System.currentTimeMillis())));
                    QueueServiceApi.getInstance().sendData(QueueConfig.getInstance().getKeyCCU(), true, ccu);
                    Database.instance.insertCCULog(GsonUtil.toJson(ccu));
                } catch (Exception e) {
                    getLogger().error("logCCU() error: ", e);
                }
            }
        }, 1, 1, TimeUnit.MINUTES);
    }

    /**
     * init những event extension sẽ bắt và xử lý khi client gửi len
     */
    private void addEventHandler(){
        addEventHandler(SFSEventType.USER_LOGIN, LoginHandler.class);
        addEventHandler(SFSEventType.USER_LOGOUT, LogoutHandler.class);
        addEventHandler(SFSEventType.USER_DISCONNECT, DisconnectHandler.class);
        addEventHandler(SFSEventType.USER_RECONNECTION_SUCCESS, ReconnectHandler.class);   
        addEventHandler(SFSEventType.USER_JOIN_ZONE, JoinZoneEventHandler.class);
        addEventHandler(SFSEventType.ROOM_REMOVED, RemoveRoomHandler.class);
        addEventHandler(SFSEventType.ROOM_ADDED, AddRoomHandler.class);
        addRequestHandler(SFSCommand.CLIENT_REQUEST, CommonClientRequest.class);

    }

    /**
     *  Tạo lobby cho game
     * @param nameLobby
     * @param idExt
     * @param classExt
     * @return 
     */
    private Room createLobbyRoom(String nameLobby, String idExt,String classExt, int moneyType) {
        Room room = null;
        try {
            CreateRoomSettings setting = new CreateRoomSettings();
            if(moneyType==MoneyContants.MONEY){
                setting.setGroupId(ExtensionConstant.LOBBY_GROUP_NAME_REAL);
            }else{
                setting.setGroupId(ExtensionConstant.LOBBY_GROUP_NAME);
            }
            
            setting.setGame(false);
            setting.setName(nameLobby);
            setting.setAutoRemoveMode(SFSRoomRemoveMode.NEVER_REMOVE);
            setting.setDynamic(false);//== true thì có thể auto remove
            setting.setMaxUsers(500000000);
           
            boolean isTournament = RoomConfig.getInstance().getTournamentNameGames().contains(nameLobby);
//            trace(nameLobby+ " tour ment check :"+isTournament);
            List<RoomVariable> vers= new ArrayList<>();
            vers.add(new SFSRoomVariable(RoomInforPropertiesKey.MONEY_TYPE, moneyType, true, false, false));
            vers.add(new SFSRoomVariable(RoomInforPropertiesKey.IS_TOURNAMENT, isTournament, true, false, false));
            

            setting.setRoomVariables(vers);

            RoomExtensionSettings roomExtSetting = new RoomExtensionSettings(idExt,classExt);
            setting.setExtension(roomExtSetting);
            getParentZone().createRoom(setting);
        } catch (Exception e) {
            getLogger().error("createLobbyRoom", e);
        }
        trace(String.format("create lobby manager: g:%s, n:%s", "gr_lobby", nameLobby));
        return room;
    }

    /**
     * tạo các room lobby
     */
    private void createLobbyRoom() {
        
        //tạo lobby cho game điểm
        String gamesconfig=RoomConfig.getInstance().getPointGames();
        if (!gamesconfig.isEmpty()) {
            JsonObject json = GsonUtil.parse(gamesconfig).getAsJsonObject();
            if (json != null) {
                for (Map.Entry<String, JsonElement> entry : json.entrySet()) {
                    if(!entry.getValue().getAsBoolean()){
                        continue;
                    }
                    String lobbyName= entry.getKey();
                    int serviceId = Utils.getServiceId(lobbyName);
                    createLobbyRoom(lobbyName, Utils.getExtIDFromServiceId(serviceId), Utils.getExtClassFromServiceId(serviceId),MoneyContants.POINT);
                }
            }
        }
        
        //tạo lobby cho game tiền
        gamesconfig=RoomConfig.getInstance().getMoneyGames();
        if (!gamesconfig.isEmpty()) {
            JsonObject json = GsonUtil.parse(gamesconfig).getAsJsonObject();
            if (json != null) {
                for (Map.Entry<String, JsonElement> entry : json.entrySet()) {
                    if(!entry.getValue().getAsBoolean()){
                        continue;
                    }
                    String lobbyName= entry.getKey();
                    int serviceId = Utils.getServiceId(lobbyName);
                    createLobbyRoom(lobbyName, Utils.getExtIDFromServiceId(serviceId), Utils.getExtClassFromServiceId(serviceId),MoneyContants.MONEY);
                }
            }
        }
        trace("create lobby manager DONE");
    }

    /**
     * init queue
     */
    private void initQueue() {
        trace("Queue init...");
        QueueService.getInstance().init();
        QueueServiceApi.getInstance().init();
        QueueUpdateConfig.instance().init();
        QueueUpdateConfig.instance().consume();
        QueueServicePayment.getInstance().init();
        QueueUserManager.instance().init(getApi(), getParentZone());
        QueueUserManager.instance().consume();
        QueueNotify.getInstance().init();
        QueueTaiXiu.getInstance().init();
        QueueServiceEvent.getInstance().init();
        QueueQuest.getInstance().init();
        trace("Queue init done");
    }
    
    private void initHazelcast() {
        trace("initHazelcast");
        HazelcastUtil.getUserStates().addEntryListener(new MyEntryListenerLogin(getApi()), true);
        HazelcastUtil.addServerInfo(ExtensionConstant.SERVER_TYPE_LOGIN);
        HazelcastUtil.removeAllUserState();
        HazelcastUtil.initBoardMap();
        trace("initHazelcast done");
    }

}