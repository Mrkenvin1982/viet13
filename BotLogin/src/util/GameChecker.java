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
public class GameChecker {
        
    /**
     * Compare 2 sorted card lists.
     * @param cardList01 1st card list.
     * @param cardList02 2nd card list.
     * @return 1 if 1st card list is bigger, -1 if 2nd card list is bigger, 0 if they are equal.
     */
    public static int compareCardByCard(List<Card> cardList01, List<Card> cardList02) {
        if (cardList01 == null || cardList02 == null) {
            return MauBinhConfig.RESULT_ERROR;
        }
        
        int length01 = cardList01.size();
        int length02 = cardList02.size();
        int cardNo = Math.min(length01, length02);
        int number01;
        int number02;
        for (int i = 0; i < cardNo; i++) {
            number01 = cardList01.get(length01 - i - 1).getCardNumber();
            number02 = cardList02.get(length02 - i - 1).getCardNumber();
            // Win.
            if (number01 > number02) {
                return MauBinhConfig.RESULT_WIN;
            } else if (number01 < number02) { // Lose.
                return MauBinhConfig.RESULT_LOSE;
            }
            
            // Draw then compare next card.
        }

        // Draw.
        return MauBinhConfig.RESULT_DRAW;
    }
    
    /**
     * Compare 2 highest cards of specified sorted lists.
     * @param cardList01 1st card list.
     * @param cardList02 2nd card list.
     * @return 1 if 1st card list is bigger, -1 if 2nd card list is bigger, 0 if they are equal.
     */
    public static int compareHighestCard(List<Card> cardList01, List<Card> cardList02) {
        if (cardList01 == null || cardList02 == null) {
            return MauBinhConfig.RESULT_ERROR;
        }

        if (cardList01.isEmpty() || cardList02.isEmpty()) {
            return MauBinhConfig.RESULT_DRAW;
        }
        
        // Get highest cards.
        int number01 = cardList01.get(cardList01.size() - 1).getCardNumber();
        int number02 = cardList02.get(cardList02.size() - 1).getCardNumber();
        // Win.
        if (number01 > number02) {
            return MauBinhConfig.RESULT_WIN;
        } else if (number01 < number02) { // Lose.
            return MauBinhConfig.RESULT_LOSE;
        } else { // Draw.
            return MauBinhConfig.RESULT_DRAW;
        }
    }
    
    /**
     * Compare 2 highest cards of specified sorted lists.
     * @param cardList01 1st card list.
     * @param cardList02 2nd card list.
     * @return 1 if 1st card list is bigger, -1 if 2nd card list is bigger, 0 if they are equal.
     */
    public static int compare2HighestCards(List<Card> cardList01, List<Card> cardList02) {
        if (cardList01 == null || cardList02 == null) {
            return MauBinhConfig.RESULT_ERROR;
        }

        if (cardList01.size() < 2 || cardList02.size() < 2) {
            return compareHighestCard(cardList01, cardList02);
        }
        
        // Get highest cards.
        int number01 = cardList01.get(cardList01.size() - 1).getCardNumber();
        int number02 = cardList02.get(cardList02.size() - 1).getCardNumber();
        // Win.
        if (number01 > number02) {
            return MauBinhConfig.RESULT_WIN;
        } else if (number01 < number02) { // Lose.
            return MauBinhConfig.RESULT_LOSE;
        }
        
        // Draw then get next highest cards.
        number01 = cardList01.get(cardList01.size() - 2).getCardNumber();
        number02 = cardList02.get(cardList02.size() - 2).getCardNumber();
        // Win.
        if (number01 > number02) {
            return MauBinhConfig.RESULT_WIN;
        } else if (number01 < number02) { // Lose.
            return MauBinhConfig.RESULT_LOSE;
        } else { // Draw.
            return MauBinhConfig.RESULT_DRAW;
        }
    }
}
