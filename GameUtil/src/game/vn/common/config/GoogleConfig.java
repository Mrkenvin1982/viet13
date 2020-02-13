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
    
    public String getPackageName() {
        return getStringAttribute("packagename", "com.funfun.thirteen");
    }

    public String getAppName() {
        return getStringAttribute("appname", "Thirteen Card");
    }

    public String getProducts() {
        return getStringAttribute("products");
    }
}
