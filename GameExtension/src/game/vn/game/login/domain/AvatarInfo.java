/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.vn.game.login.domain;

import java.io.Serializable;

/**
 *
 * Chứa thông tin 1 avatar của hệ thống
 * 
 * @author anlh
 */
public class AvatarInfo implements Serializable {
    
    private int avatarId;
    private String avatarName;
    private String avatarDesc;
    private int version;
    private boolean isEnable;   

    public AvatarInfo(int avatarId, String avatarName, String avatarDesc, int version, boolean isEnable) {
        this.avatarId = avatarId;
        this.avatarName = avatarName;
        this.avatarDesc = avatarDesc;
        this.version = version;
        this.isEnable = isEnable;
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

    /**
     * @return the avatarName
     */
    public String getAvatarName() {
        return avatarName;
    }

    /**
     * @param avatarName the avatarName to set
     */
    public void setAvatarName(String avatarName) {
        this.avatarName = avatarName;
    }

    /**
     * @return the avatarDesc
     */
    public String getAvatarDesc() {
        return avatarDesc;
    }

    /**
     * @param avatarDesc the avatarDesc to set
     */
    public void setAvatarDesc(String avatarDesc) {
        this.avatarDesc = avatarDesc;
    }

    /**
     * @return the version
     */
    public int getVersion() {
        return version;
    }

    /**
     * @param version the version to set
     */
    public void setVersion(int version) {
        this.version = version;
    }

    /**
     * @return the isEnable
     */
    public boolean isIsEnable() {
        return isEnable;
    }

    /**
     * @param isEnable the isEnable to set
     */
    public void setIsEnable(boolean isEnable) {
        this.isEnable = isEnable;
    }


}
