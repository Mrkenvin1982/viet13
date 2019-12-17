/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package game.vn.game.maubinh.object;

import game.vn.common.card.object.Card;
import game.vn.game.maubinh.utils.GameChecker;
import game.vn.game.maubinh.MauBinhConfig;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author binhnt
 */
public class Set {
    
    /**
     * Cards of this player in game.
     */
    private List<Card> cards;
    private int cardNumber;
    private int type;

    public Set(int size) {
        this.cards = new ArrayList<>();
        this.cardNumber = size;
        this.type = SetType.NOT_ENOUGH_CARD;
    }

    /**
     * get all current card of this player.
     * @return a list of card.
     */
    public List<Card> getCards() {
        return this.cards;
    }

    public void clear() {
        this.cards.clear();
        this.type = SetType.NOT_ENOUGH_CARD;
    }
    
    /**
     * received a new card from board.
     * @param card card that player received.
     */
    public boolean receivedCard(Card card) {
        // Enough cards.
        if (this.isEnough()) {
            return false;
        }
        
        // Check dupplication.
        for (Card card2 : this.cards) {
            if (card.getId() == card2.getId()) {
                return false;
            }
        }
        
        this.cards.add(card);
        Collections.sort(this.cards);
        
        // Set type if cards are enough.
        if (this.isEnough()) {
            this.setType();
        }
        
        return true;
    }
    
    public int getType() {
        return this.type;
    }
    
    public boolean isFlush() {
        return this.type == SetType.FLUSH || this.type == SetType.STRAIGHT_FLUSH;
    }
    
    public boolean isStraight() {
        return this.type == SetType.STRAIGHT || this.type == SetType.STRAIGHT_FLUSH;
    }
    
    /**
     * Get win "chi" if win, lose "chi" if lose, otherwise 0.
     * @param set a specified set.
     * @return number of win "chi" if win, lose "chi" (negative number) if lose, otherwise 0.
     */
    public int getWinChiInComparisonWith(Set set) {
        switch (this.compareWith(set)) {
            case MauBinhConfig.RESULT_WIN:
                return this.getWinChi();
            case MauBinhConfig.RESULT_DRAW:
                return 0;
            case MauBinhConfig.RESULT_LOSE:
                return -set.getWinChi();
            default:
                return MauBinhConfig.RESULT_ERROR;
        }
    }
    
    /**
     * Calculate win chi.
     * @return number of win chi.
     */
    public int getWinChi() {
        // Check input condition.
        if (this.getType() == SetType.NOT_ENOUGH_CARD) {
            return MauBinhConfig.RESULT_ERROR;
        }
        
        return MauBinhConfig.CHI_DEFAULT;
    }
    
    /**
     * Compare with specified set.
     * @param set a set of cards.
     * @return 1 if 1st self set is bigger, -1 if set is bigger, 0 if they are equal.
     */
    public int compareWith(Set set) {
        // Check input condition.
        if (set == null ||
                this.getType() == SetType.NOT_ENOUGH_CARD ||
                set.getType() == SetType.NOT_ENOUGH_CARD) {
            return MauBinhConfig.RESULT_ERROR;
        }
        
        // Type is smaller.
        if (this.getType() < set.getType()) {
            return MauBinhConfig.RESULT_LOSE;
        // Type is bigger.
        } else if (this.getType() > set.getType()) {
            return MauBinhConfig.RESULT_WIN;
        } else {
            return this.compareWithSameType(set);
        }
    }
    
    public boolean isEnough() {
        return this.cards.size() >= this.cardNumber;
    }
    
    protected void setType(int type) {
        this.type = type;
    }
    
    protected void setType() {
        // Check cards.
        if (this.cards == null || this.cards.isEmpty() ||
                this.isEnough() == false) {
            this.type = SetType.NOT_ENOUGH_CARD;
            return;
        }
        
        // isFlush: (Thung), cards have the same type.
        boolean isFlush = true;
        int pairNo = 0;
        int threeNo = 0;
        int sameCardNo = 0;
        for (int i = 1; i < this.cards.size(); i++) {
            // if 2 continuous cards have different types.
            if (isFlush && this.cards.get(i).getCardType() != this.cards.get(i - 1).getCardType()) {
                isFlush = false;
            }
            
            // Check pair, three or four of a kind.
            if (this.cards.get(i).getCardNumber() == this.cards.get(i - 1).getCardNumber()) {
                sameCardNo++;
            } else {
                switch (sameCardNo) {
                    case 0:
                        break;
                    case 1: // a pair.
                        pairNo++;
                        break;
                    case 2: // Three of a kind.
                        threeNo++;
                        break;
                    case 3: // Four of a Kind.
                        this.type = SetType.FOUR_OF_KIND;
                        return;
                    default:
                        break;
                }
                
                sameCardNo = 0;
            }
        }

        switch (sameCardNo) {
            case 0:
                break;
            case 1: // a pair.
                pairNo++;
                break;
            case 2: // Three of a kind.
                threeNo++;
                break;
            case 3: // Four of a Kind.
                this.type = SetType.FOUR_OF_KIND;
                return;
            default:
                break;
        }

        // There is a three.
        if (threeNo > 0) {
            // Cu lu.
            if (pairNo > 0) {
                this.type = SetType.FULL_HOUSE;
                return;
            } else {
                // Xam chi.
                this.type = SetType.THREE_OF_KIND;
                return;
            }
        }

        // There is NOT any three.
        // 2 dao.
        if (pairNo == 2) {
            this.type = SetType.TWO_PAIR;
            return;
        } else if (pairNo == 1) {
            // 1 dao.
            this.type = SetType.ONE_PAIR;
            return;
        }

        
        // There is NOT any pair.
        int firstCardNumber = this.cards.get(0).getCardNumber();
        int lastCardNumber = this.cards.get(this.cards.size() - 1).getCardNumber();
        // Sanh.
        if (this.isStraight(this.cards)) {
            // Thung pha sanh.
            if (isFlush) {
                this.type = SetType.STRAIGHT_FLUSH;
                return;
            } else {
                // Sanh.
                this.type = SetType.STRAIGHT;
                return;
            }
        }

        // Thung. Plus ret to compare between flush.
        if (isFlush) {
            this.type = SetType.FLUSH;
            return;
        }
        
        // Mau thau.
        this.type = SetType.HIGH_CARD;
    }
    
    /**
     * Check straight for a specified card list, which has NO pair.
     * @param cards card list.
     * @return True if success, otherwise False.
     */
    private boolean isStraight(List<Card> cards) {
        if (cards == null || cards.isEmpty()) {
            return false;
        }
        
        return this.isNormalStraight(cards) || this.is2ndStraight(cards);
    }

    /**
     * Check normal straight for a specified card list, which has NO pair.
     * @param cards card list.
     * @return True if success, otherwise False.
     */
    private boolean isNormalStraight(List<Card> cards) {
        if (cards == null || cards.isEmpty()) {
            return false;
        }
        
        // There is NOT any pair.
        int firstCardNumber = this.cards.get(0).getCardNumber();
        int lastCardNumber = this.cards.get(this.cards.size() - 1).getCardNumber();
        // Sanh.
        return lastCardNumber - firstCardNumber == this.cards.size() - 1;
    }

    /**
     * Check 2nd straight for a specified card list, which has NO pair.
     * @param cards card list.
     * @return True if success, otherwise False.
     */
    private boolean is2ndStraight(List<Card> cards) {
        if (cards == null || cards.isEmpty()) {
            return false;
        }
        
        // If the last card is Ace.
        if (MauBinhCardSet.isAce(this.cards.get(this.cards.size() - 1))) {
            // Check the send to last card.
            return (this.cards.size() == MauBinhConfig.NUMBER_CARD_BIG_SET
                    && MauBinhCardSet.is5(this.cards.get(this.cards.size() - 2)))
                    || (this.cards.size() == MauBinhConfig.NUMBER_CARD_SMALL_SET
                    && MauBinhCardSet.is3(this.cards.get(this.cards.size() - 2)));
        }
        
        return false;
    }
    
    /**
     * Compare with specified set.
     * @param set a set of cards.
     * @return 1 if win, 0 if they are draw, -1 if lose.
     */
    private int compareWithSameType(Set set) {
        switch (this.getType()) {
            case SetType.HIGH_CARD:
                return this.compareWithHighCard(set);
            case SetType.ONE_PAIR:
                return this.compareWithOnePair(set);
            case SetType.TWO_PAIR:
                return this.compareWithTwoPair(set);
            case SetType.THREE_OF_KIND:
                return this.compareWithThreeOfKind(set);
            case SetType.STRAIGHT:
                return this.compareWithStright(set);
            case SetType.FLUSH:
                return this.compareWithFlush(set);
            case SetType.FULL_HOUSE:
                return this.compareWithFullHouse(set);
            case SetType.FOUR_OF_KIND:
                return this.compareWithFourOfKind(set);
            case SetType.STRAIGHT_FLUSH:
                return this.compareWithStraightFlush(set);
            default:
                return MauBinhConfig.RESULT_ERROR;
        }
    }
    
    /**
     * Compare with set, which has the same type: High Card (Mau thau).
     * @param set a Set object.
     * @return 1 if win, 0 if they are draw, -1 if lose.
     */
    private int compareWithHighCard(Set set) {
        return GameChecker.compareCardByCard(this.getCards(), set.getCards());
    }
    
    /**
     * Compare with set, which has the same type: One Pair (1 dao).
     * @param set a Set object.
     * @return 1 if win, 0 if they are draw, -1 if lose.
     */
    private int compareWithOnePair(Set set) {
        Card pair01 = null;
        // Get card number of pair of self.
        for (int i = 1; i < this.getCards().size(); i++) {
            if (this.getCards().get(i - 1).getCardNumber() == this.getCards().get(i).getCardNumber()) {
                pair01 = this.getCards().get(i);
                break;
            }
        }
        
        Card pair02 = null;
        // Get card number of pair of set.
        for (int i = 1; i < set.getCards().size(); i++) {
            if (set.getCards().get(i - 1).getCardNumber() == set.getCards().get(i).getCardNumber()) {
                pair02 = set.getCards().get(i);
                break;
            }
        }
        
        if (pair01 == null || pair02 == null) {
            return MauBinhConfig.RESULT_ERROR;
        }
        
        if (pair01.getCardNumber() > pair02.getCardNumber()) {
            return MauBinhConfig.RESULT_WIN;
        } else if (pair01.getCardNumber() < pair02.getCardNumber()) {
            return MauBinhConfig.RESULT_LOSE;
        } else {
            return GameChecker.compareCardByCard(this.getCards(), set.getCards());
        }
    }
    
    /**
     * Compare with set, which has the same type: Two Pair (2 dao).
     * @param set a Set object.
     * @return 1 if win, 0 if they are draw, -1 if lose.
     */
    private int compareWithTwoPair(Set set) {
        if (this.getCards() == null || set.getCards() == null ||
                this.getCards().size() < 5 || set.getCards().size() < 5) {
            return MauBinhConfig.RESULT_ERROR;
        }

        // Get card number of big pairs.
        // The 4th card is card in the big pair because card lists are always sorted.
        Card pair01 = this.getCards().get(3);
        Card pair02 = set.getCards().get(3);
        
        if (pair01.getCardNumber() > pair02.getCardNumber()) {
            return MauBinhConfig.RESULT_WIN;
        } else if (pair01.getCardNumber() < pair02.getCardNumber()) {
            return MauBinhConfig.RESULT_LOSE;
        }

        // Get card number of small pairs.
        // The 2nd card is card in the small pair because card lists are always sorted.
        pair01 = this.getCards().get(1);
        pair02 = set.getCards().get(1);
        
        if (pair01.getCardNumber() > pair02.getCardNumber()) {
            return MauBinhConfig.RESULT_WIN;
        } else if (pair01.getCardNumber() < pair02.getCardNumber()) {
            return MauBinhConfig.RESULT_LOSE;
        }
        
        // If there are 2 equal pairs then compare all cards.
        return GameChecker.compareCardByCard(this.getCards(), set.getCards());
    }
    
    /**
     * Compare with set, which has the same type: Three of a kind (Xam chi).
     * @param set a Set object.
     * @return 1 if win, 0 if they are draw, -1 if lose.
     */
    private int compareWithThreeOfKind(Set set) {
        if (this.getCards() == null || set.getCards() == null ||
                this.getCards().size() < 3 || set.getCards().size() < 3) {
            return MauBinhConfig.RESULT_ERROR;
        }

        // Get card number of three of a kind.
        // It is the middle card of big list, or the last of small list
        // because card lists are always sorted.
        Card three01 = this.getCards().get(2);
        Card three02 = set.getCards().get(2);
        
        // Xam win.
        if (three01.getCardNumber() > three02.getCardNumber()) {
            return MauBinhConfig.RESULT_WIN;
        } else if (three01.getCardNumber() < three02.getCardNumber()) { // Xam lose.
            return MauBinhConfig.RESULT_LOSE;
        } else { // Cannot draw, because there is no 2 equal three of a kind in game.
            return MauBinhConfig.RESULT_DRAW;
        }
    }
    
    /**
     * Compare with set, which has the same type: Straight (Sanh).
     * @param set a Set object.
     * @return 1 if win, 0 if they are draw, -1 if lose.
     */
    private int compareWithStright(Set set) {
        // Compare 2 highest cards because
        // 10JQKA biggest, A2345 is second.
        return GameChecker.compare2HighestCards(this.getCards(), set.getCards());
    }
    
    /**
     * Compare with set, which has the same type: Flush (Thung).
     * @param set a Set object.
     * @return 1 if win, 0 if they are draw, -1 if lose.
     */
    private int compareWithFlush(Set set) {
        return GameChecker.compareCardByCard(this.getCards(), set.getCards());
    }
    
    /**
     * Compare with set, which has the same type: Full House (Thung pha sanh).
     * @param set a Set object.
     * @return 1 if win, 0 if they are draw, -1 if lose.
     */
    private int compareWithFullHouse(Set set) {
        if (this.getCards() == null || set.getCards() == null ||
                this.getCards().size() < 5 || set.getCards().size() < 5) {
            return MauBinhConfig.RESULT_ERROR;
        }

        // Get card number of three of a kind.
        // It is the middle card because card lists are always sorted.
        Card three01 = this.getCards().get(2);
        Card three02 = set.getCards().get(2);
        
        // Xam win.
        if (three01.getCardNumber() > three02.getCardNumber()) {
            return MauBinhConfig.RESULT_WIN;
        } else if (three01.getCardNumber() < three02.getCardNumber()) { // Xam lose.
            return MauBinhConfig.RESULT_LOSE;
        } else { // Cannot draw, because there is no 2 equal three of a kind in game.
            return MauBinhConfig.RESULT_DRAW;
        }
    }
    
    /**
     * Compare with set, which has the same type: Four of a kind (Tu quy).
     * @param set a Set object.
     * @return 1 if win, 0 if they are draw, -1 if lose.
     */
    private int compareWithFourOfKind(Set set) {
        if (this.getCards() == null || set.getCards() == null ||
                this.getCards().size() < 5 || set.getCards().size() < 5) {
            return MauBinhConfig.RESULT_ERROR;
        }

        // Get card number of four of a kind.
        // It is the middle card because card lists are always sorted.
        Card four01 = this.getCards().get(2);
        Card four02 = set.getCards().get(2);
        
        // Tu quy win.
        if (four01.getCardNumber() > four02.getCardNumber()) {
            return MauBinhConfig.RESULT_WIN;
        } else if (four01.getCardNumber() < four02.getCardNumber()) { // Tu quy lose.
            return MauBinhConfig.RESULT_LOSE;
        } else { // Cannot draw, because there is no 2 equal four of a kind in game.
            return MauBinhConfig.RESULT_DRAW;
        }
    }
    
    /**
     * Compare with set, which has the same type: Straight Flush (Thung pha sanh).
     * @param set a Set object.
     * @return 1 if win, 0 if they are draw, -1 if lose.
     */
    private int compareWithStraightFlush(Set set) {
        // Compare 2 highest cards because
        // 10JQKA biggest, A2345 is second.
        return GameChecker.compare2HighestCards(this.getCards(), set.getCards());
    }
}
