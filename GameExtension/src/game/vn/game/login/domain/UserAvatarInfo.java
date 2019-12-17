/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.vn.game.login.domain;

import java.io.Serializable;

/**
 * Chứa thông tin avatar của user
 * @author anlh
 */
public class UserAvatarInfo implements Serializable {
    
    private int userId;
    private int avatarId;

    /**
     * @return the userId
     */
    public int getUserId() {
        return userId;
    }

    /**
     * @param userId the userId to set
     */
    public void setUserId(int userId) {
        this.userId = userId;
    }

    /**
     * @return the avatarId
     */
    public int getAvatarId() {
        return avatarId;
    }

    /**
     * @param avatarId the avatarId to set
     */
    public void setAvatarId(int avatarId) {
        this.avatarId = avatarId;
    }
    
}
