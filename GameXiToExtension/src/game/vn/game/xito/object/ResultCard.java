/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.vn.game.xito.object;

import java.util.List;

/**
 * Ket qua sau khi tinh toan bai tren tay
 * @author tuanp
 */
public class ResultCard {
    private byte value;
    private String strValue;
    private List<XiToCard> highestCards;
    public ResultCard() {
    }

    public List<XiToCard> getHighestCards() {
        return highestCards;
    }

    public void setHighestCards(List<XiToCard> highestCard) {
        this.highestCards = highestCard;
    }

    public byte getValue() {
        return value;
    }

    public String getStrValue() {
        return strValue;
    }

    public void setValue(byte value) {
        this.value = value;
    }

    public void setStrValue(String strValue) {
        this.strValue = strValue;
    }
    
    /**
     * Lay la lon nhat trong bo lon nhat
     * @return 
     */
    public XiToCard getHighestCard(){
        return highestCards.get(highestCards.size()-1);
    }

    @Override
    public String toString() {
        return "ResultCard{" + "value=" + value + ", strValue=" + strValue + ", highestCards=" + highestCards + '}';
    }   
}
