/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.vn.common.lib.taixiu;

/**
 *
 * @author anlh
 */
public class TaiXiuWinStreakData {

    /**
     * @return the moneyType
     */
    public int getMoneyType() {
        return moneyType;
    }

    /**
     * @param moneyType the moneyType to set
     */
    public void setMoneyType(int moneyType) {
        this.moneyType = moneyType;
    }

    /**
     * @return the userId
     */
    public String getUserId() {
        return userId;
    }

    /**
     * @param userId the userId to set
     */
    public void setUserId(String userId) {
        this.userId = userId;
    }

    /**
     * @return the streak
     */
    public int getStreak() {
        return streak;
    }

    /**
     * @param streak the streak to set
     */
    public void setStreak(int streak) {
        this.streak = streak;
    }
    
    private String userId;
    private int streak;
    private int moneyType;

    public TaiXiuWinStreakData() {
    }

    public TaiXiuWinStreakData(String userId, int streak, int moneyType) {
        this.userId = userId;
        this.streak = streak;
        this.moneyType = moneyType;
    }
    
}
