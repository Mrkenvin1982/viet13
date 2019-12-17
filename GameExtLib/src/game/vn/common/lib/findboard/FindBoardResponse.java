/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.vn.common.lib.findboard;

import game.vn.common.lib.hazelcast.Board;
import java.util.List;

/**
 *
 * @author hanv
 */
public class FindBoardResponse {
    private String userId;
    private int serverId;
    private byte serviceId;
    private double betMoney;
    private List<Board> boards;

    public FindBoardResponse(FindBoardRequest request) {
        this.userId = request.getUserId();
        this.serverId = request.getServerId();
        this.serviceId = request.getServiceId();
        this.betMoney = request.getBetMoney();
    }
    
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public int getServerId() {
        return serverId;
    }

    public void setServerId(int serverId) {
        this.serverId = serverId;
    }

    public byte getServiceId() {
        return serviceId;
    }

    public void setServiceId(byte serviceId) {
        this.serviceId = serviceId;
    }

    public double getBetMoney() {
        return betMoney;
    }

    public void setBetMoney(double betMoney) {
        this.betMoney = betMoney;
    }

    public List<Board> getBoards() {
        return boards;
    }

    public void setBoards(List<Board> boards) {
        this.boards = boards;
    }
}
