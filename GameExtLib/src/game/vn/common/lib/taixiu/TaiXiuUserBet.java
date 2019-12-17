/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.vn.common.lib.taixiu;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 *
 * @author hanv
 */
public class TaiXiuUserBet implements Serializable {
    private final String userId;
    private String username;
    private byte betChoice;
    private BigDecimal betMoney;
    private long time;

    public TaiXiuUserBet(String userId, byte betChoice, BigDecimal betMoney, long time) {
        this.userId = userId;
        this.betChoice = betChoice;
        this.betMoney = betMoney;
        this.time = time;
    }

    public String getUserId() {
        return userId;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public byte getBetChoice() {
        return betChoice;
    }

    public void setBetChoice(byte betChoice) {
        this.betChoice = betChoice;
    }

    public BigDecimal getBetMoney() {
        return betMoney;
    }

    public void setBetMoney(BigDecimal betMoney) {
        this.betMoney = betMoney;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }
}
