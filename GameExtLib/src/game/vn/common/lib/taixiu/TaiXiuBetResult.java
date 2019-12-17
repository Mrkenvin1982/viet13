/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.vn.common.lib.taixiu;

import java.math.BigDecimal;

/**
 *
 * @author hanv
 */
public class TaiXiuBetResult {
    public static final byte CODE_FAIL = 0;
    public static final byte CODE_SUCCESS = 1;
    public static final byte CODE_FAIL_OVER_TIME = 2;
    public static final byte CODE_FAIL_GAME_MAINTAINING = 3;
    public static final byte CODE_FAIL_MIN_BET_MONEY = 4;
    public static final byte CODE_FAIL_MAX_BET_MONEY = 5;
    public static final byte CODE_FAIL_CONTINUOUSLY = 6;
    public static final byte CODE_FAIL_BET_TWO_SIDE = 7;
    public static final byte CODE_FAIL_INVALID_BET_MONEY = 8;
    public static final byte CODE_FAIL_NOT_ENOUGH_MONEY = 9;

    private byte code;
    private String message;
    private final String userId;
    private String username;
    private byte betChoice;
    private BigDecimal betMoney;
    private BigDecimal userBetMoney;
    private BigDecimal totalBetMoney;
    private long totalUserBet;
    private BigDecimal minBet;
    private BigDecimal maxBet;
    private long time;

    public TaiXiuBetResult(String userId, byte betChoice, BigDecimal betMoney) {
        this.userId = userId;
        this.betChoice = betChoice;
        this.betMoney = betMoney;
    }

    public byte getCode() {
        return code;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setCode(byte code) {
        this.code = code;
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

    public BigDecimal getUserBetMoney() {
        return userBetMoney;
    }

    public void setUserBetMoney(BigDecimal userBetMoney) {
        this.userBetMoney = userBetMoney;
    }

    public BigDecimal getTotalBetMoney() {
        return totalBetMoney;
    }

    public void setTotalBetMoney(BigDecimal totalBetMoney) {
        this.totalBetMoney = totalBetMoney;
    }

    public long getTotalUserBet() {
        return totalUserBet;
    }

    public void setTotalUserBet(long totalUserBet) {
        this.totalUserBet = totalUserBet;
    }

    public BigDecimal getMinBet() {
        return minBet;
    }

    public void setMinBet(BigDecimal minBet) {
        this.minBet = minBet;
    }

    public BigDecimal getMaxBet() {
        return maxBet;
    }

    public void setMaxBet(BigDecimal maxBet) {
        this.maxBet = maxBet;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public boolean isSuccess() {
        return this.code == CODE_SUCCESS;
    }
}
