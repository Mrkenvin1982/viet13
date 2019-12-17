/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.vn.game.lieng.object;

import com.smartfoxserver.v2.entities.User;
import com.smartfoxserver.v2.entities.data.SFSObject;
import game.vn.game.lieng.LiengController;
import game.vn.game.lieng.card.LiengCard;
import game.vn.game.lieng.card.LiengCardUtil;
import game.vn.game.lieng.card.ResultCard;
import game.vn.game.lieng.lang.LiengLanguage;
import game.vn.game.lieng.message.PlayerMessageFactory;
import game.vn.game.lieng.playerstate.AllInPlayerState;
import game.vn.game.lieng.playerstate.BettingPlayerState;
import game.vn.game.lieng.playerstate.FoldedPlayerState;
import game.vn.game.lieng.playerstate.ILiengPlayerState;
import game.vn.game.lieng.playerstate.LeaveState;
import game.vn.game.lieng.playerstate.ReadyPlayerState;
import game.vn.game.lieng.playerstate.WaitingToNextTurnPlayerState;
import game.vn.util.GlobalsUtil;
import game.vn.util.Utils;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 *
 * @author tuanp
 */
public class LiengPlayer {
    public final static byte NOTHING = -1;
    public final static byte BET = 1;
    public final static byte CALL = 2;
    public final static byte FOLD = 3;
    public final static byte CHECK = 4;
    private User user;
    private LiengController game;
    private PlayerMessageFactory playerMessage;
    private ILiengPlayerState state;
    private ILiengPlayerState readyState;
    private ILiengPlayerState bettingState;
    private ILiengPlayerState foldedState;
    private ILiengPlayerState allInState;
    private ILiengPlayerState waitingToNextTurnState;
    private ILiengPlayerState leaveState;
    private final List<LiengCard> cards;
    //mô tả hành động sau cùng của user
    private String lastActionDesc = "";
    //hành động sau cùng của user
    private int lastAction;
    private final List<Byte> actions;
    //tổng tiền người chơi đã tố trong ván
    private BigDecimal betMoney = BigDecimal.ZERO;
    //số win người chơi thắng trong ván
    private BigDecimal winMoney = BigDecimal.ZERO;
    //đếm số lần join board
    private double countJoinBoard;
    
    public LiengPlayer(User user, LiengController game) {
        this.user = user;
        this.game = game;
        cards = new ArrayList<>();
        actions = new ArrayList<>();
        init();
    }

    private void init() {
        readyState = new ReadyPlayerState(this);
        bettingState = new BettingPlayerState(this);
        foldedState = new FoldedPlayerState(this);
        allInState = new AllInPlayerState(this);
        waitingToNextTurnState = new WaitingToNextTurnPlayerState(this);
        leaveState = new LeaveState(this);
        state = readyState;
        playerMessage = new PlayerMessageFactory(this);
    }

    public void reset() {
        cards.clear();
        actions.clear();
        winMoney = BigDecimal.ZERO;
        betMoney = BigDecimal.ZERO;
        countJoinBoard = 0;
        state = bettingState;
        lastActionDesc = "";
        lastAction = NOTHING;
        setCountJoinBoard(0);
    }

    public void addWinMoney(BigDecimal winMoney) {
        this.winMoney = Utils.add(this.winMoney, winMoney);
    }

    public BigDecimal getWinMoney() {
        return winMoney;
    }

    public void setWinMoney(BigDecimal winMoney) {
        this.winMoney = winMoney;
    }

    /**
     * Lấy số tiền player đã tố trong ván
     *
     * @return
     */
    public BigDecimal getBetMoney() {
        return betMoney;
    }

    public void addBetMoney(BigDecimal money) {
        betMoney =Utils.add(this.betMoney, money);
    }

    public List<Byte> getActions() {
        return actions;
    }
    public List<Short> getActionsToList() {
        List<Short> cardBs = new ArrayList<>();
        for (int i = 0; i < this.actions.size(); i++) {
            cardBs.add((short)this.actions.get(i));
        }
        return cardBs;
    }
    

    public void clearAction() {
        actions.clear();
    }

    public void clearCards() {
        cards.clear();
    }

    public void addAction(Byte action) {
        actions.add(action);
    }

    public String getLastActionDesc() {
        return lastActionDesc;
    }

    public void setLastActionDesc(String lastActionDesc) {
        this.lastActionDesc = lastActionDesc;
    }
    // lưu lại hành động sau cùng của người chơi, dùng để kiểm tra ván có thể kết thúc trong isAllBettedEqual
    public void setLastAction(byte lastAction){
        this.lastAction = lastAction;
    }
    
    public int getLastAction(){
        return this.lastAction;
    }
    
    public List<LiengCard> getCards() {
        return cards;
    }
    public List<Short> getCardsToList(){
        List<Short> cardBs = new ArrayList<>();
        for (int i = 0; i < this.cards.size(); i++) {
            cardBs.add((short)this.cards.get(i).getId());
        }
        return cardBs;
    }
    public List<Short> getCardsHideToList(){
       List<Short> cardBs = new ArrayList<>();
        for (int i = 0; i < this.cards.size(); i++) {
            cardBs.add((short)-1);
        }
        return cardBs;
    }

    /**
     * Lấy lá bài có id lớn nhất
     *
     * @return
     */
    public LiengCard getBiggestCardId() {
        LiengCard card = null;
        for (LiengCard liengCard : cards) {
            if (card == null) {
                card = liengCard;
                continue;
            }

            if (liengCard.getId() > card.getId()) {
                card = liengCard;
            }
        }
        return card;
    }

    public String getStringCards() {
        String str = "";
        int i=0;
        for (LiengCard card : cards) {
            if(i==0){
                str+=card;
            }else{
               str+=" "+card; 
            }
            i++;
        }
        return str;
    }

    public void addCard(LiengCard card) {
        cards.add(card);
    }

    public ILiengPlayerState getAllInState() {
        return allInState;
    }

    public void setState(ILiengPlayerState state) {
        this.state = state;
    }

    public ILiengPlayerState getState() {
        return state;
    }

    public ILiengPlayerState getReadyState() {
        return readyState;
    }

    public ILiengPlayerState getBettingState() {
        return bettingState;
    }

    public ILiengPlayerState getWaitingState() {
        return waitingToNextTurnState;
    }

    public ILiengPlayerState getFoldedState() {
        return foldedState;
    }

    public ILiengPlayerState getLeaveState() {
        return leaveState;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public LiengController getGame() {
        return game;
    }

    public void setGame(LiengController game) {
        this.game = game;
    }

    public void call() throws Exception {
        state.call();
    }

    public void check() throws Exception {
        state.check();
    }

    public void fold() throws Exception {
        state.fold();
    }

    public void bet(BigDecimal betMoney) throws Exception {
        state.bet(betMoney);
    }

    public boolean isReady() {
        return this.state.equals(this.readyState);
    }

    public boolean isFolded() {
        return this.state.equals(this.foldedState);
    }

    public boolean isBetting() {
        return this.state.equals(this.bettingState);
    }

    public boolean isWaitingToNextTurn() {
        return this.state.equals(this.waitingToNextTurnState);
    }

    public boolean isAllIn() {
        return this.state.equals(this.allInState);
    }

    public boolean isLeave() {
        return this.state.equals(this.leaveState);
    }

    public ResultCard getResultCard() {
        return LiengCardUtil.getResult(cards, this.game.getLocaleOfUser(user));
    }

    public ResultCard getResultCard(Locale locale) {
        return LiengCardUtil.getResult(cards, locale);
    }

    /**
     * ván mới bắt đầu gửi message này về cho mỗi người chơi 3 lá
     * @return 
     */
    public SFSObject sendDealCardMessage(){
        SFSObject m = playerMessage.getDealCardMessage();
        return m;
    }

    /**
     * Gửi khi chuyển lượt cho người chơi
     */
    public void sendNextTurnMessage() {
        SFSObject m = playerMessage.getNextTurnMessage();
        game.sendAllUserMessage(m);
    }

    /**
     * Gửi message khi người chơi bet
     * @param callMoney
     * @param betMoney
     */
    public void sendBetMessage(double callMoney, double betMoney) throws Exception {
        SFSObject mEn = playerMessage.getBetMessage(GlobalsUtil.ENGLISH_LOCALE, callMoney, betMoney);
        SFSObject mVi = playerMessage.getBetMessage(GlobalsUtil.VIETNAMESE_LOCALE, callMoney, betMoney);
        SFSObject mZh = playerMessage.getBetMessage(GlobalsUtil.CHINESE_LOCALE, callMoney, betMoney);
        game.sendToAllWithLocale(mEn, mVi, mZh);
        setLastActionDesc(String.format(LiengLanguage.getMessage(LiengLanguage.BET,
                this.game.getLocaleOfUser(user)),Utils.formatNumber(callMoney),Utils.formatNumber(betMoney)));
    }

    /**
     * Gửi message khi người chơi call
     * @param callMoney
     */
    public void sendCallMessage(double callMoney) throws Exception {
        SFSObject mEn = playerMessage.getCallMessage(GlobalsUtil.ENGLISH_LOCALE, callMoney);
        SFSObject mVi = playerMessage.getCallMessage(GlobalsUtil.VIETNAMESE_LOCALE, callMoney);
        SFSObject mZh = playerMessage.getCallMessage(GlobalsUtil.CHINESE_LOCALE, callMoney);
        game.sendToAllWithLocale(mEn, mVi, mZh);
        setLastActionDesc(LiengLanguage.getMessage(LiengLanguage.CALL,
                this.game.getLocaleOfUser(user)));
    }

    /**
     * Gửi message khi người chơi fold
     * @throws java.lang.Exception
     */
    public void sendFoldMessage() throws Exception {
        SFSObject mEn = playerMessage.getFoldMessage(GlobalsUtil.ENGLISH_LOCALE);
        SFSObject mVi = playerMessage.getFoldMessage(GlobalsUtil.VIETNAMESE_LOCALE);
        SFSObject mZh = playerMessage.getFoldMessage(GlobalsUtil.CHINESE_LOCALE);
        game.sendToAllWithLocale(mEn, mVi, mZh);
    }

    /**
     * Gửi message khi người chơi fold
     */
    public void sendCheckMessage() throws Exception {
        SFSObject mEn = playerMessage.getCheckMessage(GlobalsUtil.ENGLISH_LOCALE);
        SFSObject mVi = playerMessage.getCheckMessage(GlobalsUtil.VIETNAMESE_LOCALE);
        SFSObject mZh = playerMessage.getCheckMessage(GlobalsUtil.CHINESE_LOCALE);
        game.sendToAllWithLocale(mEn, mVi, mZh);
    }

    @Override
    public String toString() {
        return "player: " + user.getName();
    }

    /**
     * set action cho người bắt đầu ván
     *
     * @param player
     */
    public void setDefaultAction() {
        clearAction();
        addAction(LiengPlayer.CHECK);
        //tiền hiện tại lớn hơn tiền phải tố thì mới dc quyền tố
        if (game.isCanBet(this)) {
            addAction(LiengPlayer.BET);
        }
        addAction(LiengPlayer.FOLD);
    }

    /**
     * đếm số lần vào bàn của player này
     */
    public void incrCountJoinBoard() {
        countJoinBoard++;
    }

    public void setCountJoinBoard(int count) {
        countJoinBoard = count;
    }

    /**
     * lấy số lần vào bàn của player
     *
     * @return
     */
    public double getCountJoinBoard() {
        return countJoinBoard;
    } 
    /**
     * User khi rời game sẽ tự động fold
     * @param nextUser
     * @throws Exception 
     */
    public void foldForLeaver(User nextUser) throws Exception{
        if(isBetting()){
           BettingPlayerState state= (BettingPlayerState) this.bettingState;
           state.foldForLeaver(nextUser);
        }
    }
    
    public SFSObject getReturnMessage(){
        return playerMessage.getReturnGameMessage();
    }

}
