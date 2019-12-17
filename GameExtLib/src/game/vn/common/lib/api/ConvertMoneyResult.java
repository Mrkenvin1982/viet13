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
public class ConvertMoneyResult {
    public BigDecimal pointBefore = BigDecimal.ZERO;
    public BigDecimal pointAfter = BigDecimal.ZERO;
    public BigDecimal moneyBefore = BigDecimal.ZERO;
    public BigDecimal moneyAfter = BigDecimal.ZERO;
    
    public boolean isSuccess() {
        return pointAfter.compareTo(pointBefore) < 0 && moneyAfter.compareTo(moneyBefore) > 0;
    }

    @Override
    public String toString() {
        return "ConvertMoneyResult{" + "pointBefore=" + pointBefore + ", pointAfter=" + pointAfter + ", moneyBefore=" + moneyBefore + ", moneyAfter=" + moneyAfter + '}';
    }

}
