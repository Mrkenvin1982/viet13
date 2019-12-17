/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.vn.game.sam.message;

import com.smartfoxserver.v2.entities.User;
import com.smartfoxserver.v2.entities.data.ISFSArray;
import com.smartfoxserver.v2.entities.data.SFSArray;
import com.smartfoxserver.v2.entities.data.SFSObject;
import game.command.SFSAction;
import game.command.SamCommand;
import game.key.SFSKey;
import game.vn.game.sam.SamController;
import game.vn.game.sam.object.SamPlayer;
import java.util.List;

/**
 *
 * @author tuanp
 */
public class SamGameMessage {

    SamController game;

    public SamGameMessage(SamController game) {
        this.game = game;
    }

    /**
     * Bỏ lượt
     *
     * @param idPlayer
     * @param idCurrentPlayer
     * @param isClearCard
     * @return
     */
    public SFSObject getSkipMessage(String idPlayer, String idCurrentPlayer, boolean isClearCard) {
        SFSObject ob = new SFSObject();
        ob.putInt(SFSKey.ACTION_INGAME, SFSAction.SKIP);
        ob.putUtfString(SFSKey.USER_ID, idPlayer);
        ob.putUtfString(SFSKey.USER_ID_CURRENT, idCurrentPlayer);
        ob.putBool(SamCommand.IS_CLEAR_CARD, isClearCard);
        return ob;
    }

    /**
     * get sam message
     *
     * @return
     */
    public SFSObject getSamMessage() {
        SFSObject ob = new SFSObject();
        ob.putInt(SFSKey.ACTION_INGAME, SamCommand.XAM);
        ob.putUtfString(SamCommand.USER_ID_SAM, this.game.getIdDBOfUser(this.game.getXamUser()));
        return ob;
    }

    /**
     * Get Skip Xâm message để gửi cho client khi toàn bộ người chơi Hủy báo Xâm
     *
     * @return
     */
    public SFSObject getSkipXamMessage() {
        SFSObject ob = new SFSObject();
        ob.putInt(SFSKey.ACTION_INGAME, SamCommand.SKIP_XAM);
        ob.putUtfString(SFSKey.USER_ID_CURRENT, this.game.getIdDBOfUser(game.getCurrentPlayer()));
        return ob;
    }

    /**
     * Gửi message tới trắng
     *
     * @param typeForce
     * @return
     */
    public SFSObject getForceFinishMessage(int typeForce) {
        SFSObject ob = new SFSObject();
        String idDBWinner = game.getIdDBOfUser(game.getWinner());
        
        ob.putInt(SFSKey.ACTION_INGAME, SamCommand.FORCE_FINISH);
        ob.putUtfString(SFSKey.USER_ID, idDBWinner);
        SamPlayer winnerPlayer = game.getXamPlayer(idDBWinner);
        ob.putShortArray(SFSKey.ARRAY_INFOR_CARD, winnerPlayer.getCardsToList());
        ob.putInt(SFSKey.TYPE, typeForce);
        return ob;
    }

    /**
     * Message gửi cho toàn bộ người chơi khi chặn Xâm
     *
     * @return
     */
    public SFSObject getChanXamMessage() {
        SFSObject ob = new SFSObject();
        ob.putInt(SFSKey.ACTION_INGAME, SamCommand.CHAN_XAM);
        ob.putUtfString(SamCommand.USER_ID_SAM, game.getIdDBOfUser(game.getXamUser()));
        return ob;
    }

    /**
     * Get stop game message
     *
     * @return
     */
    public SFSObject getStopMessage() {
        SFSObject ob = new SFSObject();
        ob.putInt(SFSKey.ACTION_INGAME, SFSAction.STOP_GAME);
        ob.putUtfString(SFSKey.USER_ID,game.getIdDBOfUser(game.getWinner()));

        ISFSArray inforPlayers = new SFSArray();
        for (User user : game.getPlayingPlayers()) {
            SFSObject fObjectPlayer = new SFSObject();
            String idDBUser = this.game.getIdDBOfUser(user);
            SamPlayer player = game.getXamPlayer(idDBUser);
            if(player==null){
                continue;
            }
            fObjectPlayer.putUtfString(SFSKey.USER_ID, idDBUser);
            fObjectPlayer.putDouble(SamCommand.PENALTY, player.getPenalty().doubleValue());
            fObjectPlayer.putShortArray(SFSKey.ARRAY_INFOR_CARD, player.getCardsToList());
            inforPlayers.addSFSObject(fObjectPlayer);
        }
        ob.putSFSArray(SFSKey.ARRAY_SFS,inforPlayers);
        return ob;
    }
   

    /**
     * Get move message
     *
     * @param isBiChat
     * @return
     */
    public SFSObject getMoveMessage(boolean isBiChat) {
        SFSObject ob = new SFSObject();
        ob.putInt(SFSKey.ACTION_INGAME, SFSAction.MOVE);
        ob.putUtfString(SFSKey.USER_ID, game.getIdDBOfUser(game.getPlayerMoved()));
        ob.putUtfString(SFSKey.USER_ID_CURRENT,game.getIdDBOfUser(game.getCurrentPlayer()));
        ob.putShortArray(SFSKey.ARRAY_INFOR_CARD, game.getCardsMoveToList());
        ob.putBool(SamCommand.IS_BI_CHAT, isBiChat);
        return ob;
    }

    /**
     * Thời gian báo sâm
     *
     * @return
     */
    public SFSObject getXamTimeInfoMessage() {
        SFSObject ob = new SFSObject();
        ob.putInt(SFSKey.ACTION_INGAME, SamCommand.XAM_TIME);
        ob.putByte(SamCommand.TIME_LIMIT, game.getTimeToXam());
        return ob;
    }

    /**
     * thông báo user chỉ còn 1 lá bài duy nhất
     *
     * @param userId: id user còn 1 lá bài
     * @return
     */
    public SFSObject getNotifyOneCardMessage(String userId) {
        SFSObject ob = new SFSObject();
        ob.putInt(SFSKey.ACTION_INGAME, SamCommand.HAVE_ONE_CARD);
        ob.putUtfString(SFSKey.USER_ID, userId);
        return ob;
    }

    public SFSObject getDenBaiMessage(String userId, List<Short> cards) {
        SFSObject ob = new SFSObject();
        ob.putInt(SFSKey.ACTION_INGAME, SamCommand.DEN_BAI);
        ob.putUtfString(SFSKey.USER_ID, userId);
        ob.putShortArray(SFSKey.ARRAY_INFOR_CARD, cards);
        return ob;
    }
    /**
     * Gửi message thông tin bàn khi có user join vào bàn lúc
     * ván đang chơi
     * @param timeLimit
     * @param remainTime
     * @param idPlayerMove
     * @param currentUserId
     * @param arrayCardMove
     * @return 
     */
    public SFSObject getPlayingMessage(int timeLimit, int remainTime,String idPlayerMove, String currentUserId,List<Short> arrayCardMove) {
        SFSObject fObject = new SFSObject();
        fObject.putInt(SFSKey.ACTION_INGAME, SFSAction.PLAYING);
        fObject.putInt(SamCommand.TIME_LIMIT, (byte) timeLimit);
        fObject.putInt(SamCommand.TIME_REMAINING, (byte) remainTime);
        fObject.putUtfString(SFSKey.USER_ID_CURRENT, currentUserId);
        fObject.putUtfString(SamCommand.USER_ID_MOVED, idPlayerMove);
        fObject.putShortArray(SFSKey.ARRAY_INFOR_CARD, arrayCardMove);
        return fObject;
    }
}
