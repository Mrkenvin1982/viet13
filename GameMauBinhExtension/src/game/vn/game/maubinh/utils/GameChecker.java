/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package game.vn.game.maubinh.utils;

import game.vn.common.card.object.Card;
import game.vn.game.maubinh.MauBinhConfig;
import game.vn.game.maubinh.MauBinhGameController;
import game.vn.game.maubinh.lang.MauBinhLanguage;
import game.vn.game.maubinh.object.Cards;
import game.vn.game.maubinh.object.Player;
import game.vn.game.maubinh.object.Result;
import game.vn.util.Utils;
import java.util.List;

/**
 *
 * @author binhnt
 */
public class GameChecker {

    /**
     * Calculate number of win chi of each player.
     * @param gameController
     * @param players player list.
     * @param result an array of Result objects.
     * @return an array of win chi.
     */
    public static int[] getWinChi(MauBinhGameController gameController,Player[] players, Result[][] result) {
        if (result == null || players == null ||
                players.length < MauBinhConfig.MIN_NUMBER_PLAYER) {
            return null;
        }
        
        int playerNo = players.length;
        int[] winChi = new int[playerNo];
        // Init sum.
        for (int i = 0; i < winChi.length; i++) {
            winChi[i] = 0;
        }
        
        for (int i = 0; i < playerNo; i++) {
            if (players[i].getUser() == null) {
                continue;
            }

            for (int j = i + 1; j < playerNo; j++) {
                if (players[j].getUser() == null) {
                    continue;
                }
                
                // Add sum.
                int temp = result[i][j].getWinChi();
                winChi[i] += temp;
                winChi[j] -= temp;
                //ăn sập hầm
                if (result[i][j].getMultiK() > 1) {
                    //trường hợp user i thắng
                    if (temp > 0) {
                                              
                        players[i].addDecriptionSapHam(Utils.formatedString(
                                MauBinhLanguage.getMessage(MauBinhLanguage.MULTI_KWIN, gameController.getLocaleOfUser(players[j].getUser())),
                                 players[j].getUser().getName(), temp));
                        
                         players[j].addDecriptionSapHam(Utils.formatedString(
                                MauBinhLanguage.getMessage(MauBinhLanguage.MULTI_KLOSE, gameController.getLocaleOfUser(players[i].getUser())),
                                players[i].getUser().getName(), temp));
                        
                    } else {//user i thua
                         players[j].addDecriptionSapHam(Utils.formatedString(
                                MauBinhLanguage.getMessage(MauBinhLanguage.MULTI_KWIN, gameController.getLocaleOfUser(players[i].getUser())),
                                players[i].getUser().getName(), -temp));
                        
                         players[i].addDecriptionSapHam(Utils.formatedString(
                                MauBinhLanguage.getMessage(MauBinhLanguage.MULTI_KLOSE, gameController.getLocaleOfUser(players[j].getUser())),
                                players[j].getUser().getName(), -temp));
                    }
                }
            }
        }
        
        return winChi;
    }
    
    public static int[] getWinChi(MauBinhGameController gameController, Cards[]cards, Result[][] result) {
        if (result == null || cards == null
                || cards.length < MauBinhConfig.MIN_NUMBER_PLAYER) {
            return null;
        }

        int playerNo = cards.length;
        int[] winChi = new int[playerNo];
        // Init sum.
        for (int i = 0; i < winChi.length; i++) {
            winChi[i] = 0;
        }

        for (int i = 0; i < playerNo; i++) {
            if (cards[i] == null) {
                continue;
            }

            for (int j = i + 1; j < playerNo; j++) {
                if (cards[j] == null) {
                    continue;
                }

                // Add sum.
                int temp = result[i][j].getWinChi();
                winChi[i] += temp;
                winChi[j] -= temp;
            }
        }

        return winChi;
    }
    
    /**
     * Compare cards of players.
     * @param players player list.
     * @return an array of Result object.
     */
    public static Result[][] comparePlayers(Player[] players) {
        if (players == null || players.length < MauBinhConfig.MIN_NUMBER_PLAYER) {
            return null;
        }
        
        int playerNo = players.length;
        
        // Count number of win-3-set (an sap ham).
        int[] winThreeSetNo = new int[playerNo];
        for (int i = 0; i < playerNo; i++) {
            // Init counter.
            winThreeSetNo[i] = 0;
        }

        // Compare player with player.
        // And save to result array.
        Result[][] result = new Result[playerNo][playerNo];
        for (int i = 0; i < playerNo; i++) {
            if (players[i].getUser() == null) {
                continue;
            }

            result[i][i] = new Result();
            
            for (int j = i + 1; j < playerNo; j++) {
                if (players[j].getUser() == null) {
                    continue;
                }
            
                // Compare player-i and player-j.
                result[i][j] = GameChecker.comparePlayers(players[i], players[j]);
                result[j][i] = result[i][j].getNegative();
                
                // Increase counter.
                if (result[i][j].isWinThreeSet()) {
                    winThreeSetNo[i] += 1;
                }

                if (result[j][i].isWinThreeSet()) {
                    winThreeSetNo[j] += 1;
                }
            }
        }

        // Check an sap ham 3 nha. Reset multiple koefficient.
        for (int i = 0; i < playerNo; i++) {
            if (winThreeSetNo[i] < MauBinhConfig.DEFAULT_NUMBER_PLAYER - 1) {
                continue;
            }
            
            for (int j = 0; j < playerNo; j++) {
                if (result[i][j] == null || result[j][i] == null) {
                    continue;
                }
                
                result[i][j].setMultiK(MauBinhConfig.getInstance().getChiWinAllByThreeSetRate());
                result[j][i].setMultiK(MauBinhConfig.getInstance().getChiWinAllByThreeSetRate());
            }
        }
        
        return result;
    }
 
    /**
     * Compare cards object
     *
     * @param cards
     * @return an array of Result object.
     */
    public static Result[][] comparePlayers(Cards[] cards) {
        if (cards == null || cards.length < MauBinhConfig.MIN_NUMBER_PLAYER) {
            return null;
        }
        
        int playerNo = cards.length;
        
        // Count number of win-3-set (an sap ham).
        int[] winThreeSetNo = new int[playerNo];
        for (int i = 0; i < playerNo; i++) {
            // Init counter.
            winThreeSetNo[i] = 0;
        }

        // Compare player with player.
        // And save to result array.
        Result[][] result = new Result[playerNo][playerNo];
        for (int i = 0; i < playerNo; i++) {
            if (cards[i] == null) {
                continue;
            }

            result[i][i] = new Result();
            
            for (int j = i + 1; j < playerNo; j++) {
                if (cards[j] == null) {
                    continue;
                }
            
                // Compare player-i and player-j.
                result[i][j] = GameChecker.comparePlayers(cards[i], cards[j]);
                result[j][i] = result[i][j].getNegative();
                
                // Increase counter.
                if (result[i][j].isWinThreeSet()) {
                    winThreeSetNo[i] += 1;
                }

                if (result[j][i].isWinThreeSet()) {
                    winThreeSetNo[j] += 1;
                }
            }
        }

        // Check an sap ham 3 nha. Reset multiple koefficient.
        for (int i = 0; i < playerNo; i++) {
            if (winThreeSetNo[i] < MauBinhConfig.DEFAULT_NUMBER_PLAYER - 1) {
                continue;
            }
            for (int j = 0; j < playerNo; j++) {
                if (result[i][j] == null || result[j][i] == null) {
                    continue;
                }
                result[i][j].setMultiK(MauBinhConfig.getInstance().getChiWinAllByThreeSetRate());
                result[j][i].setMultiK(MauBinhConfig.getInstance().getChiWinAllByThreeSetRate());
            }
        }
        
        return result;
    }
    
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
    
    /**
     * Compare cards of 2 players.
     * @param player01 1st player.
     * @param player02 2nd player.
     * @return a Result object.
     */
    private static Result comparePlayers(Player player01, Player player02) {
        if (player01 == null || player02 == null ||
                player01.getCards() == null || player02.getCards() == null ||
                player01.getCards().isEnoughCard() == false ||
                player02.getCards().isEnoughCard() == false) {
            return null;
        }
        
        return player01.getCards().compareWith(player02.getCards());
    }
    
     /**
     * Compare cards of 2 players.
     * @param player01 1st player.
     * @param player02 2nd player.
     * @return a Result object.
     */
    private static Result comparePlayers(Cards cards1, Cards cards2) {
        if (cards1== null || cards2== null ||
                cards1.isEnoughCard() == false ||
                cards2.isEnoughCard() == false) {
            return null;
        }
        
        return cards1.compareWith(cards2);
    }
    
    public static boolean isFinishAll(Player[] players) {
        if (players == null) {
            return true;
        }
        
        for (int i = 0; i < players.length; i++) {
            if (players[i].getUser() == null) {
                continue;
            }
            
            if (players[i].isFinish() == false) {
                return false;
            }
        }
        
        return true;
    }
}
