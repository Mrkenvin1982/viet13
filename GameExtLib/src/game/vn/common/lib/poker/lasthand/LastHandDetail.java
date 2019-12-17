/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.vn.common.lib.poker.lasthand;

import java.util.ArrayList;
import java.util.List;

/**
 * Hiển thị 1 ván bài của ván chơi trước đó theo dạng chữ.
 * @author 
 */
public class LastHandDetail {
    
    List<UserInforDetail> userInforDetail = new ArrayList<>();
    
    public void reset(){
        userInforDetail.clear();
    }

    public List<UserInforDetail> getUserInforDetail() {
        return userInforDetail;
    }

    public void setUserInforDetail(List<UserInforDetail> userInforDetail) {
        this.userInforDetail = new ArrayList<>(userInforDetail);
    }
    
    public void addUserInforDetail(UserInforDetail userInforDetail){
        this.userInforDetail.add(userInforDetail);
    }
}
