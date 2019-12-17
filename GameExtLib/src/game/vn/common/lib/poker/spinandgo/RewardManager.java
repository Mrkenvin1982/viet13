/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.vn.common.lib.poker.spinandgo;

import java.util.HashMap;
import java.util.Map;

/**
 * Quản lý tất cả reward theo mức cược
 * @author
 */
public class RewardManager {
    Map<Double, Reward> rewards;
    //so luong player de start spin and go
    private byte noPlayer;

    public RewardManager() {
        rewards = new  HashMap<>();
    }

    public Reward getReward(double betBoard) {
        return rewards.get(betBoard);
    }

    public void setRewards(Map<Double, Reward> rewards) {
        this.rewards = rewards;
    }
    
    public void putRewards(double betBoard, Reward reward){
        this.rewards.put(betBoard, reward);
    }

    public Map<Double, Reward> getRewards() {
        return rewards;
    }

    public byte getNoPlayer() {
        return noPlayer;
    }

    public void setNoPlayer(byte noPlayer) {
        this.noPlayer = noPlayer;
    }


}
