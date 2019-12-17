/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package game.vn.game.tienlen.utils;

import game.vn.common.card.BotCards;
import game.vn.common.card.CardUtil;
import game.vn.common.card.object.Card;
import game.vn.common.card.object.CardSet;
import game.vn.game.tienlen.language.TienLenLanguage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 *
 * @author hoanghh
 */
public class TienLenCardUtils {

    
    public static final BotCardsomparator BOT_CARDS_SORTED_DESC = new BotCardsomparator();
    /**
     * Các loại bài trong tiến lên
     */
    public static final int NOTYPE = -1;
    // rác
    public static final int ONE_CARD = 0;
    // sảnh.
    public static final int STRAIGHT = 1;
    // đôi.
    public static final int PAIR = 2;
    //3 con.
    public static final int TRIPLE = 3;
    //3 đôi thông.
    public static final int THREE_PAIR_CONT = 4;
    //4 đôi thông.
    public static final int FOUR_PAIR_CONT = 5;
    //tứ quý.
    public static final int FOUR_OF_A_KIND = 6;
    /**
     * các trường hợp ăn trắng
     */
    // Sảnh rồng
    public static final int STRAIGHT_DRAGON = 10;
    // 6 đôi
    public static final int SIX_PAIRS = 11;
    // 5 đôi thông
    public static final int FIVE_PAIRS_CONT = 12;
    // tứ quý heo
    public static final int FOUR_HEO = 13;
    // tứ quý 3 ở ván đầu tiên
    public static final int FOUR_THREECARDS = 14;
    // 4 sám cô (bộ ba)
    public static final int FOUR_TRIPLES = 15;
    // 3 đôi thông có 3 bích ở ván đầu tiên
    public static final int THREE_PAIRS_CONT_WITH_3SPADE = 16;

    /**
     * lấy loại bài
     *
     * @param bai
     * @return
     */
    public static int getType(Card[] bai) {

        if (CardUtil.isRac(bai)) {
            return ONE_CARD;
        }
        if (CardUtil.isPair(bai)) {
            return PAIR;
        }
        if (CardUtil.isTriple(bai)) {
            return TRIPLE;
        }
        if (CardUtil.isStraight(bai)) {
            return STRAIGHT;
        }
        if (CardUtil.is3DoiThong(bai)) {
            return THREE_PAIR_CONT;
        }
        if (CardUtil.is4DoiThong(bai)) {
            return FOUR_PAIR_CONT;
        }
        if (CardUtil.isTuQuy(bai)) {
            return FOUR_OF_A_KIND;
        }
        return NOTYPE;
    }

    /**
     * kiểm tra sảnh rồng
     *
     * @param cards
     * @return 
     */
    public static boolean isStraightDragon(List<Card> cards) {
        int count = 0;
        for (int i = 0; i < 12; i++) {
            // 2 con bài gần nhau bằng nhau thì xét con tiếp theo
            if (cards.get(i).getCardNumber() == cards.get(i + 1).getCardNumber()) {
                continue;
            }

            if (cards.get(i).getCardNumber() != cards.get(i + 1).getCardNumber() - 1) {
                break;
            }
            // kiểm tra con bài tiếp theo có phải heo
            if (cards.get(i + 1).getCardNumber() == 12) {
                break;
            }
            count++;
        }

        return count >= 11;
    }

    /**
     * kiểm tra 5 đôi thông
     *
     * @param cards
     * @return
     */
    private static boolean isFivePairsContinuous(List<Card> cards) {
        return CardUtil.demDoiThong(cards) >= 5;
    }

    /**
     * Kiểm tra 6 đôi bất kỳ
     *
     * @param cards
     * @return
     */
    public static boolean isSixPairs(List<Card> cards) {
        int count = 0;
        for (int i = 0; i < 12; i++) {
            // kiểm tra 2 quân bài liền kề nhau có bằng nhau
            if (cards.get(i).getCardNumber() == cards.get(i + 1).getCardNumber()) {
                count++;
                i++;
            }
        }

        return count == 6;
    }

    /**
     * Kiểm tra có 4 heo
     *
     * @param cards
     * @return
     */
    public static boolean isFourHeo(List<Card> cards) {
        int countHeo = 0;
        for (int i = 9; i < 13; i++) {
            if (cards.get(i).getCardNumber() != 12) {
                break;
            }
            countHeo++;
        }
        return countHeo == 4;
    }

    /**
     * Kiểm tra có tứ quý 3
     *
     * @param cards
     * @return
     */
    public static boolean isFourBa(List<Card> cards) {
        int count3 = 0;
        for (int i = 0; i < 4; i++) {
            if (cards.get(i).getCardNumber() != 0) {
                break;
            }
            count3++;
        }
        return count3 == 4;
    }

    /**
     * kiểm tra có 3 đôi thông trong đó có 3 bích lúc bắt đầu không
     *
     * @param cards
     * @return
     */
    public static boolean is3PairsContAtBegin(List<Card> cards) {
        if (!cards.get(0).is3Bich()) {
            return false;
        }
        int count3 = CardUtil.countTypeCard(cards, 0);
        if (count3 < 2) {
            return false;
        }
        // cardNumber con 4 là 1
        int count4 = CardUtil.countTypeCard(cards, 1);
        if (count4 < 2) {
            return false;
        }
        // cardNumber con 5 là 2
        int count5 = CardUtil.countTypeCard(cards, 2);
        return count5 >= 2;
    }

    /**
     * Kiểm tra có tứ quý A
     *
     * @param cards
     * @return
     */
    public static boolean isFourA(List<Card> cards) {
        int countA = 0;
        for (int i = 0; i < cards.size(); i++) {
            if (cards.get(i).getCardNumber() == 11) {
                countA++;
            }
        }
        return countA == 4;
    }

    /**
     * Kiểm tra có 4 sám cô (bộ 3)
     *
     * @param cards
     * @return
     */
    private static boolean isFourTriple(List<Card> cards) {
        int count = 0;
        for (int i = 0; i < 11; i++) {
            // kiểm tra 3 quân bài liền kề nhau có bằng nhau
            if (cards.get(i).getCardNumber() == cards.get(i + 1).getCardNumber()
                    && cards.get(i).getCardNumber() == cards.get(i + 2).getCardNumber()) {
                count++;
                i += 2;
            }
        }

        return count == 4;
    }

    /**
     * Kiểm tra các trường hợp tới trắng trường hợp bắt đầu game và có tứ quý 3
     * tính riêng
     *
     * @param cards
     * @param isNewGame
     * @return
     */
    public static int getTypeForceFinish(List<Card> cards ,boolean isNewGame) {
        Collections.sort(cards);
         /**
         * là ván đầu tiên thì mới xet là có 3 bích
         */
        if (isNewGame) {
            if (is3PairsContAtBegin(cards)) {
                return THREE_PAIRS_CONT_WITH_3SPADE;
            }
            if (isFourBa(cards)) {
                return FOUR_THREECARDS;
            }
        }
        if (CardUtil.isStraightDragon(cards)) {
            return STRAIGHT_DRAGON;
        }
        if (CardUtil.isSixPairsTLMN(cards)) {
            return SIX_PAIRS;
        }
        if (isFivePairsContinuous(cards)) {
            return FIVE_PAIRS_CONT;
        }
        if (isFourTriple(cards)) {
            return FOUR_TRIPLES;
        }
        if (isFourHeo(cards)) {
            return FOUR_HEO;
        }
       
        return NOTYPE;
    }

    public static String getTypeForceDescription(int typeForce, Locale locale) {
        String des = "";
        switch (typeForce) {
            case STRAIGHT_DRAGON:
                des = TienLenLanguage.getMessage(TienLenLanguage.STRAIGHT_DRAGON, locale);
                break;
            case SIX_PAIRS:
                des = TienLenLanguage.getMessage(TienLenLanguage.SIX_PAIR, locale);
                break;
            case FIVE_PAIRS_CONT:
                des = TienLenLanguage.getMessage(TienLenLanguage.FIVE_PAIRS_SEQ, locale);
                break;
            case FOUR_HEO:
                des = TienLenLanguage.getMessage(TienLenLanguage.FOUR_TWOCARDS, locale);
                break;
            case FOUR_TRIPLES:
                des = TienLenLanguage.getMessage(TienLenLanguage.FOUR_TRIPLES, locale);
                break;
        }
        return des;
    }

    /**
     * lấy quân bài ở dạng text để ghi log
     *
     * @param card
     * @return
     */
    private static String getStringCard(Card card) {
        switch (card.getCardNumber()) {
            case 0:
                return "3";
            case 1:
                return "4";
            case 2:
                return "5";
            case 3:
                return "6";
            case 4:
                return "7";
            case 5:
                return "8";
            case 6:
                return "9";
            case 7:
                return "10";
            case 8:
                return "J";
            case 9:
                return "Q";
            case 10:
                return "K";
            case 11:
                return "A";
            case 12:
                return "2";
        }

        return "";
    }

    private static String getCardTypeString(Card card) {
        switch (card.getCardType()) {
            case 0:
                return " bích";
            case 1:
                return " chuồn";
            case 2:
                return " rô";
            case 3:
                return " cơ";
        }
        return "";
    }

    /**
     * lấy bài ở dạng text để ghi log
     *
     * @param cards
     * @return
     */
    public static String getStringCards(Card[] cards) {
        if (cards == null || cards.length == 0){
            return "";
        }
        StringBuilder str = new StringBuilder();
        for (Card card : cards) {
            str.append(getStringCard(card)).append(getCardTypeString(card)).append(" ");
        }
        return str.toString();
    }
    
    /**
     * lấy bài ở dạng text để ghi log
     *
     * @param cards
     * @return
     */
    public static String getStringCards(List<Card> cards) {
        StringBuilder str = new StringBuilder();
        cards.stream().forEach((card) -> {
            str.append(getStringCard(card)).append(getCardTypeString(card)).append(" ");
        });
        return str.toString();
    }

    /**
     * tìm quân bài, nhóm bài lớn hơn bài đã đánh ra
     *
     * @param userCards : bài của người chơi
     * @param cards : bài đã đánh ra
     * @return
     */
    public static List<Card> findHigherInUserCards(List<Card> userCards, List<Card> cards) {
        List<Card> returnCards = new ArrayList<>();

        try {
            int typeCard = getType(cards.toArray(new Card[cards.size()]));
            switch (typeCard) {
                case ONE_CARD:
                    returnCards = CardUtil.findHigherOneCard(userCards, cards.get(0));
                    break;
                case PAIR:
                    returnCards = CardUtil.findHigherPair(userCards, cards.get(cards.size() - 1));
                    break;
                case TRIPLE:
                    returnCards = CardUtil.findHigherTripple(userCards, cards.get(cards.size() - 1));
                    break;
                case STRAIGHT:
                    returnCards = CardUtil.findHigherStraight(userCards, cards.get(cards.size() - 1), cards.size());
                    break;
                case THREE_PAIR_CONT:
                    returnCards = CardUtil.findPairCont(userCards, cards.get(cards.size() - 1), 3);
                    break;
                case FOUR_PAIR_CONT:
                    returnCards = CardUtil.findPairCont(userCards, cards.get(cards.size() - 1), 4);
                    break;
                case FOUR_OF_A_KIND:
                    returnCards = CardUtil.findFourOfAKind(userCards, cards.get(cards.size() - 1));
                    if (returnCards.isEmpty()) {
                        returnCards = CardUtil.findPairCont(userCards, CardSet.getCard((byte) 0), 4);
                    }
                    break;
            }
        } catch (Exception e) {
            
        }
        return returnCards;
    }

    /**
     * Kiểm tra bài có 3 đôi thông
     * @param bai
     * @return 
     */
    public static boolean is3DoiThong(List<Card> bai) {
        for (int i = 0; i < bai.size() - 6; i++) {
            List<Card> cards = bai.subList(i, i + 6);
            if (is3DoiThong(cards)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Kiểm tra bài có 4 đôi thông
     * @param bai
     * @return 
     */
    public static boolean is4DoiThong(List<Card> bai) {
        for (int i = 0; i < bai.size() - 8; i++) {
            List<Card> cards = bai.subList(i, i + 8);
            if (is4DoiThong(cards)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Kiểm tra bài có 5 đôi thông
     * @param bai
     * @return 
     */
    public static boolean is5DoiThong(List<Card> bai) {
        for (int i = 0; i < bai.size() - 10; i++) {
            List<Card> cards = bai.subList(i, i + 10);
            if (is5DoiThong(cards)) {
                return true;
            }
        }
        return false;
    }
}
