/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.vn.common.lib.log;

/**
 * thông tin chi tiết trong ván chơi
 * @author tuanp
 */
public class BoardDetail {
    
    private String playerId;
    //tiền của user trước khi trừ
    private double creditBefore;
    //tiền của user sau khi update
    private double creditAfter;
    //tiền thắng - thua nếu có phát sinh
    private double value;
    //thuế
    private double tax;
    // action của user
    private int actionId;
    //danh sách cardId
    private byte[] optionalArrayData;
    private String logDate;
    private String playerName;

    public double getCreditBefore() {
        return creditBefore;
    }

    public void setCreditBefore(double creditBefore) {
        this.creditBefore = creditBefore;
    }

    public double getCreditAfter() {
        return creditAfter;
    }

    public void setCreditAfter(double creditAfter) {
        this.creditAfter = creditAfter;
    }
    
    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public int getActionId() {
        return actionId;
    }

    public void setActionId(int actionId) {
        this.actionId = actionId;
    }

    public byte[] getOptionalArrayData() {
        return optionalArrayData;
    }

    public void setOptionalArrayData(byte[] optionalArrayData) {
        this.optionalArrayData = optionalArrayData;
    }
    
    public String getLogDate() {
        return logDate;
    }

    public void setLogDate(String logDate) {
        this.logDate = logDate;
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public String getPlayerId() {
        return playerId;
    }

    public void setPlayerId(String playerId) {
        this.playerId = playerId;
    }

    public double getTax() {
        return tax;
    }

    public void setTax(double tax) {
        this.tax = tax;
    }

}
