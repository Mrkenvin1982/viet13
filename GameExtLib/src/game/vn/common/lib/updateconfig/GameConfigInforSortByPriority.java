/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.vn.common.lib.updateconfig;

import java.io.Serializable;
import java.util.Comparator;

/**
 * Comppare game theo độ uu tiên
 * @author tuanp
 */
public class GameConfigInforSortByPriority implements Comparator<GameConfigInfor>, Serializable{


    @Override
    public int compare(GameConfigInfor o1, GameConfigInfor o2) {
        Integer priority1=o1.getPriority();
        Integer priority2=o2.getPriority();
       return priority1.compareTo(priority2);
    }
    
}
