/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.vn.common.lib.updateconfig.tournament;

/**
 * sử dụng để load từ db
 * @author tuanp
 */
public class BonusInforDB {
    private byte serviceId;
    private double betBoard;
    private String inforBonus;

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

    public String getInforBonus() {
        return inforBonus;
    }

    public void setInforBonus(String inforBonus) {
        this.inforBonus = inforBonus;
    }
    
}
