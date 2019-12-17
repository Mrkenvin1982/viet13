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
public class TaiXiuBuyTicketResult {
    public static final byte CODE_SUCCESS = 1;
    public static final byte CODE_FAIL = 0;
    public static final byte CODE_FAIL_OVER_TIME = 2;
    public static final byte CODE_FAIL_GAME_MAINTAINING = 3;
    public static final byte CODE_FAIL_CONTINUOUSLY = 4;
    public static final byte CODE_FAIL_NOT_ENOUGH_MONEY = 5;
    
    private byte code = CODE_SUCCESS;
    private String message;
    private final String userId;
    private String username;
    private final byte[] dice = new byte[3];
    private BigDecimal minBet;
    private BigDecimal maxBet;
    private BigDecimal jarMoney;
    private BigDecimal jarAddMoney;

    public TaiXiuBuyTicketResult(String userId) {
        this.userId = userId;
    }

    public TaiXiuBuyTicketResult(String userId, byte dice1, byte dice2, byte dice3) {
        this.userId = userId;
        this.dice[0] = dice1;
        this.dice[1] = dice2;
        this.dice[2] = dice3;
    }

    public String getUserId() {
        return userId;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public byte getCode() {
        return code;
    }

    public void setCode(byte code) {
        this.code = code;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setMinBet(BigDecimal minBet) {
        this.minBet = minBet;
    }

    public void setMaxBet(BigDecimal maxBet) {
        this.maxBet = maxBet;
    }

    public void setJarMoney(BigDecimal jarMoney) {
        this.jarMoney = jarMoney;
    }

    public void setJarAddMoney(BigDecimal jarAddMoney) {
        this.jarAddMoney = jarAddMoney;
    }
    
    public boolean isSuccess() {
        return code == CODE_SUCCESS;
    }
}
