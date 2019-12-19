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
public class TldlBot {

    // One of 2 main functions: DONE
    public static byte[] getCards(byte[] currentCards, byte[] botCards,
            byte[] op1Cards, byte[] op2Cards, byte[] op3Cards,
            boolean op1inRound, boolean op2inRound, boolean op3inRound, boolean passAll2, boolean isInRound) {
        List<Card> cards = getCards(convertCards(currentCards), convertCards(botCards),
                convertCards(op1Cards), convertCards(op2Cards), convertCards(op3Cards),
                op1inRound, op2inRound, op3inRound, passAll2, isInRound);
        if (cards != null) {
            byte[] ids = new byte[cards.size()];
            for (int i = 0; i < ids.length; i++) {
                ids[i] = cards.get(i).getId();
            }
            return ids;
        }
        return null;
    }

    public static List<Card> getCards(List<Card> currentCards, List<Card> botCards,
            List<Card> op1Cards, List<Card> op2Cards, List<Card> op3Cards,
            boolean op1inRound, boolean op2inRound, boolean op3inRound, boolean passAll2, boolean isInRound) {
        int count = 0;
        int minSize = 13;
        if (op1Cards != null && op1Cards.isEmpty() == false) {
            Collections.sort(op1Cards);
            count++;
            if (op1Cards.size() < minSize) {
                minSize = op1Cards.size();
            }
        }
        TLArrangement op1Arrangement = new TLArrangement(op1Cards, passAll2, isInRound);
        if (op2Cards != null && op2Cards.isEmpty() == false) {
            Collections.sort(op2Cards);
            count++;
            if (op2Cards.size() < minSize) {
                minSize = op2Cards.size();
            }
        }
        TLArrangement op2Arrangement = new TLArrangement(op2Cards, passAll2, isInRound);
        if (op3Cards != null && op3Cards.isEmpty() == false) {
            Collections.sort(op3Cards);
            count++;
            if (op3Cards.size() < minSize) {
                minSize = op3Cards.size();
            }
        }
        TLArrangement op3Arrangement = new TLArrangement(op3Cards, passAll2, isInRound);
        boolean isNo2 = (op1Arrangement.has2() || op2Arrangement.has2() || op3Arrangement.has2()) == false; // to do: check thêm hàng nhỏ
        // cheat for keep bom of bot.
        if (minSize > 7){
            isNo2 = false;
        }
        // cheat for throwing bom of bot.
        if (count == 1 && (op1Arrangement.hasOnly2() || op2Arrangement.hasOnly2() || op3Arrangement.hasOnly2())) {
            isNo2 = true;
        }

        Collections.sort(botCards);
        TLArrangement botArrangement = new TLArrangement(botCards, isNo2, isInRound);
        // Bot starts a new round.
        if (currentCards == null) {
            return TldlBot.getCardsForNewRound(botArrangement, op1Arrangement, op2Arrangement, op3Arrangement);
        } else { // Continue current round.
            Collections.sort(currentCards);
            int setType = TldlBot.getSetType(currentCards);
            return TldlBot.getCardsForCurrentRound(setType, currentCards.size(), currentCards.get(currentCards.size() - 1).getId(),
                    botArrangement, op1Arrangement, op2Arrangement, op3Arrangement, isInRound);
        }
    }

    // DONE
    private static List<Card> getCardsForNewRound(TLArrangement botArrangement,
            TLArrangement op1Arrangement, TLArrangement op2Arrangement, TLArrangement op3Arrangement) {
        TLTurnScore score = new TLTurnScore();
        int maxScore = Integer.MIN_VALUE;
        int tmpScore;
        List<Card> ret = null;
        List<Card> tmp;
        // Get option and score for Three Of Kinds
        tmp = TldlBot.getThreeOfKind(-1, botArrangement, op1Arrangement, op2Arrangement, op3Arrangement, score, false);
        tmpScore = score.getScore();
        if (tmp != null && tmp.isEmpty() == false && maxScore < tmpScore) {
            ret = tmp;
            maxScore = tmpScore;
//            System.out.println("getCardsForNewRound - three: " + maxScore);
        }

        // Get option and score for all Straights
        int maxStraightLength = botArrangement.getMaxStraightLength();
        for (int i = 3; i <= maxStraightLength; i++) {
            score.clear();
            tmp = TldlBot.getStraight(-1, i, botArrangement, op1Arrangement, op2Arrangement, op3Arrangement, score, false);
            tmpScore = score.getScore();
            if (tmp != null && tmp.isEmpty() == false && maxScore < tmpScore) {
                ret = tmp;
                maxScore = tmpScore;
//                System.out.println("getCardsForNewRound - straight: " + maxScore);
            }
        }

        // Get option and score for Pair
        score.clear();
        tmp = TldlBot.getPair(-1, botArrangement, op1Arrangement, op2Arrangement, op3Arrangement, score, false);
        tmpScore = score.getScore();
        if (tmp != null && tmp.isEmpty() == false && maxScore < tmpScore) {
            ret = tmp;
            maxScore = tmpScore;
//            System.out.println("getCardsForNewRound - pair: " + maxScore);
        }

        // Get option and score for HighCard
        score.clear();
        tmp = TldlBot.getHighCard(-1, botArrangement, op1Arrangement, op2Arrangement, op3Arrangement, score, false);
        tmpScore = score.getScore();
        if (tmp != null && tmp.isEmpty() == false && maxScore < tmpScore) {
            ret = tmp;
            maxScore = tmpScore;
//            System.out.println("getCardsForNewRound - highcard: " + maxScore);
        }

        // Get option and score for 4 đôi thông
        score.clear();
        tmp = TldlBot.get4PairStraight(-1, botArrangement, op1Arrangement, op2Arrangement, op3Arrangement, score, false);
//        tmpScore = score.getScore();
//        if (tmp != null && tmp.isEmpty() == false && maxScore < tmpScore) {
//            ret = tmp;
//            maxScore = tmpScore;
//        }
        if (tmp != null && (score.willBeWin() || botArrangement.getWaitFor2() == false)) {
            return tmp;
        }

        // Get option and score for tứ quý
        score.clear();
        tmp = TldlBot.getFourOfKind(-1, botArrangement, op1Arrangement, op2Arrangement, op3Arrangement, score, false);
//        tmpScore = score.getScore();
//        if (tmp != null && tmp.isEmpty() == false && maxScore < tmpScore) {
//            ret = tmp;
//            maxScore = tmpScore;
//        }
//        if (tmp != null && (score.willBeWin() || botArrangement.getWaitFor2() == false)) {
        if (tmp != null && score.willBeWin()) {
            return tmp;
        }

        // Get option and score for 3 đôi thông
        score.clear();
        tmp = TldlBot.get3PairStraight(-1, botArrangement, op1Arrangement, op2Arrangement, op3Arrangement, score, false);
//        tmpScore = score.getScore();
//        if (tmp != null && tmp.isEmpty() == false && maxScore < tmpScore) {
//            ret = tmp;
//            maxScore = tmpScore;
//        }
        if (tmp != null && (score.willBeWin() || botArrangement.getWaitFor2() == false)) {
            return tmp;
        }

        if (ret == null) { // bug
            return botArrangement.getRandom();
        }

        return ret;
    }

    // DONE
    public static List<Card> getCardsForCurrentRound(int type, int cardNum, int maxCardId, TLArrangement botArrangement,
            TLArrangement op1Arrangement, TLArrangement op2Arrangement, TLArrangement op3Arrangement, boolean isInRound) {
        // nhảy vòng.
        if (!isInRound) {
            if (BasicArrangement.is2(maxCardId) || type == BasicArrangement.SETTYPE_3PAIR || type == BasicArrangement.SETTYPE_FOUR) {
                // Return 4 đôi thông bất kỳ.
                return TldlBot.get4PairStraight(0, botArrangement, op1Arrangement, op2Arrangement, op3Arrangement, null, false);
            } else if (type == BasicArrangement.SETTYPE_4PAIR) {
                // Return 4 đôi thông lớn hơn.
                return TldlBot.get4PairStraight(maxCardId, botArrangement, op1Arrangement, op2Arrangement, op3Arrangement, null, false);
            }

            return null;
        } else {
            switch (type) {
                case BasicArrangement.SETTYPE_HIGHCARD:
                    return TldlBot.getHighCard(maxCardId, botArrangement, op1Arrangement, op2Arrangement, op3Arrangement, null, false);
                case BasicArrangement.SETTYPE_PAIR:
                    return TldlBot.getPair(maxCardId, botArrangement, op1Arrangement, op2Arrangement, op3Arrangement, null, false);
                case BasicArrangement.SETTYPE_THREE:
                    return TldlBot.getThreeOfKind(maxCardId, botArrangement, op1Arrangement, op2Arrangement, op3Arrangement, null, false);
                case BasicArrangement.SETTYPE_STRAIGHT:
                    return TldlBot.getStraight(maxCardId, cardNum, botArrangement, op1Arrangement, op2Arrangement, op3Arrangement, null, false);
                case BasicArrangement.SETTYPE_3PAIR:
                    return TldlBot.get3PairStraight(maxCardId, botArrangement, op1Arrangement, op2Arrangement, op3Arrangement, null, false);
                case BasicArrangement.SETTYPE_FOUR:
                    return TldlBot.getFourOfKind(maxCardId, botArrangement, op1Arrangement, op2Arrangement, op3Arrangement, null, false);
                case BasicArrangement.SETTYPE_4PAIR:
                    return TldlBot.get4PairStraight(maxCardId, botArrangement, op1Arrangement, op2Arrangement, op3Arrangement, null, false);
                default:
                    return null;
            }
        }
    }

    // DONE + to do: chặt heo
    public static List<Card> getHighCard(int maxCardId, TLArrangement botArrangement,
            TLArrangement op1Arrangement, TLArrangement op2Arrangement, TLArrangement op3Arrangement,
            TLTurnScore score, boolean isCheck) {
        if (botArrangement == null) {
            System.out.println("getHighCard01");
            return null;
        }

        int minCardNumber = TldlBot.getMinCardNumber(op1Arrangement, op2Arrangement, op3Arrangement);
        Card card = botArrangement.getOptimalSingle(maxCardId, maxCardId < 0);

        Card botMaxSingle = botArrangement.getBiggestSingleCard(maxCardId);
        if (botMaxSingle == null) { // Cannot catch.
            if (BasicArrangement.is2(maxCardId)) {
                System.out.println("getHighCard02");
                return TldlBot.get3PairStraight(0, botArrangement, op1Arrangement, op2Arrangement, op3Arrangement, score, isCheck);
            }

            System.out.println("getHighCard03");
            return null;
        }

        if (BasicArrangement.is2(maxCardId)) {
            System.out.println("getHighCard02b");
            List<Card> tmp = TldlBot.get3PairStraight(0, botArrangement, op1Arrangement, op2Arrangement, op3Arrangement, score, isCheck);
            if (tmp != null && tmp.isEmpty() == false) {
                return tmp;
            }
        }

        int maxId = 0;
        int minHighCardNumber = 13;
        List<Card> ret = null;
        boolean canCatch = true;
        int op1MaxSingleId = op1Arrangement == null ? - 1 : op1Arrangement.getBiggestSingleId(maxCardId);
        int op2MaxSingleId = op2Arrangement == null ? - 1 : op2Arrangement.getBiggestSingleId(maxCardId);
        int op3MaxSingleId = op3Arrangement == null ? - 1 : op3Arrangement.getBiggestSingleId(maxCardId);
        int oppMaxSingleId = TldlBot.max(op1MaxSingleId, op2MaxSingleId, op3MaxSingleId);
        if (TldlBot.compareTLID(botMaxSingle.getId(), op1MaxSingleId) < 0) {
            if (op1Arrangement.size() == 1) { // chạy heo + point
                ret = new ArrayList<>();
                ret.add(botMaxSingle);
                System.out.println("getHighCard04");
                return TldlBot.processScore(ret, score, false, false, botArrangement);
            }

            if (op1MaxSingleId == oppMaxSingleId && op1Arrangement.canBeWinAfter(op1Arrangement.getSingle(op1MaxSingleId))) {
                ret = new ArrayList<>(); // chạy
                ret.add(botMaxSingle);
                System.out.println("getHighCard05");
                return TldlBot.processScore(ret, score, false, false, botArrangement);
            }

            canCatch = false;
            maxId = op1MaxSingleId;
            minHighCardNumber = op1Arrangement.getHighCardNumber();
        }

        if (TldlBot.compareTLID(botMaxSingle.getId(), op2MaxSingleId) < 0) {
            if (op2MaxSingleId == oppMaxSingleId && op2Arrangement.canBeWinAfter(op2Arrangement.getSingle(op2MaxSingleId))) {
                ret = new ArrayList<>(); // chạy
                ret.add(botMaxSingle);
                System.out.println("getHighCard06");
                return TldlBot.processScore(ret, score, false, false, botArrangement);
            }

            if (op2Arrangement.size() == 1) { // chạy heo + point
                ret = new ArrayList<>(); // hơi nhát, dễ bị nát bài
                ret.add(botMaxSingle);
                System.out.println("getHighCard07");
                return TldlBot.processScore(ret, score, false, false, botArrangement);
            }

            canCatch = false;
            if (maxId < op2MaxSingleId) {
                maxId = op2MaxSingleId;
            }

            if (minHighCardNumber > op2Arrangement.getHighCardNumber()) {
                minHighCardNumber = op2Arrangement.getHighCardNumber();
            }
        }

        if (TldlBot.compareTLID(botMaxSingle.getId(), op3MaxSingleId) < 0) {
            if (op3MaxSingleId == oppMaxSingleId && op3Arrangement.canBeWinAfter(op3Arrangement.getSingle(op3MaxSingleId))) {
                ret = new ArrayList<>(); // chạy
                ret.add(botMaxSingle);
                System.out.println("getHighCard08");
                return TldlBot.processScore(ret, score, false, false, botArrangement);
            }

            if (op3Arrangement.size() == 1) { // chạy heo + point
                ret = new ArrayList<>(); // hơi nhát, dễ bị nát bài
                ret.add(botMaxSingle);
                System.out.println("getHighCard09");
                return TldlBot.processScore(ret, score, false, false, botArrangement);
            }

            canCatch = false;
            if (maxId < op3MaxSingleId) {
                maxId = op3MaxSingleId;
            }

            if (minHighCardNumber > op3Arrangement.getHighCardNumber()) {
                minHighCardNumber = op3Arrangement.getHighCardNumber();
            }
        }

        boolean canBeLose = false;
        if (op1Arrangement.size() == 1) {
            canBeLose = true;
        } else if (op1Arrangement.size() == 0 && op2Arrangement.size() == 1) {
            canBeLose = true;
        } else if (op1Arrangement.size() == 0 && op2Arrangement.size() == 0 && op3Arrangement.size() == 1) {
            canBeLose = true;
        }

        if (canBeLose) {
            ret = new ArrayList<>(); // có thể tối ưu hơn. next update
            ret.add(botMaxSingle);
            System.out.println("getHighCard10");
            return TldlBot.processScore(ret, score, true, canCatch, botArrangement);
        }

        if (canCatch) {
            if (maxCardId >= 0) {
                ret = new ArrayList<>(); // có thể tối ưu hơn. next update
                if (card == null && BasicArrangement.is2(botMaxSingle)) {
                    ret.add(botArrangement.getOptimal2(maxCardId));
                } else {
                    ret.add(card == null ? botMaxSingle : card);
                }

                System.out.println("getHighCard11");
                return TldlBot.processScore(ret, score, card == null, true, botArrangement);
            } else if (botArrangement.canBeWinAfter(botMaxSingle) || minCardNumber == 1) {
                ret = new ArrayList<>(); // có thể tối ưu hơn. next update
                if (card != null && card.getId() != botMaxSingle.getId() && minCardNumber > 1) {
                    ret.add(card);
                } else {
                    ret.add(botMaxSingle);
                }

                System.out.println("getHighCard12");
                return TldlBot.processScore(ret, score, true, true, botArrangement);
            }
        }

        if (card != null && (botArrangement.isRac(card.getId(), BasicArrangement.SETTYPE_HIGHCARD) || botArrangement.hasBomb())) {
            ret = new ArrayList<>();
            ret.add(card);
            System.out.println("getHighCard13");
            return TldlBot.processScore(ret, score, false, canCatch, botArrangement);
        }

        if (maxCardId < 0) {
            return null;
        }
        
        // chặt heo.
        if (BasicArrangement.is2(maxCardId)) {
            ret = TldlBot.get3PairStraight(0, botArrangement, op1Arrangement, op2Arrangement, op3Arrangement, score, isCheck);
            if (ret != null) {
                System.out.println("getHighCard14");
                return ret;
            }
        }

        ret = new ArrayList<>();
        if (BasicArrangement.is2(botMaxSingle)) {
            ret.add(botArrangement.getOptimal2(maxCardId));
        } else {
            ret.add(botMaxSingle);
        }
        // choi 3-4 nguoi thi bot hơi lỗ
        if (isCheck == false) {
            if (op3Arrangement.size() > 0) {
                if (TldlBot.canBeWinNewRound(op3Arrangement, botArrangement, op1Arrangement, op2Arrangement)) {
                    System.out.println("getHighCard15");
                    return TldlBot.processScore(ret, score, false, false, botArrangement);
                }
            } else if (op2Arrangement.size() > 0) {
                if (TldlBot.canBeWinNewRound(op2Arrangement, op3Arrangement, botArrangement, op1Arrangement)) {
                    System.out.println("getHighCard16");
                    return TldlBot.processScore(ret, score, false, false, botArrangement);
                }
            } else if (op1Arrangement.size() > 0) {
                if (TldlBot.canBeWinNewRound(op1Arrangement, op2Arrangement, op3Arrangement, botArrangement)) {
                    System.out.println("getHighCard17");
                    return TldlBot.processScore(ret, score, false, false, botArrangement);
                }
            }
        }

        // Biết vẫn bắt, có xác xuất đối thủ bỏ.
        if (canCatch == false && minHighCardNumber >= botArrangement.getHighCardNumber() && (maxId / 4) > botMaxSingle.getCardNumber()) {
            System.out.println("getHighCard18");
            return TldlBot.processScore(ret, score, false, false, botArrangement);
        }

        if (Utils.nextInt(100) <= 46 || botArrangement.hasBomb()) {// && maxCardId >= 0) {// 36%
            System.out.println("getHighCard18b");
            return TldlBot.processScore(ret, score, false, false, botArrangement);
        }

        System.out.println("getHighCard19");
        return null;
    }

    // DONE
    public static List<Card> getPair(int maxCardId, TLArrangement botArrangement,
            TLArrangement op1Arrangement, TLArrangement op2Arrangement, TLArrangement op3Arrangement,
            TLTurnScore score, boolean isCheck) {
        if (botArrangement == null) {
            System.out.println("getPair01");
            return null;
        }

        int minCardNumber = TldlBot.getMinCardNumber(op1Arrangement, op2Arrangement, op3Arrangement);
        int optimalPairId = botArrangement.getOptimalPairId(maxCardId);

        int botMaxPairId = botArrangement.getBiggestPairId(maxCardId);
        if (botMaxPairId < 0) { // Cannot catch.
            if (BasicArrangement.is2(maxCardId)) {
                System.out.println("getPair02");
                return TldlBot.getFourOfKind(0, botArrangement, op1Arrangement, op2Arrangement, op3Arrangement, score, isCheck);
            }

            System.out.println("getPair03");
            return null;
        }

        if (BasicArrangement.is2(maxCardId)) {
            System.out.println("getPair02b");
            List<Card> tmp = TldlBot.getFourOfKind(0, botArrangement, op1Arrangement, op2Arrangement, op3Arrangement, score, isCheck);
            if (tmp != null && tmp.isEmpty() == false) {
                return tmp;
            }
        }

        int maxId = 0;
        int minHighCardNumber = 13;
        List<Card> ret = null;
        boolean canCatch = true;
        int op1MaxPairId = op1Arrangement == null ? - 1 : op1Arrangement.getBiggestPairId(maxCardId);
        int op2MaxPairId = op2Arrangement == null ? - 1 : op2Arrangement.getBiggestPairId(maxCardId);
        int op3MaxPairId = op3Arrangement == null ? - 1 : op3Arrangement.getBiggestPairId(maxCardId);
        int oppMaxPairId = TldlBot.max(op1MaxPairId, op2MaxPairId, op3MaxPairId);
        if (TldlBot.compareTLID(botMaxPairId, op1MaxPairId) < 0) {
            if (op1Arrangement.size() == 2) { // chạy heo + point
                ret = botArrangement.getPair(botMaxPairId);
                System.out.println("getPair04");
                return TldlBot.processScore(ret, score, false, false, botArrangement);
            }

            if (op1MaxPairId == oppMaxPairId && op1Arrangement.canBeWinAfter(op1Arrangement.getPair(op1MaxPairId))) {
                ret = botArrangement.getPair(botMaxPairId); // chạy
                System.out.println("getPair05");
                return TldlBot.processScore(ret, score, false, false, botArrangement);
            }

            canCatch = false;
            maxId = op1MaxPairId;
            minHighCardNumber = op1Arrangement.getHighCardNumber();
        }

        if (TldlBot.compareTLID(botMaxPairId, op2MaxPairId) < 0) {
            if (op2MaxPairId == oppMaxPairId && op2Arrangement.canBeWinAfter(op2Arrangement.getPair(op2MaxPairId))) {
                ret = botArrangement.getPair(botMaxPairId); // chạy
                System.out.println("getPair06");
                return TldlBot.processScore(ret, score, false, false, botArrangement);
            }

            if (op2Arrangement.size() == 2) { // chạy heo + point
                ret = botArrangement.getPair(botMaxPairId); // hơi nhát, dễ bị nát bài
                System.out.println("getPair07");
                return TldlBot.processScore(ret, score, false, false, botArrangement);
            }

            canCatch = false;
            if (maxId < op2MaxPairId) {
                maxId = op2MaxPairId;
            }

            if (minHighCardNumber > op2Arrangement.getHighCardNumber()) {
                minHighCardNumber = op2Arrangement.getHighCardNumber();
            }
        }

        if (TldlBot.compareTLID(botMaxPairId, op3MaxPairId) < 0) {
            if (op3MaxPairId == oppMaxPairId && op3Arrangement.canBeWinAfter(op3Arrangement.getPair(op3MaxPairId))) {
                ret = botArrangement.getPair(botMaxPairId); // chạy
                System.out.println("getPair08");
                return TldlBot.processScore(ret, score, false, false, botArrangement);
            }

            if (op3Arrangement.size() == 2) { // chạy heo + point
                ret = botArrangement.getPair(botMaxPairId); // hơi nhát, dễ bị nát bài
                System.out.println("getPair09");
                return TldlBot.processScore(ret, score, false, false, botArrangement);
            }

            canCatch = false;
            if (maxId < op3MaxPairId) {
                maxId = op3MaxPairId;
            }

            if (minHighCardNumber > op3Arrangement.getHighCardNumber()) {
                minHighCardNumber = op3Arrangement.getHighCardNumber();
            }
        }

        boolean canBeLose = false;
        if (op1Arrangement.size() == 2) {
            canBeLose = (op1MaxPairId >= 0);
        } else if (op1Arrangement.size() == 0 && op2Arrangement.size() == 2) {
            canBeLose = (op2MaxPairId >= 0);
        } else if (op1Arrangement.size() == 0 && op2Arrangement.size() == 0 && op3Arrangement.size() == 2) {
            canBeLose = (op3MaxPairId >= 0);
        }

        ret = botArrangement.getPair(botMaxPairId); // có thể tối ưu hơn. next update
        if (canBeLose) {
//            ret = botArrangement.getPair(botMaxPairId); // có thể tối ưu hơn. next update
            System.out.println("getPair10");
            return TldlBot.processScore(ret, score, canCatch, true, botArrangement);
        }

        if (canCatch) {
            if (maxCardId >= 0) {
                if (optimalPairId < 0 && BasicArrangement.is2(botMaxPairId)) {
                    ret = botArrangement.getPair(botArrangement.getOptimalPair2(maxCardId)); // có thể tối ưu hơn. next update
                } else {
                    ret = botArrangement.getPair(optimalPairId < 0 ? botMaxPairId : optimalPairId); // có thể tối ưu hơn. next update
                }

                System.out.println("getPair11. optimalPairId = " + optimalPairId + ", botMaxPairId = " + botMaxPairId);
                return TldlBot.processScore(ret, score, optimalPairId < 0, true, botArrangement);
            } else if (botArrangement.canBeWinAfter(ret) || minCardNumber <= 2) {
                if (optimalPairId >= 0 && optimalPairId != botMaxPairId && minCardNumber != 2) {
                    System.out.println("getPair12");
                    ret = botArrangement.getPair(optimalPairId);
                    return TldlBot.processScore(ret, score, true, true, botArrangement);
                } else {
                    System.out.println("getPair12b");
                    return TldlBot.processScore(ret, score, true, true, botArrangement);
                }
            }
        }

        if (optimalPairId > 0 && botArrangement.isRac(optimalPairId, BasicArrangement.SETTYPE_PAIR)) {
            ret = botArrangement.getPair(optimalPairId);
            System.out.println("getPair13");
            return TldlBot.processScore(ret, score, false, canCatch, botArrangement);
        }

        if (maxCardId < 0) {
            return null;
        }

        // chặt 2 heo.
        if (BasicArrangement.is2(maxCardId)) {
            ret = TldlBot.getFourOfKind(0, botArrangement, op1Arrangement, op2Arrangement, op3Arrangement, score, isCheck);
            if (ret != null) {
                System.out.println("getPair14");
                return ret;
            }
        }

        ret = botArrangement.getPair(botMaxPairId);
        // choi 3-4 nguoi thi bot hơi lỗ
        if (isCheck == false) {
            if (op3Arrangement.size() > 0) {
                if (TldlBot.canBeWinNewRound(op3Arrangement, botArrangement, op1Arrangement, op2Arrangement)) {
                    System.out.println("getPair15");
                    return TldlBot.processScore(ret, score, false, false, botArrangement);
                }
            } else if (op2Arrangement.size() > 0) {
                if (TldlBot.canBeWinNewRound(op2Arrangement, op3Arrangement, botArrangement, op1Arrangement)) {
                    System.out.println("getPair16");
                    return TldlBot.processScore(ret, score, false, false, botArrangement);
                }
            } else if (op1Arrangement.size() > 0) {
                if (TldlBot.canBeWinNewRound(op1Arrangement, op2Arrangement, op3Arrangement, botArrangement)) {
                    System.out.println("getPair17");
                    return TldlBot.processScore(ret, score, false, false, botArrangement);
                }
            }
        }

        // Biết vẫn bắt, có xác xuất đối thủ bỏ.
        if (canCatch == false && minHighCardNumber >= botArrangement.getHighCardNumber() && (maxId / 4) > (botMaxPairId / 4)) {
            System.out.println("getPair18");
            return TldlBot.processScore(ret, score, false, false, botArrangement);
        }

        if (Utils.nextInt(100) <= 25) {// && maxCardId >= 0) {// 25%
            System.out.println("getPair18b");
            return TldlBot.processScore(ret, score, false, false, botArrangement);
        }

        System.out.println("getPair19");
        return null; // bỏ qua
    }

    // DONE
    public static List<Card> getThreeOfKind(int maxCardId, TLArrangement botArrangement,
            TLArrangement op1Arrangement, TLArrangement op2Arrangement, TLArrangement op3Arrangement,
            TLTurnScore score, boolean isCheck) {
        if (botArrangement == null) {
            System.out.println("getThreeOfKind01");
            return null;
        }

        int minCardNumber = TldlBot.getMinCardNumber(op1Arrangement, op2Arrangement, op3Arrangement);
        int optimalThreeId = botArrangement.getOptimalThreeId(maxCardId);

        int botMaxThreeId = botArrangement.getBiggestThreeId(maxCardId);
        if (botMaxThreeId < 0) { // Cannot catch.
            System.out.println("getThreeOfKind02");
            return null;
        }

        int maxId = 0;
        int minHighCardNumber = 13;
        List<Card> ret = null;
        boolean canCatch = true;
        int op1MaxThreeId = op1Arrangement == null ? - 1 : op1Arrangement.getBiggestThreeId(maxCardId);
        int op2MaxThreeId = op2Arrangement == null ? - 1 : op2Arrangement.getBiggestThreeId(maxCardId);
        int op3MaxThreeId = op3Arrangement == null ? - 1 : op3Arrangement.getBiggestThreeId(maxCardId);
        int oppMaxThreeId = TldlBot.max(op1MaxThreeId, op2MaxThreeId, op3MaxThreeId);
        if (TldlBot.compareTLID(botMaxThreeId, op1MaxThreeId) < 0) {
            if (op1Arrangement.size() == 3) { // chạy heo + point
                ret = botArrangement.getThree(botMaxThreeId);
                System.out.println("getThreeOfKind03");
                return TldlBot.processScore(ret, score, false, false, botArrangement);
            }

            if (op1MaxThreeId == oppMaxThreeId && op1Arrangement.canBeWinAfter(op1Arrangement.getThree(op1MaxThreeId))) {
                ret = botArrangement.getThree(botMaxThreeId); // chạy
                System.out.println("getThreeOfKind04");
                return TldlBot.processScore(ret, score, false, false, botArrangement);
            }

            canCatch = false;
            maxId = op1MaxThreeId;
            minHighCardNumber = op1Arrangement.getHighCardNumber();
        }

        if (TldlBot.compareTLID(botMaxThreeId, op2MaxThreeId) < 0) {
            if (op2MaxThreeId == oppMaxThreeId && op2Arrangement.canBeWinAfter(op2Arrangement.getThree(op2MaxThreeId))) {
                ret = botArrangement.getThree(botMaxThreeId); // chạy
                System.out.println("getThreeOfKind05");
                return TldlBot.processScore(ret, score, false, false, botArrangement);
            }

            if (op2Arrangement.size() == 3) { // chạy heo + point
                ret = botArrangement.getThree(botMaxThreeId); // hơi nhát, dễ bị nát bài
                System.out.println("getThreeOfKind06");
                return TldlBot.processScore(ret, score, false, false, botArrangement);
            }

            canCatch = false;
            if (maxId < op2MaxThreeId) {
                maxId = op2MaxThreeId;
            }

            if (minHighCardNumber > op2Arrangement.getHighCardNumber()) {
                minHighCardNumber = op2Arrangement.getHighCardNumber();
            }
        }

        if (TldlBot.compareTLID(botMaxThreeId, op3MaxThreeId) < 0) {
            if (op3MaxThreeId == oppMaxThreeId && op3Arrangement.canBeWinAfter(op3Arrangement.getThree(op3MaxThreeId))) {
                ret = botArrangement.getThree(botMaxThreeId); // chạy
                System.out.println("getThreeOfKind07");
                return TldlBot.processScore(ret, score, false, false, botArrangement);
            }

            if (op3Arrangement.size() == 3) { // chạy heo + point
                ret = botArrangement.getThree(botMaxThreeId); // hơi nhát, dễ bị nát bài
                System.out.println("getThreeOfKind08");
                return TldlBot.processScore(ret, score, false, false, botArrangement);
            }

            canCatch = false;
            if (maxId < op3MaxThreeId) {
                maxId = op3MaxThreeId;
            }

            if (minHighCardNumber > op3Arrangement.getHighCardNumber()) {
                minHighCardNumber = op3Arrangement.getHighCardNumber();
            }
        }

        boolean canBeLose = false;
        if (op1Arrangement.size() == 3) {
            canBeLose = (op1MaxThreeId >= 0);
        } else if (op1Arrangement.size() == 0 && op2Arrangement.size() == 3) {
            canBeLose = (op2MaxThreeId >= 0);
        } else if (op1Arrangement.size() == 0 && op2Arrangement.size() == 0 && op3Arrangement.size() == 3) {
            canBeLose = (op3MaxThreeId >= 0);
        }

        ret = botArrangement.getThree(botMaxThreeId); // có thể tối ưu hơn. next update
        if (canBeLose) {
//            ret = botArrangement.getThree(botMaxThreeId); // có thể tối ưu hơn. next update
            System.out.println("getThreeOfKind09");
            return TldlBot.processScore(ret, score, canCatch, true, botArrangement);
        }

        if (canCatch) {
            if (maxCardId >= 0) {
                ret = botArrangement.getThree(optimalThreeId < 0 ? botMaxThreeId : optimalThreeId); // có thể tối ưu hơn. next update
                if (ret != null && BasicArrangement.isAceOr2(ret.get(0))) {
                    if (botArrangement.canBeWinAfter(ret)) {
                        System.out.println("getThreeOfKind10");
                        return TldlBot.processScore(ret, score, optimalThreeId < 0, true, botArrangement);
                    }
                } else {
                    System.out.println("getThreeOfKind11");
                    return TldlBot.processScore(ret, score, optimalThreeId < 0, true, botArrangement);
                }
            } else if (botArrangement.canBeWinAfter(ret) || minCardNumber == 1) {
                if (optimalThreeId >= 0 && optimalThreeId != botMaxThreeId && minCardNumber != 3) {
                    System.out.println("getThreeOfKind12");
                    ret = botArrangement.getThree(optimalThreeId);
                    return TldlBot.processScore(ret, score, true, true, botArrangement);
                } else {
                    System.out.println("getThreeOfKind12b");
                    return TldlBot.processScore(ret, score, true, true, botArrangement);
                }
            }
        }

        if (optimalThreeId > 0 && botArrangement.isRac(optimalThreeId, BasicArrangement.SETTYPE_THREE)) {
            ret = botArrangement.getThree(optimalThreeId);
            System.out.println("getThreeOfKind13");
            return TldlBot.processScore(ret, score, false, canCatch, botArrangement);
        }

        if (maxCardId < 0) {
            return null;
        }

        // chặt 3 heo.
        if (BasicArrangement.is2(maxCardId)) {
            ret = TldlBot.get4PairStraight(0, botArrangement, op1Arrangement, op2Arrangement, op3Arrangement, score, isCheck);
            if (ret != null) {
               System.out.println("getThreeOfKind14");
                return ret;
            }
        }

        ret = botArrangement.getThree(botMaxThreeId);
        // choi 3-4 nguoi thi bot hơi lỗ
        if (isCheck == false) {
            if (op3Arrangement.size() > 0) {
                if (TldlBot.canBeWinNewRound(op3Arrangement, botArrangement, op1Arrangement, op2Arrangement)) {
                    System.out.println("getThreeOfKind15");
                    return TldlBot.processScore(ret, score, false, false, botArrangement);
                }
            } else if (op2Arrangement.size() > 0) {
                if (TldlBot.canBeWinNewRound(op2Arrangement, op3Arrangement, botArrangement, op1Arrangement)) {
                    System.out.println("getThreeOfKind16");
                    return TldlBot.processScore(ret, score, false, false, botArrangement);
                }
            } else if (op1Arrangement.size() > 0) {
                if (TldlBot.canBeWinNewRound(op1Arrangement, op2Arrangement, op3Arrangement, botArrangement)) {
                    System.out.println("getThreeOfKind17");
                    return TldlBot.processScore(ret, score, false, false, botArrangement);
                }
            }
        }

        // Biết vẫn bắt, có xác xuất đối thủ bỏ.
        if (canCatch == false && minHighCardNumber >= botArrangement.getHighCardNumber() && (maxId / 4) > (botMaxThreeId / 4)) {
            System.out.println("getThreeOfKind18");
            return TldlBot.processScore(ret, score, false, false, botArrangement);
        }

        if (Utils.nextInt(100) <= 16) {// && maxCardId >= 0) {// 16%
            System.out.println("getThreeOfKind18b");
            return TldlBot.processScore(ret, score, false, false, botArrangement);
        }

        System.out.println("getThreeOfKind19");
        return null; // bỏ qua
    }

    // DONE
    public static List<Card> getStraight(int maxCardId, int cardNum, TLArrangement botArrangement,
            TLArrangement op1Arrangement, TLArrangement op2Arrangement, TLArrangement op3Arrangement,
            TLTurnScore score, boolean isCheck) {
        if (botArrangement == null) {
            return null;
        }

        int optimalStraightId = botArrangement.getOptimalStraightId(cardNum, maxCardId);

        int botMaxStraightId = botArrangement.getBiggestStraightId(cardNum, maxCardId);
        if (botMaxStraightId < 0) { // Cannot catch.
            return null;
        }

        List<Card> ret = null;
        boolean canCatch = true;
        int op1MaxStraightId = op1Arrangement == null ? - 1 : op1Arrangement.getBiggestStraightId(cardNum, maxCardId);
        int op2MaxStraightId = op2Arrangement == null ? - 1 : op2Arrangement.getBiggestStraightId(cardNum, maxCardId);
        int op3MaxStraightId = op3Arrangement == null ? - 1 : op3Arrangement.getBiggestStraightId(cardNum, maxCardId);
        int oppMaxStraightId = TldlBot.max(op1MaxStraightId, op2MaxStraightId, op3MaxStraightId);
        if (TldlBot.compareTLID(botMaxStraightId, op1MaxStraightId) < 0) {
            if (op1Arrangement != null && op1Arrangement.size() == cardNum) { // chạy heo + point
                ret = botArrangement.getStraight(botMaxStraightId, cardNum);
                return TldlBot.processScore(ret, score, false, false, botArrangement);
            }

            if (op1MaxStraightId == oppMaxStraightId && op1Arrangement != null && op1Arrangement.canBeWinAfter(op1Arrangement.getStraight(op1MaxStraightId, cardNum))) {
                ret = botArrangement.getStraight(botMaxStraightId, cardNum); // chạy
                return TldlBot.processScore(ret, score, false, false, botArrangement);
            }

            canCatch = false;
        }

        if (TldlBot.compareTLID(botMaxStraightId, op2MaxStraightId) < 0) {
            if (op2MaxStraightId == oppMaxStraightId && op2Arrangement != null && op2Arrangement.canBeWinAfter(op2Arrangement.getStraight(op2MaxStraightId, cardNum))) {
                ret = botArrangement.getStraight(botMaxStraightId, cardNum); // chạy
                return TldlBot.processScore(ret, score, false, false, botArrangement);
            }

            if (op2Arrangement != null && op2Arrangement.size() == cardNum) { // chạy heo + point
                ret = botArrangement.getStraight(botMaxStraightId, cardNum); // hơi nhát, dễ bị nát bài
                return TldlBot.processScore(ret, score, false, false, botArrangement);
            }

            canCatch = false;
        }

        if (TldlBot.compareTLID(botMaxStraightId, op3MaxStraightId) < 0) {
            if (op3MaxStraightId == oppMaxStraightId && op3Arrangement != null && op3Arrangement.canBeWinAfter(op3Arrangement.getStraight(op3MaxStraightId, cardNum))) {
                ret = botArrangement.getStraight(botMaxStraightId, cardNum); // chạy
                return TldlBot.processScore(ret, score, false, false, botArrangement);
            }

            if (op3Arrangement != null && op3Arrangement.size() == cardNum) { // chạy heo + point
                ret = botArrangement.getStraight(botMaxStraightId, cardNum); // hơi nhát, dễ bị nát bài
                return TldlBot.processScore(ret, score, false, false, botArrangement);
            }

            canCatch = false;
        }

        boolean canBeLose = false;
        if (op1Arrangement.size() == cardNum) {
            canBeLose = (op1MaxStraightId >= 0);
        } else if (op1Arrangement.size() == 0 && op2Arrangement.size() == cardNum) {
            canBeLose = (op2MaxStraightId >= 0);
        } else if (op1Arrangement.size() == 0 && op2Arrangement.size() == 0 && op3Arrangement.size() == cardNum) {
            canBeLose = (op3MaxStraightId >= 0);
        }

        ret = botArrangement.getStraight(botMaxStraightId, cardNum);
        if (canBeLose) {
//            ret = botArrangement.getStraight(botMaxStraightId, cardNum);
            return TldlBot.processScore(ret, score, canCatch, true, botArrangement);
        }

        if (canCatch) {
            if (maxCardId >= 0) {
                if (optimalStraightId < 0) {
                    ret = botArrangement.getStraight(botMaxStraightId, cardNum);
                    return TldlBot.processScore(ret, score, true, true, botArrangement);
                }

                List<Card> tmpOpt = botArrangement.getStraight(optimalStraightId, cardNum);
                List<Card> tmpMax = botArrangement.getStraight(botMaxStraightId, cardNum);
                if (tmpOpt == null || tmpOpt.isEmpty() || tmpMax == null || tmpMax.isEmpty()) {
                    return null; // bug
//			} else if (tmpMax.get(tmpMax.size() - 1).getCardNumber() - tmpOpt.get(tmpOpt.size() - 1).getCardNumber() >= cardNum) { // có thể tối ưu hơn
                } else if (botArrangement.has2Straight(tmpMax.get(tmpMax.size() - 1).getCardNumber(), tmpMax.size(),
                        tmpOpt.get(tmpOpt.size() - 1).getCardNumber(), tmpOpt.size())) { // có thể tối ưu hơn
                    return TldlBot.processScore(tmpOpt, score, false, true, botArrangement);
                } else {
                    return TldlBot.processScore(tmpMax, score, true, true, botArrangement);
                }
            } else if (botArrangement.canBeWinAfter(ret)) {
                return TldlBot.processScore(ret, score, true, true, botArrangement);
            }
        }

        if (optimalStraightId > 0 && botArrangement.isStraightRac(optimalStraightId, cardNum)) {
            ret = botArrangement.getStraight(optimalStraightId, cardNum);
            return TldlBot.processScore(ret, score, false, false, botArrangement);
        }

        ret = botArrangement.getStraight(botMaxStraightId, cardNum);
        // choi 3-4 nguoi thi bot hơi lỗ
        if (maxCardId >= 0 && isCheck == false) {
            if (op3Arrangement.size() > 0) {
                if (TldlBot.canBeWinNewRound(op3Arrangement, botArrangement, op1Arrangement, op2Arrangement)) {
                    return TldlBot.processScore(ret, score, false, true, botArrangement);
                }
            } else if (op2Arrangement.size() > 0) {
                if (TldlBot.canBeWinNewRound(op2Arrangement, op3Arrangement, botArrangement, op1Arrangement)) {
                    return TldlBot.processScore(ret, score, false, true, botArrangement);
                }
            } else if (op1Arrangement.size() > 0) {
                if (TldlBot.canBeWinNewRound(op1Arrangement, op2Arrangement, op3Arrangement, botArrangement)) {
                    return TldlBot.processScore(ret, score, false, true, botArrangement);
                }
            }
        }

        return null; // bỏ qua
    }

    // DONE
    public static List<Card> get3PairStraight(int maxCardId, TLArrangement botArrangement,
            TLArrangement op1Arrangement, TLArrangement op2Arrangement, TLArrangement op3Arrangement,
            TLTurnScore score, boolean isCheck) {
        if (botArrangement == null) {
            return null;
        }

        int botMax3PairStraightId = botArrangement.getBiggestPairStraightId(3, maxCardId);
        if (botMax3PairStraightId < 0) { // Cannot catch.
//            return null;
            return TldlBot.getFourOfKind(0, botArrangement, op1Arrangement, op2Arrangement, op3Arrangement, score, isCheck);
        }

        int optimal3PairStraightId = botArrangement.getOptimalPairStraightId(3, maxCardId);

        int op1Max3PairStraightId = op1Arrangement == null ? - 1 : op1Arrangement.getBiggestPairStraightId(3, maxCardId);
        int op2Max3PairStraightId = op2Arrangement == null ? - 1 : op2Arrangement.getBiggestPairStraightId(3, maxCardId);
        int op3Max3PairStraightId = op3Arrangement == null ? - 1 : op3Arrangement.getBiggestPairStraightId(3, maxCardId);
        if (TldlBot.compareTLID(botMax3PairStraightId, op1Max3PairStraightId) < 0
                || TldlBot.compareTLID(botMax3PairStraightId, op2Max3PairStraightId) < 0
                || TldlBot.compareTLID(botMax3PairStraightId, op3Max3PairStraightId) < 0) {
            // để thúi hehe
            return TldlBot.getFourOfKind(0, botArrangement, op1Arrangement, op2Arrangement, op3Arrangement, score, isCheck);
        } else {
            if (maxCardId >= 0) {
                int op1MaxFourId = op1Arrangement == null ? - 1 : op1Arrangement.getBiggestFourId(maxCardId);
                int op2MaxFourId = op2Arrangement == null ? - 1 : op2Arrangement.getBiggestFourId(maxCardId);
                int op3MaxFourId = op3Arrangement == null ? - 1 : op3Arrangement.getBiggestFourId(maxCardId);
                if (op1MaxFourId >= 0 || op2MaxFourId >= 0 || op3MaxFourId >= 0) {
                    // 1 đối thủ có tứ quý
                    return TldlBot.getFourOfKind(0, botArrangement, op1Arrangement, op2Arrangement, op3Arrangement, score, isCheck);
                }

                int op1Max4PairStraightId = op1Arrangement == null ? - 1 : op1Arrangement.getBiggestPairStraightId(4, -1);
                int op2Max4PairStraightId = op2Arrangement == null ? - 1 : op2Arrangement.getBiggestPairStraightId(4, -1);
                int op3Max4PairStraightId = op3Arrangement == null ? - 1 : op3Arrangement.getBiggestPairStraightId(4, -1);
                if (op1Max4PairStraightId >= 0 || op2Max4PairStraightId >= 0 || op3Max4PairStraightId >= 0) {
                    // 1 đối thủ có 4 đôi thông
                    return TldlBot.get4PairStraight(0, botArrangement, op1Arrangement, op2Arrangement, op3Arrangement, score, isCheck);
                }

                // Chặt
                List<Card> ret = botArrangement.get3PairStraight(optimal3PairStraightId < 0 ? botMax3PairStraightId : optimal3PairStraightId); // can be optimized more.
                return TldlBot.processScore(ret, score, true, true, botArrangement); // can be optimized more.
            } else {
                List<Card> ret = botArrangement.get3PairStraight(botMax3PairStraightId); // can be optimized more.
                if (botArrangement.canBeWinAfter(ret)) {
                    return TldlBot.processScore(ret, score, true, true, botArrangement); // can be optimized more.
                }

                return null;
            }
        }
    }

    // DONE
    public static List<Card> getFourOfKind(int maxCardId, TLArrangement botArrangement,
            TLArrangement op1Arrangement, TLArrangement op2Arrangement, TLArrangement op3Arrangement,
            TLTurnScore score, boolean isCheck) {
        if (botArrangement == null) {
            return null;
        }

        int botMaxFourId = botArrangement.getBiggestFourId(maxCardId);
        if (botMaxFourId < 0) { // Cannot catch.
//            return null;
            return TldlBot.get4PairStraight(0, botArrangement, op1Arrangement, op2Arrangement, op3Arrangement, score, isCheck);
        }

        int optimalFourId = botArrangement.getOptimalFourId(maxCardId);

        int op1MaxFourId = op1Arrangement == null ? -1 : op1Arrangement.getBiggestFourId(maxCardId);
        int op2MaxFourId = op2Arrangement == null ? -1 : op2Arrangement.getBiggestFourId(maxCardId);
        int op3MaxFourId = op3Arrangement == null ? -1 : op3Arrangement.getBiggestFourId(maxCardId);
        if (TldlBot.compareTLID(botMaxFourId, op1MaxFourId) < 0
                || TldlBot.compareTLID(botMaxFourId, op2MaxFourId) < 0
                || TldlBot.compareTLID(botMaxFourId, op3MaxFourId) < 0) {
            // Check 4 pair straight: tìm 4 đôi thông bắt tứ quý
            return TldlBot.get4PairStraight(0, botArrangement, op1Arrangement, op2Arrangement, op3Arrangement, score, isCheck);
        } else {
            if (maxCardId >= 0) {
                int op1Max4PairStraightId = op1Arrangement == null ? -1 : op1Arrangement.getBiggestPairStraightId(4, -1);
                int op2Max4PairStraightId = op2Arrangement == null ? -1 : op2Arrangement.getBiggestPairStraightId(4, -1);
                int op3Max4PairStraightId = op3Arrangement == null ? -1 : op3Arrangement.getBiggestPairStraightId(4, -1);
                if (op1Max4PairStraightId >= 0 || op2Max4PairStraightId >= 0 || op3Max4PairStraightId >= 0) {
                    // 1 đối thủ có 4 đôi thông
                    return TldlBot.get4PairStraight(0, botArrangement, op1Arrangement, op2Arrangement, op3Arrangement, score, isCheck);
                }

                // Chặt
                List<Card> ret = botArrangement.getFour(optimalFourId < 0 ? botMaxFourId : optimalFourId); // có thể tối ưu hơn. next update
                return TldlBot.processScore(ret, score, true, true, botArrangement); // can be optimized more.
            } else {
                List<Card> ret = botArrangement.getFour(botMaxFourId); // có thể tối ưu hơn. next update
                if (botArrangement.canBeWinAfter(ret)) {
                    return TldlBot.processScore(ret, score, true, true, botArrangement); // can be optimized more.
                }

                return null;
            }
        }
    }

    // DONE
    public static List<Card> get4PairStraight(int maxCardId, TLArrangement botArrangement,
            TLArrangement op1Arrangement, TLArrangement op2Arrangement, TLArrangement op3Arrangement,
            TLTurnScore score, boolean isCheck) {
        if (botArrangement == null) {
            return null;
        }

        int botMax4PairStraightId = botArrangement.getBiggestPairStraightId(4, maxCardId);
        if (botMax4PairStraightId < 0) { // Cannot catch.
            return null;
        }

        int op1Max4PairStraightId = op1Arrangement == null ? - 1 : op1Arrangement.getBiggestPairStraightId(4, maxCardId);
        int op2Max4PairStraightId = op2Arrangement == null ? - 1 : op2Arrangement.getBiggestPairStraightId(4, maxCardId);
        int op3Max4PairStraightId = op3Arrangement == null ? - 1 : op3Arrangement.getBiggestPairStraightId(4, maxCardId);
        if (TldlBot.compareTLID(botMax4PairStraightId, op1Max4PairStraightId) < 0
                || TldlBot.compareTLID(botMax4PairStraightId, op2Max4PairStraightId) < 0
                || TldlBot.compareTLID(botMax4PairStraightId, op3Max4PairStraightId) < 0) {
            // để thúi hehe
            return null;
        } else {
            List<Card> ret = botArrangement.get4PairStraight(botMax4PairStraightId);
            if (maxCardId >= 0 || botArrangement.canBeWinAfter(ret)) {
                // Chặt
                return TldlBot.processScore(ret, score, true, true, botArrangement); // can be optimized more.
            }

            return null;
        }
    }

    /**
     * input list must be sorted.
     */
    public static int getSetType(List<Card> currentCards) {
        if (currentCards == null || currentCards.isEmpty()) {
            return BasicArrangement.SETTYPE_UNKNOWN;
        }

        switch (currentCards.size()) {
            case 1: // High card
                return BasicArrangement.SETTYPE_HIGHCARD;
            case 2: // Pair
                if (currentCards.get(0).getCardNumber() == currentCards.get(1).getCardNumber()) {
                    return BasicArrangement.SETTYPE_PAIR;
                }

                break; //unknown
            case 3: // Three of kind or Straight 3
                if ((currentCards.get(0).getCardNumber() == currentCards.get(1).getCardNumber())
                        && (currentCards.get(0).getCardNumber() == currentCards.get(2).getCardNumber())) {
                    return BasicArrangement.SETTYPE_THREE;
                }

                if (BasicArrangement.is2(currentCards.get(0)) == false
                        && (currentCards.get(0).getCardNumber() + 1 == currentCards.get(1).getCardNumber())
                        && (currentCards.get(1).getCardNumber() + 1 == currentCards.get(2).getCardNumber())) {
                    return BasicArrangement.SETTYPE_STRAIGHT;
                }

                break; //unknown
            case 4: // Four of kind or Straight 4
                if ((currentCards.get(0).getCardNumber() == currentCards.get(1).getCardNumber())
                        && (currentCards.get(0).getCardNumber() == currentCards.get(2).getCardNumber())
                        && (currentCards.get(0).getCardNumber() == currentCards.get(3).getCardNumber())) {
                    return BasicArrangement.SETTYPE_FOUR;
                }

                if (BasicArrangement.is2(currentCards.get(0)) == false
                        && (currentCards.get(0).getCardNumber() + 1 == currentCards.get(1).getCardNumber())
                        && (currentCards.get(1).getCardNumber() + 1 == currentCards.get(2).getCardNumber())
                        && (currentCards.get(2).getCardNumber() + 1 == currentCards.get(3).getCardNumber())) {
                    return BasicArrangement.SETTYPE_STRAIGHT;
                }

                break; //unknown
            default: // number of cards > 4
                if (BasicArrangement.is2(currentCards.get(0))) {
                    return BasicArrangement.SETTYPE_UNKNOWN;
                }

                if (currentCards.size() == 6
                        && // 3 Pair Straight
                        (currentCards.get(0).getCardNumber() == currentCards.get(1).getCardNumber())
                        && (currentCards.get(1).getCardNumber() + 1 == currentCards.get(2).getCardNumber())
                        && (currentCards.get(2).getCardNumber() == currentCards.get(3).getCardNumber())
                        && (currentCards.get(3).getCardNumber() + 1 == currentCards.get(4).getCardNumber())
                        && (currentCards.get(4).getCardNumber() == currentCards.get(5).getCardNumber())) {
                    return BasicArrangement.SETTYPE_3PAIR;
                }

                if (currentCards.size() == 8
                        && // 4 Pair Straight
                        (currentCards.get(0).getCardNumber() == currentCards.get(1).getCardNumber())
                        && (currentCards.get(1).getCardNumber() + 1 == currentCards.get(2).getCardNumber())
                        && (currentCards.get(2).getCardNumber() == currentCards.get(3).getCardNumber())
                        && (currentCards.get(3).getCardNumber() + 1 == currentCards.get(4).getCardNumber())
                        && (currentCards.get(4).getCardNumber() == currentCards.get(5).getCardNumber())
                        && (currentCards.get(5).getCardNumber() + 1 == currentCards.get(6).getCardNumber())
                        && (currentCards.get(6).getCardNumber() == currentCards.get(7).getCardNumber())) {
                    return BasicArrangement.SETTYPE_4PAIR;
                }

                for (int i = 1; i < currentCards.size(); i++) {
                    if (currentCards.get(i - 1).getCardNumber() + 1 != currentCards.get(i).getCardNumber()) {
                        return BasicArrangement.SETTYPE_UNKNOWN;
                    }
                }

                return BasicArrangement.SETTYPE_STRAIGHT;
        }

        return BasicArrangement.SETTYPE_UNKNOWN;
    }

    private static int compareTLID(int id1, int id2) {
        if (id1 == id2) {
            return 0;
        }

        return id1 < id2 ? -1 : 1;
//        if (BasicArrangement.is2(id1)) {
//            if (BasicArrangement.is2(id2)) {
//                return id1 < id2 ? -1 : 1;
//            } else {
//                return 1;
//            }
//        } else {
//            if (BasicArrangement.is2(id2)) {
//                return -1;
//            } else {
//                return id1 < id2 ? -1 : 1;
//            }
//        }
    }

    private static int max(int id1, int id2, int id3) {
        int ret = id1;
        if (TldlBot.compareTLID(id1, id2) < 0) {
            ret = id2;
        }

        if (TldlBot.compareTLID(ret, id3) < 0) {
            ret = id3;
        }

        return ret;
    }

    public static List<Card> processScore(List<Card> turnCards, TLTurnScore score, boolean isMaxTurn, boolean canCatch, TLArrangement botArrangement) {
        if (score == null) {
            return turnCards;
        }

        if (isMaxTurn) {
            score.setIsMaxTurn();
        }

        if (canCatch) {
            score.setCanCatch();
        }

        score.addScore(turnCards, botArrangement.getRemainingCards(turnCards), botArrangement.getWaitFor2());
        return turnCards;
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

    private static boolean canBeWinNewRound(TLArrangement botArrangement,
            TLArrangement op1Arrangement, TLArrangement op2Arrangement, TLArrangement op3Arrangement) {
        TLTurnScore score = new TLTurnScore();
        List<Card> tmp;
        // Get option and score for Three Of Kinds
        tmp = TldlBot.getThreeOfKind(-1, botArrangement, op1Arrangement, op2Arrangement, op3Arrangement, score, true);
        if (score.willBeWin() || botArrangement.canBeWinAfter(tmp)) {
            return true;
        }

        // Get option and score for all Straights
        int maxStraightLength = botArrangement.getMaxStraightLength();
        for (int i = 3; i <= maxStraightLength; i++) {
            score.clear();
            tmp = TldlBot.getStraight(-1, i, botArrangement, op1Arrangement, op2Arrangement, op3Arrangement, score, true);
            if (score.willBeWin() || botArrangement.canBeWinAfter(tmp)) {
                return true;
            }
        }

        // Get option and score for Pair
        score.clear();
        tmp = TldlBot.getPair(-1, botArrangement, op1Arrangement, op2Arrangement, op3Arrangement, score, true);
        if (score.willBeWin() || botArrangement.canBeWinAfter(tmp)) {
            return true;
        }

        // Get option and score for HighCard
        score.clear();
        tmp = TldlBot.getHighCard(-1, botArrangement, op1Arrangement, op2Arrangement, op3Arrangement, score, true);
        if (score.willBeWin() || botArrangement.canBeWinAfter(tmp)) {
            return true;
        }

        // Get option and score for 4 đôi thông
        score.clear();
        tmp = TldlBot.get4PairStraight(-1, botArrangement, op1Arrangement, op2Arrangement, op3Arrangement, score, true);
        if (score.willBeWin() || botArrangement.canBeWinAfter(tmp)) {
            return true;
        }

        // Get option and score for tứ quý
        score.clear();
        tmp = TldlBot.getFourOfKind(-1, botArrangement, op1Arrangement, op2Arrangement, op3Arrangement, score, true);
        if (score.willBeWin() || botArrangement.canBeWinAfter(tmp)) {
            return true;
        }

        // Get option and score for 3 đôi thông
        score.clear();
        tmp = TldlBot.get3PairStraight(-1, botArrangement, op1Arrangement, op2Arrangement, op3Arrangement, score, true);
        if (score.willBeWin() || botArrangement.canBeWinAfter(tmp)) {
            return true;
        }

        return false;
    }

    private static int getMinCardNumber(TLArrangement op1Arrangement, TLArrangement op2Arrangement, TLArrangement op3Arrangement) {
        int ret = 13;
        if (op1Arrangement != null && op1Arrangement.size() > 0 && ret > op1Arrangement.size()) {
            ret = op1Arrangement.size();
        }

        if (op2Arrangement != null && op2Arrangement.size() > 0 && ret > op2Arrangement.size()) {
            ret = op2Arrangement.size();
        }

        if (op3Arrangement != null && op3Arrangement.size() > 0 && ret > op3Arrangement.size()) {
            ret = op3Arrangement.size();
        }

        return ret;
    }
}
