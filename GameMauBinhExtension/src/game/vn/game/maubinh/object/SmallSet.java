/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package game.vn.game.maubinh.object;

import game.vn.game.maubinh.MauBinhConfig;


/**
 *
 * @author binhnt
 */
public class SmallSet extends Set {
    
    private boolean isStraight;
    private boolean isFlush;

    public SmallSet() {
        super(MauBinhConfig.NUMBER_CARD_SMALL_SET);
        this.isFlush = false;
        this.isStraight = false;
    }
    
    @Override
    public boolean isFlush() {
        return this.isFlush;
    }
    
    @Override
    public boolean isStraight() {
        return this.isStraight;
    }
    
    @Override
    protected void setType() {
        super.setType();

        switch (this.getType()) {
            case SetType.NOT_ENOUGH_CARD:
            case SetType.HIGH_CARD:
            case SetType.ONE_PAIR:
            case SetType.THREE_OF_KIND:
                break;

            case SetType.STRAIGHT:
                this.isStraight = true;
                this.setType(SetType.HIGH_CARD);
                break;

            case SetType.FLUSH:
                this.isFlush = true;
                this.setType(SetType.HIGH_CARD);
                break;

            case SetType.STRAIGHT_FLUSH:
                this.isStraight = true;
                this.isFlush = true;
                this.setType(SetType.HIGH_CARD);
                break;

            default:
                this.setType(SetType.HIGH_CARD);
                break;
        }
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
            case SetType.THREE_OF_KIND: // Xam chi.
                return getWinChiThreeOfKind();
            default:
                return MauBinhConfig.CHI_DEFAULT;
        }
    }
 
    private int getWinChiThreeOfKind() {
        // Xam A.
        if (MauBinhCardSet.isAce(this.getCards().get(0))) {
            return MauBinhConfig.getInstance().getChiFirstThreeOfKindAce();
        } else { // Xam thuong.
            return MauBinhConfig.getInstance().getChiFirstThreeOfKind();
        }
    }
}
