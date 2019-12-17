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
public class PointConvertConfig {
    private int turnOver;
    private int convertRate;
    private boolean enable;
    private boolean enableAutoConvert;
    private BigDecimal convertLimitPerDay;//hạn mức tối đa convert trong ngày
    private BigDecimal minConvertPerTime; //hạn mức tối thiểu mỗi lần convert
    private BigDecimal maxConvertPerTime;

    public int getTurnOver() {
        return turnOver;
    }

    public void setTurnOver(int turnOver) {
        this.turnOver = turnOver;
    }

    public int getConvertRate() {
        return convertRate;
    }

    public void setConvertRate(int convertRate) {
        this.convertRate = convertRate;
    }

    public boolean isEnable() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    public boolean isEnableAutoConvert() {
        return enableAutoConvert;
    }

    public void setEnableAutoConvert(boolean enableAutoConvert) {
        this.enableAutoConvert = enableAutoConvert;
    }

    public BigDecimal getConvertLimitPerDay() {
        return convertLimitPerDay;
    }

    public void setConvertLimitPerDay(BigDecimal covertLimitPerDay) {
        this.convertLimitPerDay = covertLimitPerDay;
    } 

    public BigDecimal getMinConvertPerTime() {
        return minConvertPerTime;
    }

    public void setMinConvertPerTime(BigDecimal minConvertPerTime) {
        this.minConvertPerTime = minConvertPerTime;
    }

    public BigDecimal getMaxConvertPerTime() {
        return maxConvertPerTime;
    }

    public void setMaxConvertPerTime(BigDecimal maxConvertPerTime) {
        this.maxConvertPerTime = maxConvertPerTime;
    }
}
