/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.vn.login.handler.request;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.smartfoxserver.v2.entities.Room;
import com.smartfoxserver.v2.entities.User;
import com.smartfoxserver.v2.entities.data.ISFSArray;
import com.smartfoxserver.v2.entities.data.ISFSObject;
import com.smartfoxserver.v2.entities.data.SFSArray;
import com.smartfoxserver.v2.entities.data.SFSObject;
import com.smartfoxserver.v2.entities.match.BoolMatch;
import com.smartfoxserver.v2.entities.match.MatchExpression;
import com.smartfoxserver.v2.entities.match.NumberMatch;
import com.smartfoxserver.v2.entities.match.RoomProperties;
import com.smartfoxserver.v2.entities.variables.SFSUserVariable;
import com.smartfoxserver.v2.entities.variables.UserVariable;
import com.smartfoxserver.v2.exceptions.SFSVariableException;
import com.smartfoxserver.v2.extensions.BaseClientRequestHandler;
import com.smartfoxserver.v2.extensions.ExtensionLogLevel;
import game.command.SFSAction;
import game.command.SFSCommand;
import game.key.SFSKey;
import game.vn.common.config.GoogleConfig;
import game.vn.common.config.QueueConfig;
import game.vn.common.config.RoomConfig;
import game.vn.common.config.ServerConfig;
import game.vn.common.config.UrlConfig;
import game.vn.common.constant.ExtensionConstant;
import game.vn.common.constant.MoneyContants;
import game.vn.common.device.Device;
import game.vn.common.lang.GameLanguage;
import game.vn.common.lib.api.APIResult;
import game.vn.common.lib.api.Transaction;
import game.vn.common.lib.event.EventData;
import game.vn.common.lib.hazelcast.Board;
import game.vn.common.lib.hazelcast.PlayingBoardManager;
import game.vn.common.lib.hazelcast.UserState;
import game.vn.common.lib.news.News;
import game.vn.common.lib.payment.PaymentInfo;
import game.vn.common.lib.payment.PaymentQueueObj;
import game.vn.common.lib.ranking.LeaderboardObject;
import game.vn.common.lib.taixiu.TaiXiuBetRequest;
import game.vn.common.lib.taixiu.TaiXiuBetResult;
import game.vn.common.lib.taixiu.TaiXiuBuyTicketRequest;
import game.vn.common.lib.taixiu.TaiXiuBuyTicketResult;
import game.vn.common.lib.taixiu.TaiXiuCommand;
import game.vn.common.lib.taixiu.TaiXiuQueueData;
import game.vn.common.lib.updateconfig.GameConfigInfor;
import game.vn.common.lib.updateconfig.GameConfigInforSortByPriority;
import game.vn.common.lib.vip.VipQueueObj;
import game.vn.common.message.MessageController;
import game.vn.common.object.ClientInfo;
import game.vn.common.object.Profile;
import game.vn.common.object.VerifyResponseData;
import game.vn.common.object.boardhistory.HistoryRequest;
import game.vn.common.properties.RoomInforPropertiesKey;
import game.vn.common.properties.UserInforPropertiesKey;
import game.vn.common.queue.QueueQuest;
import game.vn.common.queue.QueueService;
import game.vn.common.queue.QueueServiceApi;
import game.vn.common.queue.QueueTaiXiu;
import game.vn.common.config.TurnOffGameConfig;
import game.vn.common.lib.api.ConvertMoneyResult;
import game.vn.common.lib.api.P2PTransferConfig;
import game.vn.common.lib.api.RechargeCardDataInfo;
import game.vn.common.lib.api.PointConvertConfig;
import game.vn.common.lib.api.TransactionHistory;
import game.vn.common.lib.api.UserReceiveMoneyOffline;
import game.vn.common.lib.contants.UserType;
import game.vn.common.lib.iap.GGProductPurchase;
import game.vn.common.lib.payment.UserBalanceUpdate;
import game.vn.common.object.PointReceiveInfo;
import game.vn.common.object.W88VerifyResponseData;
import game.vn.common.service.BroadcastService;
import game.vn.util.APIUtils;
import game.vn.util.GsonUtil;
import game.vn.util.HazelcastUtil;
import game.vn.util.GlobalsUtil;
import game.vn.util.Utils;
import game.vn.util.db.Database;
import game.vn.util.db.TransferMoneyResult;
import game.vn.util.db.UpdateMoneyResult;
import game.vn.util.watchservice.TaiXiuConfig;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

/**
 * xử lý những command client gui len trong extension login
 *
 * @author tuanp
 */
public class CommonClientRequest extends BaseClientRequestHandler {

    private static final JsonParser PARSER = new JsonParser();
    private static final GameConfigInforSortByPriority GAME_CONFIG_INFOR_SORT_BY_MONEY_DESC = new GameConfigInforSortByPriority();
    private static final int ENGLISH_LOCALE_TYPE = 0;
    private static final int VIETNAMESE_LOCALE_TYPE = 1;

    @Override
    public void handleClientRequest(User user, ISFSObject isfso) {
        processClientMessage(user, isfso);
    }

    /**
     * Xử lý các message liên quan tới lobby chổ này
     */
    private void processClientMessage(User user, ISFSObject isfso) {
        try {
            //is lobby
            int action = isfso.getInt(SFSKey.ACTION_INCORE);
            if (action != SFSAction.PING) {
                trace(user.getName(), "send common client request action", isfso.getDump());
            }
            switch (action) {
                case SFSAction.REQUEST_INFOR_ALL_GAME:
                    //gửi thông tin danh sách game lobby về cho client
                    getParentExtension().send(SFSCommand.CLIENT_REQUEST, getBoardInfoMessage(Utils.getDevice(user)), user);
                    break;
                case SFSAction.UPDATE_PROFILE:
                    updateProfile(user, isfso);
                    break;
                case SFSAction.RANKING_GET_LEADER_BOARD_INFO:
                    getRankingLeaderBoardInfo(user, isfso);
                    break;
                case SFSAction.SET_MONEY_TYPE:
                    setUserMoneyType(user, isfso.getByte(SFSKey.MONEY_TYPE));
                    break;
                case SFSAction.GET_PROFILE:
                    processGetProfile(user);
                    break;
                case SFSAction.GET_POINT_INFO:
                    getPointInfo(user, isfso);
                    break;
                case SFSAction.RECEIVE_POINT:
                    receivePoint(user, isfso);
                    break;
                case SFSAction.GET_CHARGE_INFO:
                    getPaymentInfo(user, isfso);
                    break;
                case SFSAction.GET_NEWS:
                    getNews(user, isfso);
                    break;
                case SFSAction.GET_POPUP:
                    getPopup(user, isfso);
                    break;
                case SFSAction.REQUEST_HISTORY:
                    processRequestHistory(user, isfso);
                    break;
                case SFSAction.REQUEST_TRANSACTION_HISTORY:
                    processRequestTransHistory(user, isfso);
                    break;
                case SFSAction.VERIFY_GG_IAP:
                    verifyGoogleIAP(user, isfso);
                    break;
                case SFSAction.GET_USER_VIP_INFO:
                    processRequestUserVipInfo(user, isfso);
                    break;
                case SFSAction.GET_CASHOUT_Z_INFO:
                    processRequestUserZCashoutInfo(user, isfso);
                    break;
                case SFSAction.CASHOUT_Z_POINT:
                    processRequestUserZCashout(user, isfso);
                    break;
                case SFSAction.INFOR_BOARD_PLAYING:
                    sendBoardPlayingList(user);
                    break;
                case SFSAction.SET_LOCALE:
                    setLocaleForUser(user, isfso.getByte(SFSKey.TYPE));
                    break;
                case SFSAction.CREATE_PIN_CODE:
                    createPinCode(user, isfso);
                    break;
                case SFSAction.VERIFY_PIN:
                    verifyPin(user, isfso);
                    break;
                case SFSAction.UPDATE_PIN_CODE:
                    updatePinCode(user, isfso);
                    break;
                case SFSAction.VERIFY_PIN_ON_CHANGE:
                    verifyPinOnChange(user, isfso);
                    break;
                case SFSAction.PLAY_TAIXIU:
                    processTaiXiuRequest(user, isfso);
                    break;
                case SFSAction.GET_BTC_PAYMENT_INFO:
                    processBTCPaymentInfoRequest(user, isfso);
                    break;
                case SFSAction.UPDATE_PLAY_MODE:
                    updatePlayMode(user, isfso);
                    break;
                case SFSAction.GET_TRANSFER_INFO:
                    getTransferInfo(user, isfso);
                    break;
                case SFSAction.TRANSFER_MONEY:
                    transferMoney(user, isfso);
                    break;
                case SFSAction.GET_TRANSFER_QUOTA:
                    getTransferQuota(user, isfso);
                    break;
                case SFSAction.WITHDRAW:
                    withdraw(user, isfso);
                    break;
                case SFSAction.WITHDRAW_BANKING:
                    withdrawBanking(user, isfso);
                    break;
                case SFSAction.GET_BOARD_USER_COUNT:
                    getBoardUserCount(user, isfso);
                    break;
                case SFSAction.GET_WITHDRAW_BANKING_INFO:
                    getBankingWithdrawInfo(user, isfso);
                    break;
                case SFSAction.REQUEST_EVENT:
                    processEventRequest(user, isfso);
                    break;
                case SFSAction.RECHARGE_CARD:
                    processRechargeCard(user, isfso);
                    break;
                case SFSAction.GET_POINT_CONVERT_CONFIG:
                    getPointConvertConfig(user, isfso);
                    break;
                case SFSAction.CONVERT_POINT_2_MONEY:
                    convertPoint2Money(user, isfso);
                    break;
                case SFSAction.LINK_FACEBOOK:
                    linkFacebook(user, isfso);
                    break;
                case SFSAction.CHAT:
                    processChat(user, isfso);
                    break;
            }
        } catch (Exception e) {
            getLogger().error("CommonClientRequest.processClientMessage", e);
            sendErrorMessage(user);
        }
    }

    private void sendMessage(User user, String message) {
        ISFSObject isfso = MessageController.getStaticMessage(message);
        getParentExtension().send(SFSCommand.CLIENT_REQUEST, isfso, user);
    }

    private void sendErrorMessage(User user) {
        String message = GameLanguage.getMessage(GameLanguage.ERROR_TRY_AGAIN, Utils.getUserLocale(user));
        ISFSObject isfso = MessageController.getStaticMessage(message);
        getParentExtension().send(SFSCommand.CLIENT_REQUEST, isfso, user);
    }

    /**
     * Xét ngôn ngữ cho user
     *
     * @param user
     * @param type
     */
    private void setLocaleForUser(User user, int type) {
        UserState userState = HazelcastUtil.getUserState(Utils.getIdDBOfUser(user));
        if (userState == null) {
            return;
        }
        switch (type) {
            case ENGLISH_LOCALE_TYPE:
                user.setProperty(UserInforPropertiesKey.LOCALE_USER, GlobalsUtil.ENGLISH_LOCALE);
                userState.setLocale(GlobalsUtil.ENGLISH_LOCALE);
                break;
            case VIETNAMESE_LOCALE_TYPE:
                user.setProperty(UserInforPropertiesKey.LOCALE_USER, GlobalsUtil.VIETNAMESE_LOCALE);
                userState.setLocale(GlobalsUtil.VIETNAMESE_LOCALE);
                break;
            default:
                user.setProperty(UserInforPropertiesKey.LOCALE_USER, GlobalsUtil.CHINESE_LOCALE);
                userState.setLocale(GlobalsUtil.CHINESE_LOCALE);
                break;
        }
        HazelcastUtil.updateUserState(userState);
    }

    /**
     * Lịch sử giao dịch
     *
     * @param user
     * @param isfso
     */
    private void processRequestTransHistory(User user, ISFSObject isfso) {
        int page = isfso.getInt(SFSKey.INDEX_PAGE);
        String startDate = isfso.getUtfString(SFSKey.DATE_START);
        String endDate = isfso.getUtfString(SFSKey.DATE_END);

        String userId = user.getVariable(UserInforPropertiesKey.ID_DB_USER).getStringValue();
        byte moneyType = Utils.getMoneyTypeOfUser(user);
        TransactionHistory history = Database.instance.getUserTransaction(userId, page, moneyType, startDate, endDate, ServerConfig.getInstance().historyLimit());
        ISFSArray arr = new SFSArray();
        List<Transaction> listTx = history.getListTX();
        for (Transaction tx : listTx) {
            SFSObject txObj = new SFSObject();
            txObj.putUtfString(SFSKey.ID, tx.getId());
            txObj.putLong(SFSKey.TIME, tx.getTime());
            txObj.putDouble(SFSKey.MONEY, tx.getMoney());
            txObj.putDouble(SFSKey.VALUE, tx.getValue());
            txObj.putUtfString(SFSKey.TYPE, getTransactionType(tx.getType(), Utils.getUserLocale(user)));
            txObj.putUtfString(SFSKey.STATUS, getTransactionStatus(tx.getStatus(), Utils.getUserLocale(user)));
            arr.addSFSObject(txObj);
        }

        isfso.putSFSArray(SFSKey.DATA, arr);
        getParentExtension().send(SFSCommand.CLIENT_REQUEST, isfso, user);
    }

    private String getTransactionType(int type, Locale locale) {
        switch (type) {
            case Transaction.TYPE_INAPP:
                return GameLanguage.getMessage(GameLanguage.INAPP_PURCHASE, locale);
            case Transaction.TYPE_FREE:
                return GameLanguage.getMessage(GameLanguage.FREE, locale);
            case Transaction.TYPE_CARD:
                return GameLanguage.getMessage(GameLanguage.CARD, locale);
            case Transaction.TYPE_BTC:
                return GameLanguage.getMessage(GameLanguage.SATOSHI, locale);
            case Transaction.TYPE_TRANSFER:
                return GameLanguage.getMessage(GameLanguage.TRANSFER, locale);
            case Transaction.TYPE_DEPOSIT:
                return GameLanguage.getMessage(GameLanguage.DEPOSIT, locale);
            case Transaction.TYPE_WITHDRAW:
                return GameLanguage.getMessage(GameLanguage.WITHDRAW, locale);
            case Transaction.TYPE_REFUND:
                return GameLanguage.getMessage(GameLanguage.REFUND, locale);
            case Transaction.TYPE_CONVERT:
                return GameLanguage.getMessage(GameLanguage.CONVERT, locale);
        }
        return GameLanguage.getMessage(GameLanguage.UNKNOWN, locale);
    }

    private String getTransactionStatus(byte status, Locale locale) {
        switch (status) {
            case Transaction.STATUS_FAIL:
                return "<color=red>" + GameLanguage.getMessage(GameLanguage.FAIL, locale) + "</color>";
            case Transaction.STATUS_PENDING:
                return "<color=#8C8C8C>" + GameLanguage.getMessage(GameLanguage.WAITING, locale) + "</color>";
            case Transaction.STATUS_SUCCESS:
                return "<color=green>" + GameLanguage.getMessage(GameLanguage.SUCCESS, locale) + "</color>";
        }
        return GameLanguage.getMessage(GameLanguage.UNKNOWN, locale);
    }

    /**
     * Xử lý gửi về thông tin lịch sử ván chơi của user
     *
     * @param user
     * @param isfso
     */
    private void processRequestHistory(User user, ISFSObject isfso) {
        SFSObject obj = new SFSObject();
        obj.putInt(SFSKey.ACTION_INCORE, SFSAction.REQUEST_HISTORY);
        obj.putUtfString(SFSKey.DATA, getHistory(user, isfso));
        getParentExtension().send(SFSCommand.CLIENT_REQUEST, obj, user);
    }

    /**
     * Request API lấy lịch sử ván chơi
     *
     * @param user
     * @param isfso
     * @return
     */
    private String getHistory(User user, ISFSObject isfso) {
        String responseStr = "";
        try {
            String connection = ServerConfig.getInstance().getHistoryConnection();
            String key = ServerConfig.getInstance().getHistoryKey();
            int indexPage = isfso.getInt(SFSKey.INDEX_PAGE) + 1;
            String startDate = isfso.getUtfString(SFSKey.DATE_START);
            String endDate = isfso.getUtfString(SFSKey.DATE_END);

            String idDBUser = Utils.getIdDBOfUser(user);
            int moneyType = Utils.getMoneyTypeOfUser(user);

            String url = UrlConfig.getInstance().getUrlHistory();

            HistoryRequest hisRequest = new HistoryRequest();
            hisRequest.setPlayerId(idDBUser);
            hisRequest.setType("invoice");
            hisRequest.setBetUnit(String.valueOf(moneyType));
            hisRequest.setStartDate(startDate);
            hisRequest.setEndDate(endDate);
            hisRequest.setPage(String.valueOf(indexPage));
            hisRequest.setLimit(String.valueOf(ServerConfig.getInstance().historyLimit()));
            hisRequest.setConnection(connection);
            hisRequest.setChecksum(Utils.md5String(idDBUser + "invoice" + moneyType + startDate + endDate + indexPage + ServerConfig.getInstance().historyLimit() + connection + key));

            responseStr = APIUtils.request(url, GsonUtil.toJson(hisRequest, HistoryRequest.class));
            JsonObject json = GsonUtil.parse(responseStr).getAsJsonObject().getAsJsonObject("data");
            json.remove("message");
            responseStr = json.toString();

        } catch (Exception ex) {
            this.getLogger().error("CommonClientRequest.processClientMessage() error: ", ex);
        }
        return responseStr;
    }

    /**
     * get profile of user
     */
    private void processGetProfile(User user) {
        Profile pro = new Profile();
        pro.setMoney(Utils.getMoneyOfUser(user));
        pro.setPoint((long) Utils.getPointOfUser(user));
        pro.setTimeOnline(100);
        String userId = user.getVariable(UserInforPropertiesKey.ID_DB_USER).getStringValue();
        String pin = Database.instance.getPinCode(userId);

        SFSObject obj = new SFSObject();
        obj.putInt(SFSKey.ACTION_INCORE, SFSAction.GET_PROFILE);
        //thời gian online
        obj.putLong(SFSKey.TIME, pro.getTimeOnline());
        //tiền ảo
        obj.putLong(SFSKey.POINT, pro.getPoint());
        //tiền thật
        obj.putDouble(SFSKey.MONEY, pro.getMoney());
        obj.putBool(SFSKey.PIN, pin != null);
        Object data = user.getSession().getProperty(UserInforPropertiesKey.PROFILE);
        if (data instanceof VerifyResponseData) {
            VerifyResponseData profile = (VerifyResponseData) data;
            obj.putUtfString(SFSKey.PROFILE, GsonUtil.toJson(profile));
        } else {
            W88VerifyResponseData profile = (W88VerifyResponseData) data;
            obj.putUtfString(SFSKey.PROFILE, GsonUtil.toJson(profile));
        }

        getParentExtension().send(SFSCommand.CLIENT_REQUEST, obj, user);
    }

    /**
     * Gửi thông tin danh sách lobby game về cho client khi login
     *
     * @param device
     * @return
     */
    public SFSObject getBoardInfoMessage(Device device) {
        SFSObject ojBoardInfo = new SFSObject();
        try {
            ojBoardInfo.putInt(SFSKey.ACTION_INCORE, SFSAction.REQUEST_INFOR_ALL_GAME);
            //số lần tiền phạt khi rời bàn
            SFSArray arr = new SFSArray();
            SFSArray arrMoneyFacts = new SFSArray();

            List<GameConfigInfor> listRoomLobby = getGameInforListByMoneyType(MoneyContants.POINT);
            List<GameConfigInfor> listRoomLobbyReal = getGameInforListByMoneyType(MoneyContants.MONEY);
            //point rooms
            for (GameConfigInfor roomLobby : listRoomLobby) {
                String nameLobby = Utils.getLobbyName(roomLobby.getServiceId(), roomLobby.getMoneyType());
                if (TurnOffGameConfig.getInstance().isTurnOffGame(nameLobby, device.getPlatForm().getName(), device.getVersion().getVersionName(), device.getBundleId(), MoneyContants.POINT)) {
                    continue;
                }

                SFSObject obj = new SFSObject();
                obj.putUtfString("name", nameLobby);
                obj.putInt("pennalizeFactor", roomLobby.getPenalize());
                obj.putInt("minBuyStackOwner", roomLobby.getMinJoinOwner());
                obj.putInt("minBuyStack", roomLobby.getMinJoin());
                arr.addSFSObject(obj);
            }
            ojBoardInfo.putSFSArray("arrPoint", arr);

            //money rooms
            for (GameConfigInfor roomLobby : listRoomLobbyReal) {
                String nameLobby = Utils.getLobbyName(roomLobby.getServiceId(), roomLobby.getMoneyType());
                if (TurnOffGameConfig.getInstance().isTurnOffGame(nameLobby, device.getPlatForm().getName(), device.getVersion().getVersionName(), device.getBundleId(), MoneyContants.MONEY)) {
                    continue;
                }

                if (ServerConfig.getInstance().isCloseRealMoney()) {
                    continue;
                }

                SFSObject obj = new SFSObject();
                obj.putUtfString("name", nameLobby);
                obj.putInt("pennalizeFactor", roomLobby.getPenalize());
                obj.putInt("minBuyStackOwner", roomLobby.getMinJoinOwner());
                obj.putInt("minBuyStack", roomLobby.getMinJoin());
                arrMoneyFacts.addSFSObject(obj);
            }

            SFSArray arrMiniGame = new SFSArray();
            if (TaiXiuConfig.getInstance().isEnable()) {
                SFSObject obj = new SFSObject();
                obj.putUtfString("miniGameName", "tai_xiu");
                obj.putInt("index", TaiXiuConfig.getInstance().getIndex()); //vị trí hiển thị trong danh sách game
                arrMiniGame.addSFSObject(obj);
            }

            if (TaiXiuConfig.getInstance().isEnablePoint()) {
                SFSObject obj = new SFSObject();
                obj.putUtfString("miniGameName", "tai_xiu_p");
                obj.putInt("index", TaiXiuConfig.getInstance().getIndex()); //vị trí hiển thị trong danh sách game
                arrMiniGame.addSFSObject(obj);
            }

            ojBoardInfo.putSFSArray("miniGames", arrMiniGame);
            ojBoardInfo.putSFSArray("arrMoney", arrMoneyFacts);
            ojBoardInfo.putShort("pingTime", (short) ServerConfig.getInstance().getPingTime());
            ojBoardInfo.putInt("boardUserCountTime", ServerConfig.getInstance().getBoardUserCountTime());
        } catch (Exception e) {
            this.getLogger().error("getBoardInfoMessage erro: ", e);
        }
        return ojBoardInfo;
    }

    /**
     * Lấy ra danh sách game theo money type
     *
     * @param moneyType
     * @return
     */
    private List<GameConfigInfor> getGameInforListByMoneyType(int moneyType) {
        List<GameConfigInfor> games = new ArrayList<>();

        //point game
        String gamesconfig = RoomConfig.getInstance().getPointGames();
        if (moneyType == MoneyContants.MONEY) {
            gamesconfig = RoomConfig.getInstance().getMoneyGames();
        }
        if (!gamesconfig.isEmpty()) {
            JsonObject json = PARSER.parse(gamesconfig).getAsJsonObject();
            if (json != null) {
                for (Map.Entry<String, JsonElement> entry : json.entrySet()) {
                    if (!entry.getValue().getAsBoolean()) {
                        continue;
                    }
                    String nameLobby = entry.getKey();
                    int serviceId = Utils.getServiceId(nameLobby);
                    GameConfigInfor gameInfor = new GameConfigInfor();
                    gameInfor.setServiceId(serviceId);
                    gameInfor.setMoneyType((byte) moneyType);
                    gameInfor.setMinJoin(RoomConfig.getInstance().getMinJoinGame(nameLobby));
                    gameInfor.setPenalize(RoomConfig.getInstance().getPennalizeFactor(nameLobby));
                    gameInfor.setMinJoinOwner(RoomConfig.getInstance().getMinJoinOwner(nameLobby));
                    gameInfor.setPriority(RoomConfig.getInstance().getPriority(nameLobby));
                    games.add(gameInfor);
                }
            }
        }
        Collections.sort(games, GAME_CONFIG_INFOR_SORT_BY_MONEY_DESC);
        return games;
    }

    /**
     * Lấy ra danh sách tất cả lobby game
     *
     * @return
     */
    private List<Room> findRoomsLobby(int moneyType) {
        MatchExpression exp = new MatchExpression(RoomProperties.IS_GAME, BoolMatch.EQUALS, false)
                .and(RoomInforPropertiesKey.MONEY_TYPE, NumberMatch.EQUALS, moneyType);
        // Search Rooms
        List<Room> joinableRooms;
        if (moneyType == MoneyContants.MONEY) {
            joinableRooms = getApi().findRooms(getParentExtension().getParentZone().getRoomListFromGroup(ExtensionConstant.LOBBY_GROUP_NAME_REAL), exp, 0);
        } else {
            joinableRooms = getApi().findRooms(getParentExtension().getParentZone().getRoomListFromGroup(ExtensionConstant.LOBBY_GROUP_NAME), exp, 0);
        }
        return joinableRooms;
    }

    /**
     *
     * @param user
     * @param isfso
     */
    private void updateProfile(User user, ISFSObject isfso) {
        String userId = user.getVariable(UserInforPropertiesKey.ID_DB_USER).getStringValue();
        if (isfso.containsKey(SFSKey.DISPLAY_NAME)) {
            String displayName = isfso.getUtfString(SFSKey.DISPLAY_NAME);
            Database.instance.updateDisplayName(userId, displayName);
            List<UserVariable> vers = user.getVariables();
            vers.add(new SFSUserVariable(UserInforPropertiesKey.DISPLAY_NAME, displayName));
            getApi().setUserVariables(user, vers);
        }
        if (isfso.containsKey(SFSKey.AVATAR)) {
            String avatar = isfso.getUtfString(SFSKey.AVATAR);
            Database.instance.updateAvatar(userId, avatar);
            List<UserVariable> vers = user.getVariables();
            vers.add(new SFSUserVariable(UserInforPropertiesKey.AVATAR, avatar));
            getApi().setUserVariables(user, vers);
        }
    }

    /**
     *
     * @param user
     * @param isfso
     */
    private void getRankingLeaderBoardInfo(User user, ISFSObject isfso) {
        LeaderboardObject obj = new LeaderboardObject();
        int command = isfso.getInt(SFSKey.COMMAND);
        if (command == LeaderboardObject.SWITCH_EVENT_JOIN_STATUS) {
            obj.setStatus(isfso.getBool(SFSKey.STATUS));
        } else {
            obj.setServiceId(isfso.getInt(SFSKey.SERVICE_ID));
            obj.setPage(isfso.getInt(SFSKey.PAGE));
        }

        obj.setCommand(command);
        obj.setServerId(String.valueOf(ServerConfig.getInstance().getServerId()));
        obj.setUserid(user.getVariable(UserInforPropertiesKey.ID_DB_USER).getStringValue());
        QueueService.getInstance().sendRankingRequest(obj);
    }

    /**
     * Set type tiền cho user
     *
     * @param user
     * @param type
     */
    private void setUserMoneyType(User user, byte type) {
        String userId = Utils.getIdDBOfUser(user);
        if (type == MoneyContants.MONEY) {
            this.getApi().setUserVariables(user, Arrays.asList(new SFSUserVariable(UserInforPropertiesKey.MONEY_TYPE, MoneyContants.MONEY)));
        } else {
            this.getApi().setUserVariables(user, Arrays.asList(new SFSUserVariable(UserInforPropertiesKey.MONEY_TYPE, MoneyContants.POINT)));
        }

        UserState userState = HazelcastUtil.getUserState(userId);
        if (userState != null) {
            userState.setMoneyType(type);
            HazelcastUtil.updateUserState(userState);
        }

        TaiXiuQueueData queueData = new TaiXiuQueueData(TaiXiuCommand.QUIT, userId, ServerConfig.getInstance().getServerId());
        queueData.setMoneyType(type == MoneyContants.MONEY ? MoneyContants.POINT : MoneyContants.MONEY);
        QueueTaiXiu.getInstance().sendRequest(queueData);
    }

    private void getEventInfo(User user, ISFSObject isfso) {
        boolean checkEvent = Database.instance.checkEvent();
        int eventScreen = ServerConfig.getInstance().eventScreen();
        int eventScreenSub = ServerConfig.getInstance().eventScreenSub();
        if (checkEvent) {
            String notifyEvent = GameLanguage.getMessage(GameLanguage.EVENT_NOTIFY, Utils.getUserLocale(user));
            isfso.putUtfString(SFSKey.TITLE, notifyEvent);
        } else {
            isfso.putUtfString(SFSKey.TITLE, "");
        }
        isfso.putInt(SFSKey.SCREEN, eventScreen);
        isfso.putInt(SFSKey.SCREEN_SUB, eventScreenSub);

        getParentExtension().send(SFSCommand.CLIENT_REQUEST, isfso, user);
    }

    /**
     *
     * @param user
     */
    private void getPointInfo(User user, ISFSObject isfso) {
        String userId = user.getVariable(UserInforPropertiesKey.ID_DB_USER).getStringValue();
        int totalTimeFreeConfig = ServerConfig.getInstance().getTimeReceivePointFree();
        int totalTimeVideoConfig = ServerConfig.getInstance().getTimeReceivePointVideo();

        PointReceiveInfo lastPointReceiveInfoVideo = Database.instance.getTimeReceivePointInfo(userId, ExtensionConstant.POINT_TYPE_VIDEO);

        PointReceiveInfo lastPointReceiveInfoFree = Database.instance.getTimeReceivePointInfo(userId, ExtensionConstant.POINT_TYPE_FREE);

        int receiveLimitFree = ServerConfig.getInstance().getCountFree();

        int receiveLimitVideo = ServerConfig.getInstance().getCountVideo();

        int remainTimeFree;
        int remainTimeVideo;

        if (lastPointReceiveInfoVideo == null) {
            lastPointReceiveInfoVideo = new PointReceiveInfo(userId, ExtensionConstant.POINT_TYPE_VIDEO, null, 0);
        }

        if (lastPointReceiveInfoFree == null) {
            lastPointReceiveInfoFree = new PointReceiveInfo(userId, ExtensionConstant.POINT_TYPE_FREE, null, 0);
        }

        if (lastPointReceiveInfoVideo.getReceiveCount() == 0) {
            remainTimeVideo = 0;
        } else {
            if (lastPointReceiveInfoVideo.getReceiveCount() >= receiveLimitVideo) {
                remainTimeVideo = (int) (Utils.getTimeCurrentToZeroHour() / 1000);
            } else {
                int time = (int) (System.currentTimeMillis() - lastPointReceiveInfoVideo.getTime_receive().getTime()) / 1000;
                if (time > totalTimeVideoConfig) {
                    remainTimeVideo = 0;
                } else {
                    remainTimeVideo = Math.min(totalTimeVideoConfig - time, (int) Utils.getTimeCurrentToZeroHour() / 1000);
                }
            }
        }

        if (lastPointReceiveInfoFree.getReceiveCount() == 0) {
            remainTimeFree = 0;
        } else {
            if (lastPointReceiveInfoFree.getReceiveCount() >= receiveLimitFree) {
                remainTimeFree = (int) (Utils.getTimeCurrentToZeroHour() / 1000);
            } else {
                int time = (int) (System.currentTimeMillis() - lastPointReceiveInfoFree.getTime_receive().getTime()) / 1000;
                if (time > totalTimeFreeConfig) {
                    remainTimeFree = 0;
                } else {
                    remainTimeFree = Math.min(totalTimeFreeConfig - time, (int) Utils.getTimeCurrentToZeroHour() / 1000);
                }
            }
        }

        isfso.putInt(SFSKey.TIME_FREE, remainTimeFree);
        isfso.putInt(SFSKey.TIME_FREE_TOTAL, totalTimeFreeConfig);
        isfso.putInt(SFSKey.TIME_VIDEO, remainTimeVideo);
        isfso.putInt(SFSKey.POINT_FREE, ServerConfig.getInstance().getPointFree());
        isfso.putInt(SFSKey.POINT_VIDEO, ServerConfig.getInstance().getPointVideo());

        getParentExtension().send(SFSCommand.CLIENT_REQUEST, isfso, user);

    }

    /**
     *
     * @param user
     * @param isfso
     * @throws SFSVariableException
     */
    private void receivePoint(User user, ISFSObject isfso) throws NoSuchAlgorithmException {
        byte type = isfso.getByte(SFSKey.TYPE);
        if (type == ExtensionConstant.POINT_TYPE_FREE) {
            if (!ServerConfig.getInstance().isPointFreeEnable()) {
                String msg = GameLanguage.getMessage(GameLanguage.FUNCTION_MAINTAIN, Utils.getUserLocale(user));
                sendMessage(user, msg);
                return;
            }
        } else {
            if (!ServerConfig.getInstance().isPointVideoEnable()) {
                String msg = GameLanguage.getMessage(GameLanguage.FUNCTION_MAINTAIN, Utils.getUserLocale(user));
                sendMessage(user, msg);
                return;
            }
        }
        String userId = user.getVariable(UserInforPropertiesKey.ID_DB_USER).getStringValue();
        PointReceiveInfo lastPointReceiveInfo = Database.instance.getTimeReceivePointInfo(userId, type);
        if (lastPointReceiveInfo == null) {  // chua nhan lan nao trong ngay
            lastPointReceiveInfo = new PointReceiveInfo(userId, type, new Date(0), 0);
        }

        int receiveLimit = ServerConfig.getInstance().getCountFree();
        if (type == ExtensionConstant.POINT_TYPE_VIDEO) {
            receiveLimit = ServerConfig.getInstance().getCountVideo();
        }

        if (lastPointReceiveInfo.getReceiveCount() >= receiveLimit) {
            return;
        }

        int totalTime = ServerConfig.getInstance().getTimeReceivePointFree();
        if (type == ExtensionConstant.POINT_TYPE_VIDEO) {
            totalTime = ServerConfig.getInstance().getTimeReceivePointVideo();
        }

        long time = (System.currentTimeMillis() - lastPointReceiveInfo.getTime_receive().getTime()) / 1000;
        if (time < totalTime) {
            return;
        }

        int point = ServerConfig.getInstance().getPointFree();
        if (type == ExtensionConstant.POINT_TYPE_VIDEO) {
            point = ServerConfig.getInstance().getPointVideo();
        }

        lastPointReceiveInfo.setReceiveCount(lastPointReceiveInfo.getReceiveCount() + 1);
        UpdateMoneyResult rs = Database.instance.callUpdateMoneyProcedure(userId, new BigDecimal(point));
        if (rs.after.compareTo(rs.before) == 0) {
            trace(ExtensionLogLevel.ERROR, "call update free point error:", user.getName(), type);
            return;
        }
        Utils.updateMoneyOfUser(user, rs.after.doubleValue());
        Database.instance.updateInfoReceivePoint(userId, type, lastPointReceiveInfo.getReceiveCount());

        if (lastPointReceiveInfo.getReceiveCount() == receiveLimit) {
            totalTime = (int) Utils.getTimeCurrentToZeroHour() / 1000;
        } else {
            totalTime = Math.min(totalTime, (int) Utils.getTimeCurrentToZeroHour() / 1000);
        }
        isfso.putInt(SFSKey.TIME_WAIT, totalTime);
        getParentExtension().send(SFSCommand.CLIENT_REQUEST, isfso, user);

//        Database.instance.logTransaction(null, userId, MoneyContants.POINT, point, rs.after.doubleValue(), Transaction.TYPE_FREE, Transaction.STATUS_SUCCESS);
//
//        UserBalanceUpdate ubu = new UserBalanceUpdate();
//        ubu.setPlayerId(userId);
//        ubu.setEmail(String.valueOf(user.getSession().getProperty(UserInforPropertiesKey.EMAIL)));
//        ubu.setCurrency(GameLanguage.getMessage(GameLanguage.NAME_POINT, Locale.ENGLISH));
//        ubu.setCreatedAt(System.currentTimeMillis() / 1000);
//        ubu.setSessionId(String.valueOf(user.getProperty(UserInforPropertiesKey.SESSION_ID)));
//        ubu.setChannel(String.valueOf(user.getSession().getProperty(UserInforPropertiesKey.CHANNEL)));
//        if (type == ExtensionConstant.POINT_TYPE_VIDEO) {
//            ubu.setDescription("Receive video points");
//        } else {
//            ubu.setDescription("Receive free points");
//        }
//        ubu.setLastBalance(rs.before);
//        ubu.setBalance(rs.after);
//        ubu.setChange(point);
//        ubu.setConnectionId(ServerConfig.getInstance().getConnectionId());
//        ubu.setRequestId(Utils.md5String(userId + System.currentTimeMillis()));
//        ubu.setUnit("point");
//        ubu.setPaymentFlow(UserBalanceUpdate.PAYMENT_FLOW_ADMIN);
//        QueueServiceApi.getInstance().sendData(QueueConfig.getInstance().getKeyBalance(), true, ubu);
    }

    private static String ggAccessToken = null;
    
    private void verifyGoogleIAP(User user, ISFSObject isfso) {
        String token = isfso.getUtfString(SFSKey.TOKEN);
        String productId = isfso.getUtfString(SFSKey.PRODUCT_ID);
        
        if (ggAccessToken == null) {
            ggAccessToken = GoogleConfig.getInstance().getAccessToken();
            if (ggAccessToken.isEmpty()) {
                String response = APIUtils.getGGAccessToken();
                JsonObject json = GsonUtil.parse(response).getAsJsonObject();
                ggAccessToken = json.get("access_token").getAsString();
                GoogleConfig.getInstance().updateProperties("accesstoken", ggAccessToken);
                GoogleConfig.getInstance().save();
            }
        }
        
        String response = APIUtils.getGGProductStatus(productId, token, ggAccessToken);
        JsonObject json = GsonUtil.parse(response).getAsJsonObject();
        if (json.has("error")) {
            response = APIUtils.refreshGGAccessToken(ggAccessToken);
            json = GsonUtil.parse(response).getAsJsonObject();
            ggAccessToken = json.get("access_token").getAsString();
            GoogleConfig.getInstance().updateProperties("accesstoken", ggAccessToken);
            GoogleConfig.getInstance().save();
            
            response = APIUtils.getGGProductStatus(productId, token, ggAccessToken);
        }
        
        GGProductPurchase gpp = GsonUtil.fromJson(response, GGProductPurchase.class);
        if (!gpp.isPurchased() || gpp.isAcknowledged()) {
            trace("google purchase fail:", response);
            return;
        }

        APIUtils.acknowledgeGGProductPurchase(productId, token, ggAccessToken);

        String products = GoogleConfig.getInstance().getProducts();
        JsonArray arr = GsonUtil.parse(products).getAsJsonArray();
        for (JsonElement e : arr) {
            json = e.getAsJsonObject();
            if (json.get("productId").getAsString().equals(productId)) {
                double value = json.get("value").getAsDouble();
                String userId = user.getVariable(UserInforPropertiesKey.ID_DB_USER).getStringValue();
                UpdateMoneyResult umr = Database.instance.callUpdateMoneyProcedure(userId, new BigDecimal(value));
                if (umr != null && umr.after.compareTo(umr.before) != 0) {
                    Utils.updateMoneyOfUser(user, umr.after.doubleValue());
                    isfso.putInt(SFSKey.RESULT, 1);
                    isfso.putDouble(SFSKey.VALUE, value);
                    isfso.putDouble(SFSKey.MONEY, umr.after.doubleValue());
                } else {
                    isfso.putInt(SFSKey.RESULT, 0);
                }
                break;
            }
        }
    }

    private void processRequestUserVipInfo(User user, ISFSObject isfso) {
        VipQueueObj obj = new VipQueueObj();
        obj.setCommand(VipQueueObj.GET_USER_VIP_DATA);
        obj.setServerId(String.valueOf(ServerConfig.getInstance().getServerId()));
        obj.setUserid(user.getVariable(UserInforPropertiesKey.ID_DB_USER).getStringValue());
        obj.setLang(Utils.getUserLocale(user).getLanguage());
        QueueService.getInstance().sendVipRequest(obj);
    }

    private void processRequestUserZCashoutInfo(User user, ISFSObject isfso) {
        VipQueueObj obj = new VipQueueObj();
        obj.setCommand(VipQueueObj.GET_Z_CASHOUT_INFO);
        obj.setServerId(String.valueOf(ServerConfig.getInstance().getServerId()));
        obj.setUserid(user.getVariable(UserInforPropertiesKey.ID_DB_USER).getStringValue());
        obj.setLang(Utils.getUserLocale(user).getLanguage());
        QueueService.getInstance().sendVipRequest(obj);
    }

    private void processRequestUserZCashout(User user, ISFSObject isfso) {
        long numberZCashout = isfso.getLong("zCashout");
        VipQueueObj obj = new VipQueueObj();
        obj.setCommand(VipQueueObj.DO_Z_CASHOUT);
        obj.setServerId(String.valueOf(ServerConfig.getInstance().getServerId()));
        obj.setUserid(user.getVariable(UserInforPropertiesKey.ID_DB_USER).getStringValue());
        obj.setData(String.valueOf(numberZCashout));
        obj.setLang(Utils.getUserLocale(user).getLanguage());
        QueueService.getInstance().sendVipRequest(obj);
    }

    /**
     * lấy thông tin nạp
     *
     * @param user
     * @param isfso
     */
    private void getPaymentInfo(User user, ISFSObject isfso) throws Exception {
        isfso.putUtfString(SFSKey.DATA, GoogleConfig.getInstance().getProducts());
        send(SFSCommand.CLIENT_REQUEST, isfso, user);
    }

    /**
     * Gửi về danh sách bàn đang playing
     *
     * @param user
     * @param boards
     */
    private void sendBoardPlayingList(User user) {
        try {
            PlayingBoardManager playingBoard = HazelcastUtil.getPlayingBoard(Utils.getIdDBOfUser(user));
            JsonArray array = new JsonArray();
            String nameLobby = "";
            if (playingBoard != null) {
                if (playingBoard.getBoardPlaying() != null) {
                    String s = GsonUtil.toJson(playingBoard.getBoardPlaying(), Board.class);
                    array.add(PARSER.parse(s));
                    nameLobby = playingBoard.getNameLobby();
                }
            }

            SFSObject sfsObj = new SFSObject();
            sfsObj.putInt(SFSKey.ACTION_INCORE, SFSAction.INFOR_BOARD_PLAYING);
            sfsObj.putUtfString(SFSKey.NAME, nameLobby);
            sfsObj.putUtfString(SFSKey.BOARDS, array.toString());
            getParentExtension().send(SFSCommand.CLIENT_REQUEST, sfsObj, user);
        } catch (Exception e) {
            this.getLogger().error("sendBoardPlayingList error: ", e);
        }
    }

    /**
     * lấy tin tức
     *
     * @param user
     * @param isfso
     */
    private void getNews(User user, ISFSObject isfso) {
        SFSArray arr = new SFSArray();
        List<News> listNews = Database.instance.getListNews(Utils.getUserLocale(user).getLanguage());
        if (listNews != null && !listNews.isEmpty()) {
            for (News news : listNews) {
                SFSObject obj = new SFSObject();
                obj.putUtfString(SFSKey.TITLE, news.getTitle());
                obj.putUtfString(SFSKey.CONTENT, news.getContent());
                if (news.getButton1() != null) {
                    SFSObject btn = new SFSObject();
                    btn.putUtfString(SFSKey.CAPTION, news.getButton1().getCaption());
                    btn.putUtfString(SFSKey.DATA, news.getButton1().getData());
                    btn.putByte(SFSKey.TYPE, news.getButton1().getType());
                    obj.putSFSObject(SFSKey.BUTTON1, btn);
                }
                if (news.getButton2() != null) {
                    SFSObject btn = new SFSObject();
                    btn.putUtfString(SFSKey.CAPTION, news.getButton2().getCaption());
                    btn.putUtfString(SFSKey.DATA, news.getButton2().getData());
                    btn.putByte(SFSKey.TYPE, news.getButton2().getType());
                    obj.putSFSObject(SFSKey.BUTTON2, btn);
                }
                obj.putUtfString(SFSKey.ICON, news.getIcon());
                obj.putUtfString(SFSKey.IMAGE, news.getImage());
                obj.putUtfString(SFSKey.IMAGE_LARGE, news.getImageLarge());
                obj.putByte(SFSKey.CATEGORY, news.getCategory());
                obj.putLong(SFSKey.TIME, (System.currentTimeMillis() - news.getStartTime()) / 1000);
                arr.addSFSObject(obj);
            }
        }

        isfso.putSFSArray(SFSKey.NEWS, arr);
        getParentExtension().send(SFSCommand.CLIENT_REQUEST, isfso, user);
    }

    /**
     * lấy popup
     *
     * @param user
     * @param isfso
     */
    private void getPopup(User user, ISFSObject isfso) {
        SFSArray arr = new SFSArray();
        List<News> popups = Database.instance.getListPopup(Utils.getUserLocale(user).getLanguage(), false);
        if (popups != null && !popups.isEmpty()) {
            for (News news : popups) {
                SFSObject obj = new SFSObject();
                obj.putUtfString(SFSKey.TITLE, news.getTitle());
                obj.putUtfString(SFSKey.CONTENT, news.getContent());
                if (news.getButton1() != null) {
                    SFSObject btn = new SFSObject();
                    btn.putUtfString(SFSKey.CAPTION, news.getButton1().getCaption());
                    btn.putUtfString(SFSKey.DATA, news.getButton1().getData());
                    btn.putByte(SFSKey.TYPE, news.getButton1().getType());
                    obj.putSFSObject(SFSKey.BUTTON1, btn);
                }
                if (news.getButton2() != null) {
                    SFSObject btn = new SFSObject();
                    btn.putUtfString(SFSKey.CAPTION, news.getButton2().getCaption());
                    btn.putUtfString(SFSKey.DATA, news.getButton2().getData());
                    btn.putByte(SFSKey.TYPE, news.getButton2().getType());
                    obj.putSFSObject(SFSKey.BUTTON2, btn);
                }

                obj.putUtfString(SFSKey.ICON, news.getIcon());
                obj.putUtfString(SFSKey.IMAGE, news.getImage());
                obj.putUtfString(SFSKey.IMAGE_LARGE, news.getImageLarge());
                obj.putByte(SFSKey.CATEGORY, news.getCategory());
                obj.putLong(SFSKey.TIME, (System.currentTimeMillis() - news.getStartTime()) / 1000);
                arr.addSFSObject(obj);
            }
        }

        isfso.putSFSArray(SFSKey.POPUP, arr);
        getParentExtension().send(SFSCommand.CLIENT_REQUEST, isfso, user);

        String userId = user.getVariable(UserInforPropertiesKey.ID_DB_USER).getStringValue();
        List<UserReceiveMoneyOffline> listUserReceiveMoneyOffline = Database.instance.getMoneyReceiveOffline(userId);
        if (!listUserReceiveMoneyOffline.isEmpty()) {
            String nameMoney = GameLanguage.getMessage(GameLanguage.NAME_MONEY, Utils.getUserLocale(user));
            for (UserReceiveMoneyOffline userReceive : listUserReceiveMoneyOffline) {
                String msg = String.format(GameLanguage.getMessage(GameLanguage.TRANSFER_MONEY_SUCCESS_2, Utils.getUserLocale(user)), userReceive.getFromUser(), Utils.formatNumber(userReceive.getMoney()), nameMoney);
                sendMessage(user, msg);
                Database.instance.deleteMoneyReceiveOffline(userId);
            }
        }
    }

    /**
     * tạo mã pin
     *
     * @param user
     * @param isfso
     */
    private void createPinCode(User user, ISFSObject isfso) {
        String pinCode = isfso.getUtfString(SFSKey.PIN);
        boolean isValid = false;
        try {
            isValid = pinCode.length() == 6 && Integer.parseInt(pinCode) >= 0;
        } catch (NumberFormatException e) {
        }

        int code = 1;
        String msg = GameLanguage.getMessage(GameLanguage.UPDATE_SUCCESS, Utils.getUserLocale(user));
        if (isValid) {
            String userId = user.getVariable(UserInforPropertiesKey.ID_DB_USER).getStringValue();
            if (!Database.instance.updatePinCode(userId, pinCode)) {
                code = 0;
                msg = GameLanguage.getMessage(GameLanguage.ERROR_TRY_AGAIN, Utils.getUserLocale(user));
            }
        } else {
            code = 0;
            msg = GameLanguage.getMessage(GameLanguage.INVALID_PIN_CODE, Utils.getUserLocale(user));
        }

        isfso.putInt(SFSKey.CODE, code);
        isfso.putUtfString(SFSKey.MESSAGE, msg);
        getParentExtension().send(SFSCommand.CLIENT_REQUEST, isfso, user);
    }

    /**
     *
     * @param user
     * @param isfso
     */
    private void updatePinCode(User user, ISFSObject isfso) {
        String pinCode = isfso.getUtfString(SFSKey.PIN);
        String userId = user.getVariable(UserInforPropertiesKey.ID_DB_USER).getStringValue();
        if (!pinCode.equals(Database.instance.getPinCode(userId))) {
            isfso.putInt(SFSKey.CODE, 0);
            isfso.putUtfString(SFSKey.MESSAGE, GameLanguage.getMessage(GameLanguage.WRONG_PIN_CODE, Utils.getUserLocale(user)));
            getParentExtension().send(SFSCommand.CLIENT_REQUEST, isfso, user);
            return;
        }

        String newPin = isfso.getUtfString(SFSKey.NEW_PIN);
        if (!Database.instance.updatePinCode(userId, newPin)) {
            isfso.putInt(SFSKey.CODE, 0);
            isfso.putUtfString(SFSKey.MESSAGE, GameLanguage.getMessage(GameLanguage.ERROR_TRY_AGAIN, Utils.getUserLocale(user)));
        } else {
            isfso.putInt(SFSKey.CODE, 1);
            isfso.putUtfString(SFSKey.MESSAGE, GameLanguage.getMessage(GameLanguage.UPDATE_SUCCESS, Utils.getUserLocale(user)));
        }

        getParentExtension().send(SFSCommand.CLIENT_REQUEST, isfso, user);
    }

    /**
     * verify pin & lấy token để rút tiền
     *
     * @param user
     * @param isfso
     */
    private void verifyPin(User user, ISFSObject isfso) {
        String pinCode = isfso.getUtfString(SFSKey.PIN);
        String userId = user.getVariable(UserInforPropertiesKey.ID_DB_USER).getStringValue();

        // check pin error counter
        int[] error = Database.instance.getPinError(userId);
        if (error != null && error[0] >= 3 && error[1] > 0) {
            int minutes = error[1] / 60;
            if (minutes == 0) {
                minutes = 1;
            }
            String msg = String.format(GameLanguage.getMessage(GameLanguage.LOCKED_PIN_CODE, Utils.getUserLocale(user)), minutes);
            isfso.putInt(SFSKey.CODE, 0);
            isfso.putUtfString(SFSKey.MESSAGE, msg);
            getParentExtension().send(SFSCommand.CLIENT_REQUEST, isfso, user);
            return;
        }

        if (pinCode.equals(Database.instance.getPinCode(userId))) {
            if (error != null && error[0] > 0) {
                Database.instance.resetPinError(userId);
            }
        } else {
            String msg = GameLanguage.getMessage(GameLanguage.WRONG_PIN_CODE, Utils.getUserLocale(user));
            if (error == null) {
                Database.instance.createPinError(userId);
            } else {
                if (error[0] < 2) {
                    Database.instance.updatePinError(userId);
                } else {
                    int minutes = getTimeLockPin(error[0] + 1);
                    Database.instance.updatePinError(userId, minutes);
                    msg = String.format(GameLanguage.getMessage(GameLanguage.LOCKED_PIN_CODE_2, Utils.getUserLocale(user)), error[0] + 1, minutes);
                }
            }

            isfso.putInt(SFSKey.CODE, 0);
            isfso.putUtfString(SFSKey.MESSAGE, msg);
            getParentExtension().send(SFSCommand.CLIENT_REQUEST, isfso, user);
            return;
        }

        String url = UrlConfig.getInstance().getUrlBackend() + "/get-cashout-token";
        Map map = new HashMap();
        map.put("user_id", userId);
        map.put("pin", pinCode);

        try {
            Document doc = Jsoup.connect(url).timeout(6000).ignoreContentType(true).data(map).get();
            String response = doc.body().text();
            APIResult result = GsonUtil.fromJson(response, APIResult.class);
            isfso.putInt(SFSKey.CODE, result.getCode());
            if (result.getCode() == APIResult.CODE_SUCESS) {
                isfso.putUtfString(SFSKey.TOKEN, (String) result.getData());
            } else {
                isfso.putUtfString(SFSKey.MESSAGE, result.getMessage());
            }
        } catch (Exception e) {
            getLogger().error("verifyPin", e);
            isfso.putInt(SFSKey.CODE, 0);
            isfso.putUtfString(SFSKey.MESSAGE, GameLanguage.getMessage(GameLanguage.ERROR_TRY_AGAIN, Utils.getUserLocale(user)));
        }

        getParentExtension().send(SFSCommand.CLIENT_REQUEST, isfso, user);
    }

    private void verifyPinOnChange(User user, ISFSObject isfso) {
        String pinCode = isfso.getUtfString(SFSKey.PIN);
        String userId = user.getVariable(UserInforPropertiesKey.ID_DB_USER).getStringValue();

        // check pin error counter
        int[] error = Database.instance.getPinError(userId);
        if (error != null && error[0] >= 3 && error[1] > 0) {
            int minutes = error[1] / 60;
            if (minutes == 0) {
                minutes = 1;
            }
            String msg = String.format(GameLanguage.getMessage(GameLanguage.LOCKED_PIN_CODE, Utils.getUserLocale(user)), minutes);
            isfso.putInt(SFSKey.CODE, 0);
            isfso.putUtfString(SFSKey.MESSAGE, msg);
            getParentExtension().send(SFSCommand.CLIENT_REQUEST, isfso, user);
            return;
        }

        if (pinCode.equals(Database.instance.getPinCode(userId))) {
            isfso.putInt(SFSKey.CODE, 1);
            if (error != null && error[0] > 0) {
                Database.instance.resetPinError(userId);
            }
        } else {
            String msg = GameLanguage.getMessage(GameLanguage.WRONG_PIN_CODE, Utils.getUserLocale(user));
            if (error == null) {
                Database.instance.createPinError(userId);
            } else {
                if (error[0] < 2) {
                    Database.instance.updatePinError(userId);
                } else {
                    int minutes = getTimeLockPin(error[0] + 1);
                    Database.instance.updatePinError(userId, minutes);
                    msg = String.format(GameLanguage.getMessage(GameLanguage.LOCKED_PIN_CODE_2, Utils.getUserLocale(user)), error[0] + 1, minutes);
                }
            }

            isfso.putInt(SFSKey.CODE, 0);
            isfso.putUtfString(SFSKey.MESSAGE, msg);
        }
        getParentExtension().send(SFSCommand.CLIENT_REQUEST, isfso, user);
    }

    private int getTimeLockPin(int count) {
        return (int) (5 * Math.pow(2, count - 3));
    }

    private void processTaiXiuRequest(User user, ISFSObject isfso) {
        byte cmd = isfso.getByte(SFSKey.COMMAND);
        String userId = user.getVariable(UserInforPropertiesKey.ID_DB_USER).getStringValue();
        String displayName = user.getVariable(UserInforPropertiesKey.DISPLAY_NAME).getStringValue();
        int serverId = ServerConfig.getInstance().getServerId();
        String lang = Utils.getUserLocale(user).getLanguage();
        byte moneyType = Utils.getMoneyTypeOfUser(user);
        TaiXiuQueueData data = new TaiXiuQueueData(cmd, userId, serverId);
        switch (cmd) {
            case TaiXiuCommand.BET:
                BigDecimal betMoney = new BigDecimal(String.valueOf(isfso.getDouble(SFSKey.BET_MONEY)));
                byte choice = isfso.getByte(SFSKey.CHOICE);
                TaiXiuBetResult result = new TaiXiuBetResult(userId, choice, betMoney);
                result.setCode(TaiXiuBetResult.CODE_SUCCESS);
                double userMoney = moneyType == MoneyContants.MONEY ? Utils.getMoneyOfUser(user) : Utils.getPointOfUser(user);

                if (!TaiXiuConfig.getInstance().isEnable()) {
                    result.setCode(TaiXiuBetResult.CODE_FAIL_GAME_MAINTAINING);
                    result.setMessage(GameLanguage.getMessage(GameLanguage.FUNCTION_MAINTAIN, Utils.getUserLocale(user)));
                } else if (betMoney.doubleValue() > userMoney) {
                    result.setCode(TaiXiuBetResult.CODE_FAIL_NOT_ENOUGH_MONEY);
                    result.setMessage(GameLanguage.getMessage(GameLanguage.NOT_ENOUGH_MONEY_BET, Utils.getUserLocale(user)));
                }

                if (!result.isSuccess()) {
                    isfso.putUtfString(SFSKey.DATA, GsonUtil.toJson(result));
                    getParentExtension().send(SFSCommand.CLIENT_REQUEST, isfso, user);
                    return;
                }

                TaiXiuBetRequest rq = new TaiXiuBetRequest(betMoney, choice, lang);
                data.setData(GsonUtil.toJson(rq));
                data.setUsername(displayName);
                data.setUserType(Utils.getUserType(user));
                break;

            case TaiXiuCommand.BUY_NOHU_TICKET:
                TaiXiuBuyTicketResult buyResult = new TaiXiuBuyTicketResult(userId);
                if (!TaiXiuConfig.getInstance().isEnableNohu()) {
                    buyResult.setCode(TaiXiuBetResult.CODE_FAIL_GAME_MAINTAINING);
                    buyResult.setMessage(GameLanguage.getMessage(GameLanguage.FUNCTION_MAINTAIN, Utils.getUserLocale(user)));
                } else if (Utils.getMoneyOfUser(user) < TaiXiuConfig.getInstance().getNohuTicketPrice()) {
                    buyResult.setCode(TaiXiuBuyTicketResult.CODE_FAIL_NOT_ENOUGH_MONEY);
                    buyResult.setMessage(GameLanguage.getMessage(GameLanguage.NOT_ENOUGH_MONEY_BET, Utils.getUserLocale(user)));
                }

                if (!buyResult.isSuccess()) {
                    isfso.putUtfString(SFSKey.DATA, GsonUtil.toJson(buyResult));
                    getParentExtension().send(SFSCommand.CLIENT_REQUEST, isfso, user);
                    return;
                }

                byte[] dice = new byte[3];
                boolean isAuto = isfso.getBool(SFSKey.AUTO);
                if (!isAuto) {
                    ArrayList<Short> list = (ArrayList<Short>) isfso.getShortArray(SFSKey.DICE);
                    for (int i = 0; i < 3; i++) {
                        dice[i] = list.get(i).byteValue();
                    }
                }

                TaiXiuBuyTicketRequest buyRq = new TaiXiuBuyTicketRequest(dice, isAuto, lang);
                data.setData(GsonUtil.toJson(buyRq));
                data.setUsername(displayName);
                break;

            case TaiXiuCommand.GET_MATCH_DETAIL:
            case TaiXiuCommand.GET_LIST_NOHU_TICKET:
                data.setData(String.valueOf(isfso.getInt(SFSKey.MATCH_ID)));
                break;
        }

        data.setMoneyType(Utils.getMoneyTypeOfUser(user));
        QueueTaiXiu.getInstance().sendRequest(data);
    }

    private void processBTCPaymentInfoRequest(User user, ISFSObject isfso) {
    }

    private void updatePlayMode(User user, ISFSObject isfso) {
        Integer serviceId = isfso.getInt(SFSKey.SERVICE_ID);
        List<Integer> shuffleGames = ServerConfig.getInstance().getListShuffleGame();
        if (!shuffleGames.contains(serviceId)) {
            return;
        }

        ISFSArray ids = user.getVariable(UserInforPropertiesKey.SHUFFLE_GAMES).getSFSArrayValue();
        if (ids.contains(serviceId)) {
            for (int i = 0; i < ids.size(); i++) {
                if (ids.getInt(i) == serviceId.intValue()) {
                    ids.removeElementAt(i);
                    break;
                }
            }
        } else {
            ids.addInt(serviceId);
        }

        UserVariable var = new SFSUserVariable(UserInforPropertiesKey.SHUFFLE_GAMES, ids);
        getApi().setUserVariables(user, Arrays.asList(var));

        UserState userState = HazelcastUtil.getUserState(Utils.getIdDBOfUser(user));
        shuffleGames = new ArrayList<>();
        for (int i = 0; i < ids.size(); i++) {
            shuffleGames.add(ids.getInt(i));
        }
        userState.setShuffleGames(shuffleGames);
        HazelcastUtil.updateUserState(userState);
    }

    private void getTransferInfo(User user, ISFSObject isfso) {
        P2PTransferConfig transferConfig = Database.instance.getTransferConfig();
        if (!transferConfig.isEnable()) {
            String msg = GameLanguage.getMessage(GameLanguage.FUNCTION_MAINTAIN, Utils.getUserLocale(user));
            isfso.putInt(SFSKey.CODE, 6);
            isfso.putUtfString(SFSKey.MESSAGE, msg);
            getParentExtension().send(SFSCommand.CLIENT_REQUEST, isfso, user);
            return;
        }

        double transferMoney = isfso.getDouble(SFSKey.MONEY);
        String data = isfso.getUtfString(SFSKey.USER_ID);
        String userId = user.getVariable(UserInforPropertiesKey.ID_DB_USER).getStringValue();
        
        if (!Database.instance.checkVerify(userId)) {
            String msg = GameLanguage.getMessage(GameLanguage.TRANSFER_MONEY_FAIL_4, Utils.getUserLocale(user));
            isfso.putInt(SFSKey.CODE, 7);
            isfso.putUtfString(SFSKey.MESSAGE, msg);
            getParentExtension().send(SFSCommand.CLIENT_REQUEST, isfso, user);
            return;
        }
        
        String toUserId = Database.instance.getUserIdByTransferData(data);
        if (toUserId == null) {
            String msg = GameLanguage.getMessage(GameLanguage.TRANSFER_MONEY_FAIL_3, Utils.getUserLocale(user));
            isfso.putInt(SFSKey.CODE, 3);
            isfso.putUtfString(SFSKey.MESSAGE, msg);
            getParentExtension().send(SFSCommand.CLIENT_REQUEST, isfso, user);
            return;
        }
        
        if (!Database.instance.checkVerify(toUserId)) {
            String msg = GameLanguage.getMessage(GameLanguage.TRANSFER_MONEY_FAIL_6, Utils.getUserLocale(user));
            isfso.putInt(SFSKey.CODE, 7);
            isfso.putUtfString(SFSKey.MESSAGE, msg);
            getParentExtension().send(SFSCommand.CLIENT_REQUEST, isfso, user);
            return;
        }

        if (userId.equals(toUserId)) {
            String msg = GameLanguage.getMessage(GameLanguage.TRANSFER_MONEY_FAIL_2, Utils.getUserLocale(user));
            isfso.putInt(SFSKey.CODE, 2);
            isfso.putUtfString(SFSKey.MESSAGE, msg);
            getParentExtension().send(SFSCommand.CLIENT_REQUEST, isfso, user);
            return;
        }
        
        int userType = Utils.getUserType(user);
        double fromUserMoney = Utils.getMoneyOfUser(user);
        double minPerTrans = transferConfig.getMinPerTrans().doubleValue();
        double maxPerTransConfig = transferConfig.getMaxPerTrans().doubleValue();
        if (userType != UserType.AGENT) {
            if (minPerTrans != 0 && transferMoney < minPerTrans) {
                trace(ExtensionLogLevel.WARN, userId, "cheat transfer min");
                return;
            }

            double maxPerDay = transferConfig.getMaxPerDay().doubleValue();
            double maxPerTrans = getMaxPerTrans(fromUserMoney, maxPerTransConfig, userId, maxPerDay);
            if (transferMoney > maxPerTrans) {
                trace(ExtensionLogLevel.WARN, userId, "cheat transfer max");
                return;
            }
        }
        
        double fee = userType == UserType.AGENT ? transferConfig.getTransferFeeAgent() : transferConfig.getTransferFee();
        fee = transferMoney * (fee / 100);
        BigDecimal fees = new BigDecimal(fee).setScale(0, RoundingMode.HALF_UP);
        
        isfso.putInt(SFSKey.CODE, 1);
        isfso.putDouble(SFSKey.FEE, fees.doubleValue());
        getParentExtension().send(SFSCommand.CLIENT_REQUEST, isfso, user);
    }

    private void transferMoney(User user, ISFSObject isfso) throws NoSuchAlgorithmException {
        P2PTransferConfig transferConfig = Database.instance.getTransferConfig();

        if (!transferConfig.isEnable()) {
            String msg = GameLanguage.getMessage(GameLanguage.FUNCTION_MAINTAIN, Utils.getUserLocale(user));
            sendMessage(user, msg);
            return;
        }

        int userType = Utils.getUserType(user);
        double money = isfso.getDouble(SFSKey.MONEY);       
        double fee = userType == UserType.AGENT ? transferConfig.getTransferFeeAgent() : transferConfig.getTransferFee();
        fee = money * (fee / 100);
        BigDecimal fees = new BigDecimal(fee).setScale(0, RoundingMode.HALF_UP);
        fee = fees.doubleValue();
        
        double transferMoney = money + fee;
        
        String data = isfso.getUtfString(SFSKey.USER_ID);
        String userId = user.getVariable(UserInforPropertiesKey.ID_DB_USER).getStringValue();
        
        if (!Database.instance.checkVerify(userId)) {
            trace(ExtensionLogLevel.WARN, userId, "cheat transfer userid inactive");
            return;
        }

        String toUserId = Database.instance.getUserIdByTransferData(data);
        if (toUserId == null) {
            trace(ExtensionLogLevel.WARN, userId, "cheat transfer userid");
            return;
        }
        
        if (!Database.instance.checkVerify(toUserId)) {
            trace(ExtensionLogLevel.WARN, userId, "cheat transfer userid inactive", toUserId);
            return;
        }

        if (userId.equals(toUserId)) {
            trace(ExtensionLogLevel.WARN, userId, "cheat transfer userid");
            return;
        }

        double fromUserMoney = Utils.getMoneyOfUser(user);
//        double quota = Database.instance.getUserAvailableWithdrawMoney(userId);
//        if (transferMoney > Math.min(quota, fromUserMoney)) {
//            trace(ExtensionLogLevel.WARN, userId, "cheat transfer money");
//            return;
//        }
        if (userType != UserType.AGENT) {
            double minPerTrans = transferConfig.getMinPerTrans().doubleValue();
            if (minPerTrans != 0 && money < minPerTrans) {
                trace(ExtensionLogLevel.WARN, userId, "cheat transfer min");
                return;
            }

            double maxPerTransConfig = transferConfig.getMaxPerTrans().doubleValue();
            double maxPerDay = transferConfig.getMaxPerDay().doubleValue();
            double maxPerTrans = getMaxPerTrans(fromUserMoney, maxPerTransConfig, userId, maxPerDay);

            if (money > maxPerTrans) {
                trace(ExtensionLogLevel.WARN, userId, "cheat transfer max");
                return;
            }
        }

        double receiveMoney = money;
        BigDecimal transferMoneyR = new BigDecimal(String.valueOf(transferMoney));
        BigDecimal receiveMoneyR = new BigDecimal(String.valueOf(receiveMoney));
        TransferMoneyResult rs = Database.instance.callTransferMoneyProcedure(userId, toUserId, transferMoneyR, receiveMoneyR);
        trace("transfer: ", userId, toUserId, transferMoney, fromUserMoney);
        trace("transfer result: ", rs.toString());

        if (!rs.isSuccess()) {
            String msg = GameLanguage.getMessage(GameLanguage.ERROR_TRY_AGAIN, Utils.getUserLocale(user));
            sendMessage(user, msg);
            return;
        }
        
        Database.instance.insertTranferLog(userId, toUserId, receiveMoneyR, fees);
        Utils.updateMoneyOfUser(user, rs.fromMoneyAfter.doubleValue());

        String nameMoney = GameLanguage.getMessage(GameLanguage.NAME_MONEY, Utils.getUserLocale(user));
        String msg = GameLanguage.getMessage(GameLanguage.TRANSFER_MONEY_SUCCESS_1, Utils.getUserLocale(user));
        sendMessage(user, msg);

        User toUser = Utils.findUser(toUserId);
        if (toUser == null) {
            BigDecimal receiveMoneyOff = new BigDecimal(receiveMoney);
            Database.instance.insertReceiveMoneyOffline(userId, toUserId, receiveMoneyOff);
        } else {
            Utils.updateMoneyOfUser(toUser, rs.toMoneyAfter.doubleValue());

            msg = String.format(GameLanguage.getMessage(GameLanguage.TRANSFER_MONEY_SUCCESS_2, Utils.getUserLocale(toUser)), userId, Utils.formatNumber(receiveMoney), nameMoney);
            sendMessage(toUser, msg);
        }
//        BigDecimal tranfer = new BigDecimal(transferMoney);
//        Database.instance.addUserAvailableWithdrawMoney(userId, tranfer.negate());

//        Database.instance.updateUserAvailableWithdrawMoney(toUserId, rs.toMoneyBefore);
        long now = System.currentTimeMillis();
        PaymentInfo info = new PaymentInfo();
        info.setPlayerId(toUserId);
        info.setPlayerName(Database.instance.getDisplayName(toUserId));
        info.setBalance(receiveMoney);
        ClientInfo clientInfo = (ClientInfo) user.getSession().getProperty(UserInforPropertiesKey.CLIENT_INFOR);

        PaymentQueueObj obj = new PaymentQueueObj();
        obj.setCreatedAt(now / 1000);
        obj.setRequestId(Utils.md5String(userId + now));
        obj.setSessionId(clientInfo.getSessionId());
        obj.setPlayerId(userId);
        obj.setPlayerName(user.getVariable(UserInforPropertiesKey.DISPLAY_NAME).getStringValue());
        obj.setMoney(-transferMoney);
        obj.setCurrency(nameMoney);
        obj.setOrderId(Utils.md5String(userId + toUserId + now));
        obj.setTransaction(Utils.md5String(userId + toUserId));
        obj.setSupplierResponse(Utils.md5String(toUserId));
        obj.setSupplierTransaction(Utils.md5String(toUserId));
        obj.setMerchantId(ServerConfig.getInstance().getMerchantId());
        obj.setConnectionId(String.valueOf(ServerConfig.getInstance().getConnectionId()));
        obj.setPaymentFlow(PaymentQueueObj.PAYMENT_FLOW_TRANSFER);
        obj.setPaymentInfo(info);
        obj.setChannel(String.valueOf(user.getSession().getProperty(UserInforPropertiesKey.CHANNEL)));
        obj.setPaymentMethod("ingame");
        obj.setPaymentType("ingame");
        obj.setUnit("real");
        obj.setStatus(1);

        QueueServiceApi.getInstance().sendData(QueueConfig.getInstance().getKeyPayment(), true, obj);
        Database.instance.logTransaction(null, userId, MoneyContants.MONEY, -transferMoney, rs.fromMoneyAfter.doubleValue(), Transaction.TYPE_TRANSFER, Transaction.STATUS_SUCCESS);
        Database.instance.logTransaction(null, toUserId, MoneyContants.MONEY, receiveMoney, rs.toMoneyAfter.doubleValue(), Transaction.TYPE_TRANSFER, Transaction.STATUS_SUCCESS);

        UserBalanceUpdate ubu = new UserBalanceUpdate();
        ubu.setPlayerId(userId);
        ubu.setEmail(String.valueOf(user.getSession().getProperty(UserInforPropertiesKey.EMAIL)));
        ubu.setCurrency(GameLanguage.getMessage(GameLanguage.NAME_MONEY, Locale.ENGLISH));
        ubu.setCreatedAt(System.currentTimeMillis() / 1000);
        ubu.setSessionId(String.valueOf(user.getProperty(UserInforPropertiesKey.SESSION_ID)));
        ubu.setDescription("Transfer money");
        ubu.setLastBalance(rs.fromMoneyBefore);
        ubu.setBalance(rs.fromMoneyAfter);
        ubu.setChange(-transferMoney);
        ubu.setConnectionId(ServerConfig.getInstance().getConnectionId());
        ubu.setRequestId(Utils.md5String(userId + System.currentTimeMillis()));
        ubu.setUnit("real");
        ubu.setPaymentFlow(UserBalanceUpdate.PAYMENT_FLOW_TRANSFER);
        ubu.setLogId(Utils.md5String(userId + toUserId + System.currentTimeMillis()));
        ubu.setChannel(String.valueOf(user.getSession().getProperty(UserInforPropertiesKey.CHANNEL)));
        QueueServiceApi.getInstance().sendData(QueueConfig.getInstance().getKeyBalance(), true, ubu);

        ubu.setPlayerId(toUserId);
        ubu.setEmail(Database.instance.getUserEmail(toUserId));
        ubu.setDescription("Receive transfer money");
        ubu.setLastBalance(rs.toMoneyBefore);
        ubu.setBalance(rs.toMoneyAfter);
        ubu.setChange(receiveMoney);
        ubu.setRequestId(Utils.md5String(toUserId + System.currentTimeMillis()));
        ubu.setChannel(Database.instance.getUserChannel(toUserId));
        if (toUser != null) {
            ubu.setSessionId(String.valueOf(toUser.getProperty(UserInforPropertiesKey.SESSION_ID)));
        } else {
            ubu.setSessionId("");
        }
        QueueServiceApi.getInstance().sendData(QueueConfig.getInstance().getKeyBalance(), true, ubu);
    }

    private void getTransferQuota(User user, ISFSObject isfso) {
        double money = Utils.getMoneyOfUser(user);
        P2PTransferConfig transferConfig = Database.instance.getTransferConfig();
        double maxPerTransConfig = transferConfig.getMaxPerTrans().doubleValue();
        String userId = user.getVariable(UserInforPropertiesKey.ID_DB_USER).getStringValue();
        double minPerTrans = transferConfig.getMinPerTrans().doubleValue();
        double maxPerTrans = money;
        double transferFee = transferConfig.getTransferFee();
        if (Utils.getUserType(user) == UserType.AGENT) {
            transferFee = transferConfig.getTransferFeeAgent();
        } else {
            double maxPerDay = transferConfig.getMaxPerDay().doubleValue();
            maxPerTrans = getMaxPerTrans(money, maxPerTransConfig, userId, maxPerDay);
        }
        
        if (maxPerTrans == money) {
            maxPerTrans = maxPerTrans / (1 + transferFee / 100);
            maxPerTrans = (new BigDecimal(maxPerTrans).setScale(0, RoundingMode.FLOOR)).doubleValue();
        }

        isfso.putDouble(SFSKey.MONEY, maxPerTrans);
        isfso.putDouble(SFSKey.MIN, minPerTrans);
        getParentExtension().send(SFSCommand.CLIENT_REQUEST, isfso, user);
    }

    /**
     * crypto withdraw
     *
     * @param user
     * @param isfso
     */
    private void withdraw(User user, ISFSObject isfso) {
        String accessToken = String.valueOf(user.getSession().getProperty(UserInforPropertiesKey.ACCESS_TOKEN));
        String code = isfso.getUtfString(SFSKey.CODE);
        String address = isfso.getUtfString(SFSKey.ADDRESS);
        double money = isfso.getDouble(SFSKey.MONEY);

        double userMoney = Utils.getMoneyOfUser(user);
        if (userMoney < money) {
            sendMessage(user, GameLanguage.getMessage(GameLanguage.NOT_ENOUGH_MONEY, Utils.getUserLocale(user)));
            return;
        }

        try {
            String response = APIUtils.requestPassportWithdraw(accessToken, code, address, money, Utils.getUserLocale(user).getLanguage());
            JsonObject json = GsonUtil.parse(response).getAsJsonObject();
            if (json.get("code").getAsInt() == 90050) {
                json = json.getAsJsonObject("data");
                isfso.putDouble(SFSKey.RATE, json.get("exchange").getAsDouble());
                isfso.putDouble(SFSKey.VALUE, json.get("amountReceive").getAsDouble());
                isfso.putInt(SFSKey.RESULT, 1);

                json.addProperty("userId", user.getVariable(UserInforPropertiesKey.ID_DB_USER).getStringValue());
                json.addProperty("type", "crypto");
                QueueService.getInstance().sendWithdrawTransaction(json.toString());
            } else {
                json = json.getAsJsonObject("data");
                isfso.putInt(SFSKey.RESULT, 0);
                isfso.putUtfString(SFSKey.MESSAGE, json.get("message").getAsString());
            }
        } catch (Exception e) {
            getLogger().error("withdraw", e);
            isfso.putInt(SFSKey.RESULT, 0);
            isfso.putUtfString(SFSKey.MESSAGE, GameLanguage.getMessage(GameLanguage.ERROR_TRY_AGAIN, Utils.getUserLocale(user)));
        }
        getParentExtension().send(SFSCommand.CLIENT_REQUEST, isfso, user);
    }

    /**
     * banking withdraw
     *
     * @param user
     * @param isfso
     */
    private void withdrawBanking(User user, ISFSObject isfso) throws NoSuchAlgorithmException {
        String userId = user.getVariable(UserInforPropertiesKey.ID_DB_USER).getStringValue();
        String accessToken = String.valueOf(user.getSession().getProperty(UserInforPropertiesKey.ACCESS_TOKEN));
        String lang = Utils.getUserLocale(user).getLanguage();
        double money = isfso.getDouble(SFSKey.MONEY);
        String accountName = isfso.getUtfString(SFSKey.NAME);
        String accountNumber = isfso.getUtfString(SFSKey.NUMBER);
        String bankCode = isfso.getUtfString(SFSKey.CODE);
        String bankProvince = isfso.getUtfString(SFSKey.PROVINCE);
        String bankCity = isfso.getUtfString(SFSKey.CITY);
        String bankBranch = isfso.getUtfString(SFSKey.BRANCH);

        if (money <= 0) {
            trace(ExtensionLogLevel.WARN, user.getName(), "cheat withdraw money");
            return;
        }

        //Malaya ko có giới hạn chuyển tiền
//        if (money > Database.instance.getUserAvailableWithdrawMoney(userId)) {
//            trace(ExtensionLogLevel.WARN, user.getName(), "cheat available withdraw money");
//            sendMessage(user, GameLanguage.getMessage(GameLanguage.TRANSFER_MONEY_FAIL_5, Utils.getUserLocale(user)));
//            return;
//        }
        UpdateMoneyResult umr = Database.instance.callUpdateMoneyProcedure(userId, new BigDecimal(-money));
        if (umr == null || umr.after.compareTo(umr.before) == 0) {
            sendMessage(user, GameLanguage.getMessage(GameLanguage.NOT_ENOUGH_MONEY, Utils.getUserLocale(user)));
            return;
        }
        Utils.updateMoneyOfUser(user, umr.after.doubleValue());

        boolean isError = false;
        try {
            String response = APIUtils.requestPassportBankingWithdraw(accessToken, money, accountName, accountNumber, bankCode, bankProvince, bankCity, bankBranch, lang);
            JsonObject json = GsonUtil.parse(response).getAsJsonObject();
            isfso.putUtfString(SFSKey.DATA, json.get("data").toString());
            int code = json.get("code").getAsInt();
            switch (code) {
                case 90050:
                case 90060:
                    isfso.putInt(SFSKey.RESULT, code == 90050 ? 1 : 0);
                    json = json.getAsJsonObject("data");
                    json.addProperty("userId", userId);
                    json.addProperty("amount", money);
                    json.addProperty("type", "banking");
                    QueueService.getInstance().sendWithdrawTransaction(json.toString());
                    BigDecimal quota = new BigDecimal(money);
                    Database.instance.addUserAvailableWithdrawMoney(userId, quota.negate());
                    break;

                default:
                    isError = true;
                    isfso.putInt(SFSKey.RESULT, 0);
                    umr = Database.instance.callUpdateMoneyProcedure(userId, new BigDecimal(money));
                    if (umr != null && umr.after.compareTo(umr.before) != 0) {
                        Utils.updateMoneyOfUser(user, umr.after.doubleValue());
                    }
            }
            getParentExtension().send(SFSCommand.CLIENT_REQUEST, isfso, user);
        } catch (Exception e) {
            getLogger().error("withdrawBanking", e);
            sendMessage(user, GameLanguage.getMessage(GameLanguage.ERROR_TRY_AGAIN, Utils.getUserLocale(user)));
        }

        if (!isError) {
            UserBalanceUpdate ubu = new UserBalanceUpdate();
            ubu.setPlayerId(userId);
            ubu.setEmail(String.valueOf(user.getSession().getProperty(UserInforPropertiesKey.EMAIL)));
            ubu.setCurrency(GameLanguage.getMessage(GameLanguage.NAME_MONEY, Locale.ENGLISH));
            ubu.setCreatedAt(System.currentTimeMillis() / 1000);
            ubu.setSessionId(String.valueOf(user.getProperty(UserInforPropertiesKey.SESSION_ID)));
            ubu.setDescription("Withdraw money");
            ubu.setLastBalance(umr.before);
            ubu.setBalance(umr.after);
            ubu.setChange(money);
            ubu.setConnectionId(ServerConfig.getInstance().getConnectionId());
            ubu.setRequestId(Utils.md5String(userId + System.currentTimeMillis()));
            ubu.setUnit("real");
            ubu.setPaymentFlow(UserBalanceUpdate.PAYMENT_FLOW_WITHDRAW);
            ubu.setChannel(String.valueOf(user.getSession().getProperty(UserInforPropertiesKey.CHANNEL)));
            QueueServiceApi.getInstance().sendData(QueueConfig.getInstance().getKeyBalance(), true, ubu);
        }
    }

    private void getBankingWithdrawInfo(User user, ISFSObject isfso) {
        String userId = user.getVariable(UserInforPropertiesKey.ID_DB_USER).getStringValue();
        double quota = Database.instance.getUserAvailableWithdrawMoney(userId);
        double money = Utils.getMoneyOfUser(user);
        if (quota > money) {
            quota = money;
        }
        isfso.putDouble(SFSKey.MONEY, quota);

        String response = APIUtils.requestPassportConfig();
        JsonObject json = GsonUtil.parse(response).getAsJsonObject().getAsJsonObject("data").getAsJsonObject("configs").getAsJsonObject("ezpay").getAsJsonObject("withdraw");
        JsonArray data = json.getAsJsonArray("data");
        for (JsonElement e : data) {
            JsonObject obj = e.getAsJsonObject();
            switch (obj.get("code").getAsString()) {
                case "fee":
                    isfso.putDouble(SFSKey.FEE, obj.get("value").getAsDouble());
                    break;
                case "minTrans":
                    isfso.putDouble(SFSKey.MIN, obj.get("value").getAsDouble());
                    break;
                case "maxTrans":
                    isfso.putDouble(SFSKey.MAX, obj.get("value").getAsDouble());
                    break;
            }
        }

        getParentExtension().send(SFSCommand.CLIENT_REQUEST, isfso, user);
    }

    /**
     * lấy số bàn, số người trong từng game
     *
     * @param user
     * @param isfso
     */
    private void getBoardUserCount(User user, ISFSObject isfso) {
        if (ServerConfig.getInstance().getBoardUserCountTime() <= 0) {
            return;
        }

        Device device = Utils.getDevice(user);
        byte moneyType = Utils.getMoneyTypeOfUser(user);
        List<GameConfigInfor> listRoomLobby = getGameInforListByMoneyType(moneyType);
        SFSArray arr = new SFSArray();
        for (GameConfigInfor lobby : listRoomLobby) {
            String lobbyName = Utils.getLobbyName(lobby.getServiceId(), lobby.getMoneyType());
            if (TurnOffGameConfig.getInstance().isTurnOffGame(lobbyName, device.getPlatForm().getName(), device.getVersion().getVersionName(), device.getBundleId(), moneyType)) {
                continue;
            }

            Room room = getParentExtension().getParentZone().getRoomByName(lobbyName);
            int userCount = room.getUserList().size();
            int boardCount = HazelcastUtil.getBoardCount(lobby.getServiceId(), moneyType);

            SFSObject obj = new SFSObject();
            obj.putUtfString(SFSKey.NAME, lobbyName);
            obj.putInt(SFSKey.USER, userCount);
            obj.putInt(SFSKey.BOARDS, boardCount);
            arr.addSFSObject(obj);
        }

        isfso.putSFSArray(SFSKey.DATA, arr);
        getParentExtension().send(SFSCommand.CLIENT_REQUEST, isfso, user);
    }

    private void processEventRequest(User user, ISFSObject isfso) {
        int cmd = isfso.getInt(SFSKey.COMMAND);
        String data = isfso.containsKey(SFSKey.DATA) ? isfso.getUtfString(SFSKey.DATA) : null;
        String userId = user.getVariable(UserInforPropertiesKey.ID_DB_USER).getStringValue();
        String username = user.getVariable(UserInforPropertiesKey.DISPLAY_NAME).getStringValue();
        EventData evData = new EventData(cmd, userId, username);
        String lang = Utils.getUserLocale(user).getLanguage();
        evData.setLang(lang);
        evData.setData(data);
        evData.setServerId(ServerConfig.getInstance().getServerId());
        QueueQuest.getInstance().sendQuestRequest(evData);
    }

    private void processRechargeCard(User user, ISFSObject isfso) throws NoSuchAlgorithmException {
        if (!ServerConfig.getInstance().isChargeEnable()) {
            String msg = GameLanguage.getMessage(GameLanguage.FUNCTION_MAINTAIN, Utils.getUserLocale(user));
            sendMessage(user, msg);
            return;
        }

        String userId = user.getVariable(UserInforPropertiesKey.ID_DB_USER).getStringValue();
        String accessToken = (String) user.getSession().getProperty(UserInforPropertiesKey.ACCESS_TOKEN);
        ClientInfo clientInfo = (ClientInfo) user.getSession().getProperty(UserInforPropertiesKey.CLIENT_INFOR);
        String data = isfso.getUtfString(SFSKey.DATA);
        RechargeCardDataInfo rcd = GsonUtil.fromJson(data, RechargeCardDataInfo.class);
        rcd.setOs(clientInfo.getPlatform());
        rcd.setChannel(clientInfo.getChannel());

        String telco = rcd.getTelco();
        String serial = rcd.getSerial();
        String pin = rcd.getPin();
        double price = rcd.getPrice();

        rcd.setTransaction(Utils.md5String(userId + telco + serial + pin + price + System.currentTimeMillis()));

        String transactionId = rcd.getTransaction();
        String rechargeCardData = GsonUtil.toJson(rcd);

        String response = APIUtils.requestCharge(UrlConfig.getInstance().getRechargeCardUrl(), rechargeCardData, accessToken, Utils.getUserLocale(user).getLanguage());
        if (response != null) {
            Database.instance.addRechargeCard(userId, telco, serial, pin, price, transactionId, response);
            isfso.putUtfString(SFSKey.DATA, response);
        } else {
            JsonObject json = new JsonObject();
            json.addProperty(SFSKey.CODE, 400);
            JsonObject jsonMessage = new JsonObject();
            jsonMessage.addProperty(SFSKey.MESSAGE, GameLanguage.getMessage(GameLanguage.TRANSFER_MONEY_FAIL_5, Utils.getUserLocale(user)));
            json.add(SFSKey.DATA, jsonMessage);
            isfso.putUtfString(SFSKey.DATA, json.toString());
        }

        getParentExtension().send(SFSCommand.CLIENT_REQUEST, isfso, user);
    }

    private void getPointConvertConfig(User user, ISFSObject isfso) {
        PointConvertConfig wcc = Database.instance.getPointConvertConfig();
        if (!wcc.isEnable()) {
            String msg = GameLanguage.getMessage(GameLanguage.FUNCTION_MAINTAIN, Utils.getUserLocale(user));
            sendMessage(user, msg);
            return;
        }

        String userId = user.getVariable(UserInforPropertiesKey.ID_DB_USER).getStringValue();
        double quota = Database.instance.getPointConvertQuota(userId).doubleValue();
        double point = Utils.getPointOfUser(user);
        if (quota > point) {
            quota = point;
        }
        int convertRate = wcc.getConvertRate();
        double minConvert = wcc.getMinConvertPerTime().doubleValue();
        double maxConvert = wcc.getMaxConvertPerTime().doubleValue();

        isfso.putDouble(SFSKey.POINT, quota);
        isfso.putInt(SFSKey.RATE, convertRate);
        isfso.putDouble(SFSKey.MIN, minConvert);
        isfso.putDouble(SFSKey.MAX, maxConvert);

        getParentExtension().send(SFSCommand.CLIENT_REQUEST, isfso, user);
    }

    private void convertPoint2Money(User user, ISFSObject isfso) throws NoSuchAlgorithmException {
        PointConvertConfig wcc = Database.instance.getPointConvertConfig();
        if (!wcc.isEnable()) {
            String msg = GameLanguage.getMessage(GameLanguage.FUNCTION_MAINTAIN, Utils.getUserLocale(user));
            sendMessage(user, msg);
            return;
        }
        String userId = user.getVariable(UserInforPropertiesKey.ID_DB_USER).getStringValue();
        double pointConvert = isfso.getDouble(SFSKey.POINT);
        BigDecimal pointInputValue = new BigDecimal(Double.toString(pointConvert));

        if (wcc.getMinConvertPerTime().compareTo(pointInputValue) > 0 || pointInputValue.compareTo(wcc.getMaxConvertPerTime()) > 0) {
            trace(ExtensionLogLevel.WARN, userId, "cheat convert");
            return;
        }

        BigDecimal quota = Database.instance.getPointConvertQuota(userId);
        double point = Utils.getPointOfUser(user);

        if (pointConvert > quota.doubleValue() || pointConvert > point) {
            trace(ExtensionLogLevel.WARN, userId, "cheat convert");
            return;
        }

        String email = String.valueOf(user.getSession().getProperty(UserInforPropertiesKey.EMAIL));
        String channel = String.valueOf(user.getSession().getProperty(UserInforPropertiesKey.CHANNEL));
        BigDecimal convertRate = new BigDecimal(wcc.getConvertRate());
        BigDecimal moneyAdd = pointInputValue.divide(convertRate, 0, RoundingMode.DOWN);
        BigDecimal subPoint = moneyAdd.multiply(convertRate);

        BigDecimal moneyReceived = Database.instance.getMoneyReceiveFromPointInfo(userId);
        BigDecimal totalMoneyConvert = moneyReceived.add(moneyAdd);
        if (totalMoneyConvert.compareTo(wcc.getConvertLimitPerDay()) > 0) {
            BigDecimal remain = wcc.getConvertLimitPerDay().subtract(moneyReceived);
            if (remain.signum() <= 0) {
                String msg = GameLanguage.getMessage(GameLanguage.CONVERT_LIMIT, Utils.getUserLocale(user));
                sendMessage(user, msg);
            } else {
                BigDecimal remainPoint = remain.multiply(convertRate);
                if (wcc.getMinConvertPerTime().compareTo(remainPoint) > 0) {
                    String msg = GameLanguage.getMessage(GameLanguage.CONVERT_LIMIT, Utils.getUserLocale(user));
                    sendMessage(user, msg);
                    return;
                }
                sendConvertMessage(user, remainPoint, isfso);
            }
            return;
        }

        String id = Utils.md5String(userId + System.currentTimeMillis()).substring(0, 20);
        if (!wcc.isEnableAutoConvert()) {
            UpdateMoneyResult umr = Database.instance.callUpdatePointProcedure(userId, subPoint.negate());
            if (umr == null || umr.before.compareTo(umr.after) == 0) {
                trace("convert fail", userId, pointConvert);
                String msg = GameLanguage.getMessage(GameLanguage.ERROR_TRY_AGAIN, Utils.getUserLocale(user));
                sendMessage(user, msg);
                return;
            }

            Database.instance.addPointConvertQuota(userId, subPoint.negate());

            Utils.updatePointOfUser(user, umr.after.doubleValue());

            Database.instance.insertApprovalConvertPoint(id, userId, email, subPoint, channel, moneyAdd, Transaction.STATUS_PENDING);
            Database.instance.logTransaction(id, userId, MoneyContants.POINT, subPoint.negate().doubleValue(), umr.after.doubleValue(), Transaction.TYPE_CONVERT, Transaction.STATUS_PENDING);
            String msg = GameLanguage.getMessage(GameLanguage.APPROVAL, Utils.getUserLocale(user));
            sendMessage(user, msg);
            return;
        }

        ConvertMoneyResult rs = Database.instance.callConvertMoneyResult(userId, subPoint, moneyAdd);
        if (!rs.isSuccess()) {
            trace("convert fail", rs.toString());
            String msg = GameLanguage.getMessage(GameLanguage.ERROR_TRY_AGAIN, Utils.getUserLocale(user));
            sendMessage(user, msg);
            return;
        }

        Database.instance.insertApprovalConvertPoint(id, userId, email, subPoint, channel, moneyAdd, Transaction.STATUS_SUCCESS);
        if (rs.pointAfter.signum() == 0) {        // convert all user point -> reset convert quota
            Database.instance.updatePointConvertQuota(userId, BigDecimal.ZERO);
        } else {
            Database.instance.updatePointConvertQuota(userId, quota.subtract(subPoint));
        }

        Database.instance.updateUserAvailableWithdrawMoney(userId, rs.moneyBefore);

        String msg = GameLanguage.getMessage(GameLanguage.WIN_CONVERT_VND_SUCCESS, Utils.getUserLocale(user));
        String namePoint = GameLanguage.getMessage(GameLanguage.NAME_POINT, Utils.getUserLocale(user));
        String nameMoney = GameLanguage.getMessage(GameLanguage.NAME_MONEY, Utils.getUserLocale(user));
        msg = String.format(msg, Utils.formatNumber(subPoint.intValue()), namePoint, Utils.formatNumber(moneyAdd.intValue()), nameMoney);
        sendMessage(user, msg);

        Utils.updateMoneyOfUser(user, rs.moneyAfter.doubleValue());
        Utils.updatePointOfUser(user, rs.pointAfter.doubleValue());

        Database.instance.logTransaction(null, userId, MoneyContants.POINT, subPoint.negate().doubleValue(), rs.pointAfter.doubleValue(), Transaction.TYPE_CONVERT, Transaction.STATUS_SUCCESS);
        Database.instance.logTransaction(null, userId, MoneyContants.MONEY, moneyAdd.doubleValue(), rs.moneyAfter.doubleValue(), Transaction.TYPE_CONVERT, Transaction.STATUS_SUCCESS);

        UserBalanceUpdate ubu = new UserBalanceUpdate();
        ubu.setPlayerId(userId);
        ubu.setEmail(email);
        ubu.setCurrency(GameLanguage.getMessage(GameLanguage.NAME_POINT, Locale.ENGLISH));
        ubu.setCreatedAt(System.currentTimeMillis() / 1000);
        ubu.setSessionId(String.valueOf(user.getProperty(UserInforPropertiesKey.SESSION_ID)));
        ubu.setDescription("Convert point to money");
        ubu.setLastBalance(rs.pointBefore);
        ubu.setBalance(rs.pointAfter);
        ubu.setChange(subPoint.negate());
        ubu.setConnectionId(ServerConfig.getInstance().getConnectionId());
        ubu.setRequestId(Utils.md5String(userId + System.currentTimeMillis()));
        ubu.setUnit("point");
        ubu.setPaymentFlow(UserBalanceUpdate.PAYMENT_FLOW_CONVERT);
        ubu.setChannel(String.valueOf(user.getSession().getProperty(UserInforPropertiesKey.CHANNEL)));
        QueueServiceApi.getInstance().sendData(QueueConfig.getInstance().getKeyBalance(), true, ubu);

        ubu.setCurrency(GameLanguage.getMessage(GameLanguage.NAME_MONEY, Locale.ENGLISH));
        ubu.setDescription("Receive money from point");
        ubu.setLastBalance(rs.moneyBefore);
        ubu.setBalance(rs.moneyAfter);
        ubu.setChange(moneyAdd);
        ubu.setUnit("real");
        QueueServiceApi.getInstance().sendData(QueueConfig.getInstance().getKeyBalance(), true, ubu);

    }

    private void sendConvertMessage(User user, BigDecimal point, ISFSObject isfso) {
        isfso.putInt(SFSKey.ACTION_INCORE, SFSAction.CONVERT_POINT_2_MONEY_MESSAGE);
        isfso.putDouble(SFSKey.POINT, point.doubleValue());
        getParentExtension().send(SFSCommand.CLIENT_REQUEST, isfso, user);
    }

    public void linkFacebook(User user, ISFSObject isfso) {
        String clientId = isfso.getUtfString(SFSKey.CLIENT_ID);
        String fbToken = isfso.getUtfString(SFSKey.TOKEN);
        JsonObject json = new JsonObject();
        json.addProperty("clientId", clientId);
        json.addProperty("facebookAccesstoken", fbToken);
        String data = GsonUtil.toJson(json);
        String accessToken = (String) user.getSession().getProperty(UserInforPropertiesKey.ACCESS_TOKEN);
        String response = APIUtils.requestFacebook(UrlConfig.getInstance().getLinkFacebook(), data, accessToken, Utils.getUserLocale(user).getLanguage());
        JsonObject obj = GsonUtil.parse(response).getAsJsonObject();
        JsonObject dataRepo = obj.getAsJsonObject("data");
        if (obj.get("code").getAsInt() == 8002) {
            String facebookId = dataRepo.get("facebookId").getAsString();
            String displayName = dataRepo.get("displayName").getAsString();
            String avatar = dataRepo.get("avatar").getAsString();
            String userId = user.getVariable(UserInforPropertiesKey.ID_DB_USER).getStringValue();

            Database.instance.updateFacebookInfo(facebookId, userId, displayName, avatar);
            isfso.putInt(SFSKey.CODE, 1);
        } else {
            isfso.putInt(SFSKey.CODE, 0);
        }
        isfso.putUtfString(SFSKey.MESSAGE, dataRepo.get("message").getAsString());
        getParentExtension().send(SFSCommand.CLIENT_REQUEST, isfso, user);
    }

    private double getMaxPerTrans(double fromUserMoney, double maxPerTransConfig, String userId, double maxPerDay) {
        double maxPerTrans = fromUserMoney;

        if (maxPerTransConfig > 0 && maxPerTrans > maxPerTransConfig) {
            maxPerTrans = maxPerTransConfig;
        }

        if (maxPerDay > 0) {
            double curDateTransferMoney = Database.instance.getCurDateTranferMoney(userId).doubleValue();
            double curDateRemain = maxPerDay - curDateTransferMoney;
            if (maxPerTrans > curDateRemain) {
                maxPerTrans = curDateRemain;
            }
        }

        return maxPerTrans;
    }

    private void processChat(User user, ISFSObject isfso) {
        if (!ServerConfig.getInstance().isChatEnable()) {
            String msg = GameLanguage.getMessage(GameLanguage.FUNCTION_MAINTAIN, Utils.getUserLocale(user));
            sendMessage(user, msg);
            return;
        }
        
        long now = System.currentTimeMillis();
        long lastTimeChat = 0;
        if (user.containsProperty(UserInforPropertiesKey.LAST_TIME_CHAT_INGAME)) {
            lastTimeChat = (long) user.getProperty(UserInforPropertiesKey.LAST_TIME_CHAT_INGAME);
        }
        
        int interval = ServerConfig.getInstance().getChatInterval();
        if (now - lastTimeChat < interval) {
            long waitTime = (interval + lastTimeChat - now) / 1000;
            String msg = String.format(GameLanguage.getMessage(GameLanguage.CHAT_INTERVAL, Utils.getUserLocale(user)), waitTime);
            isfso = MessageController.getToastMessage(msg, 3);
            getParentExtension().send(SFSCommand.CLIENT_REQUEST, isfso, user);
            return;
        }
        
        String userId = user.getVariable(UserInforPropertiesKey.ID_DB_USER).getStringValue();
        String displayName = user.getVariable(UserInforPropertiesKey.DISPLAY_NAME).getStringValue();
        String message = isfso.getUtfString(SFSKey.MESSAGE);
        // todo: chat filter

        isfso.putUtfString(SFSKey.USER_ID, userId);
        isfso.putUtfString("displayName", displayName);
        BroadcastService.broadcast(SFSCommand.CLIENT_REQUEST, isfso);
        user.setProperty(UserInforPropertiesKey.LAST_TIME_CHAT_INGAME, now);
    }

}
