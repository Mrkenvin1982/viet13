/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.vn.common.object.boardhistory;

import game.vn.common.constant.MoneyContants;
import game.vn.common.lib.log.InvoiceDetail;
import java.util.ArrayList;
import java.util.List;

/**
 * Lịch sử chơi game của user
 * @author tuanp
 */
public class UserMoneyHistory {
    //mã bàn
    int boardId;
    //id DB của user
    private String userId="";
    //tiền của user
    private double money;
    //server boardId
    private int serverId;
    private String description="";
    //loại tiền trong ván
    private  int moneyType = MoneyContants.POINT;
    //thông tin khi có phát sinh tiền trong game(su dung cho history của client)
    private List<InvoiceDetail> invoiceDetail = new ArrayList<>();

    /**
     * ngày ghi boardlog: ddMMyyyy
     */
    private String boardLogDate;
    private int serviceId;

    public int getBoardId() {
        return boardId;
    }

    public void setBoardId(int boardId) {
        this.boardId = boardId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
    
    public double getMoney() {
        return money;
    }

    public void setMoney(double money) {
        this.money = money;
    }

    public int getServerId() {
        return serverId;
    }

    public void setServerId(int serverId) {
        this.serverId = serverId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
    
    public int getMoneyType() {
        return moneyType;
    }

    public void setMoneyType(int moneyType) {
        this.moneyType = moneyType;
    }

    public String getBoardLogDate() {
        return boardLogDate;
    }

    public void setBoardLogDate(String _boardLogDate) {
        this.boardLogDate = _boardLogDate;
    }

    public int getServiceId() {
        return serviceId;
    }

    public void setServiceId(int serviceId) {
        this.serviceId = serviceId;
    }

    public List<InvoiceDetail> getInvoiceDetail() {
        return invoiceDetail;
    }

    public void setInvoiceDetail(List<InvoiceDetail> invoiceDetail) {
        this.invoiceDetail = invoiceDetail;
    }
}
