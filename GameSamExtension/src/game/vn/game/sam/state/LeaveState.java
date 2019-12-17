/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package game.vn.game.sam.state;

import game.vn.common.card.object.Card;
import game.vn.game.sam.object.SamPlayer;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author tuanp
 */

public class LeaveState implements IXamPlayerState{

    private SamPlayer player;

    private static Logger log = LoggerFactory.getLogger(LeaveState.class);
    
    public LeaveState(SamPlayer player) {
        this.player = player;
    }
    
    @Override
    public void move(List<Card> cards) throws Exception {
        log.error("LeaveState.move is not supported yet");
    }

    @Override
    public void skip() throws Exception {
        log.error("LeaveState.skip is not supported yet");
    }
    
}
