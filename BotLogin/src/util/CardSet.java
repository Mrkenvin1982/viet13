/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author tuanp
 */
public class CardSet {

    /**
     * mac dinh so luong con bai trong bo bai. <br>
     * 1 bo bai co 52 con
     */
    public static final int DEFAULT_CARD_NUMBER = 52;
    public static final Map<Byte, Card> CARD_MAP;

    static {
        CARD_MAP = new HashMap<>();
        for (byte i = 0; i < DEFAULT_CARD_NUMBER; i++) {
            CARD_MAP.put(i, new Card(i));
        }
    }
    /**
     * List các con bài trong bộ bài.
     */
    protected final transient ArrayList<Card> cards = new ArrayList<Card>();
    /**
     * vị trí con bài đang sử dụng trong bộ bài.
     */
    protected transient int cardUseIndex;

    /**
     * tạo bộ bài mới, luôn luôn gồm 52 là từ 0 đến 51, qui định 0 là con bài
     * nhỏ nhất, 51 là con bài to nhất.
     *
     */
    public CardSet() {
        for (Card card : CARD_MAP.values()) {
            cards.add(card);
        }
    }

    /**
     * đảo thứ tự của các con bài trong bộ bài đi.
     */
    public final void xaoBai() {
        Collections.shuffle(cards);
        cardUseIndex = 0;
    }

    /**
     * Chia bài.
     *
     * @return con bài hiện tại của bộ bài<br> Return null neu het bai
     */
    public final Card dealCard() {
        if (cardUseIndex < DEFAULT_CARD_NUMBER) {
            return cards.get(cardUseIndex++);
        } else {
            return null;
        }
    }

    /**
     * Chia bai theo number va type, dung de test
     *
     * @param number
     * @param type
     * @return Card
     */
    public final Card dealCard(int number, int type) {
        for (Card card : cards) {
            if (card.getCardNumber() == number && card.getCardType() == type) {
                return card;
            }
        }
        return null;
    }

    /**
     * lấy thông tin số con bài của bộ bài.
     *
     * @return số lượng con bài trong bộ bài
     */
    public final int length() {
        return cards.size();
    }

    public int getCardUseIndex() {
        return cardUseIndex;
    }

    public static final Card getCard(byte id) {
        return CARD_MAP.get(id);
    }
}
