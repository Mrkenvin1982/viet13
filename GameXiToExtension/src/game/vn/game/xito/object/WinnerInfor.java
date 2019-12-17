/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.vn.game.xito.object;

import game.vn.util.Utils;
import java.math.BigDecimal;

/**
 *
 * @author tuanp
 */
public class WinnerInfor {
    
    private BigDecimal stackWin = BigDecimal.ZERO;
    private BigDecimal taxWin = BigDecimal.ZERO;

    public BigDecimal getStackWin() {
        return stackWin;
    }

    public void addStackWin(BigDecimal stackWinInput) {
        this.stackWin = Utils.add(stackWin, stackWinInput);
    }

    public BigDecimal getTaxWin() {
        return taxWin;
    }

    public void addTaxWin(BigDecimal taxWinInPut) {
        this.taxWin =  Utils.add(this.taxWin ,taxWinInPut);
    }
    
    
}
