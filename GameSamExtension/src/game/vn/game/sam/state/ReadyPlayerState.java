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
 * @author tuanp
 */
public class ReadyPlayerState implements IXamPlayerState {

    private SamPlayer xamPlayer;

    private static Logger log = LoggerFactory.getLogger(ReadyPlayerState.class);
    
    public ReadyPlayerState(SamPlayer xamPlayer) {
        this.xamPlayer = xamPlayer;
    }
    
    @Override
    public void move(List<Card> cards) throws Exception {
        String infor=GameLanguage.getMessage(GameLanguage.INVALID_CARD, xamPlayer.getLocale());
        xamPlayer.sendMoveErrorMessage(infor);
    }

    @Override
    public void skip() throws Exception {
        log.error("AllInPlayerState.skip is not supported yet");
    }
}
