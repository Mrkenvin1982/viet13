/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.vn.common.lib.usermanager;

/**
 *
 * @author tuanp
 */
public class BanUserInfor {
    private String idDBUser;
    private String reason;
    private int type;//0:unban, 1: ban

    public String getIdDBUser() {
        return idDBUser;
    }

    public void setIdDBUser(String idDBUser) {
        this.idDBUser = idDBUser;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

}
