/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.vn.common.object.boardhistory;

import game.vn.common.constant.MoneyContants;

/**
 * Lịch sử giao dịch của user
 * @author tuanp
 */
public class TransactionHistory {
    //mã giao dịch
    private int id;
    private int moneyType=MoneyContants.POINT;
    // type giao dịch
    private int typeTrans;
    //tiền của user
    private double money;
    //tiền thắng - thua
    private double value;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getMoneyType() {
        return moneyType;
    }

    public void setMoneyType(int moneyType) {
        this.moneyType = moneyType;
    }

    public int getTypeTrans() {
        return typeTrans;
    }

    public void setTypeTrans(int typeTrans) {
        this.typeTrans = typeTrans;
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
    
    
}
