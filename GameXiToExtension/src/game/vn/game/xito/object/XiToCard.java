/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.vn.game.xito.object;

import game.vn.common.card.object.Card;

/**
 *
 * @author tuanp
 */
public class XiToCard extends Card{
    private static final byte ACE_NUMBER = 14;
    private boolean isHideCard =false;
    public XiToCard(byte id) {
        super(id);
    }
    /**
     * Tinh tu 2, lon nhat la con xi 14
     * @return 
     */
    @Override
    public int getCardNumber() {
        return super.getCardNumber() + 2; 
    }
    
    /**
     * Kiem tra neu bai la xi
     * @return xi: true, con lai false
     */
    public boolean isAce(){
        return getCardNumber() == ACE_NUMBER;
    }    

    @Override
    public String toString() {
        String faceDesc;
        switch (getCardNumber()) {
            case 11:
                faceDesc = "J";
                break;
            case 12:
                faceDesc = "Q";
                break;
            case 13:
                faceDesc = "K";
                break;
            case ACE_NUMBER:
                faceDesc = "A";
                break;
            default:
                faceDesc = String.valueOf(getCardNumber());
                break;
        }
        String suitDesc = "";
        switch (super.getCardType()) {
            case 0:
                suitDesc = "bích";
                break;
            case 1:
                suitDesc = "chuồn";
                break;
            case 2:
                suitDesc = "rô";
                break;
            case 3:
                suitDesc = "cơ";
                break;
        }

//        return "XiToCard{" + "id=" + super.getId() + ", quân bài=" + faceDesc + ", cấp=" + suitDesc + '}';
        return faceDesc + " " + suitDesc;
    }

    public void setIsHideCard(boolean isHideCard) {
        this.isHideCard = isHideCard;
    }
    
    public boolean isHideCard(){
        return this.isHideCard;
    }
}
