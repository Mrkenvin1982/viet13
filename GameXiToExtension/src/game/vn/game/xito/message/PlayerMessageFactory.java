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
import game.vn.game.xito.XiToController;
import game.vn.game.xito.lang.XiToLanguage;
import game.vn.game.xito.object.ResultCard;
import game.vn.game.xito.object.Round;
import game.vn.game.xito.object.XiToCardUtil;
import game.vn.game.xito.object.XiToPlayer;

/**
 *
 * @author tuanp
 */
public class PlayerMessageFactory {
    private  XiToPlayer currentPlayer;
    private XiToController game;

    public PlayerMessageFactory(XiToPlayer p) {
        this.currentPlayer = p;
        this.game = p.getGameController();
    }
    
    /**
     * Xu ly chung cac message lien quan toi tien
     * @param cmd
     * @param desc
     * @param money
     * @return 
     */
    public SFSObject getCommonMessage(int cmd, String desc, double money){
        SFSObject fObject = new SFSObject();
        fObject.putInt(SFSKey.ACTION_INGAME, cmd);
        fObject.putUtfString(SFSKey.USER_ID_CURRENT,game.getIdDBOfUser(currentPlayer.getUser()));
        fObject.putUtfString(SFSKey.STRING_MESSAGE, desc);
        fObject.putDouble(SFSKey.MONEY_BET, money);
        fObject.putDouble(XiToCommand.STACK_KEY, currentPlayer.getStack().doubleValue());
        return fObject;
    }
    
    /**
     * cmd chuyển lượt
     * @return
     * @throws Exception 
     */
    public SFSObject getNextTurnMessage()throws Exception{
        SFSObject fObject = new SFSObject();
        fObject.putInt(SFSKey.ACTION_INGAME, SFSAction.NEXT_TURN);
        fObject.putUtfString(SFSKey.USER_ID_CURRENT, game.getIdDBOfUser(currentPlayer.getUser()));
        fObject.putDouble(XiToCommand.MIN_RAISE, game.getMinRaise().doubleValue());
        fObject.putInt(XiToCommand.TIME_LIMIT, game.getTimeLimit()/1000);
        fObject.putShortArray(XiToCommand.XI_TO_ACTION_ARRAY, currentPlayer.getActionsToList());

        return fObject;
    }
    public SFSObject getHandEvalMessage(){
        SFSObject fObject = new SFSObject();
        fObject.putInt(SFSKey.ACTION_INGAME, XiToCommand.HAND_EVAL);
        ResultCard result = XiToCardUtil.evalCards(currentPlayer.getHoldCards().values());
        fObject.putByte(XiToCommand.RESULT_CARDS_VALUE, result.getValue());
        fObject.putUtfString(SFSKey.STRING_MESSAGE, XiToLanguage.getMessage(result.getStrValue(),game.getLocaleOfUser(currentPlayer.getUser())));
        fObject.putShortArray(SFSKey.ARRAY_INFOR_CARD, currentPlayer.getHoldCardsIdToList());
        return fObject;
    }
    
    /**
     * Tra về thông tin trong ván khi user reconect lại ván
     * @return
     * @throws Exception 
     */
    public SFSObject getReturnGameMessage() throws Exception{
        SFSObject fObject= new SFSObject();
        fObject.putInt(SFSKey.ACTION_INGAME,SFSAction.ON_RETURN_GAME);
        SFSArray arrayPlayer= new SFSArray();
        Round currentRound = this.game.getCurrentRound();
        
        byte timeRemainPreFlog = game.getRemainTimePreflog();
        
        boolean isPlaying = game.isPlaying();
        boolean isFolded = false;
        if (!game.isInturn(currentPlayer.getUser()) && !currentPlayer.isAllIn()) {
            isFolded = true;
        }
        
        fObject.putInt("timePreFlog", timeRemainPreFlog);
        fObject.putBool("isFolded", isFolded);
        fObject.putBool("isPlaying", isPlaying);
        fObject.putShortArray("holdCards", currentPlayer.getHoldCardsIdToList());
        if(!isFolded){
            ResultCard result = currentPlayer.getResultCards();
            fObject.putByte("value", result.getValue());
            fObject.putUtfString("strValue", XiToLanguage.getMessage(result.getStrValue(), game.getLocaleOfUser(currentPlayer.getUser())));
        }

        fObject.putUtfString("userId", game.getIdDBOfUser(game.getCurrentPlayer()));
        fObject.putDouble("minRaise",game.getMinRaise().doubleValue());
        fObject.putShortArray("arrAction", currentPlayer.getActionsToList());
        fObject.putInt("timeRemain", game.getTimeRemain());
        fObject.putBool("isHideCard", currentPlayer.getHideCard()==null);
        
        for (User user : game.getPlayers()) {
            String idDBUser = this.game.getIdDBOfUser(user);
            XiToPlayer xiToPlayer = this.game.getXiToPlayer(idDBUser);
            if (xiToPlayer == null) {
                continue;
            }
            SFSObject ob = new SFSObject();
            ob.putUtfString("userId", idDBUser);
            ob.putDouble("stack", xiToPlayer.getStack().doubleValue());
            ob.putDouble("betStack", xiToPlayer.isAllIn() ? xiToPlayer.getBetStack().doubleValue() : currentRound.getStack(idDBUser).doubleValue());
            ob.putByte("lastAction", xiToPlayer.isAllIn() ? XiToPlayer.ALLIN : currentRound.getLastAction(idDBUser));
            isFolded = false;
            if (!game.isInturn(xiToPlayer.getUser()) && !xiToPlayer.isAllIn()) {
                isFolded = true;
            }
            ob.putBool("isfolded", isFolded);
            //đang trong vòng flog
            if(game.isPreFlog()){
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
    
    public void setCurrentPlayer(XiToPlayer currentPlayer) {
        this.currentPlayer = currentPlayer;
    }
}
