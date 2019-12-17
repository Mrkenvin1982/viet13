/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.vn.common.lib.payment;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 *
 * @author minhvtd
 */
public class UserBalanceUpdate {
    
    public static final int PAYMENT_FLOW_DEPOSIT = 1;
    public static final int PAYMENT_FLOW_TRANSFER = 2;
    public static final int PAYMENT_FLOW_WITHDRAW = 3;
    public static final int PAYMENT_FLOW_CASHBACK = 4;
    public static final int PAYMENT_FLOW_PLAY_GAME = 5;
    public static final int PAYMENT_FLOW_ADMIN = 6;
    public static final int PAYMENT_FLOW_EVENT = 7;
    public static final int PAYMENT_FLOW_CONVERT = 8;

    private int serviceId;
    private String playerId;
    private String email;
    private BigDecimal lastBalance;
    private BigDecimal change;
    private BigDecimal balance;
    private String currency;
    private int connectionId;
    private String description;
    private long createdAt;
    private String sessionId;
    private String requestId;
    private String logId;
    private String unit;
    private int paymentFlow;
    private String channel;

    public String getPlayerId() {
        return playerId;
    }

    public void setPlayerId(String playerId) {
        this.playerId = playerId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public BigDecimal getLastBalance() {
        return lastBalance;
    }

    public void setLastBalance(BigDecimal lastBalance) {
        this.lastBalance = lastBalance;
    }
    
    public void setLastBalance(double lastBalance) {
        this.lastBalance = new BigDecimal(lastBalance).setScale(2, RoundingMode.HALF_EVEN);
    }

    public BigDecimal getChange() {
        return change;
    }
    
    public void setChange(BigDecimal change) {
        this.change = change;
    }

    public void setChange(double change) {
        this.change = new BigDecimal(change).setScale(2, RoundingMode.HALF_EVEN);
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = new BigDecimal(balance).setScale(2, RoundingMode.HALF_EVEN);
    }
    
    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public int getConnectionId() {
        return connectionId;
    }

    public void setConnectionId(int connectionId) {
        this.connectionId = connectionId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public int getPaymentFlow() {
        return paymentFlow;
    }

    public void setPaymentFlow(int paymentFlow) {
        this.paymentFlow = paymentFlow;
    }

    public int getServiceId() {
        return serviceId;
    }

    public void setServiceId(int serviceId) {
        this.serviceId = serviceId;
    }

    public String getLogId() {
        return logId;
    }

    public void setLogId(String logId) {
        this.logId = logId;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

}