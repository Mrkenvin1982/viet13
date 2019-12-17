/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.vn.game.baicao.message;

import com.smartfoxserver.v2.entities.User;
import com.smartfoxserver.v2.entities.data.ISFSArray;
import com.smartfoxserver.v2.entities.data.SFSArray;
import com.smartfoxserver.v2.entities.data.SFSObject;
import game.command.SFSAction;
import game.key.SFSKey;
import game.vn.game.baicao.BaiCaoController;
import game.vn.game.baicao.lang.BaiCaoLanguage;
import game.vn.game.baicao.object.BaiCaoPlayer;
import game.vn.util.GlobalsUtil;
import game.vn.util.Utils;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Class này xử lý tất cả các message của game bài cào
 *
 * @author tuanp
 */
public class MessageFactory {

    BaiCaoController con;

    public MessageFactory(BaiCaoController gameController) {
        this.con = gameController;
    }

    /**
     * gửi về mức tiền đặt cược của user trong game
     *
     * @param u
     * @param money
     * @return
     */
    public SFSObject getBetMoneyMessage(User u, double money) {
        SFSObject ojBoardInfo = new SFSObject();
        ojBoardInfo.putInt(SFSKey.ACTION_INGAME, SFSAction.BET);
        ojBoardInfo.putDouble(SFSKey.MONEY_BET, money);
        ojBoardInfo.putUtfString(SFSKey.USER_ID, con.getIdDBOfUser(u));
        return ojBoardInfo;
    }

    /**
     * Gửi về kết quả của user trong ván
     *
     * @param result
     * @param userId
     * @return
     */
    public SFSObject getResultMesssage(int result, String userId) {
        SFSObject fob = new SFSObject();
        fob.putInt(SFSKey.ACTION_INGAME, SFSAction.RESULT);
        fob.putUtfString(SFSKey.USER_ID, userId);
        if (result > 9) {
            fob.putUtfString(SFSKey.STRING_MESSAGE_VI, BaiCaoLanguage.getMessage(BaiCaoLanguage.THREE_FACES, GlobalsUtil.VIETNAMESE_LOCALE));
            fob.putUtfString(SFSKey.STRING_MESSAGE_EN, BaiCaoLanguage.getMessage(BaiCaoLanguage.THREE_FACES, GlobalsUtil.ENGLISH_LOCALE));
            fob.putUtfString(SFSKey.STRING_MESSAGE_ZH, BaiCaoLanguage.getMessage(BaiCaoLanguage.THREE_FACES, GlobalsUtil.CHINESE_LOCALE));
        } else if (result == 0) {
            fob.putUtfString(SFSKey.STRING_MESSAGE_VI, BaiCaoLanguage.getMessage(BaiCaoLanguage.ZERO_POINT, GlobalsUtil.VIETNAMESE_LOCALE));
            fob.putUtfString(SFSKey.STRING_MESSAGE_EN, BaiCaoLanguage.getMessage(BaiCaoLanguage.ZERO_POINT, GlobalsUtil.ENGLISH_LOCALE));
            fob.putUtfString(SFSKey.STRING_MESSAGE_ZH, BaiCaoLanguage.getMessage(BaiCaoLanguage.ZERO_POINT, GlobalsUtil.CHINESE_LOCALE));
        } else {
            fob.putUtfString(SFSKey.STRING_MESSAGE_VI, "" + result + " " + BaiCaoLanguage.getMessage(BaiCaoLanguage.POINT, GlobalsUtil.VIETNAMESE_LOCALE));
            fob.putUtfString(SFSKey.STRING_MESSAGE_EN, "" + result + " " + BaiCaoLanguage.getMessage(BaiCaoLanguage.POINT, GlobalsUtil.ENGLISH_LOCALE));
            fob.putUtfString(SFSKey.STRING_MESSAGE_ZH, "" + result + " " + BaiCaoLanguage.getMessage(BaiCaoLanguage.POINT, GlobalsUtil.CHINESE_LOCALE));
        }
        return fob;
    }

    /**
     *
     * @param playingTime: thời gian playing 1 ván
     * @param arrayCard: danh sách bài của user
     * @param userIds
     * @return
     */
    public SFSObject getStartGameMessage(int playingTime, List<Short> arrayCard,List<String> userIds) {
        SFSObject fObject = new SFSObject();
        fObject.putInt(SFSKey.ACTION_INGAME, SFSAction.START_GAME);
        fObject.putInt(SFSKey.OPEN_CARD_TIME, playingTime);
        fObject.putUtfStringArray(SFSKey.ARRAY_SFS, userIds);
        fObject.putShortArray(SFSKey.ARRAY_INFOR_CARD, arrayCard);
        return fObject;
    }

    /**
     * Joiner nhận về thông tin tất cả user trong bàn khi bàn playing
     *
     * @param players
     * @param joiner
     * @return
     */
    public SFSObject getPlayingMessage(BaiCaoPlayer[] players, User joiner) {
        //goi thong tin cua no cho tat ca
        SFSObject fObject = new SFSObject();
        fObject.putInt(SFSKey.ACTION_INGAME, SFSAction.PLAYING);
        fObject.putInt("timeRemain", this.con.getTimeRemain());
        ISFSArray inforPlayers = new SFSArray();
        for (int i=0;i<players.length;i++) {
             User user= this.con.getUser(i);
            if(user==null){
                 continue;
            }
            if (Utils.isEqual(user, joiner)) {
                continue;
            }
            if (players[i].isPlaying()) {
                SFSObject fObjectPlayer = new SFSObject();
                fObjectPlayer.putUtfString(SFSKey.USER_ID,con.getIdDBOfUser(user));
                List<Short> arr = new ArrayList<Short>();
                short valueInit = -1;
                arr.add(valueInit);
                arr.add(valueInit);
                arr.add(valueInit);
                fObjectPlayer.putShortArray(SFSKey.ARRAY_INFOR_CARD, arr);
                inforPlayers.addSFSObject(fObjectPlayer);
            }
        }
        fObject.putSFSArray(SFSKey.ARRAY_SFS,inforPlayers);
        return fObject;
    }

    /**
     * Gửi về danh sách bài được mở
     *
     * @param userId
     * @param arrayCards
     * @return
     */
    public SFSObject getOpenCardsMessage(String userId, List<Short> arrayCards) {
        SFSObject fob = new SFSObject();
        fob.putInt(SFSKey.ACTION_INGAME, SFSAction.MOVE);
        fob.putUtfString(SFSKey.USER_ID, userId);
        fob.putShortArray(SFSKey.ARRAY_INFOR_CARD, arrayCards);
        return fob;
    }

    /**
     * Gửi thông tin message stop game
     *
     * @param players
     * @return
     */
    public SFSObject getStopGameMessage(BaiCaoPlayer[] players) {
        SFSObject fObject = new SFSObject();
        ISFSArray inforPlayers = new SFSArray();
        fObject.putInt(SFSKey.ACTION_INGAME, SFSAction.STOP_GAME);
        for (int i=0;i<players.length;i++) {
             User user= this.con.getUser(i);
             if(user==null){
                 continue;
             }
            if (players[i].isPlaying()) {
                SFSObject fObjectPlayer = new SFSObject();
                fObjectPlayer.putUtfString(SFSKey.USER_ID, con.getIdDBOfUser(user));
                fObjectPlayer.putShortArray(SFSKey.ARRAY_INFOR_CARD, players[i].card2List());
                fObjectPlayer.putDouble(SFSKey.MONEY_BONUS,players[i].getMoneyWinLose().doubleValue() );
                inforPlayers.addSFSObject(fObjectPlayer);
            }
        }
        fObject.putSFSArray(SFSKey.ARRAY_SFS,inforPlayers);
        return fObject;
    }
        /**
     * Lấy ra kết quả bài cào kiểu String
     * @param result
     * @param locale
     * @return 
     */
    private String getResultString(int result, Locale locale) {
        if (result > 9) {
            return BaiCaoLanguage.getMessage(BaiCaoLanguage.THREE_FACES, locale);
        } else if (result == 0) {
            return BaiCaoLanguage.getMessage(BaiCaoLanguage.ZERO_POINT, locale);
        } else {
            return result + " " +BaiCaoLanguage.getMessage(BaiCaoLanguage.POINT, locale);
        }
    }
    /**
     * Thông tin bàn đang chơi khi user reconnect
     * @param players 
     * @param locale 
     * @return  
     */
    public SFSObject getMessageReturnMessage(BaiCaoPlayer[] players, Locale locale) {
        SFSObject fObject = new SFSObject();
        fObject.putInt(SFSKey.ACTION_INGAME, SFSAction.ON_RETURN_GAME);
        ISFSArray inforPlayers = new SFSArray();

        //thời gian playing còn lại
        fObject.putInt("oct", (int) this.con.getTimeRemain());

        for (int i = 0; i < players.length; i++) {
            User user = this.con.getUser(i);
            if (user == null) {
                continue;
            }
            if (players[i].isPlaying()) {
                String idDBUser = this.con.getIdDBOfUser(user);
                SFSObject fObjectPlayer = new SFSObject();

                fObjectPlayer.putUtfString("idUser", idDBUser);
                fObjectPlayer.putShortArray("arrCards", players[i].card2List());

                double moneyBet = this.con.getBettingMoney(idDBUser).doubleValue();
                fObjectPlayer.putDouble("moneyb", moneyBet);

                fObjectPlayer.putShortArray("arrCardsShow",players[i].getListShowOnReturn());

                String inforResult = getResultString(players[i].getResult(), locale);
                fObjectPlayer.putUtfString("infor", inforResult);

                inforPlayers.addSFSObject(fObjectPlayer);
            }
        }
        fObject.putSFSArray(SFSKey.ARRAY_SFS,inforPlayers);
        return fObject;
    }
    
}
