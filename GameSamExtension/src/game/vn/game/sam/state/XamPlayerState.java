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
 * @author chinhld
 */
public class XamPlayerState implements IXamPlayerState{

    private final SamController game;
    private final SamPlayer player;
    private static final Logger log = LoggerFactory.getLogger(XamPlayerState.class);
    
    public XamPlayerState(SamPlayer player) {
        this.player = player;
        this.game = player.getGame();
    }
    
    @Override
    public void move(List<Card> cards) throws Exception {
        game.move(player.getUser(), cards);
    }

    @Override
    public void skip() throws Exception {
        game.skip(player);
    }
    
}
