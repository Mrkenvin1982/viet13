/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.vn.util.watchservice;

import java.math.BigDecimal;

/**
 *
 * @author hanv
 */
public class TaiXiuConfig extends PropertyConfigurator {
    private static TaiXiuConfig instance = new TaiXiuConfig("conf/", "taixiu.properties");
    
    public TaiXiuConfig(String path, String nameFile) {
        super(path, nameFile);
    }
    
    public static TaiXiuConfig getInstance() {
        return instance;
    }
    
    public boolean isEnable() {
        return getBooleanAttribute("ENABLE", true);
    }
    
    public boolean isEnablePoint() {
        return getBooleanAttribute("ENABLE_POINT", true);
    }
    
    public boolean isEnableNohu() {
        return getBooleanAttribute("ENABLE_NOHU");
    }
    
    public BigDecimal getMinBet() {
        return new BigDecimal(getStringAttribute("MAX_BET", "1000"));
    }
    
    public BigDecimal getMaxBet() {
        return new BigDecimal(getStringAttribute("MAX_BET", "1000000000"));
    }
    
    public int getNohuTicketPrice() {
        return getIntAttribute("NOHU_TICKET_PRICE", 25000);
    }
    
    /**
     * Vị trí xuất hiện trong game
     * @return 
     */
    public int getIndex() {
        return getIntAttribute("INDEX", 1);
    }
    
    public int getIndexPoint() {
        return getIntAttribute("INDEX_POINT", 1);
    }
}
