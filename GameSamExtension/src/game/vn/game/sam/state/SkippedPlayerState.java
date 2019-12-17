/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package game.vn.game.sam.state;

import game.vn.common.card.object.Card;
import game.vn.common.lang.GameLanguage;
import game.vn.game.sam.SamController;
import game.vn.game.sam.object.SamPlayer;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author tuanp
 */
public class SkippedPlayerState implements IXamPlayerState {

    private final SamController game;
    private final SamPlayer player;
    private final Logger log = LoggerFactory.getLogger(SkippedPlayerState.class);
    
    public SkippedPlayerState(SamPlayer player) {
        this.player = player;
        this.game = player.getGame();
    }

    @Override
    public void move(List<Card> cards) throws Exception {
        String infor=GameLanguage.getMessage(GameLanguage.INVALID_CARD, player.getLocale());
        player.sendMoveErrorMessage(infor);
    }

    @Override
    public void skip() throws Exception {
        game.skip(player);
    }
}
