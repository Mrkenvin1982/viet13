/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.vn.common.object;

import java.util.Date;

public class PointReceiveInfo {
    private String user_id;
    private int type;
    private Date time_receive;
    private int receiveCount;

    public PointReceiveInfo(String user_id, int type, Date time_receive, int receiveCount) {
        this.user_id = user_id;
        this.type = type;
        this.time_receive = time_receive;
        this.receiveCount = receiveCount;
    }
    
    
    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public Date getTime_receive() {
        return time_receive;
    }

    public void setTime_receive(Date time_receive) {
        this.time_receive = time_receive;
    }

    public int getReceiveCount() {
        return receiveCount;
    }

    public void setReceiveCount(int receiveCount) {
        this.receiveCount = receiveCount;
    }
}
