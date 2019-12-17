/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package game.vn.game.lieng.playerstate;

import game.vn.game.lieng.object.LiengPlayer;
import java.math.BigDecimal;


/**
 *
 * @author hoanghh
 */
public class WaitingToNextTurnPlayerState implements ILiengPlayerState{
    private LiengPlayer player;
    public WaitingToNextTurnPlayerState(LiengPlayer player) {
        this.player = player;
    }
    
    @Override
    public void bet(BigDecimal betMoney) throws Exception {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void call() throws Exception {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void fold() throws Exception {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void check() throws Exception {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
