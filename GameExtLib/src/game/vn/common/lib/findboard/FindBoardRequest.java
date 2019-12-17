/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.vn.common.lib.findboard;

import game.vn.common.lib.contants.MoneyContants;

/**
 *
 * @author hanv
 */
public class FindBoardRequest {
    private String userId;
    private int serverId;
    private int moneyType=MoneyContants.POINT;
    private byte serviceId;
    private double betMoney;
    private boolean autoJoin;
    private String ipUser;
    private String lobbyName;
    private int maxPlayer;
    private byte mode;
    private boolean isNormalUser;
    private String merchantId;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public int getServerId() {
        return serverId;
    }

    public void setServerId(int serverId) {
        this.serverId = serverId;
    }

    public byte getServiceId() {
        return serviceId;
    }

    public void setServiceId(byte serviceId) {
        this.serviceId = serviceId;
    }

    public double getBetMoney() {
        return betMoney;
    }

    public void setBetMoney(double betMoney) {
        this.betMoney = betMoney;
    }

    public int getMoneyType() {
        return moneyType;
    }

    public void setMoneyType(int moneyType) {
        this.moneyType = moneyType;
    }

    public boolean isAutoJoin() {
        return autoJoin;
    }

    public void setAutoJoin(boolean autoJoin) {
        this.autoJoin = autoJoin;
    }

    public String getIpUser() {
        return ipUser;
    }

    public void setIpUser(String ipUser) {
        this.ipUser = ipUser;
    }

    public String getLobbyName() {
        return lobbyName;
    }

    public void setLobbyName(String lobbyName) {
        this.lobbyName = lobbyName;
    }

    public int getMaxPlayer() {
        return maxPlayer;
    }

    public void setMaxPlayer(int maxPlayer) {
        this.maxPlayer = maxPlayer;
    }

    public byte getMode() {
        return mode;
    }

    public void setMode(byte mode) {
        this.mode = mode;
    }

    public boolean isNormalUser() {
        return isNormalUser;
    }

    public void setNormalUser(boolean isNormalUser) {
        this.isNormalUser = isNormalUser;
    }

    public String getMerchantId() {
        return merchantId;
    }

    public void setMerchantId(String merchantId) {
        this.merchantId = merchantId;
    }

    @Override
    public String toString() {
        return "FindBoardRequest{" + "userId=" + userId + ", serverId=" + serverId + ", serviceId=" + serviceId + ", betMoney=" + betMoney + '}';
    }
}
