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
public class IosConfig extends PropertyConfigurator {
    private final static IosConfig INSTANCE = new IosConfig("conf/", "ios.properties");
    
    public static IosConfig getInstance() {
        return INSTANCE;
    }

    public IosConfig(String path, String nameFile) {
        super(path, nameFile);
    }
    
    public String getAppSecret() {
        return getStringAttribute("app.secret", "4a81c353e2054153ac970bb0d5f0fae5");
    }
    
    public String getIosVerifyReceiptUrl() {
        return getStringAttribute("url.verify.receipt", "https://buy.itunes.apple.com/verifyReceipt");
    }
    
    public String getProducts() {
        return getStringAttribute("products");
    }
}
