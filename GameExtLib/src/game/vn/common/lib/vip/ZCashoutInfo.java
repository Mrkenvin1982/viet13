/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.vn.common.lib.vip;

/**
 *
 * @author anlh
 */
public class ZCashoutInfo {

    /**
     * @return the minCashoutZ
     */
    public long getMinCashoutZ() {
        return minCashoutZ;
    }

    /**
     * @param minCashoutZ the minCashoutZ to set
     */
    public void setMinCashoutZ(long minCashoutZ) {
        this.minCashoutZ = minCashoutZ;
    }
    
    private String userid;
    private int cashoutRate;
    private long currentZ;
    private Double monthQuota;    
    private long minCashoutZ;

    /**
     * @return the userid
     */
    public String getUserid() {
        return userid;
    }

    /**
     * @param userid the userid to set
     */
    public void setUserid(String userid) {
        this.userid = userid;
    }

    /**
     * @return the cashoutRate
     */
    public int getCashoutRate() {
        return cashoutRate;
    }

    /**
     * @param cashoutRate the cashoutRate to set
     */
    public void setCashoutRate(int cashoutRate) {
        this.cashoutRate = cashoutRate;
    }

    /**
     * @return the monthQuota
     */
    public Double getMonthQuota() {
        return monthQuota;
    }

    /**
     * @param monthQuota the monthQuota to set
     */
    public void setMonthQuota(Double monthQuota) {
        this.monthQuota = monthQuota;
    }

    /**
     * @return the currentZ
     */
    public long getCurrentZ() {
        return currentZ;
    }

    /**
     * @param currentZ the currentZ to set
     */
    public void setCurrentZ(long currentZ) {
        this.currentZ = currentZ;
    }
    
}
