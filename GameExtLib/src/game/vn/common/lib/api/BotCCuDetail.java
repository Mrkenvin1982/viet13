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
public class BotCCuDetail {
    private int serviceId;
    private int countMasterMoney;
    private int countMasterPoint;
    private int countPlayerMoney;
    private int countPlayerPoint;
    private int countInGameMoney;
    private int countInGamePoint;
    private int countLobbyMoney;
    private int countLobbyPoint;

    public BotCCuDetail(int serviceId, int countMasterMoney, int countPlayerMoney, int countMasterPoint, int countPlayerPoint, int countInGameMoney, int countInGamePoint, int countLobbyMoney, int countLobbyPoint) {
        this.serviceId = serviceId;
        this.countMasterMoney = countMasterMoney;
        this.countPlayerMoney = countPlayerMoney;
        this.countMasterPoint = countMasterPoint;
        this.countPlayerPoint = countPlayerPoint;
        this.countInGameMoney = countInGameMoney;
        this.countInGamePoint = countInGamePoint;
        this.countLobbyMoney = countLobbyMoney;
        this.countLobbyPoint = countLobbyPoint;
    }

    public int getServiceId() {
        return serviceId;
    }

    public void setServiceId(int serviceId) {
        this.serviceId = serviceId;
    }

    public int getCountMasterMoney() {
        return countMasterMoney;
    }

    public void setCountMasterMoney(int countMasterMoney) {
        this.countMasterMoney = countMasterMoney;
    }

    public int getCountMasterPoint() {
        return countMasterPoint;
    }

    public void setCountMasterPoint(int countMasterPoint) {
        this.countMasterPoint = countMasterPoint;
    }

    public int getCountPlayerMoney() {
        return countPlayerMoney;
    }

    public void setCountPlayerMoney(int countPlayerMoney) {
        this.countPlayerMoney = countPlayerMoney;
    }

    public int getCountPlayerPoint() {
        return countPlayerPoint;
    }

    public void setCountPlayerPoint(int countPlayerPoint) {
        this.countPlayerPoint = countPlayerPoint;
    }

    public int getCountInGameMoney() {
        return countInGameMoney;
    }

    public void setCountInGameMoney(int countInGameMoney) {
        this.countInGameMoney = countInGameMoney;
    }

    public int getCountInGamePoint() {
        return countInGamePoint;
    }

    public void setCountInGamePoint(int countInGamePoint) {
        this.countInGamePoint = countInGamePoint;
    }

    public int getCountLobbyMoney() {
        return countLobbyMoney;
    }

    public void setCountLobbyMoney(int countLobbyMoney) {
        this.countLobbyMoney = countLobbyMoney;
    }

    public int getCountLobbyPoint() {
        return countLobbyPoint;
    }

    public void setCountLobbyPoint(int countLobbyPoint) {
        this.countLobbyPoint = countLobbyPoint;
    }
}
