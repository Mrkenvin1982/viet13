/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package game.vn.game.baicao.object;

import game.vn.common.card.object.Card;
import game.vn.common.card.object.CardSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 *
 *
 * @author tuanp
 */
public class DeckTest {
    private static final int _0_diem = 1;
    private static final int _3_tien = 2;
    private static final int _9_diem = 3;
    private static final int _8_diem = 4;
    private static final int _sam_co = 5;
    private static final int _3_k = 6;
    private static final int _1_diem = 7;
    
    private static final int DEFAULT_CARD_NUMBER = 3;

    
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

    public List<Card> getTestCase(int testcase) {
        List<Card> cards = new ArrayList<>();
        switch(testcase){
            case _0_diem:
                cards = get0Diem();
                break;
            case _3_tien:
                cards = get3Tien();
                break;
            case _9_diem:
                cards = get9diem();
                break;
            case _8_diem:
                cards = get8diem();
                break;
            case _sam_co:
                Random rand = new Random();
                int cardNumber=rand.nextInt(12);
                cards = get3Kind(cardNumber);
                break;
            case _3_k:
                cards = get3k();
                break;
            case _1_diem:
                cards = get1Diem();
                break;
        }
        return cards;
    }
     private List<Card> getsamco() {
        List<Card> cards5 = new ArrayList<>();
        cards5.add(dealCard(2, 0));
        cards5.add(dealCard(2, 1));
        cards5.add(dealCard(2, 2));
        return cards5;
    }

    private List<Card> get1Diem() {
        List<Card> cards5 = new ArrayList<>();
        cards5.add(dealCard(2, 0));
        cards5.add(dealCard(2, 1));
        cards5.add(dealCard(4, 2));
        return cards5;
    }
    private List<Card> get0Diem() {
        List<Card> cards5 = new ArrayList<>();
        cards5.add(dealCard(2, 0));
        cards5.add(dealCard(2, 1));
        cards5.add(dealCard(3, 2));
        return cards5;
    }

    private List<Card> get3Tien() {
        List<Card> cards5 = new ArrayList<>();
        cards5.add(dealCard(11, 0));
        cards5.add(dealCard(11, 1));
        cards5.add(dealCard(12, 2));
        return cards5;
    }
     private List<Card> get3k() {
        List<Card> cards5 = new ArrayList<>();
        cards5.add(dealCard(12, 0));
        cards5.add(dealCard(12, 1));
        cards5.add(dealCard(12, 2));
        return cards5;
    }
     //test 3 con giống nhau
    private List<Card> get3Kind(int cardNumber) {
        List<Card> cards5 = new ArrayList<>();
        cards5.add(dealCard(cardNumber, 0));
        cards5.add(dealCard(cardNumber, 1));
        cards5.add(dealCard(cardNumber, 2));
        return cards5;
    }

    private List<Card> get9diem() {
        List<Card> cards5 = new ArrayList<>();
        cards5.add(dealCard(2, 0));
        cards5.add(dealCard(5, 1));
        cards5.add(dealCard(10, 2));
        return cards5;
    }
    private List<Card> get8diem() {
        List<Card> cards5 = new ArrayList<>();
        cards5.add(dealCard(7, 1));
        cards5.add(dealCard(12, 2));
        cards5.add(dealCard(12, 2));
        return cards5;
    }
    
}
