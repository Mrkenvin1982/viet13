/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.vn.game.lieng;

import game.vn.util.watchservice.PropertyConfigurator;

/**
 * Các config trong game Lieng
 * @author tuanp
 */
public class LiengConfig  extends PropertyConfigurator{

    private static final String MIN_MONEY_BET = "game.lieng.MinMoneyBet";
    private static final String MAX_VIEWER= "game.lieng.MaxViewer";

    private final static LiengConfig INSTANCE = new LiengConfig("conf/","lieng.properties");
    
    public LiengConfig(String path, String nameFile) {
        super(path, nameFile);
    }
    /**
     * Singleton class. Get an instance of xamConfig.
     *
     * @return An instance of ServerInfo.
     */
    public static LiengConfig getInstance() {
        return INSTANCE;
    }

     /**
     * Min số tiền cược
     * @return 
     */
     public int getMinBet() {
        return this.getIntAttribute(MIN_MONEY_BET,1000);
    } 
     
    /**
     * số viewer tối đa cho game bài cào
     *
     * @return
     */
    public int getMaxViewer() {
        return getIntAttribute(MAX_VIEWER, 0);
    }
    public boolean isTest() {
        return getBooleanAttribute("IS_TEST");
    }

    int getTestCase() {
        return getIntAttribute("TESTCASE", 0);
    }
}
