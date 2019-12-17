/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.vn.game.phom;

import game.vn.util.watchservice.PropertyConfigurator;

/**
 *
 * @author tuanp
 */
public class PhomConfig extends PropertyConfigurator{
      
    private static final String MAX_VIEWER= "game.phom.MaxViewer";
    private final static PhomConfig INSTANCE=new PhomConfig("conf/","phom.properties");

     /**
     * Singleton class. Get an instance of BaicaoConfig.
     *
     * @return An instance of ServerInfo.
     */
    public static PhomConfig getInstance() {
        return INSTANCE;
    }

    public PhomConfig(String path, String nameFile){
        super(path, nameFile);
    }

    /**
     * số viewer tối đa cho game 
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
