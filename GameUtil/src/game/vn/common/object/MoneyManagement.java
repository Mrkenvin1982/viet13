/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.vn.common.object;

import game.vn.util.Utils;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 *class này xử lý để lưu trữ số tiền đã cược, tiền trước khi start ván của player
 * @author tuanp
 */
public class MoneyManagement {
    private final Map<String, BigDecimal> mapStack;
    //Tổng số tiền cược của toàn bộ ván
    private BigDecimal sumStack = BigDecimal.ZERO;

    public MoneyManagement() {
        this.mapStack = new HashMap<>();
    }

    public synchronized void bettingMoney(String idDBUser, BigDecimal nMoney) {
        mapStack.put(idDBUser, nMoney);
    }


    public synchronized BigDecimal getBettingMoney(String idDBUser) {
        if (!mapStack.containsKey(idDBUser)) {
            return BigDecimal.ZERO;
        }
        return mapStack.get(idDBUser);
    }

    //xử lý khi user rời khỏi bàn
    public synchronized void remove(String idDBUser) {
        if (mapStack.containsKey(idDBUser)) {
            mapStack.remove(idDBUser);
        }
    }

    /**
     * Sử dụng để lưu tiền cược của tất cả user trong ván
     * @param nMoney 
     */
    public synchronized void addInGameMoney(BigDecimal nMoney) {
        sumStack = Utils.add(sumStack, nMoney);
        if (sumStack.signum() < 0) {
            sumStack = BigDecimal.ZERO;
        }
    }

    public synchronized BigDecimal updateMoney(BigDecimal money) {
        return money.min(sumStack);
    }

    /**
     * Tổng số tiền cược trong ván
     * @return 
     */
    public synchronized BigDecimal getInGameMoney() {
        return sumStack;
    }
    
    /**
     * Reset lại tổng tiền cược của ván
     */
    public void reset() {
       sumStack = BigDecimal.ZERO;
       mapStack.clear();
    }
    
    public void resetMoneyIngame(){
        sumStack = BigDecimal.ZERO;
    }
    
    /**
     * Lấy ra số tiền có thể win
     * @param userIdDB
     * @param winLoseMoney
     * @return 
     */
    public BigDecimal getCanWinOrLoseMoney(String userIdDB, BigDecimal winLoseMoney) {
        BigDecimal value = getBettingMoney(userIdDB);
        return value.min(winLoseMoney);
    }
}
