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
public class ChargeBankingInfo {
    private int id;
    private int vndFrom;
    private int vndTo;
    private double price;
    private int promotion;
    private int bank;
    private byte type;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
    
    public int getVndFrom() {
        return vndFrom;
    }

    public void setVndFrom(int vndFrom) {
        this.vndFrom = vndFrom;
    }

    public int getVndTo() {
        return vndTo;
    }

    public void setVndTo(int vndTo) {
        this.vndTo = vndTo;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public int getPromotion() {
        return promotion;
    }

    public void setPromotion(int promotion) {
        this.promotion = promotion;
    }

    public int getBank() {
        return bank;
    }

    public void setBank(int bank) {
        this.bank = bank;
    }

    public byte getType() {
        return type;
    }

    public void setType(byte type) {
        this.type = type;
    }
}
