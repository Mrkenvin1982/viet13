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
import game.vn.util.Utils;

/**
 * Quản lý messsage cho bàn chơi
 * @author tuanp
 */
public class SamPlayerMessage {

    SamController game;
    SamPlayer player;

    public SamPlayerMessage(SamPlayer player) {
        this.player=player;
        this.game = player.getGame();
        
    }
    /**
     * Get start message
     * @param playingTime
     * @return 
     */
    public SFSObject getStartMessage(int playingTime) {
        SFSObject ob = new SFSObject();
        ob.putInt(SFSKey.ACTION_INGAME, SFSAction.START_GAME);
        ob.putUtfString(SFSKey.USER_ID, game.getIdDBOfUser(game.getMoveFirst()));
        ob.putInt(SamCommand.TIME_LIMIT,(playingTime));
        ob.putShortArray(SFSKey.ARRAY_INFOR_CARD, player.getCardsToList());
        return ob;
    }
    public SFSObject getMoveErrorMessage(String mess) {
        SFSObject ob = new SFSObject();
        ob.putInt(SFSKey.ACTION_INGAME,SamCommand.MOVE_ERROR);
        ob.putUtfString(SFSKey.STRING_MESSAGE, mess);
        return ob;
    }
    /**
     * on return game
     * @return 
     */
    public SFSObject getReturnGameMessage(){
        SFSObject fObject = new SFSObject();
        ISFSArray inforPlayers = new SFSArray();
        
        fObject.putInt(SFSKey.ACTION_INGAME,SFSAction.ON_RETURN_GAME);
        fObject.putInt("timeLimit",game.getTimeLimit()/1000);
        //Gửi userId của player vừa đánh bài
        fObject.putUtfString("lastId",game.getIdDBOfUser(game.getPlayerMoved()));
        fObject.putShortArray("lastCards", game.getCardsMoveToList());
        //userID của player hiện tại đang trong lượt đi
        fObject.putUtfString("currId",game.getIdDBOfUser(game.getCurrentPlayer()));
        fObject.putShortArray("cards", player.getCardsToList());
        for (User user : game.getPlayingPlayers()) {
            SFSObject fObjectPlayer = new SFSObject();
            String idDBUser= game.getIdDBOfUser(user);
            SamPlayer player = game.getXamPlayer(idDBUser);
            if (player == null) {
                continue;
            }
            fObjectPlayer.putUtfString("userId",idDBUser);
            fObjectPlayer.putBool("isSkip", player.isSkippedState());
            fObjectPlayer.putBool("isOneCardLeft", player.isOneCardLeft());
            inforPlayers.addSFSObject(fObjectPlayer);
        }
        fObject.putSFSArray(SFSKey.ARRAY_SFS, inforPlayers);
        fObject.putInt("timeRemain", game.getTimeRemain());
        fObject.putUtfString("xamId", game.getIdDBOfUser(game.getXamUser()));
        //Trả về để biết có hiển thị dialog báo xâm hay không
        //chỗ này join lúc game đang chơi sẽ hiện bảng báo xâm, đổi state ?
        fObject.putBool("isReady", player.isReadyState());
        
        fObject.putBool("isNewRound",Utils.isEqual(game.getPlayerBeginNewRound(), player.getUser()));
        //kiểm tra toàn bộ player đã skip xâm hết chưa
        fObject.putBool("isAllSkip",game.getXamUser()!=null?true:game.isSkipXamFull());
        
        return fObject;
    }
}