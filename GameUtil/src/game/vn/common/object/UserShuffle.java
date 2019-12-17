/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.vn.common.object;

/**
 * 
 * @author hanv
 */
public class UserShuffle {
    private final String userId;
    private String lastBoard;

    public UserShuffle(String userId, String lastBoard) {
        this.userId = userId;
        this.lastBoard = lastBoard;
    }

    public String getUserId() {
        return userId;
    }

    public String getLastBoard() {
        return lastBoard;
    }

    public void setLastBoard(String lastBoard) {
        this.lastBoard = lastBoard;
    }
}
