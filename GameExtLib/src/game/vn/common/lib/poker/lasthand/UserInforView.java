/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.vn.common.lib.poker.lasthand;

import java.util.ArrayList;
import java.util.List;

/**
 * thong tin user
 * @author tuanp
 */
public class UserInforView {
    private List<Short> preCardIds = new ArrayList<>();
    private String userName;
    private String idDB;
    private String strCardsResult;
    private boolean isAutoMuckHand;
    private double winStack;

    public List<Short> getPreCardIds() {
        return preCardIds;
    }

    public void setPreCardIds(List<Short> preCardIds) {
        this.preCardIds = preCardIds;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getIdDB() {
        return idDB;
    }

    public void setIdDB(String idDB) {
        this.idDB = idDB;
    }

    public String getValueCardsResult() {
        return strCardsResult;
    }

    public void setValueCardsResult(String strCardsResult) {
        this.strCardsResult = strCardsResult;
    }

    public String getStrCardsResult() {
        return strCardsResult;
    }

    public void setStrCardsResult(String strCardsResult) {
        this.strCardsResult = strCardsResult;
    }

    public boolean isIsAutoMuckHand() {
        return isAutoMuckHand;
    }

    public void setIsAutoMuckHand(boolean isAutoMuckHand) {
        this.isAutoMuckHand = isAutoMuckHand;
    }

    public double getWinStack() {
        return winStack;
    }

    public void setWinStack(double winStack) {
        this.winStack = winStack;
    }

}
