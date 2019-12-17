/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.vn.game.tienlentour.utils;

import game.vn.util.Utils;
import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * Xử lý tiền phạt trong bàn
 * @author tuanp
 */
public class BoardMoneyUtil {
    
    private static final BigDecimal PENALTY_2 = new BigDecimal("2.00");
    private static final BigDecimal PENALTY_3 = new BigDecimal("3.00");
    private static final BigDecimal PENALTY_5 = new BigDecimal("5.00");
    private static final BigDecimal PENALTY_6 = new BigDecimal("6.00");
    private static final BigDecimal PENALTY_12 = new BigDecimal("12.00");
    private static final BigDecimal PENALTY_13 = new BigDecimal("13.00");
    private static final BigDecimal PENALTY_16 = new BigDecimal("16.00");
    private static final BigDecimal PENALTY_20 = new BigDecimal("20.00");
    private static final BigDecimal PENALTY_26 = new BigDecimal("26.00");
    //tiền cược trong game
    private BigDecimal money = BigDecimal.ZERO;

    public BoardMoneyUtil() {
    }
    
    public BigDecimal getMoney() {
        return money;
    }

    public void setMoney(BigDecimal money) {
        this.money = money;
    }
    
    public BigDecimal getNormalMoney(int count) {
        return  Utils.multiply(money, new BigDecimal(String.valueOf(count)));
    }
    
    public BigDecimal getChatHeoDen(int count) {
        BigDecimal value = Utils.multiply(PENALTY_3, new BigDecimal(String.valueOf(count)));
                   value = Utils.multiply(value, money);
        return value;
    }
    
     public BigDecimal getChatHeoDo(int count) {
       BigDecimal value = Utils.multiply(PENALTY_6, new BigDecimal(String.valueOf(count)));
                   value = Utils.multiply(value, money);
        return value;
    }
     
     public BigDecimal getThuiHeoDen(int count) {
        BigDecimal value = Utils.multiply(PENALTY_2, new BigDecimal(String.valueOf(count)));
                   value = Utils.multiply(value, money);
        return value;
    }
    
    public BigDecimal getThuiHeoDo(int count) {
        BigDecimal value = Utils.multiply(PENALTY_5, new BigDecimal(String.valueOf(count)));
                   value = Utils.multiply(value, money);
        return value;
    }
    
    public BigDecimal getChat3DoiThong(){
         return Utils.multiply(money, PENALTY_12);
    }
    
    public BigDecimal getThui3DoiThong(){
        return Utils.multiply(money, PENALTY_6);
    }
    
    public BigDecimal getChatTuQuyMoney(int count) {
        BigDecimal value = Utils.multiply(PENALTY_16, new BigDecimal(String.valueOf(count)));
                   value = Utils.multiply(value, money);
        return value;
    }
    
    public BigDecimal getThuiTuQuyMoney(int count) {
        BigDecimal value = Utils.multiply(PENALTY_12, new BigDecimal(String.valueOf(count)));
                   value = Utils.multiply(value, money);
        return value;
    }
    
    public BigDecimal getChatBonDoiThongMoney() {
        return Utils.multiply(money, PENALTY_20);
    }
    
    public BigDecimal getThuiBonDoiThongMoney() {
         return Utils.multiply(money, PENALTY_12);
    }
    
    public BigDecimal getCongMoney() {
         return Utils.multiply(money, PENALTY_13);
    }
    
    public BigDecimal getForceFinishMoney(int count) {
        BigDecimal value = Utils.multiply(PENALTY_2, new BigDecimal(String.valueOf(count)));
                   value = Utils.multiply(value, money);
        return value;
    }
    
    public BigDecimal getMoneyLeaveGame(int count){
        BigDecimal value = Utils.multiply(PENALTY_26, new BigDecimal(String.valueOf(count)));
                   value = Utils.multiply(value, money);
        return value;
    }
    
}
