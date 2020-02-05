/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.vn.common.config;

import game.vn.util.watchservice.PropertyConfigurator;

/**
 *
 * @author hanv
 */
public class GoogleConfig extends PropertyConfigurator {
    private final static GoogleConfig INSTANCE = new GoogleConfig("conf/", "google.properties");
    
    public static GoogleConfig getInstance() {
        return INSTANCE;
    }

    public GoogleConfig(String path, String nameFile) {
        super(path, nameFile);
    }
    
    public String getAuthorizationUrl() {
        return getStringAttribute("url.authorization", "https://accounts.google.com/o/oauth2/token");
    }
    
    public String getListProductUrl() {
        return getStringAttribute("url.products", "https://www.googleapis.com/androidpublisher/v3/applications/%s/inappproducts");
    }
    
    public String getPurchaseUrl() {
        return getStringAttribute("url.purchase", "https://www.googleapis.com/androidpublisher/v3/applications/%s/purchases/products/%s/tokens/%s");
    }
    
    public String getPackageName() {
        return getStringAttribute("packagename", "com.funfun.thirteen");
    }

    public String getAuthorizationCode() {
        return getStringAttribute("authcode");
    }
    
    public String getAccessToken() {
        return getStringAttribute("accesstoken");
    }
    
    public String getClientId() {
        return getStringAttribute("clientid", "964907037117-dsnq9546ldq8o8vi2jocp2n1jdkrhlg0.apps.googleusercontent.com");
    }
    
    public String getClientSecret() {
        return getStringAttribute("clientsecret", "H-h3E8m1mvdkzbe4UH-TOa5r");
    }
    
    public String getProducts() {
        return getStringAttribute("products");
    }
}
