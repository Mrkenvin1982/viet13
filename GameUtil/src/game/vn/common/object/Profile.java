/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.vn.common.object;

/**
 * Thông tin profile của user
 * @author tuanp
 */
public class Profile {

    //thời gian chơi
    private long timeOnline;
    //tiền thực của user
    private double money;
    //điểm của user (tiền ảo)
    private long point;
    //cấp bậc hiện tại của user
    private int currentLevel;
    //số điểm user đã đạt được của level hiện tại
    private int currentPointLevel;
    //tổng số điểm user phải đạt được của level hiện tại
    private int sumPointLevel;

    public long getTimeOnline() {
        return timeOnline;
    }

    public void setTimeOnline(long timeOnline) {
        this.timeOnline = timeOnline;
    }

    public double getMoney() {
        return money;
    }

    public void setMoney(double money) {
        this.money = money;
    }

    public int getCurrentLevel() {
        return currentLevel;
    }

    public void setCurrentLevel(int currentLevel) {
        this.currentLevel = currentLevel;
    }

    public long getPoint() {
        return point;
    }

    public void setPoint(long point) {
        this.point = point;
    }

    public int getCurrentPointLevel() {
        return currentPointLevel;
    }

    public void setCurrentPointLevel(int currentPointLevel) {
        this.currentPointLevel = currentPointLevel;
    }

    public int getSumPointLevel() {
        return sumPointLevel;
    }

    public void setSumPointLevel(int sumPointLevel) {
        this.sumPointLevel = sumPointLevel;
    }
    
    
}
