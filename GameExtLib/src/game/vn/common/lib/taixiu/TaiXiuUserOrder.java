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
public class TaiXiuUserOrder {
    private final String userId;
    private String username;
    private final byte order;
    private final BigDecimal win;

    public TaiXiuUserOrder(String userId, byte order, BigDecimal win) {
        this.userId = userId;
        this.order = order;
        this.win = win;
    }

    public String getUserId() {
        return userId;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
