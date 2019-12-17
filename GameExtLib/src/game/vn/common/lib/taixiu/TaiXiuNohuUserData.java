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
public class TaiXiuNohuUserData {
    private final String userId;
    private final BigDecimal money;
    private final String username;

    public TaiXiuNohuUserData(String userId, String username, BigDecimal money) {
        this.userId = userId;
        this.username = username;
        this.money = money;
    }
}
