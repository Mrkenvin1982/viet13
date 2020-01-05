/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.vn.common.object;

import game.vn.common.lib.hazelcast.Board;
import java.util.Comparator;

/**
 *
 * @author hanv
 */
public class BoardComparator implements Comparator<Board> {

    @Override
    public int compare(Board board1, Board board2) {
        if (board1 == null || board2 == null) {
            return 0;
        }
        Boolean isPlaying1 = board1.isPlaying();
        Boolean isPlaying2 = board2.isPlaying();
        int compare = isPlaying1.compareTo(isPlaying2);
        if (compare == 0) {
            Integer seat1 = board1.getFreeSeat();
            Integer seat2 = board2.getFreeSeat();
            return seat1.compareTo(seat2);
        }
        return compare;
    }

}
