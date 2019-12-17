/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.vn.game.sam;

import game.vn.util.watchservice.PropertyConfigurator;

/**
 *
 * @author tuanp
 */
public class SamConfig extends PropertyConfigurator{
    private final static SamConfig INSTANCE=new SamConfig("conf/","sam.properties");
    private static final String XAM_TIME_LIMIT = "game.xam.xamTimeLimit";
    private static final String IS_MOVE_FIRST_SMALL_CARD="game.xam.isMoveFirstSmallCard";
    private static final String MIN_BET_MONEY = "game.xam.minBetMoney";
    private static final String MAX_VIEWER= "game.xam.MaxViewer";
    
    public SamConfig(String path, String nameFile) {
        super(path, nameFile);
    }
    
     /**
     * Singleton class. Get an instance of xamConfig.
     *
     * @return An instance of ServerInfo.
     */
    public static SamConfig getInstance() {
        return INSTANCE;
    }

    /**
     * Thời gian limit cho việc Báo hay Hủy Xâm: 20s
     * @return 
     */
    public int getXamTimeLimit() {
        return this.getIntAttribute(XAM_TIME_LIMIT,20000);
    }

    public boolean isTest() {
        return getBooleanAttribute("IS_TEST");
    }

    public int getTestCase() {
        return getIntAttribute("TESTCASE", 0);
    }
    /**
     * =true: user có bài nhỏ dánh trước không phân biệt thắng thua
     *
     * @return
     */
    public boolean isMoveFirstSmallCard() {
        return getBooleanAttribute(IS_MOVE_FIRST_SMALL_CARD);
    }
    /**
     * Mức cược tối thiểu của bàn
     * @return 
     */
    public int getMinBetMoney() {
        return this.getIntAttribute(MIN_BET_MONEY,1000);
    }

    /**
     * số viewer tối đa cho game 
     *
     * @return
     */
    public int getMaxViewer() {
        return getIntAttribute(MAX_VIEWER, 0);
    }
}
