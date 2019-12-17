/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.vn.common.lib.api;

/**
 *
 * @author minhvtd
 */
public class UserCCuDetail {

    private byte serviceId;
    private int countLobbyMoney;
    private int countGameMoney;
    private int countLobbyPoint;
    private int countGamePoint;

    public UserCCuDetail(byte serviceId) {
        this.serviceId = serviceId;
    }

    public byte getServiceId() {
        return serviceId;
    }

    public void setServiceId(byte serviceId) {
        this.serviceId = serviceId;
    }

    public int getCountLobbyMoney() {
        return countLobbyMoney;
    }

    public void setCountLobbyMoney(int countLobbyMoney) {
        this.countLobbyMoney = countLobbyMoney;
    }

    public int getCountGameMoney() {
        return countGameMoney;
    }

    public void setCountGameMoney(int countGameMoney) {
        this.countGameMoney = countGameMoney;
    }

    public int getCountLobbyPoint() {
        return countLobbyPoint;
    }

    public void setCountLobbyPoint(int countLobbyPoint) {
        this.countLobbyPoint = countLobbyPoint;
    }

    public int getCountGamePoint() {
        return countGamePoint;
    }

    public void setCountGamePoint(int countGamePoint) {
        this.countGamePoint = countGamePoint;
    }

    public void increaseCountLobbyMoney() {
        countLobbyMoney++;
    }
    public void increaseCountLobbyPoint() {
        countLobbyPoint++;
    }
    public void increaseCountGameMoney() {
        countGameMoney++;
    }
    public void increaseCountGamePoint() {
        countGamePoint++;
    }
}
