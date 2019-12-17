/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.vn.common.lib.hazelcast;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Quản lý multi bàn đang chơi poker
 * @author tuanp
 */
public class PlayingPokerBoardManager implements Serializable{
    
    /**
     * key: name room, values: thong tin playing board
     */
    private Map<String,Board> playingBoards ;
    private String userId;

    public PlayingPokerBoardManager(){
        playingBoards = new HashMap();
    }
    public Map<String, Board> getPlayingBoards() {
        return playingBoards;
    }

    public void setPlayingBoards(Map<String, Board> playingBoards) {
        this.playingBoards = playingBoards;
    }

    
    public void addPlayingBoards(String nameBoard,Board playingBoard){
        this.playingBoards.put(nameBoard, playingBoard);
    }
    
    public void removePlayingBoards(String boardName){
        if(this.playingBoards.containsKey(boardName)){
            this.playingBoards.remove(boardName);
        }
    }
    
    public Board getPlayingBoard (String boardName){
        if(this.playingBoards.containsKey(boardName)){
            return this.playingBoards.get(boardName);
        }
        return null;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
    
}
