/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.vn.common.lib.updateconfig;

import java.util.ArrayList;
import java.util.List;

/**
 * danh sách pot trong tất cả các game
 * @author tuanp
 */
public class BotList {
    
    private List<BotAdvConfig> bots = new ArrayList<>();
    private double currentMoneySum;
    private double beginMoneySum;

    public List<BotAdvConfig> getBots() {
        return bots;
    }

    public void setBots(List<BotAdvConfig> bots) {
        this.bots = bots;
    }

    public double getCurrentMoneySum() {
        return currentMoneySum;
    }

    public void setCurrentMoneySum(double currentMoneySum) {
        this.currentMoneySum = currentMoneySum;
    }

    public double getBeginMoneySum() {
        return beginMoneySum;
    }

    public void setBeginMoneySum(double beginMoneySum) {
        this.beginMoneySum = beginMoneySum;
    }

}
