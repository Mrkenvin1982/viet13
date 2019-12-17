/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.vn.common.lib.poker.spinandgo;

/**
 * Giai thưởng và tỉ lệ chi tiết theo từng multiplier
 * @author
 */
public class RewardMulti {
    private int id;
     //hệ số nhân giải thưởng
    private int multiplier;
    //tỉ lệ trả thưởng người thắng nhất
    private double place_1st; 
    //tỉ lệ trả thưởng người thắng nhì
    private double place_2nd ;
    //tỉ lệ trả thưởng người thắng ba
    private double place_3rd ;
    //tỉ lệ xuất hiện
    private double frequency;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getMultiplier() {
        return multiplier;
    }

    public void setMultiplier(int multiplier) {
        this.multiplier = multiplier;
    }

    public double getPlace_1st() {
        return place_1st;
    }

    public void setPlace_1st(double place_1st) {
        this.place_1st = place_1st;
    }

    public double getPlace_2nd() {
        return place_2nd;
    }

    public void setPlace_2nd(double place_2nd) {
        this.place_2nd = place_2nd;
    }

    public double getPlace_3rd() {
        return place_3rd;
    }

    public void setPlace_3rd(double place_3rd) {
        this.place_3rd = place_3rd;
    }

    public double getFrequency() {
        return frequency;
    }

    public void setFrequency(double frequency) {
        this.frequency = frequency;
    }
    
}
