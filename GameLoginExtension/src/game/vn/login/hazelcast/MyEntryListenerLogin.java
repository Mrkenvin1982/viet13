/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.vn.login.hazelcast;

import com.hazelcast.core.EntryEvent;
import com.hazelcast.map.listener.EntryUpdatedListener;
import com.smartfoxserver.v2.api.ISFSApi;
import com.smartfoxserver.v2.entities.User;
import com.smartfoxserver.v2.entities.data.SFSObject;
import game.command.SFSCommand;
import game.vn.common.constant.MoneyContants;
import game.vn.common.lib.hazelcast.UserState;
import game.vn.common.message.MessageController;
import game.vn.util.HazelcastUtil;
import game.vn.util.Utils;
import game.vn.util.db.Database;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author tuanp
 */
public class MyEntryListenerLogin implements EntryUpdatedListener<String, String> {

    private final static Logger LOGGER = LoggerFactory.getLogger(MyEntryListenerLogin.class);
    private final ISFSApi api;
    
    public MyEntryListenerLogin(ISFSApi api){
        this.api = api;
    }

    @Override
    public void entryUpdated(EntryEvent<String, String> event) {
        switch (event.getName()) {
            case HazelcastUtil.USER_STATE_LIST:
                updateUserStateList(event);
                break;
        }
    }

    /**
     * update USER_STATE_LIST cua user trong hazelcast
     *
     * @param event
     */
    private void updateUserStateList(EntryEvent<String, String> event) {
        try { 
            UserState userState = HazelcastUtil.getUserState(event.getKey());
            if(userState == null){
                return;
            }
            if (!userState.isIsLogoutGame() && !userState.isIsUpdateMoneySum() ) {
                return;
            }
            
            User user =this.api.getUserByName(userState.getUserId());
            if (user == null) {
                if (!HazelcastUtil.isOnlineUser(userState.getUserId()) && userState.isIsLogoutGame()) {
                    //không còn online -> remove lun khỏi cache
                    HazelcastUtil.removeUserState(userState.getUserId());
                }
                return;
            }

            //trường hợp user thoát server login
            if (userState.getMoneyType() == MoneyContants.MONEY) {
                Utils.updateMoneyOfUser(user, Database.instance.getUserMoney(userState.getUserId()));
            } else {
                Utils.updatePointOfUser(user, Database.instance.getUserPoint(userState.getUserId()));
            }
            //phải gửi cmd về để client update lại tiền tổng khi thoát khỏi bàn
            SFSObject ob = MessageController.getUpdateMoneyAndPointMessage((long) Utils.getPointOfUser(user), Utils.getMoneyOfUser(user));
            user.getZone().getExtension().send(SFSCommand.CLIENT_REQUEST, ob, user);
            
            userState.setIsLogoutGame(false);
            userState.setIsUpdateMoneySum(false);
            userState.setMoneyStack(0);
            userState.setPointStack(0);
            userState.setBetBoard(0);
            HazelcastUtil.updateUserState(userState);
        } catch (Exception e) {
            LOGGER.error("updateUserStateList() error: ", e);
        }
    }

}

