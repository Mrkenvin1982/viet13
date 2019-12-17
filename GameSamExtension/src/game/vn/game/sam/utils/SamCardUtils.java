/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.vn.game.sam.utils;

import game.vn.common.card.CardUtil;
import game.vn.common.card.object.Card;
import game.vn.common.card.object.CardSet;
import game.vn.game.sam.lang.SamLanguage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author tuanp
 */
public class SamCardUtils {

    private static Logger log = LoggerFactory.getLogger(SamCardUtils.class.getName());
    /**
     * Các loại bài trong game Xâm
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
    //2 tứ quý
    public static final int DOUBLE_FOUR_OF_A_KIND = 7;
    /**
     * Các trường hợp tới trắng đang đồng bộ với tlmn de support client
     * vì client tlmn va sâm cùng 1 source
     */
    // Sảnh rồng
    public static final int STRAIGHT_DRAGON = 10;
    // 6 đôi
    public static final int FIVE_PAIRS = 17;
    // 10 lá cùng màu
    public static final int TEN_CARDS_SAME_COLOR = 18;
    // 3 sám cô (bộ ba)
    public static final int THREE_TRIPLES = 19;
    // tứ quý heo
    public static final int FOUR_2 = 13;

    /**
     * Lấy loại bài
     *
     * @param cards
     * @return
     */
    public static int getType(List<Card> cards) {

        Card[] bai = new Card[cards.size()];
        bai = cards.toArray(bai);
        if (CardUtil.isRac(bai)) {
            return ONE_CARD;
        }
        if (CardUtil.isPair(bai)) {
            return PAIR;
        }
        if (CardUtil.isTriple(bai)) {
            return TRIPLE;
        }
        if (isStraight(cards)) {
            return STRAIGHT;
        }
//        if (CardUtil.is3DoiThong(bai)) {
//            return THREE_PAIR_CONT;
//        }
//        if (CardUtil.is4DoiThong(bai)) {
//            return FOUR_PAIR_CONT;
//        }
        
        if(is2TuQuy(bai))
        {
            return DOUBLE_FOUR_OF_A_KIND;
        }
        if (CardUtil.isTuQuy(bai)) {
            return FOUR_OF_A_KIND;
        }
        return NOTYPE;
    }

    private static boolean isStraight(List<Card> cards) {

        if (cards.size() < 3) {
            return false;
        }
        /**
         * xử lý với trường hợp sảnh A 2 3 4 ..., 2 3 4 5 ... </br>
         * chuyển cardNumber của A -> 0 , 2 -> 1, 3-> 2 , ... theo công thức
         * newCardNumber = (oldCardNumber + 2)% 13 </br>
         * kiểm tra trên listCardNumber này có phải sảnh không
         */
        if (cards.get(cards.size() - 1).isHeo()) {
            // list lưu cardNumber để kiểm tra sảnh
            List<Integer> listCardNumber = new ArrayList<>();
            for (int i = 0; i < cards.size(); i++) {
                Card c = cards.get(i);
                if (c != null) {
                    listCardNumber.add((c.getCardNumber() + 2) % 13);
                }
            }
            Collections.sort(listCardNumber);
            for (int i = 1; i < listCardNumber.size(); i++) {
                if (listCardNumber.get(i - 1) != listCardNumber.get(i) - 1) {
                    return false;
                }
            }
        } else {
            for (int i = 1; i < cards.size(); i++) {
                if (cards.get(i - 1).getCardNumber() != cards.get(i).getCardNumber() - 1) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Kiểm tra sảnh rồng
     *
     * @param cards
     * @return
     */
    private static boolean isStraightDragon(List<Card> cards) {
        return isStraight(cards);
    }

    /**
     * Kiểm tra 5 đôi bất kỳ
     *
     * @param cards
     * @return
     */
    private static boolean isFivePairs(List<Card> cards) {
        int count = 0;
        for (int i = 0; i < 9; i++) {
            // kiểm tra 2 quân bài liền kề nhau có bằng nhau
            if (cards.get(i).getCardNumber() == cards.get(i + 1).getCardNumber()) {
                count++;
                i++;
            }
        }

        if (count == 5) {
            return true;
        }
        return false;
    }

    /**
     * Kiểm tra có 3 Xám cô
     *
     * @param cards
     * @return
     */
    private static boolean isThreeTriple(List<Card> cards) {
        int count = 0;
        for (int i = 0; i < 8; i++) {
            // kiểm tra 3 quân bài liền kề nhau có bằng nhau
            if (cards.get(i).getCardNumber() == cards.get(i + 1).getCardNumber()
                    && cards.get(i).getCardNumber() == cards.get(i + 2).getCardNumber()) {
                count++;
                i += 2;
            }
        }

        if (count == 3) {
            return true;
        }
        return false;
    }

    /**
     * Kiểm tra xem toàn bộ bài có cùng màu không
     *
     * @param cards
     * @return
     */
    private static boolean isTenCardsSameColor(List<Card> cards) {
        int count = 0;
        for (int i = 0; i < 9; i++) {
            //kiem tra 2 quan bài liên tiếp có cùng màu không
            if (cards.get(i).isTypeBlack() == cards.get(i + 1).isTypeBlack()) {
                count++;
            } else {
                break;
            }
        }

        if (count == 9) {
            return true;
        }

        return false;
    }

    /**
     * Kiem tra tới trắng
     *
     * @param cards
     * @return
     */
    public static int getTypeForceFinish(List<Card> cards) {
        Collections.sort(cards);
        if (isStraightDragon(cards)) {
            return STRAIGHT_DRAGON;
        }
        if (CardUtil.countTypeCard(cards, Card.HEO_NUMBER) == 4) {
            return FOUR_2;
        }
        if (isFivePairs(cards)) {
            return FIVE_PAIRS;
        }
        if (isThreeTriple(cards)) {
            return THREE_TRIPLES;
        }
        if (isTenCardsSameColor(cards)) {
            return TEN_CARDS_SAME_COLOR;
        }

        return NOTYPE;
    }

    /**
     * Lấy description cách tới trắng
     *
     * @param typeForce
     * @param locale
     * @return
     */
    public static String getTypeForceDescription(int typeForce, Locale locale) {
        String des = "";
        switch (typeForce) {
            case STRAIGHT_DRAGON:
                des = SamLanguage.getMessage(SamLanguage.STRAIGHT_DRAGON, locale);
                break;
            case FIVE_PAIRS:
                des = SamLanguage.getMessage(SamLanguage.FIVE_PAIR, locale);
                break;
            case THREE_TRIPLES:
                des = SamLanguage.getMessage(SamLanguage.THREE_TRIPLES, locale);
                break;
            case TEN_CARDS_SAME_COLOR:
                des = SamLanguage.getMessage(SamLanguage.TEN_CARDS_SAME_COLOR, locale);
                break;
            case FOUR_2:
                des = SamLanguage.getMessage(SamLanguage.FOUR_2, locale);
        }
        return des;
    }

    /**
     * lấy quân bài ở dạng text để ghi log
     *
     * @param card
     * @return
     */
    public static String getStringCard(Card card) {
        String strCard = "";
        switch (card.getCardNumber()) {
            case 0:
                strCard = "3";
                break;
            case 1:
                strCard = "4";
                break;
            case 2:
                strCard = "5";
                break;
            case 3:
                strCard = "6";
                break;
            case 4:
                strCard = "7";
                break;
            case 5:
                strCard = "8";
                break;
            case 6:
                strCard = "9";
                break;
            case 7:
                strCard = "10";
                break;
            case 8:
                strCard = "J";
                break;
            case 9:
                strCard = "Q";
                break;
            case 10:
                strCard = "K";
                break;
            case 11:
                strCard = "A";
                break;
            case 12:
                strCard = "2";
                break;
        }

        switch (card.getCardType()) {
            case 0:
                strCard += " bích";
                break;
            case 1:
                strCard += " chuồn";
                break;
            case 2:
                strCard += " rô";
                break;
            case 3:
                strCard += " cơ";
                break;
        }
        return strCard;
    }

    /**
     * lấy bài ở dạng text để ghi log
     *
     * @param cards
     * @return
     */
    public static String getStringCards(List<Card> cards) {
        String str = "";
        if (cards == null) {
            return str;
        }
        for (Card card : cards) {
            str += getStringCard(card) + " ";
        }
        return str;
    }

    /**
     * Kiem tra bài có lớn hơn hay không
     *
     * @param card1
     * @param card2
     * @return
     */
    public static boolean isHigher(Card card1, Card card2) {
        if (card1.getCardNumber() > card2.getCardNumber()) {
            return true;
        }
        return false;
    }

    public static int countHeo(List<Card> cards) {
        int heo = 0;
        for (Card card : cards) {
            if (card.isHeo()) {
                heo++;
            }
        }
        return heo;
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
            int typeCard = getType(cards);
            switch (typeCard) {
                case ONE_CARD:
                    returnCards = findHigherOneCard(userCards, cards.get(0));
                    break;
                case PAIR:
                    returnCards = findHigherPair(userCards, cards.get(cards.size() - 1));
                    break;
                case TRIPLE:
                    returnCards = findHigherTripple(userCards, cards.get(cards.size() - 1));
                    break;
                case STRAIGHT:
                    if (cards.size() < 3) {
                        return returnCards;
                    }
                    // kiêm con lớn nhất trong sảnh
                    Card highestCard;
                    // kiếm trong sảnh có heo
                    if (cards.get(cards.size() - 1).isHeo()) {
                        // sảnh có xì thì con cao nhất là con thứ 3 từ phải qua trái
                        if (cards.get(cards.size() - 2).getCardNumber() == 11) {                            
                            highestCard = cards.get(cards.size() - 3);
                        } else {
                            highestCard = cards.get(cards.size() - 2);
                        }
                    } else {
                        highestCard = cards.get(cards.size() - 1);
                    }
                    returnCards = findHigherStraight(userCards, highestCard, cards.size());
                    break;

                case FOUR_OF_A_KIND:
                    returnCards = findFourOfAKind(userCards, cards.get(cards.size() - 1));
                    break;                           
            }
        } catch (Exception e) {
            log.error("findHigherInUserCards error:", e);
        }
        return returnCards;
    }

    public static List<Card> findHigherStraight(List<Card> cards, Card highestCard, int straightSize) {
        log.debug("begin findHigherStraight >>> highestcard =" + getStringCard(highestCard) + " size =" + straightSize);
        List<Card> returnCards = new ArrayList<>();
        for (int i = cards.size() - 1; i >= straightSize - 1; i--) {
            if (cards.get(i).getCardNumber() <= highestCard.getCardNumber()) {
                break;
            }
            for (int lessAmount = 0; lessAmount < straightSize; lessAmount++) {
                int k;
                // xet sảnh từ con thứ i đến con thứ k
                for (k = i - returnCards.size(); k >= 0; k--) {
                    if (cards.get(k).getCardNumber() < cards.get(i).getCardNumber() - lessAmount) {
                        break;
                    }
                    if (cards.get(k).getCardNumber() == cards.get(i).getCardNumber() - lessAmount) {
                        returnCards.add(cards.get(k));
                        break;
                    }
                }
                if (cards.get(k).getCardNumber() < cards.get(i).getCardNumber() - lessAmount) {
                    returnCards.clear();
                    break;
                }
            }
            Collections.sort(returnCards);
            if (returnCards.size() == straightSize && isStraight(returnCards)) {
                log.debug("find higher straight =" + getStringCards(returnCards));
                break;
            }else {
                returnCards.clear();
            }
        }
        if (returnCards.size() < straightSize) {
            returnCards.clear();
        }
        
        return returnCards;
    }

    public static List<Card> findHigherOneCard(List<Card> cards, Card highestCard) {
        List<Card> returnCards = new ArrayList<>();
        Card lastCard = cards.get(cards.size() - 1);
        if (lastCard.getCardNumber() > highestCard.getCardNumber()) {
            returnCards.add(lastCard);
            return returnCards;
        }
        // check heo trước, kiếm heo lớn hơn hoặc hàng
        if (highestCard.isHeo()) {
            // tìm tứ quý
            if (returnCards.isEmpty()) {
                returnCards = findFourOfAKind(cards, CardSet.getCard((byte) 0));
            }
        }
        return returnCards;
    }

    public static List<Card> findHigherPair(List<Card> cards, Card highestCard) {
        List<Card> returnCards = new ArrayList<>();
        // kiếm đôi lớn hơn
        for (int i = cards.size() - 1; i > 0; i--) {
            if (cards.get(i).getCardNumber() <= highestCard.getCardNumber()) {
                break;
            }
            if (cards.get(i).getCardNumber() == cards.get(i - 1).getCardNumber()) {
                returnCards.add(cards.get(i));
                returnCards.add(cards.get(i - 1));
                // kiếm được đôi lớn hơn thì không cần kiếm nữa
                break;
            }
        }
        if (!returnCards.isEmpty()) {
            return returnCards;
        }
        if (highestCard.isHeo()) {
            // tìm 2 tứ quý
            returnCards = findDoubleFourOfAKind(cards);
        }

        return returnCards;
    }

    public static List<Card> findHigherTripple(List<Card> cards, Card highestCard) {
        List<Card> returnCards = new ArrayList<>();
        if (highestCard.isHeo()) {
            return returnCards;
        }
        for (int i = cards.size() - 1; i >= 2; i--) {
            if (cards.get(i).getCardNumber() < highestCard.getCardNumber()) {
                break;
            }
            if (cards.get(i).getCardNumber() == cards.get(i - 1).getCardNumber() && cards.get(i - 1).getCardNumber() == cards.get(i - 2).getCardNumber()) {
                returnCards.add(cards.get(i));
                returnCards.add(cards.get(i - 1));
                returnCards.add(cards.get(i - 2));
                // kiếm được đôi lớn hơn thì không cần kiếm nữa
                break;
            }
        }
        return returnCards;
    }
    
    /**
     * Kiểm tra trường hợp bài là 2 tứ quý
     * @param cards
     * @return 
     */
    public static boolean is2TuQuy(Card[] cards) {
        if (cards.length == 8) {
            Card[] part1 = new Card[4];
            Card[] part2 = new Card[4];
            System.arraycopy(cards, 0, part1, 0, part1.length);
            System.arraycopy(cards, part1.length, part2, 0, part1.length);
            if (CardUtil.isTuQuy(part1) && CardUtil.isTuQuy(part2)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Tìm ra người đền là người có 2 tứ quý
     * @param cards
     * @return 
     */
    public static List<Card> findDoubleFourOfAKind(List<Card> cards) {
        List<Card> returnCards = new ArrayList<>();
        Collections.sort(cards);

        for (int i = cards.size() - 1; i >= 3; i--) {
            if (cards.get(i).getCardNumber() == cards.get(i - 1).getCardNumber() && 
                    cards.get(i - 1).getCardNumber() == cards.get(i - 2).getCardNumber() && 
                    cards.get(i - 2).getCardNumber() == cards.get(i - 3).getCardNumber()) {
                returnCards.add(cards.get(i));
                returnCards.add(cards.get(i - 1));
                returnCards.add(cards.get(i - 2));
                returnCards.add(cards.get(i - 3));
            }
        }
        if (returnCards.size() < 8) {
            returnCards.clear();
        }

        return returnCards;
    }
    
    /**
     * Tìm ra người đền là người có 1 tứ quý
     * @param cards
     * @param highestCard
     * @return 
     */
    public static List<Card> findFourOfAKind(List<Card> cards, Card highestCard) {
        List<Card> returnCards = new ArrayList<>();
        Collections.sort(cards);
        // tứ quý xì thì khỏi kiếm lớn hơn
        if (highestCard.getCardNumber() == 11) {
            return returnCards;
        }
        for (int i = cards.size() - 1; i >= 3; i--) {
            if (cards.get(i).getCardNumber() < highestCard.getCardNumber()) {
                break;
            }
            if (cards.get(i).getCardNumber() == cards.get(i - 1).getCardNumber() && cards.get(i - 1).getCardNumber() == cards.get(i - 2).getCardNumber() && cards.get(i - 2).getCardNumber() == cards.get(i - 3).getCardNumber()) {
                returnCards.add(cards.get(i));
                returnCards.add(cards.get(i - 1));
                returnCards.add(cards.get(i - 2));
                returnCards.add(cards.get(i - 3));
                break;
            }
        }
        return returnCards;
    }
    /**
     * Kiểm tra số tứ quý user có
     * @param cards
     * @return 
     */
    public static int countFourOfAKind(List<Card> cards)
    {
        int count=0;
        Collections.sort(cards);
        for (int i = cards.size() - 1; i >= 3; i--) {
            if (cards.get(i).getCardNumber() == cards.get(i - 1).getCardNumber() && cards.get(i - 1).getCardNumber() == cards.get(i - 2).getCardNumber() && cards.get(i - 2).getCardNumber() == cards.get(i - 3).getCardNumber()) {
                count++;
                break;
            }
        }
        return count;
    }
}
