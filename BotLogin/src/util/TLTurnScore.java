/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package util;

import java.util.Collections;
import java.util.List;

/**
 *
 * @author binhnt
 */
public class TLTurnScore {

    private static final int MAX_NORMAL_SCORE = 200;
    private static final int MAX_TOTAL_SCORE = 1000;

    private boolean canCatch;
    private boolean isMaxTurn;
    private int basicScore;
    
    private boolean canBeWin = false;

    public TLTurnScore() {
        this.canCatch = false;
        this.isMaxTurn = false;
        this.basicScore = 0;
    }

    public void clear() {
        this.canCatch = false;
        this.isMaxTurn = false;
        this.basicScore = 0;
        this.canBeWin = false;
    }

    public void setCanCatch() {
        this.canCatch = true;
    }

    public void setIsMaxTurn() {
        this.canCatch = true;
        this.isMaxTurn = true;
    }

    public void addScore(int value) {
        this.basicScore += value;
    }

    public boolean willBeWin() {
        if (this.basicScore >= TLTurnScore.MAX_TOTAL_SCORE) {
            return true;
        }

        return this.canBeWin;
    }

    public void addScore(List<Card> turnCards, List<Card> remainingCards, boolean waitFor2) {
        if (remainingCards == null || remainingCards.isEmpty()) {
            // hết bài.
            this.basicScore += TLTurnScore.MAX_TOTAL_SCORE;
            return;
        }

//        int tmpScore = 13 - remainingCards.size();
//        int tmpScore2 = 0;
//        Collections.sort(remainingCards);
//        int setType = TldlBot.getSetType(remainingCards);
//        if (setType != BasicArrangement.SETTYPE_UNKNOWN) {
//            tmpScore2 += (this.isMaxTurn ? 5 : 4);
//            this.canBeWin = true;
//        } else {
//            tmpScore2 += Math.max(0, 5 - Math.min(this.getNumberOfSameCardSet(remainingCards), this.getNumberOfOpenStraight(remainingCards)));
//        }
//
//        this.basicScore += this.canCatch ? TLTurnScore.MAX_NORMAL_SCORE : 0;
//        this.basicScore += (tmpScore * 10 + tmpScore2);

        Collections.sort(turnCards);
        int tmpScore = 13 - (turnCards.isEmpty() ? 0 : turnCards.get(0).getCardNumber());
        int tmpScore2 = 0;
        Collections.sort(remainingCards);
        int setType = TldlBot.getSetType(remainingCards);
        if (setType != BasicArrangement.SETTYPE_UNKNOWN) {
            tmpScore2 += (this.isMaxTurn ? 5 : 4);
            this.canBeWin = true;
        } else {
            tmpScore2 += Math.max(0, 5 - Math.min(this.getNumberOfSameCardSet(remainingCards), this.getNumberOfOpenStraight(remainingCards)));
        }

        this.basicScore += this.canCatch ? TLTurnScore.MAX_NORMAL_SCORE : 0;
        this.basicScore += (tmpScore * 10 + tmpScore2);
    }

    public int getScore() {
        int ret = 0;//this.canCatch ? TLTurnScore.MAX_NORMAL_SCORE : 0;
        ret += this.basicScore;
        return ret;
    }

    // remainingCards must be sorted.
    private int getLastSetScore(int type, boolean waitFor2, List<Card> remainingCards) {
        if (remainingCards == null || remainingCards.isEmpty()) {
            return MAX_NORMAL_SCORE;
        }

        switch (type) {
            case BasicArrangement.SETTYPE_UNKNOWN:
                return (3 - Math.min(this.getNumberOfSameCardSet(remainingCards), this.getNumberOfOpenStraight(remainingCards))) * 13;
            case BasicArrangement.SETTYPE_HIGHCARD:
                return (remainingCards.get(0).getCardNumber() - 6) * 13;
            case BasicArrangement.SETTYPE_PAIR:
                return (remainingCards.get(0).getCardNumber() - 6) * 13;
            case BasicArrangement.SETTYPE_THREE:
                return (remainingCards.get(0).getCardNumber() - 6) * 9;
            case BasicArrangement.SETTYPE_STRAIGHT:
                return (remainingCards.get(remainingCards.size() - 1).getCardNumber() - 6) * Math.max(13 - remainingCards.size(), 7);
            case BasicArrangement.SETTYPE_3PAIR:
                return waitFor2 ? 30 : 13;
            case BasicArrangement.SETTYPE_FOUR:
                return waitFor2 ? 45 : 26;
            case BasicArrangement.SETTYPE_4PAIR:
                return waitFor2 ? 60 : 13;
            default:
                return (3 - Math.min(this.getNumberOfSameCardSet(remainingCards), this.getNumberOfOpenStraight(remainingCards))) * 13;
        }
    }

    // remainingCards must be sorted.
    private int getNumberOfSameCardSet(List<Card> remainingCards) {
        if (remainingCards == null || remainingCards.isEmpty()) {
            return 0;
        }

        int ret = 1;
        int currentNum = remainingCards.get(0).getCardNumber();
        for (int i = 1; i < remainingCards.size(); i++) {
            if (remainingCards.get(i).getCardNumber() == currentNum) {
                continue;
            }

            ret++;
        }

        return ret;
    }

    // remainingCards must be sorted.
    private int getNumberOfOpenStraight(List<Card> remainingCards) {
        if (remainingCards == null || remainingCards.isEmpty()) {
            return 0;
        }

        int ret = 0;
        boolean has2 = false;
        int j;
        for (j = 0; j < remainingCards.size(); j++) {
            if (BasicArrangement.is2(remainingCards.get(j))) {
                has2 = true;
            } else {
                break;
            }
        }

        if (has2) {
            ret++;
        }

        int set1Count = 1;
        int set1LastNum = remainingCards.get(j).getCardNumber();
        int set2Count = 0;
        int set2LastNum = -1;
        int temp;
        for (int i = j + 1; i < remainingCards.size(); i++) {
            temp = remainingCards.get(i).getCardNumber() - set1LastNum;
            switch (temp) {
                case 0:
                    if (set2LastNum < 0) {
                        set2LastNum = set1LastNum;
                        set2Count = set1Count;
                        set1LastNum = remainingCards.get(i).getCardNumber();
                        set1Count = 1;
                    } else {
                        if (remainingCards.get(i).getCardNumber() - set2LastNum == 1) {
                            set2LastNum = remainingCards.get(i).getCardNumber();
                            set2Count++;
                        } else if (remainingCards.get(i).getCardNumber() - set2LastNum > 1) {
                            if (set2Count >= 3) {
                                ret++;
                            } else {
                                set1Count = set2Count;
                            }

                            set2Count = 0;
                            set2LastNum = -1;
                        }
                    }

                    break;
                case 1:
                    set1LastNum = remainingCards.get(i).getCardNumber();
                    set1Count++;
                    break;
                default:
                    if (set2Count >= 3) {
                        ret++;
                    } else {
                        set1Count = set2Count;
                    }

                    set2Count = 0;
                    set2LastNum = -1;

                    if (set1Count >= 3) {
                        ret++;
                    }

                    set1LastNum = remainingCards.get(i).getCardNumber();
                    set1Count = 1;
                    break;
            }
        }

        if (set2Count >= 3) {
            ret++;
        } else {
            set1Count = set2Count;
        }

        if (set1Count >= 3) {
            ret++;
        }

        return ret;
    }
}
