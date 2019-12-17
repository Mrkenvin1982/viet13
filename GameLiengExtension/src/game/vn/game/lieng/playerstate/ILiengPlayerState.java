/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.vn.game.lieng.playerstate;

import java.math.BigDecimal;

/**
 *
 * @author tuanp
 */
public interface ILiengPlayerState {
    public void bet(BigDecimal betMoney) throws Exception;
    public void call() throws Exception;
    public void fold() throws Exception;
    public void check() throws Exception;
    
}
