/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.vn.common.lib.usermanager;

/**
 * kick user
 * @author tuanp
 */
public class KickUserInfor {
    private String idDBUser;
    private String reason;

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
}
