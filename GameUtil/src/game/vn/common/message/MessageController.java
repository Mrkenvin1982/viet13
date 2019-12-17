/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.vn.common.message;

import com.smartfoxserver.v2.entities.User;
import com.smartfoxserver.v2.entities.data.ISFSArray;
import com.smartfoxserver.v2.entities.data.SFSArray;
import com.smartfoxserver.v2.entities.data.SFSObject;
import game.command.SFSAction;
import game.key.SFSKey;
import game.vn.common.GameController;
import game.vn.common.object.boardhistory.UserMoneyLog;
import game.vn.common.properties.UserInforPropertiesKey;
import game.vn.util.Utils;
import java.util.List;
import java.util.Map;

/**
 * Class này xử lý tất cả các message chung cho tất cả các game
 * trong Gamecontroler
 * @author tuanp
 */
public class MessageController {
    
    private final GameController game;
    public MessageController(GameController gameInput) {
        this.game= gameInput;
    }
     /**
     * Gửi message update tiền thắng- thua trong game
     * @param userID
     * @param value
     * @param text
     * @return 
     */
    public SFSObject getBonusMoney(String userID, double value, String text) {
        SFSObject fObject = new SFSObject();
        fObject.putInt(SFSKey.ACTION_INGAME, SFSAction.BONUS_MONEY);
        fObject.putUtfString(SFSKey.USER_ID, userID);
        fObject.putDouble(SFSKey.MONEY_BONUS, value);
        fObject.putUtfString(SFSKey.STRING_MESSAGE_VI, "");

        return fObject;
    }
    
    /**
     * gửi message thông báo đến user
     *
     * @param point
     * @param money
     * @return
     */
    public static SFSObject getUpdateMoneyAndPointMessage(long point,double money) {
        SFSObject fObject = new SFSObject();
        fObject.putInt(SFSKey.ACTION_INCORE, SFSAction.UPDATE_MONEY_POINT);
        fObject.putDouble(SFSKey.MONEY_USER, money);
        fObject.putLong(SFSKey.POINT, point);
        return fObject;
    }

    /**
     * gửi message thông báo đến user dạng dialog tĩnh
     * @param msg
     * @return 
     */
    public static SFSObject getStaticMessage(String msg) {
        SFSObject sfsObj = new SFSObject();
        sfsObj.putInt(SFSKey.ACTION_INCORE, SFSAction.MESSAGE_ERROR);
        sfsObj.putUtfString(SFSKey.TOAST_MESSAGE, msg);
        sfsObj.putByte(SFSKey.TIME_EXIT, (byte) 0);
        return sfsObj;
    }

    /**
     * gửi message thông báo đến user
     *
     * @param erro
     * @param timeExitMessage
     * @return
     */
    public static SFSObject getToastMessage(String erro, int timeExitMessage) {
        SFSObject fObject = new SFSObject();
        fObject.putInt(SFSKey.ACTION_INCORE, SFSAction.MESSAGE_ERROR);
        fObject.putUtfString(SFSKey.TOAST_MESSAGE, erro);
        fObject.putByte(SFSKey.TIME_EXIT, (byte) timeExitMessage);
        return fObject;
    }
    /**
     * Gửi về thời gian auto start ván
     *
     * @param autoStartTime
     * @return
     */
    public SFSObject getBoardInfoMessage(int autoStartTime) {
        SFSObject ojBoardInfo = new SFSObject();
        ojBoardInfo.putInt(SFSKey.ACTION_INGAME, SFSAction.BOARD_INFO);
        ojBoardInfo.putByte(SFSKey.COUNTDOWN_START_GAME_TIME, (byte) autoStartTime);
        return ojBoardInfo;
    }
    /**
     * Gửi thông tin mua tẩy
     * @param minStack
     * @param maxStack
     * @return 
     */
    public SFSObject getBuyStackMessage(double minStack, double maxStack){
        SFSObject ob = new SFSObject();
        ob.putInt(SFSKey.ACTION_INGAME, SFSAction.BUY_STACK_IN_GAME);
        ob.putDouble(SFSKey.MIN_MONEY_STACK, minStack);
        ob.putDouble(SFSKey.MAX_MONEY_STACK ,maxStack);
        return ob;
    }
    /**
     * Gửi về lịch sử ván chơi cho client
     * khi có phát sinh tiền
     * @param userMoneyLog
     * @return 
     */
    public SFSObject getHistoryMessage(UserMoneyLog userMoneyLog) {
        SFSObject fObject = new SFSObject();
        fObject.putInt(SFSKey.ACTION_INGAME, SFSAction.HISTORY_BOARD);
        fObject.putUtfString("bLogDate", userMoneyLog.getBoardLogDate());
        fObject.putUtfString("desc", userMoneyLog.getDescription());
        fObject.putDouble("moneyOfUser", userMoneyLog.getMoney());
        fObject.putDouble("value", userMoneyLog.getValue());
        fObject.putShort("reasonID", (short) userMoneyLog.getReasonId());
        fObject.putByte("serverID", (byte) userMoneyLog.getServerId());
        fObject.putShort("serviceID", (short) userMoneyLog.getServiceId());
        fObject.putDouble("tax", userMoneyLog.getTax());
        fObject.putUtfString("userId", userMoneyLog.getUserId());
        fObject.putShortArray("arrIDCard",userMoneyLog.getOptionalArrayData());
        
        return fObject;
    }
    public SFSObject getContinueMessage(String erro) {
        SFSObject fObject = new SFSObject();
        fObject.putInt(SFSKey.ACTION_INGAME, SFSAction.CONTINUE_GAME);
        fObject.putUtfString(SFSKey.TOAST_MESSAGE, erro);
        return fObject;
    }
    /**
     * Gửi cho tất cả user tròng bàn khi user rời khỏi game
     * @param userId
     * @return 
     */
    public SFSObject getLeaveRoomMessage(String userId) {
        SFSObject ojBoardInfo = new SFSObject();
        ojBoardInfo.putInt(SFSKey.ACTION_INGAME, SFSAction.LEAVE_GAME);
        ojBoardInfo.putUtfString(SFSKey.USER_ID,userId);
        return ojBoardInfo;
    }

    /**
     * Gửi cho tất cả user tròng bàn khi server leaveShuffle
     * @return 
     */
    public SFSObject getShuffleMessage() {
        SFSObject sfsObj = new SFSObject();
        sfsObj.putInt(SFSKey.ACTION_INGAME, SFSAction.SHUFFLE);
        return sfsObj;
    }

    /**
     * Thông tin user trong phòng
     * @param players
     * @return 
     */
    public SFSObject getJoinRoomMessage(List<User> players) {
        SFSObject boardInfor = new SFSObject();
        boardInfor.putInt(SFSKey.ACTION_INGAME, SFSAction.JOIN_BOARD);
        ISFSArray inforPlayers = new SFSArray();
        for (User user : players) {
            SFSObject fObjectPlayer = new SFSObject();
            fObjectPlayer.putDouble("stack", game.getMoneyFromUser(user).doubleValue());
            fObjectPlayer.putByte("seat", (byte) game.getSeatUser(user));
            fObjectPlayer.putBool("isWaiter", game.getStatusWaiter(user));
            fObjectPlayer.putBool("isInturn", game.isInturn(user));
            fObjectPlayer.putUtfString("idDBUser", Utils.getIdDBOfUser(user));
            fObjectPlayer.putUtfString("userName", game.getUserName(user));
            fObjectPlayer.putBool("isQuickPlay", game.isQuickPlayOfUser(user));
            fObjectPlayer.putUtfString("avatar", game.getUserAvatar(user));
            inforPlayers.addSFSObject(fObjectPlayer);
        }
        boardInfor.putSFSArray("array", inforPlayers);

        return boardInfor;
    }
    
    /**
     * Thông tin user mới vừa join vào phòng
     * @param userJoin
     * @return 
     */
    public SFSObject getAddPlayerMessage(User userJoin) {
        SFSObject boardInfor = new SFSObject();
        boardInfor.putInt(SFSKey.ACTION_INGAME, SFSAction.ADD_PLAYER);
        boardInfor.putDouble("stack", game.getMoneyFromUser(userJoin).doubleValue());
        boardInfor.putByte("seat", (byte) game.getSeatUser(userJoin));
        boardInfor.putBool("isWaiter", game.getStatusWaiter(userJoin));
        boardInfor.putUtfString("idDBUser", Utils.getIdDBOfUser(userJoin));
        boardInfor.putUtfString("userName", game.getUserName(userJoin));
        boardInfor.putBool("isQuickPlay", game.isQuickPlayOfUser(userJoin));
        boardInfor.putUtfString("avatar", game.getUserAvatar(userJoin));
        return boardInfor;
    }
    /**
     * Gửi câu thông báo trang thai user
     * @param user
     * @param key
     * @return 
     */
    public SFSObject getVariableUserMessage(User user, String key) {
        SFSObject variableUser = new SFSObject();
        variableUser.putInt(SFSKey.ACTION_INGAME, SFSAction.UPDATE_VARIABLE_USER);
        variableUser.putUtfString("idUser", Utils.getIdDBOfUser(user));
        variableUser.putUtfString("key", key);
        switch (key) {
            case UserInforPropertiesKey.IS_WAITER:
                variableUser.putBool(key, game.getStatusWaiter(user));
                break;
            case UserInforPropertiesKey.MONEY_STACK:
                variableUser.putDouble(key, game.getMoneyFromUser(user).doubleValue());
                break;
            case UserInforPropertiesKey.QUICK_PLAY:
                variableUser.putBool(key, game.isQuickPlayOfUser(user));
                break;
        }
        return variableUser;
    }
    /**
     * Gửi cho tất cả user tròng bàn khi user rời khỏi game
     * @return 
     */
    public SFSObject getNoMoneyMessage() {
        SFSObject ojBoardInfo = new SFSObject();
        ojBoardInfo.putInt(SFSKey.ACTION_INGAME, SFSAction.NO_MONEY);
        return ojBoardInfo;
    }
    /**
     * 
     * @param players
     * @param timePlaying
     * @param currentId
     * @return 
     */
    public SFSObject geStartGameViewerMessage(List<User> players, int timePlaying, String currentId) {
        SFSObject boardInfor = new SFSObject();
        boardInfor.putInt(SFSKey.ACTION_INGAME, SFSAction.START_GAME_VIEWER);
        boardInfor.putInt("timePlaying", timePlaying);
        boardInfor.putUtfString("currId", currentId);
        ISFSArray inforPlayers = new SFSArray();
        for (User user : players) {
            SFSObject fObjectPlayer = new SFSObject();
            fObjectPlayer.putUtfString("idUser", Utils.getIdDBOfUser(user));
            inforPlayers.addSFSObject(fObjectPlayer);
        }
        boardInfor.putSFSArray("array", inforPlayers);

        return boardInfor;
    }
    /**
     * gửi message thông báo đến user trong board
     *
     * @param infor
     * @param userId
     * @return
     */
    public  SFSObject getMessageInBoard(String infor, String userId) {
        SFSObject fObject = new SFSObject();
        fObject.putInt(SFSKey.ACTION_INGAME, SFSAction.MESSAGE_IN_BOARD);
        fObject.putUtfString("userId",userId);
        fObject.putUtfString("infor", infor);
        return fObject;
    }
    
    public SFSObject getRemainTimeInBoard(int time) {
        SFSObject fObject = new SFSObject();
        fObject.putInt(SFSKey.ACTION_INGAME, SFSAction.GET_REMAIN_TIME);
        fObject.putInt("time",time);
        return fObject;
    }
    
    public SFSObject getTournamnetInBoard(double bonusMoney, int serviceId, int countWins) {
        SFSObject fObject = new SFSObject();
        fObject.putInt(SFSKey.ACTION_INGAME, SFSAction.TOURNAMNET_INFOR);
        fObject.putDouble("bonusMoney",bonusMoney);
        fObject.putByte("countWins", (byte) countWins);
        return fObject;
    }
    
    /**
     * Thời gian xoay trúng thưởng
     *
     * @param rotateTime
     * @return
     */
    public SFSObject getRotateTimeInfoMessage(byte rotateTime) {
        SFSObject ob = new SFSObject();
        ob.putInt(SFSKey.ACTION_INGAME, SFSAction.ROTATE_TIME_TOURNAMENT);
        ob.putByte("timeRotate", rotateTime);
        return ob;
    }
    
    /**
     * Gửi về kết quả ván chơi cho user
     * @param winners
     * @param players
     * @param idWiner
     * @param bonusMoney
     * @return 
     */
    public SFSObject getResultTournament(Map<String, Byte> winners, List<User> players, String idWiner, double bonusMoney){
        SFSObject ob = new SFSObject();
        ob.putInt(SFSKey.ACTION_INGAME, SFSAction.RESULT_TOURNAMENT);
        ob.putUtfString("idWinner", idWiner);
        ob.putDouble("bonusMoney", bonusMoney);
        ISFSArray inforPlayers = new SFSArray();
        for (User user : players) {
            SFSObject fObjectPlayer = new SFSObject();
            String idDB = Utils.getIdDBOfUser(user);
            if(!winners.containsKey(idDB)){
                continue;
            }
            fObjectPlayer.putUtfString("idUser",Utils.getIdDBOfUser(user));
            fObjectPlayer.putByte("winTime",winners.get(idDB));
            inforPlayers.addSFSObject(fObjectPlayer);
        }
        ob.putSFSArray("array", inforPlayers);
        
        return ob;
    }
    
    /**
     * Trạng thái thắng thua của user
     *
     * @param userId
     * @param type
     * @return
     */
    public SFSObject getStatusUser(String userId, byte type) {
        SFSObject fObject = new SFSObject();
        fObject.putInt(SFSKey.ACTION_INGAME, SFSAction.STATUS_WIN_LOSE);
        fObject.putUtfString("userId", userId);
        fObject.putByte("type", type);
        return fObject;
    }
    
    public SFSObject getAutoBuyInMessage(boolean isAutoBuyIn) {
        SFSObject ob = new SFSObject();
        ob.putInt(SFSKey.ACTION_INGAME, SFSAction.AUTO_BUY_IN);
        ob.putBool("isAutoBuyIn", isAutoBuyIn);
        return ob;
    }
    
    public SFSObject getAutoLeaveMessage(boolean isAutoLeave) {
        SFSObject ob = new SFSObject();
        ob.putInt(SFSKey.ACTION_INGAME, SFSAction.AUTO_LEAVE_GAME);
        ob.putBool("isAutoLeave", isAutoLeave);
        return ob;
    }
}
