/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package game.vn.game.lieng.object;

import game.vn.game.lieng.card.ResultCard;
import java.util.Comparator;

/**
 *
 * @author tuanp
 */
public class LiengPlayerScoreComparator implements Comparator<LiengPlayer> {

    @Override
    public int compare(LiengPlayer player1, LiengPlayer player2) {
        ResultCard result1 = player1.getResultCard();
        ResultCard result2 = player2.getResultCard();
        if (result1.getValue() > result2.getValue()) {
            return -1;
            //bai ngang nhau, xet con lon nhat
        } else if (result1.getValue() == result2.getValue()) {
            if (result1.getScore()
                    > result2.getScore()) {
                return -1;
            } else if (result1.getScore() == result2.getScore() 
                    && player1.getBiggestCardId().getId() > player2.getBiggestCardId().getId()) {
                    return -1;
            }
            return 1;
        }
        return 0;
    }
}
