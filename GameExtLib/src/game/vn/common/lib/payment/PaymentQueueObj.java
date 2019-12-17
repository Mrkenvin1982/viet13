/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.vn.common.lib.payment;

/**
 *
 * @author anlh
 */
public class PaymentQueueObj {
    
    public static final int PAYMENT_FLOW_DEPOSIT = 1;
    public static final int PAYMENT_FLOW_TRANSFER = 2;
    public static final int PAYMENT_FLOW_WITHDRAW = 3;
    public static final int PAYMENT_FLOW_CASHBACK = 4;

    private String sessionId;
    private long createdAt;//(unixTime),
    private String requestId;//(unique),
    private String playerId;
    private String playerName;
    private String paymentMethod;//(sms,card,bank,...),
    private String paymentType;//(9029,viettel,tpbank,...),
    private int paymentFlow;//1-deposit, 2-transfer, 3-withdraw, 4-cashback
    private double money;
    private String currency;// (vnd, usd),
    private double credit;//(point in game),
    private String unit;//(gold,diamond,ruby,win,....),
    private String orderId;//(z88 orderId),
    private String transaction;//(payment transaction),
    private String supplierTransaction;
    private String supplierResponse;
    private String connectionId = "1";
    private int status;//"":int(1: success, 0: fail),
    private String merchantId = "";
    private PaymentInfo paymentInfo;//"":jsonObject{""playerName"":"""",""balance"":"""",...}    
    private String info;
    private String channel;

    /**
     * @return the sessionId
     */
    public String getSessionId() {
        return sessionId;
    }

    /**
     * @param sessionId the sessionId to set
     */
    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    /**
     * @return the createdAt
     */
    public long getCreatedAt() {
        return createdAt;
    }

    /**
     * @param createdAt the createdAt to set
     */
    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * @return the requestId
     */
    public String getRequestId() {
        return requestId;
    }

    /**
     * @param requestId the requestId to set
     */
    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    /**
     * @return the playerId
     */
    public String getPlayerId() {
        return playerId;
    }

    /**
     * @param playerId the playerId to set
     */
    public void setPlayerId(String playerId) {
        this.playerId = playerId;
    }

    /**
     * @return the playerName
     */
    public String getPlayerName() {
        return playerName;
    }

    /**
     * @param playerName the playerName to set
     */
    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    /**
     * @return the paymentMethod
     */
    public String getPaymentMethod() {
        return paymentMethod;
    }

    /**
     * @param paymentMethod the paymentMethod to set
     */
    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    /**
     * @return the paymentType
     */
    public String getPaymentType() {
        return paymentType;
    }

    /**
     * @param paymentType the paymentType to set
     */
    public void setPaymentType(String paymentType) {
        this.paymentType = paymentType;
    }

    /**
     * @return the money
     */
    public double getMoney() {
        return money;
    }

    /**
     * @param money the money to set
     */
    public void setMoney(double money) {
        this.money = money;
    }

    /**
     * @return the currency
     */
    public String getCurrency() {
        return currency;
    }

    /**
     * @param currency the currency to set
     */
    public void setCurrency(String currency) {
        this.currency = currency;
    }

    /**
     * @return the credit
     */
    public double getCredit() {
        return credit;
    }

    /**
     * @param credit the credit to set
     */
    public void setCredit(double credit) {
        this.credit = credit;
    }

    /**
     * @return the unit
     */
    public String getUnit() {
        return unit;
    }

    /**
     * @param unit the unit to set
     */
    public void setUnit(String unit) {
        this.unit = unit;
    }

    /**
     * @return the orderId
     */
    public String getOrderId() {
        return orderId;
    }

    /**
     * @param orderId the orderId to set
     */
    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    /**
     * @return the transaction
     */
    public String getTransaction() {
        return transaction;
    }

    /**
     * @param transaction the transaction to set
     */
    public void setTransaction(String transaction) {
        this.transaction = transaction;
    }

    /**
     * @return the supplierTransaction
     */
    public String getSupplierTransaction() {
        return supplierTransaction;
    }

    /**
     * @param supplierTransaction the supplierTransaction to set
     */
    public void setSupplierTransaction(String supplierTransaction) {
        this.supplierTransaction = supplierTransaction;
    }

    /**
     * @return the supplierResponse
     */
    public String getSupplierResponse() {
        return supplierResponse;
    }

    /**
     * @param supplierResponse the supplierResponse to set
     */
    public void setSupplierResponse(String supplierResponse) {
        this.supplierResponse = supplierResponse;
    }

    /**
     * @return the status
     */
    public int getStatus() {
        return status;
    }

    /**
     * @param status the status to set
     */
    public void setStatus(int status) {
        this.status = status;
    }

    /**
     * @return the paymentInfo
     */
    public PaymentInfo getPaymentInfo() {
        return paymentInfo;
    }

    /**
     * @param paymentInfo the paymentInfo to set
     */
    public void setPaymentInfo(PaymentInfo paymentInfo) {
        this.paymentInfo = paymentInfo;
    }

    /**
     * @return the connectionId
     */
    public String getConnectionId() {
        return connectionId;
    }

    /**
     * @param connectionId the connectionId to set
     */
    public void setConnectionId(String connectionId) {
        this.connectionId = connectionId;
    }

    public void setPaymentFlow(int paymentFlow) {
        this.paymentFlow = paymentFlow;
    }

    public String getMerchantId() {
        return merchantId;
    }

    public void setMerchantId(String merchantId) {
        this.merchantId = merchantId;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }
}
