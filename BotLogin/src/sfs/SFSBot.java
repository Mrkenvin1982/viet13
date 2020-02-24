/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sfs;

import com.google.gson.JsonObject;
import com.smartfoxserver.v2.entities.data.ISFSArray;
import com.smartfoxserver.v2.entities.data.ISFSObject;
import com.smartfoxserver.v2.entities.data.SFSObject;
import com.smartfoxserver.v2.exceptions.SFSException;
import constant.Constant;
import db.Database;
import domain.BotConfig;
import domain.BotSchedule;
import domain.BotScheduleTime;
import game.command.SFSAction;
import game.command.SFSCommand;
import game.key.SFSKey;
import game.vn.common.lib.taixiu.TaiXiuCommand;
import game.vn.common.lib.taixiu.TaiXiuGameInfo;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import service.Service;
import sfs2x.client.SmartFox;
import sfs2x.client.core.BaseEvent;
import sfs2x.client.core.IEventListener;
import sfs2x.client.core.SFSEvent;
import sfs2x.client.entities.Room;
import sfs2x.client.entities.User;
import sfs2x.client.entities.variables.UserVariable;
import sfs2x.client.requests.ExtensionRequest;
import sfs2x.client.requests.JoinRoomRequest;
import sfs2x.client.requests.LeaveRoomRequest;
import sfs2x.client.requests.LoginRequest;
import sfs2x.client.util.ConfigData;
import util.Configs;
import util.DateUtil;
import util.GsonUtil;
import util.Utils;

/**
 *
 * @author vinhnp
 */
public class SFSBot implements IEventListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(SFSBot.class);
    private static final long ONE_MINUTE = 60000;

    private final String userId;
    private double betMoney;
    private byte moneyType;
    private byte serviceId;
    private int userType;
    private int minBuyStack;
    private int minBuyStackOwner;

    private final SmartFox sfs;
    private final ConfigData cfg;
    private Object[] bets;
    private Room roomLobby;
    private Room roomGame;
    private String roomName;
    private final Timer timer = new Timer();
    private final ExtensionRequest pingRequest;
    private boolean isRunning = true;
    private long timeBetTaiXiu = 0;
    private BotConfig config;

    public SFSBot(String userId) {
        this.userId = userId;

        sfs = new SmartFox();
        sfs.addEventListener(SFSEvent.CONNECTION, this);
        sfs.addEventListener(SFSEvent.CONNECTION_LOST, this);
        sfs.addEventListener(SFSEvent.LOGIN, this);
        sfs.addEventListener(SFSEvent.LOGIN_ERROR, this);
        sfs.addEventListener(SFSEvent.LOGOUT, this);
        sfs.addEventListener(SFSEvent.ROOM_JOIN, this);
        sfs.addEventListener(SFSEvent.USER_EXIT_ROOM, this);
        sfs.addEventListener(SFSEvent.USER_VARIABLES_UPDATE, this);
        sfs.addEventListener(SFSEvent.EXTENSION_RESPONSE, new IEventListener() {
            @Override
            public void dispatch(BaseEvent e) throws SFSException {
                SFSObject sfsObj = (SFSObject) e.getArguments().get("params");
                if (Configs.getInstance().isLogEnable()) {
                    LOGGER.info(sfsObj.getDump());
                }
                try {
                    int action = sfsObj.containsKey(SFSKey.ACTION_INCORE) ? sfsObj.getInt(SFSKey.ACTION_INCORE) : sfsObj.getInt(SFSKey.ACTION_INGAME);
                    switch (action) {
                        case SFSAction.JOIN_ZONE_SUCCESS:
                            userType = sfs.getMySelf().getVariable("userType").getIntValue();
                            serviceId = Service.getServiceId(userType);
                            moneyType = Service.getMoneyType(userType);
//                            config = Database.INSTANCE.getBotConfig(serviceId, moneyType);
                            setMoneyType();
//                            changeUsername();
//                            updateSchedule();
                            new Timer().schedule(new TimerTask() {
                                @Override
                                public void run() {
                                    updateSchedule();
                                }
                            }, ONE_MINUTE, ONE_MINUTE);
                            if (serviceId == Service.TAI_XIU) {
                                if (userType == Constant.USER_TYPE_BOT_TX || userType == Constant.POINT_TYPE_BOT_TX) {
                                    getTaiXiuInfo();
                                }
                            } else {
                                requestInfoAllGame();
                            }
                            break;
                            
                        case SFSAction.REQUEST_INFOR_ALL_GAME:
                            ISFSArray sfsArr = moneyType == Constant.MONEY ? sfsObj.getSFSArray("arrMoney") : sfsObj.getSFSArray("arrPoint");
                            for (int i=0; i<sfsArr.size(); i++) {
                                ISFSObject obj = sfsArr.getSFSObject(i);
                                if (obj.getUtfString("name").equals(Service.getLobbyName(serviceId, moneyType))) {
                                    minBuyStack = obj.getInt("minBuyStack");
                                    minBuyStackOwner = obj.getInt("minBuyStackOwner");
                                }
                            }
                            joinLobby();
                            break;

                        case SFSAction.LOBBY_LIST_COUNTER:
                            bets = sfsObj.getDoubleArray(SFSKey.LIST_BET_BOARD).toArray();
                            betMoney = (double)bets[Utils.nextInt(bets.length)];
                            LOGGER.info(userId + " buy stack from lobby " + betMoney);
                            buyStack(betMoney);
                            break;

                        case SFSAction.BUY_STACK_IN_LOBBY:
                            LOGGER.info(userId + " error buy stack: " + sfsObj.getDump());
                            roomName = null;
                            leaveLobby();
                            break;

                        case SFSAction.PLAY_TAIXIU:
                            byte cmd = sfsObj.getByte(SFSKey.COMMAND);
                            switch (cmd) {
                                case TaiXiuCommand.UPDATE_CURRENT_MATCH_INFO:
                                    if (System.currentTimeMillis() < timeBetTaiXiu || !isRunning) {
                                        break;
                                    }
                                    String data = sfsObj.getUtfString(SFSKey.DATA);
                                    TaiXiuGameInfo info = GsonUtil.fromJson(data, TaiXiuGameInfo.class);
                                    if (info.getTimeBetRemain() > 0) {  // lúc start ván
                                        BotConfig config = Database.INSTANCE.getBotConfig(serviceId, moneyType); 
                                        int botEach = Utils.nextInt(100);
                                        if (botEach < config.getBotEachTurnTo()) {
                                            int count = Utils.nextInt(config.getBotEachTurn()) + 1;   
                                            Integer choice = Utils.nextInt(2);
                                            long time = info.getTimeBetRemain();
                                            for (int i=0; i<count; i++) {
                                                long delay = Math.abs(Utils.nextLong()) % time;
                                                time -= delay;
                                                Utils.sleep(delay);
                                                int j = Utils.nextInt(info.getBetMoneys().size() - 1);
                                                double betMoney = info.getBetMoneys().get(j).doubleValue();
                                                sendTaiXiuBetRequest(choice.byteValue(), betMoney);
                                            }
                                        }

                                        if (Configs.getInstance().getTaiXiuBetDelayFrom() > 0) {
                                            long delay = Utils.nextInt(Configs.getInstance().getTaiXiuBetDelayFrom(), Configs.getInstance().getTaiXiuBetDelayTo());
                                            timeBetTaiXiu = System.currentTimeMillis() + delay * 60000;
                                            LOGGER.info(userId + " next bet tx in " + delay + " minutes");
                                        }
                                    } else {
                                        if (Utils.nextInt(50) <= 1) {
                                            changeUsername();
                                        }
                                    }
                                    break;
                            }
                            break;
                    }
                } catch (Exception ex) {
                    LOGGER.error(userId + " " + userId, ex);
                }
            }

        });

        cfg = new ConfigData();
        cfg.setHost(Configs.getInstance().getLoginHost());
        cfg.setPort(Configs.getInstance().getLoginPort());
        cfg.setZone(Configs.getInstance().getLoginZone());

        SFSObject sfsObj = new SFSObject();
        sfsObj.putInt(SFSKey.ACTION_INCORE, SFSAction.PING);
        pingRequest = new ExtensionRequest(SFSCommand.CLIENT_REQUEST, sfsObj);
        
        start();
    }

    @Override
    public void dispatch(BaseEvent e) throws SFSException {
        switch (e.getType()) {
            case SFSEvent.CONNECTION:
                boolean success = (Boolean) e.getArguments().get("success");
                LOGGER.info(userId + " connect " + success);
                if (success) {
                    login();
                }
                break;

            case SFSEvent.CONNECTION_LOST:
                LOGGER.info(userId + " lost connection");
                timer.cancel();
                break;

            case SFSEvent.LOGIN:
                LOGGER.info(userId + " login success");
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        sendPingRequest();
                    }
                }, 0, 30000);
                sfs.enableLagMonitor(true, 30);
                break;

            case SFSEvent.LOGIN_ERROR:
                String error = e.getArguments().get("errorMessage").toString();
                LOGGER.error(userId + " login error:  " + error);
                sfs.disconnect();
                break;

            case SFSEvent.LOGOUT:
                sfs.disconnect();
                break;

            case SFSEvent.ROOM_JOIN:
                Room room = (Room) e.getArguments().get("room");
                LOGGER.info(userId + " join room: " + room.getName());
                if (room.isGame()) {
                    roomGame = room;
                } else {
                    roomLobby = room;
                    if (isRunning) {
                        switch (userType) {
                            case Constant.USER_TYPE_BOT_BC_CAI:
                            case Constant.USER_TYPE_BOT_XD_CAI:
                            case Constant.USER_TYPE_BOT_MB_CAI:
                            case Constant.USER_TYPE_BOT_TL_CAI:
                            case Constant.POINT_TYPE_BOT_BC_CAI:
                            case Constant.POINT_TYPE_BOT_XD_CAI:
                            case Constant.POINT_TYPE_BOT_MB_CAI:
                            case Constant.POINT_TYPE_BOT_TL_CAI:
                                getListBetMoney();
                                break;
                        }
                    }
                }
                break;

            case SFSEvent.USER_EXIT_ROOM:
                User user = (User) e.getArguments().get("user");
                if (user.isItMe()) {
                    
                }
                break;

            case SFSEvent.USER_VARIABLES_UPDATE:
                ArrayList<String> vars = (ArrayList) e.getArguments().get("changedVars");
                if (vars.contains("userType")) {
                    
                }
                break;
        }
    }

    public void connect() {
        sfs.connect(cfg);
    }
    
    public boolean isConnected() {
        return sfs.isConnected();
    }
    
    public void disconnect() {
        sfs.disconnect();
    }
    
    public boolean isInGame() {
        return roomGame != null;
    }
    
    public boolean isPlaying(){
        return true;
    }

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    public byte getMoneyType() {
        return moneyType;
    }

    public double getBetMoney() {
        return betMoney;
    }

    public String getUserId() {
        return userId;
    }

    public String getEmail() {
        return userId;
    }

    public int getUserType() {
        return userType;
    }

    private void login() {
        JsonObject json = new JsonObject();
        json.addProperty("platform", "web");
        json.addProperty("channel", "2|win888xxx|1.0.6");
        json.addProperty("app_version", "1.0.3");
        json.addProperty("bundle_id", "");
        json.addProperty("udid", "botbc-udid-" + userId);
        json.addProperty("sessionId", "");
        json.addProperty("email", userId);
        json.addProperty("authorizeType", 1);

        SFSObject params = new SFSObject();
        params.putByte("login_type", (byte)2);
        params.putUtfString("login_token", userId);
        params.putUtfString("client_info", json.toString());
        LoginRequest rq = new LoginRequest("", "", Configs.getInstance().getLoginZone(), params);
        sfs.send(rq);
    }

    private void requestInfoAllGame() {
        SFSObject sfsObj = new SFSObject();
        sfsObj.putInt(SFSKey.ACTION_INCORE, SFSAction.REQUEST_INFOR_ALL_GAME);
        sfs.send(new ExtensionRequest(SFSCommand.CLIENT_REQUEST, sfsObj));
    }

    public void getListBetMoney() {
        SFSObject sfsObj = new SFSObject();
        sfsObj.putInt(SFSKey.ACTION_INCORE, SFSAction.LOBBY_LIST_COUNTER);
        sfs.send(new ExtensionRequest(SFSCommand.CLIENT_REQUEST, sfsObj, roomLobby));
    }

    public void buyStack(double betMoney) {
        int n = 10;
        double stackMoney = (n * betMoney) + Utils.nextInt((int)betMoney);
        SFSObject sfsObj = new SFSObject();
        sfsObj.putInt(SFSKey.ACTION_INCORE, SFSAction.BUY_STACK_IN_LOBBY);
        sfsObj.putDouble(SFSKey.BET_BOARD, betMoney);
        sfsObj.putDouble(SFSKey.MONEY_STACK, stackMoney);
        sfsObj.putBool(SFSKey.IS_OWNER, true);
        sfs.send(new ExtensionRequest(SFSCommand.CLIENT_REQUEST, sfsObj, roomLobby));
    }

    private void setMoneyType() {
        SFSObject sfsObj = new SFSObject();
        sfsObj.putInt(SFSKey.ACTION_INCORE, SFSAction.SET_MONEY_TYPE);
        sfsObj.putByte(SFSKey.MONEY_TYPE, moneyType);
        sfs.send(new ExtensionRequest(SFSCommand.CLIENT_REQUEST, sfsObj));
    }

    public boolean start() {
        try {
            connect();

        } catch (Exception e) {
            LOGGER.error("error starting bot " + userId, e);
            return false;
        }
        return true;
    }

    private void joinLobby() {
        sfs.send(new JoinRoomRequest(Service.getLobbyName(serviceId, moneyType)));
    }

    public void leaveLobby() {
        sfs.send(new LeaveRoomRequest());
    }

    private void sendPingRequest() {
        if (!sfs.isConnected()) {
            return;
        }
        
        sfs.send(pingRequest);
    }

    private void getTaiXiuInfo() {
        SFSObject sfsObj = new SFSObject();
        sfsObj.putInt(SFSKey.ACTION_INCORE, SFSAction.PLAY_TAIXIU);
        sfsObj.putByte(SFSKey.COMMAND, TaiXiuCommand.GET_CURRENT_MATCH_INFO);
        sfs.send(new ExtensionRequest(SFSCommand.CLIENT_REQUEST, sfsObj));
    }
    
    private void sendTaiXiuBetRequest(byte choice, double money) {
        SFSObject sfsObj = new SFSObject();
        sfsObj.putInt(SFSKey.ACTION_INCORE, SFSAction.PLAY_TAIXIU);
        sfsObj.putByte(SFSKey.COMMAND, TaiXiuCommand.BET);
        sfsObj.putByte(SFSKey.CHOICE, choice);
        sfsObj.putDouble(SFSKey.BET_MONEY, money);
        sfs.send(new ExtensionRequest(SFSCommand.CLIENT_REQUEST, sfsObj));
    }
    
    private void quitTaiXiu() {
        SFSObject sfsObj = new SFSObject();
        sfsObj.putInt(SFSKey.ACTION_INCORE, SFSAction.PLAY_TAIXIU);
        sfsObj.putByte(SFSKey.COMMAND, TaiXiuCommand.QUIT);
        sfs.send(new ExtensionRequest(SFSCommand.CLIENT_REQUEST, sfsObj));
    }

    private void changeUsername() {
        try {
            List<String> names = Configs.getInstance().getListName();
            String name = names.get(Utils.nextInt(names.size()));
            SFSObject sfsObj = new SFSObject();
            sfsObj.putInt(SFSKey.ACTION_INCORE, SFSAction.UPDATE_PROFILE);
            sfsObj.putUtfString(SFSKey.DISPLAY_NAME, name);
            sfs.send(new ExtensionRequest(SFSCommand.CLIENT_REQUEST, sfsObj));
        } catch (Exception e) {
            LOGGER.error(userId + " " + userId, e);
        }

    }

    public boolean canBuyStack(double betMoney) {
        try {
            String key = moneyType == Constant.MONEY ? "moneyUser" : "pointUser";
            UserVariable uv = sfs.getMySelf().getVariable(key);
            if (uv == null) {
                return false;
            }
            double money = sfs.getMySelf().getVariable(key).getDoubleValue();
            switch (userType) {
                case Constant.USER_TYPE_BOT_BC_CAI:
                case Constant.USER_TYPE_BOT_XD_CAI:
                case Constant.USER_TYPE_BOT_MB_CAI:
                case Constant.USER_TYPE_BOT_TL_CAI:
                case Constant.POINT_TYPE_BOT_BC_CAI:
                case Constant.POINT_TYPE_BOT_XD_CAI:
                case Constant.POINT_TYPE_BOT_MB_CAI:
                case Constant.POINT_TYPE_BOT_TL_CAI:
                    return money >= minBuyStackOwner * betMoney;
                default:
                    return money >= minBuyStack * betMoney;
            }
        } catch (Exception e) {
            LOGGER.error("", e);
        }
        return false;
    }

    private void updateSchedule() {
        if (Configs.getInstance().isStop()) {
            if (isInGame()) {
                if (isPlaying()) {
                    
                } else {

                }
            } else {
                disconnect();
            }
            return;
        }
        
        config = Database.INSTANCE.getBotConfig(serviceId, moneyType);
        if (!config.isEnable()) {
            if (isRunning) {
                isRunning = false;
            } else {
                return;
            }
        } else {
            BotSchedule schedule = Database.INSTANCE.getBotSchedule(userId);
            if (schedule == null) {
                return;
            }

            boolean onRunTime = onRunTime(schedule);
            if (onRunTime == isRunning) {
                return;
            }

            isRunning = onRunTime;
        }

        if (isRunning) {
            LOGGER.info(userId + " is on");
            if (isMaster()) {
                betMoney = 0;
                getListBetMoney();
            }
        } else {
            LOGGER.info(userId + " is off");
            if (isInGame()) {
                if (isPlaying()) {

                } else {

                }
            }
        }
    }
    
    private boolean onRunTime(BotSchedule schedule) {
        try {
            Date curDate = new Date();
            if (schedule.getRepeat() != BotSchedule.REPEAT_DAILY) {
                if (schedule.getStartDate() != null) {
                    Date startDate = DateUtil.parseString(schedule.getStartDate(), "yyyy-MM-dd");
                    Date endDate = DateUtil.parseString(schedule.getEndDate(), "yyyy-MM-dd");

                    if (curDate.before(startDate) || curDate.after(DateUtil.getEndOfDay(endDate))) {
                        return false;
                    }
                }

                if (schedule.getWeekDays() != null && !schedule.getWeekDays().trim().isEmpty()) {
                    int day = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
                    if (day == Calendar.SUNDAY) {
                        day = 6;
                    } else {
                        day -= 2;
                    }
                    String[] runDays = schedule.getWeekDays().split(",");
                    if (Byte.parseByte(runDays[day]) == 0) {
                        return false;
                    }
                }
            }
            
            List<BotScheduleTime> listTime = schedule.getTime();
            curDate = DateUtil.getDate(curDate, "HH:mm");
            for (BotScheduleTime time : listTime) {
                Date startHour = DateUtil.parseString(time.getStartTime(), "HH:mm");
                Date endHour = DateUtil.parseString(time.getEndTime(), "HH:mm");
                if (curDate.after(startHour) && curDate.before(endHour)) {
                    return true;
                }
            }

        } catch (Exception e) {
            LOGGER.error(userId + " " + userId, e);
        }

        return false;
    }

    private boolean isMaster() {
        switch (userType) {
            case Constant.USER_TYPE_BOT_BC_CAI:
            case Constant.USER_TYPE_BOT_XD_CAI:
            case Constant.USER_TYPE_BOT_MB_CAI:
            case Constant.USER_TYPE_BOT_TL_CAI:
            case Constant.POINT_TYPE_BOT_BC_CAI:
            case Constant.POINT_TYPE_BOT_XD_CAI:
            case Constant.POINT_TYPE_BOT_MB_CAI:
            case Constant.POINT_TYPE_BOT_TL_CAI:
                return true;
        }
        return false;
    }

    public boolean isRunning() {
        return isRunning;
    }

    public BotConfig getConfig() {
        return config;
    }
    
    public static void main(String[] args) throws Exception {
        new SFSBot("010907782672");
    }
}
