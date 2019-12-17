/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.vn.game.xito;

import game.vn.util.watchservice.PropertyConfigurator;

/**
 *
 * @author tuanp
 */
public class XiToConfig extends PropertyConfigurator{

    private static final String PRE_FLOG_TIME = "game.xito.PreFlogTime";
    private static final String MAX_VIEWER= "game.xito.MaxViewer";

    private final static XiToConfig INSTANCE = new XiToConfig("conf/","xito.properties");

    public XiToConfig(String path, String nameFile) {
        super(path, nameFile);
    }
    
    /**
     * Singleton class. Get an instance of MauBinhConfig.
     *
     * @return An instance of ServerInfo.
     */
    public static XiToConfig getInstance() {
        return INSTANCE;
    }

    /**
     * Thời gian chọn lá bài lật ở đầu ván
     *
     * @return
     */
    public int getPreFlogTime() {
        return this.getIntAttribute(PRE_FLOG_TIME, 10);
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

    public String getTestCase() {
        return getStringAttribute("TESTCASE", "test");
    }
}
