/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package game.vn.game.sam.state;

import game.vn.common.card.object.Card;
import java.util.List;

public interface IXamPlayerState {
    /**
     * Đánh bài
     * @param cards
     * @throws Exception 
     */
    public void move(List<Card> cards) throws Exception;
    /**
     * Bỏ lượt
     * @throws Exception 
     */
    public void skip() throws Exception;
}
