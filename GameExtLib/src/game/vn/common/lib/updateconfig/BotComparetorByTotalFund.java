/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.vn.common.lib.updateconfig;

import java.util.Comparator;

/**
 *
 * @author tuanp
 */
public class BotComparetorByTotalFund implements Comparator<BotAdvConfig>{

    @Override
    public int compare(BotAdvConfig o1, BotAdvConfig o2) {
        Integer total1=o1.getTotalFund();
        Integer total2=o2.getTotalFund();
        return total1.compareTo(total2);
    }
}
