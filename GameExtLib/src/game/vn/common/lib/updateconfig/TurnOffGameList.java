/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.vn.common.lib.updateconfig;

import java.util.List;

/**
 * Danh sách turn off game
 * @author tuanp
 */
public class TurnOffGameList {
    private List<TurnOffGameDetail> pointGameList;
    private List<TurnOffGameDetail> moneyGameList;
    
    public TurnOffGameList(){
        
    }

    public List<TurnOffGameDetail> getPointGameList() {
        return pointGameList;
    }

    public void setPointGameList(List<TurnOffGameDetail> pointGameList) {
        this.pointGameList = pointGameList;
    }

    public List<TurnOffGameDetail> getMoneyGameList() {
        return moneyGameList;
    }

    public void setMoneyGameList(List<TurnOffGameDetail> moneyGameList) {
        this.moneyGameList = moneyGameList;
    }

    
}
