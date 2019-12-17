/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.vn.game.tienlen.message;

import com.smartfoxserver.v2.entities.User;
import com.smartfoxserver.v2.entities.data.SFSArray;
import com.smartfoxserver.v2.entities.data.SFSObject;
import game.command.SFSAction;
import game.command.SamCommand;
import game.command.TienLenCommand;
import game.key.SFSKey;
import game.vn.common.card.object.Card;
import game.vn.game.tienlen.object.TienLenPlayer;
import game.vn.game.tienlen.TienLenController;
import java.util.ArrayList;
import java.util.List;

/**
 * Quản lý tất cả message của game TLMN tại đây
 *
 * @author hoanghh
 */
public class MessageFactory {

    private static final MessageFactory INSTANCE = new MessageFactory();

    public static MessageFactory getINSTANCE() {
        return INSTANCE;
    }

    /**
     * Tạo message xử lý chức năng start game
     *
     * @param timeLimit
     * @param cards
     * @param userIdCurrentPlayer
     * @return
     */
    public SFSObject createMessageStartGame(int timeLimit, List<Card> cards, String userIdCurrentPlayer) {
        SFSObject startObj = new SFSObject();
        startObj.putInt(SFSKey.ACTION_INGAME, SFSAction.START_GAME);
        startObj.putInt(SamCommand.TIME_LIMIT, timeLimit);
        List<Short> cardIds = new ArrayList<>();
        for (int i = 0; i < cards.size(); i++) {
            cardIds.add((short)cards.get(i).getId());
        }
        startObj.putShortArray(SFSKey.ARRAY_INFOR_CARD, cardIds);

        startObj.putUtfString(SFSKey.USER_ID, userIdCurrentPlayer);

        return startObj;
    }

    /**
     * Tạo message stop game
     *
     * @param userIdCurrentPlayer
     * @param players
     * @param game
     * @return
     */
    public SFSObject createMessageStopGame(String userIdCurrentPlayer, TienLenPlayer[] players, TienLenController game) {
        SFSObject stopObj = new SFSObject();
        stopObj.putInt(SFSKey.ACTION_INGAME, SFSAction.STOP_GAME);
        // lat bai cua nguoi ve bet
        stopObj.putUtfString("curUid", userIdCurrentPlayer);
        SFSArray sfsArrUsers = new SFSArray();
        for (int i = 0; i < players.length; i++) {
            User user = game.getUser(i);
            if (user != null) {
                SFSObject playerData = new SFSObject();
                playerData.putUtfString("userId", game.getIdDBOfUser(user));
                playerData.putShortArray("arrCards", players[i].cardsToList());
                sfsArrUsers.addSFSObject(playerData);
            }
        }
        stopObj.putSFSArray("sfsArray", sfsArrUsers);

        return stopObj;
    }

    /**
     * Tạo message finish
     *
     * @param userId
     * @param typeWin
     * @return
     */
    public SFSObject createMessageFinishGame(String userId, byte typeWin) {
        SFSObject finishObj = new SFSObject();
        finishObj.putInt(SFSKey.ACTION_INGAME, TienLenCommand.FINISH);
        finishObj.putUtfString("uid", userId);
        finishObj.putByte("win", typeWin);
        return finishObj;
    }

    /**
     * Message tới trắng
     *
     * @param userId
     * @param cards
     * @param typeForce
     * @return
     */
    public SFSObject createMessageForceFinish(String userId, List<Card> cards, int typeForce) {
        SFSObject sfso = new SFSObject();
        sfso.putInt(SFSKey.ACTION_INGAME, SamCommand.FORCE_FINISH);
        sfso.putUtfString(SFSKey.USER_ID, userId);
        List<Short> cardIds = new ArrayList<>();
        for (int i = 0; i < cards.size(); i++) {
            cardIds.add((short)cards.get(i).getId());
        }
        sfso.putShortArray(SFSKey.ARRAY_INFOR_CARD, cardIds);
        sfso.putInt(SFSKey.TYPE, typeForce);

        return sfso;
    }

    /**
     * Tạo message xử lý chức năng move card của user
     *
     * @param userIdMover
     * @param cardMove
     * @param currentUser
     * @param isChatHeo
     * @param sizeCards
     * @return
     */
    public SFSObject createMessageMove(String userIdMover, List<Short> cardMove, String currentUser,boolean isChatHeo, int sizeCards) {
        SFSObject sfso = new SFSObject();
        sfso.putInt(SFSKey.ACTION_INGAME, SFSAction.MOVE);
        sfso.putUtfString(SFSKey.USER_ID, userIdMover);
        sfso.putUtfString(SFSKey.USER_ID_CURRENT, currentUser);
        sfso.putShortArray(SFSKey.ARRAY_INFOR_CARD, cardMove);
        sfso.putBool(SamCommand.IS_BI_CHAT, isChatHeo);
        return sfso;
    }

    /**
     * Tạo message xử lý chức năng skip
     *
     * @param userIdSkipper
     * @param userIdCurrentPlayer
     * @param isSkipAll 
     * @return
     */
    public SFSObject createMessageSkip(String userIdSkipper, String userIdCurrentPlayer, boolean isSkipAll) {
        SFSObject sfso = new SFSObject();
        sfso.putInt(SFSKey.ACTION_INGAME, SFSAction.SKIP);
        sfso.putUtfString(SFSKey.USER_ID, userIdSkipper);
        sfso.putUtfString(SFSKey.USER_ID_CURRENT, userIdCurrentPlayer);
        sfso.putBool(SamCommand.IS_CLEAR_CARD, isSkipAll);
        return sfso;
    }

    /**
     * Tạo message return game
     *
     * @param controller
     * @param userMover
     * @param cardMove
     * @param currentUser
     * @param cardsMyself
     * @param getTimeRemain
     * @param isUserBeginNewRound
     * @param players
     * @param playingTime
     * @return
     */
    public SFSObject createReturnMessage(TienLenController controller, String userMover, Card[] cardMove, String currentUser, List<Card> cardsMyself,
            int getTimeRemain, boolean isUserBeginNewRound, TienLenPlayer[] players, int playingTime) {
        SFSObject sfso = new SFSObject();
        sfso.putInt(SFSKey.ACTION_INGAME, SFSAction.ON_RETURN_GAME);
        sfso.putInt("tLimit", playingTime);
        sfso.putUtfString("uMoved", userMover);
        List<Short> cardsIdMove = new ArrayList<>();
        if (cardMove != null && cardMove.length > 0) {
            for (int i = 0; i < cardMove.length; i++) {
                cardsIdMove.add((short)cardMove[i].getId());
            }
            sfso.putShortArray("cardsMoved", cardsIdMove);
        } else {
            sfso.putShortArray("cardsMoved", cardsIdMove);
        }


        sfso.putUtfString("curUser", currentUser);
        //gui bai cua ban than
         //gui bai cua ban than
        List<Short> myCardIds = new ArrayList<>();
        for (int i = 0; i < cardsMyself.size(); i++) {
            myCardIds.add((short)cardsMyself.get(i).getId());
        }
        sfso.putShortArray("myCards", myCardIds);
        SFSArray sfsArrUsers = new SFSArray();
        // gui ve danh sach nguoi choi con lai trong ban va trang thai luc do(nhat, nhi...)
        for (int i=0; i<players.length; i++) {
            User u = controller.getUser(i);
            if (u != null) {
                SFSObject playerData = new SFSObject();
                playerData.putUtfString("userId",controller.getIdDBOfUser(u));
                playerData.putInt("win", players[i].getWin());
                playerData.putBool("isSkip", players[i].isSkipstatus());
                sfsArrUsers.addSFSObject(playerData);
            }
        }
        sfso.putSFSArray("listPlayerData", sfsArrUsers);
        sfso.putInt("tRemain", getTimeRemain);
        sfso.putBool("isNewRound", isUserBeginNewRound);
        return sfso;
    }
    /**
     * gửi message cho user join game khi ván đang playing
     * sửa lại key giống game Sâm vì client dang code sam va tlmn 1 source
     * @param userIdMover
     * @param cardMove
     * @param currentUser
     * @param remainingTime
     * @param playingTime
     * @return 
     */
    public SFSObject createMessagePlaying(String userIdMover, List<Short> cardMove, String currentUser, int remainingTime, int playingTime) {
        SFSObject sfso = new SFSObject();
        sfso.putInt(SFSKey.ACTION_INGAME, SFSAction.PLAYING);
        sfso.putInt(SamCommand.TIME_LIMIT, playingTime);
        sfso.putInt(SamCommand.TIME_REMAINING, remainingTime);
        sfso.putUtfString(SFSKey.USER_ID_CURRENT, currentUser);
        sfso.putUtfString(SamCommand.USER_ID_MOVED, userIdMover);
        sfso.putShortArray(SFSKey.ARRAY_INFOR_CARD, cardMove);
        return sfso;
    }
    
     /**
     * infor cards for bot
     *
     * @param botSeat
     * @param players
     * @param game
     * @return
     */
    public SFSObject createMessageInforCardsGame(int botSeat, TienLenPlayer[] players, TienLenController game) {
        SFSObject stopObj = new SFSObject();
        stopObj.putInt(SFSKey.ACTION_INGAME, SFSAction.BOT_REQUEST_INFOR_CARDS);
        SFSArray sfsArrUsers = new SFSArray();
        for (int i = 0; i < players.length; i++) {
            int seat = botSeat % players.length; 
            User user = game.getUser(seat);
            if (user != null && game.isInturn(user)) {
                SFSObject playerData = new SFSObject();
                playerData.putUtfString("userId", game.getIdDBOfUser(user));
                playerData.putShortArray("arrCards", players[seat].cardsToList());
                playerData.putBool("isSkip", players[seat].isSkipstatus());
                sfsArrUsers.addSFSObject(playerData);
            }
            botSeat++;
        }
        stopObj.putSFSArray("sfsArray", sfsArrUsers);

        return stopObj;
    }
}
