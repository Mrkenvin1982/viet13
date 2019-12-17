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
public class TaiXiuUserHistory {
    private int matchId;
    private long time;
    private byte betChoice;
    private byte resultType;
    private String result;
    private BigDecimal betMoney;
    private BigDecimal receivedMoney;
    private BigDecimal returnMoney;
    private BigDecimal nohuMoney;
    private List tickets;

    /**
     * @return the matchId
     */
    public int getMatchId() {
        return matchId;
    }

    /**
     * @param matchId the matchId to set
     */
    public void setMatchId(int matchId) {
        this.matchId = matchId;
    }

    /**
     * @return the time
     */
    public long getTime() {
        return time;
    }

    /**
     * @param time the time to set
     */
    public void setTime(long time) {
        this.time = time;
    }

    /**
     * @return the betChoice
     */
    public byte getBetChoice() {
        return betChoice;
    }

    /**
     * @param betChoice the betChoice to set
     */
    public void setBetChoice(byte betChoice) {
        this.betChoice = betChoice;
    }

    /**
     * @return the result
     */
    public String getResult() {
        return result;
    }

    /**
     * @param result the result to set
     */
    public void setResult(String result) {
        this.result = result;
    }

    /**
     * @return the betMoney
     */
    public BigDecimal getBetMoney() {
        return betMoney;
    }

    /**
     * @param betMoney the betMoney to set
     */
    public void setBetMoney(BigDecimal betMoney) {
        this.betMoney = betMoney;
    }

    /**
     * @return the receivedMoney
     */
    public BigDecimal getReceivedMoney() {
        return receivedMoney;
    }

    /**
     * @param receivedMoney the receivedMoney to set
     */
    public void setReceivedMoney(BigDecimal receivedMoney) {
        this.receivedMoney = receivedMoney;
    }

    public BigDecimal getReturnMoney() {
        return returnMoney;
    }

    public void setReturnMoney(BigDecimal returnMoney) {
        this.returnMoney = returnMoney;
    }

    public void setResultType(byte resultType) {
        this.resultType = resultType;
    }
    
    public void setListNohuTicket(List tickets) {
        this.tickets = tickets;
    }

    public void setNohuMoney(BigDecimal nohuMoney) {
        this.nohuMoney = nohuMoney;
    }
}