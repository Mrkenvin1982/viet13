/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.vn.common.object.boardhistory;

import game.vn.common.constant.MoneyContants;
import java.util.List;

/**
 * Thông tin cần lưu khi update tiền của user
 * log ván chơi
 * @author hanv
 */
public class UserMoneyLog {
    //mã bàn
    int boardId;
    //id DB của user
    private String userId="";
    private String userName="";
    //tiền của user
    private double money;
    //tiền thắng - thua
    private double value;
    //thuế
    private double tax;
    //server boardId
    private int serverId;
    // lý do phát sinh tiền
    private int reasonId;
    private String description="";
    //danh sách cardId
    private List<Short> optionalArrayData;
    //loại tiền trong ván
    private  int moneyType = MoneyContants.POINT;

    /**
     * ngày ghi boardlog: ddMMyyyy
     */
    private String _boardLogDate;
    private int serviceId;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String _userName) {
        this.userName = _userName;
    }

    public double getMoney() {
        return money;
    }

    public void setMoney(double _money) {
        this.money = _money;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double _value) {
        this.value = _value;
    }

    public double getTax() {
        return tax;
    }

    public void setTax(double _tax) {
        this.tax = _tax;
    }

    public int getServerId() {
        return serverId;
    }

    public void setServerId(int _serverId) {
        this.serverId = _serverId;
    }

    public int getReasonId() {
        return reasonId;
    }

    public void setReasonId(int _reasonId) {
        this.reasonId = _reasonId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getBoardLogDate() {
        return _boardLogDate;
    }

    public void setBoardLogDate(String _boardLogDate) {
        this._boardLogDate = _boardLogDate;
    }

    public int getServiceId() {
        return serviceId;
    }

    public void setServiceId(int serviceId) {
        this.serviceId = serviceId;
    }

    public int getMoneyType() {
        return moneyType;
    }

    public void setMoneyType(int moneyType) {
        this.moneyType = moneyType;
    }

    public int getBoardId() {
        return boardId;
    }

    public void setBoardId(int boardId) {
        this.boardId = boardId;
    }

    public List<Short> getOptionalArrayData() {
        return optionalArrayData;
    }

    public void setOptionalArrayData(List<Short> optionalArrayData) {
        this.optionalArrayData = optionalArrayData;
    }
    
}