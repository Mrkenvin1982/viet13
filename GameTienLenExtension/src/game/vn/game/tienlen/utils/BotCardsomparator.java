/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.vn.game.tienlen.utils;

import game.vn.common.card.BotCards;
import java.util.Comparator;

/**
 *
 * @author
 */
public class BotCardsomparator implements Comparator<BotCards> {

    @Override
    public int compare(BotCards o1, BotCards o2) {
        if (o1 == null || o2 == null) {
            return 0;
        }
        
        if(o1.getValue() < o2.getValue()){
            return -1;
        }
        
        if(o1.getValue() > o2.getValue()){
            return 1;
        }
            
        return 0;
    }
    
}
