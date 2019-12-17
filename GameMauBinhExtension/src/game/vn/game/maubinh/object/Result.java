/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package game.vn.game.maubinh.object;

import game.vn.game.maubinh.MauBinhConfig;


/**
 *
 * @author binhnt
 */
public class Result {
    // If win mau binh, then don't care remaining info.
    int winChiMauBinh;
    // Win chi of 1st set.
    int winChi01;
    // Win chi of 2nd set.
    int winChi02;
    // Win chi of 3rd set.
    int winChi03;
    // Win chi by Ace.
    int winChiAce;
    // Multiple koefficient:
    // 1 default.
    // 2 if win 3 set (an sap ham).
    // 4 if win 3 set of 3 players (an sap ham ca 3 nha).
    int multiK;
    
    public Result() {
        this.winChiMauBinh = 0;
        this.winChi01 = 0;
        this.winChi02 = 0;
        this.winChi03 = 0;
        this.winChiAce = 0;
        this.multiK = 1;
    }
    
    public int getWinChiMauBinh() {
        return this.winChiMauBinh;
    }
    
    public int getWinChi01() {
        return this.winChi01;
    }
    
    public int getWinChi02() {
        return this.winChi02;
    }
    
    public int getWinChi03() {
        return this.winChi03;
    }
    
    public int getWinChiAce() {
        return this.winChiAce;
    }
    
    /**
     * Get sum of win chi.
     * @return sum of win chi.
     */
    public int getWinChi() {
        // If win or lose mau binh.
        if (this.getWinChiMauBinh() != 0) {
            return this.getWinChiMauBinh() + this.getWinChiAce();
        }
        
        return (this.getWinChi01() + this.getWinChi02() + this.getWinChi03()) * this.getMultiK()
                + this.getWinChiAce();
    }
    
    public int getMultiK() {
        return this.multiK;
    }
    
    public void setWinChiMauBinh(int value) {
        this.winChiMauBinh = value;
    }
    
    public void setWinChi01(int value) {
        this.winChi01 = value;
    }
    
    public void setWinChi02(int value) {
        this.winChi02 = value;
    }
    
    public void setWinChi03(int value) {
        this.winChi03 = value;
    }
    
    public void setWinChiAce(int value) {
        this.winChiAce = value;
    }
    
    public void setMultiK(int value) {
        this.multiK = value;
    }
    
    public Result getNegative() {
        Result ret = new Result();

        ret.setWinChiMauBinh(-this.getWinChiMauBinh());
        ret.setWinChi01(-this.getWinChi01());
        ret.setWinChi02(-this.getWinChi02());
        ret.setWinChi03(-this.getWinChi03());
        ret.setWinChiAce(-this.getWinChiAce());
        ret.setMultiK(this.getMultiK());
        
        return ret;
    }
    
    public boolean isWinThreeSet() {
        return (this.getMultiK() == MauBinhConfig.getInstance().getChiWinThreeSetRate() ||
                this.getMultiK() == MauBinhConfig.getInstance().getChiWinAllByThreeSetRate()) &&
                this.getWinChi01() > 0; // Cannot use this.getWinChi, because it includes Ace chi.
    }
}
