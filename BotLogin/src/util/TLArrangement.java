/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package util;

import java.util.List;

/**
 *
 * @author binhnt
 */
public class TLArrangement extends BasicArrangement {

    private BasicArrangement fullArrangement = null;
    private boolean waitFor2 = false;

    public TLArrangement(List<Card> cards, boolean isNo2, boolean isInRound) {
        this.fullArrangement = new BasicArrangement(cards, isInRound);
        if (isNo2) { // không cần ưu tiên hàng khi không còn heo
            this.setCards(cards);
        } else {
            List<Card> remainingCards = this.fullArrangement.getRemainingCards();
            this.setCards(remainingCards);
            System.out.println("TLArrangement - All: " + (cards == null ? 0 : cards.size()) +
                    ", remaining: " + (remainingCards == null ? 0 : remainingCards.size()));
        }

        this.waitFor2 = !isNo2;
        this.isInRound = isInRound;
    }

    @Override
    public List<Card> getRemainingCards(List<Card> turnCards) {
        return this.fullArrangement.getRemainingCards(turnCards);
    }

    public boolean getWaitFor2() {
        return this.waitFor2;
    }

    @Override
    public int getOptimalFourId(int opponentLastCard) {
        return this.fullArrangement.getOptimalFourId(opponentLastCard);
    }

    @Override
    public int getBiggestFourId(int opponentLastCard) {
        return this.fullArrangement.getBiggestFourId(opponentLastCard);
    }

    @Override
    public int getOptimalPairStraightId(int number, int opponentLastCard) {
        return this.fullArrangement.getOptimalPairStraightId(number, opponentLastCard);
    }

    @Override
    public int getBiggestPairStraightId(int number, int opponentLastCard) {
        return this.fullArrangement.getBiggestPairStraightId(number, opponentLastCard);
    }

    @Override
    public List<Card> getFour(int id) {
        return this.fullArrangement.getFour(id);
    }

    @Override
    public List<Card> get3PairStraight(int id) {
        return this.fullArrangement.get3PairStraight(id);
    }

    @Override
    public List<Card> get4PairStraight(int id) {
        return this.fullArrangement.get4PairStraight(id);
    }

    @Override
    public List<Card> getRandom() {
        return this.fullArrangement.getRandom();
    }

    @Override
    public boolean canBeWinAfter(List<Card> catchCards) {
        return this.fullArrangement.canBeWinAfter(catchCards);
    }

    @Override
    public boolean canBeWinAfter(Card catchCard) {
        return this.fullArrangement.canBeWinAfter(catchCard);
    }

    @Override
    public int size() {
        return this.fullArrangement.size();
    }

    @Override
    public boolean isRac(int id, int type) {
        return this.fullArrangement.isRac(id, type);
    }

    @Override
    public boolean hasBomb() { // hàng, not consider 4 cards 2
        return this.fullArrangement.hasBomb();
    }
}
