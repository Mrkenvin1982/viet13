/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package game.vn.game.sam.state;

import game.vn.common.card.object.Card;
import game.vn.game.sam.SamController;
import game.vn.game.sam.object.SamPlayer;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author tuanp
 */
public class MovingPlayerState implements IXamPlayerState{

    private SamController game;
    private SamPlayer player;
    private static Logger log = LoggerFactory.getLogger(MovingPlayerState.class);
    public MovingPlayerState(SamPlayer player) {
        this.game = player.getGame();
        this.player = player;
    }
    
    @Override
    public void move(List<Card> cards) throws Exception {
        game.move(player.getUser(), cards);
    }

    @Override
    public void skip() throws Exception {
        log.error("MovingPlayerState.skip is not supported yet");
    }
}
