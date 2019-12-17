/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.vn.common.lib.poker.stakes;

import java.util.ArrayList;
import java.util.List;

/**
 * small blind va big blind cua ban
 * @author tuanp
 */
public class Stakes {
    
    private double smallBlind;
    private double bigBlind;
    //danh sách range buy-in
    private List<Double> ranges;
    //Mức thuế của bàn
    private Rake rake;
    //số người hổ trợ trong bàn(6,9 người)
    private List<Byte> seats;
    
    public Stakes(){
        this.ranges = new ArrayList<>();
    }

    public double getSmallBlind() {
        return smallBlind;
    }

    public void setSmallBlind(double smallBlind) {
        this.smallBlind = smallBlind;
    }

    public double getBigBlind() {
        return bigBlind;
    }

    public void setBigBlind(double bigBlind) {
        this.bigBlind = bigBlind;
    }

    public List<Double> getRanges() {
        return ranges;
    }

    public void setRanges(List<Double> ranges) {
        this.ranges = ranges;
    }

    public Rake getRake() {
        return rake;
    }

    public void setRake(Rake rake) {
        this.rake = rake;
    }

    public List<Byte> getSeats() {
        return seats;
    }

    public void setSeats(List<Byte> seats) {
        this.seats = seats;
    }
    
}
