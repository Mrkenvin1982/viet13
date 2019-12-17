/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.vn.common.lib.poker.stakes;

/**
 *
 * @author 
 */
public class StakesRangeManager {

    private StakesRange low ;
    private StakesRange medium ;
    private StakesRange over ;
    //Thời gian chờ bắt đầu ván
    private int waitingTime;
    //Thời gian chờ hết 1 lượt(tlmn,..), 1 ván (bài cào,..)
    private int playingTime;
    //Thời gian hiển thị kết quả khi kết thúc ván
    private int resultTime;  
    //thứ tự ưu tiên của game
    private int priority=0;
    private byte moneyType = 0;

    public StakesRange getLow() {
        return low;
    }

    public void setLow(StakesRange low) {
        this.low = low;
    }

    public StakesRange getMedium() {
        return medium;
    }

    public void setMedium(StakesRange medium) {
        this.medium = medium;
    }

    public StakesRange getOver() {
        return over;
    }

    public void setOver(StakesRange over) {
        this.over = over;
    }

    public int getWaitingTime() {
        return waitingTime;
    }

    public void setWaitingTime(int waitingTime) {
        this.waitingTime = waitingTime;
    }

    public int getPlayingTime() {
        return playingTime;
    }

    public void setPlayingTime(int playingTime) {
        this.playingTime = playingTime;
    }

    public int getResultTime() {
        return resultTime;
    }

    public void setResultTime(int resultTime) {
        this.resultTime = resultTime;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public byte getMoneyType() {
        return moneyType;
    }

    public void setMoneyType(byte moneyType) {
        this.moneyType = moneyType;
    }

}
