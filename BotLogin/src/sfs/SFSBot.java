/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sfs;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
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
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
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
import sfs2x.client.util.PasswordUtil;
import util.Configs;
import util.DateUtil;
import util.GsonUtil;
import util.HTTPUtil;
import util.Utils;

/**
 *
 * @author vinhnp
 */
public class SFSBot implements IEventListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(SFSBot.class);
    private static final long ONE_MINUTE = 60000;

    private final String email;
    private final String password;
    private String token;
    private String userId;
    private String clientId;
    private String gameToken;
    private double betMoney;
    private byte moneyType;
    private byte serviceId;
    private int userType;
    private int minBuyStack;
    private int minBuyStackOwner;

    private final SmartFox sfs;
    private final ConfigData cfg;
    private Object[] bets;
    private Room room;
    private String roomName;
    private SFSBotGame sfsGame;
    private final SFSBot sfsBot;
    private final Timer timer = new Timer();
    private final ExtensionRequest pingRequest;
    private boolean isRunning = true;
    private long timeBetTaiXiu = 0;
    private BotConfig config;

    public SFSBot(String email, String password) {
        this.email = email;
        this.password = password;
        this.sfsBot = this;

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
                            config = Database.INSTANCE.getBotConfig(serviceId, moneyType);
                            setMoneyType();
                            changeUsername(true);
                            updateSchedule();
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
                            List<Double> betCheck = new ArrayList<>();
                            for (Object bet : bets) {
                                double betMoney = (double) bet;
                                if (config.getListBet().contains(betMoney)) {
                                    betCheck.add(betMoney);
                                }
                            }
                            if (betMoney == 0 || !betCheck.contains(betMoney)) {
                                betMoney = betCheck.get(Utils.nextInt(betCheck.size()));
                            }
                            LOGGER.info(email + " buy stack from lobby " + betMoney);
                            buyStack(betMoney);
                            break;

                        case SFSAction.FIND_BOARD:
                            String boards = sfsObj.getUtfString(SFSKey.BOARDS);
                            JsonArray arr = new JsonParser().parse(boards).getAsJsonArray();
                            JsonObject json = arr.get(0).getAsJsonObject();
                            int port = json.get("port").getAsInt();
                            String ip = Configs.getInstance().getGameHost();
                            if (ip == null || ip.isEmpty()) {
                                ip = json.get("ip").getAsString();
                            }
                            String zone = json.get("zone").getAsString();
                            betMoney = json.get("betMoney").getAsDouble();
                            if (sfsGame == null) {
                                sfsGame = new SFSBotGame(sfsBot, gameToken, ip, port, zone, betMoney, moneyType, userType, roomName, userId, serviceId);
                            } else {
                                sfsGame.setBetMoney(betMoney);
                                sfsGame.setRoomName(roomName);
                                sfsGame.setToken(gameToken);
                                sfsGame.setServiceId(serviceId);
                                sfsGame.setUserType(userType);
                                sfsGame.setMoneyType(moneyType);
                            }
                            sfsGame.connect();
                            break;

                        case SFSAction.BUY_STACK_IN_LOBBY:
                            LOGGER.info(email + " error buy stack: " + sfsObj.getDump());
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
                                            LOGGER.info(email + " next bet tx in " + delay + " minutes");
                                        }
                                    } else {
                                        if (Utils.nextInt(50) <= 1) {
                                            changeUsername(false);
                                        }
                                    }
                                    break;
                            }
                            break;
                    }
                } catch (Exception ex) {
                    LOGGER.error(email + " " + userId, ex);
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
    }

    @Override
    public void dispatch(BaseEvent e) throws SFSException {
        switch (e.getType()) {
            case SFSEvent.CONNECTION:
                boolean success = (Boolean) e.getArguments().get("success");
                LOGGER.info(email + " connect " + success);
                if (success) {
                    login();
                }
                break;

            case SFSEvent.CONNECTION_LOST:
                LOGGER.info(email + " lost connection");
                timer.cancel();
                break;

            case SFSEvent.LOGIN:
                LOGGER.info(email + " login success");
                SFSObject sfsObj = (SFSObject) e.getArguments().get("data");
                gameToken = sfsObj.getUtfString(SFSKey.TOKEN_LOGIN);
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
                LOGGER.error(email + " login error:  " + error);
                sfs.disconnect();
                break;

            case SFSEvent.LOGOUT:
                sfs.disconnect();
                break;

            case SFSEvent.ROOM_JOIN:
                room = (Room) e.getArguments().get("room");
                LOGGER.info(email + " join room: " + room.getName());
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
                            Utils.sleep(500);
                            getListBetMoney();
                            break;
                    }
                }
                break;

            case SFSEvent.USER_EXIT_ROOM:
                User user = (User) e.getArguments().get("user");
                if (user.isItMe()) {
                    LOGGER.info(email + " leave room " + room.getName());
                    Utils.sleepRandom(2000, 3000);
                    Utils.sleepRandom(2000, 3000);
                    changeUsername(false);
                    room = null;
                    if (serviceId == Service.TAI_XIU) {
                        if (userType == Constant.USER_TYPE_BOT_TX || userType == Constant.POINT_TYPE_BOT_TX) {
                            getTaiXiuInfo();
                        }
                    } else {
                        requestInfoAllGame();
                    }
                }
                break;

            case SFSEvent.USER_VARIABLES_UPDATE:
                ArrayList<String> vars = (ArrayList) e.getArguments().get("changedVars");
                if (vars.contains("userType")) {
                    int newUserType = sfs.getMySelf().getVariable("userType").getIntValue();
                    if (newUserType != userType && userType != 0) {
                        int oldServiceId = serviceId;
                        userType = newUserType;
                        serviceId = Service.getServiceId(userType);
                        config = Database.INSTANCE.getBotConfig(serviceId, moneyType);
                        if (isInGame()) {
                            disconnectGame();
                        } else {
                            if (oldServiceId == Service.TAI_XIU) {
                                quitTaiXiu();
                                if (serviceId == Service.TAI_XIU) {
                                    if (userType == Constant.USER_TYPE_BOT_TX || userType == Constant.POINT_TYPE_BOT_TX) {
                                        getTaiXiuInfo();
                                    }
                                } else {
                                    requestInfoAllGame();
                                }
                            } else {    // đang là bot game bài
                                if (room != null) {
                                    leaveLobby();
                                }
                            }
                        }
                        LOGGER.info(email + " user type changed: " + userType);
                    }
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
    
    public void disconnectGame() {
        sfsGame.disconnect();
    }
    
    public boolean isOwner() {
        return sfsGame != null && sfsGame.isOwner();
    }

    public boolean isInGame() {
        return sfsGame != null && sfsGame.isConnected();
    }
    
    public boolean isPlaying(){
        return sfsGame.isPlaying();
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
        return email;
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
        json.addProperty("udid", "botbc-udid-" + email);
        json.addProperty("sessionId", "");
        json.addProperty("email", email);
        json.addProperty("authorizeType", 1);

        SFSObject params = new SFSObject();
        if (token != null) {
        params.putUtfString("login_token", token);
        }
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
        sfs.send(new ExtensionRequest(SFSCommand.CLIENT_REQUEST, sfsObj, room));
    }

    public void buyStack(double betMoney) {
        int n = config.getMinBuyIn() + Utils.nextInt(config.getMaxBuyIn() - config.getMinBuyIn());
        String key = moneyType == Constant.MONEY ? "moneyUser" : "pointUser";
        double money = sfs.getMySelf().getVariable(key).getDoubleValue();
        double stackMoney = (n * betMoney) + Utils.nextInt((int)betMoney);
        if (stackMoney > money) {
            stackMoney = Math.min(config.getMinBuyIn() * betMoney, money);
        }

        SFSObject sfsObj = new SFSObject();
        sfsObj.putInt(SFSKey.ACTION_INCORE, SFSAction.BUY_STACK_IN_LOBBY);
        sfsObj.putDouble(SFSKey.BET_BOARD, betMoney);
        sfsObj.putDouble(SFSKey.MONEY_STACK, stackMoney);
        sfsObj.putBool(SFSKey.IS_OWNER, true);
        sfs.send(new ExtensionRequest(SFSCommand.CLIENT_REQUEST, sfsObj, room));
    }

    private void setMoneyType() {
        SFSObject sfsObj = new SFSObject();
        sfsObj.putInt(SFSKey.ACTION_INCORE, SFSAction.SET_MONEY_TYPE);
        sfsObj.putByte(SFSKey.MONEY_TYPE, moneyType);
        sfs.send(new ExtensionRequest(SFSCommand.CLIENT_REQUEST, sfsObj));
    }

    public boolean start() {
        try {
            if (Configs.getInstance().isLoginByToken()) {
            String response = registerClient();
            JsonObject json = Utils.parse(response);
            if (json.get("code").getAsInt() != 7000) {
                LOGGER.info("registerClient fail: " + response);
                return false;
            }

            clientId = json.get("data").getAsJsonObject().get("clientId").getAsString();
            response = authenticate();
            json = Utils.parse(response);
            if (json.get("code").getAsInt() != 2000) {
                LOGGER.info("authenticate fail: " + email + " - " + response);
                return false;
            }

            json = json.get("data").getAsJsonObject();
            token = json.get("accessToken").getAsString();
            userId = json.get("accountId").getAsString();
            }
            connect();

        } catch (Exception e) {
            LOGGER.error("error starting bot " + email, e);
            return false;
        }
        return true;
    }

    private String registerClient() throws IOException {
        String idFa = UUID.randomUUID().toString();
        JsonObject json = new JsonObject();
        json.addProperty("userAgent", "GT7690");
        json.addProperty("platform", "web");
        json.addProperty("deviceId", "Samsung galaxy S8+");
        json.addProperty("lang", "vn");
        json.addProperty("version", "1.0");
        json.addProperty("channel", "2|win888bot|1.0.6");
        json.addProperty("idFa", idFa);
        json.addProperty("gaId", PasswordUtil.MD5Password(idFa));
        String response = HTTPUtil.request(Configs.getInstance().getVerifyUrl() + "RegisterClient", json.toString());
        return response;
    }

    private String authenticate() throws IOException {
        JsonObject json = new JsonObject();
        json.addProperty("email", email);
        json.addProperty("password", PasswordUtil.MD5Password(password));
        json.addProperty("clientId", clientId);
        String response = HTTPUtil.request(Configs.getInstance().getVerifyUrl() + "Authorize", json.toString());
        return response;
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

    private void changeUsername(boolean sendAPI) {
        try {
            List<String> names = Configs.getInstance().getListName();
            String name = names.get(Utils.nextInt(names.size()));
            if (sendAPI) {
                JsonObject json = new JsonObject();
                json.addProperty("clientId", clientId);
                json.addProperty("displayName", name);
                HTTPUtil.request(Configs.getInstance().getVerifyUrl() + "UpdateProfile", json.toString(), token);
            }
            SFSObject sfsObj = new SFSObject();
            sfsObj.putInt(SFSKey.ACTION_INCORE, SFSAction.UPDATE_PROFILE);
            sfsObj.putUtfString(SFSKey.DISPLAY_NAME, name);
            sfs.send(new ExtensionRequest(SFSCommand.CLIENT_REQUEST, sfsObj));
        } catch (Exception e) {
            LOGGER.error(email + " " + userId, e);
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
                    sfsGame.stop();
                } else {
                    disconnectGame();
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
            LOGGER.info(email + " is on");
            if (isMaster()) {
                betMoney = 0;
                getListBetMoney();
            }
        } else {
            LOGGER.info(email + " is off");
            if (isInGame()) {
                if (isPlaying()) {
                    sfsGame.stop();
                } else {
                    disconnectGame();
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
            LOGGER.error(email + " " + userId, e);
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
}
