/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package game.vn.game.sam.state;

import game.vn.common.card.object.Card;
import game.vn.common.lang.GameLanguage;
import game.vn.game.sam.object.SamPlayer;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author chinhld
 */
public class SkipXamPlayerState implements IXamPlayerState{

    private final SamPlayer player;

    private static final Logger log = LoggerFactory.getLogger(SkipXamPlayerState.class);

    public SkipXamPlayerState(SamPlayer player) {
        this.player = player;
    }
    
    @Override
    public void move(List<Card> cards) throws Exception {
        String infor=GameLanguage.getMessage(GameLanguage.INVALID_CARD, player.getLocale());
        player.sendMoveErrorMessage(infor);
    }

    @Override
    public void skip() throws Exception {
        log.error("SkipXamPlayerState.skip is not supported yet");
    }
    
}
