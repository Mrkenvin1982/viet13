/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.vn.game.blackjack;

import game.vn.util.watchservice.PropertyConfigurator;

/**
 *
 * @author tuanp
 */
public class BlackJackConfig extends PropertyConfigurator{
    
    private final static BlackJackConfig INSTANCE=new BlackJackConfig("conf/","blackjack.properties");
    
    private static final String MIN_TIME_SET_MONEY = "game.blackjack.MinTimeSetMoney";
    private static final String MAX_VIEWER= "game.blackjack.MaxViewer";
    private static final String MIN_POINT= "game.blackjack.minPoint";

    public BlackJackConfig(String path, String nameFile) {
        super(path, nameFile);
    }
    
    public static BlackJackConfig getInstance(){
        return INSTANCE;
    }

    /**
     * Thời gian countDown tối thiểu có thể đổi cược
     * @return 
     */
    public int getMinTimeSetMoney() {
        return this.getIntAttribute(MIN_TIME_SET_MONEY,5);
    }
    
    /**
     * số viewer tối đa cho game bài cào
     * @return 
     */
    public int getMaxViewer(){
         return getIntAttribute(MAX_VIEWER,0);
    }
    public boolean isTest() {
        return getBooleanAttribute("IS_TEST");
    }

    public String getTestCase() {
        return getStringAttribute("TESTCASE", "test");
    }

    public int getMinPoint(){
        return getIntAttribute(MIN_POINT, 16);
    }
}
