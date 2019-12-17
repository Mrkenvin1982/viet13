/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.vn.game.tienlen.utils;

import game.vn.util.Utils;
import java.math.BigDecimal;

/**
 * Xử lý tiền phạt trong bàn
 * @author tuanp
 */
public class BoardMoneyUtil {
    
    private static final BigDecimal PENALTY_2 = new BigDecimal("2.00");
    private static final BigDecimal PENALTY_3 = new BigDecimal("3.00");
    private static final BigDecimal PENALTY_4 = new BigDecimal("4.00");
    private static final BigDecimal PENALTY_5 = new BigDecimal("5.00");

    //tiền cược trong game
    private BigDecimal money = BigDecimal.ZERO;
    //tiền phạt khi rời bàn
    private BigDecimal moneyPot = BigDecimal.ZERO;

    public BoardMoneyUtil() {
    }
    
    public BigDecimal getMoneyPot() {
        return moneyPot;
    }
    
    public void addMoneyPot(BigDecimal money) {
        moneyPot = Utils.add(this.moneyPot, money);
    }
    
    public void setMoneyPot(BigDecimal moneyPot){
        this.moneyPot = moneyPot;
    }
    
    public BigDecimal getMoney() {
        return money;
    }

    public void setMoney(BigDecimal money) {
        this.money = money;
    }
    
    public BigDecimal getChatHeoDen(int count) {
        return Utils.multiply(money,new BigDecimal(String.valueOf(count)));
    }
    
     public BigDecimal getChatHeoDo(int count) {
         BigDecimal value = Utils.multiply(PENALTY_2, new BigDecimal(String.valueOf(count)));
                   value = Utils.multiply(value, money);
        return value;
    }
     
    public BigDecimal getThuiHeoDen(int count) {
        return Utils.multiply(money, new BigDecimal(String.valueOf(count)));
    }
    
    public BigDecimal getThuiHeoDo(int count) {
        BigDecimal value = Utils.multiply(PENALTY_2, new BigDecimal(String.valueOf(count)));
                   value = Utils.multiply(value, money);
                   return value;
    }
    
    public BigDecimal getChat3DoiThong(){
         return Utils.multiply(money, PENALTY_3);
    }
    
    public BigDecimal getThui3DoiThong(){
        return Utils.multiply(money, PENALTY_3);
    }
    
    public BigDecimal getChatTuQuyMoney(int count) {
        BigDecimal value = Utils.multiply(PENALTY_4, new BigDecimal(String.valueOf(count)));
                   value = Utils.multiply(value, money);
                   return value;
    }
    
    public BigDecimal getThuiTuQuyMoney(int count) {
         BigDecimal value = Utils.multiply(PENALTY_4, new BigDecimal(String.valueOf(count)));
                   value = Utils.multiply(value, money);
                   return value;
    }
    
    public BigDecimal getChatBonDoiThongMoney() {
        return Utils.multiply(money, PENALTY_5);
    }
    
    public BigDecimal getThuiBonDoiThongMoney() {
         return Utils.multiply(money, PENALTY_5);
    }
    
    public BigDecimal getCongMoney() {
         return  Utils.multiply(money, PENALTY_2);
    }
    
    public BigDecimal getForceFinishMoney() {
        return money;
    }

}
