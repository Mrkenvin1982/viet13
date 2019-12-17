/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.vn.game.baicao;

import game.vn.util.watchservice.PropertyConfigurator;


/**
 *
 * @author tuanp
 */
public class BaiCaoConfig extends PropertyConfigurator{
    
    private static final String MIN_TIME_SET_MONEY = "game.baicao.MinTimeSetMoney";
    private static final String MAX_VIEWER= "game.baicao.MaxViewer";
    
    private final static BaiCaoConfig INSTANCE=new BaiCaoConfig("conf/","baicao.properties");

     /**
     * Singleton class. Get an instance of BaicaoConfig.
     *
     * @return An instance of ServerInfo.
     */
    public static BaiCaoConfig getInstance() {
        return INSTANCE;
    }

    public BaiCaoConfig(String path, String nameFile){
        super(path, nameFile);
    }

    /**
     * Thời gian countDown tối thiểu có thể đổi cược
     * @return 
     */
    public int getMinTimeSetMoney() {
        return this.getIntAttribute(MIN_TIME_SET_MONEY,5);
    }

    public boolean isTest() {
        return getBooleanAttribute("IS_TEST");
    }

    int getTestCase() {
        return getIntAttribute("TESTCASE", 0);
    }
    /**
     * số viewer tối đa cho game bài cào
     * @return 
     */
    public int getMaxViewer(){
         return getIntAttribute(MAX_VIEWER,0);
    }
}
