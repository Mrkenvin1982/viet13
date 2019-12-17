/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.vn.game.xito.playerstate;

import java.math.BigDecimal;

/**
 *
 * @author tuanp
 */
public interface IPlayerState {
    //TurnState do this
    public void betting(BigDecimal stack) throws Exception;
    public void raising(BigDecimal stack) throws Exception;
    public void calling() throws Exception;
    public void allIn() throws Exception;
    public void checking() throws Exception;
    public void folding() throws Exception;   
}
