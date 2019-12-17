/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.vn.common.lib.taixiu;

/**
 *
 * @author hanv
 */
public class TaiXiuQueueData {
    private final byte command;
    private String userId;
    private String username;
    private int userType;
    private String data;
    private int serverId;
    private byte moneyType;

    public TaiXiuQueueData(byte command) {
        this.command = command;
    }

    public TaiXiuQueueData(byte command, String userId, int serverId) {
        this.command = command;
        this.userId = userId;
        this.serverId = serverId;
    }

    public byte getCommand() {
        return command;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public int getUserType() {
        return userType;
    }

    public void setUserType(int userType) {
        this.userType = userType;
    }

    public int getServerId() {
        return serverId;
    }
    
    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public byte getMoneyType() {
        return moneyType;
    }

    public void setMoneyType(byte moneyType) {
        this.moneyType = moneyType;
    }

}
