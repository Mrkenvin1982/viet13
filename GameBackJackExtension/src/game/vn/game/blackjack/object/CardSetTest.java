/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.vn.game.blackjack.object;

import game.vn.common.card.object.Card;
import game.vn.common.card.object.CardSet;
import java.util.ResourceBundle;

/**
 * card set nay dung de test
 * @author tuanp
 */
public class CardSetTest extends CardSet{
    
        
    public CardSetTest(String setCards) {
        try {
            cards.clear();
            ResourceBundle rb = ResourceBundle.getBundle(CardSetTest.class.getName());
            String scards = rb.getString(setCards);
            String[] aCards = scards.split(",");
            for (String c : aCards) {
                String[] array = c.split(":");
                byte id = (byte) (Byte.parseByte(array[0]) * 4 + Byte.parseByte(array[1]));
                cards.add(new Card(id));
            }
        } catch (Exception e) {
        }
        //add full card
    }
}
