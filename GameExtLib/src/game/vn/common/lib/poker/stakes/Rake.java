/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.vn.common.lib.poker.stakes;

/**
 * Mức thuế của bàn
 * @author tuanp
 */
public class Rake {
    private int tax;//% số thuế trên tổng pot
    private double min;//số tiền thuế tối thiểu, nếu nhỏ hơn sẽ không thu
    private double max;// số tiền thuế tối đa, nếu lơn hơn số  này chỉ thu o mức tối đa

    public int getTax() {
        return tax;
    }

    public void setTax(int tax) {
        this.tax = tax;
    }

    public double getMin() {
        return min;
    }

    public void setMin(double min) {
        this.min = min;
    }

    public double getMax() {
        return max;
    }

    public void setMax(double max) {
        this.max = max;
    }

}
