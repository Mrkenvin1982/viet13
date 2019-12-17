/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.vn.common.lib.taixiu;

import java.math.BigDecimal;
import java.util.List;

/**
 *
 * @author hanv
 */
public class TaiXiuGameInfo {
    private long timeBetRemain;
    private long timeShowResultRemain;
    private int totalUserTai;
    private int totalUserXiu;
    private BigDecimal totalMoneyTai;
    private BigDecimal totalMoneyXiu;
    private BigDecimal moneyTai;
    private BigDecimal moneyXiu;
    private final byte[] dice = new byte[3];
    private byte result;
    private BigDecimal moneyReceived;
    private BigDecimal minBet;
    private BigDecimal maxBet;
    private Byte[] historyList;
    private int timeBetTotal;
    private int timeShowResultTotal;
    private int matchId;
    private BigDecimal jarMoney;
    private BigDecimal ticketPrice;
    private List<BigDecimal> betMoneys;

    public long getTimeBetRemain() {
        return timeBetRemain;
    }

    public void setTimeBetRemain(long timeBetRemain) {
        this.timeBetRemain = timeBetRemain;
    }

    public void setTimeShowResultRemain(long timeShowResultRemain) {
        this.timeShowResultRemain = timeShowResultRemain;
    }

    public void setTotalUserTai(int totalUserTai) {
        this.totalUserTai = totalUserTai;
    }

    public void setTotalUserXiu(int totalUserXiu) {
        this.totalUserXiu = totalUserXiu;
    }

    public void setTotalMoneyTai(BigDecimal totalMoneyTai) {
        this.totalMoneyTai = totalMoneyTai;
    }

    public void setTotalMoneyXiu(BigDecimal totalMoneyXiu) {
        this.totalMoneyXiu = totalMoneyXiu;
    }

    public void setMoneyTai(BigDecimal moneyTai) {
        this.moneyTai = moneyTai;
    }

    public void setMoneyXiu(BigDecimal moneyXiu) {
        this.moneyXiu = moneyXiu;
    }

    public void setDice(byte dice1, byte dice2, byte dice3) {
        this.dice[0] = dice1;
        this.dice[1] = dice2;
        this.dice[2] = dice3;
    }

    public void setResult(byte result) {
        this.result = result;
    }

    public BigDecimal getMoneyReceived() {
        return moneyReceived;
    }

    public void setMoneyReceived(BigDecimal moneyReceived) {
        this.moneyReceived = moneyReceived;
    }

    public void setMinBet(BigDecimal minBet) {
        this.minBet = minBet;
    }

    public void setMaxBet(BigDecimal maxBet) {
        this.maxBet = maxBet;
    }

    public void setHistoryList(Byte[] historyList) {
        this.historyList = historyList;
    }

    public void setTimeBetTotal(int timeBetTotal) {
        this.timeBetTotal = timeBetTotal;
    }

    public void setTimeShowResultTotal(int timeShowResultTotal) {
        this.timeShowResultTotal = timeShowResultTotal;
    }

    public void setMatchId(int matchId) {
        this.matchId = matchId;
    }

    public void setJarMoney(BigDecimal jarMoney) {
        this.jarMoney = jarMoney;
    }

    public void setTicketPrice(BigDecimal ticketPrice) {
        this.ticketPrice = ticketPrice;
    }

    public List<BigDecimal> getBetMoneys() {
        return betMoneys;
    }

    public void setBetMoneys(List<BigDecimal> betMoneys) {
        this.betMoneys = betMoneys;
    }

}
