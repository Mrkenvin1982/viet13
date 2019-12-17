/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.vn.common.lib.hazelcast;

import java.io.Serializable;

/**
 *  Quản lý single board đang chơi 
 * @author tuanp
 */
public class PlayingBoardManager implements Serializable{
    
    private String userId;
    private String nameLobby;
    private Board boardPlaying;

    public String getNameLobby() {
        return nameLobby;
    }

    public void setNameLobby(String nameLobby) {
        this.nameLobby = nameLobby;
    }

    public Board getBoardPlaying() {
        return boardPlaying;
    }

    public void setBoardPlaying(Board boardPlaying) {
        this.boardPlaying = boardPlaying;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
