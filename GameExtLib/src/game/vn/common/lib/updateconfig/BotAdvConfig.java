/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.vn.common.lib.updateconfig;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Bot cho game bai cao
 * @author 
 */
public class BotAdvConfig implements Serializable {
    
   private int ratio;//phần trăm xuất hiện bài lớn
   private int totalFund;//% thấp hơn so với tổng tiền của các bot 
   private int minPoint;//số điểm tối thiểu của bài lớn
   private BigDecimal revenueValue;

    public BigDecimal getRevenueValue() {
        return revenueValue;
    }

    public void setRevenueValue(BigDecimal revenueValue) {
        this.revenueValue = revenueValue;
    }

    public int getRatio() {
        return ratio;
    }

    public void setRatio(int ratio) {
        this.ratio = ratio;
    }

    public int getTotalFund() {
        return totalFund;
    }

    public void setTotalFund(int totalFund) {
        this.totalFund = totalFund;
    }

    public int getMinPoint() {
        return minPoint;
    }

    public void setMinPoint(int minPoint) {
        this.minPoint = minPoint;
    }

    @Override
    public String toString() {
        return "BotAdvConfig{" + "ratio=" + ratio + ", totalFund=" + totalFund + ", minPoint=" + minPoint + ", revenueValue=" + revenueValue + '}';
    }
}
