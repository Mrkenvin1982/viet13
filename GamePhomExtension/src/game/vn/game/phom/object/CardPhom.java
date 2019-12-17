/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.vn.game.phom.object;

import java.util.List;

/**
 *
 * @author tuanp
 */
public class CardPhom implements Comparable<CardPhom> {
    public static final int HEO_NUMBER = 12;
    /**
     * qui dinh so loai nuoc tren cung 1 con bai.
     * Voi bai tay thong thuong thi co Co - Ro - Chuon - Bich.
     */
    private static final int TYPE_NUMBER = 4;
    /**
     * id cua con bai.
     */
    private final byte id;
    /**
     * Tên con bài được qui định bằng số.
     */
    private final int cardNumber;
    /**
     * Nước của con bài
     * 0: bí
     * 1: chuồn
     * 2: rô
     * 3: cơ.
     */
    private final int cardType;
    /**
     * instance 1 con bai.
     * @param id cua con bai
     */
    public CardPhom(byte id) {
        this.id = id;
        cardNumber = id / 4  +1;
        cardType = id % TYPE_NUMBER;
    }

    /**
     * từ 0 -> 12
     * Lay gi tri cua con bai bang id.
     * @return id cua con bai
     */
    public byte getId() {
        return id;
    }
    /**
     * từ 0 -> 51
     * vơi tien len: 0 la 3 bich
     * voi phom: 0 la A bich.
     * @return so thu tu cua con bai.
     */
    public int getCardNumber() {
        return cardNumber;
    }

    /**
     * lấy chất của con bài
     * 0: bí
     * 1: chuồn
     * 2: rô
     * 3: cơ.
     */
    public int getCardType(){
        return cardType;
    }
    /**
     * voi bai tay thong thuong, nuoc bai la co, ro, chuon, bich.
     * @return nuoc bai cua con bai
     */
//    public int getCardType() {
//        return cardType;
//    }

    /**
     * lay thong tin mau bai cua con bai
     * @return true neu la nuoc bich hoac chuon, nguoc lai la false
     */
    public boolean isTypeBlack() {
        return cardType < 2;
    }

    /**
     * kiem tra xem con bai nay co phai la 3 bich hay khong.
     * @return true neu la con 3bich, nguoc lai la false.
     */
    public boolean is3Bich() {
        return id == 0;
    }

    /**
     * kiem tra xem con bai nay co phai la heo hay khong.
     * @return true neu la heo, nguoc lai la false.
     */
    public boolean isHeo() {
        return cardNumber == HEO_NUMBER;
    }

    /**
     * Kiem tra xem card1 co lon hon card2 hay khong.
     * @param card1
     * @param card2
     * @return true neu card1 > card 2 trong tien len, con lai return false.
     */
    public static boolean isHigher(CardPhom card1, CardPhom card2) {
        return card1.compareTo(card2) > 0;
    }

    /**
     * tra ve -1 neu nho hon, 0 la bang, 1 la lon hon.
     * @param o objec can so sanh
     * @return ket qua so sanh
     */
    public int compareTo(CardPhom o) {
        Byte thisID = Byte.valueOf(this.getId());
        Byte oID = Byte.valueOf(o.getId());
        return thisID.compareTo(oID);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final CardPhom other = (CardPhom) obj;
        if (this.id != other.id) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 79 * hash + this.id;
        return hash;
    }

    @Override
    public String toString() {
        return "" + cardNumber + " " + cardType;
    }
        /**
     * Đếm số K
     * @param cards
     * @return 
     */
     public static int countK(List<CardPhom> cards) {
        int count = 0;
        for (CardPhom c : cards) {
            if (c.getCardNumber() == 13) {
                count++;
            }
        }
        return count;
    }
     
    public static int countA(List<CardPhom> cards) {
        int count = 0;
        for (CardPhom c : cards) {
            if (c.getCardNumber() == 1) {
                count++;
            }
        }
        return count;
    }
}
