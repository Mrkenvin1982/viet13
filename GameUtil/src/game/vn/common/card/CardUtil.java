/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package game.vn.common.card;

import game.vn.common.card.object.Card;
import game.vn.common.card.object.CardSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * class xu ly nhung utilities cho 1 bo bai.
 *
 * @author tuanp
 */
public class CardUtil {

    public static void removeCard(final List<Card> cards, final Card card) {
        for (int i = 0; i < cards.size(); i++) {
            if (cards.get(i).getId() == card.getId()) {
                cards.remove(i);
                break;
            }
        }
    }

    public static boolean isRac(Card[] bai) {
        return bai.length == 1;
    }

    public static boolean isRac(List<Card> bai) {
        return bai.size() == 1;
    }

    /**
     * Hai quân bài có giá trị ngang nhau.
     *
     * @param bai
     * @return
     */
    public static boolean isPair(Card[] bai) {
        return bai.length == 2 && bai[0].getCardNumber() == bai[1].getCardNumber();
    }

    public static boolean isPair(List<Card> bai) {
        return bai.size() == 2 && bai.get(0).getCardNumber() == bai.get(1).getCardNumber();
    }

    /**
     * Hai con heo
     *
     * @param bai
     * @return
     */
    public static boolean isPairHeo(Card[] bai) {
        return bai.length == 2 && bai[0].isHeo() && bai[1].isHeo();
    }

    /**
     * Cặp trong tiến lên miền bắc: hai quân bài có giá trị ngang nhau và cùng
     * màu(đỏ hoặc đen).
     *
     * @param bai
     * @return
     */
    public static boolean isPairMB(Card[] bai) {
        if (bai.length == 2) {
            if (bai[0].getCardNumber() == bai[1].getCardNumber() && isSameColor(bai)) {
                return true;
            }
            if (bai[0].isHeo() && bai[1].isHeo()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Kiểm tra một mảng bài có cùng màu
     *
     * @param card
     * @return
     */
    public static boolean isSameColor(Card[] card) {
        //lấy màu con đầu tiên
        int color = card[0].getColor();
        for (int i = 1; i < card.length; i++) {
            if (color != card[i].getColor()) {
                return false;
            }
        }///end for
        return true;
    }

    /**
     * Kiểm tra nếu bài là quân đỏ
     *
     * @param card
     * @return
     */
    public static boolean isRed(Card card) {
        return card.getId() % 4 > 1;
    }

    /**
     * Kiểm tra nếu bài là quân đen
     *
     * @param card
     * @return
     */
    public static boolean isBlack(Card card) {
        return card.getId() % 4 < 2;
    }

    /**
     * Xét bài đồng chất đồng màu
     *
     * @param card1
     * @param card2
     * @return
     */
    public static boolean isSameTypeAndColor(Card[] card1, Card[] card2) {
        if (card1.length != card2.length) {
            return false;
        }
        for (int i = 0; i < card1.length; i++) {
            if (card1[i].getId() % 4 != card2[i].getId() % 4) {
                return false;
            }
        }
        return true;
    }

    /**
     * Kiểm tra cùng chất: cơ, rô, chuồn, bích
     *
     * @param card
     * @return
     */
    public static boolean isSameType(Card[] card) {
        //lấy chất con đầu tiên
        int type = card[0].getId() % 4;
        for (int i = 1; i < card.length; i++) {
            if (type != card[i].getId() % 4) {
                return false;
            }
        }///end for
        return true;
    }

    public static boolean isSameType(byte[] cardsId) {
        //lấy chất con đầu tiên
        int type = cardsId[0] % 4;
        for (int i = 1; i < cardsId.length; i++) {
            if (type != cardsId[i] % 4) {
                return false;
            }
        }///end for
        return true;
    }

    /**
     * kiểm tra bài cùng chất
     *
     * @param cardsId
     * @return
     */
    public static boolean isSameType(List<Byte> cardsId) {
        Collections.sort(cardsId);
        //lấy chất con đầu tiên
        int type = cardsId.get(0) % 4;
        for (int i = 1; i < cardsId.size(); i++) {
            if (type != cardsId.get(i) % 4) {
                return false;
            }
        }///end for
        return true;
    }

    /**
     * Sám cô: (bộ 3 lá) 3 quân bài có giá trị ngang nhau.
     *
     * @param bai
     * @return
     */
    public static boolean isTriple(Card[] bai) {
        return bai.length == 3 && bai[0].getCardNumber() == bai[1].getCardNumber() && bai[1].getCardNumber() == bai[2].getCardNumber();
    }

    public static boolean isTriple(List<Card> bai) {
        return bai.size() == 3 && bai.get(0).getCardNumber() == bai.get(1).getCardNumber() && bai.get(1).getCardNumber() == bai.get(2).getCardNumber();
    }

    /**
     * Sám cô heo dùng trong tiến lên miền bắc
     *
     * @param bai
     * @return
     */
    public static boolean isTripleHeo(Card[] bai) {
        return bai.length == 3 && bai[0].getCardNumber() == bai[1].getCardNumber() && bai[1].getCardNumber() == bai[2].getCardNumber()
                && bai[0].isHeo();
    }

    public static boolean is3DoiThong(Card[] bai) {
        if (bai.length != 6 || bai[bai.length - 1].getId() >= 48) {
            return false;
        }
        for (int i = 1; i < bai.length; i++) {
            if (i % 2 != 0 && bai[i - 1].getCardNumber() != bai[i].getCardNumber()) {
                return false;
            }
            if (i % 2 == 0 && bai[i - 1].getCardNumber() != bai[i].getCardNumber() - 1) {
                return false;
            }
        }
        return true;
    }

    public static boolean is3DoiThong(List<Card> cards) {

        if (cards.size() != 6 || cards.get(cards.size() - 1).getId() >= 48) {
            return false;
        }
        for (int i = 1; i < cards.size(); i++) {
            if (i % 2 != 0 && cards.get(i - 1).getCardNumber() != cards.get(i).getCardNumber()) {
                return false;
            }
            if (i % 2 == 0 && cards.get(i - 1).getCardNumber() != cards.get(i).getCardNumber() - 1) {
                return false;
            }
        }
        return true;
    }

    public static boolean is3DoiThongMB(Card[] bai) {
        if (bai.length != 6 || bai[bai.length - 1].getId() >= 48) {
            return false;
        }
        for (int i = 1; i < bai.length; i++) {
            if (!isSameColor(new Card[]{bai[i - 1], bai[i]})) {
                return false;
            }
            if (i % 2 != 0 && bai[i - 1].getCardNumber() != bai[i].getCardNumber()) {
                return false;
            }
            if (i % 2 == 0 && bai[i - 1].getCardNumber() != bai[i].getCardNumber() - 1) {
                return false;
            }
        }
        return true;
    }

    public static boolean is3DoiThongMB(List<Card> cards) {

        if (cards.size() != 6 || cards.get(cards.size() - 1).getId() >= 48) {
            return false;
        }
        for (int i = 1; i < cards.size(); i++) {
            if (!isSameColor(new Card[]{cards.get(i - 1), cards.get(i)})) {
                return false;
            }
            if (i % 2 != 0 && cards.get(i - 1).getCardNumber() != cards.get(i).getCardNumber()) {
                return false;
            }
            if (i % 2 == 0 && cards.get(i - 1).getCardNumber() != cards.get(i).getCardNumber() - 1) {
                return false;
            }
        }
        return true;
    }

    public static boolean is4DoiThong(Card[] bai) {
        if (bai.length != 8 || bai[bai.length - 1].getId() >= 48) {
            return false;
        }
        for (int i = 1; i < bai.length; i++) {
            if (i % 2 != 0 && bai[i - 1].getCardNumber() != bai[i].getCardNumber()) {
                return false;
            }
            if (i % 2 == 0 && bai[i - 1].getCardNumber() != bai[i].getCardNumber() - 1) {
                return false;
            }
        }
        return true;
    }

    public static boolean is4DoiThong(List<Card> bai) {
        if (bai.size() != 8 || bai.get(bai.size() - 1).getId() >= 48) {
            return false;
        }
        for (int i = 1; i < bai.size(); i++) {
            if (i % 2 != 0 && bai.get(i - 1).getCardNumber() != bai.get(i).getCardNumber()) {
                return false;
            }
            if (i % 2 == 0 && bai.get(i - 1).getCardNumber() != bai.get(i).getCardNumber() - 1) {
                return false;
            }
        }
        return true;
    }

    public static boolean is4DoiThongMB(Card[] bai) {
        if (bai.length != 8 || bai[bai.length - 1].getId() >= 48) {
            return false;
        }
        for (int i = 1; i < bai.length; i++) {
            if (!isSameColor(new Card[]{bai[i - 1], bai[i]})) {
                return false;
            }
            if (i % 2 != 0 && bai[i - 1].getCardNumber() != bai[i].getCardNumber()) {
                return false;
            }
            if (i % 2 == 0 && bai[i - 1].getCardNumber() != bai[i].getCardNumber() - 1) {
                return false;
            }
        }
        return true;
    }

    /**
     * Tứ quý (bộ 4 lá): 4 quân bài có giá trị ngang nhau
     *
     * @param bai
     * @return
     */
    public static boolean isTuQuy(Card[] bai) {
        return bai.length == 4 && bai[0].getCardNumber() == bai[1].getCardNumber() && bai[1].getCardNumber() == bai[2].getCardNumber() && bai[2].getCardNumber() == bai[3].getCardNumber();
    }

    public static boolean isTuQuy(List<Card> bai) {
        return bai.size() == 4 && bai.get(0).getCardNumber() == bai.get(1).getCardNumber() && bai.get(1).getCardNumber() == bai.get(2).getCardNumber() && bai.get(2).getCardNumber() == bai.get(3).getCardNumber();
    }

    /**
     * Sảnh tiến lên miền Bắc
     *
     * @param bai
     * @return
     */
    public static boolean isStraightMB(Card[] bai) {
        if (bai.length < 3) {
            return false;
        }
        //lấy chất của con đầu tiên
        int type = bai[0].getId() % 4;
        for (int i = 1; i < bai.length; i++) {
            //giá trị ko liên tiếp
            if (bai[i - 1].getCardNumber() != bai[i].getCardNumber() - 1) {
                return false;
            } else if (type != bai[i].getId() % 4) {//ko cùng chất
                return false;
            }
        }
        // sanh khong tan cung bang 2
        return bai[bai.length - 1].getId() / 4 != Card.HEO_NUMBER;
    }

    /**
     * Sảnh tiến lên miền nam
     *
     * @param bai
     * @return
     */
    public static boolean isStraight(Card[] bai) {
        if (bai.length < 3) {
            return false;
        }
        for (int i = 1; i < bai.length; i++) {
            if (bai[i - 1].getCardNumber() != bai[i].getCardNumber() - 1) {
                return false;
            }
        }
        // sanh khong tan cung bang 2
        return bai[bai.length - 1].getId() / 4 != Card.HEO_NUMBER;
    }

    public static boolean isStraight(List<Card> bai) {
        if (bai.size() < 3) {
            return false;
        }
        for (int i = 1; i < bai.size(); i++) {
            if (bai.get(i - 1).getCardNumber() != bai.get(i).getCardNumber() - 1) {
                return false;
            }
        }
        // sanh khong tan cung bang 2
        return bai.get(bai.size() - 1).getId() / 4 != Card.HEO_NUMBER;
    }

    /**
     * kiểm tra list card id có phải phỏm ko dùng cho game phỏm mới
     *
     * @param cardsId
     * @return
     */
    public static boolean isPhomList(List<Byte> cardsId) {
        if (cardsId.size() < 3) {
            return false;
        }

        Collections.sort(cardsId);

        Byte bTemp = -1;
        for (Byte b : cardsId) {
            if (bTemp.equals(b)) {
                return false;
            }
            bTemp = b;
        }
//        for(Byte b : cardsId){
//            System.err.println(b);
//        }
        //xem mảng có cùng chất ko
        //cùng chất thì phải là sảnh
        if (isSameType(cardsId)) {
            for (int i = 1; i < cardsId.size(); i++) {
                if ((cardsId.get(i - 1) / 4 + 1) != ((cardsId.get(i) / 4 + 1) - 1)) {
                    return false;
                }
            }
        } else {//khác chất thì phải là 3 con giống nhau hoặc tứ quý
            for (int i = 1; i < cardsId.size(); i++) {
                if ((cardsId.get(i - 1) / 4 + 1) != (cardsId.get(i) / 4 + 1)) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * kiểm tra mảng cardId dùng trong phỏm mới
     *
     * @param cardsId
     * @return
     */
    public static boolean isPhom(byte[] cardsId) {
        if (cardsId.length < 3) {
            return false;
        }

        Arrays.sort(cardsId);

        Byte bTemp = -1;
        for (Byte b : cardsId) {
            if (bTemp.equals(b)) {
                return false;
            }
            bTemp = b;
        }
        //xem mảng có cùng chất ko
        //cùng chất thì phải là sảnh
        if (isSameType(cardsId)) {
            for (int i = 1; i < cardsId.length; i++) {
                if ((cardsId[i - 1] / 4 + 1) != ((cardsId[i] / 4 + 1) - 1)) {
                    return false;
                }
            }
        } else {//khác chất thì phải là 3 con giống nhau hoặc tứ quý
            for (int i = 1; i < cardsId.length; i++) {
                if ((cardsId[i - 1] / 4 + 1) != (cardsId[i] / 4 + 1)) {
                    return false;
                }
            }
        }
        return true;
    }

    public static int demDoiThong(List<Card> bai) {
        // boolean valid = false;
        int max = 0;
        int count = 0;
        for (int i = 0; i < bai.size() - 1; i++) {
            if (bai.get(i).getId() >= 48) {
                break;
            }
            if (count == 0 && bai.get(i).getId() / 4 == bai.get(i + 1).getId() / 4) {
                count = 1;
                // valid = true;
            } else {
                if (count % 2 != 0) {
                    if (bai.get(i).getId() / 4 == bai.get(i + 1).getId() / 4 - 1) {
                        count++;
                    } else if (bai.get(i).getId() / 4 != bai.get(i + 1).getId() / 4) {
                        // valid = false;
                        if (count > max) {
                            max = count;
                        }
                        count = 0;
                    }
                } else {
                    if (bai.get(i).getId() / 4 == bai.get(i + 1).getId() / 4) {
                        count++;
                    } else {
                        // valid = false;
                        if (count > max) {
                            max = count;
                        }
                        count = 0;
                    }
                }
            }
        }
        if (count > max) {
            max = count;
        }
        return (max + 1) / 2;
    }
    /**
     * Đếm số đôi thông trong game tlmb
     * @param bai
     * @return 
     */
    public static int countDoithongMB(List<Card> bai)
    {
        //đôi thông tối thiểu phải có 3 đôi
        int minPairDoiThong=3;
        //số lượng đôi thông
        int count = 0;
        // pair trước đó
        Card []pairOld = null;
        for (int i = 0; i < bai.size() - 1; i++) {
            if (bai.get(i).getId() >= 48) {
                break;
            }
            //3,4 card giong va lien ke nhau
            if(pairOld!=null &&pairOld[0].getCardNumber()== bai.get(i).getCardNumber())
            {
                continue;
            }
            /**
             * Kiểm tra card
             * i va (i+1) là 1 đôi cùng màu
             */
            if (bai.get(i).getCardNumber() == bai.get(i + 1).getCardNumber()
                    && isSameColor(new Card[]{bai.get(i), bai.get(i + 1)})){
                // chưa có đôi nào
                if (pairOld == null) {
                    pairOld = new Card[]{bai.get(i), bai.get(i + 1)};
                    count++;
                    i += 1;
                    continue;
                }
                //kiểm tra có phải là 2 đôi lien tiếp
                if (isPairCont(pairOld, new Card[]{bai.get(i), bai.get(i + 1)})) {
                    count++;
                    pairOld = new Card[]{bai.get(i), bai.get(i + 1)};
                } else {
                    //đôi thứ 3 không phải là đôi liên tiếp
                    if (count < minPairDoiThong) {
                        count = 1;
                        pairOld = new Card[]{bai.get(i), bai.get(i + 1)};
                    }
                }
               i += 1;
            } else {
                //không có 3 đôi liền kề trở lên thì reset lại 
                if (count<minPairDoiThong) {
                    pairOld=null;
                    count=0;
                }
            }
        }
        return count;
        
    }
    /***
     * Là 2 đôi liền kề nhau
     */
    private static boolean isPairCont(Card[] cardFirst, Card[] cardEnd) {
        if (cardFirst.length != 2 || cardEnd.length != 2) {
            return false;
        }
        //2 đôi nằm liền kề nhau
        return cardEnd[0].getCardNumber()-cardFirst[0].getCardNumber() == 1;
    }
    public static int demDoiThongMB(List<Card> bai) {
        int max = 0;
        int count = 0;
        for (int i = 0; i < bai.size() - 1; i++) {
            if (bai.get(i).getId() >= 48) {
                break;
            }
            if (count == 0 && bai.get(i).getId() / 4 == bai.get(i + 1).getId() / 4) {
                count = 1;
                // valid = true;
            } else {
                if (count % 2 != 0) {
                    if (bai.get(i).getId() / 4 == bai.get(i + 1).getId() / 4 - 1
                            && isSameColor(new Card[]{bai.get(i), bai.get(i + 1)})) {
                        count++;
                    } else if (bai.get(i).getId() / 4 != bai.get(i + 1).getId() / 4
                            && isSameColor(new Card[]{bai.get(i), bai.get(i + 1)})) {
                        // valid = false;
                        if (count > max) {
                            max = count;
                        }
                        count = 0;
                    }
                } else {
                    if (bai.get(i).getId() / 4 == bai.get(i + 1).getId() / 4
                            && isSameColor(new Card[]{bai.get(i), bai.get(i + 1)})) {
                        count++;
                    } else {
                        // valid = false;
                        if (count > max) {
                            max = count;
                        }
                        count = 0;
                    }
                }
            }
        }
        if (count > max) {
            max = count;
        }
        return (max + 1) / 2;
    }

    public static int demSamCoMB(List<Card> bai) {
        int count = 0;
        for (int i = 0; i < bai.size() - 2; i++) {
            if (isTriple(new Card[]{bai.get(i), bai.get(i + 1), bai.get(i + 2)})) {
                count++;
                i = i + 2;
            }
        }
        return count;
    }

    public static int countA(List<Card> cards) {
        int count = 0;
        count = cards.stream().filter((c) -> (c.getCardNumber() == 11)).map((_item) -> 1).reduce(count, Integer::sum);
        return count;
    }

    /**
     * đếm số quân bai
     *
     * @param cards
     * @param cardNumber
     * @return
     */
    public static int countTypeCard(List<Card> cards, int cardNumber) {
        int count = 0;
        count = cards.stream().filter((c) -> (c.getCardNumber() == cardNumber)).map((_item) -> 1).reduce(count, Integer::sum);
        return count;
    }

    public static List<Card> findHigherOneCard(List<Card> cards, Card highestCard) {
        List<Card> returnCards = new ArrayList<>();
        Card lastCard = cards.get(cards.size() - 1);
        if (lastCard.getId() > highestCard.getId()) {
            returnCards.add(lastCard);
            return returnCards;
        }
        // check heo trước, kiếm heo lớn hơn hoặc hàng
        if (highestCard.isHeo()) {
            // trường hợp ko có heo lớn hơn
            // xét hàng
            returnCards = findPairCont(cards, CardSet.getCard((byte) 0), 3);
            // ko có 3,4 đôi thông tìm tứ quý
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
            if (cards.get(i).getId() < highestCard.getId()) {
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
            // ko có đôi heo lớn hơn kiếm tứ quý hoặc 4 đôi thông
            returnCards = findPairCont(cards, CardSet.getCard((byte) 0), 4);
            // ko có 4 đôi thông tìm tứ quý
            if (returnCards.isEmpty()) {
                returnCards = findFourOfAKind(cards, CardSet.getCard((byte) 0));
            }
        }

        return returnCards;
    }

    public static List<Card> findHigherTripple(List<Card> cards, Card highestCard) {
        List<Card> returnCards = new ArrayList<>();
        if (highestCard.isHeo()) {
            return returnCards;
        }
        for (int i = cards.size() - 1; i >= 2; i--) {
            if (cards.get(i).getId() < highestCard.getId()) {
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

    public static List<Card> findHigherStraight(List<Card> cards, Card highestCard, int straightSize) {
        List<Card> returnCards = new ArrayList<>();
        for (int i = cards.size() - 1; i >= straightSize - 1; i--) {
            if (cards.get(i).getId() < highestCard.getId()) {
                break;
            }
            
            if(cards.get(i).isHeo()){
                continue;
            }
            
            for (int lessAmount = 0; lessAmount < straightSize; lessAmount++) {
                int k;
                
                for (k = i - returnCards.size(); k > 0; k--) {
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
            
            if (returnCards.size() == straightSize) {
                break;
            }
        }
        if (returnCards.size() < straightSize) {
            returnCards.clear();
        }
        return returnCards;
    }

    public static List<Card> findPairCont(List<Card> cards, Card highestCard, int noPair) {
        List<Card> returnCards = new ArrayList<>();
        for (int i = cards.size(); --i >= 0;) {
            if (cards.get(i).getId() >= 48) {
                continue;
            }

            for (int lessAmount = 0; lessAmount <= noPair; lessAmount++) {
                // Tìm quân bài cần thiết, nhỏ hơn quân bài lớn nhất
                // Mỗi bậc phải có 2 quân bài
                // Duyệt từ quân bài thứ i trở ngược lại
                byte lessCount = 0; // Bien dem xem co đúng 2 quân bài ở cấp này không
                for (int k = i - returnCards.size(); k > 0; k--) {
                    if (cards.get(k).getCardNumber() == cards.get(i).getCardNumber() - lessAmount) {

                        lessCount++;
                        if (lessCount == 2) // Nếu đã tìm đủ 2 quân bài
                        // thì không cần đếm tiếp
                        {
                            returnCards.add(cards.get(k));
                            returnCards.add(cards.get(k + 1));
                            break;
                        }
                    }
                }
                if (lessCount != 2) // Khong tim thay du 2 con thi thoi
                {
                    returnCards.clear();
                    break;
                }
            }
            // lấy 3 đôi thông lớn hơn trước
            if (returnCards.size() == noPair * 2) {
                if (returnCards.get(returnCards.size() - 1).getId() > highestCard.getId()) {
                    break;
                } else {
                    returnCards.clear();
                }
            }
            // lấy 4 đôi thông (5 đôi thông tới trắng nên không cần xét)
            if (returnCards.size() > noPair * 2) {
                break;
            }
        }
        return returnCards;
    }

    /**
     * kiếm tứ quý lớn hơn tứ quý khác
     * @param cards 
     * @param highestCard: lá bài đại diện cho tứ quý khác
     * @return
     */
    public static List<Card> findFourOfAKind(List<Card> cards, Card highestCard) {
        List<Card> returnCards = new ArrayList<>();
        Collections.sort(cards);
        // tứ quý xì thì khỏi kiếm lớn hơn
        if (highestCard.getCardNumber() == 11) {
            return returnCards;
        }
        for (int i = cards.size() - 1; i > 3; i--) {
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
     * sắp xếp bài theo nước cơ, rô , chuồn ,bích
     *
     * @param cards
     * @return
     */
    public static List<Card> sortCardByType(List<Card> cards) {
        // Check cards.
        if (cards == null || cards.isEmpty()) {
            return null;
        }
        Collections.sort(cards, new ComparatorByCardType());
        return cards;
    }
    /**
     * count heo
     *
     * @param cards
     * @return
     */
    public static int countHeo(List<Card> cards) {
        int countHeo = 0;
        for (int i = cards.size()-4; i < cards.size(); i++) {
            if (cards.get(i).getCardNumber() != 12) {
                continue;
            }
            countHeo++;
        }
        return countHeo;
    }
     /**
     * Kiểm tra bài có tứ quý  trong danh sách bài khi start ván
     * @param bai
     * @return 
     */
    public static boolean haveTuQuyInListCard(List<Card> bai) {
        for (int i = 0; i < bai.size() - 3; i++) {
            if (CardUtil.isTuQuy(new Card[]{bai.get(i), bai.get(i + 1), bai.get(i + 2), bai.get(i + 3)})) {
                return true;
            }
        }
        return false;
    }
        /**
     * Kiểm tra 6 đôi bất kỳ
     *
     * @param cards
     * @return
     */
    public static boolean isSixPairsTLMN(List<Card> cards) {
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

        if (count >= 11) {
            return true;
        }
        return false;
    }
}
