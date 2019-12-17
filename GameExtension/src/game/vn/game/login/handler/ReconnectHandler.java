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
import game.vn.common.properties.UserInforPropertiesKey;

/**
 *
 * @author binhnt
 */
public class ReconnectHandler extends BaseServerEventHandler {

    @Override
    public void handleServerEvent(ISFSEvent isfse) throws SFSException {
        User user = (User) isfse.getParameter(SFSEventParam.USER);
        this.getLogger().debug("ReconnectHandler username: " + user.getName());
        String userId = (String)user.getSession().getProperty(UserInforPropertiesKey.ID_DB_USER);

    }
    
}
