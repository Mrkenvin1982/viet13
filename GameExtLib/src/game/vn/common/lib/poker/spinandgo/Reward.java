/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.vn.common.lib.poker.spinandgo;

import java.util.List;

/**
 * Điều chỉnh giải thưởng và tỉ lệ giải
 * @author 
 */
public class Reward {
    private int id;
    //gia vé
    private double bet;
   //trích % số tiền vé của user để bỏ vao quỷ trả tưởng
    private int fund;
    //thời giang nâng cược
    private int levelUpTime;
    private List<RewardMulti> rewardMulti;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public double getBet() {
        return bet;
    }

    public void setBet(double bet) {
        this.bet = bet;
    }

    public int getFund() {
        return fund;
    }

    public void setFund(int fund) {
        this.fund = fund;
    }

    public int getLevelUpTime() {
        return levelUpTime;
    }

    public void setLevelUpTime(int levelUpTime) {
        this.levelUpTime = levelUpTime;
    }

    public List<RewardMulti> getRewardMulti() {
        return rewardMulti;
    }

    public void setRewardMulti(List<RewardMulti> rewardMulti) {
        this.rewardMulti = rewardMulti;
    }
    
}
