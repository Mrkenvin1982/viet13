/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.vn.common.lib.event;

import java.util.List;

/**
 * Event săn bài đẹp
 * @author
 */
public class UserCardsObj {
    
    private String userId;
    private String gameId;
    private int moneyType; //1 point, 2 real
    private String condition; //list below
    private String roomName;
    private double moneyBoard;
    private double turnOver;
    private int numOfPlayer;
    private long timeCreate;
    private int serverId;
    private List<Integer> listCardIds;
    private String invoiceId;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getGameId() {
        return gameId;
    }

    public void setGameId(String gameId) {
        this.gameId = gameId;
    }

    public int getMoneyType() {
        return moneyType;
    }

    public void setMoneyType(int moneyType) {
        this.moneyType = moneyType;
    }

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    public double getMoneyBoard() {
        return moneyBoard;
    }

    public void setMoneyBoard(double moneyBoard) {
        this.moneyBoard = moneyBoard;
    }

    public int getNumOfPlayer() {
        return numOfPlayer;
    }

    public void setNumOfPlayer(int numOfPlayer) {
        this.numOfPlayer = numOfPlayer;
    }

    public long getTimeCreate() {
        return timeCreate;
    }

    public void setTimeCreate(long timeCreate) {
        this.timeCreate = timeCreate;
    }

    public int getServerId() {
        return serverId;
    }

    public void setServerId(int serverId) {
        this.serverId = serverId;
    }

    public List<Integer> getListCardIds() {
        return listCardIds;
    }

    public void setListCardIds(List<Integer> listCardIds) {
        this.listCardIds = listCardIds;
    }

    public String getInvoiceId() {
        return invoiceId;
    }

    public void setInvoiceId(String invoiceId) {
        this.invoiceId = invoiceId;
    }

    public double getTurnOver() {
        return turnOver;
    }

    public void setTurnOver(double turnOver) {
        this.turnOver = turnOver;
    }
    
}
