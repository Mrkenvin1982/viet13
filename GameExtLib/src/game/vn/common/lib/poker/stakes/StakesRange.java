/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.vn.common.lib.poker.stakes;

import java.util.ArrayList;
import java.util.List;

/**
 * Danh sách các  mức stack
 * @author tuanp
 */
public class StakesRange {
    
    private double minStack;
    private double maxStack;
    private List<Stakes> stakes;
    
    public StakesRange(){
        stakes = new ArrayList<>();
    }

    public double getMinStack() {
        return minStack;
    }

    public void setMinStack(double minStack) {
        this.minStack = minStack;
    }

    public double getMaxStack() {
        return maxStack;
    }

    public void setMaxStack(double maxStack) {
        this.maxStack = maxStack;
    }

    public List<Stakes> getStakes() {
        return stakes;
    }

    public void setStakes(List<Stakes> stakes) {
        this.stakes = stakes;
    }
    
    
    
}
