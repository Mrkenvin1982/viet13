/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.vn.common.lib.taixiu;

import java.util.List;

/**
 *
 * @author hanv
 */
public class TaiXiuUserOrderList {
    private List<TaiXiuUserOrder> userOrderList;
    
    public TaiXiuUserOrderList(List<TaiXiuUserOrder> userOrderList) {
        this.userOrderList = userOrderList;
    }

    public List<TaiXiuUserOrder> getUserOrderList() {
        return userOrderList;
    }
}
