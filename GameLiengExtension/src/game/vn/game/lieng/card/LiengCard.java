/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.vn.game.lieng.card;

import game.vn.common.card.object.Card;

/**
 *
 * @author tuanp
 */
public class LiengCard extends Card {

    private static final byte ACE_NUMBER = 14;

    public LiengCard(byte id) {
        super(id);
    }

    /**
     * Tinh tu heo = 2, xì thì number = 1
     *
     * @return
     */
    @Override
    public int getCardNumber() {
        return super.getCardNumber() + 2;
    }

    /**
     * Kiem tra neu bai la xi
     *
     * @return xi: true, con lai false
     */
    public boolean isAce() {
        return getCardNumber() == ACE_NUMBER;
    }

    public boolean isHuman() {
        if (getCardNumber() == 11 || getCardNumber() == 12 || getCardNumber() == 13) {
            return true;
        }
        return false;
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
        String suitDesc;
        switch (getCardType()) {
            case 0:
                suitDesc = "bích";
                break;
            case 1:
                suitDesc = "chuồn";
                break;
            case 2:
                suitDesc = "cơ";
                break;
            case 3:
                suitDesc = "rô";
                break;
            default:
                suitDesc = ""+getCardType();
                break;
        }

//        return "XiToCard{" + "id=" + super.getId() + ", quân bài=" + faceDesc + ", cấp=" + suitDesc + '}';
        return faceDesc + " "+suitDesc;
    }
}
