/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.vn.game.lieng.message;

import com.smartfoxserver.v2.entities.User;
import com.smartfoxserver.v2.entities.data.ISFSArray;
import com.smartfoxserver.v2.entities.data.SFSArray;
import com.smartfoxserver.v2.entities.data.SFSObject;
import game.command.LiengCommand;
import game.command.SFSAction;
import game.key.SFSKey;
import game.vn.game.lieng.LiengController;
import game.vn.game.lieng.lang.LiengLanguage;
import game.vn.game.lieng.object.LiengPlayer;
import game.vn.game.lieng.utils.BettingUtil;
import game.vn.util.Utils;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 *
 * @author tuanp
 */
public class PlayerMessageFactory{
    private LiengPlayer player;
    LiengController game;
    public PlayerMessageFactory(LiengPlayer player){
        this.game=player.getGame();
        this.player=player;
    }

    /**
     *  ván mới bắt đầu gửi message này về cho mỗi người chơi 3 lá
     * @return 
     */
    public SFSObject getDealCardMessage(){
        SFSObject ob= new SFSObject();
        ob.putInt(SFSKey.ACTION_INGAME, LiengCommand.DEAL_CARD);
        SFSArray listPlayers = new SFSArray();
        String idDBPlayer=game.getIdDBOfUser(this.player.getUser());
        for (User user : game.getPlayingPlayers()) {
           SFSObject obPlayer = new SFSObject();
            String idDBUser=game.getIdDBOfUser(user);
            LiengPlayer p = game.getLiengPlayer(idDBPlayer);
            if(p==null){
                continue;
            }
            obPlayer.putUtfString(SFSKey.USER_ID, idDBUser);
            if (!idDBUser.equals(idDBPlayer)) {
                obPlayer.putUtfString(SFSKey.STRING_MESSAGE, "");
                obPlayer.putShortArray(SFSKey.ARRAY_INFOR_CARD, p.getCardsHideToList());
            }else{
                obPlayer.putUtfString(SFSKey.STRING_MESSAGE,this.player.getResultCard().getStrValue());
                obPlayer.putShortArray(SFSKey.ARRAY_INFOR_CARD, p.getCardsToList());
            }
            listPlayers.addSFSObject(obPlayer);
        }
        ob.putSFSArray(SFSKey.ARRAY_SFS, listPlayers);
        return ob;
    }
    /**
     * next turn
     * @return 
     */
    public SFSObject getNextTurnMessage(){
        SFSObject ob = new SFSObject();
        ob.putInt(SFSKey.ACTION_INGAME, LiengCommand.NEXT_TURN);
        ob.putUtfString(SFSKey.USER_ID, this.game.getIdDBOfUser(player.getUser()));
        ob.putInt(LiengCommand.TIME_LIMIT, game.getPlayingTime() / 1000);
        ob.putShortArray(LiengCommand.ARRAY_ACTION, this.player.getActionsToList());
        if (player.getActions().contains(LiengPlayer.BET)) {
            List<BigDecimal> list = BettingUtil.getListChips(game.getRemainMoneyToBet(player), game.getMoney());
            List<Double> listDouble = new ArrayList<>();
            for(BigDecimal value : list){
                listDouble.add(value.doubleValue());
            }
            ob.putDoubleArray(LiengCommand.ARRAY_BET, listDouble);
        } else if (player.getActions().contains(LiengPlayer.CALL)) {
            BigDecimal callMoney =Utils.subtract(game.getLastBet(),player.getBetMoney());
            List<Double> list = new ArrayList<>();
            list.add(callMoney.doubleValue());
            ob.putDoubleArray(LiengCommand.ARRAY_BET, list);
        }else{
            ob.putDoubleArray(LiengCommand.ARRAY_BET, new ArrayList<>());
        }
        return ob;
    }
    /**
     * Bet
     * @param locale
     * @param callMoney
     * @param betMoney
     * @return 
     */
    public SFSObject getBetMessage(Locale locale, double callMoney, double betMoney){
        String desc = String.format(LiengLanguage.getMessage(LiengLanguage.BET, locale),
                Utils.formatNumber(callMoney),Utils.formatNumber(betMoney));
        SFSObject ob = new SFSObject();
        ob.putInt(SFSKey.ACTION_INGAME, LiengCommand.BET);
        ob.putUtfString(SFSKey.USER_ID, this.game.getIdDBOfUser(player.getUser()));
        ob.putUtfString(SFSKey.STRING_MESSAGE, desc);
        ob.putDouble(LiengCommand.BET_MONEY, betMoney);
        ob.putDouble(LiengCommand.CALL_MONEY, callMoney);
        return ob;
    }
    /**
     * Call
     * @param locale
     * @param callMoney
     * @return 
     */
    public SFSObject getCallMessage(Locale locale, double callMoney){
        String desc = LiengLanguage.getMessage(LiengLanguage.CALL, locale);
        SFSObject ob = new SFSObject();
        ob.putInt(SFSKey.ACTION_INGAME, LiengCommand.CALL);
        ob.putUtfString(SFSKey.USER_ID, this.game.getIdDBOfUser(player.getUser()));
        ob.putUtfString(SFSKey.STRING_MESSAGE, desc);
        ob.putDouble(LiengCommand.CALL_MONEY, callMoney);
        return ob;
    }
    /**
     * Fold
     * @param locale
     * @return 
     */
    public SFSObject getFoldMessage(Locale locale){
        String desc = LiengLanguage.getMessage(LiengLanguage.FOLD, locale);
        SFSObject ob = new SFSObject();
        ob.putInt(SFSKey.ACTION_INGAME, LiengCommand.FOLD);
        ob.putUtfString(SFSKey.USER_ID, this.game.getIdDBOfUser(player.getUser()));
        ob.putUtfString(SFSKey.STRING_MESSAGE, desc);
        return ob;
    }
    /**
     * Check
     * @param locale
     * @return 
     */
    public SFSObject getCheckMessage(Locale locale){
        String desc = LiengLanguage.getMessage(LiengLanguage.CHECK, locale);
        SFSObject ob = new SFSObject();
        ob.putInt(SFSKey.ACTION_INGAME, LiengCommand.CHECK);
        ob.putUtfString(SFSKey.USER_ID,this.game.getIdDBOfUser(player.getUser()));
        ob.putUtfString(SFSKey.STRING_MESSAGE, desc);
        return ob;
    }
    /**
     * Trả về hành động cuối cùng(bet,fold,raise,...) của user trong bàn,
     * sử dụng cho mục đích reconect
     * @return 
     */
    public SFSObject getLastActionInfoMessage(){
        SFSObject ob = new SFSObject();
        ob.putInt(SFSKey.ACTION_INGAME, LiengCommand.LAST_ACTION);
        List<User> list = game.getPlayerInturnAndAllinFold();
        SFSArray listPlayers = new SFSArray();
        for (User user : list) {
           SFSObject obPlayer = new SFSObject();
           String idDBUser = game.getIdDBOfUser(user);
            LiengPlayer p = game.getLiengPlayer(idDBUser);
            if(p==null){
                continue;
            }
            BigDecimal moneyBet=Utils.subtract(p.getBetMoney(),game.getMoney());
            obPlayer.putUtfString(SFSKey.USER_ID, idDBUser);
            obPlayer.putDouble(LiengCommand.BET_MONEY, moneyBet.doubleValue());
            obPlayer.putByte(LiengCommand.ACTION, (byte) p.getLastAction());
            listPlayers.addSFSObject(obPlayer);
        }
        ob.putSFSArray(SFSKey.ARRAY_SFS, listPlayers);
        return ob;
    }
    /**
     * gửi message return game cho user
     * @return 
     */
    public SFSObject getReturnGameMessage() {
        SFSObject fObject = new SFSObject();
        fObject.putInt(SFSKey.ACTION_INGAME, SFSAction.ON_RETURN_GAME);
        ISFSArray inforPlayers = new SFSArray();
        //roundSize - 1 vì vòng đặt cược đầu tiên ko hiển thị ra cho user thấy
        fObject.putByte("roundSize",(byte) (game.getRounds().size()-1));
        fObject.putDouble("totalBet",game.getTotalBetMoney().doubleValue());
        fObject.putUtfString("idCurrent", game.getIdDBOfUser(game.getCurrentPlayer()));
        fObject.putInt("timeRemain", game.getTimeRemain());
        fObject.putDouble("totalBetUser",player.getBetMoney().doubleValue());
        fObject.putByte("lastAction",(byte) player.getLastAction());
        fObject.putShortArray("arrayCards", player.getCardsToList());
        fObject.putBool("isFolded", player.isFolded());
        if(!player.isFolded()){
            fObject.putUtfString("strValue", player.getResultCard().getStrValue());
            fObject.putShortArray("arrAction", player.getActionsToList());
            boolean isCanBet = player.getActions().contains(LiengPlayer.BET);
            fObject.putBool("isCanBet", isCanBet);
            if(isCanBet){
                 List<BigDecimal> listChips = BettingUtil.getListChips(game.getRemainMoneyToBet(player), game.getMoney());
                 List<Double> listDouble = new ArrayList<>();
                for (BigDecimal value : listChips) {
                    listDouble.add(value.doubleValue());
                }
                 fObject.putDoubleArray("listChips", listDouble);
            }
            
        }else{
            //ván ko chơi thì lấy thời gian bắt đầu ván tiếp theo
             fObject.putInt("timeStart",game.getTimeToStart());
        }
        // gửi thông tin của người chơi khác trong bàn
        for (User user : game.getPlayingPlayers()) {
            String idDBUser = game.getIdDBOfUser(user);
            LiengPlayer p = game.getLiengPlayer(idDBUser);
            if (p == null || Utils.isEqual(user, player.getUser())) {
                continue;
            }
            SFSObject obPlayer = new SFSObject();
            obPlayer.putUtfString("userId",idDBUser);
            obPlayer.putDouble("money", game.getMoneyFromUser(user).doubleValue());
            obPlayer.putDouble("totalBetUser",p.getBetMoney().doubleValue());
            obPlayer.putByte("lastAction",(byte) p.getLastAction());
            obPlayer.putBool("isFolded", p.isFolded());
            obPlayer.putUtfString("actionDesc", p.getLastActionDesc());
            obPlayer.putShortArray("hideCards", p.getCardsHideToList());
            inforPlayers.addSFSObject(obPlayer);
        }
        fObject.putSFSArray(SFSKey.ARRAY_SFS, inforPlayers);
        /**
         * nếu call thì gửi về thêm số tiền user call: sử dung để hiển thị trên
         * button call
         *
         */
        if (player.getActions().contains(LiengPlayer.CALL)) {
            BigDecimal callMoney =Utils.subtract(game.getLastBet(), player.getBetMoney());
            fObject.putDouble("callMoney", callMoney.doubleValue());
        }
        return fObject;
    }
}
