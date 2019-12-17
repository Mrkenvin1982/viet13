/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.vn.common.lib.payment.malaya;

/**
 *
 * @author hanv
 */
public class ChargePromotionTime {
    private String startTime;
    private String endTime;

    public ChargePromotionTime() {
    }

    public ChargePromotionTime(String startTime, String endTime) {
        this.startTime = startTime;
        this.endTime = endTime;
    }
    
    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

}
