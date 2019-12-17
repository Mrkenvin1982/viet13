/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.vn.game.xito.object;

import game.vn.game.xito.XiToController;
import game.vn.util.Utils;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * moi luot chia bai tuong ung voi mot round chot stack va round cua vong
 * <br />
 * Một round có thể bet nhiều vòng, đến khi tất cả bet xong
 *
 * @author tuanp
 */
public class Round {
    //key: userName -- value: stack in round

    private final  Map<String, BigDecimal> mapStack;
    private final  Map<String, Byte> lastAction;
    /**
     * dùng để check trạng thái của user
     */
    private final XiToController game;

    public Round(XiToController game) {
        this.mapStack = new HashMap<>();
        this.lastAction = new HashMap<>();
        this.game = game;
    }

    /**
     * Tổng số stack của vòng
     *
     * @return
     */
    public BigDecimal getStack() {
        BigDecimal result = BigDecimal.ZERO;
        for (BigDecimal value : mapStack.values()) {
            result = Utils.add(result, value);
        }
        return result;
    }

    /**
     * Trả lại người chơi stack còn thừa của vòng. Trường hợp xảy ra khi người
     * thắng bet ít hơn người thua
     *
     * @param idDBUser
     * @param stack
     */
    public void minusStack(String idDBUser, BigDecimal stack) {
        stack =Utils.subtract(this.mapStack.get(idDBUser), stack);
        mapStack.put(idDBUser, stack);
    }

    public void addStack(String idDBUser, BigDecimal stack) {
        if (this.mapStack.get(idDBUser) != null) {
            stack =Utils.add(this.mapStack.get(idDBUser), stack);
        }
        mapStack.put(idDBUser, stack);
    }

    public Collection<String> getIdDBUsers() {
        return mapStack.keySet();
    }

    public boolean isContain(String idDBUser) {
        return this.mapStack.containsKey(idDBUser);
    }

    /**
     * Nếu ko có người nào trong vòng thì là vòng mới
     *
     * @return
     */
    public boolean isNewRound() {
        return mapStack.isEmpty();
    }

    /**
     * Trả về số stack người chơi đã bet trong vòng
     *
     * @param idDBUser
     * @return
     */
    public BigDecimal getStack(String idDBUser) {
        if (!mapStack.containsKey(idDBUser)) {
            return BigDecimal.ZERO;
        }
        return mapStack.get(idDBUser);
    }

    /**
     * Trả về số stack của thằng bet nhiều nhất trong vòng
     * <br />
     * Dùng để tính số stack còn thiếu khi call
     *
     * @return
     * @throws Exception
     */
    public BigDecimal getMaxBetStackOfPlayer() throws Exception {
      String foundIdDBUser= null;

        for (String idDBUser : this.mapStack.keySet()) {
            if (foundIdDBUser==null) {
                foundIdDBUser = idDBUser;
                continue;
            }

            if (getStack(foundIdDBUser).compareTo(getStack(idDBUser)) < 0) {
                foundIdDBUser = idDBUser;
            }
        }
        if(foundIdDBUser == null){
            return BigDecimal.ONE.negate();
        }
        return getStack(foundIdDBUser);
    }

    /**
     * tìm người thắng của vòng, ko có trả về null
     *
     * @return
     * @throws Exception
     */
    public XiToPlayer findWinner() throws Exception {
        XiToPlayer roundWinner = null;
        for (String idDB : this.mapStack.keySet()) {
            XiToPlayer xtPlayer = game.getXiToPlayer(idDB);
            //null trong trường hợp user đã thoát ra khỏi ván chơi
            if (xtPlayer == null) {
                continue;
            }
            if (xtPlayer.isLeave()) {
                continue;
            }
            if (!game.isInturn(xtPlayer.getUser()) && !xtPlayer.isAllIn()) {
                continue;
            }
            if (roundWinner == null) {
                roundWinner = xtPlayer;
                continue;
            }
            roundWinner = XiToGameUtil.comparePlayerScore(roundWinner, xtPlayer);
        }
        return roundWinner;
    }
    
    public Map<String, BigDecimal> getMapStack() {
        return mapStack;
    }
    
    public void updateLastAction(String idDBUser, byte actionId) {
        lastAction.put(idDBUser, actionId);
    }

    public byte getLastAction(String idDBUser) {
        if (!lastAction.containsKey(idDBUser)) {
            return -1;
        }
        return lastAction.get(idDBUser);
    }
}
