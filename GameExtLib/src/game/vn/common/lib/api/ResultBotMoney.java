/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.vn.common.lib.api;

import java.math.BigDecimal;

/**
 *
 * @author minhvtd
 */
public class ResultBotMoney {
    public BigDecimal money = BigDecimal.ZERO;          // tổng tiền bot (SUM table sfs_user_money)
    public BigDecimal inputMoney = BigDecimal.ZERO;     // tổng tiền input cho bot (SUM table sfs_bot_money_log)
    public BigDecimal stackMoney = BigDecimal.ZERO;     // tổng tiền stack của bot (SUM table sfs_user_money_stack)
}
