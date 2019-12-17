/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.vn.game.login.handler;

import com.smartfoxserver.v2.core.ISFSEvent;
import com.smartfoxserver.v2.core.SFSEventParam;
import com.smartfoxserver.v2.entities.User;
import com.smartfoxserver.v2.extensions.BaseServerEventHandler;
import game.vn.common.config.ServerConfig;
import game.vn.common.lib.hazelcast.UserState;
import game.vn.common.properties.UserInforPropertiesKey;
import game.vn.util.HazelcastUtil;

/**
 *
 * @author binhnt
 */
public class LogoutHandler extends BaseServerEventHandler {

    @Override
    public void handleServerEvent(ISFSEvent isfse) {
        try {
            User user = (User) isfse.getParameter(SFSEventParam.USER);
            this.getLogger().debug("LogoutHandler username: " + user.getName());
            // Remove after logout process
            String userId = (String) user.getSession().getProperty(UserInforPropertiesKey.ID_DB_USER);
            HazelcastUtil.lockUserState(userId);
            UserState userstat = HazelcastUtil.getUserState(userId);
            //không có user stat tức chưa qua loginExt, sai
            if (userstat != null) {
                userstat.removeListServerId(ServerConfig.getInstance().getServerId());
                HazelcastUtil.updateUserState(userstat);   
            }
            HazelcastUtil.unlockUserState(userId);
        } catch (Exception e) {
            this.getLogger().error("LogoutHandler.handleServerEvent()", e);
        }
    }

}
