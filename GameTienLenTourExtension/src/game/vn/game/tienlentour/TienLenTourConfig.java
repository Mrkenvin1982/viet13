/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.vn.game.tienlentour;

import game.vn.util.watchservice.PropertyConfigurator;

/**
 *
 * @author TuanP
 */
public class TienLenTourConfig extends PropertyConfigurator{
    public static final String CONFIG_FILE_NAME = "tienlentour.properties";
    public static final String PATH = "conf/";
    private static final String MAX_VIEWER= "game.tienlentour.MaxViewer";
    
    
    private final static TienLenTourConfig INSTANCE = new TienLenTourConfig();
    public TienLenTourConfig() {
        super(PATH,CONFIG_FILE_NAME);
    }
    /**
     * Singleton class. Get an instance of MauBinhConfig.
     *
     * @return An instance of ServerInfo.
     */
    public static TienLenTourConfig getInstance() {
        return INSTANCE;
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
