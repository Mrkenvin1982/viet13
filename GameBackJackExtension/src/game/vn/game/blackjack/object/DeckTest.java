/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package game.vn.game.blackjack.object;

import game.vn.common.card.object.Card;
import game.vn.common.card.object.CardSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 *
 * @author tuanp
 */
public class DeckTest {
    private static final int _xi_dzach = 1;
    private static final int _2_A = 2;
    private static final int _ngu_linh = 3;
    private static final int DEFAULT_CARD_NUMBER = 2;

    
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
            case _xi_dzach:
                cards = getXiDzach();
                break;
            case _2_A:
                cards = get2A();
                break;
            case _ngu_linh:
                cards = getNguLinh();
                break;
        }
        return cards;
    }

    private List<Card> getXiDzach() {
        List<Card> cards5 = new ArrayList<>();
        cards5.add(dealCard(0, 0));
        cards5.add(dealCard(11, 1));
        return cards5;
    }

    private List<Card> get2A() {
        List<Card> cards5 = new ArrayList<>();
        cards5.add(dealCard(0, 0));
        cards5.add(dealCard(0, 1));
        return cards5;
    }

    private List<Card> getNguLinh() {
        List<Card> cards5 = new ArrayList<>();
        cards5.add(dealCard(0, 0));
        cards5.add(dealCard(1, 1));
        cards5.add(dealCard(2, 3));
        cards5.add(dealCard(2, 1));
        return cards5;
    }
    
}
