/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.vn.common.tournament;

/**
 *
 * @author tuanp
 */
public class SumMoneyByBetBoard {
    private byte serviceId;
    private double betBoard;
    private double sumMoney;

    public byte getServiceId() {
        return serviceId;
    }

    public void setServiceId(byte serviceId) {
        this.serviceId = serviceId;
    }

    public double getBetBoard() {
        return betBoard;
    }

    public void setBetBoard(double betBoard) {
        this.betBoard = betBoard;
    }

    public double getSumMoney() {
        return sumMoney;
    }

    public void setSumMoney(double sumMoney) {
        this.sumMoney = sumMoney;
    }
 
}
