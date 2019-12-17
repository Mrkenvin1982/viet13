/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package game.vn.game.maubinh.object;

import game.vn.common.card.object.Card;
import game.vn.game.maubinh.MauBinhConfig;


/**
 *
 * @author binhnt
 */
public class LastSet extends BigSet {
    
    public LastSet() {
        super();
    }
    
    /**
     * Calculate win chi.
     * @return number of win chi.
     */
    @Override
    public int getWinChi() {
        switch (this.getType()) {
            case SetType.NOT_ENOUGH_CARD:
                return MauBinhConfig.RESULT_ERROR;
            case SetType.FOUR_OF_KIND: // Tu quy.
                return this.getWinChiOfFourOfKind();
            case SetType.STRAIGHT_FLUSH: // Thung pha sanh.
                return this.getWinChiOfStraightFlush();
            default:
                return MauBinhConfig.CHI_DEFAULT;
        }
    }
    
    private int getWinChiOfFourOfKind() {
        Card four = null;
        // Get card number of four.
        for (int i = 1; i < this.getCards().size(); i++) {
            if (this.getCards().get(i - 1).getCardNumber() == this.getCards().get(i).getCardNumber()) {
                four = this.getCards().get(i);
                break;
            }
        }

        // Tu quy A.
        if (four != null && MauBinhCardSet.isAce(four)) {
            return MauBinhConfig.getInstance().getChiLastFourOfKindAce();
        } else { // Tu quy thuong.
            return MauBinhConfig.getInstance().getChiLastFourOfKind();
        }
    }
    
    private int getWinChiOfStraightFlush() {
        // Sanh: 10JQKA or A2345.
        if (MauBinhCardSet.isAce(this.getCards().get(this.getCards().size() - 1))) {
            // A2345.
            if (MauBinhCardSet.is5(this.getCards().get(this.getCards().size() - 2))) {
                return MauBinhConfig.getInstance().getChiLastStraightFlushA2345();
            } else { // 10JQKA.
                return MauBinhConfig.getInstance().getChiLastStraightFlush10JQKA();
            }
        } else { // Sanh thuong.
            return MauBinhConfig.getInstance().getChiLastStraightFlush();
        }
    }
}
