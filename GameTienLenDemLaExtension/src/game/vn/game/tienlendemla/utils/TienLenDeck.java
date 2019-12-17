/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package game.vn.game.tienlendemla.utils;

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
public class TienLenDeck {
    private static final int _TU_QUY = 1;
    private static final int _3_DOI_THONG = 2;
    private static final int _4_DOI_THONG = 3;
    private static final int _5_DOI_THONG = 4;
    private static final int _4_HEO = 5;
    private static final int _4_A = 6;
    private static final int _6_PAIR= 7;
    private static final int _DRAGON_STRAIGHT= 8;
    private static final int _3_HEO= 9;
    private static final int _3_DOI_THONG_3_BICH = 12;
    private static final int _4_CON_3= 13;
    private static final int _3_DOI_THONG_TU_QUY= 14;
    
    private transient CardSet cardSet;
    private List<Card> cardUsed;
    public TienLenDeck() {
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
        while (cards.size() < 13) {
            Card c = dealCard();
            if (!cardUsed.contains(c)) {
                cards.add(c);
                cardUsed.add(c);
            }
        }
        Collections.sort(cards);
    }

    /**
     * sảnh rồng
     * @return 
     */
    public List<Card> getSanhrong() {
        List<Card> cards7;
        cards7 = new ArrayList<>();
        cards7.add(new Card((byte) 0));
        cards7.add(new Card((byte)1));
        cards7.add(new Card((byte) 4));
        cards7.add(new Card((byte) 8));
        cards7.add(new Card((byte) 12));
        cards7.add(new Card((byte) 16));
        cards7.add(new Card((byte) 20));
        cards7.add(new Card((byte) 24));
        cards7.add(new Card((byte) 28));
        cards7.add(new Card((byte) 32));
        cards7.add(new Card((byte) 36));
        cards7.add(new Card((byte) 40));
        cards7.add(new Card((byte) 44));     
        Collections.sort(cards7);
        return cards7;
    }
    /**
     * 4 đôi thông
     * @return 
     */
//    public List<Card> get4PairsCont() {
//        List<Card> cards7;
//        cards7 = new ArrayList<>();
//        cards7.add(dealCard(2, 0));
//        cards7.add(dealCard(2, 2));
//        cards7.add(dealCard(3, 0));
//        cards7.add(dealCard(3, 1));
//        cards7.add(dealCard(4, 1));
//        cards7.add(dealCard(4, 2));
//        cards7.add(dealCard(5, 2));
//        cards7.add(dealCard(5, 3));
//        Collections.sort(cards7);
//        return cards7;
//    }
//     * 4 đôi thông
//     *
//     * @return
//     */
    public List<Card> get4PairsCont() {
        List<Card> cards7;
        cards7 = new ArrayList<>();

        cards7.add(dealCard(6, 0));
        cards7.add(dealCard(6, 1));
        cards7.add(dealCard(7, 0));
        cards7.add(dealCard(7, 1));
        cards7.add(dealCard(8, 0));
        cards7.add(dealCard(8, 1));
        cards7.add(dealCard(9, 0));
        cards7.add(dealCard(9, 1));
        Collections.sort(cards7);
        return cards7;
    }
    /**
     * 5 doi thong
     * @return 
     */
    public List<Card> get5PairsCont() {
        List<Card> cards7;
        cards7 = new ArrayList<>();
        cards7.add(dealCard(5, 0));
        cards7.add(dealCard(5, 1));
        cards7.add(dealCard(1, 0));
        cards7.add(dealCard(1, 1));
        cards7.add(dealCard(2, 0));
        cards7.add(dealCard(2, 1));
        cards7.add(dealCard(3, 0));
        cards7.add(dealCard(3, 1));
        cards7.add(dealCard(4, 0));
        cards7.add(dealCard(4, 1));

        return cards7;
    }
    /**
     * 6 đôi
     * @return 
     */
     public List<Card> get6Pairs() {
        List<Card> cards7;
        cards7 = new ArrayList<>();
        cards7.add(dealCard(0, 0));
        cards7.add(dealCard(0, 1));
        cards7.add(dealCard(1, 0));
        cards7.add(dealCard(1, 1));
        cards7.add(dealCard(2, 0));
        cards7.add(dealCard(2, 1));
        cards7.add(dealCard(3, 0));
        cards7.add(dealCard(3, 1));
        cards7.add(dealCard(4, 0));
        cards7.add(dealCard(4, 1));
        cards7.add(dealCard(5, 0));
        cards7.add(dealCard(5, 1));

        return cards7;
    }
    public List<Card> get4OfAKind() {
        // add tu quy 9;
        List<Card> cards5 = new ArrayList<>();
        cards5.add(dealCard(6, 0));
        cards5.add(dealCard(6, 1));
        cards5.add(dealCard(6, 2));
        cards5.add(dealCard(6, 3));
        return cards5;
    }
    public List<Card> get4OfAKindA() {
        // add tu quy 9;
        List<Card> cards5 = new ArrayList<>();
        cards5.add(dealCard(11, 0));
        cards5.add(dealCard(11, 1));
        cards5.add(dealCard(11, 2));
        cards5.add(dealCard(11, 3));
        return cards5;
    }
    public List<Card> get3PairsCont() {
        List<Card> cards7;
        cards7 = new ArrayList<>();

        cards7.add(dealCard(7, 0));
        cards7.add(dealCard(7, 1));
        cards7.add(dealCard(8, 0));
        cards7.add(dealCard(8, 1));
        cards7.add(dealCard(9, 0));
        cards7.add(dealCard(9, 1));
        Collections.sort(cards7);
        return cards7;
    }

    public List<Card> get4Heo() {
        List<Card> cards = new ArrayList<>();

        cards.add(dealCard(12, 0));
        cards.add(dealCard(12, 1));
        cards.add(dealCard(12, 2));
        cards.add(dealCard(12, 3));
        return cards;
    }
    public List<Card>CardXu()
    {
        List<Card> cards = new ArrayList<>();
        
        cards.add(CardSet.getCard((byte) 0));
        cards.add(CardSet.getCard((byte) 4));
        cards.add(CardSet.getCard((byte) 8));
        cards.add(CardSet.getCard((byte) 12));
        cards.add(CardSet.getCard((byte) 17));
        cards.add(CardSet.getCard((byte) 20));
        
        cards.add(CardSet.getCard((byte) 24));
        cards.add(CardSet.getCard((byte) 28));
        cards.add(CardSet.getCard((byte) 32));
        cards.add(CardSet.getCard((byte) 36));
        
        cards.add(CardSet.getCard((byte)48));
        cards.add(CardSet.getCard((byte) 49));
        cards.add(CardSet.getCard((byte) 50));
        return cards;
    }
    
    public List<Card>CardXu1()
    {
        List<Card> cards = new ArrayList<>();
        
        cards.add(CardSet.getCard((byte) 20));
        cards.add(CardSet.getCard((byte) 21));
        cards.add(CardSet.getCard((byte) 24));
        cards.add(CardSet.getCard((byte) 25));
        cards.add(CardSet.getCard((byte) 28));
        cards.add(CardSet.getCard((byte) 29));
        
        cards.add(CardSet.getCard((byte) 30));
        cards.add(CardSet.getCard((byte) 31));
        return cards;
    }

    public List<Card> getTestCase(int testcase) {
        List<Card> cards = new ArrayList<>();
        switch(testcase){
            case _3_DOI_THONG:
                cards = get3PairsCont();
                break;
            case _4_DOI_THONG:
                cards = get4PairsCont();
                break;
            case _5_DOI_THONG:
                cards = get5PairsCont();
                break;
            case _TU_QUY:
                cards = get4OfAKind();
                break;
            case _4_HEO:
                cards = get4Heo();
                break;
            case _4_A:
                cards = get4OfAKindA();
                break;
            case _6_PAIR:
                cards = get6Pairs();
                break;
            case _DRAGON_STRAIGHT:
                cards = getSanhrong();
                break;
            case _3_HEO:
                cards = CardXu();
                break;
            case _3_DOI_THONG_3_BICH:
                cards = get3PairsCont3Bich();
                break;
            case _4_CON_3:
                cards = get4Con3();
                break;
            case _3_DOI_THONG_TU_QUY:
                cards = get3PairsContAnd4Kind();
                break;  
        }
        cardUsed.addAll(cards);
        return cards;
    }

    public List<Card> get3PairsCont3Bich() {
        List<Card> cards7;
        cards7 = new ArrayList<>();

        cards7.add(dealCard(0, 0));
        cards7.add(dealCard(0, 1));
        cards7.add(dealCard(1, 0));
        cards7.add(dealCard(1, 1));
        cards7.add(dealCard(2, 0));
        cards7.add(dealCard(2, 1));
        Collections.sort(cards7);
        return cards7;
    }

    public List<Card> get4Con3() {
        List<Card> cards7;
        cards7 = new ArrayList<>();

        cards7.add(dealCard(0, 0));
        cards7.add(dealCard(0, 1));
        cards7.add(dealCard(0, 2));
        cards7.add(dealCard(0, 3));
        Collections.sort(cards7);
        return cards7;
    }
    
    public List<Card> get3PairsContAnd4Kind() {
        List<Card> cards7;
        cards7 = new ArrayList<>();

        cards7.add(dealCard(7, 0));
        cards7.add(dealCard(7, 1));
        cards7.add(dealCard(8, 0));
        cards7.add(dealCard(8, 1));
        cards7.add(dealCard(9, 0));
        cards7.add(dealCard(9, 1));
        cards7.add(dealCard(3, 0));
        cards7.add(dealCard(3, 1));
        cards7.add(dealCard(3, 2));
        cards7.add(dealCard(3, 3));
        Collections.sort(cards7);
        return cards7;
    }
    private void test() {
//TienLenDeck deck = new TienLenDeck();
//        List<Card> mcards = deck.get3PairsCont();
//        deck.addFullCard(mcards);
//        players[0].getCards().addAll(mcards);
//        List<Card> list4pairsCont = deck.get4PairsCont();
//        list4pairsCont.add(CardSet.getCard((byte) 46));
//        list4pairsCont.add(CardSet.getCard((byte) 47));
//        players[1].getCards().addAll(list4pairsCont);
//        deck.addFullCard(players[1].getCards());
    }
}
