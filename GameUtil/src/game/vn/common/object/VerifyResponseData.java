/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.vn.common.object;

/**
 *
 * @author anlh
 */
public class VerifyResponseData {

    /**
     * example: Xác thực thông tin thành công
     */
    private String message;
    /**
     * ID định danh User, example: wqoiu342io12
     */
    private String accountId;
    /**
     * tên hiển thị
     */
    private String displayName;
    /**
     * avatar
     */
    private String avatar = "";
    /**
     * email
     */
    private String email = "";
    /**
     * sđt
     */
    private String phone;
    /**
     * ngày đk
     */
    private String date;
    /**
     * loại tài khoản
     */
    private String type;

    private String merchantId = "";
    private String currency = "";
    private String channel;

    private boolean isVerify;
    
    private boolean isKYC;
       
    /**
     * @return the message
     */
    public String getMessage() {
        return message;
    }

    /**
     * @param message the message to set
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * @return the accountId
     */
    public String getAccountId() {
        return accountId;
    }

    /**
     * @param accountId the accountId to set
     */
    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMerchantId() {
        return merchantId;
    }

    public void setMerchantId(String merchantId) {
        this.merchantId = merchantId;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public boolean isVerify() {
        return isVerify;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public boolean isKYC() {
        return isKYC;
    }

    public void setKYC(boolean isKYC) {
        this.isKYC = isKYC;
    }
}
