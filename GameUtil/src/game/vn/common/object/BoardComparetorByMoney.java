/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.vn.common.object;

import game.vn.common.lib.hazelcast.Board;
import java.util.Comparator;

/**
 * sort board theo mức cược
 * @author tuanp
 */
public class BoardComparetorByMoney  implements Comparator<Board>{

    @Override
    public int compare(Board o1, Board o2) {
        Double bet1=o1.getBetMoney();
        Double bet2=o2.getBetMoney();
        return bet2.compareTo(bet1);
    }
    
}
