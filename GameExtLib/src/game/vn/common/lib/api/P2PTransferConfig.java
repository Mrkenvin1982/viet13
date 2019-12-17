/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.vn.common.lib.api;

import java.math.BigDecimal;

/**
 *
 * @author minhvtd
 */
public class P2PTransferConfig {

    private double transferFee;
    private double transferFeeAgent;
    private BigDecimal minPerTrans;
    private BigDecimal maxPerTrans;
    private BigDecimal maxPerDay;
    private boolean enable;
    
    public double getTransferFee() {
        return transferFee;
    }

    public void setTransferFee(double transferFee) {
        this.transferFee = transferFee;
    }
    
     public double getTransferFeeAgent() {
        return transferFeeAgent;
    }

    public void setTransferFeeAgent(double transferFeeAgent) {
        this.transferFeeAgent = transferFeeAgent;
    }

    public BigDecimal getMinPerTrans() {
        return minPerTrans;
    }

    public void setMinPerTrans(BigDecimal minPerTrans) {
        this.minPerTrans = minPerTrans;
    }

    public BigDecimal getMaxPerTrans() {
        return maxPerTrans;
    }

    public void setMaxPerTrans(BigDecimal maxPerTrans) {
        this.maxPerTrans = maxPerTrans;
    }

    public BigDecimal getMaxPerDay() {
        return maxPerDay;
    }

    public void setMaxPerDay(BigDecimal maxPerDay) {
        this.maxPerDay = maxPerDay;
    }

    public boolean isEnable() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }

}
