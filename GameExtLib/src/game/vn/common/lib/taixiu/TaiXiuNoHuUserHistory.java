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
public class TaiXiuNoHuUserHistory {
    private int matchId;
    private long time;
    private final byte[] dice = new byte[3];
    private final byte[] result = new byte[3];

    private BigDecimal betMoney;
    private BigDecimal receivedMoney;

    public TaiXiuNoHuUserHistory(int matchId) {
        this.matchId = matchId;
    }

    public int getMatchId() {
        return matchId;
    }

    public void setMatchId(int matchId) {
        this.matchId = matchId;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public void setBetMoney(BigDecimal betMoney) {
        this.betMoney = betMoney;
    }

    public void setReceivedMoney(BigDecimal receivedMoney) {
        this.receivedMoney = receivedMoney;
    }
    
    public void setDice(byte dice1, byte dice2, byte dice3) {
        dice[0] = dice1;
        dice[1] = dice2;
        dice[2] = dice3;
    }
    
    public byte getDice(int i) {
        return dice[i];
    }

    public void setResult(byte dice1, byte dice2, byte dice3) {
        result[0] = dice1;
        result[1] = dice2;
        result[2] = dice3;
    }

    public boolean isNohu() {
        return dice[0] == result[0] && dice[1] == result[1] && dice[2] == result[2];
    }

}