/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.vn.common.lib.vip;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author anlh
 */
public class ListUserSendTax {
    
    private List<UserTaxData> listUserTaxData;

    public ListUserSendTax() {
        
        listUserTaxData = new ArrayList<>();
        
    }    
    
    public void addTax(UserTaxData data) {
        
        listUserTaxData.add(data);        
        
    }    
    public void clearData() {
        
        listUserTaxData.clear();
        
    }    
    

    /**
     * @return the listUserTaxData
     */
    public List<UserTaxData> getListUserTaxData() {
        return listUserTaxData;
    }

    /**
     * @param listUserTaxData the listUserTaxData to set
     */
    public void setListUserTaxData(List<UserTaxData> listUserTaxData) {
        this.listUserTaxData = listUserTaxData;
    }
    
}
