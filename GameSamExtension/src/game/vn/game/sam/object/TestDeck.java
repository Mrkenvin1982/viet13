/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.vn.game.sam.object;

import game.vn.common.card.object.Card;
import game.vn.common.card.object.CardSet;
import game.vn.game.sam.SamController;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author tuanp
 */
public class TestDeck {

    private transient CardSet cardSet;
    private List<Card> cardUsed;
    private static final int _4_quy= 1;
    private static final int _4_doi_thong = 2;
    private static final int _3_doi_thong = 3;
    private static final int _sanh_6= 4;
    private static final int _5_doi= 5;
    private static final int _sanh_rong= 6;
    private static final int _10_card_same_red= 7;
    private static final int _10_card_same_black= 8;
    private static final int _3_of_kind= 9;
    private static final int _4_of_kind= 10;
    private static final int _2_heo= 11;
    private static final int _3_heo= 12;
    private static final int _4_heo= 13;
    private static final int _3_sam_co= 14;

    public TestDeck() {
        cardSet = new CardSet();
        cardUsed = new ArrayList<>();
    }

    public void reset() {
        cardSet.xaoBai();
        cardUsed.clear();
    }

    public Card dealCard(int number, int type) {
        Card c = cardSet.dealCard(number, type);
        cardUsed.add(c);
        return c;
    }

    public Card dealCard() {
        return cardSet.dealCard();
    }

    public void addFullCard(List<Card> cards) {
        while (cards.size() < SamController.DEFAULT_NUMBER_XAM_CARD) {
            Card c = dealCard();
            if (!cardUsed.contains(c)) {
                cards.add(c);
                cardUsed.add(c);
            }
        }
        Collections.sort(cards);
    }

    public List<Card> get4PairsCont() {
        List<Card> cards7;
        cards7 = new ArrayList<>();
        cards7.add(dealCard(2, 0));
        cards7.add(dealCard(2, 2));
        cards7.add(dealCard(3, 0));
        cards7.add(dealCard(3, 1));
        cards7.add(dealCard(4, 1));
        cards7.add(dealCard(4, 2));
        cards7.add(dealCard(5, 2));
        cards7.add(dealCard(5, 3));
        Collections.sort(cards7);
        return cards7;
    }
    public List<Card> getStraight6(){
        List<Card> cards7 = new ArrayList<>();
        cards7.add(dealCard(1, 1));
        cards7.add(dealCard(2, 2));
        cards7.add(dealCard(3, 3));
        cards7.add(dealCard(4, 0));
        cards7.add(dealCard(5, 1));
        cards7.add(dealCard(6, 2));
        cards7.add(dealCard(7, 3));
        cards7.add(dealCard(12, 2));
        cards7.add(dealCard(12, 3));
        Collections.sort(cards7);
        return cards7;
    }
    public List<Card> get5Pairs() {
        List<Card> cards7;
        cards7 = new ArrayList<>();
        cards7.add(dealCard(7, 0));
        cards7.add(dealCard(7, 1));
        cards7.add(dealCard(4, 2));
        cards7.add(dealCard(4, 3));
        cards7.add(dealCard(9, 0));
        cards7.add(dealCard(9, 1));
        cards7.add(dealCard(6, 0));
        cards7.add(dealCard(6, 3));
        cards7.add(dealCard(11, 2));
        cards7.add(dealCard(11, 1));
        Collections.sort(cards7);
        return cards7;
    }
    
    public List<Card> getStraightDragon(){
        List<Card> cards7 = new ArrayList<>();
        cards7.add(dealCard(0, 0));
        cards7.add(dealCard(1, 1));
        cards7.add(dealCard(2, 2));
        cards7.add(dealCard(3, 3));
        cards7.add(dealCard(4, 0));
        cards7.add(dealCard(5, 1));
        cards7.add(dealCard(6, 2));
        cards7.add(dealCard(7, 3));
        cards7.add(dealCard(8, 2));
        cards7.add(dealCard(9, 3));
        Collections.sort(cards7);
        return cards7;
    }
    public List<Card> get10SameColorsRed(){
        List<Card> cards7 = new ArrayList<>();
        cards7.add(dealCard(0, 2));
        cards7.add(dealCard(1, 3));
        cards7.add(dealCard(8, 2));
        cards7.add(dealCard(8, 3));
        cards7.add(dealCard(4, 2));
        cards7.add(dealCard(5, 2));
        cards7.add(dealCard(6, 3));
        cards7.add(dealCard(7, 3));
        cards7.add(dealCard(12, 2));
        cards7.add(dealCard(12, 3));
        Collections.sort(cards7);
        return cards7;
    }
    public List<Card> get10SameColorsBlack(){
        List<Card> cards7 = new ArrayList<>();
        cards7.add(dealCard(0, 1));
        cards7.add(dealCard(1, 0));
        cards7.add(dealCard(8, 0));
        cards7.add(dealCard(8, 1));
        cards7.add(dealCard(4, 1));
        cards7.add(dealCard(5, 0));
        cards7.add(dealCard(6, 1));
        cards7.add(dealCard(7, 0));
        cards7.add(dealCard(12, 0));
        cards7.add(dealCard(12, 1));
        Collections.sort(cards7);
        return cards7;
    }
    
    public List<Card> get3OfAkind(int cardNumber){
        List<Card> cards5 = new ArrayList<>();
        cards5.add(dealCard(cardNumber, 0));
        cards5.add(dealCard(cardNumber, 1));
        cards5.add(dealCard(cardNumber, 3));
        return cards5;
    }
    public List<Card> get4OfAKind(int cardNumber) {
        // add tu quy 9;
        List<Card> cards5 = new ArrayList<>();
        cards5.add(dealCard(cardNumber, 0));
        cards5.add(dealCard(cardNumber, 1));
        cards5.add(dealCard(cardNumber, 2));
        cards5.add(dealCard(cardNumber, 3));
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

    public List<Card> get3Heo() {
        List<Card> cards = new ArrayList<>();
        cards.add(dealCard(12, 1));
        cards.add(dealCard(12, 3));
        cards.add(dealCard(12, 2));
        return cards;
    }
  public List<Card> get2Heo() {
        List<Card> cards = new ArrayList<>();
        cards.add(dealCard(12, 1));
        cards.add(dealCard(12, 3));
        return cards;
    }
    public List<Card> get4Heo() {
        List<Card> cards = new ArrayList<>();
        cards.add(dealCard(12, 0));
        cards.add(dealCard(12, 1));
        cards.add(dealCard(12, 2));
        cards.add(dealCard(12, 3));
        cards.add(dealCard(4, 0));
        cards.add(dealCard(4, 1));
        cards.add(dealCard(4, 2));
        cards.add(dealCard(11, 0));
        cards.add(dealCard(11, 1));
        cards.add(dealCard(11, 3));
        return cards;
    }

    public List<Card> get3SamCo() {
        List<Card> cards = new ArrayList<>();
        cards.add(dealCard(0, 0));
        cards.add(dealCard(0, 1));
        cards.add(dealCard(0, 2));
        cards.add(dealCard(1, 0));
        cards.add(dealCard(1, 1));
        cards.add(dealCard(1, 3));
        cards.add(dealCard(5, 0));
        cards.add(dealCard(5, 1));
        cards.add(dealCard(5, 2));
        return cards;
    }
    
    public List<Card> get8OfAKind(int cardNumber) {
        // add tu quy 9;
        List<Card> cards5 = new ArrayList<>();
        cards5.add(dealCard(cardNumber, 0));
        cards5.add(dealCard(cardNumber, 1));
        cards5.add(dealCard(cardNumber, 2));
        cards5.add(dealCard(cardNumber, 3));
        return cards5;
    }
    
    public List<Card> getCardsXu(){
        List<Card> cards7 = new ArrayList<>();
        cards7.add(dealCard(0, 1));
        cards7.add(dealCard(1, 0));
        cards7.add(dealCard(2, 0));
        cards7.add(dealCard(3, 1));
        cards7.add(dealCard(4, 1));
        cards7.add(dealCard(5, 2));
        cards7.add(dealCard(6, 1));
        cards7.add(dealCard(7, 0));
        cards7.add(dealCard(11, 0));
        cards7.add(dealCard(11, 1));
        Collections.sort(cards7);
        return cards7;
    }
     public List<Card> getDoiXido(){
        List<Card> cards7 = new ArrayList<>();
        cards7.add(dealCard(11, 2));
        cards7.add(dealCard(11, 3));
        Collections.sort(cards7);
        return cards7;
    }
     
    public List<Card> getTest(int type) {
        List<Card> cards = new ArrayList<>();
        switch (type) {
            case _4_quy:
                cards = get8OfAKind(4);
                break;
            case _10_card_same_black:
                cards = get10SameColorsBlack();
                break;
            case _10_card_same_red:
                cards = get10SameColorsRed();
                break;
            case _2_heo:
                cards = get2Heo();
                break;
            case _3_doi_thong:
                cards = get3PairsCont();
                break;
            case _3_heo:
                cards = get3Heo();
                break;
            case _3_of_kind:
                cards = get3OfAkind(4);
                break;
            case _3_sam_co:
                cards = get3SamCo();
                break;
            case _4_doi_thong:
                cards = get4PairsCont();
                break;
            case _4_heo:
                cards = get4Heo();
                break;
            case _4_of_kind:
                cards = get4OfAKind(4);
                break;
            case _5_doi:
                cards = get5Pairs();
                break;
            case _sanh_6:
                cards = getStraight6();
                break;
            case _sanh_rong:
                cards = getStraightDragon();
                break;

        }
        return cards;
    }
}