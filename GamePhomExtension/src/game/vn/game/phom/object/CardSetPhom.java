/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.vn.game.phom.object;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Class chua thong tin cua bo bai.
 *
 * @author tuanp
 */
public class CardSetPhom {

    /**
     * mac dinh so luong con bai trong bo bai. <br>
     * 1 bo bai co 52 con
     */
    public static final int DEFAULT_CARD_NUMBER = 52;
    public static final CardPhom[] CARD_PHOMS;

    static {
        CARD_PHOMS = new CardPhom[DEFAULT_CARD_NUMBER];
        for (byte i = 0; i < CARD_PHOMS.length; i++) {
            CARD_PHOMS[i] = new CardPhom(i);
        }
    }
    private static List<CardPhom> listCardsUsed = new ArrayList<>();
    /**
     * List các con bài trong bộ bài.
     */
    private final transient ArrayList<CardPhom> cards = new ArrayList<>();
    /**
     * vị trí con bài đang sử dụng trong bộ bài.
     */
    private transient int cardUseIndex;

    /**
     * tạo bộ bài mới, luôn luôn gồm 52 là từ 0 đến 52, qui định 0 là con bài
     * nhỏ nhất, 51 là con bài to nhất.
     *
     * @param numberCard so luong con bai trong bo bai.
     */
    public CardSetPhom(final int numberCard) {
        for (CardPhom cardPhom : CARD_PHOMS) {
            cards.add(cardPhom);
        }
    }

    /**
     * đảo thứ tự của các con bài trong bộ bài đi.
     */
    public final void xaoBai() {
        Collections.shuffle(cards);
        cardUseIndex = 0;
        listCardsUsed.clear();
    }

    /**
     * Chia bài.
     *
     * @return con bài hiện tại của bộ bài
     */
    public final CardPhom dealCard() {
        if (this.cardUseIndex >= DEFAULT_CARD_NUMBER
                || this.cardUseIndex < 0) {
            return null;
        }
        CardPhom c = cards.get(cardUseIndex++);
        return c;
    }

    /**
     * Chia bai theo number va type, dung de test
     *
     * @param number
     * @param type
     * @return Card
     */
    public final CardPhom dealCard(int number, int type) {
        for (CardPhom card : cards) {
            if (card.getCardNumber() == number && card.getCardType() == type && !listCardsUsed.contains(card)) {
                listCardsUsed.add(card);
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

    public boolean hasCard() {
        return this.cardUseIndex < DEFAULT_CARD_NUMBER;
    }
}
