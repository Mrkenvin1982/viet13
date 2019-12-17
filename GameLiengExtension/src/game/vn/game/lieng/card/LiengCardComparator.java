/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.vn.game.lieng.card;

import java.util.Comparator;

/**
 *
 * @author tuanp
 */
public class LiengCardComparator implements Comparator<LiengCard> {

    @Override
    public int compare(LiengCard t, LiengCard t1) {
        int cardNumber = getCardNumber(t);
        int cardNumber1 = getCardNumber(t1);
        if (cardNumber > cardNumber1) {
            return 1;
        } else if (cardNumber < cardNumber1) {
            return -1;
        } else {
            return 0;
        }
    }
    
    private int getCardNumber(LiengCard card){
        if(card.isAce()) return 1;
        return card.getCardNumber();
    }
}