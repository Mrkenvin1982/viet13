/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.vn.common.lib.taixiu;

/**
 *
 * @author hanv
 */
public class TaiXiuBuyTicketRequest {
    private final byte[] dice;
    private final boolean isAuto;
    private final String lang;

    public TaiXiuBuyTicketRequest(byte[] dice, boolean isAuto, String lang) {
        this.dice = dice;
        this.isAuto = isAuto;
        this.lang = lang;
    }

    public byte getDice(int i) {
        return dice[i];
    }

    public boolean isAuto() {
        return isAuto;
    }

    public String getLang() {
        return lang;
    }
}
