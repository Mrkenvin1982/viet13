/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.vn.game.xito.playerstate;

import com.smartfoxserver.v2.entities.User;
import com.smartfoxserver.v2.entities.data.SFSObject;
import game.command.XiToCommand;
import game.vn.game.xito.XiToController;
import game.vn.game.xito.lang.XiToLanguage;
import game.vn.game.xito.object.Round;
import game.vn.game.xito.object.XiToPlayer;
import game.vn.util.CommonMoneyReasonUtils;
import game.vn.util.GlobalsUtil;
import game.vn.util.Utils;
import java.math.BigDecimal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *Van choi da bat dau, nguoi choi co cac hanh dong cu the theo luot cua minh
 * @author tuanp
 */
public class PlayerTurnState implements IPlayerState {

    private final XiToPlayer player;
    private final XiToController xiToGame;
    private static final Logger log = LoggerFactory.getLogger(PlayerTurnState.class.getName());

    public PlayerTurnState(XiToPlayer currentPlayer) {
        this.player = currentPlayer;
        this.xiToGame = currentPlayer.getGameController();
    }
    @Override
    public void betting(BigDecimal stack) throws Exception {
        stack = Utils.getRoundBigDecimal(stack);
        //kiểm tra nết bet nhiều hơn stack đang có
        if (stack.compareTo(player.getStack()) > 0) {
            this.xiToGame.sendToastMessage(XiToLanguage.getMessage(
                    XiToLanguage.NOT_ENOUGH_STACK_TO_BET,this.xiToGame.getLocaleOfUser(this.player.getUser())), this.player.getUser(), 3);
            return;
        }
        //trừ vô stack
        player.minusStack(stack);
        // chuyển trạng thái sang wait
        player.setState(player.getWaitToNextTurnState());
        
        sendBetMessage(stack.doubleValue());
        this.xiToGame.getCurrentRound().updateLastAction(this.xiToGame.getIdDBOfUser(player.getUser()), XiToPlayer.BET);
        String textLog=String.format("Tố %s, tẩy còn lại: %s.", Utils.formatNumber(stack),Utils.formatNumber(player.getStack()));
        this.xiToGame.getLogger(this.player.getUser().getName()+", "+textLog);
        if (!xiToGame.updateMoney(player.getUser(), stack.negate(),
                CommonMoneyReasonUtils.BET, "",BigDecimal.ZERO,null)) {
            log.error("update money was failed when user is betting");
            log.error("user:" + player.getUser().getName());
            log.error("-stack:" + -stack.doubleValue());
            log.error("tẩy còn lại:" + player.getStack());
            throw new Exception("update money was failed when user is betting: "+textLog);
        }
        //nếu ko đủ stack để chơi tiếp thì set là all-in
        checkAndSetAllIn();
        processNextTurnMessage(XiToPlayer.BET);
    }

    @Override
    public void raising(BigDecimal stack) throws Exception {
        stack = Utils.getRoundBigDecimal(stack);
        User user = this.player.getUser();
        //kiểm tra nết bet nhiều hơn stack đang có
        if (stack.compareTo(player.getStack()) > 0) {
            this.xiToGame.sendToastMessage(XiToLanguage.getMessage(
                    XiToLanguage.NOT_ENOUGH_STACK_TO_RAISE,this.xiToGame.getLocaleOfUser(user)), user, 3);
            return;
        }
        if (stack.compareTo(xiToGame.getMinRaise()) < 0) {
            this.xiToGame.sendToastMessage(XiToLanguage.getMessage(
                    XiToLanguage.INVALID_RAISE_STACK,this.xiToGame.getLocaleOfUser(user)), user, 3);
            return;
        }
        //trừ vô stack
        player.minusStack(stack);
        player.setState(player.getWaitToNextTurnState());
        sendRaiseMessage(stack.doubleValue());
        this.xiToGame.getCurrentRound().updateLastAction(this.xiToGame.getIdDBOfUser(player.getUser()), XiToPlayer.RAISE);
        String textLog=String.format("Tố thêm %s, tẩy còn lại: %s.", Utils.formatNumber(stack),Utils.formatNumber(player.getStack()));
        this.xiToGame.getLogger(user.getName()+", "+textLog);
        if (!xiToGame.updateMoney(player.getUser(), stack.negate(),
                CommonMoneyReasonUtils.RAISE, "",BigDecimal.ZERO, null)) {
            log.error("update money was failed when user is raising");
            log.error("user:" + player.getUser().getName());
            log.error("-stack:" + -stack.doubleValue());
            log.error("tẩy còn lại:" + player.getStack());
            throw new Exception("update money was failed when user is raising: "+textLog);
        }
        //nếu ko đủ stack để chơi tiếp thì set là all-in
        checkAndSetAllIn();
        processNextTurnMessage(XiToPlayer.RAISE);
    }

    @Override
    public void calling() throws Exception {
        Round currentRound = xiToGame.getCurrentRound();
        //số stack phải theo
        BigDecimal callStack = Utils.subtract(currentRound.getMaxBetStackOfPlayer(), currentRound.getStack(player.getIdBDUSer()));
        //trừ stack của người chơi
        callStack = callStack.min(player.getStack());
        player.minusStack(callStack);
        player.setState(player.getWaitToNextTurnState());
        
        //gửi message call
        sendCallMessage(callStack.doubleValue());
        this.xiToGame.getCurrentRound().updateLastAction(this.xiToGame.getIdDBOfUser(player.getUser()), XiToPlayer.CALL);
        String textLog=String.format("Theo lượt tố %s, tẩy còn lại: %s.", Utils.formatNumber(callStack),Utils.formatNumber(player.getStack()));
        this.xiToGame.getLogger(this.player.getUser().getName()+", "+textLog);
        //trừ win
        if (!xiToGame.updateMoney(player.getUser(), callStack.negate(),
                CommonMoneyReasonUtils.CALL, "",BigDecimal.ZERO, null )) {
            log.error("update money was failed when user is calling: ");
            log.error("user: " + this.player.getUser().getName());
            log.error("-callStack: " + callStack);
            log.error("tẩy còn lại: " + player.getStack());
            throw new Exception("update money was failed when user is calling:"+textLog);
        }
        //nếu ko đủ stack để chơi tiếp thì set là all-in
        checkAndSetAllIn();

        processNextTurnMessage(XiToPlayer.CALL);
    }

    @Override
    public void allIn() throws Exception {
        //tru stack cua user
        BigDecimal allinStack = player.getStack();
        player.minusStack(allinStack);
        sendAllInMessage(allinStack.doubleValue());

        this.xiToGame.getCurrentRound().updateLastAction(this.xiToGame.getIdDBOfUser(player.getUser()), XiToPlayer.ALLIN);
        xiToGame.setInturn(this.player.getUser(), false);
        player.setState(player.getAllInState());
        this.xiToGame.getLogger(this.player.getUser().getName()+", "+"Tố hết " + Utils.formatNumber(allinStack));
        //trừ win
        if (!xiToGame.updateMoney(player.getUser(), allinStack.negate(),
                CommonMoneyReasonUtils.ALL_IN, "",BigDecimal.ZERO,null)) {
            log.error("update money was failed when user is allin: ");
            log.error("user: " + this.player.getUser().getName());
            log.error("-callStack: " + -allinStack.doubleValue());
            log.error("tẩy còn lại: " + player.getStack());
            throw new Exception("update money was failed when user is allin");
        }
        processNextTurnMessage(XiToPlayer.ALLIN);
    }

    @Override
    public void checking() throws Exception {
        Round currentRound = this.xiToGame.getCurrentRound();
        this.xiToGame.getLogger(this.player.getUser().getName()+", "+" CHECKING " );
        //gui message check
        sendCheckMessage();
        //coi như nó tố 0 đồng
        currentRound.addStack(player.getIdBDUSer(), BigDecimal.ZERO);
        player.setState(player.getWaitToNextTurnState());
        currentRound.updateLastAction(this.xiToGame.getIdDBOfUser(player.getUser()), XiToPlayer.CHECK);
        xiToGame.updateLogGameForUser(this.player.getUser(), CommonMoneyReasonUtils.CHECK, null);
        //chuyển lượt cho người chơi tiếp theo
        processNextTurnMessage(XiToPlayer.CHECK);
    }

    @Override
    public void folding() throws Exception {
        this.xiToGame.getLogger(this.player.getUser().getName()+", "+" FOLDING " );
        xiToGame.incrCountFold();
        sendFoldMessage();
        this.xiToGame.getCurrentRound().updateLastAction(this.xiToGame.getIdDBOfUser(player.getUser()), XiToPlayer.FOLD);
        
        xiToGame.setInturn(this.player.getUser(), false);
        xiToGame.updateLogGameForUser(this.player.getUser(), CommonMoneyReasonUtils.FOLD, null);
        //chuyển lượt cho người chơi tiếp theo
        processNextTurnMessage(XiToPlayer.FOLD);
    }
       /**
     * Gửi message úp bỏ bài cho cả bàn
     *
     * @throws Exception
     */
    private void sendFoldMessage() throws Exception {
       
        String descVi = XiToLanguage.getMessage(XiToLanguage.FOLD, GlobalsUtil.VIETNAMESE_LOCALE);
        SFSObject mVi = player.getPlayerMessageFactory()
                .getCommonMessage(XiToCommand.FOLD, descVi, 0);
        
         String descEn = XiToLanguage.getMessage(XiToLanguage.FOLD, GlobalsUtil.ENGLISH_LOCALE);
        SFSObject mEn = player.getPlayerMessageFactory()
                .getCommonMessage(XiToCommand.FOLD, descEn, 0);
        
        String descZh = XiToLanguage.getMessage(XiToLanguage.FOLD, GlobalsUtil.CHINESE_LOCALE);
        SFSObject mZh = player.getPlayerMessageFactory()
                .getCommonMessage(XiToCommand.FOLD, descZh, 0);
        
        xiToGame.sendToAllWithLocale(mEn, mVi, mZh);
    }

    /**
     * Gửi message all in cho cả bàn
     *
     * @throws Exception
     */
    private void sendAllInMessage(double allinStack) throws Exception {
        String descEn = String.format(XiToLanguage.getMessage(XiToLanguage.ALL_IN, GlobalsUtil.ENGLISH_LOCALE),
                Utils.formatNumber(allinStack));
        String descVi = String.format(XiToLanguage.getMessage(XiToLanguage.ALL_IN, GlobalsUtil.VIETNAMESE_LOCALE),
                Utils.formatNumber(allinStack));
        SFSObject mEn = player.getPlayerMessageFactory().getCommonMessage(
                XiToCommand.ALL_IN, descEn, allinStack);
        SFSObject mVi = player.getPlayerMessageFactory().getCommonMessage(
                XiToCommand.ALL_IN, descVi, allinStack);
        
        String descZh =String.format(XiToLanguage.getMessage(XiToLanguage.ALL_IN, GlobalsUtil.CHINESE_LOCALE),
                Utils.formatNumber(allinStack));
        SFSObject mZh = player.getPlayerMessageFactory()
                .getCommonMessage(XiToCommand.ALL_IN, descZh, allinStack);
        
        xiToGame.sendToAllWithLocale(mEn, mVi, mZh);
    }

    /**
     * Gửi message bet stack cho cả bàn
     *
     * @throws Exception
     */
    private void sendBetMessage(double stack) throws Exception {
        String descEn = String.format(XiToLanguage.getMessage(XiToLanguage.BET, GlobalsUtil.ENGLISH_LOCALE),Utils.formatNumber(stack));
        String descVi = String.format(XiToLanguage.getMessage(XiToLanguage.BET, GlobalsUtil.VIETNAMESE_LOCALE),Utils.formatNumber(stack));
        SFSObject mEn = player.getPlayerMessageFactory().getCommonMessage(
                XiToCommand.BET, descEn, stack);
        SFSObject mVi = player.getPlayerMessageFactory().getCommonMessage(
                XiToCommand.BET, descVi, stack);
        
        String descZh = String.format(XiToLanguage.getMessage(XiToLanguage.BET, GlobalsUtil.CHINESE_LOCALE),Utils.formatNumber(stack));
        SFSObject mZh = player.getPlayerMessageFactory()
                .getCommonMessage(XiToCommand.BET, descZh, stack);
        
        xiToGame.sendToAllWithLocale(mEn, mVi, mZh);
    }

    /**
     * Gửi message raise stack cho cả bàn
     *
     * @throws Exception
     */
    private void sendRaiseMessage(double stack) throws Exception {
        String descVi = String.format(XiToLanguage.getMessage(XiToLanguage.RAISE, GlobalsUtil.VIETNAMESE_LOCALE), Utils.formatNumber(stack));
        String descEn = String.format(XiToLanguage.getMessage(XiToLanguage.RAISE, GlobalsUtil.ENGLISH_LOCALE), Utils.formatNumber(stack));
        SFSObject mVi = player.getPlayerMessageFactory()
                .getCommonMessage(XiToCommand.RAISE, descVi, stack);
        SFSObject mEn = player.getPlayerMessageFactory()
                .getCommonMessage(XiToCommand.RAISE, descEn, stack);
        
        String descZh = String.format(XiToLanguage.getMessage(XiToLanguage.RAISE, GlobalsUtil.CHINESE_LOCALE), Utils.formatNumber(stack));
        SFSObject mZh = player.getPlayerMessageFactory()
                .getCommonMessage(XiToCommand.RAISE, descZh, stack);
        
        xiToGame.sendToAllWithLocale(mEn, mVi, mZh);
    }

    /**
     * Gửi message bỏ lượt tố cho cả bàn
     *
     * @throws Exception
     */
    private void sendCheckMessage() throws Exception {
        String descEn = XiToLanguage.getMessage(XiToLanguage.CHECK,
                GlobalsUtil.ENGLISH_LOCALE);
        String descVi = XiToLanguage.getMessage(XiToLanguage.CHECK,
                GlobalsUtil.VIETNAMESE_LOCALE);
        SFSObject mEn = player.getPlayerMessageFactory()
                .getCommonMessage(XiToCommand.CHECK, descEn, 0);
        SFSObject mVi = player.getPlayerMessageFactory()
                .getCommonMessage(XiToCommand.CHECK, descVi, 0);
        
        String descZh = XiToLanguage.getMessage(XiToLanguage.CHECK, GlobalsUtil.CHINESE_LOCALE);
        SFSObject mZh = player.getPlayerMessageFactory()
                .getCommonMessage(XiToCommand.CHECK, descZh, 0);
        
        xiToGame.sendToAllWithLocale(mEn, mVi, mZh);
    }

    /**
     * Gửi message theo lượt tố cho cả bàn
     *
     * @throws Exception
     */
    private void sendCallMessage(double callStack) throws Exception {
        String descVi = String.format(XiToLanguage.getMessage(XiToLanguage.CALL, GlobalsUtil.VIETNAMESE_LOCALE),Utils.formatNumber(callStack));
        String descEn = String.format(XiToLanguage.getMessage(XiToLanguage.CALL,
                GlobalsUtil.ENGLISH_LOCALE),Utils.formatNumber(callStack));
        SFSObject mEn = player.getPlayerMessageFactory()
                .getCommonMessage(XiToCommand.CALL, descEn,
                        callStack);
        SFSObject mVi = player.getPlayerMessageFactory()
                .getCommonMessage(XiToCommand.CALL, descVi,
                        callStack);
        
        String descZh = String.format(XiToLanguage.getMessage(XiToLanguage.CALL, GlobalsUtil.CHINESE_LOCALE),Utils.formatNumber(callStack));
        SFSObject mZh = player.getPlayerMessageFactory()
                .getCommonMessage(XiToCommand.CALL, descZh, callStack);
        
        xiToGame.sendToAllWithLocale(mEn, mVi, mZh);
    }

    /**
     * chuyển lượt cho người chơi tiếp theo <br />
     * gửi message next turn cho cả bàn
     *
     * @throws Exception
     */
    private void processNextTurnMessage(byte action) throws Exception {
        //chuyển lượt cho người chơi tiếp theo
        xiToGame.nextTurn();
        //gửi message next turn cho cả bàn
        User currentUser = xiToGame.getCurrentPlayer();
        if (currentUser == null) {
            return;
        }

        if (xiToGame.isFinishRound()) {
            //player fold thì check stop game rồi mới chia bài, xử lý bên xì tố game
            //các trường hợp còn lại chia bài rồi mới xác định người bắt đầu vòng mới
            if (action == XiToPlayer.FOLD && xiToGame.getInTurnPlayers().size() <= 1) {
                if (!xiToGame.isAllFolded()) {
                    //Kết thúc ván mà chưa chia ra đủ 5 lá
                    xiToGame.dealRemainCard();
                }
                xiToGame.stopGame();
                return;
            } else if (xiToGame.getCountRemainDealCard() > 0) {
                xiToGame.dealFlogCard();
            }

            xiToGame.sendUpdatePotMessage();
            determineBeginnerOfRound();
            currentUser = xiToGame.getCurrentPlayer();
        }

        //trường hợp chỉ còn 1 nguoi chơi thì kết thúc game
        if (xiToGame.getInTurnPlayers().size() <= 1 && !player.isAllIn()
                && !player.isTurn()) {
            return;
        }
        if (xiToGame.getRounds().isEmpty()) {
            return;
        }
        XiToPlayer nextPlayer = xiToGame.getXiToPlayer(xiToGame.getIdDBOfUser(currentUser));
        nextPlayer.setState(nextPlayer.getTurnState());
        setActions(action, nextPlayer);
        nextPlayer.sendNextTurnMessage();
    }

    private void setActions(byte lastAction, XiToPlayer currentPlayer) throws Exception {
        currentPlayer.getActions().clear();
        switch (lastAction) {
            case XiToPlayer.ALLIN:
            case XiToPlayer.BET:
            case XiToPlayer.CALL:
            case XiToPlayer.RAISE:
            case XiToPlayer.FOLD:
                if (xiToGame.getRounds().size() == 1||xiToGame.isFinishRound() || xiToGame.getCurrentRound().getStack().compareTo(BigDecimal.ZERO) == 0) {
                    currentPlayer.addAction(XiToPlayer.CHECK);
                    if (currentPlayer.getStack().compareTo(xiToGame.getMoney()) >= 0) {
                        currentPlayer.addAction(XiToPlayer.BET);
                    }
                    currentPlayer.addAction(XiToPlayer.ALLIN);
                } else {
                    currentPlayer.addAction(XiToPlayer.ALLIN);
                    BigDecimal maxBet = Utils.add(xiToGame.getCurrentRound().getMaxBetStackOfPlayer(), xiToGame.getMoney());
                    if (currentPlayer.getStack().compareTo(maxBet) >=  0 
                            && xiToGame.getInTurnPlayers().size() > 1) {
                        currentPlayer.addAction(XiToPlayer.RAISE);
                    }
                    BigDecimal callStack = Utils.add(xiToGame.getCurrentRound().getStack(currentPlayer.getIdBDUSer()), currentPlayer.getStack());
                               callStack = Utils.subtract(callStack, xiToGame.getCurrentRound().getMaxBetStackOfPlayer());
                 
                    //call:số tiền cược player phải bằng hoặc lớn hơn lần bet cuối cùng
                    if (callStack.signum() >=0) {
                        currentPlayer.addAction(XiToPlayer.CALL);
                    }
                }
                break;
            case XiToPlayer.CHECK:
                if (xiToGame.getCurrentRound().getStack().signum() > 0) {//có người đã tố              
                    currentPlayer.addAction(XiToPlayer.ALLIN);
                    if (currentPlayer.getStack().compareTo(xiToGame.getMinRaise()) >= 0) {
                        currentPlayer.addAction(XiToPlayer.RAISE);
                    }
                    if (currentPlayer.getStack().compareTo(xiToGame.getLastBetStack()) >= 0) {
                         currentPlayer.addAction(XiToPlayer.CALL);
                    }
                } else {
                    currentPlayer.addAction(XiToPlayer.CHECK);
                    if (currentPlayer.getStack().compareTo(xiToGame.getMoney()) >= 0) {
                        currentPlayer.addAction(XiToPlayer.BET);
                    }
                    currentPlayer.addAction(XiToPlayer.ALLIN);
                }
        }
    }

    /**
     * Kiểm tra nếu ko đủ stack để chơi nữa thì set inturn = false và state =
     * allin
     */
    private void checkAndSetAllIn() {
        if (player.getStack().signum() == 0) {
            xiToGame.setInturn(this.player.getUser(), false);
            player.setState(player.getAllInState());
        }
    }

    /**
     * nếu là kết thúc vòng thì phải xác định lại người bắt đầu vòng mới
     *
     * @throws Exception
     */
    private void determineBeginnerOfRound() throws Exception {
        XiToPlayer beginNewRound = null;
        for (User corePlayer : xiToGame.getInTurnPlayers()) {
            XiToPlayer p = xiToGame.getXiToPlayer(xiToGame.getIdDBOfUser(corePlayer));
            if(p==null){
                continue;
            }

            if (p.getStack().signum() <= 0) {
                continue;
            }

            if (beginNewRound == null) {
                beginNewRound = p;
                continue;
            }
            if (p.getResultShowedCards().getValue() > beginNewRound.getResultShowedCards().getValue()) {
                beginNewRound = p;
            } else if (p.getResultShowedCards().getValue()
                    == beginNewRound.getResultShowedCards().getValue()) {
                //trường hợp 2 thằng đều mậu thầu, lấy lá sau cùng dc chia để so sánh id
//                if (p.getResultShowedCards().getValue() == XiToCardUtil.HIGH_CARD_VALUE) {
//                    if (p.getLastHoldCard().getId() > beginNewRound.getLastHoldCard().getId()) {
//                        beginNewRound = p;
//                        continue;
//                    }
//                }
                if (p.getResultShowedCards().getHighestCard().getId()
                        > beginNewRound.getResultShowedCards().getHighestCard().getId()) {
                    beginNewRound = p;
                }
            }
        }

        if (beginNewRound != null && beginNewRound.getUser() != null) {
            xiToGame.setCurrentPlayer(beginNewRound.getUser());
        }
    }
}
