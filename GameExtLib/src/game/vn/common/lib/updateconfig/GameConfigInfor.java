/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.vn.common.lib.updateconfig;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author tuanp
 */
public class GameConfigInfor {
    //id game
    private int serviceId;
    //loai tiền: point, money
    private byte moneyType;
    //danh sách mức cược trong game
    private List<Double> bets;
    //số tiền tối thiểu vào bàn của nhà cái
    private int minJoinOwner;
    //số tiền tối thiểu để vào bàn
    private int minJoin;
    //số lần tiền đặt cược tối đa cho game đặt cược
    private int maxBet;
    //số lượng user tối đa trong bàn
    private int noPlayer;
    //số tiền phạt khi rời bàn
    private int penalize;
    //Thời gian chờ bắt đầu ván
    private int waitingTime;
    //Thời gian chờ hết 1 lượt(tlmn,..), 1 ván (bài cào,..)
    private int playingTime;
    //Thời gian hiển thị kết quả khi kết thúc ván
    private int resultTime;  
    //thứ tự ưu tiên của game
    private int priority=0;
    //thuế của game
    private int tax;

    public GameConfigInfor(){
        bets= new ArrayList<>();
    }
    public int getServiceId() {
        return serviceId;
    }

    public void setServiceId(int serviceId) {
        this.serviceId = serviceId;
    }

    public byte getMoneyType() {
        return moneyType;
    }

    public void setMoneyType(byte moneyType) {
        this.moneyType = moneyType;
    }

    public List<Double> getBets() {
        return bets;
    }

    public void setBets(List<Double> bets) {
        this.bets = bets;
    }

    public int getMinJoin() {
        return minJoin;
    }

    public void setMinJoin(int minJoin) {
        this.minJoin = minJoin;
    }

    public int getMaxBet() {
        return maxBet;
    }

    public void setMaxBet(int maxBet) {
        this.maxBet = maxBet;
    }

    public int getNoPlayer() {
        return noPlayer;
    }

    public void setNoPlayer(int noPlayer) {
        this.noPlayer = noPlayer;
    }

    public int getPenalize() {
        return penalize;
    }

    public void setPenalize(int penalize) {
        this.penalize = penalize;
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

    public int getTax() {
        return tax;
    }

    public void setTax(int tax) {
        this.tax = tax;
    }

    public int getMinJoinOwner() {
        return minJoinOwner;
    }

    public void setMinJoinOwner(int minJoinOwner) {
        this.minJoinOwner = minJoinOwner;
    }
    
    
}
