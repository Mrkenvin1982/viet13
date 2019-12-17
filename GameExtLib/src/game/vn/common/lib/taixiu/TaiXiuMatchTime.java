/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.vn.common.lib.taixiu;

/**
 *
 * @author hanv
 */
public class TaiXiuMatchTime {
    private long timeBetRemain;
    private long timeShowResultRemain;
    private int timeBetTotal;
    private int timeShowResultTotal;

    public void setTimeBetRemain(long timeBetRemain) {
        this.timeBetRemain = timeBetRemain;
    }

    public void setTimeShowResultRemain(long timeShowResultRemain) {
        this.timeShowResultRemain = timeShowResultRemain;
    }

    public void setTimeBetTotal(int timeBetTotal) {
        this.timeBetTotal = timeBetTotal;
    }

    public void setTimeShowResultTotal(int timeShowResultTotal) {
        this.timeShowResultTotal = timeShowResultTotal;
    }

}
