/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.vn.common.config;

import game.vn.util.watchservice.PropertyConfigurator;

/**
 *
 * @author hanv
 */
public class LeaderBoardConfig extends PropertyConfigurator {

    private final static LeaderBoardConfig INSTANCE = new LeaderBoardConfig("conf/","leaderboard.properties");
    
    public LeaderBoardConfig(String path, String nameFile) {
        super(path, nameFile);
    }
    
    public static LeaderBoardConfig getInstance(){
        return INSTANCE;
    }
    
}
