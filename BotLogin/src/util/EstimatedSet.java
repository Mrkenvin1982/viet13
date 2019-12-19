/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.HashSet;

/**
 *
 * @author binhnt
 */
public class EstimatedSet {

    public static final int LAST_SET = 0;
    public static final int MIDDLE_SET = 1;
    public static final int FIRST_SET = 2;

    public static final int[] LAST_SET_RATE = new int[] {1, 2, 1, 2};
    public static final int[] MIDDLE_SET_RATE = new int[] {1, 1, 2, 1};
    public static final int[] FIRST_SET_RATE = new int[] {1, 2, 2, 2};

    // High Card: 0 --> 12 ==> 0 --> 12
    // Pair     : 0 --> 12 ==> 13 -> 25
    // Two Pair : 1 --> 12 ==> 26 -> 37
    // 3 Of Kind: 0 --> 12 ==> 38 -> 50
    // Straight : 4 --> 12 ==> 51 -> 59
    // Flush    : 5 --> 12 ==> 60 -> 67
    // FullHouse: 0 --> 12 ==> 68 -> 80
    // 4 Of Kind: 0 --> 12 ==> 81 -> 93
    // FlushStra: 4 --> 12 ==> 94 -> 102
    private static final int[] SETTYPE_SCORE = new int[]{0, 13, 26, 38, 51, 60, 68, 81, 94};
    private static final int[] SETTYPE_SUBSCORE = new int[]{0, 0, 1, 0, 4, 5, 0, 0, 4};

    private static final int LAST_SET_AVG = 60;
    private static final int MIDDLE_SET_AVG = 32;
    private static final int FIRST_SET_AVG = 15;

    /**
     * Cards of this player in game.
     */
    private Set<Card> set;
    private List<Integer> cards;
    private int type;

    public EstimatedSet() {
        this.set = new HashSet<>();
        this.cards = new ArrayList<>();
        this.type = SetType.HIGH_CARD;
    }

    public EstimatedSet(Set<Card> set, int type) {
        this.set = new HashSet<>();
        this.set.addAll(set);
        this.cards = new ArrayList<>();
        this.type = type;
    }

    public EstimatedSet(List<Card> set, int type) {
        this.set = new HashSet<>();
        this.set.addAll(set);
        this.cards = new ArrayList<>();
        this.type = type;
    }

    public EstimatedSet(Set<Card> set, int type, List<Integer> list) {
        this.set = new HashSet<>();
        this.set.addAll(set);
        this.cards = new ArrayList<>();
        this.cards.addAll(list);
        this.type = type;
    }

    public void cloneWith(EstimatedSet set) {
        this.clear();
        if (set != null) {
            this.addSet(set.getSet());
            this.setType(set.getType());
            this.cards.addAll(set.getCards());
        }
    }

    public Set<Card> getSet() {
        return this.set;
    }

    public void addSet(Set<Card> set) {
        if (this.set == null) {
            this.set = new HashSet<>();
        }

        this.set.addAll(set);
    }

    public boolean contains(Card card) {
        if (this.set == null) {
            return false;
        }

        return this.set.contains(card);
    }

    public boolean remove2ndCard(Card card) {
        if (this.is2ndCard(card) == false) {
            return false;
        }

        return this.set.remove(card);
    }

    /**
     * To remove unused cards and correct the set type.
     */
    public void toFinalSet(List<Card> remaining) {
        if (this.set == null || this.set.size() < MauBinhConfig.NUMBER_CARD_BIG_SET) {
            return;
        }

        if (remaining == null) {
            remaining = new ArrayList<>();
        }

        List<Card> tmp = new ArrayList<>();
        tmp.addAll(this.set);
        Collections.sort(tmp);

        if (this.type == SetType.FLUSH) {
            //tìm 1 đôi hoặc 1 sám cho small
            for (int j = 0; j < remaining.size(); j++) {
                for (int i = 0; i < tmp.size() - 1; i++) {
                    if (remaining.get(j).getCardNumber() == tmp.get(i).getCardNumber()) {
                        if (this.set.size() > MauBinhConfig.NUMBER_CARD_BIG_SET) {
                            if (this.set.remove(tmp.get(i))) {
                                remaining.add(tmp.get(i));
                            }
                        }
                    }
                }
            }

            for (int i = 0; i < tmp.size(); i++) {
                if (this.set.size() > MauBinhConfig.NUMBER_CARD_BIG_SET) {
                    if (this.set.remove(tmp.get(i))) {
                        remaining.add(tmp.get(i));
                    }
                }
            }

            // Correct the type of set.
            if (this.isStraight()) {
                this.type = SetType.STRAIGHT_FLUSH;
            }
        } else if (this.type == SetType.STRAIGHT || this.type == SetType.STRAIGHT_FLUSH) {
            for (int i = 1; i < tmp.size(); i++) {
                if (tmp.get(i).getCardNumber() == tmp.get(i - 1).getCardNumber()) {
                    if (this.set.size() > MauBinhConfig.NUMBER_CARD_BIG_SET) {
                        if (this.set.remove(tmp.get(i))) {
                            remaining.add(tmp.get(i));
                        }
                    }
                }
            }

            boolean is2ndStraight = MauBinhCardSet.isAce(tmp.get(tmp.size() - 1)) && MauBinhCardSet.is2(tmp.get(0));
            if (is2ndStraight) {
                for (int i = tmp.size() - 1; i >= 0; i--) {
                    if (MauBinhCardSet.isAce(tmp.get(i))) {
                        if (this.set.size() > MauBinhConfig.NUMBER_CARD_BIG_SET) {
                            if (this.set.remove(tmp.get(i))) {
                                remaining.add(tmp.get(i));
                            }
                        }
                    } else {
                        break;
                    }
                }
            }

            for (int i = 0; i < tmp.size(); i++) {
                if (this.set.size() > MauBinhConfig.NUMBER_CARD_BIG_SET) {
                    if (this.set.remove(tmp.get(i))) {
                        remaining.add(tmp.get(i));
                    }
                }
            }

            // Correct the type of set.
            if (this.isFlush()) {
                this.type = SetType.STRAIGHT_FLUSH;
            }
        }
    }

    public boolean is2ndCard(Card card) {
        if (this.set == null || this.type != SetType.STRAIGHT || card == null) {
            return false;
        }

        if (this.set.contains(card) == false) {
            return false;
        }

        for (Card tmp : this.set) {
            if (tmp.getId() != card.getId() && tmp.getCardNumber() == card.getCardNumber()) {
                return true;
            }
        }

        return false;
    }

    /**
     * get all current card of this player.
     *
     * @return a list of card.
     */
    public List<Integer> getCards() {
        return this.cards;
    }

    public void clear() {
        this.set.clear();
        this.cards.clear();
        this.type = SetType.HIGH_CARD;
    }

    /**
     * received a new card from board.
     *
     * @param card card that player received.
     */
    public void addCard(int card) {
        this.cards.add(card);
    }

    public int getType() {
        return this.type;
    }

    /**
     * Compare with specified set.
     *
     * @param set a set of cards.
     * @return 1 if 1st self set is bigger, -1 if set is bigger, 0 if they are
     * equal.
     */
    public int compareWith(EstimatedSet set) {
        // Check input condition.
        if (set == null) {
            return 1;
        }

        // Type is smaller.
        if (this.getType() < set.getType()) {
            return -1;
            // Type is bigger.
        } else if (this.getType() > set.getType()) {
            return 1;
        } else {
            return this.compareWithSameType(set);
        }
    }

    public void setType(int type) {
        this.type = type;
    }

    /**
     * Compare with specified set.
     *
     * @param set a set of cards.
     * @return 1 if win, 0 if they are draw, -1 if lose.
     */
    private int compareWithSameType(EstimatedSet set) {
        if (this.cards == null || this.cards.isEmpty()) {
            return -1;
        }

        if (set == null || set.getCards() == null || set.getCards().isEmpty()) {
            return 1;
        }

        for (int i = 0; i < this.cards.size(); i++) {
            if (i >= set.getCards().size()) {
                return 1;
            }

            if (this.cards.get(i) > set.getCards().get(i)) {
                return 1;
            } else if (this.cards.get(i) < set.getCards().get(i)) {
                return -1;
            }
        }

        if (this.cards.size() < set.getCards().size()) {
            return -1;
        }

        return 0;
    }

    /*   public double getScore(int index) {
//        switch (index) {
//            case EstimatedSet.LAST_SET:
//                return MauBinhConfig.getInstance().getIntLastSet() * this.getScoreForLastSet();
//            case EstimatedSet.MIDDLE_SET:
//                return MauBinhConfig.getInstance().getIntMiddleSet() * this.getScoreForMiddleSet();
//            case EstimatedSet.FIRST_SET:
//                return MauBinhConfig.getInstance().getIntSmallSet() * this.getScoreForFirstSet();
//            default:
//                return Integer.MIN_VALUE;
//        }
        
        switch (index) {
            case EstimatedSet.LAST_SET:
                return this.getScoreForLastSet();
            case EstimatedSet.MIDDLE_SET:
                return  this.getScoreForMiddleSet();
            case EstimatedSet.FIRST_SET:
                return  this.getScoreForFirstSet();
            default:
                return Integer.MIN_VALUE;
        }
    }

    private double getScoreForLastSet() {
        int card = (this.cards == null || this.cards.isEmpty()) ? -1 : this.cards.get(0);
        switch (this.getType()) {
            case SetType.HIGH_CARD:
                return -9;
            case SetType.ONE_PAIR:
                return -9;
            case SetType.TWO_PAIR:
                return -9;
            case SetType.THREE_OF_KIND:
                return Math.max(-9, (card - 27) / 3.0); // -5 --> -9
            case SetType.STRAIGHT:
                return Math.max(-4, (card - 11) / 2.0); // -4-->0
            case SetType.FLUSH:
                return card / 2.0; // 0-6
            case SetType.FULL_HOUSE:
                return Math.max(6, (card + 18) / 3.0); // 6-10
            case SetType.FOUR_OF_KIND:
                return 10;
            case SetType.STRAIGHT_FLUSH:
                return 10;
            default:
                return Integer.MIN_VALUE;
        }
    }

    private double getScoreForMiddleSet() {
        int card = (this.cards == null || this.cards.isEmpty()) ? -1 : this.cards.get(0);
        switch (this.getType()) { // min = -9, max = 10
            case SetType.HIGH_CARD:
                return -9;
            case SetType.ONE_PAIR:
                return Math.max(-9, (card - 21) / 2.0); // -5 --> -9
            case SetType.TWO_PAIR:
                return Math.max(-5, (card - 11) / 2.0); // -5-->0
            case SetType.THREE_OF_KIND:
                return card / 2; // 0-6
            case SetType.STRAIGHT:
                return Math.max(6, (card + 8) / 2.0); // 6-10
            case SetType.FLUSH:
                return 10;
            case SetType.FULL_HOUSE:
                return 10;
            case SetType.FOUR_OF_KIND:
                return 10;
            case SetType.STRAIGHT_FLUSH:
                return 10;
            default:
                return Integer.MIN_VALUE;
        }
    }

    private double getScoreForFirstSet() {
        int card = (this.cards == null || this.cards.isEmpty()) ? -1 : this.cards.get(0);
        switch (this.getType()) { // min = -10, max = 12
            case SetType.HIGH_CARD:
                return card - 12.5; // 234 = -10 -> Ace = 0.
            case SetType.ONE_PAIR:
                return card + 0.5; // 0 - 12.
            case SetType.THREE_OF_KIND:
                return 10;
            case SetType.TWO_PAIR:
            case SetType.STRAIGHT:
            case SetType.FLUSH:
            case SetType.FULL_HOUSE:
            case SetType.FOUR_OF_KIND:
            case SetType.STRAIGHT_FLUSH:
            default:
                return Integer.MIN_VALUE;
        }
    }
     */
    public double getScore(int index, int tmpId) {
//        switch (index) {
//            case EstimatedSet.LAST_SET:
//                return MauBinhConfig.getInstance().getIntLastSet() * this.getScoreForLastSet();
//            case EstimatedSet.MIDDLE_SET:
//                return MauBinhConfig.getInstance().getIntMiddleSet() * this.getScoreForMiddleSet();
//            case EstimatedSet.FIRST_SET:
//                return MauBinhConfig.getInstance().getIntSmallSet() * this.getScoreForFirstSet();
//            default:
//                return Integer.MIN_VALUE;
//        }

        double ret = Integer.MIN_VALUE;
        int rate = 1;
        switch (index) {
            case EstimatedSet.LAST_SET:
                ret = this.getLinearScore() - LAST_SET_AVG;
                rate = LAST_SET_RATE[tmpId];
                break;
            case EstimatedSet.MIDDLE_SET:
                ret = this.getLinearScore() - MIDDLE_SET_AVG;
                rate = MIDDLE_SET_RATE[tmpId];
                break;
            case EstimatedSet.FIRST_SET:
                ret = this.getLinearScore() - FIRST_SET_AVG;
                rate = FIRST_SET_RATE[tmpId];
                break;
            default:
                break;
        }

        ret = this.getNonLinearScore(ret);
        if (this.type == SetType.FOUR_OF_KIND || this.type == SetType.STRAIGHT_FLUSH) {
            ret += 1; // đảm bảo Tứ quý và Thùng phá sảnh không bị xé.
        }

        return ret * rate;
    }

    private double getLinearScore() {
        int index = Math.min(SETTYPE_SCORE.length - 1, this.type - SetType.HIGH_CARD);
        double ret = SETTYPE_SCORE[index];
        if (this.cards == null || this.cards.isEmpty()) {
            return ret;
        }

        ret += (this.cards.get(0) - SETTYPE_SUBSCORE[index]);
        double rate = 1.0 / 13;
        for (int i = 1; i < this.cards.size(); i++) {
            ret += (rate * this.cards.get(i));
            rate /= 13;
        }

        return ret;
    }

    private double getNonLinearScore(double linearScore) {
        linearScore = linearScore / 10; // cho điểm mịn hơn
        return linearScore / (1 + Math.abs(linearScore));
    }

    public boolean isStraight() {
        if (this.set == null || this.set.isEmpty()) {
            return false;
        }

        List<Card> tmp = new ArrayList<>();
        tmp.addAll(this.set);
        Collections.sort(tmp);

        for (int i = 1; i < tmp.size(); i++) {
            if (tmp.get(i).getCardNumber() != (tmp.get(i - 1).getCardNumber() + 1)) {
                return false;
            }
        }

        return true;
    }

    public boolean isFlush() {
        if (this.set == null || this.set.isEmpty()) {
            return false;
        }

        int tmp = Integer.MIN_VALUE;
        for (Card card : this.set) {
            if (tmp == Integer.MIN_VALUE) { // 1st card
                tmp = card.getCardType();
                continue;
            }

            if (tmp != card.getCardType()) {
                return false;
            }
        }

        return true;
    }

    public static double getMaxScore(int id) {
		if (id < 0 || id >= LAST_SET_RATE.length || id >= MIDDLE_SET_RATE.length || id >= FIRST_SET_RATE.length) {
			return 100; // bug
		}

        return LAST_SET_RATE[id] + MIDDLE_SET_RATE[id] + FIRST_SET_RATE[id];
    }

    public String toString() {
        String ret = "";
        if (this.set == null || this.set.isEmpty()) {
            return ret;
        }

        for (Card card : this.set) {
            ret += card.getCardNumber();
            ret += ", ";
        }

        return ret;
    }
}
