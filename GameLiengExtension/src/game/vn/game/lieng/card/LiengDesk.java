/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.vn.game.lieng.card;

import game.vn.game.lieng.object.LiengPlayer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author tuanp
 */
public class LiengDesk {

    public static final int NUM_CARD = 52;
    public static final LiengCard[] LIENG_CARDS;
    private final List<LiengCard> cards = new ArrayList<>();
    private static final List<LiengCard> listCardsUsed = new ArrayList<>();
    private int usedcardnumber = 0;
    
    static {
        LIENG_CARDS = new LiengCard[NUM_CARD];
        for (byte i = 0; i < NUM_CARD; i++) {
            LIENG_CARDS[i] = new LiengCard(i);
        }
    }

    public LiengDesk() {
        for (LiengCard liengCard : LIENG_CARDS) {
            cards.add(liengCard);
        }
        Collections.shuffle(cards);
    }
    public void xaoBai(){
        usedcardnumber=0;
        listCardsUsed.clear();
        Collections.shuffle(cards);
    }
        /**
     * Chia bai theo number va type, dung de test
     *
     * @param number
     * @param type
     * @return Card
     */
    public final LiengCard dealCard(int number, int type) {
        for (LiengCard card : cards) {
            if (card.getCardNumber() == number && card.getCardType() == type && !listCardsUsed.contains(card)) {
                listCardsUsed.add(card);
                return card;
            }
        }
        return null;
    }

    public LiengCard dealCard() {
         if (this.usedcardnumber >= NUM_CARD
                || this.usedcardnumber < 0) {
            return null;
        }
        return this.cards.get(usedcardnumber++);
    }

    public void dealCard(LiengPlayer player) {
        for (int i = 0; i < 3; i++) {
            player.addCard(dealCard());
        }   
    }
}
