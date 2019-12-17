/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.vn.common.lib.api;

/**
 *
 * @author hanv
 */
public class UpdateMoneyInfo {
    private String userId;
    private int moneyType;
    private double value;
    private String desc;

    public UpdateMoneyInfo() {
    }

    public UpdateMoneyInfo(String userId, int moneyType, double value) {
        this.userId = userId;
        this.moneyType = moneyType;
        this.value = value;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public int getMoneyType() {
        return moneyType;
    }

    public void setMoneyType(int moneyType) {
        this.moneyType = moneyType;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

}