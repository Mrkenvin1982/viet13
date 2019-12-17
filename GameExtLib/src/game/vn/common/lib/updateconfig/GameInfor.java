/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.vn.common.lib.updateconfig;

/**
 *
 * @author tuanp
 */
public class GameInfor {
    private int serviceId;
    //loai tiền: point, money
    private byte moneyType;
    private String name;
    private boolean isActive;

    public int getServiceId() {
        return serviceId;
    }

    public void setServiceId(int serviceId) {
        this.serviceId = serviceId;
    }

    public byte getMoneyType() {
        return moneyType;
    }

    public void setMoneyType(byte moneyType) {
        this.moneyType = moneyType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isIsActive() {
        return isActive;
    }

    public void setIsActive(boolean isActive) {
        this.isActive = isActive;
    }
    
}
