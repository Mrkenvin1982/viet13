/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.vn.common.lib.iap;

/**
 *
 * @author hanv
 */
public class GGProductPurchase {
    private String kind;
    private String orderId;
    private String developerPayload;
    private int acknowledgementState;   // 0.Yet to be acknowledged     1.Acknowledged
    private int consumptionState;       // 0.Yet to be consumed         1.Consumed
    private int purchaseState;          // 0.Purchased      1.Canceled      2.Pending
    private int purchaseType;           // 0.Test   1.Promo     2.Rewarded
    private long purchaseTimeMillis;
    
    public String getKind() {
        return kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getDeveloperPayload() {
        return developerPayload;
    }

    public void setDeveloperPayload(String developerPayload) {
        this.developerPayload = developerPayload;
    }

    public int getAcknowledgementState() {
        return acknowledgementState;
    }

    public void setAcknowledgementState(int acknowledgementState) {
        this.acknowledgementState = acknowledgementState;
    }

    public int getConsumptionState() {
        return consumptionState;
    }

    public void setConsumptionState(int consumptionState) {
        this.consumptionState = consumptionState;
    }

    public int getPurchaseState() {
        return purchaseState;
    }

    public void setPurchaseState(int purchaseState) {
        this.purchaseState = purchaseState;
    }

    public int getPurchaseType() {
        return purchaseType;
    }

    public void setPurchaseType(int purchaseType) {
        this.purchaseType = purchaseType;
    }

    public long getPurchaseTimeMillis() {
        return purchaseTimeMillis;
    }

    public void setPurchaseTimeMillis(long purchaseTimeMillis) {
        this.purchaseTimeMillis = purchaseTimeMillis;
    }
    
    public boolean isAcknowledged() {
        return acknowledgementState == 1;
    }
    
    public boolean isConsumed() {
        return consumptionState == 1;
    }
    
    public boolean isPurchased() {
        return purchaseState == 0;
    }
    
    public boolean isCanceled() {
        return purchaseState == 1;
    }
    
    public boolean isPending() {
        return purchaseState == 2;
    }
}
