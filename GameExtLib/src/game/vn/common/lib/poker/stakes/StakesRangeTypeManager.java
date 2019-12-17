/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.vn.common.lib.poker.stakes;

/**
 * quản lý range stakes theo money type
 * @author tuanp
 */
public class StakesRangeTypeManager {
    
    private StakesRangeManager stakesRangeMoneyType;
    private StakesRangeManager stakesRangePointType;

    public StakesRangeManager getStakesRangeMoneyType() {
        return stakesRangeMoneyType;
    }

    public void setStakesRangeMoneyType(StakesRangeManager stakesRangeMoneyType) {
        this.stakesRangeMoneyType = stakesRangeMoneyType;
    }

    public StakesRangeManager getStakesRangePointType() {
        return stakesRangePointType;
    }

    public void setStakesRangePointType(StakesRangeManager stakesRangePointType) {
        this.stakesRangePointType = stakesRangePointType;
    }

    
    
}
