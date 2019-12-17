/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package game.vn.common.card;

import game.vn.common.card.object.Card;
import java.util.Comparator;

/**
 *
 * @author hoanghh
 */
public class ComparatorByCardType implements Comparator<Card> {
    /**
     * So sánh 2 lá bài, so sánh chất trước theo thứ tự bích chuồn rô cơ, cùng chất thì con nhỏ đứng trước, lớn đứng sau
     * @param o1
     * @param o2
     * @return 
     */
    @Override
    public int compare(Card o1, Card o2) {
        if (o1.getCardType() < o2.getCardType()) {
            return -1;
        } else if (o1.getCardType() > o2.getCardType()) {
            return 1;
        } else {
            if (o1.getId() < o2.getId()) {
                return -1;
            }else {
                return 1;
            }
        }
    }
}
