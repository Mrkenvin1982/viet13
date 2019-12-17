/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.vn.common.lib.api;

import java.util.List;

/**
 *
 * @author tuanp
 */
public class CCUInfor {
    // Thời gian tạo
    private long createdAt;
    //tự động bên server tạo unit
    private String requestId;
    //id con game (Z88)
    private int connectionId;
    //id small game in z88
    private int serviceId;
    //số lượng user
    private int count;
    //số lượng bot
    private int countBot;
   
    private List<BotCCuDetail> botDetail;
    private List<UserCCuDetail> userDetail;

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public int getConnectionId() {
        return connectionId;
    }

    public void setConnectionId(int connectionId) {
        this.connectionId = connectionId;
    }

    public int getServiceId() {
        return serviceId;
    }

    public void setServiceId(int serviceId) {
        this.serviceId = serviceId;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public int getCountBot() {
        return countBot;
    }

    public void setCountBot(int countBot) {
        this.countBot = countBot;
    }

    public List<BotCCuDetail> getBotDetail() {
        return botDetail;
    }

    public void setBotDetail(List<BotCCuDetail> botDetail) {
        this.botDetail = botDetail;
    }

    public List<UserCCuDetail> getUserDetail() {
        return userDetail;
    }

    public void setUserDetail(List<UserCCuDetail> userDetail) {
        this.userDetail = userDetail;
    }
}

