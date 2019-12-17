/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.vn.game.sam.object;

import com.smartfoxserver.v2.entities.User;
import com.smartfoxserver.v2.entities.data.SFSObject;
import game.vn.common.card.object.Card;
import game.vn.game.sam.SamController;
import game.vn.game.sam.message.SamPlayerMessage;
import game.vn.game.sam.state.IXamPlayerState;
import game.vn.game.sam.state.LeaveState;
import game.vn.game.sam.state.MovingPlayerState;
import game.vn.game.sam.state.ReadyPlayerState;
import game.vn.game.sam.state.SkipXamPlayerState;
import game.vn.game.sam.state.SkippedPlayerState;
import game.vn.game.sam.state.XamPlayerState;
import game.vn.game.sam.utils.SamCardUtils;
import game.vn.util.GlobalsUtil;
import game.vn.util.Utils;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;

/**
 *
 * @author tuanp
 */
public class SamPlayer {
/**
     * board player in system.
     */
    private User user;
    /**
     * Tiền user bị phạt
     */
    private BigDecimal penalty = BigDecimal.ZERO;
    /**
     * Danh sách bài hiện tại của player
     */
    private List<Card> cards;

    private SamController game;
    private SamPlayerMessage playerMessage;

    //Player state
    private IXamPlayerState state;
    private IXamPlayerState readyState;
    private IXamPlayerState leaveState;
    private IXamPlayerState movingState;
    private IXamPlayerState skippedState;
    private IXamPlayerState xamState;
    private IXamPlayerState skipXamState;
    
    public SamPlayer(User user, SamController game) {
        this.user = user;
        this.game = game;
        cards = new ArrayList<>();
        playerMessage = new SamPlayerMessage(this);
        readyState = new ReadyPlayerState(this);
        leaveState = new LeaveState(this);
        movingState = new MovingPlayerState(this);
        skippedState = new SkippedPlayerState(this);
        xamState = new XamPlayerState(this);
        skipXamState = new SkipXamPlayerState(this);
        state = readyState;
        penalty = BigDecimal.ZERO;
    }
    
    public void reset() {
        cards.clear();
        state = readyState;
        penalty = BigDecimal.ZERO;
    }
    
    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public List<Card> getCards() {
        return cards;
    }
    
    public List<Short> getCardsToList(){
        List<Short> arr= new ArrayList<>();
        for(int i=0;i<this.cards.size();i++){
            arr.add((short)this.cards.get(i).getId());
        }
        return arr;
    }

    public void setCards(List<Card> cards) {
        this.cards = cards;
    }

    public BigDecimal getPenalty() {
        return penalty;
    }

    public void setPenalty(BigDecimal penalty) {
        this.penalty = penalty;
    }
    
    public void addPenalty(BigDecimal penalty) {
        this.penalty = Utils.add(this.penalty, penalty);
    }

    public SamController getGame() {
        return game;
    }

    public void setGame(SamController game) {
        this.game = game;
    }

    public SamPlayerMessage getPlayerMessage() {
        return playerMessage;
    }

    public void setPlayerMessage(SamPlayerMessage playerMessage) {
        this.playerMessage = playerMessage;
    }

    public IXamPlayerState getState() {
        return state;
    }

    public void setState(IXamPlayerState state) {
        this.state = state;
    }

    public IXamPlayerState getReadyState() {
        return readyState;
    }

    public void setReadyState(IXamPlayerState allInState) {
        this.readyState = allInState;
    }

    public IXamPlayerState getLeaveState() {
        return leaveState;
    }

    public void setLeaveState(IXamPlayerState leaveState) {
        this.leaveState = leaveState;
    }

    public IXamPlayerState getMovingState() {
        return movingState;
    }

    public void setMovingState(IXamPlayerState movingState) {
        this.movingState = movingState;
    }

    public IXamPlayerState getSkippedState() {
        return skippedState;
    }

    public void setSkippedState(IXamPlayerState skippedState) {
        this.skippedState = skippedState;
    }

    public IXamPlayerState getXamState() {
        return xamState;
    }

    public void setXamState(IXamPlayerState xamState) {
        this.xamState = xamState;
    }

    public IXamPlayerState getSkipXamState() {
        return skipXamState;
    }

    public void setSkipXamState(IXamPlayerState skipXamState) {
        this.skipXamState = skipXamState;
    }
    
    public boolean isReadyState() {
        return this.state.equals(this.readyState);
    }
    
    public boolean isLeaveState() {
        return this.state.equals(this.leaveState);
    }
    
    public boolean isMovingState() {
        return this.state.equals(this.movingState);
    }
    
    public boolean isSkippedState() {
        return this.state.equals(this.skippedState);
    }

    public boolean isXamState() {
        return this.state.equals(this.xamState);
    }
    
    public boolean isOneCardLeft() {
        return cards != null && cards.size() == 1;
    }

    public void move(List<Card> cards) throws Exception {
        state.move(cards);
    }
    
    public void skip() throws Exception {
        state.skip();
    }
    
    /**
     * Reset toàn bộ bài hiện tại
     */
    public final void resetCards() {
        cards = new ArrayList<>();
    }
    
    /**
     * Nhận một lá bài cho vào bài trên tay
     * @param card 
     */
    public void receiveCard(Card card) {
        cards.add(card);
        Collections.sort(cards);
    }
    
    /**
     * Xếp bài
     */
    public void sortCards() {
        Collections.sort(cards);
    }
    /**
     * Lay con bài nhỏ nhất trong bộ bài
     * @return 
     */
    public List<Card> getSmallestCards() {
        if (cards.size() == 1) {
            return cards;
        }
        Collections.sort(cards);
        List<Card> remainCard = new ArrayList<>();
        List<Card> autoCards = new ArrayList<>(cards);
        ListIterator<Card> iter = autoCards.listIterator(autoCards.size());
        while (iter.hasPrevious()) {
            Card c = iter.previous();
            if (SamCardUtils.getType(autoCards) != SamCardUtils.NOTYPE) {
                break;
            } else {
                remainCard.add(c);
                iter.remove();
            }
        }
        /* không được để heo cuối
         * 
         */
        boolean lastCardIsHeo = true;
        for (Card c : remainCard){
            // kiểm tra có con bài khác heo trong bài còn lại không
            if(!c.isHeo()){
                lastCardIsHeo = false;
                break;
            }
        }
        // bài còn lại là heo, đôi heo,3 heo, phải đánh heo trước
        if (lastCardIsHeo && !remainCard.isEmpty()){
            return remainCard;
        }
        return autoCards;
    }
    /**
     * Send startmessage 
     * @param playingTime
     * @throws Exception 
     */
    public void sendStartMessage(int playingTime) throws Exception {
        SFSObject m = playerMessage.getStartMessage(playingTime);
        this.game.sendUserMessage(m, user);
    }
    
    /**
     * Send move error message
     * @param mess 
     * @throws Exception 
     */
    public void sendMoveErrorMessage(String mess) throws Exception {
        SFSObject m = playerMessage.getMoveErrorMessage(mess);
        this.game.sendUserMessage(m, user);
    }

    /**
     *
     * @return con bai nho nhat ma player nay dang so huu.
     */
    public Card getSmallestCard() {
        return cards.get(0);
    }
    public Locale getLocale(){
        if(getUser()==null){
            return GlobalsUtil.DEFAULT_LOCALE;
        }
        return this.getGame().getLocaleOfUser(getUser());
    }
    /**
     * Tra ve thông tin bàn khi user on return
     * @return 
     */
    public SFSObject getOnReturnMessage(){
        return playerMessage.getReturnGameMessage();
    }
}
