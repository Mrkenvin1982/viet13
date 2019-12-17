/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.vn.common.lib.poker.lasthand;

import java.math.BigDecimal;

/**
 *
 * @author tuanp
 */
public class UserInforDetail {
    
    private String userName;
    private byte actionId;
    private Double betValue;
    private byte roundIndex;

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public byte getActionId() {
        return actionId;
    }

    public void setActionId(byte actionId) {
        this.actionId = actionId;
    }

    public double getBetValue() {
        return betValue;
    }

    public void setBetValue(double betValue) {
        this.betValue = betValue;
    }

    public byte getRoundIndex() {
        return roundIndex;
    }

    public void setRoundIndex(byte roundIndex) {
        this.roundIndex = roundIndex;
    }

}
