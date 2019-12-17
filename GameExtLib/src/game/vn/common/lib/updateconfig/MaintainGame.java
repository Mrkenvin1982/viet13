/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.vn.common.lib.updateconfig;

import java.util.ArrayList;
import java.util.List;

/**
 * Bảo trì hệ thống
 * @author tuanp
 */
public class MaintainGame {
    private List<Integer> serviceIds= new ArrayList<>();
    private boolean isAllGame;
    private boolean isMaintain;
    private String mess="";
    private String messEn="";

    public List<Integer> getServiceIds() {
        return serviceIds;
    }

    public void setServiceIds(List<Integer> serviceIds) {
        this.serviceIds = serviceIds;
    }

    public boolean isIsAllGame() {
        return isAllGame;
    }

    public void setIsAllGame(boolean isAllGame) {
        this.isAllGame = isAllGame;
    }

    public boolean isIsMaintain() {
        return isMaintain;
    }

    public void setIsMaintain(boolean isMaintain) {
        this.isMaintain = isMaintain;
    }

    public String getMess() {
        return mess;
    }

    public void setMess(String mess) {
        this.mess = mess;
    }

    public String getMessEn() {
        return messEn;
    }

    public void setMessEn(String messEn) {
        this.messEn = messEn;
    }

}
