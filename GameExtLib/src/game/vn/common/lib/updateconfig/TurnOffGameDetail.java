/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.vn.common.lib.updateconfig;

/**
 * Luu thong tin off game
 * @author tuanp
 */
public class TurnOffGameDetail {
    

    private int serviceId;
    private int moneyType;
    private String platform;
    private String version;
    private String bundle;

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getBundle() {
        return bundle;
    }

    public void setBundle(String bundle) {
        this.bundle = bundle;
    }

    public int getServiceId() {
        return serviceId;
    }

    public void setServiceId(int serviceId) {
        this.serviceId = serviceId;
    }

    public int getMoneyType() {
        return moneyType;
    }

    public void setMoneyType(int moneyType) {
        this.moneyType = moneyType;
    }
 
}
