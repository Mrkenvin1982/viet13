/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package game.vn.game.sam.utils;

import game.vn.util.Utils;
import java.math.BigDecimal;


/**
 * Xử lý tiền phạt trong bàn
 * @author chinhld
 */
public class BoardMoneyUtil {
    
    private static final BigDecimal PENALTY_15= new BigDecimal("15.00");
    private static final BigDecimal PENALTY_20 = new BigDecimal("20.00");
    private static final BigDecimal PENALTY_25 = new BigDecimal("25.00");
    private static final BigDecimal PENALTY_UNUSED_2= new BigDecimal("15.00");
    private static final BigDecimal PENALTY_UNUSED_FOUR_OF_A_KIND= new BigDecimal("16.00");//úng tứ quý: trừ ra 4 lá tứ quý
    
    BigDecimal money = BigDecimal.ZERO;
    BigDecimal moneyPot = BigDecimal.ZERO;

    public BoardMoneyUtil(BigDecimal money) {
        this.money = money;
        moneyPot = BigDecimal.ZERO;
    }
    
    public BigDecimal getMoneyPot() {
        return moneyPot;
    }

    public void setMoneyPot(BigDecimal moneyPot) {
        this.moneyPot = moneyPot;
    }
    
    public void addMoneyPot(BigDecimal money) {
        this.moneyPot = Utils.add(this.moneyPot, money);
    }
    
    public BigDecimal getMoney() {
        return money;
    }

    public void setMoney(BigDecimal money) {
        this.money = money;
    }
    
    /**
     * Tiền phạt khi thua theo cách thông thường
     * @param count: số lá bài còn lại bị phạt
     * @return 
     */
    public BigDecimal getNormalMoney(double count) {
        return Utils.multiply(money, new BigDecimal(String.valueOf(count)));
    }
    
    /**
     * Bị chặt heo bị phạt 15 lần tiền cược
     * @param count: số heo bị chặt
     * @return 
     */
    public BigDecimal getChatHeoMoney(double count) {
        BigDecimal value = Utils.multiply(money, new BigDecimal(String.valueOf(count)));
                   value = Utils.multiply(value, PENALTY_15);
        return value;
    }
    
    /**
     * Bị chặt tứ quý phạt 20 lần tiền cược
     * @param count: số tứ quý bị chặt
     * @return 
     */
    public BigDecimal getChatTuQuyMoney(double count) {
         BigDecimal value = Utils.multiply(money, new BigDecimal(String.valueOf(count)));
                   value = Utils.multiply(value, PENALTY_20);
        return value;
    }
    
    /**
     * Bị chặt 4 đôi thông phạt 25 lần tiền cược
     * @param count: số bốn đôi thông bị chặt
     * @return 
     */
    public BigDecimal getChatBonDoiThongMoney(double count) {
        BigDecimal value = Utils.multiply(money, new BigDecimal(String.valueOf(count)));
                   value = Utils.multiply(value, PENALTY_25);
        return value;
    }
    
    /**
     * Thua cóng bị phạt 20 lần tiền cược
     * @return 
     */
    public BigDecimal getCongMoney() {
        return Utils.multiply(money, PENALTY_20);
    }
    
    /**
     * Báo xâm thành công thắng 25 lần tiền cược
     * @return 
     */
    public BigDecimal getXamSuccessMoney() {
        return Utils.multiply(money, PENALTY_25);
    }
    
    /**
     * Chặn xâm thành công mất 25 lần tiền cược cho thằng chặn xâm
     * @return 
     */
    public BigDecimal getChanXamMoney() {
        return Utils.multiply(money, PENALTY_25);
    }
    
    /**
     * Báo Xâm thất bại bị phạt 25 lần tiền cược cho mỗi người chơi
     * @param playerCount
     * @return 
     */
    public BigDecimal getXamFailMoney(double playerCount) {
        BigDecimal value = Utils.multiply(money, new BigDecimal(String.valueOf(playerCount)));
                   value = Utils.multiply(value, PENALTY_25);
        return value;
    }
    
    /**
     * Tới trắng thắng mỗi người chơi 20 lần tiền cược
     * @return 
     */
    public BigDecimal getForceFinishMoney() {
        return Utils.multiply(money, PENALTY_20);
    }
    
    /**
     * Úng heo phạt 15 lần tiền cược x số heo úng
     * @param heoCount
     * @return 
     */
    public BigDecimal getUngHeoMoney(double heoCount) {
        BigDecimal value = Utils.multiply(money, new BigDecimal(String.valueOf(heoCount)));
                   value = Utils.multiply(value, PENALTY_UNUSED_2);
        return value;
    }
    
    public BigDecimal getUngTuQuyMoney(double tuQuyCount) {
        BigDecimal value = Utils.multiply(money, new BigDecimal(String.valueOf(tuQuyCount)));
                   value = Utils.multiply(value, PENALTY_UNUSED_FOUR_OF_A_KIND);
        return value;
    }
}
