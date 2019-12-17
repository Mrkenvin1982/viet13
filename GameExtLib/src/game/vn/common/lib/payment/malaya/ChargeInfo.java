/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.vn.common.lib.payment.malaya;

import java.util.List;

/**
 *
 * @author hanv
 */
public class ChargeInfo {
    private List<ChargeCardInfo> card;
    private List<ChargeBankingInfo> banking;
    private List<ChargePromotionSchedule> promotionSchedule;

    public List<ChargeCardInfo> getCard() {
        return card;
    }

    public void setCard(List<ChargeCardInfo> card) {
        this.card = card;
    }

    public List<ChargeBankingInfo> getBanking() {
        return banking;
    }

    public void setBanking(List<ChargeBankingInfo> banking) {
        this.banking = banking;
    }

    public List<ChargePromotionSchedule> getPromotionSchedule() {
        return promotionSchedule;
    }

    public void setPromotionSchedule(List<ChargePromotionSchedule> promotionSchedule) {
        this.promotionSchedule = promotionSchedule;
    }
}