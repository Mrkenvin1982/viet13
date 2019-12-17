/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.vn.game.lieng.message;

import com.smartfoxserver.v2.entities.User;
import com.smartfoxserver.v2.entities.data.SFSArray;
import com.smartfoxserver.v2.entities.data.SFSObject;
import game.command.LiengCommand;
import game.command.SFSAction;
import game.key.SFSKey;
import game.vn.game.lieng.LiengConfig;
import game.vn.game.lieng.LiengController;
import game.vn.game.lieng.lang.LiengLanguage;
import game.vn.game.lieng.object.LiengPlayer;
import java.util.Locale;

/**
 *
 * @author tuanp
 */
public class LiengGameMessageFactory {
    LiengController game;
    
    public LiengGameMessageFactory(LiengController game){
        this.game=game;  
    }
    /**
     * message start game
     * @return 
     */
    public SFSObject getStartGameMessage(){
        SFSObject ob= new SFSObject();
        ob.putInt(SFSKey.ACTION_INGAME,SFSAction.START_GAME);
        return ob;
    }
    /**
     * Tổng số tiền bet trong ván
     * @return 
     */
    public SFSObject getBoardBetMessage(){
        SFSObject ob= new SFSObject();
        ob.putInt(SFSKey.ACTION_INGAME,LiengCommand.BOARD_BET);
        ob.putDouble(LiengCommand.TOTAL_BET_MONEY, game.getTotalBetMoney().doubleValue());
        ob.putByte(LiengCommand.ROUND_KEY, (byte) (game.getRounds().size()-1));
        return ob;
    }
    /**
     * Gửi về message kết quả của bàn
     * @param locale
     * @return 
     */
    public SFSObject getResultMessage(Locale locale) {
        boolean isAllFolded = game.isAllFolded();
        SFSObject ob = new SFSObject();
        ob.putInt(SFSKey.ACTION_INGAME, SFSAction.RESULT);
        ob.putUtfString(SFSKey.USER_ID, game.getIdDBOfUser(game.getWinner()));
        SFSArray listPlayers = new SFSArray();
        for (User user : game.getPlayingPlayers()) {
            SFSObject obPlayer = new SFSObject();
            String idDBUser = game.getIdDBOfUser(user);
            LiengPlayer p = game.getLiengPlayer(idDBUser);
            if(p==null){
                continue;
            }
            obPlayer.putUtfString(SFSKey.USER_ID, idDBUser);
            if (isAllFolded) {
                obPlayer.putUtfString(SFSKey.STRING_MESSAGE, "");
                obPlayer.putShortArray(SFSKey.ARRAY_INFOR_CARD, p.getCardsHideToList());
            }else{
                obPlayer.putUtfString(SFSKey.STRING_MESSAGE,p.getResultCard(locale).getStrValue());
                obPlayer.putShortArray(SFSKey.ARRAY_INFOR_CARD, p.getCardsToList());
            }
            listPlayers.addSFSObject(obPlayer);
        }
        ob.putSFSArray(SFSKey.ARRAY_SFS, listPlayers);
        return ob;
    }
    /**
     * Gửi lúc vào bàn và bắt đầu ván
     * @param isStart
     * @return 
     */
    public SFSObject getViewerMessage(boolean isStart){
        SFSObject ob = new SFSObject();
        ob.putInt(SFSKey.ACTION_INGAME, LiengCommand.VIEWER);
        ob.putInt("timeRemain", game.getTimeRemain());
        ob.putUtfString("currId",game.getIdDBOfUser(game.getCurrentPlayer()));
        //roundSize - 1 vì vòng đặt cược đầu tiên ko hiển thị ra cho user thấy
        ob.putByte("roundSize",(byte) (game.getRounds().size()-1));
        ob.putDouble("totalBet",game.getTotalBetMoney().doubleValue());
        SFSArray listPlayers = new SFSArray();
        for (User user : game.getPlayingPlayers()) {
            String idDBUser = game.getIdDBOfUser(user);
            LiengPlayer p = game.getLiengPlayer(idDBUser);
            if(p==null){
                continue;
            }
            SFSObject obPlayer = new SFSObject();
            obPlayer.putUtfString("userId", idDBUser);
            obPlayer.putDouble("money", game.getMoneyFromUser(user).doubleValue());
            obPlayer.putDouble("totalBetUser",p.getBetMoney().doubleValue());
            obPlayer.putByte("lastAction",(byte) p.getLastAction());
            obPlayer.putBool("isFolded", p.isFolded());
            obPlayer.putUtfString("actionDesc", p.getLastActionDesc());
            obPlayer.putShortArray("hideCards", p.getCardsHideToList());
            listPlayers.addSFSObject(obPlayer);
        }
        ob.putSFSArray(SFSKey.ARRAY_SFS, listPlayers);
        ob.putBool(LiengCommand.IS_STARTED, isStart);
        return ob;
    }
    /**
     * Trả về khi user ko đủ win để vào bàn
     * @param user
     * @return 
     */
    public SFSObject getNotEnoughWinMessage(User user){
        SFSObject ob= new SFSObject();
        ob.putInt(SFSKey.ACTION_INGAME, LiengCommand.SIT);
        String infor=String.format(LiengLanguage.getMessage(LiengLanguage.NOT_ENOUGH_WIN_TO_CREATE,this.game.getLocaleOfUser(user)),this.game.getCurrency(this.game.getLocaleOfUser(user)),game.getMinJoinGame());
        ob.putUtfString(SFSKey.STRING_MESSAGE,infor);
        return ob;
    }
}
