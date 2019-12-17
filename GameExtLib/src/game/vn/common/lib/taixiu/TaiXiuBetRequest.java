/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.vn.common.lib.taixiu;

import java.math.BigDecimal;

/**
 *
 * @author hanv
 */
public class TaiXiuBetRequest {
    private final BigDecimal betMoney;
    private final byte betChoice;
    private final String lang;

    public TaiXiuBetRequest(BigDecimal betMoney, byte betChoice, String lang) {
        this.betMoney = betMoney;
        this.betChoice = betChoice;
        this.lang = lang;
    }

    public BigDecimal getBetMoney() {
        return betMoney;
    }

    public byte getBetChoice() {
        return betChoice;
    }

    public String getLang() {
        return lang;
    }
}
