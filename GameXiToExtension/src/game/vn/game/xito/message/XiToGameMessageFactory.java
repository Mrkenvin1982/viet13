/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.vn.game.xito.message;

import com.smartfoxserver.v2.entities.User;
import com.smartfoxserver.v2.entities.data.SFSArray;
import com.smartfoxserver.v2.entities.data.SFSObject;
import game.command.SFSAction;
import game.command.XiToCommand;
import game.key.SFSKey;
import game.vn.game.xito.XiToConfig;
import game.vn.game.xito.XiToController;
import game.vn.game.xito.lang.XiToLanguage;
import game.vn.game.xito.object.ResultCard;
import game.vn.game.xito.object.Round;
import game.vn.game.xito.object.XiToCardUtil;
import game.vn.game.xito.object.XiToPlayer;
import game.vn.util.Utils;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Các message trong game xì tố
 * @author tuanp
 */
public class XiToGameMessageFactory {
    private final XiToController xiToGame;
    public XiToGameMessageFactory(XiToController game){
        this.xiToGame=game;
    }
    /**
     * Message start game
     * @return 
     */
    public SFSObject getStartGameMessage(){
        SFSObject fObject= new SFSObject();
        fObject.putInt(SFSKey.ACTION_INGAME, SFSAction.START_GAME);
        fObject.putInt(XiToCommand.PRE_FLOG_TIME,XiToConfig.getInstance().getPreFlogTime());
        return fObject;
    }
    /**
     * gui kết quả bài của user sau khi start ván
     * @param p
     * @return 
     */
    public SFSObject getPreFlopMessage(XiToPlayer p){
         SFSObject fObject= new SFSObject();
         fObject.putInt(SFSKey.ACTION_INGAME, XiToCommand.PRE_FLOP);
         fObject.putShortArray(SFSKey.ARRAY_INFOR_CARD,p.getHoldCardsIdToList());
         return fObject;
    }
    
    /**
     * Gửi ve cmd prefor=-1 cho user sit out de fix bug client
     * @param p
     * @return 
     */
    public SFSObject getPreFlopForSitOutMessage(XiToPlayer p){
         SFSObject fObject= new SFSObject();
         fObject.putInt(SFSKey.ACTION_INGAME, XiToCommand.PRE_FLOP);
         fObject.putShortArray(SFSKey.ARRAY_INFOR_CARD,p.getHidePreflogCardsIdToList());
         return fObject;
    }
    
    /**
     * chia mỗi người một lá
     * @param map
     * @return 
     */
    public SFSObject getFlopMessage(Map<String, Byte> map){
        SFSObject fObject= new SFSObject();
        fObject.putInt(SFSKey.ACTION_INGAME, XiToCommand.FLOP);
        SFSArray cards= new SFSArray();
        for(String id: map.keySet()){
            SFSObject ob= new SFSObject();
            ob.putUtfString(SFSKey.USER_ID, id);
            ob.putByte(SFSKey.INFOR_CARD, map.get(id));
            cards.addSFSObject(ob);
        }
        fObject.putSFSArray(SFSKey.ARRAY_SFS, cards);
        return fObject;
    }
    
    /**
     * Gui message show card cho ca ban ket thuc vong preFlog
     * @return 
     */
    public SFSObject getShowOneCardMessage() {
        SFSObject fObject = new SFSObject();
        fObject.putInt(SFSKey.ACTION_INGAME, XiToCommand.SHOW_ONE_CARD);
        SFSArray cards = new SFSArray();
        for (User user : this.xiToGame.getInTurnPlayers()) {
            String idDBUser=this.xiToGame.getIdDBOfUser(user);
            XiToPlayer xiToPlayer = this.xiToGame.getXiToPlayer(idDBUser);
            if (xiToPlayer == null) {
                continue;
            }
            SFSObject ob = new SFSObject();
            ob.putUtfString(SFSKey.USER_ID,idDBUser);
            ob.putByte(SFSKey.INFOR_CARD, xiToPlayer.getShowedCard().getId());
            cards.addSFSObject(ob);
        }
        fObject.putSFSArray(SFSKey.ARRAY_SFS, cards);
        return fObject;
    }
    
    public SFSObject getUpdatePotMessage(){
        SFSObject fObject = new SFSObject();
        fObject.putInt(SFSKey.ACTION_INGAME, XiToCommand.UPDATE_POT);
        fObject.putDouble(XiToCommand.TOTAL_BET_STACK, this.xiToGame.getTotalBetStack().doubleValue());
        return fObject;
    }
    /**
     * Gui ve ket qua cua tung user
     * @param locale
     * @return 
     */
    public SFSObject getResultMessage(Locale locale){
        SFSObject fObject = new SFSObject();
        fObject.putInt(SFSKey.ACTION_INGAME, XiToCommand.RESULT);
        fObject.putUtfString(SFSKey.USER_ID,xiToGame.getIdDBOfUser(xiToGame.getWinner()));
        //nếu tất cả đã up bài thì ko cho thấy bài thằng thắng
        boolean isAllFolded = xiToGame.isAllFolded();
        List<User> list=this.xiToGame.getPlayers();
        Iterator<User> iter = list.iterator();
        //loại thằng in turn = false và ko all in, tức là những thằng mới vào khi bàn đang chơi
        while (iter.hasNext()) {
            User player = iter.next();
            if (!this.xiToGame.isInturn(player)) {
                String idDBUser=this.xiToGame.getIdDBOfUser(player);
                XiToPlayer xiToPlayer = xiToGame.getXiToPlayer(idDBUser);
                if (xiToPlayer != null && !xiToPlayer.isAllIn()) {
                    iter.remove();
                }
            }
        }
        SFSArray listPlayers= new SFSArray();
        for (User player : list) {
            String idDBUser=this.xiToGame.getIdDBOfUser(player);
            XiToPlayer xitoPlayer = xiToGame.getXiToPlayer(idDBUser);
            if (xiToGame.isInturn(player) || xitoPlayer.isAllIn()) {
                SFSObject obPlayer= new SFSObject();
                obPlayer.putUtfString(SFSKey.USER_ID, idDBUser);
                obPlayer.putDouble(XiToCommand.WIN_STACK, xitoPlayer.getWinStack().doubleValue());
                
                ResultCard result = XiToCardUtil.evalCards(xitoPlayer.getHoldCards().values());
                obPlayer.putByte(XiToCommand.RESULT_CARDS_VALUE, result.getValue());
                if(isAllFolded && Utils.isEqual(xitoPlayer.getUser(), xiToGame.getWinner())){
                    obPlayer.putShortArray(SFSKey.ARRAY_INFOR_CARD, new ArrayList<>());
                    obPlayer.putUtfString(SFSKey.STRING_MESSAGE, "");
                }else{
                    obPlayer.putShortArray(SFSKey.ARRAY_INFOR_CARD, xitoPlayer.getHoldCardsIdToList());
                    obPlayer.putUtfString(SFSKey.STRING_MESSAGE, XiToLanguage.getMessage(result.getStrValue(), locale));
                }
                listPlayers.addSFSObject(obPlayer);  
            }
        }
        fObject.putSFSArray(SFSKey.ARRAY_SFS, listPlayers);
        return fObject;
    }
    /**
     * Gửi message command mua tẩy
     * @param money
     * @param buyStack
     * @param min
     * @param max
     * @param user
     * @return 
     */
    public SFSObject getBuyStackMessage(double money,String buyStack,double min, double max,User user){
        SFSObject fObject = new SFSObject();
        fObject.putInt(SFSKey.ACTION_INGAME, XiToCommand.BUY_STACK);
        fObject.putUtfString(SFSKey.STRING_MESSAGE,buyStack);
        fObject.putDouble(XiToCommand.BUY_STACK_KEY, money);
        List<User> list = xiToGame.getPlayers();
        SFSArray listPlayers= new SFSArray();
        for(User u: list){
            SFSObject obPlayer= new SFSObject();
            String idDBUser=this.xiToGame.getIdDBOfUser(u);
            XiToPlayer xitoPlayer = xiToGame.getXiToPlayer(idDBUser);
            obPlayer.putUtfString(SFSKey.USER_ID, idDBUser);
            boolean isPreFlog = xiToGame.isPreFlog();
            if(isPreFlog){
                obPlayer.putShortArray(SFSKey.ARRAY_INFOR_CARD, xitoPlayer.getHidePreflogCardsIdToList());
            }else{
                obPlayer.putShortArray(SFSKey.ARRAY_INFOR_CARD,xitoPlayer.getHoldCardsCheckedToList());
            }
            listPlayers.addSFSObject(obPlayer);
        }
        fObject.putSFSArray(SFSKey.ARRAY_SFS, listPlayers);
        return fObject;
    }
    /**
     *  Khi game start, gửi message này cho viewer
     * @return 
     */
    public SFSObject getViewerMessage(){
        SFSObject fObject = new SFSObject();
        fObject.putInt(SFSKey.ACTION_INGAME, XiToCommand.VIEWER);
        List<User> list =xiToGame.getPlayers();
        Iterator<User> iter = list.iterator();
        //loại thằng in turn = false và ko all in, tức là những thằng mới vào khi bàn đang chơi
        while (iter.hasNext()) {
            User player = iter.next();
            if (!this.xiToGame.isInturn(player)) {
                String idDBUser=this.xiToGame.getIdDBOfUser(player);
                XiToPlayer xiToPlayer = xiToGame.getXiToPlayer(idDBUser);
                if (xiToPlayer != null && !xiToPlayer.isAllIn()) {
                    iter.remove();
                }
            }
        }
        SFSArray listPlayers= new SFSArray();
        for(User u: list){
            SFSObject obPlayer = new SFSObject();
            String idDBUser=this.xiToGame.getIdDBOfUser(u);
            XiToPlayer xiToPlayer = xiToGame.getXiToPlayer(idDBUser);
            obPlayer.putUtfString(SFSKey.USER_ID, idDBUser);
            boolean isPlaying = false;
            if (!this.xiToGame.isInturn(u) && !xiToPlayer.isAllIn()) {
                isPlaying = true;
            }
            obPlayer.putBool(XiToCommand.IS_PLAYING, isPlaying);
            boolean isPreFlog = xiToGame.isPreFlog();
            if(isPreFlog){
                obPlayer.putShortArray(SFSKey.ARRAY_INFOR_CARD, xiToPlayer.getHidePreflogCardsIdToList());
            }else{
                obPlayer.putShortArray(SFSKey.ARRAY_INFOR_CARD,xiToPlayer.getHoldCardsCheckedToList());
            }
            listPlayers.addSFSObject(obPlayer);
        }
        fObject.putSFSArray(SFSKey.ARRAY_SFS, listPlayers);
        return fObject;
    }
    
    /**
     * Thông tin stack của người chơi trong bàn
     * @return 
     */
    public SFSObject getStackMessage(){
        SFSObject fObject = new SFSObject();
        fObject.putInt(SFSKey.ACTION_INGAME, XiToCommand.STACK);
        List<User> list = xiToGame.getPlayers();
        SFSArray listPlayers= new SFSArray();
        for(User user:list){
            SFSObject obPlayer = new SFSObject();
            String idDBUser=this.xiToGame.getIdDBOfUser(user);
            XiToPlayer xiToPlayer = xiToGame.getXiToPlayer(idDBUser);
            if(xiToPlayer==null){
                continue;
            }
            obPlayer.putUtfString(SFSKey.USER_ID, idDBUser);
            obPlayer.putDouble(XiToCommand.STACK_KEY, xiToPlayer.getStack().doubleValue());
            listPlayers.addSFSObject(obPlayer);
        }
        fObject.putSFSArray(SFSKey.ARRAY_SFS, listPlayers);
        return fObject;
    }
    /**
     * Client lấy thông tin trước khi set lại tiền cược của bàn
     * @return 
     */
    public SFSObject getBetMoneyInfoMessage(){
        SFSObject fObject = new SFSObject();
        fObject.putInt(SFSKey.ACTION_INGAME, XiToCommand.BET_MONEY_INFO);
        fObject.putDouble(XiToCommand.MAX_BET,this.xiToGame.getMaxBet());
        fObject.putDouble(XiToCommand.MIM_BET,this.xiToGame.getMinBet());
        return fObject;
    }

     /**
     * Trả về khi hết chỗ, hết tiền....
     *
     * @param reason
     * @param user
     * @return
     * @throws Exception
     */
    public SFSObject getSittingMessage(String reason, User user) throws Exception {
        SFSObject fObject = new SFSObject();
        fObject.putInt(SFSKey.ACTION_INGAME, XiToCommand.SIT);
        fObject.putUtfString(SFSKey.STRING_MESSAGE, reason);
        return fObject;
    }
    
    /**
     * Thông tin user đang choi trong ván
     * @return
     * @throws Exception 
     */
    public SFSObject getPlayingMessage() throws Exception {
        SFSObject fObject = new SFSObject();
        SFSArray arrayPlayer= new SFSArray();
        Round currentRound = this.xiToGame.getCurrentRound();
        
        fObject.putInt(SFSKey.ACTION_INGAME, SFSAction.PLAYING);
        fObject.putInt("timePreFlog", xiToGame.getRemainTimePreflog());
        fObject.putUtfString("currId",xiToGame.getIdDBOfUser(xiToGame.getCurrentPlayer()) );
        fObject.putInt("timeRemain", xiToGame.getTimeRemain());
        fObject.putDouble("totalBet", this.xiToGame.getTotalBetStack().doubleValue());
        boolean isFolded = false;
        for (User user : xiToGame.getPlayers()) {
            String idDBUser = this.xiToGame.getIdDBOfUser(user);
            XiToPlayer xiToPlayer = this.xiToGame.getXiToPlayer(idDBUser);
            if (xiToPlayer == null) {
                continue;
            }
            SFSObject ob = new SFSObject();
            ob.putUtfString("userId", idDBUser);
            ob.putDouble("stack", xiToPlayer.getStack().doubleValue());
            ob.putDouble("betStack", xiToPlayer.isAllIn() ? xiToPlayer.getBetStack().doubleValue() : currentRound.getStack(idDBUser).doubleValue());
            ob.putByte("lastAction", xiToPlayer.isAllIn() ? XiToPlayer.ALLIN : currentRound.getLastAction(idDBUser));
            isFolded = false;
            if (!xiToGame.isInturn(xiToPlayer.getUser()) && !xiToPlayer.isAllIn()) {
                isFolded = true;
            }
            ob.putBool("isfolded", isFolded);
            //đang trong vòng flog
            if(xiToGame.isPreFlog()){
                 ob.putShortArray("cards", xiToPlayer.getHidePreflogCardsIdToList());
            }else{
                //đang o vong bình thường
                 ob.putShortArray("cards", xiToPlayer.getHoldCardsCheckedToList());
            }
           arrayPlayer.addSFSObject(ob);
        }
        fObject.putSFSArray(SFSKey.ARRAY_SFS, arrayPlayer);
        return fObject;
    }
}
