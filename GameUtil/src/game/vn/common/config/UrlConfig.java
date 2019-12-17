/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.vn.common.config;

import game.vn.util.watchservice.PropertyConfigurator;

/**
 *
 * @author tuanp
 */
public class UrlConfig extends PropertyConfigurator{
    
    private  static final String URL_GET_HISTORY="url.getHistory";
    
    private final static UrlConfig INSTANCE = new UrlConfig("conf/","url.properties");

    public UrlConfig(String path, String nameFile) {
        super(path, nameFile);
    }
    
    public static UrlConfig getInstance(){
        return INSTANCE;
    }
    
    public String getUrlHistory(){
        return this.getStringAttribute(URL_GET_HISTORY);
    }
    
    public String getUrlBackend() {
        return getStringAttribute("url.backend", "http://test2.z88.net:9901/z88-backend/");
    }

    public String getUrlPassportConfig() {
        return getStringAttribute("url.passport.config", "https://api-psp.devuid.club/V1/getConfigs");
    }

    public String getUrlPassportPaymentList() {
        return getStringAttribute("url.passport.payment", "https://api-psp.devuid.club/V1/getPaymentList");
    }

    public String getUrlPassportWithdraw() {
        return getStringAttribute("url.passport.withdraw", "https://api-psp.devuid.club/V1/wallet/withdraw");
    }
    
    public String getUrlPassportBankingWithdraw() {
        return getStringAttribute("url.passport.withdraw.banking", "https://api-psp.devuid.club/V1/ezpay/withdraw");
    }

    public String getDefaultAvatarUrl() {
        return getStringAttribute("url.avatar.default", "https://psp.devuid.club/assets/images/avatar/Avatar_doanhnhannam@2x.png");
    }

    public String getVerifyAccessTokenUrl() {
        return getStringAttribute("url.verify.access.token", "http://account-sandbox.z88.net/VerifyAccessToken");
    }
    public String getMerchantVerifyAccessTokenUrl(String merchantId) {
        return getStringAttribute("url.verify." + merchantId, "http://integration.aqzbouat.com/1/Z88Auth");
    }
    
    public String getRandomVerifyAccessTokenUrl(String merchantId) {
        return getStringAttribute("url.verify." + merchantId, "account.devuid.club");
    }
    
    public String getVN88VerifyAccessTokenUrl() {
        return getStringAttribute("url.verify.vn88.token", "http://integration.aqzbouat.com/56/Z88Auth");
    }

    public String getPaymentInfoUrl() {
        return getStringAttribute("url.payment.info", "https://api-psp.devuid.club/V1/getPaymentList");
    }
    
    public String getRechargeCardUrl() {
        return getStringAttribute("url.recharge.info", "https://api-psp.devuid.club/V1/card/scratchcard");
    }
    
    public String getLinkFacebook() {
        return getStringAttribute("url.facebook.info", "https://account.devuid.club/LinkFacebook");
    }
    
    public String getGameListMoon() {
        return getStringAttribute("url.moon.game.list", "https://gameapi.devmoo.club/moon/metro/get-game-list");
    }
    
    public String getGameListHandicap() {
        return getStringAttribute("url.handicap.game.list", "http://sv-footballex.devcas.club/metro/get-game-list");
    }   
}
