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
public class Transaction {
    public static final byte TYPE_INAPP = 0;
    public static final byte TYPE_FREE = 1;
    public static final byte TYPE_CARD = 2;
    public static final byte TYPE_BTC = 3;
    public static final byte TYPE_TRANSFER = 4;
    public static final byte TYPE_DEPOSIT = 5;
    public static final byte TYPE_WITHDRAW = 6;
    public static final byte TYPE_REFUND = 7;
    public static final byte TYPE_CONVERT = 8;

    public static final byte STATUS_FAIL = 0;
    public static final byte STATUS_SUCCESS = 1;
    public static final byte STATUS_PENDING = 2;

    private String id;
    private int type;
    private double money;
    private double value;
    private long time;
    private byte status;

    public Transaction(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public double getMoney() {
        return money;
    }

    public void setMoney(double money) {
        this.money = money;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public byte getStatus() {
        return status;
    }

    public void setStatus(byte status) {
        this.status = status;
    }

}