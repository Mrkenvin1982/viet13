/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.vn.util.db;

import java.math.BigDecimal;

/**
 *
 * @author hanv
 */
public class TransferMoneyResult {
    public BigDecimal fromMoneyBefore = BigDecimal.ZERO;
    public BigDecimal fromMoneyAfter = BigDecimal.ZERO;
    public BigDecimal toMoneyBefore = BigDecimal.ZERO;
    public BigDecimal toMoneyAfter = BigDecimal.ZERO;
    
    public boolean isSuccess() {
        return fromMoneyAfter.compareTo(fromMoneyBefore) < 0 && toMoneyAfter.compareTo(toMoneyBefore) > 0;
    }

    @Override
    public String toString() {
        return "TransferMoneyResult{" + "fromMoneyBefore=" + fromMoneyBefore + ", fromMoneyAfter=" + fromMoneyAfter + ", toMoneyBefore=" + toMoneyBefore + ", toMoneyAfter=" + toMoneyAfter + '}';
    }
}