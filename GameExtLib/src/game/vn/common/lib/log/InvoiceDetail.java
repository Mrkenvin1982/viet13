/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.vn.common.lib.log;

/**
 * Thông tin chi tiết của user khi có phát sinh tiền
 * @author tuanp
 */
public class InvoiceDetail {
    private String logId;
    private String playerId;
    //tiền của user trước khi trừ
    private double creditBefore;
    //tiền của user sau khi update
    private double creditAfter;
    //tiền thắng - thua
    private double value;
    // lý do phát sinh tiền
    private int reasonId;
    //thuế
    private double rake;
    //danh sách cardId
    private byte[] optionalArrayData;
    private String logDate;
    private String playerName;

    public void setLogId(String logId) {
        this.logId = logId;
    }

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

    public int getReasonId() {
        return reasonId;
    }

    public void setReasonId(int reasonId) {
        this.reasonId = reasonId;
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

    public double getRake() {
        return rake;
    }

    public void setRake(double rake) {
        this.rake = rake;
    }

    public String getLogId() {
        return logId;
    } 
}
