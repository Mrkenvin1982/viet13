/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.vn.game.phom.utils;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author tuanp
 */
public class PhomUtils {

    /**
     * sử dụng cho game phỏm
     *
     * @param cardId
     * @return trả về tên của con bài
     */
    public static String getCardPhomString(int cardId) {
        int cardvalue = cardId / 4 + 1;
        if (cardvalue == 13) {
            return "K";
        } else if (cardvalue == 12) {
            return "Q";
        } else if (cardvalue == 11) {
            return "J";
        } else if (cardvalue == 1) {
            return "A";
        }
        return "" + cardvalue++;
    }

    /**
     * Sử dụng cho bộ bài của game phỏm
     *
     * @param cardId
     * @return Tên gọi của lá bài: 3 bích, 3 rô...
     */
    public static String getCardName(int cardId) {
        if (cardId == -1) {
            return "";
        }
        return getCardPhomString(cardId) + " " + getCardType(cardId);
    }

    /**
     * lấy tên con mảng bài, dùng cho phỏm
     *
     * @param arrayCardsId
     * @return
     */
    public static String listCardToString(byte[] arrayCardsId) {
        if (arrayCardsId == null || arrayCardsId.length == 0) {
            return "";
        }
        StringBuilder returnString = new StringBuilder();
        for (Byte b : arrayCardsId) {
            returnString.append(getCardName(b)).append(" ");
        }
        return returnString.toString();
    }

    /**
     *
     * @param list
     * @return
     */
    public static String listCardToString(List<Byte> list) {
        if (list == null || list.isEmpty()) {
            return "";
        }
        String returnString = "";
         for (byte b : list) {
                returnString += PhomUtils.getCardName(b) + " ";
            }
        return returnString;
    }
    
    /**
     * Lấy chất bài theo cardId
     *
     * @param cardId
     * @return
     */
    private static String getCardType(int cardId) {
        int type = cardId % 4;
        if (type == 0) {
            return "bích";
        } else if (type == 1) {
            return "chuồn";
        } else if (type == 2) {
            return "rô";
        } else {
            return "cơ";
        }
    }
    
    public static List<Short> list2List(List<Byte> cardset) {
        List<Short> cards = new ArrayList<>();
        for (int i = 0; i < cardset.size(); i++) {
            cards.add((short)cardset.get(i));
        }
        return cards;
    }
}
