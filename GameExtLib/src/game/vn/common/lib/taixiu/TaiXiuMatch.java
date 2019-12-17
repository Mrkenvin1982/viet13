/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.vn.common.lib.taixiu;

import java.io.Serializable;
import java.util.List;

/**
 *
 * @author hanv
 */
public class TaiXiuMatch implements Serializable {
    private int id;
    private long time;
    private final byte[] dice = new byte[3];
    private byte result;
    private List<TaiXiuUserBet> userBets;

    public TaiXiuMatch(int id, byte dice1, byte dice2, byte dice3, long time) {
        this.id = id;
        this.dice[0] = dice1;
        this.dice[1] = dice2;
        this.dice[2] = dice3;
        this.time = time;
    }
    
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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
     * @return the result
     */
    public byte getResult() {
        return result;
    }

    public List<TaiXiuUserBet> getUserBets() {
        return userBets;
    }

    public void setUserBets(List<TaiXiuUserBet> userBets) {
        this.userBets = userBets;
    }
    
    public byte getDice(int i) {
        return dice[i];
    }

    public void updateResult() {
        result = dice[0] + dice[1] + dice[2] >= 11 ? (byte)0 : (byte)1;
    }

}