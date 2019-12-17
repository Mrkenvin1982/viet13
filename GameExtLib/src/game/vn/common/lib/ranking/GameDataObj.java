/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.vn.common.lib.ranking;

import java.io.Serializable;
import org.msgpack.annotation.Message;

/**
 *
 * @author anlh
 */
@Message
public class GameDataObj implements Serializable {
    
    private String winUserid;
    private String winUsername;
    private double betmoney;
    private double tax;
    private int service_id;
    private int bonusPoint;
    private long startTime;

    /**
     * @return the winUsername
     */
    public String getWinUsername() {
        return winUsername;
    }

    /**
     * @param winUsername the winUsername to set
     */
    public void setWinUsername(String winUsername) {
        this.winUsername = winUsername;
    }

    /**
     * @return the betmoney
     */
    public double getBetmoney() {
        return betmoney;
    }

    /**
     * @param betmoney the betmoney to set
     */
    public void setBetmoney(double betmoney) {
        this.betmoney = betmoney;
    }

    /**
     * @return the tax
     */
    public double getTax() {
        return tax;
    }

    /**
     * @param tax the tax to set
     */
    public void setTax(double tax) {
        this.tax = tax;
    }

    /**
     * @return the service_id
     */
    public int getService_id() {
        return service_id;
    }

    /**
     * @param service_id the service_id to set
     */
    public void setService_id(int service_id) {
        this.service_id = service_id;
    }

    /**
     * @return the bonusPoint
     */
    public int getBonusPoint() {
        return bonusPoint;
    }

    /**
     * @param bonusPoint the bonusPoint to set
     */
    public void setBonusPoint(int bonusPoint) {
        this.bonusPoint = bonusPoint;
    }

    /**
     * @return the startTime
     */
    public long getStartTime() {
        return startTime;
    }

    /**
     * @param startTime the startTime to set
     */
    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    /**
     * @return the winUserid
     */
    public String getWinUserid() {
        return winUserid;
    }

    /**
     * @param winUserid the winUserid to set
     */
    public void setWinUserid(String winUserid) {
        this.winUserid = winUserid;
    }
    
}
