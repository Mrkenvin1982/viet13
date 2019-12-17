/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.vn.common.lib.payment.malaya;

/**
 *
 * @author hanv
 */
public class ChargeCardInfo {
    private int id;
    private int vnd;
    private int win;
    private int promotion;
    private int telco;
  
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
    
    public int getVnd() {
        return vnd;
    }

    public void setVnd(int vnd) {
        this.vnd = vnd;
    }

    public int getWin() {
        return win;
    }

    public void setWin(int win) {
        this.win = win;
    }

    public int getPromotion() {
        return promotion;
    }

    public void setPromotion(int promotion) {
        this.promotion = promotion;
    }

    public int getTelco() {
        return telco;
    }

    public void setTelco(int telco) {
        this.telco = telco;
    }

}
