/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package game.vn.game.lieng.playerstate;

import com.smartfoxserver.v2.entities.User;
import game.vn.game.lieng.LiengController;
import game.vn.game.lieng.lang.LiengLanguage;
import game.vn.game.lieng.object.LiengPlayer;
import game.vn.util.CommonMoneyReasonUtils;
import game.vn.util.Utils;
import java.math.BigDecimal;
import java.util.ArrayList;

/**
 *
 * @author tuanp
 */
public class BettingPlayerState implements ILiengPlayerState {

    private final LiengPlayer player;
    private final LiengController game;

    public BettingPlayerState(LiengPlayer player) {
        this.player = player;
        this.game = player.getGame();
    }

    @Override
    public void bet(BigDecimal betMoney) throws Exception {
        //betMoney: bao gồm tiền call va bet
        //temp1 là tiền cần để theo
        BigDecimal callMoney =Utils.subtract(game.getLastBet(), player.getBetMoney());
        //tempBet số tiền bet của user
        BigDecimal tempBet = Utils.subtract(betMoney,callMoney);
        //ko đủ win để bet
        if (tempBet.compareTo(game.getMoney()) < 0 || this.game.getMoneyFromUser(player.getUser()).compareTo(betMoney)< 0) {
            String error = String.format(LiengLanguage.getMessage(LiengLanguage.NOT_ENOUGH_WIN_TO_BET,this.game.getLocaleOfUser(player.getUser())), 
                    this.game.getCurrencyName(this.game.getLocaleOfUser(player.getUser())));
            this.game.sendToastMessage(error, player.getUser(), 3);
            return;
        }

        if (game.getMoney().signum() !=0 &&!game.updateMoney(player.getUser(), betMoney.negate(), CommonMoneyReasonUtils.BET, "",BigDecimal.ZERO,null)) {
            throw new Exception("bet update money error");
        }
        game.setLastBet(player.getBetMoney());
        game.getCurrentRound().addBetMoney(this.game.getIdDBOfUser(player.getUser()), betMoney);
        // chuyển trạng thái sang waiting
        player.setState(player.getWaitingState());
        checkAndSetAllIn();   
        player.sendBetMessage(callMoney.doubleValue(), tempBet.doubleValue());
        processNextTurnMessage(LiengPlayer.BET);        
    }

    @Override
    public void call() throws Exception {
        BigDecimal callMoney = Utils.subtract(game.getLastBet(), player.getBetMoney());
        callMoney = this.game.getMoneyFromUser(player.getUser()).min(callMoney);
        if (game.getMoney().signum()!=0 &&!game.updateMoney(player.getUser(), callMoney.negate(),
                CommonMoneyReasonUtils.CALL, "",BigDecimal.ZERO,null)) {
            throw new Exception("call update money error: " + callMoney);
        }
        game.getCurrentRound().addBetMoney(this.game.getIdDBOfUser(player.getUser()), callMoney);
        player.setState(player.getWaitingState());
        checkAndSetAllIn();
        player.sendCallMessage(callMoney.doubleValue());
        processNextTurnMessage(LiengPlayer.CALL);
    }

    @Override
    public void fold() throws Exception {
        game.incrCountFold();
        player.sendFoldMessage();
        player.setLastActionDesc(LiengLanguage.getMessage(LiengLanguage.FOLD,this.game.getLocaleOfUser(player.getUser())));
        game.setInturn(player.getUser(), false);
        player.setState(player.getFoldedState());
        processNextTurnMessage(LiengPlayer.FOLD);
        game.updateLogGameForUser(player.getUser(),CommonMoneyReasonUtils.FOLD,new ArrayList<Short>());
    }

    @Override
    public void check() throws Exception {
        player.sendCheckMessage();
        //coi như nó tố 0 win
        player.setState(player.getWaitingState());
        game.getCurrentRound().addBetMoney(this.game.getIdDBOfUser(player.getUser()), BigDecimal.ZERO);
        processNextTurnMessage(LiengPlayer.CHECK);
        game.updateLogGameForUser(player.getUser(),CommonMoneyReasonUtils.CHECK,null);
    }

    private void processNextTurnMessage(byte action) throws Exception {
        // lưu lại hành động sau cùng của người chơi 
        player.setLastAction(action);
        //chuyển lượt cho người chơi tiếp theo
        game.nextTurn();
        //gửi message next turn cho cả bàn
        setNextUser(game.getCurrentPlayer(),action);      
    }
    /**
     * Xet user tiep theo va gui message cho cả bàn
     * @param user
     * @param action
     * @throws Exception 
     */
    private void setNextUser(User user,byte action) throws Exception{
        //gửi message next turn cho cả bàn
        if (user == null) {
            return;
        }
        LiengPlayer nextPlayer = game.getLiengPlayer(this.game.getIdDBOfUser(user));
        if(nextPlayer==null){
            return;
        }
        nextPlayer.setState(nextPlayer.getBettingState());
        setActions(action, nextPlayer);
        nextPlayer.sendNextTurnMessage();  
    }
    private void setActions(byte lastAction, LiengPlayer currentPlayer) throws Exception {
        currentPlayer.getActions().clear();
        switch (lastAction) {
            case LiengPlayer.CALL:
            case LiengPlayer.BET:
                currentPlayer.addAction(LiengPlayer.CALL);
                if (game.isCanBet(currentPlayer)) {
                    currentPlayer.addAction(LiengPlayer.BET);
                }
                break;
            case LiengPlayer.FOLD:
                if (game.isCanCheck()) {
                    currentPlayer.addAction(LiengPlayer.CHECK);
                } else if (game.getTotalBetMoney().signum() > 0) {
                    currentPlayer.addAction(LiengPlayer.CALL);
                }
                if (game.isCanBet(currentPlayer)) {
                    currentPlayer.addAction(LiengPlayer.BET);
                }
                break;
            case LiengPlayer.CHECK:
                currentPlayer.addAction(LiengPlayer.CHECK);
                if (game.isCanBet(currentPlayer)) {
                    currentPlayer.addAction(LiengPlayer.BET);
                }
        }
    }

    /**
     * Kiểm tra nếu ko đủ win để chơi nữa thì set inturn = false và state =
     * allin
     */
    private void checkAndSetAllIn() {
        if (game.getMoneyFromUser(player.getUser()).signum() == 0) {
            game.setInturn(player.getUser(), false);
            player.setState(player.getAllInState());
        }
    }
     /**
     * Gọi khi user rời game
     * Fold khi user rời game
     * Lượt user tiếp theo
     * 
     * @param nextUser
     * @throws Exception 
     */
    public void foldForLeaver(User nextUser) throws Exception {
        game.incrCountFold();
        player.sendFoldMessage();
        player.setLastActionDesc(LiengLanguage.getMessage(LiengLanguage.FOLD,game.getLocaleOfUser(player.getUser())));
        
        player.setState(player.getFoldedState());
        player.setLastAction(LiengPlayer.FOLD);
        game.setCurrentPlayer(nextUser);
        setNextUser(nextUser,LiengPlayer.FOLD);
        
        game.updateLogGameForUser(player.getUser(),CommonMoneyReasonUtils.BO_CUOC,null);
    }
}
