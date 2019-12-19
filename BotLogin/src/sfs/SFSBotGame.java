/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sfs;

import com.smartfoxserver.v2.entities.data.ISFSArray;
import com.smartfoxserver.v2.entities.data.ISFSObject;
import com.smartfoxserver.v2.entities.data.SFSObject;
import com.smartfoxserver.v2.exceptions.SFSException;
import constant.Constant;
import game.command.BlackJackCommand;
import game.command.MauBinhCommand;
import game.command.SFSAction;
import game.command.SFSCommand;
import game.key.SFSKey;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import service.BotManager;
import service.Service;
import sfs2x.client.SmartFox;
import sfs2x.client.core.BaseEvent;
import sfs2x.client.core.IEventListener;
import sfs2x.client.core.SFSEvent;
import sfs2x.client.entities.Room;
import sfs2x.client.entities.User;
import sfs2x.client.requests.ExtensionRequest;
import sfs2x.client.requests.JoinRoomRequest;
import sfs2x.client.requests.LoginRequest;
import sfs2x.client.util.ConfigData;
import util.AutoArrangementBotNew;
import util.Card;
import util.CardSet;
import util.Configs;
import util.TldlBot;
import util.Utils;

/**
 *
 * @author vinhnp
 */
public class SFSBotGame implements IEventListener {

    private final Logger LOGGER = LoggerFactory.getLogger(getClass());
    private Timer timer;

    private String token;
    private final String zone;
    private final String userId;
    private byte serviceId;
    private final SmartFox sfs = new SmartFox();
    private final ConfigData cfg = new ConfigData();
    private boolean isOwner = false;
    private boolean isStartGame = false;
    private boolean isStop = false;
    private double betMoney;
    private byte moneyType;
    private int userType;
    private long timeLeave;
    private final SFSBot sfsBot;
    private final Short MINUS_ONE = -1;
    private final byte CARD_NONE = -1;

    private Room room;
    private String roomName;
    private List<Short> cards;
    private final List<String> userIds = new ArrayList<>(); // list userId
    private final List<String> userChecked = new ArrayList<>(); // danh sách user đã xét bài
    private final Map<String, Integer> userGetCards = new HashMap<>(); // danh sach user rut bai game xi dach, user nao ko co trong list la ko rut bai
    private final Map<String, Byte> userSeats = new HashMap<>();
    private int point;
    private byte[] myCards;
    private byte[] movedCards;
    private final SFSObject pingObj = new SFSObject();
    private final boolean isCallBot = Utils.nextBoolean();

    private void initSmartFox() {
        sfs.addEventListener(SFSEvent.CONNECTION, this);
        sfs.addEventListener(SFSEvent.CONNECTION_LOST, this);
        sfs.addEventListener(SFSEvent.LOGIN, this);
        sfs.addEventListener(SFSEvent.LOGIN_ERROR, this);
        sfs.addEventListener(SFSEvent.LOGOUT, this);
        sfs.addEventListener(SFSEvent.ROOM_JOIN, this);
        sfs.addEventListener(SFSEvent.USER_ENTER_ROOM, this);
        sfs.addEventListener(SFSEvent.USER_EXIT_ROOM, this);
        sfs.addEventListener(SFSEvent.ROOM_VARIABLES_UPDATE, this);
        sfs.addEventListener(SFSEvent.MODERATOR_MESSAGE, this);
        sfs.addEventListener(SFSEvent.EXTENSION_RESPONSE, new IEventListener() {
            @Override
            public void dispatch(BaseEvent e) throws SFSException {
                SFSObject sfsObj = (SFSObject) e.getArguments().get("params");
                if (Configs.getInstance().isLogEnable()) {
                    LOGGER.info(sfsObj.getDump());
                }
                if (sfsObj.containsKey(SFSKey.ACTION_INCORE)) {
                    int action = sfsObj.getInt(SFSKey.ACTION_INCORE);
                    processCoreAction(action);
                } else {
                    int action = sfsObj.getInt(SFSKey.ACTION_INGAME);
                    processGameAction(action, sfsObj);
                }
            }
        });
    }

    private void processCoreAction(int action) {
        switch (action) {
            case SFSAction.JOIN_ZONE_SUCCESS:
                Utils.sleep(1000);
                if (roomName == null) {
                    LOGGER.info(sfsBot.getEmail() + " create room " + betMoney);
                    sendCreateBoard(betMoney);
                } else {
                    LOGGER.info(sfsBot.getEmail() + " request join room " + roomName);
                    sfs.send(new JoinRoomRequest(roomName));
                }
                break;
        }
    }

    private void processGameAction(int action, SFSObject sfsObj) {
        switch (action) {
            case SFSAction.START_GAME:
                startGame(sfsObj);
                break;
            case BlackJackCommand.OWNER_CHECK_CARD:
                processOwnerCheckCard(sfsObj);
                break;
            case BlackJackCommand.GET_CARD:
                processBlackJackGetCard(sfsObj);
                break;
            case 101:
                switch (serviceId) {
                    case Service.BLACKJACK:
                        processBlackJackTurn(sfsObj);
                        break;
                    case Service.TIENLEN:
                        processTienLenFinish(sfsObj);
                        break;
                }
                break;
            case SFSAction.RESULT:
                processResultMessage(sfsObj);
                break;
            case MauBinhCommand.FINISH:
                break;
            case MauBinhCommand.AUTO_ARRANGE:
                finishMB(sfsObj);
                break;
            case SFSAction.UPDATE_VARIABLE_USER:
                break;
            case SFSAction.ADD_PLAYER:
                String joinId = sfsObj.getUtfString("idDBUser");
                byte seat = sfsObj.getByte(SFSKey.SEAT_USER);
                userSeats.put(joinId, seat);
                userIds.add(joinId);
                if (Utils.nextBoolean()) {
                    Utils.sleepRandom(3000, 4000);
                    sendQuickPlay();
                }
                break;
            case SFSAction.JOIN_BOARD:
                ISFSArray arr = sfsObj.getSFSArray("array");
                for (int i = 0; i < arr.size(); i++) {
                    ISFSObject userObj = arr.getSFSObject(i);
                    String id = userObj.getUtfString("idDBUser");
                    userIds.add(id);
                    userSeats.put(id, userObj.getByte(SFSKey.SEAT_USER));
                }
                if (Utils.nextBoolean()) {
                    Utils.sleepRandom(2000);
                    sendQuickPlay();
                }
                break;
            case SFSAction.LEAVE_GAME:
                String leaveId = sfsObj.getUtfString(SFSKey.USER_ID);
                if (!leaveId.equals(userId)) {
                    userIds.remove(leaveId);
                    userSeats.remove(leaveId);
                    if (!isStartGame) {
                        if (countUser() == 0) {
                            LOGGER.info(sfsBot.getEmail() + " all user leave room " + roomName);
                            leaveRoom();
                        }
                    }
                }
                break;
            case SFSAction.STOP_GAME:
                isStartGame = false;
                if (serviceId == Service.TIENLEN) {
                    if (room.getUserCount() > 2) {
                        isStop = true;
                    }
                }

                if (isStop) {
                    leaveRoom();
                    isStop = false;
                    return;
                }

                int countUser = countUser();
                if (countUser == 0) {
                    LOGGER.info(sfsBot.getEmail() + " all user leave room on stop game " + roomName);
                    leaveRoom();
                    return;
                } else if (countUser == 3) {
                    if (serviceId == Service.MAUBINH) {
                        LOGGER.info(sfsBot.getEmail() + " leave room 3 users " + roomName);
                        Utils.sleepRandom(7000, 9000);
                        leaveRoom();
                        return;
                    }
                }

                if (timeLeave <= System.currentTimeMillis()) {
                    if (serviceId == Service.MAUBINH) {
                        Utils.sleepRandom(7000, 9000);  // mb rời bàn chậm hơn để chờ so bài xong
                    } else {
                        Utils.sleepRandom(2000, 4000);
                    }

                    leaveRoom();

                    if (countUser == 1) {
                        LOGGER.info(roomName + " all bot leave, recall bot");
                        BotManager.getInstance().recallBot(room.getName(), betMoney, moneyType, serviceId);
                    }

                } else {
                    Utils.sleepRandom(5000);
                    if (serviceId == Service.BAI_CAO || serviceId == Service.BLACKJACK) {
                        if (Utils.nextBoolean()) {
                            changeBetMoney();
                        }
                    }
                    if (Utils.nextBoolean()) {
                        sendQuickPlay();
                    }
                }
                break;

            case SFSAction.MOVE:
                processMove(sfsObj);
                break;

            case SFSAction.SKIP:
                processSkip(sfsObj);
                break;

            case 46:
                processUserCards(sfsObj);
                break;
        }
    }

    public SFSBotGame(SFSBot sfsBot, String token, String host, int port, String zone, double betMoney, byte moneyType, int userType, String roomName, String userId, byte serviceId) throws Exception {

        initSmartFox();

        this.sfsBot = sfsBot;
        this.token = token;
        this.zone = zone;
        this.roomName = roomName;
        this.betMoney = betMoney;
        this.moneyType = moneyType;
        this.userType = userType;
        this.userId = userId;
        this.serviceId = serviceId;
        cfg.setHost(host);
        cfg.setPort(port);
        cfg.setZone(zone);
        
        pingObj.putInt(SFSKey.ACTION_INGAME, SFSAction.CONTINUE_GAME);
    }

    @Override
    public void dispatch(BaseEvent e) throws SFSException {
        switch (e.getType()) {
            case SFSEvent.CONNECTION:
                boolean success = (Boolean) e.getArguments().get("success");
                LOGGER.info(sfsBot.getEmail() + " connect game " + success);
                if (success) {
                    login();
                }
                break;

            case SFSEvent.CONNECTION_LOST:
                LOGGER.info(sfsBot.getEmail() + " lost connection " + e.getArguments().toString());
                if (timer != null) {
                    timer.cancel();
                }
                if (Configs.getInstance().isStop()) {
                    sfsBot.disconnect();
                } else {
                    sfsBot.setRoomName(null);
                    sfsBot.leaveLobby();
                }
                break;

            case SFSEvent.LOGIN:
                LOGGER.info(sfsBot.getEmail() + " login success");
                sfs.enableLagMonitor(true, 30);
                break;

            case SFSEvent.LOGIN_ERROR:
                String error = e.getArguments().get("errorMessage").toString();
                LOGGER.info(sfsBot.getEmail() + " login error:  " + error);
                sfs.disconnect();
                break;

            case SFSEvent.LOGOUT:
                sfs.disconnect();
                break;

            case SFSEvent.ROOM_JOIN:
                room = (Room) e.getArguments().get("room");
                if (serviceId == Service.TIENLEN && room.getUserCount() > 2) {
                    Utils.sleep(1000);
                    leaveRoom();
                    break;
                }
                
                if (serviceId == Service.MAUBINH) {
                    if (countUser() == 3) {
                        LOGGER.info(sfsBot.getEmail() + " leave room 3 users");
                        leaveRoom();
                        break;
                    }
                }
                
                sendAutoBuyin();
                LOGGER.info(sfsBot.getEmail() + " join room " + room.getName());
                roomName = room.getName();
                sfsBot.setRoomName(roomName);
                if (room.getVariable("ID_OWNER") != null) {
                    String ownerId = room.getVariable("ID_OWNER").getStringValue();
                    isOwner = ownerId.equals(userId);
                } else {
                    isOwner = true;
                }

                timer = new Timer();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        sendContinueGame();
                    }
                }, 0, 5000);

                timeLeave = 0;
                if (isOwner && isCallBot) {
                    BotManager.getInstance().callBot(room.getName(), betMoney, moneyType, serviceId);
                }
                break;

            case SFSEvent.ROOM_VARIABLES_UPDATE:
                LOGGER.info("ROOM_VARIABLES_UPDATE " + e.getArguments().toString());
                String ownerId = room.getVariable("ID_OWNER").getStringValue();
                isOwner = ownerId.equals(userId);
                //todo: check user type
                break;

            case SFSEvent.MODERATOR_MESSAGE:
                LOGGER.info(sfsBot.getEmail() + " MODERATOR_MESSAGE " + e.getArguments().toString());
                break;

            case SFSEvent.USER_ENTER_ROOM:
                User user = (User) e.getArguments().get("user");
                LOGGER.info(user.getName() + " " + user.getVariable("displayName").getStringValue() + " enter room");
                if (serviceId == Service.TIENLEN && room.getUserCount() > 2 && !isStartGame) {
                    if (Utils.nextInt(10) == 9) {
                        Utils.sleep(1000);
                    }
                    leaveRoom();
                }

                if (serviceId == Service.MAUBINH && !isStartGame) {
                    if (countUser() == 3) {
                        Utils.sleep(1000);
                        leaveRoom();
                    }
                }
                break;

            case SFSEvent.USER_EXIT_ROOM:
                user = (User) e.getArguments().get("user");
                LOGGER.info(user.getName() + " " + user.getVariable("displayName").getStringValue() + " exit room");
                break;
        }
    }

    public void connect() {
        LOGGER.info(sfsBot.getEmail() + " connect game");
        sfs.connect(cfg);
    }

    public void disconnect() {
        sfs.disconnect();
    }

    public boolean isConnected() {
        return sfs.isConnected();
    }

    public boolean isOwner() {
        return isOwner;
    }

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public void setBetMoney(double betMoney) {
        this.betMoney = betMoney;
    }

    public void setServiceId(byte serviceId) {
        this.serviceId = serviceId;
    }

    public void setMoneyType(byte moneyType) {
        this.moneyType = moneyType;
    }

    public void setUserType(int userType) {
        this.userType = userType;
    }

    private void login() {
        SFSObject params = new SFSObject();
        params.putInt(SFSKey.LOGIN_TYPE, 3);
        params.putUtfString(SFSKey.LOGIN_TOKEN, token);
        LoginRequest rq = new LoginRequest("", "", zone, params);
        sfs.send(rq);
    }

    private void sendCreateBoard(double betMoney) {
        SFSObject sfsObj = new SFSObject();
        sfsObj.putInt(SFSKey.ACTION_INCORE, SFSAction.CREATE_BOARD);
        sfsObj.putDouble(SFSKey.BET_BOARD, betMoney);
        sfsObj.putBool(SFSKey.IS_OWNER, true);
        sfs.send(new ExtensionRequest(SFSCommand.CLIENT_REQUEST, sfsObj));
    }

    private void sendOpenCard(List<Short> cards) {
        SFSObject sfsObj = new SFSObject();
        sfsObj.putInt(SFSKey.ACTION_INGAME, SFSAction.MOVE);
        sfsObj.putShortArray(SFSKey.ARRAY_INFOR_CARD, cards);
        sfs.send(new ExtensionRequest(SFSCommand.CLIENT_REQUEST_INGAME, sfsObj, room));
    }

    private void sendQuickPlay() {
        SFSObject sfsObj = new SFSObject();
        sfsObj.putInt(SFSKey.ACTION_INGAME, SFSAction.QUICK_PLAY);
        sfs.send(new ExtensionRequest(SFSCommand.CLIENT_REQUEST_INGAME, sfsObj, room));
    }

    private void sendContinueGame() {
        if (!sfs.isConnected()) {
            return;
        }
        sfs.send(new ExtensionRequest(SFSCommand.CLIENT_REQUEST_INGAME, pingObj, room));
    }

    private void processOwnerCheckCard(SFSObject sfsObj) {
        if (sfsObj.getUtfString(SFSKey.USER_ID).equals(userId)) {
            point = sfsObj.getInt(SFSKey.POINT);
        }
    }

    private void processOwnerCheckCard() {
        if (point < 15) {
            Utils.sleepRandom(3000, 8000);
            getCard(Constant.GET_CARD);
            return;
        }

        if (point >= 18) {
            if (Utils.nextBoolean()) {
                Utils.sleepRandom(3000, 5000);
                sendBlackJackCheckCard(Constant.CHECK_ALL, serviceId);
            } else {
                for (String id : userIds) {
                    if (!userChecked.contains(id)) {
                        userChecked.add(id);
                        Utils.sleepRandom(3000, 5000);
                        sendBlackJackCheckCard(Constant.CHECK_ONE, userSeats.get(id));
                        return;
                    }
                }
            }
        }

        switch (point) {
            case 15:
            case 16:
                for (String id : userIds) {
                    if (userChecked.contains(id)) {
                        continue;   // user đã xét -> bỏ qua
                    }

                    if (userGetCards.containsKey(id)) {
                        Integer cardNum = userGetCards.get(id);
                        int percent = cardNum == 1 ? 5 : 8; // 50% xét nhà 3 lá, 80% xét nhà 4,5 lá
                        if (Utils.nextInt(10) < percent) {
                            userChecked.add(id);
                            Utils.sleepRandom(3000, 5000);
                            sendBlackJackCheckCard(Constant.CHECK_ONE, userSeats.get(id));
                            return;
                        }
                    }
                }
                break;

            case 17:
                for (String id : userIds) {
                    if (userChecked.contains(id)) {
                        continue;   // user đã xét -> bỏ qua
                    }

                    int percent = userGetCards.containsKey(id) ? 9 : 5; // 90% xét nhà 3 lá trở lên, 50% xét nhà 2 lá
                    if (Utils.nextInt(10) < percent) {
                        userChecked.add(id);
                        Utils.sleepRandom(3000, 5000);
                        sendBlackJackCheckCard(Constant.CHECK_ONE, userSeats.get(id));
                        return;
                    }
                }
        }

        // còn user chưa xét -> rút
        Utils.sleepRandom(3000, 8000);
        getCard(Constant.GET_CARD);
    }

    private void getCard(int type) {
        SFSObject obj = new SFSObject();
        obj.putInt(SFSKey.ACTION_INGAME, SFSAction.MOVE);
        obj.putUtfString(SFSKey.USER_ID, userId);
        obj.putByte(SFSKey.TYPE, (byte) type);
        sfs.send(new ExtensionRequest(SFSCommand.CLIENT_REQUEST_INGAME, obj, room));
    }

    //process for maubinh
    private void autoArrange() {
        SFSObject obj = new SFSObject();
        obj.putInt(SFSKey.ACTION_INGAME, MauBinhCommand.AUTO_ARRANGE);
        sfs.send(new ExtensionRequest(SFSCommand.CLIENT_REQUEST_INGAME, obj, room));
    }

    private void finishMB(SFSObject sfsObj) {
        SFSObject obj = new SFSObject();
        obj.putInt(SFSKey.ACTION_INGAME, MauBinhCommand.FINISH);
        obj.putShortArray(SFSKey.ARRAY_INFOR_CARD, sfsObj.getShortArray(SFSKey.ARRAY_INFOR_CARD));
        sfs.send(new ExtensionRequest(SFSCommand.CLIENT_REQUEST_INGAME, obj, room));
    }

    private void startGame(SFSObject sfsObj) {
        isStartGame = true;
        if (timeLeave == 0) {
            timeLeave = System.currentTimeMillis() + (sfsBot.getConfig().getTimeChangeBoardFrom()
                        + Utils.nextInt(sfsBot.getConfig().getTimeChangeBoardTo() - sfsBot.getConfig().getTimeChangeBoardFrom())) * 1000;
        }
        switch (serviceId) {
            case Service.BAI_CAO:
                startGameBaiCao(sfsObj);
                break;
            case Service.BLACKJACK:
                startGameBlackJack(sfsObj);
                break;
            case Service.MAUBINH:
                startGameMauBinh(sfsObj);
                break;
            case Service.TIENLEN:
                startGameTienLen(sfsObj);
                break;
        }
    }

    private void startGameBaiCao(SFSObject sfsObj) {
        cards = new ArrayList(sfsObj.getShortArray(SFSKey.ARRAY_INFOR_CARD));
        Utils.sleepRandom(2000, 5000);
        if (Utils.nextBoolean()) {
            sendOpenCard(cards);
        } else {
            List<Short> openCards = new ArrayList<>();
            openCards.add(cards.get(0));
            openCards.add(MINUS_ONE);
            openCards.add(MINUS_ONE);
            sendOpenCard(openCards);

            Utils.sleepRandom(1000, 2000);
            openCards.clear();
            openCards.add(cards.get(0));
            openCards.add(cards.get(1));
            openCards.add(MINUS_ONE);
            sendOpenCard(openCards);

            Utils.sleepRandom(1000, 2000);
            sendOpenCard(cards);
        }
    }

    private void startGameTienLen(SFSObject sfsObj) {
        myCards = convertShortArray(new ArrayList(sfsObj.getShortArray(SFSKey.ARRAY_INFOR_CARD)));
        String uId = sfsObj.getUtfString("ui");
        if (uId.equals(sfs.getMySelf().getName())) {
            Utils.sleep(1500);
            movedCards = null;
            getUserCards();
        }
    }

    private void startGameBlackJack(SFSObject sfsObj) {
        point = 0;
        userGetCards.clear();
        userChecked.clear();
        cards = new ArrayList(sfsObj.getShortArray(SFSKey.ARRAY_INFOR_CARD));
        String id = sfsObj.getUtfString(SFSKey.USER_ID);
        if (id.equals(userId)) {
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    processBlackJackTurn();
                }
            }, 10);
        }
    }

    private void startGameMauBinh(SFSObject sfsObj) {
//        byte time = sfsObj.getByte(MauBinhCommand.LIMIT_TIME);
        myCards = convertShortArray(new ArrayList(sfsObj.getShortArray(SFSKey.ARRAY_INFOR_CARD)));
        byte[] arrangedCards = AutoArrangementBotNew.getBestSolution(myCards);
        Utils.sleepRandom(3000, 40000);
        if (arrangedCards != null) {
            SFSObject obj = new SFSObject();
            obj.putInt(SFSKey.ACTION_INGAME, MauBinhCommand.FINISH);
            obj.putShortArray(SFSKey.ARRAY_INFOR_CARD, convertByteArray(arrangedCards));
            sfs.send(new ExtensionRequest(SFSCommand.CLIENT_REQUEST_INGAME, obj, room));
        } else {
            autoArrange();
        }
    }

    private void processBlackJackGetCard(ISFSObject sfsObj) {
        String uId = sfsObj.getUtfString(SFSKey.USER_ID);
        if (uId.equals(userId)) {
            cards.add(sfsObj.getByte(SFSKey.INFOR_CARD).shortValue());
        } else if (isOwner) {
            Integer numOfCards = userGetCards.get(uId);
            if (numOfCards == null) {   // user chua rut la nao
                userGetCards.put(uId, 1);
            } else {
                userGetCards.put(uId, numOfCards + 1);
            }
        }
    }

    private void sendBlackJackCheckCard(byte type, byte seat) {
        SFSObject sfsObj = new SFSObject();
        sfsObj.putInt(SFSKey.ACTION_INGAME, SFSAction.CHECK_CARD);
        sfsObj.putByte(SFSKey.TYPE, type);
        sfsObj.putByte(SFSKey.SEAT_USER, seat);
        sfs.send(new ExtensionRequest(SFSCommand.CLIENT_REQUEST_INGAME, sfsObj, room));
    }

    private void processResultMessage(ISFSObject sfsObj) {
        switch (serviceId) {
            case Service.BLACKJACK:
                processResultMessageBlackJack(sfsObj);
                break;
            default:
        }
    }

    private void processResultMessageBlackJack(ISFSObject sfsObj) {
        String id = sfsObj.getUtfString(SFSKey.USER_ID);
        if (id.equals(userId) && sfsObj.containsKey(SFSKey.POINT)) {
            point = sfsObj.getInt(SFSKey.POINT);
        }
    }

    private void processBlackJackTurn(ISFSObject sfsObj) {
        if (sfsObj.getUtfString(SFSKey.USER_ID).equals(userId)) {
            if (isOwner) {
                processOwnerCheckCard();
            } else {
                processBlackJackTurn();
            }
        }
    }

    private void processBlackJackTurn() {
        byte type;
        if (cards.size() == 5) {
            Utils.sleep(1000);
            getCard(Constant.STOP_GET_CARD);
            return;
        }
        Utils.sleepRandom(3000, 8000);
        if (point == 0 || point >= 18) {
            getCard(Constant.STOP_GET_CARD);
        } else if (point == 17) {
            type = Utils.nextInt(10) < 2 ? Constant.GET_CARD : Constant.STOP_GET_CARD;
            getCard(type);
        } else if (point == 16) {
            type = Utils.nextBoolean() ? Constant.GET_CARD : Constant.STOP_GET_CARD;
            getCard(type);
        } else {
            getCard(Constant.GET_CARD);
        }
    }

    private void processMove(ISFSObject sfsObj) {
        switch (serviceId) {
            case Service.TIENLEN:
                processTienLenMove(sfsObj);
                break;
        }
    }

    private void processSkip(ISFSObject sfsObj) {
        switch (serviceId) {
            case Service.TIENLEN:
                processTienLenSkip(sfsObj);
                break;
        }
    }

    private void processTienLenMove(ISFSObject sfsObj) {
        String uId = sfsObj.getUtfString("uicurr");
        String userIdMove = sfsObj.getUtfString("ui");
        movedCards = convertShortArray(new ArrayList(sfsObj.getShortArray(SFSKey.ARRAY_INFOR_CARD)));
        boolean isChatHeo = sfsObj.getBool("sIsBiChat");
        if (uId.equals(userId)) {
            getUserCards();
        }
    }

    private void processTienLenSkip(ISFSObject sfsObj) {
        String uId = sfsObj.getUtfString("uicurr");
        if (uId.equals(userId)) {
            boolean isSkipAll = sfsObj.getBool("sClCrd");
            if (isSkipAll) {
                movedCards = null;
            }
            getUserCards();
        }
    }

    private void processTienLenFinish(ISFSObject sfsObj) {
    }

    private void sendTienLenMove(byte[] cards) {
        SFSObject sfsObj = new SFSObject();
        sfsObj.putInt(SFSKey.ACTION_INGAME, SFSAction.MOVE);
        sfsObj.putShortArray("cards", convertByteArray(cards));
        sfs.send(new ExtensionRequest(SFSCommand.CLIENT_REQUEST_INGAME, sfsObj, room));
    }

    private void sendBetRequest(double newBet) {
        SFSObject sfsObj = new SFSObject();
        sfsObj.putInt(SFSKey.ACTION_INGAME, SFSAction.BET);
        sfsObj.putDouble(SFSKey.MONEY_BET, newBet);
        sfs.send(new ExtensionRequest(SFSCommand.CLIENT_REQUEST_INGAME, sfsObj, room));
    }

    private void leaveRoom() {
        SFSObject sfsObj = new SFSObject();
        sfsObj.putInt(SFSKey.ACTION_INGAME, SFSAction.LEAVE_GAME);
        sfs.send(new ExtensionRequest(SFSCommand.CLIENT_REQUEST_INGAME, sfsObj, room));
    }

    private void changeBetMoney() {
        try {
            int maxBet = (int) (room.getVariable("MAX_BET_BOARD").getDoubleValue() / betMoney);
            double newBet = (1 + Utils.nextInt(maxBet)) * betMoney;
            sendBetRequest(newBet);
        } catch (Exception e) {
            LOGGER.error("", e);
        }
    }

    private List<Short> convertByteArray(byte[] cards) {
        List<Short> list = new ArrayList<>();
        for (byte card : cards) {
            list.add(new Short(card));
        }
        return list;
    }

    private byte[] convertShortArray(List<Short> listCard) {
        byte[] cards = new byte[listCard.size()];
        for (int i = 0; i < listCard.size(); i++) {
            cards[i] = listCard.get(i).byteValue();
        }
        return cards;
    }

    private void sendTienLenSkip() {
        SFSObject sfsObj = new SFSObject();
        sfsObj.putInt(SFSKey.ACTION_INGAME, SFSAction.SKIP);
        sfs.send(new ExtensionRequest(SFSCommand.CLIENT_REQUEST_INGAME, sfsObj, room));
    }

    private byte getBiggerCard(byte card) {
        for (int i = 0; i < myCards.length; i++) {
            if (myCards[i] > card) {
                return myCards[i];
            }
        }
        return CARD_NONE;
    }

    private byte[] getBiggerPair(byte[] cards) {
        if (cards[0] >= 48) { // doi heo
            byte[] hang = getBonDoiThong();
            if (hang != null) {
                return hang;
            } else {
                hang = getTuQuy();
                if (hang != null) {
                    return hang;
                }
            }
        }

        for (int i = 0; i < myCards.length; i++) {
            if (myCards[i] > cards[0] && myCards[i] > cards[1]) {
                Card card1 = CardSet.getCard(myCards[i]);
                for (int j = i + 1; j < myCards.length; j++) {
                    if (myCards[j] < 0) {
                        continue;
                    }
                    Card card2 = CardSet.getCard(myCards[j]);
                    if (card2.getCardNumber() == card1.getCardNumber()) {
                        return new byte[]{myCards[i], myCards[j]};
                    }
                }
            }
        }
        return null;
    }

    private byte[] getBiggerTamCo(byte[] cards) {
        for (int i = 0; i < myCards.length; i++) {
            if (myCards[i] > cards[0]) {
                Card card1 = CardSet.getCard(myCards[i]);
                for (int j = i + 1; j < myCards.length; j++) {
                    if (myCards[j] < 0) {
                        continue;
                    }
                    Card card2 = CardSet.getCard(myCards[j]);
                    if (card2.getCardNumber() == card1.getCardNumber()) {
                        for (int k = j + 1; k < myCards.length; k++) {
                            if (myCards[k] < 0) {
                                continue;
                            }
                            Card card3 = CardSet.getCard(myCards[k]);
                            if (card3.getCardNumber() == card1.getCardNumber()) {
                                return new byte[]{myCards[i], myCards[j], myCards[k]};
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    private byte[] getBiggerSanh(byte[] cards) {
        byte max = cards[cards.length - 1];
        for (int i = 0; i < myCards.length; i++) {
            if (myCards[i] > max && myCards[i] < 48) {
                byte[] sanh = new byte[cards.length];
                sanh[cards.length - 1] = myCards[i];
                int id = cards.length - 2;
                for (int j = i - 1; j >= 0; j--) {
                    if (myCards[j] < 0) {
                        continue;
                    }
                    Card card1 = CardSet.getCard(myCards[j]);
                    Card card2 = CardSet.getCard(sanh[id + 1]);
                    if (card1.getCardNumber() == card2.getCardNumber() - 1) {
                        sanh[id] = myCards[j];
                        id--;
                        if (id < 0) {
                            return sanh;
                        }
                    }
                }
            }
        }
        return null;
    }

    private byte[] getBiggerCards(byte[] cards) {
        byte[] biggerCards = null;
        switch (cards.length) {
            case 1:
                if (cards[0] >= 48) {   // heo
                    biggerCards = getHang();
                    if (biggerCards != null) {
                        break;
                    }
                }
                byte card = getBiggerCard(cards[0]);
                if (card > CARD_NONE) {
                    biggerCards = new byte[]{card};
                }
                break;

            case 2:
                biggerCards = getBiggerPair(cards);
                break;

            default:
                if (isTamCo(cards)) {
                    biggerCards = getBiggerTamCo(cards);
                } else if (isSanh(cards)) {
                    biggerCards = getBiggerSanh(cards);
                }
        }
        return biggerCards;
    }

    private byte[] getHang() {
        byte[] hang = getBonDoiThong();
        if (hang == null) {
            hang = getTuQuy();
            if (hang == null) {
                hang = getBaDoiThong();
            }
        }
        return hang;
    }

    private byte[] getBaDoiThong() {
        for (int i = 5; i < myCards.length; i++) {
            Card card1 = CardSet.getCard(myCards[i - 5]);
            Card card2 = CardSet.getCard(myCards[i - 4]);
            Card card3 = CardSet.getCard(myCards[i - 3]);
            Card card4 = CardSet.getCard(myCards[i - 2]);
            Card card5 = CardSet.getCard(myCards[i - 1]);
            Card card6 = CardSet.getCard(myCards[i]);
            if (card1 == null || card2 == null || card3 == null || card4 == null || card5 == null || card6 == null) {
                continue;
            }
            if (card1.getCardNumber() == card2.getCardNumber() && card2.getCardNumber() == card3.getCardNumber() - 1
                    && card3.getCardNumber() == card4.getCardNumber() && card4.getCardNumber() == card5.getCardNumber() - 1
                    && card5.getCardNumber() == card6.getCardNumber() && !card5.isHeo()) {
                return new byte[]{myCards[i - 5], myCards[i - 4], myCards[i - 3], myCards[i - 2], myCards[i - 1], myCards[i]};
            }
        }
        return null;
    }

    private byte[] getBonDoiThong() {
        for (int i = 7; i < myCards.length; i++) {
            Card card1 = CardSet.getCard(myCards[i - 7]);
            Card card2 = CardSet.getCard(myCards[i - 6]);
            Card card3 = CardSet.getCard(myCards[i - 5]);
            Card card4 = CardSet.getCard(myCards[i - 4]);
            Card card5 = CardSet.getCard(myCards[i - 3]);
            Card card6 = CardSet.getCard(myCards[i - 2]);
            Card card7 = CardSet.getCard(myCards[i - 1]);
            Card card8 = CardSet.getCard(myCards[i]);
            if (card1 == null || card2 == null || card3 == null || card4 == null || card5 == null || card6 == null || card7 == null || card8 == null) {
                continue;
            }
            if (card1.getCardNumber() == card2.getCardNumber() && card2.getCardNumber() == card3.getCardNumber() - 1
                    && card3.getCardNumber() == card4.getCardNumber() && card4.getCardNumber() == card5.getCardNumber() - 1
                    && card5.getCardNumber() == card6.getCardNumber() && card6.getCardNumber() == card7.getCardNumber() - 1
                    && card7.getCardNumber() == card8.getCardNumber() && !card7.isHeo()) {
                return new byte[]{myCards[i - 7], myCards[i - 6], myCards[i - 5], myCards[i - 4],
                    myCards[i - 3], myCards[i - 2], myCards[i - 1], myCards[i]};
            }
        }
        return null;
    }

    private byte[] getTuQuy() {
        for (int i = 0; i < myCards.length; i++) {
            if (myCards[i] < 0) {
                continue;
            }
            Card card1 = CardSet.getCard(myCards[i]);
            for (int j = i + 1; j < myCards.length; j++) {
                if (myCards[j] < 0 || i == j) {
                    continue;
                }
                Card card2 = CardSet.getCard(myCards[j]);
                if (card2.getCardNumber() == card1.getCardNumber()) {
                    for (int k = j + 1; k < myCards.length; k++) {
                        if (myCards[k] < 0 || k == i || k == j) {
                            continue;
                        }
                        Card card3 = CardSet.getCard(myCards[k]);
                        if (card3.getCardNumber() == card1.getCardNumber()) {
                            for (int l = k + 1; l < myCards.length; l++) {
                                if (myCards[l] < 0) {
                                    continue;
                                }
                                Card card4 = CardSet.getCard(myCards[l]);
                                if (card4.getCardNumber() == card1.getCardNumber()) {
                                    return new byte[]{myCards[i], myCards[j], myCards[k], myCards[l]};
                                }
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    private boolean isTamCo(byte[] cards) {
        if (cards.length != 3) {
            return false;
        }
        Card card1 = CardSet.getCard(cards[0]);
        Card card2 = CardSet.getCard(cards[1]);
        Card card3 = CardSet.getCard(cards[2]);
        return card1.getCardNumber() == card2.getCardNumber() && card2.getCardNumber() == card3.getCardNumber();
    }

    private boolean isSanh(byte[] cards) {
        for (int i = 1; i < cards.length; i++) {
            Card card1 = CardSet.getCard(cards[i - 1]);
            Card card2 = CardSet.getCard(cards[i]);
            if (card1.getCardNumber() != card2.getCardNumber() - 1) {
                return false;
            }
        }
        return true;
    }

    public boolean isPlaying() {
        return isStartGame;
    }

    public void stop() {
        isStop = true;
    }

    private void getUserCards() {
        SFSObject sfsObj = new SFSObject();
        sfsObj.putInt(SFSKey.ACTION_INGAME, 46);
        sfs.send(new ExtensionRequest(SFSCommand.CLIENT_REQUEST_INGAME, sfsObj, room));
    }

    private void processUserCards(ISFSObject sfsObj) {
        if (Configs.getInstance().isLogEnable()) {
            LOGGER.info("cheat: " + sfsObj.getDump());
        }
        ISFSArray arr = sfsObj.getSFSArray("sfsArray");
        byte[] op1Cards = null;
        byte[] op2Cards = null;
        byte[] op3Cards = null;
        myCards = null;
        boolean op1inRound = false;
        boolean op2inRound = false;
        boolean op3inRound = false;
        for (int i = 0; i < arr.size(); i++) {
            ISFSObject userObj = arr.getSFSObject(i);
            String uId = userObj.getUtfString("userId");
            byte[] uCards = convertShortArray(new ArrayList(userObj.getShortArray("arrCards")));
            boolean isInRound = userObj.getBool("isSkip") == false;
            if (uId.equals(userId)) {
                myCards = uCards;
            } else if (myCards != null) {
                if (op1Cards == null) {
                    op1Cards = uCards;
                    op1inRound = isInRound;
                } else if (op2Cards == null) {
                    op2Cards = uCards;
                    op2inRound = isInRound;
                } else {
                    op3Cards = uCards;
                    op3inRound = isInRound;
                }
            }
        }

        for (int i = 0; i < arr.size(); i++) {
            ISFSObject userObj = arr.getSFSObject(i);
            String uId = userObj.getUtfString("userId");
            byte[] uCards = convertShortArray(new ArrayList(userObj.getShortArray("arrCards")));
            boolean isInRound = userObj.getBool("isSkip") == false;
            if (op3Cards != null || uId.equals(userId)) {
                break;
            } else if (myCards != null) {
                if (op1Cards == null) {
                    op1Cards = uCards;
                    op1inRound = isInRound;
                } else if (op2Cards == null) {
                    op2Cards = uCards;
                    op2inRound = isInRound;
                } else {
                    op3Cards = uCards;
                    op3inRound = isInRound;
                }
            }
        }

//        if (movedCards != null) {
//            System.out.println();
//            System.out.print("currentCards: ");
//            for (byte id : movedCards) {
//                System.out.print(id + " ");
//            }
//        }
//        if (myCards != null) {
//            System.out.println();
//            System.out.print("bot cards: ");
//            for (byte id : myCards) {
//                System.out.print(id + " ");
//            }
//        }
//        System.out.println();
//        System.out.print("user cards: ");
//        for (byte id : op1Cards) {
//            System.out.print(id + " ");
//        }
        movedCards = TldlBot.getCards(movedCards, myCards, op1Cards, op2Cards, op3Cards, op1inRound, op2inRound, op3inRound, false, true);
        Utils.sleepRandom(5000, 7000);
        if (movedCards != null && movedCards.length > 0) {
            sendTienLenMove(movedCards);
        } else {
            sendTienLenSkip();
        }
    }

    private void sendAutoBuyin() {
        SFSObject sfsObj = new SFSObject();
        sfsObj.putInt(SFSKey.ACTION_INGAME, SFSAction.AUTO_BUY_IN);
        sfsObj.putBool("isAutoBuyIn", true);
        sfs.send(new ExtensionRequest(SFSCommand.CLIENT_REQUEST_INGAME, sfsObj, room));
    }
    
    private int countUser() {
        int count = 0;
        List<User> users = room.getUserList();
        for (User user : users) {
            if (!Utils.isBot(user)) {
                count++;
            }
        }
        return count;
    }
}
