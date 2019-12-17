/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package game.vn.game.maubinh.object;

import com.smartfoxserver.v2.entities.User;
import game.vn.common.card.object.Card;
import game.vn.util.Utils;
import java.math.BigDecimal;


/**
 *
 * @author binhnt
 */
public class Player {
    
    private User user = null;
    private Cards cards;
    // When one player leaves from game, then remaining players will receive bonus as game play.
    private BigDecimal bonusMoney = BigDecimal.ZERO;
    private int bonusChi;
    
    private boolean isFinish;
    private boolean isUsedAutoArrangement;
    private boolean timeOut;
    //Sử dụng để mô tả khi sập hầm
    private String decriptionSapHam="";
    public Player() {
        this.reset();
    }

    /**
     * Reset all cards and info of this player.
     */
    public void reset() {
        // Reset all cards of this player.
        this.cards = new Cards();
        
        // Reset info status.
        this.bonusMoney = BigDecimal.ZERO;
        this.bonusChi = 0;
        this.isFinish = false;
        this.timeOut = false;
        this.isUsedAutoArrangement = false;
        decriptionSapHam="";
    }
    
    /**
     * get all current card of this player.
     * @return a list of card.
     */
    public Cards getCards() {
        return this.cards;
    }
    
    public User getUser() {
        return this.user;
    }
    
    public void setUser(User user) {
        this.user = user;
    }

    public boolean isTimeOut() {
        return timeOut;
    }

    public void setIsTimeOut(boolean timeOut) {
        this.timeOut = timeOut;
    }
    
    public BigDecimal getBonusMoney() {
        return this.bonusMoney;
    }
    
    public void addBonusMoney(BigDecimal value) {
        this.bonusMoney = Utils.add(this.bonusMoney, value);
    }
    
    public int getBonusChi() {
        return this.bonusChi;
    }
    
    public void addBonusChi(int chi) {
        this.bonusChi += chi;
    }
    
    public boolean isFinish() {
        return this.isFinish;
    }
    
    public void setFinishFlag(boolean value) {
        this.isFinish = value;
    }
    
    public boolean isUsedAutoArrangement() {
        return this.isUsedAutoArrangement;
    }
    
    public void setAutoArrangementFlag(boolean value) {
        this.isUsedAutoArrangement = value;
    }
    
    public void addDecriptionSapHam(String dec) {
        decriptionSapHam += dec;
    }
    
    public String getDecriptionSapHam() {
        return decriptionSapHam;
    }

    @Override
    public String toString() {
        String infor =cards.getMauBinhType()+": ";
        for(Card card : cards.getCards()){
            infor += card.getCardNumber() +", "+getCardType(card) +"\n";
        }
        return infor;
    }
    
    private String getCardType(Card card){
        // Check type of card.
            switch (card.getCardType()) {
                case MauBinhCardSet.TYPE_HEART:
                    return " Heart";
                case MauBinhCardSet.TYPE_DIAMOND:
                    return " Diamond";
                case MauBinhCardSet.TYPE_CLUB:
                    return " Club";
                case MauBinhCardSet.TYPE_SPADE:
                    return " Spade";
                default:
                    break;
            }
            return " Unknow";
    }
    
    
    
}
