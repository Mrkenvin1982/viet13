/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.vn.game.baicao.object;

import game.vn.common.card.object.Card;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author
 */
public class VisualPlayer implements Comparable<VisualPlayer>{

    private List<Card> cards;
    private int result;

    public VisualPlayer() {
        cards = new ArrayList<>();
    }
    
    public List<Card> getCards(){
        return cards;
    }
    
    public void addCards(Card card){
        this.cards.add(card);
    }

    public int getResult() {
        return result;
    }

    public void setResult(int result) {
        this.result = result;
    }

    @Override
    public int compareTo(VisualPlayer o) {
        Integer o1 = this.result;
        Integer o2 = o.getResult();
        return o1.compareTo(o2);
    }
}
