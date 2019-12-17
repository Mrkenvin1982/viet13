/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.vn.common.lib.hazelcast;

import game.vn.common.lib.contants.MoneyContants;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author hanv
 */
public class Board implements Serializable {
        
    static final long serialVersionUID = 1l;
    
    private String ip ="";
    private String ipWS="";
    private int port;
    private int portWS;
    private String zone="";
    private String name="";
    private byte serviceId;
    private double betMoney;
    private int freeSeat;
    private boolean isPlaying;
    private boolean isAutoJoin;
    private int moneyType=MoneyContants.POINT;
    private final Map<String, String> ips = new HashMap<>();
    private String lobbyName="";
    private int maxPlayer;
    private byte mode;
    private byte serverId;
    private String merchantId="";

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getIpWS() {
        return ipWS;
    }

    public void setIpWS(String ipWS) {
        this.ipWS = ipWS;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getZone() {
        return zone;
    }

    public void setZone(String zone) {
        this.zone = zone;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public int getFreeSeat() {
        return freeSeat;
    }

    public void setFreeSeat(int freeSeat) {
        this.freeSeat = freeSeat;
    }

    public boolean isPlaying() {
        return isPlaying;
    }

    public void setIsPlaying(boolean isPlaying) {
        this.isPlaying = isPlaying;
    }

    public int getMoneyType() {
        return moneyType;
    }

    public void setMoneyType(int moneyType) {
        this.moneyType = moneyType;
    }

    public boolean isIsAutoJoin() {
        return isAutoJoin;
    }

    public void setIsAutoJoin(boolean isAutoJoin) {
        this.isAutoJoin = isAutoJoin;
    }

    public Map<String, String> getIps() {
        return ips;
    } 

    public int getPortWS() {
        return portWS;
    }

    public void setPortWS(int portWS) {
        this.portWS = portWS;
    }

    public String getLobbyName() {
        return lobbyName;
    }

    public void setLobbyName(String nameLobby) {
        this.lobbyName = nameLobby;
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

    public byte getServerId() {
        return serverId;
    }

    public void setServerId(byte serverId) {
        this.serverId = serverId;
    }  

    public String getMerchantId() {
        return merchantId;
    }

    public void setMerchantId(String merchantId) {
        this.merchantId = merchantId;
    }

}
