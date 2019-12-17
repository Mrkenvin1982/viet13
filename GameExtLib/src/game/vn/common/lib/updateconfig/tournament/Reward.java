/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.vn.common.lib.updateconfig.tournament;

/**
 *
 * @author hanv
 */
public class Reward {
    private int id;
    private int multiplier;
    private double place1st; 
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

    public double getPlace1st() {
        return place1st;
    }

    public void setPlace1st(double place1st) {
        this.place1st = place1st;
    }

    public double getFrequency() {
        return frequency;
    }

    public void setFrequency(double frequency) {
        this.frequency = frequency;
    }    
}