/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.vn.common.lib.vip;

import org.msgpack.annotation.Message;

/**
 *
 * @author anlh
 */
@Message
public class VipQueueObj {
    
    private String userid;
    private String serverId;
    private int command;
    private String data;
    private String lang;
    
    public static final int GET_VIP_INFO_OBJ = 0;
    public static final int GET_USER_VIP_DATA = 1;
    public static final int GET_Z_CASHOUT_INFO = 2;
    public static final int DO_Z_CASHOUT = 3;
    public static final int PUSH_UP_LEVEL = 4;
    public static final int PUSH_CASHOUT_BONUS_MONEY = 5;
    public static final int RELOAD_LIST_CONFIG = 6;

    /**
     * @return the userid
     */
    public String getUserid() {
        return userid;
    }

    /**
     * @param userid the userid to set
     */
    public void setUserid(String userid) {
        this.userid = userid;
    }

    /**
     * @return the serverId
     */
    public String getServerId() {
        return serverId;
    }

    /**
     * @param serverId the serverId to set
     */
    public void setServerId(String serverId) {
        this.serverId = serverId;
    }

    /**
     * @return the command
     */
    public int getCommand() {
        return command;
    }

    /**
     * @param command the command to set
     */
    public void setCommand(int command) {
        this.command = command;
    }

    /**
     * @return the data
     */
    public String getData() {
        return data;
    }

    /**
     * @param data the data to set
     */
    public void setData(String data) {
        this.data = data;
    }

    /**
     * @return the lang
     */
    public String getLang() {
        return lang;
    }

    /**
     * @param lang the lang to set
     */
    public void setLang(String lang) {
        this.lang = lang;
    }

}
