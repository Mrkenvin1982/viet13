/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 * @author
 */
public class AutoArrangementBotNew {

    /**
     * Sort a card list.
     *
     * @param cards card list.
     * @return an sorted card list.
     */
    public static List<Card> sortCardByOrder(List<Card> cards) {
        // Check cards.
        if (cards == null || cards.isEmpty()) {
            return null;
        }

        Collections.sort(cards);
        return cards;
    }

    /**
     * Sort a card list.
     *
     * @param cards card list.
     * @return an sorted card list.
     */
    public static List<Card> sortCardByType(List<Card> cards) {
        // Check cards.
        if (cards == null || cards.isEmpty()) {
            return null;
        }

        List<Card> heartList = new ArrayList<>();
        List<Card> diamondList = new ArrayList<>();
        List<Card> clubList = new ArrayList<>();
        List<Card> spadeList = new ArrayList<>();
        for (int i = 0; i < cards.size(); i++) {
            // Check type of card.
            switch (cards.get(i).getCardType()) {
                case MauBinhCardSet.TYPE_HEART:
                    heartList.add(cards.get(i));
                    break;
                case MauBinhCardSet.TYPE_DIAMOND:
                    diamondList.add(cards.get(i));
                    break;
                case MauBinhCardSet.TYPE_CLUB:
                    clubList.add(cards.get(i));
                    break;
                case MauBinhCardSet.TYPE_SPADE:
                    spadeList.add(cards.get(i));
                    break;
                default:
                    break;
            }
        }

        List<Integer> typeNumberList = new ArrayList<>();
        typeNumberList.add(heartList.size());
        typeNumberList.add(diamondList.size());
        typeNumberList.add(clubList.size());
        typeNumberList.add(spadeList.size());
        List<Integer> sortedList = new ArrayList<>();
        sortedList.addAll(typeNumberList);
        Collections.sort(sortedList);

        List<Card> ret = new ArrayList<>();
        for (int i = 0; i < sortedList.size(); i++) {
            int index = typeNumberList.indexOf(sortedList.get(i));
            typeNumberList.set(index, -1);
            switch (index) {
                case 0:
                    ret.addAll(heartList);
                    break;
                case 1:
                    ret.addAll(diamondList);
                    break;
                case 2:
                    ret.addAll(clubList);
                    break;
                case 3:
                    ret.addAll(spadeList);
                    break;
                default:
                    break;
            }
        }

        return ret;
    }

    /**
     * Get an arrange solution.
     *
     * @param cards card list.
     * @return an arrange card list.
     */
    public static List<Card> getSolution(List<Card> cards) {
        // Check cards.
        if (cards == null || cards.size() != MauBinhConfig.DEFAULT_NUMBER_CARD) {
            return null;
        }

        // Get optimal 3rd set.
        List<Card> lastSet = AutoArrangementBotNew.getOptimalBigSet(cards);
        List<Card> remaining = new ArrayList<>();
        for (int i = 0; i < cards.size(); i++) {
            if (lastSet.contains(cards.get(i))) {
                continue;
            }

            remaining.add(cards.get(i));
        }

        // Return for special cases.
        if (remaining.isEmpty()) {
            return lastSet;
        }

        // Then get optimal 2nd set.
        List<Card> middleSet = AutoArrangementBotNew.getOptimalBigSet(remaining);
        List<Card> remaining2 = new ArrayList<>();
        for (int i = 0; i < remaining.size(); i++) {
            if (middleSet.contains(remaining.get(i))) {
                continue;
            }

            remaining2.add(remaining.get(i));
        }

        // Then get optimal 1st set.
        List<Card> firstSet = AutoArrangementBotNew.getOptimalSmallSet(remaining2);

        List<Card> ret = new ArrayList<>();
        ret.addAll(firstSet);
        ret.addAll(middleSet);
        for (int i = remaining2.size() - 1; i >= 0; i--) {
            if (firstSet.contains(remaining2.get(i))) {
                continue;
            }

            ret.add(remaining2.get(i));
        }

        // Combine to return.
        ret.addAll(lastSet);

        return ret;
    }

    private static List<Card> getOptimalBigSet(List<Card> cards) {
        // Check cards.
        if (cards == null || cards.size() < MauBinhConfig.NUMBER_CARD_BIG_SET) {
            return null;
        }

        List<Card> heartList = new ArrayList<>();
        List<Card> diamondList = new ArrayList<>();
        List<Card> clubList = new ArrayList<>();
        List<Card> spadeList = new ArrayList<>();
        switch (cards.get(0).getCardType()) {
            case MauBinhCardSet.TYPE_HEART:
                heartList.add(cards.get(0));
                break;
            case MauBinhCardSet.TYPE_DIAMOND:
                diamondList.add(cards.get(0));
                break;
            case MauBinhCardSet.TYPE_CLUB:
                clubList.add(cards.get(0));
                break;
            case MauBinhCardSet.TYPE_SPADE:
                spadeList.add(cards.get(0));
                break;
            default:
                break;
        }

        List<Integer> singleIndex = new ArrayList<>();
        List<Integer> lastPairIndex = new ArrayList<>();
        List<Integer> lastThreeIndex = new ArrayList<>();
        int lastFourIndex = -1;

        int sameCardNo = 0;
        for (int i = 1; i < cards.size(); i++) {
            // Check type of card.
            switch (cards.get(i).getCardType()) {
                case MauBinhCardSet.TYPE_HEART:
                    heartList.add(cards.get(i));
                    break;
                case MauBinhCardSet.TYPE_DIAMOND:
                    diamondList.add(cards.get(i));
                    break;
                case MauBinhCardSet.TYPE_CLUB:
                    clubList.add(cards.get(i));
                    break;
                case MauBinhCardSet.TYPE_SPADE:
                    spadeList.add(cards.get(i));
                    break;
                default:
                    break;
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
                        lastFourIndex = i - 1;
                        break;
                    default:
                        break;
                }

                sameCardNo = 0;
            }
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
                lastFourIndex = cards.size() - 1;
                break;
            default:
                break;
        }

        // Get biggest straight flush.
        List<Card> ret = AutoArrangementBotNew.getOptimalStraightFlush(
                heartList, diamondList, clubList, spadeList);
        if (ret != null) {
            return ret;
        }

        // Get biggest Four of a Kind.
        if (lastFourIndex == 3) {
            ret = new ArrayList<>();
            ret.add(cards.get(0));
            ret.add(cards.get(1));
            ret.add(cards.get(2));
            ret.add(cards.get(3));
//            ret.add(cards.get(4)); // add a single later
            return ret;
        } else if (lastFourIndex > 3) {
            ret = new ArrayList<>();
//            ret.add(cards.get(0)); // add a single later
            ret.add(cards.get(lastFourIndex - 3));
            ret.add(cards.get(lastFourIndex - 2));
            ret.add(cards.get(lastFourIndex - 1));
            ret.add(cards.get(lastFourIndex));
            return ret;
        }

        // Get biggest Full house.
        if (lastThreeIndex.isEmpty() == false) {
            // If has a pair.
            if (lastPairIndex.isEmpty() == false) {
                ret = new ArrayList<>();
                ret.add(cards.get(lastPairIndex.get(0) - 1));
                ret.add(cards.get(lastPairIndex.get(0)));
                // Get the biggest.
                int tempIndex = lastThreeIndex.get(lastThreeIndex.size() - 1);
                ret.add(cards.get(tempIndex - 2));
                ret.add(cards.get(tempIndex - 1));
                ret.add(cards.get(tempIndex));
                Collections.sort(ret);
                return ret;
            }

            // If there is NOT any pair, then check three of kind.
            if (lastThreeIndex.size() > 1) {
                ret = new ArrayList<>();
                ret.add(cards.get(lastThreeIndex.get(0) - 1));
                ret.add(cards.get(lastThreeIndex.get(0)));
                // Get the biggest.
                int tempIndex = lastThreeIndex.get(lastThreeIndex.size() - 1);
                ret.add(cards.get(tempIndex - 2));
                ret.add(cards.get(tempIndex - 1));
                ret.add(cards.get(tempIndex));
                Collections.sort(ret);
                return ret;
            }
        }

        // Get biggest flush.
        ret = AutoArrangementBotNew.getOptimalFlush(heartList, diamondList, clubList, spadeList);
        if (ret != null) {
            return ret;
        }

        // Get biggest straight.
        ret = AutoArrangementBotNew.getOptimalStraight(cards);
        if (ret != null) {
            return ret;
        }

        // Get biggest Three of a Kind.
//        if (lastThreeIndex.isEmpty() == false && singleIndex.size() >= 2) {
        if (lastThreeIndex.isEmpty() == false) {
            ret = new ArrayList<>();
            // Get 2 of smallest Three of Kind.
//            ret.add(cards.get(singleIndex.get(0))); // add a single later
//            ret.add(cards.get(singleIndex.get(1))); // add a single later
            // Get the biggest.
            int tempIndex = lastThreeIndex.get(lastThreeIndex.size() - 1);
            ret.add(cards.get(tempIndex - 2));
            ret.add(cards.get(tempIndex - 1));
            ret.add(cards.get(tempIndex));
            Collections.sort(ret);
            return ret;
        }

        // For 5 pairs.
        if (lastPairIndex.size() == 5 && singleIndex.size() == 3) {
            ret = new ArrayList<>();
            // 1st set.
            ret.add(cards.get(singleIndex.get(2)));
            ret.add(cards.get(lastPairIndex.get(4) - 1));
            ret.add(cards.get(lastPairIndex.get(4)));
            // 2nd set.
            ret.add(cards.get(singleIndex.get(1)));
            ret.add(cards.get(lastPairIndex.get(2) - 1));
            ret.add(cards.get(lastPairIndex.get(2)));
            ret.add(cards.get(lastPairIndex.get(1) - 1));
            ret.add(cards.get(lastPairIndex.get(1)));
            // 3rd set.
            ret.add(cards.get(singleIndex.get(0)));
            ret.add(cards.get(lastPairIndex.get(3) - 1));
            ret.add(cards.get(lastPairIndex.get(3)));
            ret.add(cards.get(lastPairIndex.get(0) - 1));
            ret.add(cards.get(lastPairIndex.get(0)));
            return ret;
        }

        // For 4 pairs.
        if (lastPairIndex.size() == 4 && singleIndex.size() == 5) {
            ret = new ArrayList<>();
            // 1st set.
            ret.add(cards.get(singleIndex.get(4)));
            ret.add(cards.get(lastPairIndex.get(2) - 1));
            ret.add(cards.get(lastPairIndex.get(2)));
            // 2nd set.
            ret.add(cards.get(singleIndex.get(3)));
            ret.add(cards.get(singleIndex.get(2)));
            ret.add(cards.get(singleIndex.get(1)));
            ret.add(cards.get(lastPairIndex.get(3) - 1));
            ret.add(cards.get(lastPairIndex.get(3)));
            // 3rd set.
            ret.add(cards.get(singleIndex.get(0)));
            ret.add(cards.get(lastPairIndex.get(1) - 1));
            ret.add(cards.get(lastPairIndex.get(1)));
            ret.add(cards.get(lastPairIndex.get(0) - 1));
            ret.add(cards.get(lastPairIndex.get(0)));
            return ret;
        }

        // For 3 pairs.
        if (lastPairIndex.size() == 3 && singleIndex.size() == 7) {
            ret = new ArrayList<>();
            // 1st set.
            ret.add(cards.get(singleIndex.get(6)));
            ret.add(cards.get(lastPairIndex.get(0) - 1));
            ret.add(cards.get(lastPairIndex.get(0)));
            // 2nd set.
            ret.add(cards.get(singleIndex.get(5)));
            ret.add(cards.get(singleIndex.get(4)));
            ret.add(cards.get(singleIndex.get(3)));
            ret.add(cards.get(lastPairIndex.get(1) - 1));
            ret.add(cards.get(lastPairIndex.get(1)));
            // 3rd set.
            ret.add(cards.get(singleIndex.get(2)));
            ret.add(cards.get(singleIndex.get(1)));
            ret.add(cards.get(singleIndex.get(0)));
            ret.add(cards.get(lastPairIndex.get(2) - 1));
            ret.add(cards.get(lastPairIndex.get(2)));
            return ret;
        }

        // For 2nd set collection.
        // Get biggest pairs.
//        if (lastPairIndex.size() > 1 && singleIndex.isEmpty() == false) {
        if (lastPairIndex.size() > 2) { // 3 pairs for 2nd set.
            ret = new ArrayList<>();
//            ret.add(cards.get(singleIndex.get(0))); // add a single later
            ret.add(cards.get(lastPairIndex.get(0) - 1));
            ret.add(cards.get(lastPairIndex.get(0)));
            ret.add(cards.get(lastPairIndex.get(1) - 1));
            ret.add(cards.get(lastPairIndex.get(1)));
            Collections.sort(ret);
            return ret;
        }

        // < 3 pairs for 2nd set.
        // Get biggest pair.
//        if (lastPairIndex.isEmpty() == false && singleIndex.size() >= 3) {
        if (lastPairIndex.isEmpty() == false) {
            ret = new ArrayList<>();
//            ret.add(cards.get(singleIndex.get(0))); // add a single later
//            ret.add(cards.get(singleIndex.get(1))); // add a single later
//            ret.add(cards.get(singleIndex.get(2))); // add a single later
            ret.add(cards.get(lastPairIndex.get(lastPairIndex.size() - 1) - 1));
            ret.add(cards.get(lastPairIndex.get(lastPairIndex.size() - 1)));
            Collections.sort(ret);
            return ret;
        }

        // Get biggest cards.
        ret = new ArrayList<>();
        ret.add(cards.get(cards.size() - 5));
        ret.add(cards.get(cards.size() - 4));
        ret.add(cards.get(cards.size() - 3));
        ret.add(cards.get(cards.size() - 2));
        ret.add(cards.get(cards.size() - 1));

        return ret;
    }

    private static List<Card> getOptimalStraight(List<Card> cards) {
        // Check cards.
        if (cards == null || cards.isEmpty()
                || cards.size() < MauBinhConfig.NUMBER_CARD_BIG_SET) {
            return null;
        }

        List<Card> ret = getOptimalNormalStraight(cards);
        // Check the 1st straight: 10JQKA.
        if (ret != null && ret.isEmpty() == false
                && MauBinhCardSet.isAce(ret.get(ret.size() - 1))) {
            return ret;
        }

        // Check the 2nd straight: A2345.
        List<Card> temp = get2ndStraight(cards);
        if (temp != null && temp.isEmpty() == false) {
            return temp;
        }

        return ret;
    }

    private static List<Card> get2ndStraight(List<Card> cards) {
        // Check cards.
        if (cards == null || cards.isEmpty()
                || cards.size() < MauBinhConfig.NUMBER_CARD_BIG_SET) {
            return null;
        }

        List<Card> ret = new ArrayList<>();
        // Check if the last card is NOT Ace then return.
        // Check if the first card is NOT 2 then return.
        if (MauBinhCardSet.isAce(cards.get(cards.size() - 1)) == false
                || MauBinhCardSet.is2(cards.get(0)) == false) {
            return null;
        }

        // Add the Ace and 2.
        ret.add(cards.get(cards.size() - 1));
        ret.add(cards.get(0));
        for (int i = 1; i < cards.size() - 1; i++) {
            int temp = cards.get(i).getCardNumber() - cards.get(i - 1).getCardNumber();
            switch (temp) {
                case 0: // Discard the duplicated cards.
                    break;
                case 1: // Add the next card.
                    ret.add(cards.get(i));
                    break;
                default: // NOT straight.
                    return null;
            }

            if (ret.size() == MauBinhConfig.NUMBER_CARD_BIG_SET) {
                Collections.sort(ret);
                return ret;
            }
        }

        return null;
    }

    private static List<Card> getOptimalNormalStraight(List<Card> cards) {
        // Check cards.
        if (cards == null || cards.isEmpty()
                || cards.size() < MauBinhConfig.NUMBER_CARD_BIG_SET) {
            return null;
        }

        List<Card> ret = new ArrayList<>();
        // Add the highest card.
        ret.add(cards.get(cards.size() - 1));
        for (int i = cards.size() - 2; i >= 0; i--) {
            int temp = cards.get(i + 1).getCardNumber() - cards.get(i).getCardNumber();
            switch (temp) {
                case 0:
                    break;
                case 1:
                    ret.add(cards.get(i));
                    break;
                default:
                    ret.clear();
                    ret.add(cards.get(i));
                    break;
            }

            if (ret.size() == MauBinhConfig.NUMBER_CARD_BIG_SET) {
                Collections.sort(ret);
                return ret;
            }
        }

        return null;
    }

    private static List<Card> getOptimalStraightFlush(
            List<Card> heartList, List<Card> diamondList, List<Card> clubList, List<Card> spadeList) {

        List<Card> ret = null;
        List<Card> temp = null;
        // Check heart cards.
        if (heartList != null && heartList.size() >= MauBinhConfig.NUMBER_CARD_BIG_SET) {
            if (ret == null) {
                ret = AutoArrangementBotNew.getOptimalStraight(heartList);
            } else {
                temp = AutoArrangementBotNew.getOptimalStraight(heartList);
                if (temp != null && temp.get(temp.size() - 1).getCardNumber()
                        > ret.get(ret.size() - 1).getCardNumber()) {
                    ret = temp;
                }
            }
        }

        // Check diamond cards.
        if (diamondList != null && diamondList.size() >= MauBinhConfig.NUMBER_CARD_BIG_SET) {
            if (ret == null) {
                ret = AutoArrangementBotNew.getOptimalStraight(diamondList);
            } else {
                temp = AutoArrangementBotNew.getOptimalStraight(diamondList);
                if (temp != null && temp.get(temp.size() - 1).getCardNumber()
                        > ret.get(ret.size() - 1).getCardNumber()) {
                    ret = temp;
                }
            }
        }

        // Check club cards.
        if (clubList != null && clubList.size() >= MauBinhConfig.NUMBER_CARD_BIG_SET) {
            if (ret == null) {
                ret = AutoArrangementBotNew.getOptimalStraight(clubList);
            } else {
                temp = AutoArrangementBotNew.getOptimalStraight(clubList);
                if (temp != null && temp.get(temp.size() - 1).getCardNumber()
                        > ret.get(ret.size() - 1).getCardNumber()) {
                    ret = temp;
                }
            }
        }

        // Check spade cards.
        if (spadeList != null && spadeList.size() >= MauBinhConfig.NUMBER_CARD_BIG_SET) {
            if (ret == null) {
                ret = AutoArrangementBotNew.getOptimalStraight(spadeList);
            } else {
                temp = AutoArrangementBotNew.getOptimalStraight(spadeList);
                if (temp != null && temp.get(temp.size() - 1).getCardNumber()
                        > ret.get(ret.size() - 1).getCardNumber()) {
                    ret = temp;
                }
            }
        }

        return ret;
    }

    private static List<Card> getOptimalFlush(
            List<Card> heartList, List<Card> diamondList, List<Card> clubList, List<Card> spadeList) {

        List<Card> temp = null;
        // Check heart cards.
        if (heartList != null && heartList.size() >= MauBinhConfig.NUMBER_CARD_BIG_SET) {
            if (temp == null) {
                temp = heartList;
            }
        }

        // Check diamond cards.
        if (diamondList != null && diamondList.size() >= MauBinhConfig.NUMBER_CARD_BIG_SET) {
            if (temp == null) {
                temp = diamondList;
            } else {
                if (GameChecker.compareCardByCard(diamondList, temp) == MauBinhConfig.RESULT_WIN) {
                    temp = diamondList;
                }
            }
        }

        // Check club cards.
        if (clubList != null && clubList.size() >= MauBinhConfig.NUMBER_CARD_BIG_SET) {
            if (temp == null) {
                temp = clubList;
            } else {
                if (GameChecker.compareCardByCard(clubList, temp) == MauBinhConfig.RESULT_WIN) {
                    temp = clubList;
                }
            }
        }

        // Check spade cards.
        if (spadeList != null && spadeList.size() >= MauBinhConfig.NUMBER_CARD_BIG_SET) {
            if (temp == null) {
                temp = spadeList;
            } else {
                if (GameChecker.compareCardByCard(spadeList, temp) == MauBinhConfig.RESULT_WIN) {
                    temp = spadeList;
                }
            }
        }

        if (temp == null) {
            return null;
        }

        List<Card> ret = new ArrayList<>();
        ret.add(temp.get(temp.size() - 5));
        ret.add(temp.get(temp.size() - 4));
        ret.add(temp.get(temp.size() - 3));
        ret.add(temp.get(temp.size() - 2));
        ret.add(temp.get(temp.size() - 1));

        return ret;
    }

    private static List<Card> getOptimalSmallSet(List<Card> cards) {
        // Check cards.
        if (cards == null || cards.size() < MauBinhConfig.NUMBER_CARD_SMALL_SET) { // NUMBER_CARD_SMALL_SET = 3
            return null;
        }

        List<Integer> singleIndex = new ArrayList<>();
        List<Integer> lastPairIndex = new ArrayList<>();
        List<Integer> lastThreeIndex = new ArrayList<>();

        int sameCardNo = 0;
        for (int i = 1; i < cards.size(); i++) {
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
                    case 3: // Four of a Kind.
                        lastThreeIndex.add(i - 1);
                        break;
                    default:
                        break;
                }

                sameCardNo = 0;
            }
        }

        switch (sameCardNo) {
            case 0:
                singleIndex.add(cards.size() - 1);
                break;
            case 1: // a pair.
                lastPairIndex.add(cards.size() - 1);
                break;
            case 2: // Three of a kind.
            case 3: // Four of a Kind.
                lastThreeIndex.add(cards.size() - 1);
                break;
            default:
                break;
        }

        List<Card> ret;
        // Get biggest Three of a Kind.
        if (lastThreeIndex.isEmpty() == false) {
            ret = new ArrayList<>();
            // Get the biggest.
            int tempIndex = lastThreeIndex.get(lastThreeIndex.size() - 1);
            ret.add(cards.get(tempIndex - 2));
            ret.add(cards.get(tempIndex - 1));
            ret.add(cards.get(tempIndex));
            Collections.sort(ret);
            return ret;
        }

        // Get biggest pair.
        if (lastPairIndex.isEmpty() == false && singleIndex.size() >= 1) {
            ret = new ArrayList<>();
            ret.add(cards.get(singleIndex.get(singleIndex.size() - 1)));
            ret.add(cards.get(lastPairIndex.get(lastPairIndex.size() - 1) - 1));
            ret.add(cards.get(lastPairIndex.get(lastPairIndex.size() - 1)));
            Collections.sort(ret);
            return ret;
        }

        // Get biggest cards.
        ret = new ArrayList<>();
        ret.add(cards.get(cards.size() - 3));
        ret.add(cards.get(cards.size() - 2));
        ret.add(cards.get(cards.size() - 1));

        return ret;
    }

    /*==========================================================================================================*/
    public static byte[] getBestSolution(byte[] cards) {
        List<Card> listCard = getBestSolution(convertCards(cards));
        if (listCard != null) {
            byte[] ids = new byte[listCard.size()];
            for (int i=0; i<ids.length; i++) {
                ids[i] = listCard.get(i).getId();
            }
            return ids;
        }
        return null;
    }

    public static List<Card> getBestSolution(List<Card> cards) {
        // Check cards.
        if (cards == null || cards.size() != MauBinhConfig.DEFAULT_NUMBER_CARD) {
            return null;
        }

        // Get all big set.
        List<EstimatedSet> allBigSet = AutoArrangementBotNew.getAllBigSet(cards);
        if (allBigSet == null || allBigSet.isEmpty()) {
            return null;
        }
//
//        System.out.println("Big set number: " + allBigSet.size());
//        for (int i = 0; i < allBigSet.size(); i++) {
//            System.out.println(allBigSet.get(i).toString());
//        }
	int tmpId = AutoArrangementBotNew.getRandomRateId();

        EstimatedSet firstSet = null;
        EstimatedSet lastSet = new EstimatedSet();
        EstimatedSet middleSet = new EstimatedSet();
        List<Card> remaining = new ArrayList<>();
        List<Card> ret = new ArrayList<>();
        List<Card> temp = new ArrayList<>();
        double score = Integer.MIN_VALUE;
        double tmpScore;
        for (int i = 0; i < allBigSet.size(); i++) {
            for (int j = i; j < allBigSet.size(); j++) {
                if (j == i) {
                    if (allBigSet.get(i).getSet().size() < (2 * MauBinhConfig.NUMBER_CARD_BIG_SET)) {
                        continue;
                    }

                    // process a set with size >= 10
                    if (allBigSet.get(i).getType() == SetType.FLUSH) {
                        lastSet.clear();
                        lastSet.setType(SetType.FLUSH);
                        middleSet.clear();
                        middleSet.setType(SetType.FLUSH);
                        remaining.clear();
                        for (int k = 0; k < cards.size(); k++) {
                            if (allBigSet.get(i).contains(cards.get(k))) {
                                continue;
                            }

                            remaining.add(cards.get(k));
                        }

                        List<Card> dupList = new ArrayList<>();
                        for (int k = cards.size() - 1; k >= 0; k--) {
                            for (int n = 0; n < remaining.size(); n++) {
                                if (cards.get(k).getCardNumber() == remaining.get(n).getCardNumber()) {
                                    if (remaining.size() + dupList.size() < MauBinhConfig.NUMBER_CARD_SMALL_SET) {
                                        dupList.add(cards.get(k));
                                    }
                                }
                            }
                        }

                        remaining.addAll(dupList);
                        for (int k = 0; k < cards.size(); k++) {
                            if (remaining.contains(cards.get(k)) == false && remaining.size() < MauBinhConfig.NUMBER_CARD_SMALL_SET) {
                                remaining.add(cards.get(k));
                            }
                        }

                        for (int k = cards.size() - 1; k >= 0; k--) {
                            if (remaining.contains(cards.get(k))) {
                                continue;
                            }

                            if (lastSet.getSet().size() <= middleSet.getSet().size()) {
                                lastSet.getSet().add(cards.get(k));
                                lastSet.addCard(cards.get(k).getCardNumber());
                            } else {
                                middleSet.getSet().add(cards.get(k));
                                middleSet.addCard(cards.get(k).getCardNumber());
                            }
                        }

                        lastSet.toFinalSet(remaining);
                        middleSet.toFinalSet(remaining);
						Collections.sort(remaining);
                        firstSet = AutoArrangementBotNew.getSmallSet(remaining);
                        if (firstSet == null) {
                            continue;
                        }
                    } else { // Straight & Straight-Flush
                        List<Card> tmpSet = AutoArrangementBotNew.getOptimalStraight(cards);
                        if (tmpSet == null) {
                            continue;
                        }

                        Collections.sort(tmpSet);
                        lastSet.clear();
                        lastSet.setType(allBigSet.get(i).getType());
                        lastSet.getSet().addAll(tmpSet);
                        for (int k = tmpSet.size() - 1; k >= 0; k--) {
                            lastSet.addCard(tmpSet.get(k).getCardNumber());
                        }

                        remaining.clear();
                        for (int k = 0; k < cards.size(); k++) {
                            if (tmpSet.contains(cards.get(k))) {
                                continue;
                            }

                            remaining.add(cards.get(k));
                        }

                        tmpSet = AutoArrangementBotNew.getOptimalStraight(remaining);
                        if (tmpSet == null) {
                            continue;
                        }

                        Collections.sort(tmpSet);
                        middleSet.clear();
                        middleSet.setType(allBigSet.get(i).getType());
                        middleSet.getSet().addAll(tmpSet);
                        for (int k = tmpSet.size() - 1; k >= 0; k--) {
                            middleSet.addCard(tmpSet.get(k).getCardNumber());
                        }

                        List<Card> remaining2 = new ArrayList<>();
                        for (int k = 0; k < remaining.size(); k++) {
                            if (tmpSet.contains(remaining.get(k))) {
                                continue;
                            }

                            remaining2.add(remaining.get(k));
                        }

						Collections.sort(remaining2);
                        firstSet = AutoArrangementBotNew.getSmallSet(remaining2);
                        if (firstSet == null) {
                            continue;
                        }

                        // remaining co dung phia sau, chua nhung la le, can clear trong case nay
                        remaining.clear();
                    }
                } else {
                    lastSet.clear();
                    middleSet.clear();
                    if (AutoArrangementBotNew.isCouple(allBigSet.get(i), allBigSet.get(j), lastSet, middleSet) == false) {
                        continue;
                    }

                    remaining.clear();
                    for (int k = 0; k < cards.size(); k++) {
                        if (lastSet.contains(cards.get(k)) || middleSet.contains(cards.get(k))) {
                            continue;
                        }

                        remaining.add(cards.get(k));
                    }

                    lastSet.toFinalSet(remaining);
                    middleSet.toFinalSet(remaining);

                    // Then get optimal 1st set.
					Collections.sort(remaining);
                    firstSet = AutoArrangementBotNew.getSmallSet(remaining);
                    if (firstSet == null || firstSet.compareWith(middleSet) > 0 || middleSet.compareWith(lastSet) > 0) {
//                        System.out.println("lastSet: " + lastSet.toString());
//                        System.out.println("middleSet: " + middleSet.toString());
                        continue;
                    }
                }

                temp.clear();
                temp.addAll(firstSet.getSet());
                temp.addAll(middleSet.getSet());
                for (int k = remaining.size() - 1; k >= 0; k--) {
                    if (firstSet.contains(remaining.get(k))) {
                        continue;
                    }

                    temp.add(remaining.get(k));
                }

                // Combine to a solution.
                temp.addAll(lastSet.getSet());

//                for (int k = 0; k < temp.size(); k++) {
//                    System.out.print("" + temp.get(k).getCardNumber() + ", ");
//                }
                // Check score of this solution with current best
                tmpScore = AutoArrangementBotNew.getScore(lastSet, middleSet, firstSet, tmpId);
//                System.out.println("score: " + tmpScore);
                if (score < tmpScore) {
                    score = tmpScore;
                    ret = new ArrayList<>(temp);
                }
            }
        }

        return ret.isEmpty() ? null : ret;
    }

    /**
     * - a, b có tạo thành lastSet, middleSet hợp lệ
     *
     * @param a
     * @param b
     * @param lastSet
     * @param middleSet
     * @return
     */
    private static boolean isCouple(EstimatedSet a, EstimatedSet b, EstimatedSet lastSet, EstimatedSet middleSet) {
        if (a == null || b == null) {
            return false;
        }

        Set<Card> setA = a.getSet();
        Set<Card> setB = b.getSet();
        if (setA == null || setA.isEmpty() || setB == null || setB.isEmpty()) {
            return false;
        }

        List<Card> remaining = new ArrayList<>();
        if (setA.size() <= MauBinhConfig.NUMBER_CARD_BIG_SET) { // setA is normal set
            for (Card card : setB) {
                if (setA.contains(card) == false) {
                    remaining.add(card);
                }
            }

            // no common item
            if (remaining.size() == setB.size()) {
                if (a.compareWith(b) > 0) {
                    lastSet.cloneWith(a);
                    middleSet.cloneWith(b);
                } else {
                    lastSet.cloneWith(b);
                    middleSet.cloneWith(a);
                }

                return true;
            }

            // has common item
            if (setB.size() <= MauBinhConfig.NUMBER_CARD_BIG_SET) { // setB is normal set
                return false;
            } else { // setB is special set
                Collections.sort(remaining);
                EstimatedSet b2 = AutoArrangementBotNew.getSpecialSet(remaining);
                if (b2 == null) {
                    return false;
                }

                if (a.compareWith(b2) > 0) {
                    lastSet.cloneWith(a);
                    middleSet.cloneWith(b2);
                } else {
                    lastSet.cloneWith(b2);
                    middleSet.cloneWith(a);
                }

                return true;
            }
        } else { //setA is special flush/straight/full-straight
            for (Card card : setA) {
                if (setB.contains(card) == false) {
                    remaining.add(card);
                }
            }

            // no common item
            if (remaining.size() == setA.size()) {
                if (a.compareWith(b) > 0) {
                    lastSet.cloneWith(a);
                    middleSet.cloneWith(b);
                } else {
                    lastSet.cloneWith(b);
                    middleSet.cloneWith(a);
                }

                return true;
            }

            // has common item
            if (setB.size() <= MauBinhConfig.NUMBER_CARD_BIG_SET) { // setB is normal set
                Collections.sort(remaining);
                EstimatedSet a2 = AutoArrangementBotNew.getSpecialSet(remaining);
                if (a2 == null) {
                    return false;
                }

                if (a2.compareWith(b) > 0) {
                    lastSet.cloneWith(a2);
                    middleSet.cloneWith(b);
                } else {
                    lastSet.cloneWith(b);
                    middleSet.cloneWith(a2);
                }

                return true;
            } else { // setB is special set ==> A & B are special
                if (a.isFlush()) {
                    if (b.isFlush()) {
                        // if flush A & B has not same type, then has no common item and return true above.
                        // else then exists a set of all cards in A & B.
                        return false;
                    }

                    // B is a straight
                    remaining.clear();
                    EstimatedSet b2 = new EstimatedSet();
                    b2.cloneWith(b);
                    for (Card card : setA) {
                        if (setB.contains(card) == false) {
                            remaining.add(card);
                        } else if (b2.remove2ndCard(card)) {
                            remaining.add(card);
                        }
                    }

                    if (remaining.size() < MauBinhConfig.NUMBER_CARD_BIG_SET) {
                        return false;
                    }

                    Collections.sort(remaining);
                    EstimatedSet a2 = AutoArrangementBotNew.getSpecialSet(remaining);
                    if (a2.compareWith(b2) > 0) {
                        lastSet.cloneWith(a2);
                        middleSet.cloneWith(b2);
                    } else {
                        lastSet.cloneWith(b2);
                        middleSet.cloneWith(a2);
                    }

                    return true;
                } else { // A is straight / flush-straight
                    if (b.isFlush() == false) {
                        // A & B are straight, so:
                        // if A & B has no common item, then return true above.
                        // else then A is sub of B, or B is a sub of A, return false.
                        return false;
                    }

                    remaining.clear();
                    EstimatedSet a2 = new EstimatedSet();
                    a2.cloneWith(a);
                    for (Card card : setB) {
                        if (setA.contains(card) == false) {
                            remaining.add(card);
                        } else if (a2.remove2ndCard(card)) {
                            remaining.add(card);
                        }
                    }

                    if (remaining.size() < MauBinhConfig.NUMBER_CARD_BIG_SET) {
                        return false;
                    }

                    Collections.sort(remaining);
                    EstimatedSet b2 = AutoArrangementBotNew.getSpecialSet(remaining);
                    if (a2.compareWith(b2) > 0) {
                        lastSet.cloneWith(a2);
                        middleSet.cloneWith(b2);
                    } else {
                        lastSet.cloneWith(b2);
                        middleSet.cloneWith(a2);
                    }

                    return true;
                }
            }
        }

    }

    private static double getScore(EstimatedSet lastSet, EstimatedSet middleSet, EstimatedSet firstSet, int tmpId) {
        double ret = 0;
        if (lastSet != null) {
            ret += lastSet.getScore(EstimatedSet.LAST_SET, tmpId);
        }

        if (middleSet != null) {
            ret += middleSet.getScore(EstimatedSet.MIDDLE_SET, tmpId);
        }

        if (firstSet != null) {
            ret += firstSet.getScore(EstimatedSet.FIRST_SET, tmpId);
        }

        // Sepcial cases
        if ((firstSet.isStraight() && middleSet.isStraight() && lastSet.isStraight()) ||
            (firstSet.isFlush() && middleSet.isFlush() && lastSet.isFlush())) {
            ret += EstimatedSet.getMaxScore(tmpId);
        }

        return ret;
    }

    /**
     * la trường hợp là sảnh và thung
     *
     * @param cards
     * @return
     */
    private static EstimatedSet getSpecialSet(List<Card> cards) {
        // Check cards.
        if (cards == null || cards.size() < MauBinhConfig.NUMBER_CARD_BIG_SET) {
            return null;
        }

        boolean isFlush = true;
        int type0 = cards.get(0).getCardType();
        for (int i = 1; i < cards.size(); i++) {
            // Check type of card.
            if (cards.get(i).getCardType() != type0) {
                isFlush = false;
                break;
            }
        }

        boolean isStraight = false;
        // Get all straight.
        List<EstimatedSet> ret = AutoArrangementBotNew.getAllStraight(cards);
        if (ret != null && ret.isEmpty() == false) {
            isStraight = true;
        }

        if (isFlush) {
            if (isStraight) {
                ret.get(0).setType(SetType.STRAIGHT_FLUSH);
                return ret.get(0);
            } else {
                Set temp = new HashSet<>();
                temp.addAll(cards);

                EstimatedSet retSet = new EstimatedSet(temp, SetType.FLUSH);
                for (int i = cards.size() - 1; i >= 0; i--) {
                    retSet.addCard(cards.get(i).getCardNumber());
                }

                return retSet;
            }
        } else {
            if (isStraight) {
                return ret.get(0);
            } else {
                return null;
            }
        }
    }

    private static List<EstimatedSet> getAllBigSet(List<Card> cards) {
        // Check cards.
        if (cards == null || cards.size() < MauBinhConfig.NUMBER_CARD_BIG_SET) {
            return null;
        }

        List<Card> heartList = new ArrayList<>();
        List<Card> diamondList = new ArrayList<>();
        List<Card> clubList = new ArrayList<>();
        List<Card> spadeList = new ArrayList<>();
        switch (cards.get(0).getCardType()) {
            case MauBinhCardSet.TYPE_HEART:
                heartList.add(cards.get(0));
                break;
            case MauBinhCardSet.TYPE_DIAMOND:
                diamondList.add(cards.get(0));
                break;
            case MauBinhCardSet.TYPE_CLUB:
                clubList.add(cards.get(0));
                break;
            case MauBinhCardSet.TYPE_SPADE:
                spadeList.add(cards.get(0));
                break;
            default:
                break;
        }

        List<Integer> singleIndex = new ArrayList<>();
        List<Integer> lastPairIndex = new ArrayList<>();
        List<Integer> lastThreeIndex = new ArrayList<>();
        List<Integer> lastFourIndex = new ArrayList<>();

        int sameCardNo = 0;
        for (int i = 1; i < cards.size(); i++) {
            // Check type of card.
            switch (cards.get(i).getCardType()) {
                case MauBinhCardSet.TYPE_HEART:
                    heartList.add(cards.get(i));
                    break;
                case MauBinhCardSet.TYPE_DIAMOND:
                    diamondList.add(cards.get(i));
                    break;
                case MauBinhCardSet.TYPE_CLUB:
                    clubList.add(cards.get(i));
                    break;
                case MauBinhCardSet.TYPE_SPADE:
                    spadeList.add(cards.get(i));
                    break;
                default:
                    break;
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
                        break;
                    default:
                        break;
                }

                sameCardNo = 0;
            }
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

        List<EstimatedSet> ret = new ArrayList<>();
        List<Integer> setType = new ArrayList<>();

        // Get all straight flush.
        List<EstimatedSet> ret2 = AutoArrangementBotNew.getAllStraightFlush(heartList, diamondList, clubList, spadeList);
        if (ret2 != null && ret2.isEmpty() == false) {
            ret.addAll(ret2);
        }

        // Get all Four of a Kind.
        ret2 = AutoArrangementBotNew.getAll4OfKind(cards, lastFourIndex);
        if (ret2 != null && ret2.isEmpty() == false) {
            ret.addAll(ret2);
        }

        // Get all Full house.
        ret2 = AutoArrangementBotNew.getAllFullHouse(cards, lastThreeIndex, lastPairIndex);
        if (ret2 != null && ret2.isEmpty() == false) {
            ret.addAll(ret2);
        }

        // Get biggest flush.
        ret2 = AutoArrangementBotNew.getAllFlush(heartList, diamondList, clubList, spadeList);
        if (ret2 != null && ret2.isEmpty() == false) {
            ret.addAll(ret2);
        }

        // Get all straight.
        ret2 = AutoArrangementBotNew.getAllStraight(cards);
        if (ret2 != null && ret2.isEmpty() == false) {
            ret.addAll(ret2);
        }

        // Get all Three of a Kind.
        ret2 = AutoArrangementBotNew.getAll3OfKind(cards, lastThreeIndex);
        if (ret2 != null && ret2.isEmpty() == false) {
            ret.addAll(ret2);
        }

        // Get all 2 pairs.
        ret2 = AutoArrangementBotNew.getAll2Pairs(cards, lastPairIndex, lastThreeIndex);
        if (ret2 != null && ret2.isEmpty() == false) {
            ret.addAll(ret2);
        }

        // Get all 1 pair.
        ret2 = AutoArrangementBotNew.getAll1Pair(cards, lastPairIndex, lastThreeIndex);
        if (ret2 != null && ret2.isEmpty() == false) {
            ret.addAll(ret2);
        }

        return ret;
    }

    private static List<EstimatedSet> getAllStraightFlush(
            List<Card> heartList, List<Card> diamondList, List<Card> clubList, List<Card> spadeList) {

        List<EstimatedSet> ret = new ArrayList<>();
        List<EstimatedSet> ret2 = null;
        // Check heart cards.
        if (heartList != null && heartList.size() >= MauBinhConfig.NUMBER_CARD_BIG_SET) {
            ret2 = AutoArrangementBotNew.getAllStraight(heartList);
            if (ret2 != null && ret2.isEmpty() == false) {
                for (EstimatedSet set : ret2) {
                    set.setType(SetType.STRAIGHT_FLUSH);
                    ret.add(set);
                }
            }
        }

        // Check diamond cards.
        if (diamondList != null && diamondList.size() >= MauBinhConfig.NUMBER_CARD_BIG_SET) {
            ret2 = AutoArrangementBotNew.getAllStraight(diamondList);
            if (ret2 != null && ret2.isEmpty() == false) {
                for (EstimatedSet set : ret2) {
                    set.setType(SetType.STRAIGHT_FLUSH);
                    ret.add(set);
                }
            }
        }

        // Check club cards.
        if (clubList != null && clubList.size() >= MauBinhConfig.NUMBER_CARD_BIG_SET) {
            ret2 = AutoArrangementBotNew.getAllStraight(clubList);
            if (ret2 != null && ret2.isEmpty() == false) {
                for (EstimatedSet set : ret2) {
                    set.setType(SetType.STRAIGHT_FLUSH);
                    ret.add(set);
                }
            }
        }

        // Check spade cards.
        if (spadeList != null && spadeList.size() >= MauBinhConfig.NUMBER_CARD_BIG_SET) {
            ret2 = AutoArrangementBotNew.getAllStraight(spadeList);
            if (ret2 != null && ret2.isEmpty() == false) {
                for (EstimatedSet set : ret2) {
                    set.setType(SetType.STRAIGHT_FLUSH);
                    ret.add(set);
                }
            }
        }

        return ret;
    }

    private static List<List<Card>> getAllStraightPre(List<Card> cards) {
        // Check cards.
        if (cards == null || cards.isEmpty()
                || cards.size() < MauBinhConfig.NUMBER_CARD_BIG_SET) {
            return null;
        }

        int count = 0;
        boolean is2ndStraight = false;
        List<Card> ret = new ArrayList<>();
        // Check if the last card is Ace then return.
        // Check if the first card is 2 then return.
        if (MauBinhCardSet.isAce(cards.get(cards.size() - 1))
                && MauBinhCardSet.is2(cards.get(0))) {
            // Add the Ace and 2.
            count++;
            for (int i = cards.size() - 1; i >= 0; i--) {
                if (MauBinhCardSet.isAce(cards.get(i))) {
                    ret.add(cards.get(i));
                } else {
                    break;
                }
            }

            is2ndStraight = true;
        }

        count++;
        ret.add(cards.get(0));

        List<List<Card>> ret2 = new ArrayList<>();
        int tmp;
        for (int i = 1; i < cards.size(); i++) {
            int temp = cards.get(i).getCardNumber() - cards.get(i - 1).getCardNumber();
            switch (temp) {
                case 0: // Discard the duplicated cards.
                    ret.add(cards.get(i));
                    break;
                case 1: // Add the next card.
                    ret.add(cards.get(i));
                    count++;
                    break;
                default: // NOT straight.
                    if (count >= MauBinhConfig.NUMBER_CARD_BIG_SET) {
                        if (is2ndStraight) {
                            is2ndStraight = false;
                            Collections.sort(ret);
                        }

                        ret2.add(ret);
                    }

                    ret = new ArrayList<>();
                    ret.add(cards.get(i));
                    count = 1;
                    break;
            }
        }

        if (count >= MauBinhConfig.NUMBER_CARD_BIG_SET) {
            if (is2ndStraight) {
                Collections.sort(ret);
            }

            ret2.add(ret);
        }

        return ret2.isEmpty() ? null : ret2;
    }

    private static List<EstimatedSet> getAllStraight(List<Card> cards) {
        // Check cards.
        if (cards == null || cards.isEmpty()
                || cards.size() < MauBinhConfig.NUMBER_CARD_BIG_SET) {
            return null;
        }

        List<List<Card>> tmpRet = getAllStraightPre(cards);
        if (tmpRet == null || tmpRet.isEmpty()) {
            return null;
        }

        List<EstimatedSet> ret = new ArrayList<>();
        List<Card> tmpList;
        EstimatedSet retSet;
        int count;
        int tmp;
        for (int i = 0; i < tmpRet.size(); i++) {
            tmpList = tmpRet.get(i);
            count = tmpList.get(tmpList.size() - 1).getCardNumber() - tmpList.get(0).getCardNumber() + 1;
            // it's 2nd straight.
            if (count >= 13) {
                int jNoAce = 0;
                for (int j = tmpList.size() - 1; j >= (MauBinhConfig.NUMBER_CARD_BIG_SET - 2); j--) {
                    if (MauBinhCardSet.isAce(tmpList.get(j)) == false) {
                        jNoAce = j;
                        break;
                    }
                }

                for (int j = jNoAce; j >= (MauBinhConfig.NUMBER_CARD_BIG_SET - 2); j--) {
                    count = tmpList.get(j).getCardNumber() - tmpList.get(0).getCardNumber() + 1;
                    if (count >= MauBinhConfig.NUMBER_CARD_BIG_SET - 1) {
                        retSet = new EstimatedSet();
                        retSet.setType(SetType.STRAIGHT);
                        // add Ace
                        for (int k = tmpList.size() - 1; k > jNoAce; k--) {
                            retSet.getSet().add(tmpList.get(k));
                        }

                        for (int k = j; k >= 0; k--) {
                            retSet.getSet().add(tmpList.get(k));
                        }

                        tmp = tmpList.get(tmpList.size() - 1).getCardNumber();
                        retSet.addCard(tmp);
                        tmp = tmpList.get(0).getCardNumber();
                        for (int k = 0; k < MauBinhConfig.NUMBER_CARD_BIG_SET - 1; k++) {
                            retSet.addCard(tmp + k);
                        }

                        ret.add(retSet);
                    }
                }
            } else { // normal straight
                for (int j = tmpList.size() - 1; j >= (MauBinhConfig.NUMBER_CARD_BIG_SET - 1); j--) {
                    count = tmpList.get(j).getCardNumber() - tmpList.get(0).getCardNumber() + 1;
                    if (count >= MauBinhConfig.NUMBER_CARD_BIG_SET) {
                        retSet = new EstimatedSet();
                        retSet.setType(SetType.STRAIGHT);
                        for (int k = j; k >= 0; k--) {
                            retSet.getSet().add(tmpList.get(k));
                        }

                        tmp = tmpList.get(j).getCardNumber();
                        for (int k = 0; k < MauBinhConfig.NUMBER_CARD_BIG_SET; k++) {
                            retSet.addCard(tmp - k);
                        }

                        ret.add(retSet);
                    }
                }
            }
        }

        return ret.isEmpty() ? null : ret;
    }

    private static List<EstimatedSet> getAll4OfKind(List<Card> cards, List<Integer> lastFourIndex) {
        // Check cards.
        if (cards == null || cards.isEmpty()
                || cards.size() < MauBinhConfig.NUMBER_CARD_BIG_SET
                || lastFourIndex == null || lastFourIndex.isEmpty()) {
            return null;
        }

        List<EstimatedSet> ret = new ArrayList<>();
        Set<Card> ret2;
        EstimatedSet retSet;
        for (int i = 0; i < lastFourIndex.size(); i++) {
            if (lastFourIndex.get(i) < 3 && lastFourIndex.get(i) >= cards.size()) {
                continue;
            }

            ret2 = new HashSet<>();
            ret2.add(cards.get(lastFourIndex.get(i) - 3));
            ret2.add(cards.get(lastFourIndex.get(i) - 2));
            ret2.add(cards.get(lastFourIndex.get(i) - 1));
            ret2.add(cards.get(lastFourIndex.get(i)));

            retSet = new EstimatedSet(ret2, SetType.FOUR_OF_KIND);
            retSet.addCard(cards.get(lastFourIndex.get(i)).getCardNumber());
            ret.add(retSet);
        }

        return ret.isEmpty() ? null : ret;
    }

    private static List<EstimatedSet> getAllFullHouse(List<Card> cards, List<Integer> lastThreeIndex, List<Integer> lastPairIndex) {
        // Check cards.
        if (cards == null || cards.isEmpty()
                || cards.size() < MauBinhConfig.NUMBER_CARD_BIG_SET
                || lastThreeIndex == null || lastThreeIndex.isEmpty()) {
            return null;
        }

        List<EstimatedSet> ret = new ArrayList<>();
        Set<Card> ret2;
        EstimatedSet retSet;
        for (int j = 0; j < lastThreeIndex.size(); j++) {
            int tempIndex = lastThreeIndex.get(j);
            // If has a pair.
            for (int i = 0; i < lastPairIndex.size(); i++) {
                ret2 = new HashSet<>();
                ret2.add(cards.get(lastPairIndex.get(i) - 1));
                ret2.add(cards.get(lastPairIndex.get(i)));
                // Get current three of kind.
                ret2.add(cards.get(tempIndex - 2));
                ret2.add(cards.get(tempIndex - 1));
                ret2.add(cards.get(tempIndex));

                retSet = new EstimatedSet(ret2, SetType.FULL_HOUSE);
                retSet.addCard(cards.get(tempIndex).getCardNumber());
                retSet.addCard(cards.get(lastPairIndex.get(i)).getCardNumber());
                ret.add(retSet);
            }

            // If there is NOT any pair, then check three of kind.
            for (int k = 0; k < lastThreeIndex.size(); k++) {
                if (k == j) {
                    continue;
                }

                int number = cards.get(lastThreeIndex.get(k)).getCardNumber();
                // 1st
                ret2 = new HashSet<>();
                ret2.add(cards.get(lastThreeIndex.get(k) - 1));
                ret2.add(cards.get(lastThreeIndex.get(k)));
                // Get the biggest.
                ret2.add(cards.get(tempIndex - 2));
                ret2.add(cards.get(tempIndex - 1));
                ret2.add(cards.get(tempIndex));

                retSet = new EstimatedSet(ret2, SetType.FULL_HOUSE);
                retSet.addCard(cards.get(tempIndex).getCardNumber());
                retSet.addCard(number);
                ret.add(retSet);

                // 2nd
                ret2 = new HashSet<>();
                ret2.add(cards.get(lastThreeIndex.get(k) - 1));
                ret2.add(cards.get(lastThreeIndex.get(k) - 2));
                // Get the biggest.
                ret2.add(cards.get(tempIndex - 2));
                ret2.add(cards.get(tempIndex - 1));
                ret2.add(cards.get(tempIndex));

                retSet = new EstimatedSet(ret2, SetType.FULL_HOUSE);
                retSet.addCard(cards.get(tempIndex).getCardNumber());
                retSet.addCard(number);
                ret.add(retSet);

                // 3rd
                ret2 = new HashSet<>();
                ret2.add(cards.get(lastThreeIndex.get(k) - 2));
                ret2.add(cards.get(lastThreeIndex.get(k)));
                // Get the biggest.
                ret2.add(cards.get(tempIndex - 2));
                ret2.add(cards.get(tempIndex - 1));
                ret2.add(cards.get(tempIndex));

                retSet = new EstimatedSet(ret2, SetType.FULL_HOUSE);
                retSet.addCard(cards.get(tempIndex).getCardNumber());
                retSet.addCard(number);
                ret.add(retSet);
            }
        }

        return ret.isEmpty() ? null : ret;
    }

    private static List<EstimatedSet> getAllFlush(List<Card> sameTypeList) {

        List<EstimatedSet> ret = new ArrayList<>();
        Set<Card> temp = null;
        EstimatedSet retSet;
        // Check heart cards.
        if (sameTypeList != null) {
            if (sameTypeList.size() >= MauBinhConfig.NUMBER_CARD_BIG_SET) {
                temp = new HashSet<>();
                temp.addAll(sameTypeList);

                retSet = new EstimatedSet(temp, SetType.FLUSH);
                for (int i = sameTypeList.size() - 1; i >= 0; i--) {
                    retSet.addCard(sameTypeList.get(i).getCardNumber());
                }

                ret.add(retSet);
            }

            if (sameTypeList.size() > MauBinhConfig.NUMBER_CARD_BIG_SET) {
                for (int j = sameTypeList.size() - 1; j >= 0; j--) {
                    temp = new HashSet<>();
                    temp.addAll(sameTypeList);

                    retSet = new EstimatedSet(temp, SetType.FLUSH);
                    for (int i = sameTypeList.size() - 1; i >= 0; i--) {
                        if (i == j) {
                            // remove the j-th card.
                            retSet.getSet().remove(sameTypeList.get(i));
                            continue;
                        }

                        retSet.addCard(sameTypeList.get(i).getCardNumber());
                    }

                    // Add all flush with length - 1.
                    ret.add(retSet);
                }
            }
        }

        return ret;
    }

    private static List<EstimatedSet> getAllFlush(
            List<Card> heartList, List<Card> diamondList, List<Card> clubList, List<Card> spadeList) {

        List<EstimatedSet> ret = new ArrayList<>();

        // Check heart cards.
        List<EstimatedSet> temp = getAllFlush(heartList);
        if (temp != null && temp.isEmpty() == false) {
            ret.addAll(temp);
        }

        // Check diamond cards.
        temp = getAllFlush(diamondList);
        if (temp != null && temp.isEmpty() == false) {
            ret.addAll(temp);
        }

        // Check club cards.
        temp = getAllFlush(clubList);
        if (temp != null && temp.isEmpty() == false) {
            ret.addAll(temp);
        }

        // Check spade cards.
        temp = getAllFlush(spadeList);
        if (temp != null && temp.isEmpty() == false) {
            ret.addAll(temp);
        }

        return ret;
    }

    private static List<EstimatedSet> getAll3OfKind(List<Card> cards, List<Integer> lastThreeIndex) {
        // Check cards.
        if (cards == null || cards.isEmpty()
                || lastThreeIndex == null || lastThreeIndex.isEmpty()) {
            return null;
        }

        List<EstimatedSet> ret = new ArrayList<>();
        Set<Card> ret2;
        EstimatedSet retSet;
        for (int i = 0; i < lastThreeIndex.size(); i++) {
            if (lastThreeIndex.get(i) < 2 && lastThreeIndex.get(i) >= cards.size()) {
                continue;
            }

            ret2 = new HashSet<>();
            ret2.add(cards.get(lastThreeIndex.get(i) - 2));
            ret2.add(cards.get(lastThreeIndex.get(i) - 1));
            ret2.add(cards.get(lastThreeIndex.get(i)));

            retSet = new EstimatedSet(ret2, SetType.THREE_OF_KIND);
            retSet.addCard(cards.get(lastThreeIndex.get(i)).getCardNumber());
            ret.add(retSet);
        }

        return ret.isEmpty() ? null : ret;
    }

    private static List<EstimatedSet> getAll2Pairs(List<Card> cards, List<Integer> lastPairIndex, List<Integer> lastThreeIndex) {
        // Check cards.
        if (cards == null || cards.isEmpty()
                || cards.size() < MauBinhConfig.NUMBER_CARD_BIG_SET) {
            return null;
        }

        List<EstimatedSet> ret = new ArrayList<>();
        Set<Card> ret2;
        EstimatedSet retSet;
        if (lastPairIndex != null && lastPairIndex.size() >= 2) {
            for (int i = 0; i < lastPairIndex.size(); i++) {
                for (int j = i + 1; j < lastPairIndex.size(); j++) {
                    ret2 = new HashSet<>();
                    ret2.add(cards.get(lastPairIndex.get(i) - 1));
                    ret2.add(cards.get(lastPairIndex.get(i)));
                    ret2.add(cards.get(lastPairIndex.get(j) - 1));
                    ret2.add(cards.get(lastPairIndex.get(j)));

                    retSet = new EstimatedSet(ret2, SetType.TWO_PAIR);
                    retSet.addCard(cards.get(lastPairIndex.get(j)).getCardNumber());
                    retSet.addCard(cards.get(lastPairIndex.get(i)).getCardNumber());
                    ret.add(retSet);
                }
            }
        }

        if (lastThreeIndex == null || lastThreeIndex.isEmpty()) {
            return ret.isEmpty() ? null : ret;
        }

        int maxTmp;
        int minTmp;
        for (int i = 0; i < lastPairIndex.size(); i++) {
            for (int j = 0; j < lastThreeIndex.size(); j++) {
                maxTmp = Math.max(cards.get(lastThreeIndex.get(j)).getCardNumber(), cards.get(lastPairIndex.get(i)).getCardNumber());
                minTmp = Math.min(cards.get(lastThreeIndex.get(j)).getCardNumber(), cards.get(lastPairIndex.get(i)).getCardNumber());
                // 1st
                ret2 = new HashSet<>();
                ret2.add(cards.get(lastPairIndex.get(i) - 1));
                ret2.add(cards.get(lastPairIndex.get(i)));
                ret2.add(cards.get(lastThreeIndex.get(j) - 1));
                ret2.add(cards.get(lastThreeIndex.get(j)));

                retSet = new EstimatedSet(ret2, SetType.TWO_PAIR);
                retSet.addCard(maxTmp);
                retSet.addCard(minTmp);
                ret.add(retSet);
                // 2nd
                ret2 = new HashSet<>();
                ret2.add(cards.get(lastPairIndex.get(i) - 1));
                ret2.add(cards.get(lastPairIndex.get(i)));
                ret2.add(cards.get(lastThreeIndex.get(j) - 2));
                ret2.add(cards.get(lastThreeIndex.get(j)));

                retSet = new EstimatedSet(ret2, SetType.TWO_PAIR);
                retSet.addCard(maxTmp);
                retSet.addCard(minTmp);
                ret.add(retSet);
                // 3rd
                ret2 = new HashSet<>();
                ret2.add(cards.get(lastPairIndex.get(i) - 1));
                ret2.add(cards.get(lastPairIndex.get(i)));
                ret2.add(cards.get(lastThreeIndex.get(j) - 2));
                ret2.add(cards.get(lastThreeIndex.get(j) - 1));

                retSet = new EstimatedSet(ret2, SetType.TWO_PAIR);
                retSet.addCard(maxTmp);
                retSet.addCard(minTmp);
                ret.add(retSet);
            }
        }

        for (int i = 0; i < lastThreeIndex.size(); i++) {
            for (int j = i + 1; j < lastThreeIndex.size(); j++) {
                for (int n = 0; n < 3; n++) {
                    for (int m = 0; m < 3; m++) {
                        ret2 = new HashSet<>();
                        ret2.add(cards.get(lastThreeIndex.get(i) - n));
                        ret2.add(cards.get(lastThreeIndex.get(i) - ((n + 1) % 3)));
                        ret2.add(cards.get(lastThreeIndex.get(j) - m));
                        ret2.add(cards.get(lastThreeIndex.get(j) - ((m + 1) % 3)));

                        retSet = new EstimatedSet(ret2, SetType.TWO_PAIR);
                        retSet.addCard(cards.get(lastThreeIndex.get(j)).getCardNumber());
                        retSet.addCard(cards.get(lastThreeIndex.get(i)).getCardNumber());
                        ret.add(retSet);
                    }
                }
            }
        }

        return ret.isEmpty() ? null : ret;
    }

    private static List<EstimatedSet> getAll1Pair(List<Card> cards, List<Integer> lastPairIndex, List<Integer> lastThreeIndex) {
        // Check cards.
        if (cards == null || cards.isEmpty()
                || cards.size() < MauBinhConfig.NUMBER_CARD_BIG_SET) {
            return null;
        }

        List<EstimatedSet> ret = new ArrayList<>();
        Set<Card> ret2;
        EstimatedSet retSet;
        if (lastPairIndex != null && lastPairIndex.isEmpty() == false) {
            for (int i = 0; i < lastPairIndex.size(); i++) {
                ret2 = new HashSet<>();
                ret2.add(cards.get(lastPairIndex.get(i) - 1));
                ret2.add(cards.get(lastPairIndex.get(i)));

                retSet = new EstimatedSet(ret2, SetType.ONE_PAIR);
                retSet.addCard(cards.get(lastPairIndex.get(i)).getCardNumber());
                ret.add(retSet);
            }
        }

        if (lastThreeIndex == null || lastThreeIndex.isEmpty()) {
            return ret.isEmpty() ? null : ret;
        }

        for (int i = 0; i < lastThreeIndex.size(); i++) {
            // 1st
            ret2 = new HashSet<>();
            ret2.add(cards.get(lastThreeIndex.get(i) - 1));
            ret2.add(cards.get(lastThreeIndex.get(i)));

            retSet = new EstimatedSet(ret2, SetType.ONE_PAIR);
            retSet.addCard(cards.get(lastThreeIndex.get(i)).getCardNumber());
            ret.add(retSet);
            // 2nd
            ret2 = new HashSet<>();
            ret2.add(cards.get(lastThreeIndex.get(i) - 2));
            ret2.add(cards.get(lastThreeIndex.get(i)));

            retSet = new EstimatedSet(ret2, SetType.ONE_PAIR);
            retSet.addCard(cards.get(lastThreeIndex.get(i)).getCardNumber());
            ret.add(retSet);
            // 3rd
            ret2 = new HashSet<>();
            ret2.add(cards.get(lastThreeIndex.get(i) - 2));
            ret2.add(cards.get(lastThreeIndex.get(i) - 1));

            retSet = new EstimatedSet(ret2, SetType.ONE_PAIR);
            retSet.addCard(cards.get(lastThreeIndex.get(i)).getCardNumber());
            ret.add(retSet);
        }

        return ret.isEmpty() ? null : ret;
    }

    private static EstimatedSet getSmallSet(List<Card> cards) {
        // Check cards.
        if (cards == null || cards.size() < MauBinhConfig.NUMBER_CARD_SMALL_SET) { // NUMBER_CARD_SMALL_SET = 3
            return null;
        }

        List<Integer> singleIndex = new ArrayList<>();
        List<Integer> lastPairIndex = new ArrayList<>();
        List<Integer> lastThreeIndex = new ArrayList<>();

        int sameCardNo = 0;
        for (int i = 1; i < cards.size(); i++) {
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
                    case 3: // Four of a Kind.
                        lastThreeIndex.add(i - 1);
                        break;
                    default:
                        break;
                }

                sameCardNo = 0;
            }
        }

        switch (sameCardNo) {
            case 0:
                singleIndex.add(cards.size() - 1);
                break;
            case 1: // a pair.
                lastPairIndex.add(cards.size() - 1);
                break;
            case 2: // Three of a kind.
            case 3: // Four of a Kind.
                lastThreeIndex.add(cards.size() - 1);
                break;
            default:
                break;
        }

        EstimatedSet retSet;
        List<Card> ret;
        // Get biggest Three of a Kind.
        if (lastThreeIndex.isEmpty() == false) {
            ret = new ArrayList<>();
            // Get the biggest.
            int tempIndex = lastThreeIndex.get(lastThreeIndex.size() - 1);
            ret.add(cards.get(tempIndex - 2));
            ret.add(cards.get(tempIndex - 1));
            ret.add(cards.get(tempIndex));

            retSet = new EstimatedSet(ret, SetType.THREE_OF_KIND);
            retSet.addCard(cards.get(tempIndex).getCardNumber());
            return retSet;
        }

        // Get biggest pair.
        if (lastPairIndex.isEmpty() == false && singleIndex.size() >= 1) {
            ret = new ArrayList<>();
            ret.add(cards.get(singleIndex.get(singleIndex.size() - 1)));
            ret.add(cards.get(lastPairIndex.get(lastPairIndex.size() - 1) - 1));
            ret.add(cards.get(lastPairIndex.get(lastPairIndex.size() - 1)));

            retSet = new EstimatedSet(ret, SetType.ONE_PAIR);
            retSet.addCard(cards.get(lastPairIndex.get(lastPairIndex.size() - 1)).getCardNumber());
            retSet.addCard(cards.get(singleIndex.get(singleIndex.size() - 1)).getCardNumber());
            return retSet;
        }

        // Get biggest cards.
        ret = new ArrayList<>();
        ret.add(cards.get(cards.size() - 3));
        ret.add(cards.get(cards.size() - 2));
        ret.add(cards.get(cards.size() - 1));

        retSet = new EstimatedSet(ret, SetType.HIGH_CARD);
        retSet.addCard(cards.get(cards.size() - 1).getCardNumber());
        retSet.addCard(cards.get(cards.size() - 2).getCardNumber());
        retSet.addCard(cards.get(cards.size() - 3).getCardNumber());
        return retSet;
    }
    
    private static int getRandomRateId() {
        int rateId = (int) (Math.random() * 100);
        rateId %= 4;
        return rateId;
    }
    
    private static List<Card> convertCards(byte[] cards) {
        if (cards == null) {
            return null;
        }
        List<Card> listCards = new ArrayList<>();
        for (byte card : cards) {
            listCards.add(new Card(card));
        }
        return listCards;
    }
}
