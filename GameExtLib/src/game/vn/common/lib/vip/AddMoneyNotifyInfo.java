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
public class AddMoneyNotifyInfo {

    //no effect
    public static final int NOT_SHOW = 0;
    //show popup
    public static final int SHOW_POPUP = 1;
    //show Toast
    public static final int SHOW_TOAST = 2;
    //show effect
    public static final int SHOW_EFFECT = 3;

    public static final byte UPDATE_TYPE_CHARGE = 0;
    public static final byte UPDATE_TYPE_CASHOUT = 1;
    public static final byte UPDATE_TYPE_CASHBACK = 2;
    
    //userid
    private String userid;
    //so cong
    private double value;
    //cau popup neu cos
    private String desc;
    //popup type, constant be tren
    private int popupType;
    //1 = realmoney, 2 = point
    private int moneyType;

    private byte updateType;

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
     * @return the value
     */
    public double getValue() {
        return value;
    }

    /**
     * @param value the value to set
     */
    public void setValue(double value) {
        this.value = value;
    }

    /**
     * @return the desc
     */
    public String getDesc() {
        return desc;
    }

    /**
     * @param desc the desc to set
     */
    public void setDesc(String desc) {
        this.desc = desc;
    }

    /**
     * @return the popupType
     */
    public int getPopupType() {
        return popupType;
    }

    /**
     * @param popupType the popupType to set
     */
    public void setPopupType(int popupType) {
        this.popupType = popupType;
    }

    /**
     * @return the moneyType
     */
    public int getMoneyType() {
        return moneyType;
    }

    /**
     * @param moneyType the moneyType to set
     */
    public void setMoneyType(int moneyType) {
        this.moneyType = moneyType;
    }

    public byte getUpdateType() {
        return updateType;
    }

    public void setUpdateType(byte updateType) {
        this.updateType = updateType;
    }

}
