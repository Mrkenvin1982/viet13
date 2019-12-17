/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.vn.common.lib.api;

/**
 *
 * @author minhvtd
 */
public class BotAdvantage {

    private int firstRatio;
    private int advRatio;
    private int minPoint;
    private boolean enable;

    public int getAdvRatio() {
        return advRatio;
    }

    public void setAdvRatio(int advRatio) {
        this.advRatio = advRatio;
    }

    public int getMinPoint() {
        return minPoint;
    }

    public void setMinPoint(int minPoint) {
        this.minPoint = minPoint;
    }

    public int getFirstRatio() {
        return firstRatio;
    }

    public void setFirstRatio(int firstRatio) {
        this.firstRatio = firstRatio;
    }

    public boolean isEnable() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }
 
}
