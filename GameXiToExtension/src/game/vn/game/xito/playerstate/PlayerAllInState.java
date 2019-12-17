/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.vn.game.xito.playerstate;

import game.vn.game.xito.object.XiToPlayer;
import java.math.BigDecimal;

/**
 *
 * @author tuanp
 */
public class PlayerAllInState implements IPlayerState{
    private final XiToPlayer player;
    public PlayerAllInState(XiToPlayer player){
        this.player = player;
    }

    @Override
    public void betting(BigDecimal stack) throws Exception {
    }

    @Override
    public void raising(BigDecimal stack) throws Exception {
    }

    @Override
    public void calling() throws Exception {
    }

    @Override
    public void allIn() throws Exception {
    }

    @Override
    public void checking() throws Exception {
    }
    
    @Override
    public void folding() throws Exception {
    }
}
