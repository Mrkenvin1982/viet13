/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.vn.game.login.handler;

import com.smartfoxserver.v2.core.ISFSEvent;
import com.smartfoxserver.v2.core.SFSEventParam;
import com.smartfoxserver.v2.entities.User;
import com.smartfoxserver.v2.exceptions.SFSException;
import com.smartfoxserver.v2.extensions.BaseServerEventHandler;
import com.smartfoxserver.v2.util.ClientDisconnectionReason;
import game.vn.common.config.ServerConfig;
import game.vn.common.lib.hazelcast.UserState;
import game.vn.common.properties.UserInforPropertiesKey;
import game.vn.util.HazelcastUtil;

/**
 *
 * @author tuanp
 */
public class DisConnectHandler extends BaseServerEventHandler {

    @Override
    public void handleServerEvent(ISFSEvent isfse) throws SFSException {
       try {
           User user = (User) isfse.getParameter(SFSEventParam.USER);
          
           ClientDisconnectionReason reason =(ClientDisconnectionReason)isfse.getParameter(SFSEventParam.DISCONNECTION_REASON);
            // Remove after logout process
            String userId = (String) user.getSession().getProperty(UserInforPropertiesKey.ID_DB_USER);
            user.setJoining(false);
            this.getLogger().debug("---------- UserDisConnectHandler --------------"+ userId);
            if(reason == ClientDisconnectionReason.UNKNOWN){
                return;
            }
            
           HazelcastUtil.lockUserState(userId);
           UserState userstat = HazelcastUtil.getUserState(userId);
           userstat.removeListServerId(ServerConfig.getInstance().getServerId());
           HazelcastUtil.updateUserState(userstat);
           HazelcastUtil.unlockUserState(userId);
        } catch (Exception e) {
            this.getLogger().error("DisConnectHandler.handleServerEvent()", e);
        }
    }
    
}
