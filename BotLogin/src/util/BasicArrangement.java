/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author binhnt
 */
public class BasicArrangement {

    public static final int SETTYPE_UNKNOWN = -1;
    public static final int SETTYPE_HIGHCARD = 0;
    public static final int SETTYPE_PAIR = 1;
    public static final int SETTYPE_THREE = 2;
    public static final int SETTYPE_STRAIGHT = 3;
    public static final int SETTYPE_3PAIR = 4;
    public static final int SETTYPE_FOUR = 5;
    public static final int SETTYPE_4PAIR = 6;

    private List<Card> cards = null;

    private List<Integer> twoList = null;

    private List<Integer> singleIndex = null;
    private List<Integer> singleIndex2 = null;
    private List<Integer> singleIndex3 = null;
    private List<Integer> lastPairIndex = null;
    private List<Integer> lastThreeIndex = null;
    private List<Integer> lastFourIndex = null;

    private List<Integer> straightNumber = null;
    private List<Card> lastStraight = null;
    private List<Integer> pairStraightNumber = null;
    private List<Card> lastPairStraight = null;

    protected boolean isInRound = false;

    public BasicArrangement() {
    }

    public BasicArrangement(List<Card> cards, boolean isInRound) {
        this.setCards(cards);
        this.isInRound = isInRound;
    }

    public void setCards(List<Card> cards) {
        if (cards == null || cards.isEmpty()) {
            return;
        }

        this.cards = new ArrayList<>(cards);
        this.twoList = new ArrayList<>();
        this.singleIndex = new ArrayList<>();
        this.lastPairIndex = new ArrayList<>();
        this.lastThreeIndex = new ArrayList<>();
        this.lastFourIndex = new ArrayList<>();
        this.getSameCardCombo(this.cards, this.twoList, this.singleIndex, this.lastPairIndex, this.lastThreeIndex, this.lastFourIndex);

        this.straightNumber = new ArrayList<>();
        this.lastStraight = new ArrayList<>();
        this.pairStraightNumber = new ArrayList<>();
        this.lastPairStraight = new ArrayList<>();
        this.getStraight(this.cards, this.straightNumber, this.lastStraight, this.pairStraightNumber, this.lastPairStraight);

        this.singleIndex2 = this.getHighCard(this.cards, this.singleIndex, this.straightNumber, this.lastStraight);
        this.singleIndex3 = this.getHighCard3(this.cards, this.lastPairIndex, this.straightNumber, this.lastStraight);
    }

    private void getSameCardCombo(List<Card> cards, List<Integer> twoList,
            List<Integer> singleIndex, List<Integer> lastPairIndex,
            List<Integer> lastThreeIndex, List<Integer> lastFourIndex) {
        // Check cards.
        if (cards == null || cards.isEmpty() || singleIndex == null || lastPairIndex == null
                || lastThreeIndex == null || lastFourIndex == null) {
            return;
        }

        int sameCardNo = 0;
        for (int i = 1; i < cards.size(); i++) {
            if (this.isTwo(cards.get(i - 1))) {
                twoList.add(i - 1);
                continue;
            }

            // Check pair, three or four of a kind.
            if (cards.get(i).getCardNumber() == cards.get(i - 1).getCardNumber()) {
                sameCardNo++;
            } else {
                switch (sameCardNo) {
                    case 0:
                        singleIndex.add(i - 1);
                        break;
                    case 1: // a pair.
                        lastPairIndex.add(i - 1);
                        break;
                    case 2: // Three of a kind.
                        lastThreeIndex.add(i - 1);
                        break;
                    case 3: // Four of a Kind.
                        lastFourIndex.add(i - 1);
                        System.out.println("getSameCardCombo: Four of a Kind.");
                        break;
                    default:
                        break;
                }

                sameCardNo = 0;
            }
        }

        if (this.isTwo(cards.get(cards.size() - 1))) {
            twoList.add(cards.size() - 1);
            return;
        }

        switch (sameCardNo) {
            case 0:
                singleIndex.add(cards.size() - 1);
                break;
            case 1: // a pair.
                lastPairIndex.add(cards.size() - 1);
                break;
            case 2: // Three of a kind.
                lastThreeIndex.add(cards.size() - 1);
                break;
            case 3: // Four of a Kind.
                lastFourIndex.add(cards.size() - 1);
                break;
            default:
                break;
        }
    }
/*
    private void getStraight(List<Card> cards, List<Integer> straightNumber, List<Card> lastStraight,
            List<Integer> pairStraightNumber, List<Card> lastPairStraight) {
        // Check cards.
        if (cards == null || cards.isEmpty()
                || straightNumber == null || lastStraight == null) {
            return;
        }

        Card lastPairCard = null;
        int firstPairId = -1;
        int temp2;
        int j;
        for (j = cards.size() - 1; j >= 0; j--) {
            if (this.isTwo(cards.get(j)) == false) {
                break;
            }
        }

        if (j < 0) {
            return;
        }

        List<Card> ret = new ArrayList<>();
        // Add the highest card.
        ret.add(cards.get(j));
        for (int i = j - 1; i >= 0; i--) {
            int temp = cards.get(i + 1).getCardNumber() - cards.get(i).getCardNumber();
            switch (temp) {
                case 0:
                    if (lastPairCard == null) {
                        lastPairCard = cards.get(i + 1);
                        firstPairId = i;
                    } else {
                        switch (cards.get(firstPairId).getCardNumber() - cards.get(i).getCardNumber()) {
                            case 0:
                                break;
                            case 1:
                                firstPairId = i;
                                break;
                            default:
                                temp2 = lastPairCard.getCardNumber() - cards.get(firstPairId).getCardNumber() + 1;
                                if (temp2 >= 3) { // Min number of a pair straight is 3.
                                    pairStraightNumber.add(temp2);
                                    lastPairStraight.add(lastPairCard);
                                }

                                lastPairCard = null;
                                firstPairId = -1;
                                break;
                        }
                    }

                    break;
                case 1:
                    ret.add(cards.get(i));
                    break;
                default:
                    if (ret.size() >= 3) { // Min number of a straight is 3.
                        straightNumber.add(ret.size());
                        lastStraight.add(ret.get(0));
                    }

                    if (lastPairCard != null) {
                        temp2 = lastPairCard.getCardNumber() - cards.get(firstPairId).getCardNumber() + 1;
                        if (temp2 >= 3) { // Min number of a pair straight is 3.
                            pairStraightNumber.add(temp2);
                            lastPairStraight.add(lastPairCard);
                        }

                        lastPairCard = null;
                        firstPairId = -1;
                    }

                    ret.clear();
                    ret.add(cards.get(i));
                    break;
            }
        }

        if (ret.size() >= 3) { // Min number of a straight is 3.
            straightNumber.add(ret.size());
            lastStraight.add(ret.get(0));
        }

        if (lastPairCard != null) {
            temp2 = lastPairCard.getCardNumber() - cards.get(firstPairId).getCardNumber() + 1;
            if (temp2 >= 3) { // Min number of a pair straight is 3.
                pairStraightNumber.add(temp2);
                lastPairStraight.add(lastPairCard);
            }
        }
    }
*/
    private void getStraight(List<Card> cards, List<Integer> straightNumber, List<Card> lastStraight,
            List<Integer> pairStraightNumber, List<Card> lastPairStraight) {
        // Check cards.
        if (cards == null || cards.isEmpty()
                || straightNumber == null || lastStraight == null) {
            return;
        }

        int j;
        for (j = cards.size() - 1; j >= 0; j--) {
            if (this.isTwo(cards.get(j)) == false) {
                break;
            }
        }

        if (j < 0) {
            return;
        }

        List<Card> pairCards = new ArrayList<>();
        List<Card> ret = new ArrayList<>();
        // Add the highest card.
        ret.add(cards.get(j));
        for (int i = j - 1; i >= 0; i--) {
            int temp = cards.get(i + 1).getCardNumber() - cards.get(i).getCardNumber();
            switch (temp) {
                case 0:
                    pairCards.add(cards.get(i + 1));
                    break;
                case 1:
                    ret.add(cards.get(i));
                    break;
                default:
                    if (ret.size() >= 3) { // Min number of a straight is 3.
                        straightNumber.add(0, ret.size());
                        lastStraight.add(0, ret.get(0));
                    }

                    ret.clear();
                    ret.add(cards.get(i));
                    break;
            }
        }

        if (ret.size() >= 3) { // Min number of a straight is 3.
            straightNumber.add(0, ret.size());
            lastStraight.add(0, ret.get(0));
        }

        if (pairCards.isEmpty()) {
            return;
        }

        Collections.sort(pairCards);
        ret.clear();
        // Add the highest card.
        ret.add(pairCards.get(0));
        for (int i = 1; i < pairCards.size(); i++) {
            int temp = pairCards.get(i).getCardNumber() - pairCards.get(i - 1).getCardNumber();
            switch (temp) {
                case 0:
                    break;
                case 1:
                    ret.add(pairCards.get(i));
                    break;
                default:
                    if (ret.size() >= 3) { // Min number of a pair straight is 3.
                        pairStraightNumber.add(ret.size());
                        lastPairStraight.add(ret.get(ret.size() - 1));
                    }

                    ret.clear();
                    ret.add(pairCards.get(i));
                    break;
            }
        }

        if (ret.size() >= 3) { // Min number of a pair straight is 3.
            pairStraightNumber.add(ret.size());
            lastPairStraight.add(ret.get(ret.size() - 1));
        }
    }

    private List<Integer> getHighCard(List<Card> cards, List<Integer> singleIndex, List<Integer> straightNumber, List<Card> lastStraight) {
        List<Integer> ret = new ArrayList<>();
        if (cards == null || cards.isEmpty() || singleIndex == null || singleIndex.isEmpty()) {
            return ret;
        }

        if (straightNumber == null || lastStraight == null) {
            ret.addAll(singleIndex);
            return ret;
        }

        boolean isInStraight;
        for (int i = 0; i < singleIndex.size(); i++) {
            isInStraight = false;
            for (int j = 0; j < lastStraight.size(); j++) {
                if (cards.get(singleIndex.get(i)).getCardNumber() <= lastStraight.get(j).getCardNumber() && straightNumber.size() > j &&
                    cards.get(singleIndex.get(i)).getCardNumber() > lastStraight.get(j).getCardNumber() - straightNumber.get(j)) {
                    isInStraight = true;
                    break;
                }
            }

            if (isInStraight == false) {
                ret.add(singleIndex.get(i));
            }
        }

        return ret;
    }

    private List<Integer> getHighCard3(List<Card> cards, List<Integer> lastPairIndex, List<Integer> straightNumber, List<Card> lastStraight) {
        List<Integer> ret = new ArrayList<>();
        if (cards == null || cards.isEmpty()) {
            return ret;
        }

        for (int i = 0; i < cards.size(); i++) {
            for (int j = 0; j < lastStraight.size(); j++) {
                if (straightNumber.size() > j && straightNumber.get(j) > 3 &&
                    (cards.get(i).getCardNumber() == lastStraight.get(j).getCardNumber() || 
                        cards.get(i).getCardNumber() == lastStraight.get(j).getCardNumber() - straightNumber.get(j) + 1)) {
                    ret.add(i);
                    break;
                }
            }
        }

        for (int i = 0; i < lastPairIndex.size(); i++) {
            for (int j = 0; j < lastStraight.size(); j++) {
                if (cards.get(lastPairIndex.get(i)).getCardNumber() <= lastStraight.get(j).getCardNumber() && straightNumber.size() > j &&
                    cards.get(lastPairIndex.get(i)).getCardNumber() > lastStraight.get(j).getCardNumber() - straightNumber.get(j)) {
                    ret.add(lastPairIndex.get(i));
                    break;
                }
            }
        }

        Collections.sort(ret);
        return ret;
    }

    public static boolean isAceOr2(Card card) {
        return isAce(card) || is2(card);
    }

    public static boolean isAce(Card card) {
        if (card == null) {
            return false;
        }

        return card.getCardNumber() == 11; // Is it right?
    }

    public static boolean is2(Card card) {
        if (card == null) {
            return false;
        }

        return card.getCardNumber() == 12; // Is it right?
    }

    public static boolean is2(int cardId) {
        return cardId >= 48 && cardId < 52; // Is it right?
    }

    private boolean isTwo(Card card) {
        return BasicArrangement.is2(card);
    }

    public boolean hasBomb() { // hàng, not consider 4 cards 2
        return (this.lastFourIndex != null && this.lastFourIndex.isEmpty() == false) ||
                        (this.lastPairStraight != null && this.lastPairStraight.isEmpty() == false);
    }
/*	
	public boolean hasFour() { // hàng, not consider 4 cards 2om
		return this.lastFourIndex != null && this.lastFourIndex.isEmpty() == false;
	}

	public boolean hasPairStraight() { // hàng, not consider 4 cards 2
		return this.lastPairStraight != null && this.lastPairStraight.isEmpty() == false;
	}

	public boolean has4PairStraight() { // hàng, not consider 4 cards 2
		if (this.pairStraightNumber == null || this.pairStraightNumber.isEmpty() ||
			this.lastPairStraight == null || this.lastPairStraight.isEmpty()) {
			return false;
		}

		for (int i = 0; i < this.pairStraightNumber.size(); i++) {
			if (this.pairStraightNumber.get(i) == 4 && this.lastPairStraight.size() > i) {
				return true;
			}
		}

		return false;
	}
     */
    public boolean has2() {
        if (this.cards == null || this.cards.isEmpty()) {
            return false;
        }

        return this.isTwo(this.cards.get(this.cards.size() - 1));
    }

    public boolean hasOnly2() {
        if (this.cards == null || this.cards.isEmpty()) {
            return false;
        }

        return this.isTwo(this.cards.get(this.cards.size() - 1)) && this.isTwo(this.cards.get(0));
    }

    // get number of singles in the sorted list
    private int getSingleNumber(List<Card> cardList) {
        if (cardList == null || cardList.isEmpty()) {
            return 0;
        }

        int ret = 0;
        boolean isSingle = true;
        for (int i = 1; i < cardList.size(); i++) {
            if (cardList.get(i).getCardNumber() == cardList.get(i - 1).getCardNumber()) {
                isSingle = false;
                continue;
            } else {
                if (isSingle) {
                    ret++;
                } else {
                    isSingle = false;
                }
            }
        }

        if (isSingle) {
            ret++;
        }

        return ret;
    }

    // get number of pairs in the sorted list
    private int getPairNumber(List<Card> cardList) {
        if (cardList == null || cardList.isEmpty()) {
            return 0;
        }

        int ret = 0;
        for (int i = 1; i < cardList.size(); i++) {
            if (cardList.get(i).getCardNumber() == cardList.get(i - 1).getCardNumber()) {
                ret++;
                i += 1;
            }
        }

        return ret;
    }

    // get number of pairs in the sorted straight
    private int getSubStraightNumber(List<Card> cardList) {
        if (cardList == null || cardList.isEmpty()) {
            return 0;
        }

        int ret = 0;
        int lastNumber = cardList.get(0).getCardNumber();
        for (int i = 1; i < cardList.size(); i++) {
            if (cardList.get(i).getCardNumber() == cardList.get(i - 1).getCardNumber()) {
                if (cardList.get(i).getCardNumber() - lastNumber + 1 >= 3) {
                    ret++;
                    lastNumber = cardList.get(i).getCardNumber();
                }
            }
        }

        if (cardList.get(cardList.size() - 1).getCardNumber() - lastNumber + 1 >= 3) {
            ret++;
        }

        return ret;
    }

    public boolean isComplexStraight(int id) {
        List<Card> straight = this.getPreStraight(id);
        if (straight == null || straight.isEmpty()) {
            return true; // bug if it happens
        }

        int realStraightSize = straight.get(straight.size() - 1).getCardNumber() - straight.get(0).getCardNumber() + 1;
        switch (realStraightSize) {
            case 1:
                return true; // bug if it happens
            case 2:
                return true; // bug if it happens
            case 3:
                if (straight.size() > 5) {
                    return true;
                }

                return this.getPairNumber(straight) >= 2;
            case 4:
                if (straight.size() == 4 || straight.size() == 5) {
                    return false;
                }

                if (straight.size() > 6) {
                    return this.getPairNumber(straight) >= 3;
                }

                return this.getPairNumber(straight) >= 2;
            case 5:
                if (straight.size() == 5) {
                    return false;
                }

                return this.getSubStraightNumber(straight) >= 2;
            default:
                return false;
        }
    }

    public boolean isCrushStraight(int id, int number) {
        List<Card> straight = this.getPreStraight(id);
        if (straight == null || straight.isEmpty()) {
            return true; // bug if it happens
        }

        int realStraightSize = straight.get(straight.size() - 1).getCardNumber() - straight.get(0).getCardNumber() + 1;
        if (realStraightSize < number) {
            return true; // bug if it happens
        }

        if (realStraightSize - number >= 3) {
            return false;
        }

        List<Card> tmp = new ArrayList<>(straight);
        int cardNum = straight.get(straight.size() - 1).getCardNumber();
        for (int i = straight.size() - 1; (i >= 0) && (number > 0); i--) {
            if (straight.get(i).getCardNumber() == cardNum) {
                tmp.remove(straight.get(i));
                cardNum--;
                number--;
            }
        }

        if (this.getSubStraightNumber(tmp) > 0) {
            return false;
        }

        return this.getSingleNumber(tmp) > 2;
    }

    public int size() {
        if (this.cards == null || this.cards.isEmpty()) {
            return 0;
        }

        return this.cards.size();
    }

    public boolean isRac(int id, int type) {
        if (id < 32) { // < J
            return true;
        }

        int index = this.getIndexOf(id);
        int turnSize = this.getTurnSize(type);
        if (index + 1 >= turnSize) {
            if ((index + 1 - turnSize) * 2 <= (this.size() - turnSize + 1)) {
                return true;
            }

            return (this.size() - index - 1) + id/4 > 12;
        }

//        if (type == SETTYPE_HIGHCARD && this.cards != null && this.singleIndex != null) {
//            int i;
//            for (i = 0; i < this.singleIndex.size(); i++) {
//                if (this.cards.get(this.singleIndex.get(i)).getId() == id) {
//                    break;
//                }
//            }
//
//            return i * 2 < this.singleIndex.size();
//        }
        return false;
    }

    public boolean isStraightRac(int id, int turnSize) {
        if (id < 28) { // < 10
            return true;
        }

        int index = this.getIndexOf(id);
        if (index + 1 >= turnSize) {
            return (index + 1 - turnSize) * 2 <= (this.size() - turnSize);
        }

        return false;
    }

    public boolean canBeWinAfter(List<Card> catchCards) {
        if (this.cards == null || this.cards.isEmpty()) {
            return false; // bug if it happens
        }

        List<Card> tmp = new ArrayList<>();
        tmp.addAll(this.cards);
        if (catchCards != null && catchCards.isEmpty() == false) {
            for (int i = 0; i < catchCards.size(); i++) {
                tmp.remove(catchCards.get(i));
            }
        }

        Collections.sort(tmp);

        return tmp.isEmpty() || TldlBot.getSetType(tmp) != SETTYPE_UNKNOWN;
    }

    public boolean canBeWinAfter(Card catchCard) {
        if (this.cards == null || this.cards.isEmpty()) {
            return false; // bug if it happens
        }

        List<Card> tmp = new ArrayList<>();
        tmp.addAll(this.cards);
        if (catchCard != null) {
            tmp.remove(catchCard);
        }

        Collections.sort(tmp);

        return tmp.isEmpty() || TldlBot.getSetType(tmp) != SETTYPE_UNKNOWN;
    }

    public Card getOptimalSingle(int opponentLastCard, boolean isNewRound) {
        if (this.cards == null || this.cards.isEmpty() || this.isInRound == false) {
            return null;
        }

        Card card = null;
        // has 2
        if (this.twoList != null && this.twoList.size() == 1) {
            card = this.cards.get(this.twoList.get(this.twoList.size() - 1));
            if (is2(opponentLastCard)) { // 2 vs 2
                return card.getId() > opponentLastCard ? card : null;
            }
        }

        // no 2 vs opponent-2
        if (is2(opponentLastCard)) {
            return null;
        }

        // no 2 && no single
        Card card2 = null;
        if (this.singleIndex2 != null && this.singleIndex2.isEmpty() == false) {
            // single vs single
            for (int i = 0; i < this.singleIndex2.size(); i++) {
                if (this.cards.get(this.singleIndex2.get(i)).getId() > opponentLastCard) {
                    card2 = this.cards.get(this.singleIndex2.get(i));
                    if (this.isRac(card2.getId(), BasicArrangement.SETTYPE_HIGHCARD)) {
                        return card2;
                    }

                    break;
                }
            }
        }

        Card card3 = null;
        if (isNewRound == false && this.singleIndex3 != null && this.singleIndex3.isEmpty() == false) {
            // single vs single
            for (int i = 0; i < this.singleIndex3.size(); i++) {
                if (this.cards.get(this.singleIndex3.get(i)).getId() > opponentLastCard) {
                    card3 = this.cards.get(this.singleIndex3.get(i));
                    if (this.isRac(card3.getId(), BasicArrangement.SETTYPE_HIGHCARD)) {
                        return card3;
                    }

                    break;
                }
            }
        }

        if (card2 != null) {
            return card2;
        }

        if (card3 != null) {
            return card3;
        }

        return card;
    }

    public int getBiggestSingleId(int opponentLastCard) {
        Card card = this.getBiggestSingleCard(opponentLastCard);
        return card == null ? -1 : card.getId();
    }

    public Card getBiggestSingleCard(int opponentLastCard) {
        if (this.cards == null || this.cards.isEmpty() || this.isInRound == false) {
            return null;
        }

        Card card = null;
/*        if (this.twoList != null && this.twoList.isEmpty() == false) {
            card = this.cards.get(this.twoList.get(this.twoList.size() - 1));
            if (is2(opponentLastCard)) { // 2 vs 2
                return card.getId() > opponentLastCard ? card : null;
            }
        }

        // no 2 vs opponent-2
        if (is2(opponentLastCard)) {
            return null;
        }
*/
        card = this.cards.get(this.cards.size() - 1);
        return card.getId() > opponentLastCard ? card : null;
    }

    public int getOptimalStraightId(int number, int opponentLastCard) {
        if (this.cards == null || this.cards.isEmpty() || this.isInRound == false) {
            return -1;
        }

        if (this.straightNumber != null) {
            int id;
            for (int i = 0; i < this.straightNumber.size(); i++) {
                if (this.straightNumber.get(i) == number && this.lastStraight.size() > i) {
                    id = this.lastStraight.get(i).getId();
                    if (id > opponentLastCard && this.isComplexStraight(id) == false) {
                        return id;
                    }
                }

                if (this.straightNumber.get(i) > number && this.lastStraight.size() > i) {
                    id = this.lastStraight.get(i).getId();
                    if (id > opponentLastCard && this.isCrushStraight(id, number) == false) {
                        return id;
                    }
                }
            }
        }

        return -1;
    }

    public int getBiggestStraightId(int number, int opponentLastCard) {
        if (this.cards == null || this.cards.isEmpty() || this.isInRound == false) {
            return -1;
        }

        if (this.straightNumber != null) {
            int id;
            for (int i = this.straightNumber.size() - 1; i >= 0; i--) {
                if (this.straightNumber.get(i) >= number && this.lastStraight.size() > i) {
                    id = this.lastStraight.get(i).getId();
                    if (id > opponentLastCard) {
                        return id;
                    }
                }
            }
        }

        return -1;
    }

    public int getOptimalPairId(int opponentLastCard) {
        if (this.cards == null || this.cards.isEmpty() || this.isInRound == false) {
            return -1;
        }

        int id = -1;
        // has 2
        if (this.twoList != null && this.twoList.size() == 2) {
            id = this.cards.get(this.twoList.get(this.twoList.size() - 1)).getId();
            if (is2(opponentLastCard)) { // 2 vs 2
                return id > opponentLastCard ? id : -1;
            }
        }

        // no 2 vs opponent-2
        if (is2(opponentLastCard)) {
            return -1;
        }

        // no 2 && no pair
        if (this.lastPairIndex == null || this.lastPairIndex.isEmpty()) {
            return id;
        }

        // pair vs pair
        int id2;
        for (int i = 0; i < this.lastPairIndex.size(); i++) {
            id2 = this.cards.get(this.lastPairIndex.get(i)).getId();
            if (id2 > opponentLastCard) {
                return id2;
            }
        }

        return id;
    }

    public int getBiggestPairId(int opponentLastCard) {
        if (this.cards == null || this.cards.isEmpty() || this.isInRound == false) {
            return -1;
        }

        for (int i = this.cards.size() - 1; i > 0; i--) {
            if (this.cards.get(i).getId() <= opponentLastCard) {
                break;
            }

            if (this.cards.get(i).getCardNumber() == this.cards.get(i - 1).getCardNumber()) {
                return this.cards.get(i).getId() > opponentLastCard ? this.cards.get(i).getId() : -1;
            }
        }

        return -1;
    }

    public int getOptimalThreeId(int opponentLastCard) {
        if (this.cards == null || this.cards.isEmpty() || this.isInRound == false) {
            return -1;
        }

        int id = -1;
        // has 2
        if (this.twoList != null && this.twoList.size() == 3) {
            id = this.cards.get(this.twoList.get(this.twoList.size() - 1)).getId();
            if (is2(opponentLastCard)) { // 2 vs 2
                return id > opponentLastCard ? id : -1;
            }
        }

        // no 2 vs opponent-2
        if (is2(opponentLastCard)) {
            return -1;
        }

        // no 2 && no three
        if (this.lastThreeIndex == null || this.lastThreeIndex.isEmpty()) {
            return id;
        }

        // three vs three
        int id2;
        for (int i = 0; i < this.lastThreeIndex.size(); i++) {
            id2 = this.cards.get(this.lastThreeIndex.get(i)).getId();
            if (id2 > opponentLastCard) {
                return id2;
            }
        }

        return id;
    }

    public int getBiggestThreeId(int opponentLastCard) {
        if (this.cards == null || this.cards.isEmpty() || this.isInRound == false) {
            return -1;
        }

        for (int i = this.cards.size() - 1; i > 1; i--) {
            if (this.cards.get(i).getCardNumber() == this.cards.get(i - 1).getCardNumber() &&
                    this.cards.get(i - 1).getCardNumber() == this.cards.get(i - 2).getCardNumber()) {
                return this.cards.get(i).getId() > opponentLastCard ? this.cards.get(i).getId() : -1;
            }
        }

        return -1;
    }

    public int getOptimalFourId(int opponentLastCard) {
        if (this.cards == null || this.cards.isEmpty() || this.isInRound == false) {
            return -1;
        }

        int id = -1;
        // has 2
        if (this.twoList != null && this.twoList.size() == 4) {
            id = this.cards.get(this.twoList.get(this.twoList.size() - 1)).getId();
            if (is2(opponentLastCard)) { // 2 vs 2
                return id > opponentLastCard ? id : -1;
            }
        }

        // no 2 vs opponent-2
        if (is2(opponentLastCard)) {
            return -1;
        }

        // no 2 && no four
        if (this.lastFourIndex == null || this.lastFourIndex.isEmpty()) {
            return id;
        }

        // four vs four
        int id2;
        for (int i = 0; i < this.lastFourIndex.size(); i++) {
            id2 = this.cards.get(this.lastFourIndex.get(i)).getId();
            if (id2 > opponentLastCard) {
                return id2;
            }
        }

        return id;
    }

    public int getBiggestFourId(int opponentLastCard) {
        if (this.cards == null || this.cards.isEmpty() || this.isInRound == false) {
            return -1;
        }

        int id = -1;
        if (this.twoList != null && this.twoList.size() > 3) {
            id = this.cards.get(this.twoList.get(this.twoList.size() - 1)).getId();
            if (is2(opponentLastCard)) { // 2 vs 2
                return id > opponentLastCard ? id : -1;
            }
        }

        // no 2 vs opponent-2
        if (is2(opponentLastCard)) {
            return -1;
        }

        int id2;
        if (this.lastFourIndex != null && this.lastFourIndex.isEmpty() == false) {
            for (int i = this.lastFourIndex.size() - 1; i >= 0; i--) {
                id2 = this.cards.get(this.lastFourIndex.get(i)).getId();
                if (id2 > opponentLastCard) {
                    id = id2;
                    break;
                }
            }
        }

        return id;
    }

    public int getOptimalPairStraightId(int number, int opponentLastCard) {
        if (this.cards == null || this.cards.isEmpty()) {
            return -1;
        }

        if (this.isInRound == false && number == 3) {
            return -1;
        }

        if (this.pairStraightNumber != null) {
            int id;
            for (int i = 0; i < this.pairStraightNumber.size(); i++) {
                if (this.pairStraightNumber.get(i) == number && this.lastPairStraight.size() > i) {
                    id = this.lastPairStraight.get(i).getId();
                    if (id > opponentLastCard) {
                        return id;
                    }
                }
            }
        }

        return -1;
    }

    public int getBiggestPairStraightId(int number, int opponentLastCard) {
        if (this.cards == null || this.cards.isEmpty()) {
            return -1;
        }

        if (this.isInRound == false && number == 3) {
            return -1;
        }

        if (this.pairStraightNumber != null) {
            int id;
            for (int i = this.pairStraightNumber.size() - 1; i >= 0; i--) {
                if (this.pairStraightNumber.get(i) >= number && this.lastPairStraight.size() > i) {
                    id = this.lastPairStraight.get(i).getId();
                    if (id > opponentLastCard) {
                        return id;
                    }
                }
            }
        }

        return -1;
    }

    /**
     * Get set via id in the sort card list *************
     * @param id
     * @return 
     */
    public List<Card> getSingle(int id) {
        if (id < 0 || this.cards == null || this.cards.isEmpty() || this.isInRound == false) {
            return null;
        }

        for (int i = 0; i < this.cards.size(); i++) {
            if (this.cards.get(i).getId() == id) {
                List<Card> ret = new ArrayList<>();
                ret.add(this.cards.get(i));
                return ret;
            }
        }

        return null;
    }

    public List<Card> getPair(int id) {
        if (id < 0 || this.cards == null || this.cards.isEmpty() || this.isInRound == false) {
            return null;
        }

        for (int i = 1; i < this.cards.size(); i++) {
            if (this.cards.get(i).getId() == id) {
                if (this.cards.get(i - 1).getCardNumber() != this.cards.get(i).getCardNumber()) {
                    return null;
                }

                List<Card> ret = new ArrayList<>();
                ret.add(this.cards.get(i - 1));
                ret.add(this.cards.get(i));
                return ret;
            }
        }

        return null;
    }

    public List<Card> getThree(int id) {
        if (id < 0 || this.cards == null || this.cards.isEmpty() || this.isInRound == false) {
            return null;
        }

        for (int i = 2; i < this.cards.size(); i++) {
            if (this.cards.get(i).getId() == id) {
                if (this.cards.get(i - 1).getCardNumber() != this.cards.get(i).getCardNumber() &&
                        this.cards.get(i - 2).getCardNumber() != this.cards.get(i - 1).getCardNumber()) {
                    return null;
                }

                List<Card> ret = new ArrayList<>();
                ret.add(this.cards.get(i - 2));
                ret.add(this.cards.get(i - 1));
                ret.add(this.cards.get(i));
                return ret;
            }
        }

        return null;
    }

    public List<Card> getFour(int id) {
        if (id < 0 || this.cards == null || this.cards.isEmpty() || this.isInRound == false) {
            return null;
        }

        for (int i = 3; i < this.cards.size(); i++) {
            if (this.cards.get(i).getId() == id) {
                if (this.cards.get(i - 1).getCardNumber() != this.cards.get(i).getCardNumber() &&
                        this.cards.get(i - 2).getCardNumber() != this.cards.get(i - 1).getCardNumber() &&
                        this.cards.get(i - 3).getCardNumber() != this.cards.get(i - 2).getCardNumber()) {
                    return null;
                }

                List<Card> ret = new ArrayList<>();
                ret.add(this.cards.get(i - 3));
                ret.add(this.cards.get(i - 2));
                ret.add(this.cards.get(i - 1));
                ret.add(this.cards.get(i));
                return ret;
            }
        }

        return null;
    }

    public List<Card> get3PairStraight(int id) {
        if (id < 0 || this.cards == null || this.cards.isEmpty() || this.isInRound == false) {
            return null;
        }

        for (int i = 5; i < this.cards.size(); i++) {
            if (this.cards.get(i).getId() == id) {
                List<Card> ret = new ArrayList<>();
                if (this.cards.get(i - 1).getCardNumber() != this.cards.get(i).getCardNumber()) {
                    return null;
                }

                ret.add(this.cards.get(i - 1));
                ret.add(this.cards.get(i));

                int tmp = this.cards.get(i).getCardNumber();
                int j = i - 2;
                while (j >= 0 && this.cards.get(j).getCardNumber() == tmp) {
                    j--;
                }

                if (j < 1 || this.cards.get(j - 1).getCardNumber() != this.cards.get(j).getCardNumber()) {
                    return null;
                }

                ret.add(0, this.cards.get(j));
                ret.add(0, this.cards.get(j - 1));

                tmp = this.cards.get(j).getCardNumber();
                j = j - 2;
                while (j >= 0 && this.cards.get(j).getCardNumber() == tmp) {
                    j--;
                }

                if (j < 1 || this.cards.get(j - 1).getCardNumber() != this.cards.get(j).getCardNumber()) {
                    return null;
                }

                ret.add(0, this.cards.get(j));
                ret.add(0, this.cards.get(j - 1));

                return ret;
            }
        }

        return null;
    }

    public List<Card> get4PairStraight(int id) {
        if (id < 0 || this.cards == null || this.cards.isEmpty()) {
            return null;
        }

        for (int i = 7; i < this.cards.size(); i++) {
            if (this.cards.get(i).getId() == id) {
                List<Card> ret = new ArrayList<>();
                if (this.cards.get(i - 1).getCardNumber() != this.cards.get(i).getCardNumber()) {
                    return null;
                }

                ret.add(this.cards.get(i - 1));
                ret.add(this.cards.get(i));

                int tmp = this.cards.get(i).getCardNumber();
                int j = i - 2;
                while (j >= 0 && this.cards.get(j).getCardNumber() == tmp) {
                    j--;
                }

                if (j < 1 || this.cards.get(j - 1).getCardNumber() != this.cards.get(j).getCardNumber()) {
                    return null;
                }

                ret.add(0, this.cards.get(j));
                ret.add(0, this.cards.get(j - 1));

                tmp = this.cards.get(j).getCardNumber();
                j = j - 2;
                while (j >= 0 && this.cards.get(j).getCardNumber() == tmp) {
                    j--;
                }

                if (j < 1 || this.cards.get(j - 1).getCardNumber() != this.cards.get(j).getCardNumber()) {
                    return null;
                }

                ret.add(0, this.cards.get(j));
                ret.add(0, this.cards.get(j - 1));

                tmp = this.cards.get(j).getCardNumber();
                j = j - 2;
                while (j >= 0 && this.cards.get(j).getCardNumber() == tmp) {
                    j--;
                }

                if (j < 1 || this.cards.get(j - 1).getCardNumber() != this.cards.get(j).getCardNumber()) {
                    return null;
                }

                ret.add(0, this.cards.get(j));
                ret.add(0, this.cards.get(j - 1));

                return ret;
            }
        }

        return null;
    }

    public List<Card> getRemainingCards() {
        if (this.cards == null || this.cards.isEmpty()) {
            return null;
        }

        List<Card> ret = new ArrayList<>();
        ret.addAll(this.cards);

        int id = this.getBiggestPairStraightId(4, 0);
        List<Card> tmp = this.get4PairStraight(id);
        if (tmp != null) {
            for (Card card : tmp) {
                ret.remove(card);
            }
        }

        List<Card> tmp2 = new ArrayList<>();
        if (this.lastFourIndex != null) {
            for (int i = 0; i < this.lastFourIndex.size(); i++) {
                tmp = this.getFour(this.cards.get(this.lastFourIndex.get(i)).getId());
                if (tmp == null) {
                    continue;
                }

                tmp2.addAll(tmp);
                for (Card card : tmp) {
                    ret.remove(card);
                }
            }
        }

        if (this.lastPairStraight != null) {
            for (int i = 0; i < this.lastPairStraight.size(); i++) {
                if (this.pairStraightNumber.size() > i && this.pairStraightNumber.get(i) > 3) {
                    continue;
                }

                tmp = this.get3PairStraight(this.lastPairStraight.get(i).getId());
                if (tmp == null || tmp.size() < 6) {
                    continue;
                }

                if (tmp2.contains(tmp.get(0)) || tmp2.contains(tmp.get(2)) || tmp2.contains(tmp.get(4))) {
                    continue;
                }

                for (Card card : tmp) {
                    ret.remove(card);
                }
            }
        }

        return ret;
    }

    public List<Card> getPreStraight(int id) {
        if (id < 0 || this.cards == null || this.cards.isEmpty()) {
            return null;
        }

        List<Card> ret = new ArrayList<>();
        int i;
        for (i = 2; i < this.cards.size(); i++) {
            if (this.cards.get(i).getId() == id) {
                break;
            }
        }

        ret.add(this.cards.get(i));
        for (int j = i - 1; j >= 0; j--) {
            if (this.cards.get(j + 1).getCardNumber() - this.cards.get(j).getCardNumber() <= 1) {
                ret.add(this.cards.get(j));
            } else {
                break;
            }
        }

        Collections.sort(ret);
        return ret;
    }

    public List<Card> getStraight(int id, int number) {
        if (id < 0 || this.cards == null || this.cards.isEmpty() || this.isInRound == false) {
            return null;
        }

        List<Card> ret = new ArrayList<>();
        int i;
        for (i = 2; i < this.cards.size(); i++) {
            if (this.cards.get(i).getId() == id) {
                break;
            }
        }

        ret.add(this.cards.get(i));
        number--;
        int tmp;
        for (int j = i - 1; (j >= 0) && (number > 0); j--) {
            tmp = this.cards.get(j + 1).getCardNumber() - this.cards.get(j).getCardNumber();
            switch (tmp) {
                case 0:
                    continue;
                case 1:
                    ret.add(this.cards.get(j));
                    number--;
                    break;
                default:
                    return null; // not enough card for straight
            }
        }

        Collections.sort(ret);
        return ret;
    }

    public List<Card> getRemainingCards(List<Card> turnCards) {
        List<Card> ret = new ArrayList<>();
        ret.addAll(this.cards);
        if (turnCards == null || turnCards.isEmpty()) {
            return ret;
        }

        for (int i = 0; i < turnCards.size(); i++) {
            ret.remove(turnCards.get(i));
        }

        return ret;
    }

    public boolean has2Straight(int cardNum1, int number1, int cardNum2, int number2) {
        if (this.cards == null || this.cards.isEmpty()) {
            return false;
        }

        for (int i = this.cards.size() - 1; i >= 0; i--) {
            if (number1 > 0 && cardNum1 > 0 && this.cards.get(i).getCardNumber() == cardNum1) {
                cardNum1--;
                number1--;
            } else if (number2 > 0 && cardNum2 > 0 && this.cards.get(i).getCardNumber() == cardNum2) {
                cardNum2--;
                number2--;
            }
        }

        return number1 == 0 && number2 == 0;
    }

    public int getMaxStraightLength() {
        if (this.straightNumber == null || this.lastStraight == null) {
            return 0;
        }

        int ret = 0;
        for (int i = 0; i < this.lastStraight.size(); i++) {
            if (i < this.straightNumber.size() && ret < this.straightNumber.get(i)) {
                ret = this.straightNumber.get(i);
            }
        }

        return ret;
    }

    public List<Card> getRandom() {
        if (this.cards == null || this.cards.isEmpty()) {
            return null;
        }

        Card card = this.cards.get(0);
        List<Card> ret = new ArrayList<>();
        ret.add(card);
        return ret;
    }

    private int getIndexOf(int id) {
        for (int i = 0; i < cards.size(); i++) {
            if (cards.get(i).getId() == id) {
                return i;
            }
        }

        return -1;
    }

    private int getTurnSize(int type) {
        switch (type) {
            case BasicArrangement.SETTYPE_UNKNOWN:
                return 0;
            case BasicArrangement.SETTYPE_HIGHCARD:
                return 1;
            case BasicArrangement.SETTYPE_PAIR:
                return 2;
            case BasicArrangement.SETTYPE_THREE:
                return 3;
            case BasicArrangement.SETTYPE_STRAIGHT:
                return 0; // unknown
            case BasicArrangement.SETTYPE_3PAIR:
                return 6;
            case BasicArrangement.SETTYPE_FOUR:
                return 4;
            case BasicArrangement.SETTYPE_4PAIR:
                return 8;
            default:
                return 0;
        }
    }

    public boolean isBigTurnScore(List<Card> turn) {
        return this.getTurnScore(turn) * 2 > this.getHandScore();
    }

    private double getTurnScore(List<Card> turn) {
        if (turn == null || turn.isEmpty()) {
            return 0;
        }

        double ret = 0;
        for (int i = 0; i < turn.size(); i++) {
            ret += this.getCardScore(turn.get(i));
        }

        return ret;
    }

    private double getHandScore() {
        if (this.cards == null || this.cards.isEmpty()) {
            return 0;
        }

        double ret = 0;
        for (int i = 0; i < this.cards.size(); i++) {
            ret += this.getCardScore(this.cards.get(i));
        }

        return ret;
    }

    private double getCardScore(Card card) {
        if (card == null) {
            return 0;
        }

        double ret = this.getInitialScore(card.getId(), SETTYPE_HIGHCARD);
        if (this.cards == null || this.cards.isEmpty()) {
            return ret;
        }

        if (this.lastPairIndex != null) {
            for (int i = 0; i < this.lastPairIndex.size(); i++) {
                if (this.cards.get(i).getCardNumber() == card.getCardNumber()) {
                    ret += this.getInitialScore(card.getId(), SETTYPE_PAIR) / 2.0;
                    break;
                }
            }
        }

        if (this.lastThreeIndex != null) {
            for (int i = 0; i < this.lastThreeIndex.size(); i++) {
                if (this.cards.get(i).getCardNumber() == card.getCardNumber()) {
                    ret += this.getInitialScore(card.getId(), SETTYPE_PAIR) / 2.0;
                    ret += this.getInitialScore(card.getId(), SETTYPE_THREE) / 3.0;
                    break;
                }
            }
        }

        if (this.lastFourIndex != null) {
            for (int i = 0; i < this.lastFourIndex.size(); i++) {
                if (this.cards.get(i).getCardNumber() == card.getCardNumber()) {
                    ret += this.getInitialScore(card.getId(), SETTYPE_PAIR) / 2.0;
                    ret += this.getInitialScore(card.getId(), SETTYPE_THREE) / 3.0;
                    ret += this.getInitialScore(card.getId(), SETTYPE_FOUR) / 4.0;
                    break;
                }
            }
        }

        if (this.lastStraight != null) {
            for (int i = 0; i < this.lastStraight.size(); i++) {
                if (this.lastStraight.get(i).getCardNumber() >= card.getCardNumber() && this.straightNumber.size() > i &&
                        card.getCardNumber() > this.lastStraight.get(i).getCardNumber() - this.straightNumber.get(i)) {
                    ret += this.getInitialScore(card.getId(), SETTYPE_3PAIR) / 3.0;
                    break;
                }
            }
        }

        if (this.lastPairStraight != null) {
            for (int i = 0; i < this.lastPairStraight.size(); i++) {
                if (this.lastPairStraight.get(i).getCardNumber() >= card.getCardNumber() && this.pairStraightNumber.size() > i &&
                        card.getCardNumber() > this.lastPairStraight.get(i).getCardNumber() - this.pairStraightNumber.get(i)) {
                    ret += this.getInitialScore(card.getId(), SETTYPE_4PAIR) * 1.0 / this.pairStraightNumber.get(i);
                    break;
                }
            }
        }

        return ret;
    }

    private int getInitialScore(int cardId, int type) {
        switch (type) {
            case BasicArrangement.SETTYPE_UNKNOWN:
                return 0;
            case BasicArrangement.SETTYPE_HIGHCARD:
                return cardId;
            case BasicArrangement.SETTYPE_PAIR:
                return cardId;
            case BasicArrangement.SETTYPE_THREE:
                return cardId;
            case BasicArrangement.SETTYPE_STRAIGHT:
                return cardId;
            case BasicArrangement.SETTYPE_3PAIR:
                return 44 + cardId; // 51 - 7 + (8 --> 47)
            case BasicArrangement.SETTYPE_FOUR:
                return 92 + cardId; // 51 + 40 [8 --> 47] + (0 --> 47)
            case BasicArrangement.SETTYPE_4PAIR:
                return 128 + cardId; // 51 + 40 [8 --> 47] + 48 [0 --> 47] + (12 --> 47) ==> 139 - 11 + ... ==> max = 175
            default:
                return 0;
        }
    }

    public int getHighCardNumber() {
        if (this.singleIndex2 == null) {
            return 0;
        }

        return this.singleIndex2.size();
    }

    public Card getOptimal2(int opponentLastCard) {
        if (this.cards == null || this.cards.isEmpty() || this.isInRound == false) {
            return null;
        }

        if (this.twoList == null || this.twoList.isEmpty()) {
            return null;
        }

        Card card = null;
        // has 2
        for (int i = 0; i < this.twoList.size(); i++) {
            card = this.cards.get(this.twoList.get(i));
            if (card.getId() > opponentLastCard) {
                return card;
            }
        }

        return null;
    }

    public int getOptimalPair2(int opponentLastCard) {
        if (this.cards == null || this.cards.isEmpty() || this.isInRound == false) {
            return -1;
        }

        if (this.twoList == null || this.twoList.size() < 2) {
            return -1;
        }

        int cardId = -1;
        // has 2
        for (int i = 1; i < this.twoList.size(); i++) {
            cardId = this.cards.get(this.twoList.get(i)).getId();
            if (cardId > opponentLastCard) {
                return cardId;
            }
        }

        return -1;
    }
}
