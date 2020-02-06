/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.vn.common.lib.iap;

/**
 *
 * @author hanv
 */
public class IosReceiptVerifyResponse {
    private int status;
    private String environment;
    private IosReceipt receipt;

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getEnvironment() {
        return environment;
    }

    public void setEnvironment(String environment) {
        this.environment = environment;
    }

    public boolean isValid() {
        return this.status == 0;
    }

    public IosReceipt getReceipt() {
        return receipt;
    }

    public void setReceipt(IosReceipt receipt) {
        this.receipt = receipt;
    }
}
