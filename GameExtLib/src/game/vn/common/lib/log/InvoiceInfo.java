/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.vn.common.lib.log;

/**
 * 
 * @author tuanp
 */
public class InvoiceInfo {
    //mức cược trong game
    private double credit;
    //thuế trong game
    private double tax;
    private double bet;
    private double receive;
    private double returns;
    private double penalty;
    private int result; // hanv add, user for BigSmall result
    private byte[] dice;
    private int betUnit;
    private double playMoney;

    public double getCredit() {
        return credit;
    }

    public void setCredit(double credit) {
        this.credit = credit;
    }

    public double getTax() {
        return tax;
    }

    public void setTax(double tax) {
        this.tax = tax;
    }

    public void setBet(double bet) {
        this.bet = bet;
    }

    public void setReceive(double receive) {
        this.receive = receive;
    }

    public void setResult(int result) {
        this.result = result;
    }

    public void setDice(byte[] dice) {
        this.dice = dice;
    }

    public void setReturns(double returns) {
        this.returns = returns;
    }

    public double getPenalty() {
        return penalty;
    }

    public void setPenalty(double pelnaty) {
        this.penalty = pelnaty;
    }

    public int getBetUnit() {
        return betUnit;
    }

    public void setBetUnit(int betUnit) {
        this.betUnit = betUnit;
    }

    public double getBet() {
        return bet;
    }

    public double getReceive() {
        return receive;
    }

    public double getReturns() {
        return returns;
    }

    public int getResult() {
        return result;
    }

    public byte[] getDice() {
        return dice;
    }

    public double getPlayMoney() {
        return playMoney;
    }

    public void setPlayMoney(double playMoney) {
        this.playMoney = playMoney;
    }  
}
