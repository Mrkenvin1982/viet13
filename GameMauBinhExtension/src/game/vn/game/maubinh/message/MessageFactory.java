/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package game.vn.game.maubinh.message;

import com.smartfoxserver.v2.entities.data.ISFSArray;
import com.smartfoxserver.v2.entities.data.SFSArray;
import com.smartfoxserver.v2.entities.data.SFSObject;
import game.command.MauBinhCommand;
import game.command.SFSAction;
import game.key.SFSKey;
import game.vn.common.card.object.Card;
import game.vn.game.maubinh.MauBinhConfig;
import game.vn.game.maubinh.object.Player;
import game.vn.game.maubinh.object.Result;
import game.vn.util.Utils;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author binhnt
 */
public class MessageFactory {
    
    private static Logger log = LoggerFactory.getLogger(MessageFactory.class);
    
    public static Message makeStartMessage(byte limitTime,
            List<Card> cards, byte maubinhType) {
        Message m = createMauBinhMessage(SFSAction.START_GAME);
        try {
            m.putByte(MauBinhCommand.LIMIT_TIME, limitTime);
            List<Short>cardList = new ArrayList<>();
            for (int i = 0; i < cards.size(); i++) {
                cardList.add((short)cards.get(i).getId());
            }

            m.putShortArray(SFSKey.ARRAY_INFOR_CARD, cardList);
            m.putByte(MauBinhCommand.MAUBINH_TYPE, maubinhType);
        } catch (Exception e) {
            log.error("Mau binh MessageFactory.makeStartMessage error: ", e);
        }

        return m;
    }
    
    public static Message makeStopMessage() {
        return createMauBinhMessage(SFSAction.STOP_GAME);
    }
    
    public static Message makeTableInfoMessage(byte limitTime, byte restTime) {
        Message m = createMauBinhMessage(MauBinhCommand.TABLE_INFO);
        try {
            m.putByte(MauBinhCommand.LIMIT_TIME, limitTime);
            m.putByte(MauBinhCommand.REST_TIME, restTime);
        } catch (Exception e) {
            log.error("Mau binh MessageFactory.makeTableInfoMessage error: ", e);
        }
        
        return m;
    }

    public static Message makeSendCardMessage(String userId, int type, List<Card> cards) {
        Message m = createMauBinhMessage(MauBinhCommand.SEND_CARDS);
        try {
            m.putUtfString(SFSKey.USER_ID, userId);
            m.putByte(MauBinhCommand.MAUBINH_TYPE, (byte)type);
            List<Short>cardList = new ArrayList<>();
            for (int i = 0; i < cards.size(); i++) {
                cardList.add((short)cards.get(i).getId());
            }

            m.putShortArray(SFSKey.ARRAY_INFOR_CARD, cardList);
        } catch (Exception e) {
            log.error("Mau binh MessageFactory.makeSendCardMessage error: ", e);
        }
        return m;
    }

    public static Message makeFinishMessage(String userID) {
        Message m = createMauBinhMessage(MauBinhCommand.FINISH);
        try {
            m.putUtfString(SFSKey.USER_ID, userID);
        } catch (Exception e) {
            log.error("Mau binh MessageFactory.makeFinishMessage error: ", e);
        }
        
        return m;
    }
    
    public static Message makeInterfaceErrorMessage(byte command, String errorMessage) {
        Message m = createMauBinhMessage(MauBinhCommand.INTERFACE_ERROR);
        try {
            m.putByte(MauBinhCommand.ERR_COMMAND, command);
            m.putUtfString(MauBinhCommand.ERR_MESSAGE, errorMessage);
        } catch (Exception e) {
            log.error("Mau binh MessageFactory.makeInterfaceErrorMessage error: ", e);
        }
        
        return m;
    }
    
    public static Message makeUserMoneyInfoMessage(String userId, double money){
        Message m = createMauBinhMessage(MauBinhCommand.USER_MONEY_INFO);
        try {
            m.putUtfString(SFSKey.USER_ID, userId);
            m.putDouble(SFSKey.MONEY_USER, money);
        } catch (Exception e) {
            log.error("Mau binh MessageFactory.makeUserMoneyInfoMessage error: ", e);
        }
        
        return m;
    }
    /**
     * Gửi thông tin sập hầm về cho user
     * @param player
     * @return
     */
    public static Message decriptionSapHamMessage(Player player) {
        if (player == null) {
            return null;
        }
        Message m = createMauBinhMessage(MauBinhCommand.DEC_SAP_HAM);
        try {
            //gửi về thông tin sập hầm
            m.putUtfString(SFSKey.STRING_MESSAGE_VI, player.getDecriptionSapHam());
        } catch (Exception ex) {
            log.error("Mau binh MessageFactory.decriptionSapHamMessage error: ", ex);
        }

        return m;
    }
    public static Message makeResultMessage(int playerIndex, Player[] players,
            double[] winMoney, int[] winChi, Result[][] result) {
        if (players == null || winChi == null || result == null
                || playerIndex < 0 || playerIndex >= players.length
                || players[playerIndex].getUser() == null) {
            return null;
        }
        
        Message m = createMauBinhMessage(SFSAction.RESULT);
        ISFSArray resultList = new SFSArray();
        SFSObject temp = null;
        try {
            // Write self info:
            // 1. user ID.
            // 2. win money.
            // 3. sum of win chi.
            // 4. maubinh or failed type.
            temp = new SFSObject();
            temp.putUtfString(SFSKey.USER_ID,Utils.getIdDBOfUser(players[playerIndex].getUser()));
            temp.putDouble(MauBinhCommand.WIN_MONEY, winMoney[playerIndex]);
            temp.putInt(MauBinhCommand.WIN_CHI, winChi[playerIndex]);
            int type = players[playerIndex].getCards().IsFailedArrangement()
                    ? MauBinhConfig.FAILED_ARRANGEMENT : players[playerIndex].getCards().getMauBinhType();
//            if (players[playerIndex].isTimeOut()) {
//                type = MauBinhConfig.FAILED_ARRANGEMENT;
//            }
            temp.putByte(MauBinhCommand.MAUBINH_TYPE, (byte)type);
            resultList.addSFSObject(temp);
            // Write result info with opponient.
            for (int i = 0; i < players.length; i++) {
                if (i == playerIndex || players[i].getUser() == null) {
                    continue;
                }

                // Write info of opponient:
                // 1. user ID.
                // 2. win money.
                // 3. sum of win chi.
                // 4. card list.
                // 5. maubinh or failed type.
                temp = new SFSObject();
                temp.putUtfString(SFSKey.USER_ID,Utils.getIdDBOfUser(players[i].getUser()));
                temp.putDouble(MauBinhCommand.WIN_MONEY, winMoney[i]);
                temp.putInt(MauBinhCommand.WIN_CHI, winChi[i]);
                List<Card> cardList = players[i].getCards().getArrangeCards();
                if (cardList == null) {
                    cardList = players[i].getCards().getCards();
                }
                
                List<Short> cards = new ArrayList<>();
                for (int j = 0; j < cardList.size(); j ++) {
                    cards.add((short) cardList.get(j).getId());
                }

                temp.putShortArray(SFSKey.ARRAY_INFOR_CARD, cards);
                
                type = players[i].getCards().IsFailedArrangement()
                        ? MauBinhConfig.FAILED_ARRANGEMENT : players[i].getCards().getMauBinhType();
                temp.putByte(MauBinhCommand.MAUBINH_TYPE, (byte)type);
                // Result when compare with opponient.
                temp.putInt(MauBinhCommand.WIN_CHI_MAUBINH, result[playerIndex][i].getWinChiMauBinh());
                temp.putInt(MauBinhCommand.WIN_CHI_01, result[playerIndex][i].getWinChi01());
                temp.putInt(MauBinhCommand.WIN_CHI_02, result[playerIndex][i].getWinChi02());
                temp.putInt(MauBinhCommand.WIN_CHI_03, result[playerIndex][i].getWinChi03());
                temp.putInt(MauBinhCommand.MULTI_K, result[playerIndex][i].getMultiK());
                temp.putInt(MauBinhCommand.WIN_CHI_ACE, result[playerIndex][i].getWinChiAce());
                resultList.addSFSObject(temp);
            }

            m.putSFSArray(SFSKey.ARRAY_SFS, resultList);
        } catch (Exception ex) {
            log.error("Mau binh MessageFactory.makeResultMessage error: ", ex);
        }
        
        return m;
    }
    
    public static Message makeAutoArrangeResultMessage(List<Card> cards) {
        Message m = createMauBinhMessage(MauBinhCommand.AUTO_ARRANGE);
        try {
            List<Short>cardList = new ArrayList<>();
            for (int i = 0; i < cards.size(); i++) {
                cardList.add((short)cards.get(i).getId());
            }

            m.putShortArray(SFSKey.ARRAY_INFOR_CARD, cardList);
        } catch (Exception ex) {
            log.error("Mau binh MessageFactory.makeAutoArrangeResultMessage error: ", ex);
        }
        
        return m;
    }
    
    public static Message makeSortByOrderMessage(List<Card> cards) {
        Message m = createMauBinhMessage(MauBinhCommand.SORT_BY_ORDER);
        try {
            List<Short>cardList = new ArrayList<>();
            for (int i = 0; i < cards.size(); i++) {
                cardList.add((short)cards.get(i).getId());
            }

            m.putShortArray(SFSKey.ARRAY_INFOR_CARD, cardList);
        } catch (Exception ex) {
            log.error("Mau binh MessageFactory.makeSortByOrderMessage error: ", ex);
        }
        
        return m;
    }
    
    public static Message makeSortByTypeMessage(List<Card> cards) {
        Message m = createMauBinhMessage(MauBinhCommand.SORT_BY_TYPE);
        try {
            List<Short>cardList = new ArrayList<>();
            for (int i = 0; i < cards.size(); i++) {
                cardList.add((short)cards.get(i).getId());
            }

            m.putShortArray(SFSKey.ARRAY_INFOR_CARD, cardList);
        } catch (Exception ex) {
            log.error("Mau binh MessageFactory.makeSortByTypeMessage error: ", ex);
        }
        
        return m;
    }
    
    public static Message makeErrorMessage(String errorMessage) {
        Message m = createMauBinhMessage(SFSAction.MESSAGE_ERROR);
        try {
            m.putUtfString(MauBinhCommand.ERR_MESSAGE, errorMessage);
            m.putByte(SFSKey.TIME_EXIT, (byte) 0);
        } catch (Exception e) {
            log.error("Mau binh MessageFactory.makeErrorMessage error: ", e);
        }
        
        return m;
    }
    
    public static Message makeInGameInforMessage(boolean isFinish, List<Card> cards, byte limitTime, byte restTime, byte maubinhType, Player[] players) {
        Message m = createMauBinhMessage(SFSAction.ON_RETURN_GAME);
        try {
            if (cards == null || cards.isEmpty()) {
                return null;
            }
            
            int temp = isFinish ? MauBinhConfig.STATUS_FINISH : MauBinhConfig.STATUS_NOT_FINISH;
            m.putByte(MauBinhCommand.GAME_STATUS, (byte)temp);
            List<Short>cardList = new ArrayList<>();
            for (int i = 0; i < cards.size(); i++) {
                cardList.add((short)cards.get(i).getId());
            }

            m.putShortArray(SFSKey.ARRAY_INFOR_CARD, cardList);
            
            SFSArray arr= new SFSArray();
            for(int i = 0; i < players.length; i++) {
                if (players[i].getUser() == null) {
                    continue;
                }
                SFSObject ob= new SFSObject();
                ob.putUtfString(SFSKey.USER_ID, Utils.getIdDBOfUser(players[i].getUser()));
                ob.putBool(MauBinhCommand.IS_FINISHED, players[i].isFinish());
                arr.addSFSObject(ob);
             }
            
            m.putSFSArray(SFSKey.ARRAY_SFS, arr);
            m.putByte(MauBinhCommand.LIMIT_TIME, limitTime);
            m.putByte(MauBinhCommand.REST_TIME, restTime);
            m.putByte(MauBinhCommand.MAUBINH_TYPE, maubinhType);
        } catch (Exception e) {
            log.error("Mau binh MessageFactory.makeInGameInforMessage error: ", e);
        }
        
        return m;
    }
    
    public static Message makeInGameInforMessageForViewer(byte limitTime, byte restTime) {
        Message m = createMauBinhMessage(SFSAction.ON_RETURN_GAME);
        try {
            m.putByte(MauBinhCommand.GAME_STATUS, (byte)MauBinhConfig.STATUS_WAIT);
            m.putByte(MauBinhCommand.LIMIT_TIME, limitTime);
            m.putByte(MauBinhCommand.REST_TIME, restTime);
        } catch (Exception e) {
            log.error("Mau binh MessageFactory.makeInGameInforMessageForViewer error: ", e);
        }
        
        return m;
    }
    
    private static Message createMauBinhMessage(int command) {
        Message m = new Message();
        m.putInt(SFSKey.ACTION_INGAME, command);
        return m;
    }
    
    public static Message getBoardInfoMessage(byte autoStartTime) throws IOException{
        Message m = createMauBinhMessage(SFSAction.BOARD_INFO);
        m.putByte(MauBinhCommand.AUTO_START_TIME, autoStartTime);
        return m;
    }
}
