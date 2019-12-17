/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.vn.common.lib.updateconfig.tournament;

import java.util.List;

/**
 * Config game tournament
 * @author tuanp
 */
public class GameTournamentConfigInfor {
    //danh sách mức cược trong game
    private List<Ticket> tickets;
    //số lượng user tối đa trong bàn
    private int noPlayer;
    //thứ tự ưu tiên của game
    private int priority=0;
    //thời gian xoay giải thưởng
    private int spinTime;
    //Thời gian chờ bắt đầu ván
    private int waitingTime;
    //Thời gian chờ hết 1 lượt(tlmn,..), 1 ván (bài cào,..)
    private int playingTime;
    //Thời gian hiển thị kết quả khi kết thúc ván
    private int resultTime; 
    //so ván thắng
    private int winning;  
    private double totalFund;
    private int serviceId;

    public List<Ticket> getTickets() {
        return tickets;
    }

    public void setTickets(List<Ticket> tickets) {
        this.tickets = tickets;
    }

    public int getNoPlayer() {
        return noPlayer;
    }

    public void setNoPlayer(int noPlayer) {
        this.noPlayer = noPlayer;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public int getSpinTime() {
        return spinTime;
    }

    public void setSpinTime(int spinTime) {
        this.spinTime = spinTime;
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

    public double getTotalFund() {
        return totalFund;
    }

    public void setTotalFund(double totalFund) {
        this.totalFund = totalFund;
    }

    public int getWinning() {
        return winning;
    }

    public void setWinning(int winning) {
        this.winning = winning;
    }

    public int getServiceId() {
        return serviceId;
    }

    public void setServiceId(int serviceId) {
        this.serviceId = serviceId;
    }

}
