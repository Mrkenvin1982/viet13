/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.vn.game.phom.message;

import com.smartfoxserver.v2.entities.User;
import com.smartfoxserver.v2.entities.data.ISFSArray;
import com.smartfoxserver.v2.entities.data.SFSArray;
import com.smartfoxserver.v2.entities.data.SFSObject;
import game.command.PhomCommand;
import game.command.SFSAction;
import game.key.SFSKey;
import game.vn.game.phom.PhomController;
import game.vn.game.phom.object.PhomPlayer;
import game.vn.game.phom.object.Phom;
import game.vn.game.phom.utils.PhomUtils;
import java.util.ArrayList;
import java.util.List;

/**
 * tất cả message trong game Phỏm
 * @author tuanp
 */
public class MessageFactory {
    private PhomController con;
    
    public MessageFactory (PhomController gamecontroller){
        this.con=gamecontroller;
    }
    /**
     * Gưi thong tin card tất cả user trong bàn khi
     * user join bàn trong lúc ván đang chơi
     * @param timeLimit
     * @param currentUserId
     * @param firstHaPlayerId
     * @return 
     */
    public SFSObject getPlayingMessage(int timeLimit, String currentUserId, String firstHaPlayerId) {
        SFSObject fObject = new SFSObject();
        fObject.putInt(SFSKey.ACTION_INGAME, SFSAction.PLAYING);
        fObject.putInt(PhomCommand.TIME_LIMIT, timeLimit);
        fObject.putUtfString(PhomCommand.ID_USER_CURRENT, currentUserId);
        fObject.putUtfString(PhomCommand.ID_USER_FIRST_HA, currentUserId);
        ISFSArray inforPlayers = new SFSArray();
        for (int i = 0; i < this.con.getPlayers().length; i++) {
            User user = this.con.getUser(i);
            if (user == null) {
                continue;
            }
            
            SFSObject fObjectPlayer = new SFSObject();
            fObjectPlayer.putUtfString(SFSKey.USER_ID, con.getIdDBOfUser(user));
            fObjectPlayer.putShortArray(PhomCommand.ARRAY_INFOR_CARD_MOVE,PhomUtils.list2List(this.con.getPlayers()[i].getCardMove()));
            fObjectPlayer.putShortArray(PhomCommand.ARRAY_INFOR_CARD_EAT, PhomUtils.list2List(this.con.getPlayers()[i].getCardEat()));
            inforPlayers.addSFSObject(fObjectPlayer);

        }
        fObject.putSFSArray(SFSKey.ARRAY_SFS,inforPlayers);
        return fObject;
    }
    /**
     * User gửi gửi bài tới phỏm của user khác để tạo thành phỏm mới
     * @param cards
     * @return 
     */
    public SFSObject getListAddCardsMessage(List<Short> cards){
        SFSObject fObject = new SFSObject();
        fObject.putInt(SFSKey.ACTION_INGAME, PhomCommand.SEND_LIST_ADD_CARD);
        fObject.putShortArray(SFSKey.ARRAY_INFOR_CARD, cards);
        return fObject;
    }
    /**
     * command hạ phỏm
     * @param player
     * @return 
     */
    public SFSObject getHaPhomMesssage(PhomPlayer player) {
        SFSObject fObject = new SFSObject();
        ISFSArray inforPlayers = new SFSArray();
        fObject.putInt(SFSKey.ACTION_INGAME, PhomCommand.HA_PHOM);
        if (!player.isIsChay()) {
            for (Phom phom : player.getCardPhom()) {
                if (phom.isIsHa()) {
                    SFSObject fObjectPlayer = new SFSObject();
                    fObjectPlayer.putShortArray(SFSKey.ARRAY_INFOR_CARD, phom.getCardIds2List());
                    inforPlayers.addSFSObject(fObjectPlayer);
                }
            }
        }
        fObject.putSFSArray(SFSKey.ARRAY_SFS,inforPlayers);
        return fObject;
    }
    /**
     * message stop game
     * @param player
     * @param idUser
     * @return 
     */
    public SFSObject getStopMesssage(PhomPlayer player, String idUser) {
        SFSObject fObject = new SFSObject();
        fObject.putInt(SFSKey.ACTION_INGAME, SFSAction.STOP_GAME);
        fObject.putUtfString(SFSKey.USER_ID, idUser);
        fObject.putDouble(SFSKey.MONEY_BONUS, player.getBonusMoney().doubleValue());
        fObject.putInt(PhomCommand.SCORES, player.getScores());
        fObject.putShortArray(SFSKey.ARRAY_INFOR_CARD, player.getCurrCardsId2Array());
        /**
         * Trong trường hợp user Ù trước khi hạ phỏm(chưa gửi phỏm về)
         * Thì gửi thêm phỏm về chổn ày
         */
        ISFSArray arrayPhom = new SFSArray();
        if (!player.isIsChay()) {
            for (Phom phom : player.getCardPhom()) {
                if (!phom.isIsHa()) {
                    SFSObject ob = new SFSObject();
                    ob.putShortArray(SFSKey.ARRAY_INFOR_CARD, phom.getCardIds2List());
                    arrayPhom.addSFSObject(ob);
                }
            }
        }
        fObject.putSFSArray(SFSKey.ARRAY_SFS,arrayPhom);
        return fObject;
    }
    /**
     * get message Ù
     * @param p
     * @param userId
     * @param uType
     * @return 
     */
    public SFSObject getUMessage(PhomPlayer p,String userId, byte uType){
        SFSObject fObject = new SFSObject();
        ISFSArray inforPlayers = new SFSArray();
        fObject.putInt(SFSKey.ACTION_INGAME, PhomCommand.U); 
        fObject.putUtfString(SFSKey.USER_ID, userId);
        fObject.putByte(SFSKey.TYPE, uType);
         for (Phom phom : p.getCardPhom()) {
                SFSObject fObjectPlayer = new SFSObject();
                fObjectPlayer.putShortArray(SFSKey.ARRAY_INFOR_CARD, phom.getCardIds2List());
                inforPlayers.addSFSObject(fObjectPlayer);
            }
        fObject.putSFSArray(SFSKey.ARRAY_SFS,inforPlayers);
        return fObject;
    }
    /**
     * Message start game
     * @param player
     * @param idUser
     * @param idFirstHa
     * @param timeLimit
     * @return 
     */
    public SFSObject getStartGameMessage(PhomPlayer player, String idUser,String idFirstHa,int timeLimit){
        SFSObject fObject = new SFSObject();
        fObject.putInt(SFSKey.ACTION_INGAME, SFSAction.START_GAME); 
        fObject.putInt(PhomCommand.TIME_LIMIT, timeLimit);
        fObject.putUtfString(SFSKey.USER_ID, idUser);
        fObject.putUtfString(PhomCommand.ID_USER_FIRST_HA, idFirstHa);
        fObject.putShortArray(SFSKey.ARRAY_INFOR_CARD,PhomUtils.list2List(player.getCardDealId()));
        return fObject;
    }
    /**
     * Add card to phỏm
     * @param idFromUser
     * @param userId
     * @param card: card gửi
     * @param cardset: phỏm add card vào
     * @return 
     */
    public SFSObject getAddCardMessage(String idFromUser, String userId, byte card, List<Short> cardset){
        SFSObject fObject = new SFSObject();
        fObject.putInt(SFSKey.ACTION_INGAME, PhomCommand.ADD_CARD);
        fObject.putUtfString(PhomCommand.ID_USER_CURRENT, idFromUser);
        fObject.putUtfString(SFSKey.USER_ID, userId);
        fObject.putByte(SFSKey.INFOR_CARD, card);
        fObject.putShortArray(SFSKey.ARRAY_INFOR_CARD, cardset);
        return fObject;
    }
    /**
     * Đánh bài
     * @param idUser
     * @param idCurrentUser
     * @param cardId
     * @param numCardMove
     * @return 
     */
    public SFSObject getMoveMessage(String idUser, String idCurrentUser,int cardId, int numCardMove){
        SFSObject fObject = new SFSObject();
        fObject.putInt(SFSKey.ACTION_INGAME, SFSAction.MOVE); 
        fObject.putUtfString(SFSKey.USER_ID, idUser); 
        fObject.putUtfString(PhomCommand.ID_USER_CURRENT, idCurrentUser); 
        fObject.putByte(SFSKey.INFOR_CARD,(byte) cardId); 
        fObject.putInt(PhomCommand.NUM_CARD_MOVE, numCardMove); 
        return fObject;
    }
    /**
     * Rút bài
     * @param idUser
     * @param cardId
     * @return 
     */
    public SFSObject getCardMessage(String idUser,int cardId){
        SFSObject fObject = new SFSObject();
        fObject.putInt(SFSKey.ACTION_INGAME, PhomCommand.GET_CARD); 
        fObject.putUtfString(SFSKey.USER_ID, idUser); 
        fObject.putByte(SFSKey.INFOR_CARD,(byte) cardId);  
        return fObject;
    }
    /**
     * Qua lượt
     * @param idCurrentUser
     * @param idFirstHa
     * @return 
     */
    public SFSObject getSkipMessage(String idCurrentUser,String idFirstHa){
        SFSObject fObject = new SFSObject();
        fObject.putInt(SFSKey.ACTION_INGAME, SFSAction.SKIP); 
        fObject.putUtfString(PhomCommand.ID_USER_CURRENT, idCurrentUser); 
        fObject.putUtfString(PhomCommand.ID_USER_FIRST_HA, idFirstHa);  
        return fObject;
    }
    /**
     * Ăn bài
     * @param idFirstHa
     * @return 
     */
    public SFSObject getEatCardMessage(String idFirstHa){
        SFSObject fObject = new SFSObject();
        fObject.putInt(SFSKey.ACTION_INGAME, PhomCommand.EAT_CARD); 
        fObject.putUtfString(PhomCommand.ID_USER_FIRST_HA, idFirstHa);  
        return fObject;
    }
    /**
     * Trả về thông tin bàn khi user reconnect game
     * @param timeLimit
     * @param currentPlayerId
     * @param lastMovePlayerId
     * @param firstHaPlayerId
     * @param currentCardId
     * @param numOfPlayingPlayer
     * @param players
     * @param timeRemain
     * @param player
     * @return 
     */
    public SFSObject getOnreturnGameMessage(int timeLimit, String currentPlayerId, String lastMovePlayerId, String firstHaPlayerId,
            int currentCardId, int numOfPlayingPlayer, PhomPlayer[] players, int timeRemain, PhomPlayer player) {
        SFSObject fObject = new SFSObject();
        ISFSArray inforPlayers = new SFSArray();
        
        fObject.putInt(SFSKey.ACTION_INGAME, SFSAction.ON_RETURN_GAME);
        fObject.putInt("timeLimit", timeLimit);
        fObject.putInt("timeRemain", timeRemain);
        fObject.putUtfString("currId", currentPlayerId);
        fObject.putUtfString("lastId", lastMovePlayerId);
        fObject.putUtfString("firstId", firstHaPlayerId);
        fObject.putInt("currCardId", currentCardId);
        fObject.putByte("numPlayers", (byte) numOfPlayingPlayer);
        for(int i=0; i<players.length; i++){
            
            User user=this.con.getUser(i);
            if(user==null){
                continue;
            }
            
            PhomPlayer p= players[i];
            if(p.isIsLeaved()){
                continue;
            }
            
            ISFSArray inforPhoms = new SFSArray();
            SFSObject ob= new SFSObject();
            ob.putUtfString("userId", con.getIdDBOfUser(user));
            ob.putShortArray("arrCardMove", PhomUtils.list2List(p.getCardMove()));
            ob.putShortArray("arrCardEat", PhomUtils.list2List(p.getCardEat()));
            for (Phom phom : players[i].getCardPhom()) {
                if (phom.isIsHa()) {
                    SFSObject fObjectPlayer = new SFSObject();
                    fObjectPlayer.putShortArray("arrPhom", phom.getCardIds2List());
                    inforPhoms.addSFSObject(fObjectPlayer);
                }
            }
            ob.putSFSArray("arrPhom", inforPhoms);
            inforPlayers.addSFSObject(ob);
        }

        fObject.putSFSArray("arrPlayers", inforPlayers);
        
        List<Byte> listAllCard = new ArrayList<>();
        for(Phom phom : player.getCardPhom()) {
            if (!phom.isIsHa()) {
                listAllCard.addAll(phom.getCardIds());
            }
        }
        listAllCard.addAll(player.getCurrCardsId());
        fObject.putShortArray("arrCards",PhomUtils.list2List(listAllCard));
        
        return fObject;
    }
}
