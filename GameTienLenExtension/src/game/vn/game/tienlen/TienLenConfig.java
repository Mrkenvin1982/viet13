/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.vn.game.tienlen;

import game.vn.util.watchservice.PropertyConfigurator;

/**
 *
 * @author hoanghh
 */
public class TienLenConfig extends PropertyConfigurator{
    public static final String CONFIG_FILE_NAME = "tienlenmiennam.properties";
    public static final String PATH = "conf/";
    private static final String MONEY_CONTINUE_PLAYING = "game.tienlenmiennam.MoneyToContinuePlaying";
    private static final String MAX_VIEWER= "game.tienlenmiennam.MaxViewer";
    
    
    private final static TienLenConfig INSTANCE = new TienLenConfig();
    public TienLenConfig() {
        super(PATH,CONFIG_FILE_NAME);
    }
    /**
     * Singleton class. Get an instance of MauBinhConfig.
     *
     * @return An instance of ServerInfo.
     */
    public static TienLenConfig getInstance() {
        return INSTANCE;
    }

    /**
     * user dưới MoneyToContinuePlaying lần tiền cược sẽ bị kick ra khỏi bàn,
     * @return 
     */
    public int getMoneyToContinuePlaying() {
        return this.getIntAttribute(MONEY_CONTINUE_PLAYING,2);
    }

    public boolean isTest() {
        return getBooleanAttribute("IS_TEST");
    }

    public int getTestCase() {
        return getIntAttribute("TESTCASE", 0);
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
