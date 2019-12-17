/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.vn.common.lib.taixiu;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author hanv
 */
public class TaiXiuNohuData {
    private final int matchId;
    private final byte[] dice = new byte[3];
    private final BigDecimal nohuMoney;
    private final List<TaiXiuNohuUserData> data = new ArrayList<>();

    public TaiXiuNohuData(int matchId, byte dice1, byte dice2, byte dice3, BigDecimal nohuMoney) {
        this.matchId = matchId;
        this.dice[0] = dice1;
        this.dice[1] = dice2;
        this.dice[2] = dice3;
        this.nohuMoney = nohuMoney;
    }

    public void addUser(String userId, String username, BigDecimal money) {
        data.add(new TaiXiuNohuUserData(userId, username, money));
    }
}
