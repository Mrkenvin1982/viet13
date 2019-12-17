/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.vn.game.blackjack.message;

import com.smartfoxserver.v2.entities.User;
import com.smartfoxserver.v2.entities.data.ISFSArray;
import com.smartfoxserver.v2.entities.data.SFSArray;
import com.smartfoxserver.v2.entities.data.SFSObject;
import game.command.BlackJackCommand;
import game.command.SFSAction;
import game.key.SFSKey;
import game.vn.game.blackjack.BlackJackGameController;
import game.vn.game.blackjack.object.BlackJackPlayer;
import game.vn.util.Utils;
import java.util.List;
import java.util.Locale;

/**
 * Xử lý tất cả message của game black jack
 * @author tuanp
 */
public class MessageFactory {
    BlackJackGameController con;

    public MessageFactory(BlackJackGameController gameController) {
        this.con = gameController;
    }
    /**
     * Nhà cái xét bài user
     * @param player
     * @param userId
     * @return 
     */
    public SFSObject getProcessLatBaiMessage(BlackJackPlayer player, String userId){
        SFSObject fObject = new SFSObject();
        fObject.putInt(SFSKey.ACTION_INGAME, BlackJackCommand.OWNER_CHECK_CARD);
        fObject.putUtfString(SFSKey.USER_ID, userId);
        fObject.putShortArray(SFSKey.ARRAY_INFOR_CARD, player.card2List());
        fObject.putInt(SFSKey.POINT, player.getResult());
        fObject.putByte(SFSKey.TYPE, (byte) player.getStatus());

        return fObject;
    }
    /**
     * Tới lượt
     * @param idNextPlayer
     * @param playingTime
     * @return 
     */
    public SFSObject getNextTurnMessage(String idNextPlayer, int playingTime) {
        SFSObject fObject = new SFSObject();
        fObject.putInt(SFSKey.ACTION_INGAME, BlackJackCommand.TURN_AROUND_USER);
        fObject.putUtfString(SFSKey.USER_ID,idNextPlayer);
        fObject.putInt(SFSKey.OPEN_CARD_TIME, playingTime);
        return fObject;
    }
    /**
     * Rút bài
     * @param idUser
     * @param idCard
     * @return 
     */
    public SFSObject getProcessGetCardMessage(String idUser, byte idCard){
        SFSObject fObject = new SFSObject();
        fObject.putInt(SFSKey.ACTION_INGAME, BlackJackCommand.GET_CARD);
        fObject.putUtfString(SFSKey.USER_ID, idUser);
        fObject.putByte(SFSKey.INFOR_CARD, idCard);
        
        return fObject;
    }
    /**
     * Kết quả bài của player(xì dách,quắc,...)
     * @param player
     * @param idPlayer
     * @param strResult
     * @return 
     */
    public SFSObject getSendResultMessage(BlackJackPlayer player,String idPlayer, String strResult){
        SFSObject fObject = new SFSObject();
        fObject.putInt(SFSKey.ACTION_INGAME, SFSAction.RESULT);
        fObject.putUtfString(SFSKey.USER_ID, idPlayer);
        fObject.putUtfString(SFSKey.STRING_MESSAGE, strResult);

        return fObject;
    }
    /**
     * gửi message start game
     * @param player
     * @param idPlayer
     * @param playingTime
     * @param userIds
     * @return 
     */
    public SFSObject getStartGameMessage(BlackJackPlayer player,String idPlayer, int playingTime, List<String> userIds){
        SFSObject fObject = new SFSObject();
        fObject.putInt(SFSKey.ACTION_INGAME, SFSAction.START_GAME);
        fObject.putUtfString(SFSKey.USER_ID, idPlayer);
        fObject.putInt(SFSKey.OPEN_CARD_TIME, playingTime);
        fObject.putUtfStringArray(SFSKey.ARRAY_SFS, userIds);
        fObject.putShortArray(SFSKey.ARRAY_INFOR_CARD, player.card2List());

        return fObject;
    }
    /**
     * message stop game
     * @param players
     * @param lo
     * @return 
     */
    public SFSObject getStopGameMessage(BlackJackPlayer [] players, Locale lo){
        SFSObject fObject = new SFSObject();
        ISFSArray inforPlayers = new SFSArray();
        fObject.putInt(SFSKey.ACTION_INGAME, SFSAction.STOP_GAME);
        for (int i=0;i< players.length;i++) {
            User user= this.con.getUser(i);
            if(user==null){
                continue;
            }
            if (players[i].isPlaying()) {
                SFSObject fObjectPlayer = new SFSObject();
                fObjectPlayer.putUtfString(SFSKey.USER_ID,Utils.getIdDBOfUser(user));
                fObjectPlayer.putByte(SFSKey.TYPE, (byte)players[i].getStatus());
                fObjectPlayer.putUtfString(SFSKey.STRING_MESSAGE, players[i].getResultString(lo, players[i].getResult()));
                fObjectPlayer.putShortArray(SFSKey.ARRAY_INFOR_CARD, players[i].card2List());
                inforPlayers.addSFSObject(fObjectPlayer);
            }
        }
        fObject.putSFSArray(SFSKey.ARRAY_SFS,inforPlayers);
        return fObject;
     }
    
    /**
     * Thông tin khi user reconnect trở lại ván
     * @param seatPlayerReturn
     * @param locale
     * @return 
     */
    public SFSObject getOnReturnMessage(int seatPlayerReturn, Locale locale) {
        SFSObject fObject = new SFSObject();
        fObject.putInt(SFSKey.ACTION_INGAME, SFSAction.ON_RETURN_GAME);
        ISFSArray inforPlayers = new SFSArray();

        BlackJackPlayer playerReturn = this.con.getPlayers()[seatPlayerReturn];
        //thời gian playing còn lại
        fObject.putInt(SFSKey.OPEN_CARD_TIME, (int) this.con.getTimeRemain());
        fObject.putUtfString(SFSKey.USER_ID, this.con.getIdDBOfUser(this.con.getCurrentPlayer()));
        fObject.putShortArray(SFSKey.ARRAY_INFOR_CARD, playerReturn.card2List());
        fObject.putUtfString(SFSKey.STRING_MESSAGE, playerReturn.getResultString(locale, playerReturn.getResult()));
        fObject.putByte(SFSKey.TYPE, (byte) this.con.getPlayers()[seatPlayerReturn].getStatus());

        for (int i = 0; i < this.con.getPlayers().length; i++) {
            User user = this.con.getUser(i);
            if (user == null) {
                continue;
            }
            if(seatPlayerReturn==i){
                continue;
            }
            String idDB = this.con.getIdDBOfUser(user);
            BlackJackPlayer blackJackPlayer = this.con.getPlayers()[i];
            SFSObject fObjectPlayer = new SFSObject();
            fObjectPlayer.putUtfString(SFSKey.USER_ID, idDB);
            fObjectPlayer.putByte(SFSKey.TYPE, (byte) blackJackPlayer.getStatus());
            fObjectPlayer.putDouble(SFSKey.MONEY_BET, this.con.getBettingMoney(idDB).doubleValue());

            if (!blackJackPlayer.isPlaying()) {
                fObjectPlayer.putShortArray(SFSKey.ARRAY_INFOR_CARD, blackJackPlayer.card2List());
                fObjectPlayer.putUtfString(SFSKey.STRING_MESSAGE, blackJackPlayer.getResultString(locale, blackJackPlayer.getResult()));
            } else {
                fObjectPlayer.putShortArray(SFSKey.ARRAY_INFOR_CARD, blackJackPlayer.card2ListHide());
                fObjectPlayer.putUtfString(SFSKey.STRING_MESSAGE, "");
            }
            inforPlayers.addSFSObject(fObjectPlayer);
        }
        fObject.putSFSArray(SFSKey.ARRAY_SFS, inforPlayers);
        return fObject;
    }
    /**
     * Gưi thong tin card tất cả user trong bàn khi
     * user join bàn trong lúc ván đang chơi
     * @param playerReturn
     * @return 
     */
    public SFSObject getPlayingMessage(User playerReturn) {
        SFSObject fObject = new SFSObject();
        fObject.putInt(SFSKey.ACTION_INGAME, SFSAction.PLAYING);
        fObject.putUtfString("currId", this.con.getIdDBOfUser(this.con.getCurrentPlayer()));
        fObject.putInt("timeRemain", this.con.getTimeRemain());
        ISFSArray inforPlayers = new SFSArray();
         for (int i=0;i<this.con.getPlayers().length;i++) {
            User  user= this.con.getUser(i);
            if (user == null) {
                continue;
            }
            if(Utils.isEqual(playerReturn, user) || !this.con.isInturn(user)){
                continue;
            }
            SFSObject fObjectPlayer = new SFSObject();   
            fObjectPlayer.putUtfString(SFSKey.USER_ID, this.con.getIdDBOfUser(user));
            fObjectPlayer.putShortArray(SFSKey.ARRAY_INFOR_CARD, this.con.getPlayers()[i].card2ListHide());
            inforPlayers.addSFSObject(fObjectPlayer);
         }
        fObject.putSFSArray(SFSKey.ARRAY_SFS,inforPlayers);
        return fObject;
    }
    /**
     * User đặt cược
     * @param u
     * @param money
     * @return 
     */
    public SFSObject getBetMoneyMessage(User u, double money) {
        SFSObject ojBoardInfo = new SFSObject();
        ojBoardInfo.putInt(SFSKey.ACTION_INGAME, SFSAction.BET);
        ojBoardInfo.putDouble(SFSKey.MONEY_BET, money);
        ojBoardInfo.putUtfString(SFSKey.USER_ID, this.con.getIdDBOfUser(u));
        return ojBoardInfo;
    }
       
}
