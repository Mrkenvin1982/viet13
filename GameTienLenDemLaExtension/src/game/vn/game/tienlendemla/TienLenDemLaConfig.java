/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.vn.game.tienlendemla;

import game.vn.util.watchservice.PropertyConfigurator;

/**
 *
 * @author hoanghh
 */
public class TienLenDemLaConfig extends PropertyConfigurator{
    public static final String CONFIG_FILE_NAME = "tienlenmiennamdemla.properties";
    public static final String PATH = "conf/";
    private static final String MONEY_CONTINUE_PLAYING = "game.tienlenmiennamdemla.MoneyToContinuePlaying";
    private static final String IS_MOVE_FIRST_SMALL_CARD="game.tienlenmiennamdemla.isMoveFirstSmallCard";
    private static final String MAX_VIEWER= "game.tienlenmiennamdemla.MaxViewer";
    
    
    private final static TienLenDemLaConfig INSTANCE = new TienLenDemLaConfig();
    public TienLenDemLaConfig() {
        super(PATH,CONFIG_FILE_NAME);
    }
    /**
     * Singleton class. Get an instance of MauBinhConfig.
     *
     * @return An instance of ServerInfo.
     */
    public static TienLenDemLaConfig getInstance() {
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
     * =true: user có bài nhỏ dánh trước không phân biệt thắng thua
     * @return 
     */
    public boolean isMoveFirstSmallCard(){
       return getBooleanAttribute(IS_MOVE_FIRST_SMALL_CARD); 
    }  

    /**
     * số viewer tối đa cho game bài cào
     *
     * @return
     */
    public int getMaxViewer() {
        return getIntAttribute(MAX_VIEWER, 0);
    }
}
