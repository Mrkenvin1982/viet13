/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.vn.login.handler;

import com.smartfoxserver.v2.core.ISFSEvent;
import com.smartfoxserver.v2.core.SFSEventParam;
import com.smartfoxserver.v2.entities.User;
import com.smartfoxserver.v2.exceptions.SFSException;
import com.smartfoxserver.v2.extensions.BaseServerEventHandler;
import game.vn.common.config.QueueConfig;
import game.vn.common.lib.hazelcast.PlayingBoardManager;
import game.vn.common.lib.hazelcast.UserState;
import game.vn.common.object.boardhistory.LogOutInfor;
import game.vn.common.properties.UserInforPropertiesKey;
import game.vn.common.queue.QueueServiceApi;
import game.vn.util.HazelcastUtil;
import game.vn.util.Utils;

/**
 *
 * @author binhnt
 */
public class LogoutHandler extends BaseServerEventHandler {

    @Override
    public void handleServerEvent(ISFSEvent isfse) throws SFSException {
        User user = (User) isfse.getParameter(SFSEventParam.USER);
        this.getLogger().debug("LogoutHandler username: " + user.getName());
        
        // Remove after logout process
        String userId = String.valueOf(user.getSession().getProperty(UserInforPropertiesKey.ID_DB_USER));
        HazelcastUtil.removeUserInfo(userId);
        HazelcastUtil.removeUserState(userId);
        UserState userState = HazelcastUtil.getUserState(userId);
        if (userState != null && userState.getLoginToken()!= null) {
            HazelcastUtil.removeUserLoginToken(userState.getLoginToken());
        } 
        
        PlayingBoardManager playingBoard= HazelcastUtil.getPlayingBoard(userId);
        if(playingBoard!=null){
            HazelcastUtil.removePlayingBoard(userId);
        }
        
        if(user!=null){
            logOutInfor(user);
        }
    }
    
    private void logOutInfor(User user) {
        try {
            String sessionId = String.valueOf(user.getProperty(UserInforPropertiesKey.SESSION_ID));
            LogOutInfor logOutInfor = new LogOutInfor();
            logOutInfor.setSessionId(sessionId);
            logOutInfor.setCreatedAt(System.currentTimeMillis() / 1000);
            logOutInfor.setRequestId(Utils.md5String(String.valueOf(System.currentTimeMillis())));
            QueueServiceApi.getInstance().sendData(QueueConfig.getInstance().getKeyLogout(), true, logOutInfor);
        } catch (Exception e) {
            this.getLogger().error("logOutInfor username: " + user.getName(), e);
        }
    }


}
