/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.vn.game.lieng.object;

import game.vn.game.lieng.LiengController;
import game.vn.util.Utils;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author tuanp
 */
public class Round {
   //Key: player. Value: win player đã cược trong vòng
    private final Map<String, BigDecimal> mapPlayers = new HashMap<>();
    private final LiengController game;

    public Round(LiengController game) {
        this.game = game;
    }

    public void addBetMoney(String userId, BigDecimal money) {
        if (mapPlayers.containsKey(userId)) {
            money =Utils.add(money, mapPlayers.get(userId));
        }
        mapPlayers.put(userId, money);
    }

    /**
     * Trả về tổng tiền tố của vòng
     * 
     * @return
     */
    public BigDecimal getTotalBetMoney() {
        BigDecimal result = BigDecimal.ZERO;
        for (BigDecimal value : mapPlayers.values()) {
            result=Utils.add(result, value);
        }
        return result;
    }

    public boolean isContain(String userId) {
        return this.mapPlayers.containsKey(userId);
    }
    
    /**
     * Lấy ra số lượng người chơi trong vòng
     * @return 
     */
    public int getPlayersInGround()
    {
        return this.mapPlayers.size();
    } 
}
