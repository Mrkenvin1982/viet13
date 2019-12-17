/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.vn.common.lib.ranking;

import org.msgpack.annotation.Message;

/**
 *
 * @author anlh
 */
@Message
public class LeaderboardObject {
    
    private String userid;
    private String serverId;
    private int command;
    private int serviceId;
    private int page;
    private String data;
    private boolean status; // user co tham gia bxh hay ko
    
    /**
     * Lay danh sach top user hien tai
     */
    public static final int GET_CURRENT_LEADERBOARD = 0;
    /**
     * Lay danh sach top user tuan truoc
     */
    public static final int GET_LASTWEEK_LEADERBOARD = 1;
    /**
     * Tắt/mở tham gia event đua top
     */
    public static final int SWITCH_EVENT_JOIN_STATUS = 2;

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
     * 
     * @return 
     */
    public boolean getStatus() {
        return status;
    }

    /**
     * 
     * @param status 
     */
    public void setStatus(boolean status) {
        this.status = status;
    }

    /**
     * 
     * @return 
     */
    public int getServiceId() {
        return serviceId;
    }

    /**
     * 
     * @param serviceId 
     */
    public void setServiceId(int serviceId) {
        this.serviceId = serviceId;
    }

    /**
     * 
     * @return 
     */
    public int getPage() {
        return page;
    }

    /**
     * 
     * @param page 
     */
    public void setPage(int page) {
        this.page = page;
    }

}
