/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.vn.common.lib.updateconfig.tournament;

/**
 * Thông tin mỗi vé tour
 * @author tuanp
 */
public class BonusInfor {
    
    //hệ số nhân
    private int multiply;
    //số lần xuất hiện trong 1 chu kỳ
    private int rate;

    public int getMultiply() {
        return multiply;
    }

    public void setMultiply(int multiply) {
        this.multiply = multiply;
    }

    public int getRate() {
        return rate;
    }

    public void setRate(int rate) {
        this.rate = rate;
    }
    
    
}
