/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package game.vn.game.tienlentour.object;

import game.vn.common.card.object.Card;
import game.vn.game.tienlentour.utils.TienLenCardUtils;
import game.vn.util.Utils;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;

/**
 *
 * @author hoanghh
 */
public class TienLenPlayer {

    /**
     * status if player be skiped in before turn.
     */
    private boolean skipstatus;

    //về nhất, nhì, ba, bét
    private int win = -1;
    //tiền trước khi start game
    private BigDecimal moneyBeforeStartGame= BigDecimal.ZERO;
    /**
     * Tiền thưởng, tiền phạt dùng để hiển thị
     */
    private double penalty;

    public void reset() {
        skipstatus = false;
        win = -1;
        setPenalty(0);
    }
    /**
     * card of this player in game.
     */
    private final List<Card> cards = new ArrayList<>();

    public TienLenPlayer() {
    }

    /**
     * get skip status of this player in before turn.
     *
     * @return true if this player is skipped, else return false.
     */
    public boolean isSkipstatus() {
        return skipstatus;
    }

    /**
     * Set new skip status for this player in game.
     *
     * @param skipstatus status will be set.
     */
    public void setSkipstatus(boolean skipstatus) {
        this.skipstatus = skipstatus;
    }

    /**
     * reset all cards of this player.
     */
    public final void resetCards() {
        cards.clear();
    }

    /**
     * received a new card from board.
     *
     * @param card card that player received.
     */
    public void receivedCard(Card card) {
        cards.add(card);
        Collections.sort(cards);
    }

    /**
     * get all current card of this player.
     *
     * @return a list of card.
     */
    public List<Card> getCards() {
        return cards;
    }
/**
     * Lấy nhóm bài nhỏ nhất để đánh tự động thứ tự : đôi, xám, sảnh (nhỏ đến
     * lớn), 3 đôi thông, tứ quý, bốn đôi thông.
     *
     * @return
     */
    public Card[] getAutoCards() {
        if (cards.size() == 1) {
            return cards.toArray(new Card[cards.size()]);
        }

        List<Card> autoCards = new ArrayList<>(cards);
        ListIterator<Card> iter = autoCards.listIterator(autoCards.size());
        while (iter.hasPrevious()) {
            iter.previous();
            if (TienLenCardUtils.getType(autoCards.toArray( new Card [autoCards.size()])) != TienLenCardUtils.NOTYPE) {
                break;
            } else {
                iter.remove();
            }
        }
        return autoCards.toArray(new Card [autoCards.size()]);
    }

    /**
     * remove a card from this player.
     *
     * @param card card will be remove.
     */
    public void removeCards(Card card) {
        for (int i = 0; i < cards.size(); i++) {
            if (cards.get(i).equals(card)) {
                cards.remove(i);
                break;
            }
        }
    }

    /**
     *
     * @return con bai nho nhat ma player nay dang so huu.
     */
    public Card getSmallestCard() {
        return cards.get(0);
    }

    public List<Short> cardsToList(){
        List<Short> cardIds = new ArrayList<>();
        for (int i = 0 ; i< cards.size() ; i++){
            cardIds.add((short)cards.get(i).getId());
        }
        return cardIds;
    }
    /**
     * @return the win
     */
    public int getWin() {
        return win;
    }

    /**
     * @param win the win to set
     */
    public void setWin(int win) {
        this.win = win;
    }

    public BigDecimal getMoneyBeforeStartGame() {
        return moneyBeforeStartGame;
    }

    public void setMoneyBeforeStartGame(BigDecimal money) {
        this.moneyBeforeStartGame = money;
    }
    
    public double getPenalty() {
        return penalty;
    }

    public void setPenalty(long penalty) {
        this.penalty = penalty;
    }
  
    /**
     * Cộng tiền phạt
     *
     * @param penalty
     */
    public void addPenalty(double penalty) {
        this.penalty = Utils.add(this.penalty, penalty);
    }
    
}
