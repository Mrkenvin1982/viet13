/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package game.vn.game.maubinh.object;

import game.vn.common.card.object.Card;
import game.vn.common.card.object.CardSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 *
 * @author hoanghh
 */
public class DeckTest {
    public static final int MAUBINH_FULL_HOUSE=1;
    public static final int MAUBINH_3_XI=2;
    public static final int MAUBINH_4_OF_A_KIND=3;
    public static final int MAUBINH_4XI=4;
    public static final int MAUBINH_FLUSH=5;
    public static final int MAUBINH_STRAIGHT_FLUSH=6;
    public static final int MAUBINH_BIG_STRAIGHT_FLUSH=7;
    public static final int MAUBINH_SMALL_STRAIGHT_FLUSH=8;
    public static final int MAUBINH_6_PAIR=9;
    public static final int MAUBINH_13STRAIGHT=10;
    public static final int FOUR_OF_THREE=11;
    public static final int MAUBINH_13_SAME_HEART=24;
    public static final int MAUBINH_13_SAME_DIAMOND=26;
    public static final int MAUBINH_13_SAME_CLUB=27;
    public static final int MAUBINH_13_SAME_SPADE=28;
    public static final int MAUBINH_3STRAIGHT=29;
    public static final int MAUBINH_BIG_STRAIGHT_FLUSH_HEART=30;
    public static final int MAUBINH_SMAILL_STRAIGHT_FLUSH_HEART=31;
    public static final int MAUBINH_6_PAIR_2=12;
    public static final int MAUBINH_6_PAIR_3=13;
    public static final int MAUBINH_3_FLUSH=32;
    private static final int DEFAULT_CARD_NUMBER = 13;

    
    private final transient CardSet cardSet;
    private final List<Card> cardUsed;
    public DeckTest() {
        cardSet = new CardSet();
        cardUsed = new ArrayList<>();
    }
    
    public void reset(){
        cardSet.xaoBai();
        cardUsed.clear();
    }
    
    public Card dealCard(int number, int type){
        Card c = cardSet.dealCard(number, type);
        cardUsed.add(c);
        return c;
    }
    public Card dealCard(){
        return cardSet.dealCard();
    }
    
    
    public void addFullCard(List<Card> cards) {
        while (cards.size() < DEFAULT_CARD_NUMBER) {
            Card c = dealCard();
            if (!cardUsed.contains(c)) {
                cards.add(c);
                cardUsed.add(c);
            }
        }
        Collections.sort(cards);
    }

    /***
     * Cù lũ
     * @return 
     */
    private List<Card> getFullHouseCards() {
        List<Card> cards = new ArrayList<>();
        cards.add(dealCard(0, 1));//2
        cards.add(dealCard(0, 2));
        cards.add(dealCard(1, 0));//3
        cards.add(dealCard(1, 1));
        cards.add(dealCard(1, 2));
        return cards;
    }
    /**
     * Đồng hoa 13 lá bích
     * @return 
     */
    private List<Card> get13SameClubCards() {
        List<Card> cards = new ArrayList<>();
        cards.add(dealCard(0, 0));//2
        cards.add(dealCard(1, 0));
        cards.add(dealCard(2, 0));//3
        cards.add(dealCard(3, 0));
        cards.add(dealCard(4, 0));
        cards.add(dealCard(5, 0));
        cards.add(dealCard(6, 0));
        cards.add(dealCard(7, 0));
        cards.add(dealCard(8, 0));
        cards.add(dealCard(9, 0));
        cards.add(dealCard(10, 0));
        cards.add(dealCard(11, 0));
        cards.add(dealCard(12, 0));
        return cards;
    }
    /**
     * Đồng hoa 13 lá chuồn
     * @return 
     */
    private List<Card> get13SameSpadeCards() {
        List<Card> cards = new ArrayList<>();
        cards.add(dealCard(0, 1));//2
        cards.add(dealCard(1, 1));
        cards.add(dealCard(2, 1));//3
        cards.add(dealCard(3, 1));
        cards.add(dealCard(4, 1));
        cards.add(dealCard(5, 1));
        cards.add(dealCard(6, 1));
        cards.add(dealCard(7, 1));
        cards.add(dealCard(8, 1));
        cards.add(dealCard(9, 1));
        cards.add(dealCard(10, 1));
        cards.add(dealCard(11, 1));
        cards.add(dealCard(12, 1));
        return cards;
    }
    /**
     * Đồng hoa 13 lá rô
     * @return 
     */
    private List<Card> get13SameDiamonCards() {
        List<Card> cards = new ArrayList<>();
        cards.add(dealCard(0, 2));//2
        cards.add(dealCard(1, 2));
        cards.add(dealCard(2, 2));//3
        cards.add(dealCard(3, 2));
        cards.add(dealCard(4, 2));
        cards.add(dealCard(5, 2));
        cards.add(dealCard(6, 2));
        cards.add(dealCard(7, 2));
        cards.add(dealCard(8, 2));
        cards.add(dealCard(9, 2));
        cards.add(dealCard(10, 2));
        cards.add(dealCard(11, 2));
        cards.add(dealCard(12, 2));
        return cards;
    }
    /**
     * Đồng hoa 13 lá rô
     * @return 
     */
    private List<Card> get13SameHeartCards() {
        List<Card> cards = new ArrayList<>();
        cards.add(dealCard(0, 3));//2
        cards.add(dealCard(1, 3));
        cards.add(dealCard(2, 3));//3
        cards.add(dealCard(3, 3));
        cards.add(dealCard(4, 3));
        cards.add(dealCard(5, 3));
        cards.add(dealCard(6, 3));
        cards.add(dealCard(7, 3));
        cards.add(dealCard(8, 3));
        cards.add(dealCard(9, 3));
        cards.add(dealCard(10, 3));
        cards.add(dealCard(11, 3));
        cards.add(dealCard(12, 3));
        return cards;
    }
    /**
     * 3 sảnh
     * @return 
     */
    private List<Card> get13StraightCards() {
        List<Card> cards = new ArrayList<>();
        cards.add(dealCard(0, 3));//2
        cards.add(dealCard(1, 2));
        cards.add(dealCard(2, 3));//3
        cards.add(dealCard(3, 2));
        cards.add(dealCard(4, 1));
        cards.add(dealCard(5, 3));
        cards.add(dealCard(6, 0));
        cards.add(dealCard(7, 3));
        cards.add(dealCard(8, 1));
        cards.add(dealCard(9, 2));
        cards.add(dealCard(9, 3));
        cards.add(dealCard(10, 2));
        cards.add(dealCard(11, 3));
        
//         cards.add(dealCard(0, 3));//2
//        cards.add(dealCard(1, 3));
//        cards.add(dealCard(2, 3));//3
//        cards.add(dealCard(3, 3));
//        cards.add(dealCard(4, 0));
//        cards.add(dealCard(6, 3));//7 cơ
//        cards.add(dealCard(7, 1));
//        cards.add(dealCard(9, 0));
//        cards.add(dealCard(10, 2));
//        cards.add(dealCard(11, 2));
//        cards.add(dealCard(11, 3));
//        cards.add(dealCard(12, 1));
//        cards.add(dealCard(12, 3));
        return cards;
    }
    
     private List<Card> get3FlushCards() {
        List<Card> cards = new ArrayList<>();
        cards.add(dealCard(0, 0));//2
        cards.add(dealCard(1, 0));
        cards.add(dealCard(2, 0));//3
        cards.add(dealCard(3, 0));
        cards.add(dealCard(5, 0));
        cards.add(dealCard(5, 1));
        cards.add(dealCard(6, 1));
        cards.add(dealCard(7, 1));
        cards.add(dealCard(8, 1));
        cards.add(dealCard(10, 1));
        cards.add(dealCard(9, 2));
        cards.add(dealCard(10, 2));
        cards.add(dealCard(11, 2));
        
        return cards;
     }
     /**
     * 3 xì
     * @return 
     */
    private List<Card> get3ACards() {
        List<Card> cards = new ArrayList<>();
        cards.add(dealCard(12, 0));
        cards.add(dealCard(12, 1));
        cards.add(dealCard(12, 2));
        return cards;
    }
     /**
     * 4 xì
     * @return 
     */
    private List<Card> get4ACards() {
        List<Card> cards = new ArrayList<>();
        cards.add(dealCard(12, 0));
        cards.add(dealCard(12, 1));
        cards.add(dealCard(12, 2));
        cards.add(dealCard(12, 3));
        return cards;
    }
     /**
     * 4 3
     * @return 
     */
    private List<Card> get43Cards() {
        List<Card> cards = new ArrayList<>();
        cards.add(dealCard(1, 0));
        cards.add(dealCard(1, 1));
        cards.add(dealCard(1, 2));
        cards.add(dealCard(1, 3));
        return cards;
    }
    /**
     * 6 đôi
     * @return 
     */
    private List<Card> get6PairCards() {
        List<Card> cards = new ArrayList<>();
        cards.add(dealCard(0, 1));
        cards.add(dealCard(0, 2));
        cards.add(dealCard(2, 2));
        cards.add(dealCard(2, 3));
        
        cards.add(dealCard(3, 0));
        cards.add(dealCard(3, 2));
        cards.add(dealCard(5, 0));
        cards.add(dealCard(5, 3));
        
        cards.add(dealCard(9, 0));
        cards.add(dealCard(9, 2));
        cards.add(dealCard(11, 2));
        cards.add(dealCard(12, 0));
        
        cards.add(dealCard(12, 2));
        return cards;
    }
    
     /**
     * 6 đôi
     * @return 
     */
    private List<Card> get6PairCards_2() {
        List<Card> cards = new ArrayList<>();
        cards.add(dealCard(1, 2));
        cards.add(dealCard(1, 3));
        cards.add(dealCard(3, 3));
        cards.add(dealCard(6, 0));
        
        cards.add(dealCard(6, 2));
        cards.add(dealCard(7, 2));
        cards.add(dealCard(7, 3));
        cards.add(dealCard(8, 1));
        
        cards.add(dealCard(8, 3));
        cards.add(dealCard(10, 1));
        cards.add(dealCard(10, 2));
        cards.add(dealCard(12, 1));
        
        cards.add(dealCard(12, 2));
        return cards;
    }
    
     /**
     * Thùng phá sảnh thượng
     * @return 
     */
    private List<Card> getBigStrainghtFlush() {
        List<Card> cards = new ArrayList<>();
        cards.add(dealCard(8, 1));
        cards.add(dealCard(9, 1));
        cards.add(dealCard(10, 1));
        cards.add(dealCard(11, 1));
        cards.add(dealCard(12, 1));
        return cards;
    }
    /**
     * Thùng phá sảnh thượng cơ
     * @return 
     */
    private List<Card> getBigStrainghtFlushHeart() {
        List<Card> cards = new ArrayList<>();
        cards.add(dealCard(8, 3));
        cards.add(dealCard(9, 3));
        cards.add(dealCard(10, 3));
        cards.add(dealCard(11, 3));
        cards.add(dealCard(12, 3));
        return cards;
    }
     /**
     * Thùng
     * @return 
     */
    private List<Card> getFlush() {
        List<Card> cards = new ArrayList<>();
        cards.add(dealCard(2, 3));
        cards.add(dealCard(9, 3));
        cards.add(dealCard(5, 3));
        cards.add(dealCard(11, 3));
        cards.add(dealCard(8, 3));
        return cards;
    }
    /**
     * Thùng phá sảnh small cơ
     * @return 
     */
    private List<Card> getSmallStrainghtFlushHeart() {
        List<Card> cards = new ArrayList<>();
        cards.add(dealCard(0, 3));
        cards.add(dealCard(1, 3));
        cards.add(dealCard(2, 3));
        cards.add(dealCard(3, 3));
        cards.add(dealCard(4, 3));
        return cards;
    }
     /**
     * Thùng phá sảnh small
     * @return 
     */
    private List<Card> getSmallStrainghtFlush(){
        List<Card> cards = new ArrayList<>();
        cards.add(dealCard(0, 2));
        cards.add(dealCard(1, 2));
        cards.add(dealCard(2, 2));
        cards.add(dealCard(3, 2));
        cards.add(dealCard(4, 2));
        return cards;
    }
    
     /**
     * Thùng phá sảnh small
     * @return 
     */
    private List<Card> getStrainghtFlush(){
        List<Card> cards = new ArrayList<>();
        cards.add(dealCard(2, 2));
        cards.add(dealCard(3, 2));
        cards.add(dealCard(4, 2));
        cards.add(dealCard(5, 2));
        cards.add(dealCard(6, 2));
        return cards;
    }
    
     /**
     * Sảnh rồng
     * @return 
     */
    private List<Card> get13StrainghtCards() {
        List<Card> cards = new ArrayList<>();
        cards.add(dealCard(0, 3));//2
        cards.add(dealCard(1, 3));
        cards.add(dealCard(2, 1));//3
        cards.add(dealCard(3, 3));
        cards.add(dealCard(4, 3));
        cards.add(dealCard(5, 0));
        cards.add(dealCard(6, 3));
        cards.add(dealCard(7, 3));
        cards.add(dealCard(8, 3));
        cards.add(dealCard(9, 2));
        cards.add(dealCard(10, 3));
        cards.add(dealCard(11, 3));
        cards.add(dealCard(12, 3));
        return cards;
    }
    
    /**
     * 
     * @param index
     * @return 
     */
    public List<Card> getTestCase(int index) {
        List<Card> cards = new ArrayList<>();
        switch (index) {
            case MAUBINH_FULL_HOUSE:
                return getFullHouseCards();
            case MAUBINH_13_SAME_CLUB:
                return get13SameClubCards();
            case MAUBINH_13_SAME_DIAMOND:
                return get13SameDiamonCards();
            case MAUBINH_13_SAME_HEART:
                return get13SameHeartCards();
            case MAUBINH_13_SAME_SPADE:
                return get13SameSpadeCards();
            case MAUBINH_3STRAIGHT:
                return  get13StraightCards();
            case MAUBINH_3_FLUSH:
                return  get3FlushCards();
            case MAUBINH_3_XI:
                return get3ACards();
            case MAUBINH_4XI:
                return get4ACards();
            case MAUBINH_4_OF_A_KIND:
                return get43Cards();
            case MAUBINH_6_PAIR:
                return get6PairCards();
            case MAUBINH_6_PAIR_2:
                return get6PairCards_2();
             case MAUBINH_6_PAIR_3:
                return get6PairCards_3();
            case MAUBINH_BIG_STRAIGHT_FLUSH:
                return getBigStrainghtFlush();
            case MAUBINH_BIG_STRAIGHT_FLUSH_HEART:
                return getBigStrainghtFlushHeart();
            case MAUBINH_FLUSH:
                return getFlush();
            case MAUBINH_SMAILL_STRAIGHT_FLUSH_HEART:
                return getSmallStrainghtFlushHeart();
            case MAUBINH_SMALL_STRAIGHT_FLUSH:
                return getSmallStrainghtFlush();
            case MAUBINH_STRAIGHT_FLUSH:
                return getStrainghtFlush();
            case MAUBINH_13STRAIGHT:
                return get13StrainghtCards();
            case FOUR_OF_THREE:
                return get4XamCards();
        }
        return cards;
    }
    
      /**
     * 4 3
     * @return 
     */
    private List<Card> get4XamCards() {
        List<Card> cards = new ArrayList<>();
        cards.add(dealCard(1, 0));
        cards.add(dealCard(1, 1));
        cards.add(dealCard(1, 2));
        
        cards.add(dealCard(3, 0));
        cards.add(dealCard(3, 1));
        cards.add(dealCard(3, 2));
        
        cards.add(dealCard(5, 0));
        cards.add(dealCard(5, 1));
        cards.add(dealCard(5, 2));
        
        cards.add(dealCard(7, 0));
        cards.add(dealCard(7, 1));
        cards.add(dealCard(7, 2));
        
        return cards;
    }

    private List<Card> get6PairCards_3() {
       List<Card> cards = new ArrayList<>();
        cards.add(dealCard(0, 0));
        cards.add(dealCard(0, 3));
        cards.add(dealCard(1, 0));
        
        cards.add(dealCard(1, 1));
        cards.add(dealCard(5, 0));
        cards.add(dealCard(5, 2));
        
        cards.add(dealCard(11, 1));
        cards.add(dealCard(7, 0));
        cards.add(dealCard(7, 1));
        
        cards.add(dealCard(8, 0));
        cards.add(dealCard(8, 1));
        cards.add(dealCard(11, 0));
        cards.add(dealCard(11, 2));
        
        return cards;
    }
  
}
