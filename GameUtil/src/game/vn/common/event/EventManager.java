/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.vn.common.event;

import game.vn.common.lib.event.UserCardsObj;
import game.vn.common.thread.ThreadPoolGame;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author 
 */
public class EventManager {
     //Tien Len
    public static final String TL_4_DOI_THONG="TL_4_DOI_THONG";
    public static final String TL_5_DOI_THONG="TL_5_DOI_THONG";
    public static final String TL_IS_STRAIGHT_DRAGON="TL_IS_STRAIGHT_DRAGON";
    public static final String TL_4_HEO = "TL_4_HEO";
    public static final String TL_6_PAIR = "TL_6_PAIR";
    public static final String TL_CHAT_3_DOI_THONG= "TL_CHAT_3_DOI_THONG";
    public static final String TL_CHAT_4_QUY= "TL_CHAT_4_QUY";
    public static final String TL_4_XAM_CO= "TL_4_XAM_CO";
    
    //Mau Binh
    public static final String MAUBINH_4_OF_A_KIND = "MAUBINH_4_OF_A_KIND";
    public static final String MAUBINH_STRAIGHT_FLUSH = "MAUBINH_STRAIGHT_FLUSH";
    public static final String IS_MAUBINH = "IS_MAUBINH";
    public static final String MAUBINH_SAME_COLOR_13 = "MAUBINH_SAME_COLOR_13";
    public static final String MAUBINH_4_XAM = "MAUBINH_4_XAM";
    public static final String MAUBINH_6_PAIR = "MAUBINH_6_PAIR";
    public static final String MAUBINH_SIX_PAIR_WITH_THREE = "MAUBINH_SIX_PAIR_WITH_THREE";
    public static final String MAUBINH_IS_STRAIGHT_DRAGON="MAUBINH_IS_STRAIGHT_DRAGON";
    
    //Bai Cao
    public static final String BAICAO_3_TIEN = "BAICAO_3_TIEN";
    public static final String BAICAO_3_SAME_CARD = "BAICAO_3_SAME_CARD";
    
    //blackJack
    public static final String XI_DACH = "XI_DACH";
    public static final String XI_BANG = "XI_BANG";
    
    //phom
    public static final String PHOM_U = "PHOM_U";
    
    //sam
    public static final String SAM_IS_STRAIGHT_DRAGON="SAM_IS_STRAIGHT_DRAGON";
    public static final String SAM_SAME_COLOR_10 = "SAM_SAME_COLOR_10";
    public static final String SAM_5_PAIR = "SAM_5_PAIR";
    public static final String SAM_3_XAM = "SAM_3_XAM";
    public static final String SAM_4_HEO = "SAM_4_HEO";
    
    public static final String PLAYING_USER = "PLAYING_USER";
    
    // danh sách user có thể nhận item event, cuối ván game gửi qua queue
    private final List<UserCardsObj> listUserGetEvent;
    
    public EventManager() {
        listUserGetEvent = new ArrayList<>();
    }

    public List<UserCardsObj> getListUserGetEvent() {
        return listUserGetEvent;
    }

    public void clearListUserGetEvent() {
        listUserGetEvent.clear();
    }
    
    public void addUserGetEvent(UserCardsObj userCardsObj){
        listUserGetEvent.add(userCardsObj);
    }
    
    public void runEventTask(Runnable eventTask) {
        ThreadPoolGame.getPool().executeEvent(eventTask);
    }
    
    /**
     * Kiểm tra đóng và mở event
     * @return 
     */
    public boolean isEnableEvent(){
        return EventConfig.getInstance().isEnableEvent();
    }

}
