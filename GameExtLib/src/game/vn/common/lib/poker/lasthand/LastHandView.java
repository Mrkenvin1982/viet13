/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.vn.common.lib.poker.lasthand;

import java.util.ArrayList;
import java.util.List;

/**
 * Hiển thị chi tiết các sự kiện diễn ra trong bàn của 1 ván trước đó==> dạng hình ảnh.
 * @author 
 */
public class LastHandView {
    
    private List<Double> pots = new ArrayList<>();
    private List<UserInforView> users = new ArrayList<>();
    private List<Short> cardBoards = new ArrayList<>();

    public List<UserInforView> getUsers() {
        return users;
    }

    public void addUser(UserInforView userInfor){
        this.users.add(userInfor);
    }

    public List<Short> getCardBoards() {
        return cardBoards;
    }

    public void setCardBoards(List<Short> cardBoards) {
        this.cardBoards = cardBoards;
    }

    public List<Double> getPots() {
        return pots;
    }

    public void setPots(List<Double> pots) {
        this.pots = pots;
    }

    public void reset(){
        this.pots.clear();
        this.cardBoards.clear();
        this.users.clear();
    }

}
