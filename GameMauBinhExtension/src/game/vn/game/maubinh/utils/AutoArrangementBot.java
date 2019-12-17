/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package game.vn.game.maubinh.utils;

import game.vn.common.card.object.Card;
import game.vn.game.maubinh.MauBinhConfig;
import game.vn.game.maubinh.object.MauBinhCardSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Xử lý auto binh cho bot
 * @author binhnt
 */
public class AutoArrangementBot {
    
    /**
     * Sort a card list.
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
        for (int i = 0; i < sortedList.size() ; i++) {
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
     * @param cards card list.
     * @return an arrange card list.
     */
    public static List<Card> getSolution(List<Card> cards) {
        // Check cards.
        if (cards == null || cards.size() != MauBinhConfig.DEFAULT_NUMBER_CARD) {
            return null;
        }
        
        // Get optimal 3rd set.
        List<Card> lastSet = AutoArrangementBot.getOptimalBigSet(cards);
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
        List<Card> middleSet = AutoArrangementBot.getOptimalBigSet(remaining);
        List<Card> remaining2 = new ArrayList<>();
        for (int i = 0; i < remaining.size(); i++) {
            if (middleSet.contains(remaining.get(i))) {
                continue;
            }
            
            remaining2.add(remaining.get(i));
        }
        
        // Then get optimal 1st set.
        List<Card> firstSet = AutoArrangementBot.getOptimalSmallSet(remaining2);

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
        List<Card> ret = AutoArrangementBot.getOptimalStraightFlush(
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
        ret = AutoArrangementBot.getOptimalFlush(heartList, diamondList, clubList, spadeList);
        if (ret != null) {
            return ret;
        }
        
        // Get biggest straight.
        ret = AutoArrangementBot.getOptimalStraight(cards);
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
        if (cards == null || cards.isEmpty() ||
                cards.size() < MauBinhConfig.NUMBER_CARD_BIG_SET) {
            return null;
        }
        
        List<Card> ret = getOptimalNormalStraight(cards);
        // Check the 1st straight: 10JQKA.
        if (ret != null && ret.isEmpty() == false &&
                MauBinhCardSet.isAce(ret.get(ret.size() - 1))) {
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
        if (cards == null || cards.isEmpty() ||
                cards.size() < MauBinhConfig.NUMBER_CARD_BIG_SET) {
            return null;
        }
        
        List<Card> ret = new ArrayList<>();
        // Check if the last card is NOT Ace then return.
        // Check if the first card is NOT 2 then return.
        if (MauBinhCardSet.isAce(cards.get(cards.size() - 1)) == false ||
                MauBinhCardSet.is2(cards.get(0)) == false) {
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
        if (cards == null || cards.isEmpty() ||
                cards.size() < MauBinhConfig.NUMBER_CARD_BIG_SET) {
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
                ret = AutoArrangementBot.getOptimalStraight(heartList);
            } else {
                temp = AutoArrangementBot.getOptimalStraight(heartList);
                if (temp != null && temp.get(temp.size() - 1).getCardNumber() >
                        ret.get(ret.size() - 1).getCardNumber()) {
                    ret = temp;
                }
            }
        }
        
        // Check diamond cards.
        if (diamondList != null && diamondList.size() >= MauBinhConfig.NUMBER_CARD_BIG_SET) {
            if (ret == null) {
                ret = AutoArrangementBot.getOptimalStraight(diamondList);
            } else {
                temp = AutoArrangementBot.getOptimalStraight(diamondList);
                if (temp != null && temp.get(temp.size() - 1).getCardNumber() >
                        ret.get(ret.size() - 1).getCardNumber()) {
                    ret = temp;
                }
            }
        }

        // Check club cards.
        if (clubList != null && clubList.size() >= MauBinhConfig.NUMBER_CARD_BIG_SET) {
            if (ret == null) {
                ret = AutoArrangementBot.getOptimalStraight(clubList);
            } else {
                temp = AutoArrangementBot.getOptimalStraight(clubList);
                if (temp != null && temp.get(temp.size() - 1).getCardNumber() >
                        ret.get(ret.size() - 1).getCardNumber()) {
                    ret = temp;
                }
            }
        }

        // Check spade cards.
        if (spadeList != null && spadeList.size() >= MauBinhConfig.NUMBER_CARD_BIG_SET) {
            if (ret == null) {
                ret = AutoArrangementBot.getOptimalStraight(spadeList);
            } else {
                temp = AutoArrangementBot.getOptimalStraight(spadeList);
                if (temp != null && temp.get(temp.size() - 1).getCardNumber() >
                        ret.get(ret.size() - 1).getCardNumber()) {
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
}
