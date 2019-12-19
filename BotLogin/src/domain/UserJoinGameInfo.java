/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package domain;

/**
 *
 * @author hanv
 */
public class UserJoinGameInfo {

    private String userId;
    
    private String roomName;

    private byte moneyType;
    
    private int userType;
    
    private double betBoard;
    
    private double maxBetBoard;
    
    private String idOwner;
    
    private boolean isTournament;
    
    private int mode;
    
    private int serviceId;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    public byte getMoneyType() {
        return moneyType;
    }

    public void setMoneyType(byte moneyType) {
        this.moneyType = moneyType;
    }

    public int getUserType() {
        return userType;
    }

    public void setUserType(int userType) {
        this.userType = userType;
    }

    public double getBetBoard() {
        return betBoard;
    }

    public void setBetBoard(double betBoard) {
        this.betBoard = betBoard;
    }

    public double getMaxBetBoard() {
        return maxBetBoard;
    }

    public void setMaxBetBoard(double maxBetBoard) {
        this.maxBetBoard = maxBetBoard;
    }

    public String getIdOwner() {
        return idOwner;
    }

    public void setIdOwner(String idOwner) {
        this.idOwner = idOwner;
    }

    public boolean isTournament() {
        return isTournament;
    }

    public void setIsTournament(boolean isTournament) {
        this.isTournament = isTournament;
    }

    public int getMode() {
        return mode;
    }

    public void setMode(int mode) {
        this.mode = mode;
    }

    public int getServiceId() {
        return serviceId;
    }

    public void setServiceId(int serviceId) {
        this.serviceId = serviceId;
    }
    
}
