/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.vn.common.filter;

import com.smartfoxserver.v2.SmartFoxServer;
import com.smartfoxserver.v2.controllers.filter.SysControllerFilter;
import com.smartfoxserver.v2.entities.User;
import com.smartfoxserver.v2.entities.data.ISFSObject;
import com.smartfoxserver.v2.exceptions.SFSException;
import com.smartfoxserver.v2.extensions.filter.FilterAction;
import game.command.SFSCommand;
import game.vn.common.config.ServerConfig;
import game.vn.common.lang.GameLanguage;
import game.vn.common.message.MessageController;
import game.vn.common.properties.UserInforPropertiesKey;
import game.vn.util.Utils;

/**
 *
 * @author hanv
 */
public class ChatInGameFilter extends SysControllerFilter {

    @Override
    public FilterAction handleClientRequest(User user, ISFSObject isfso) throws SFSException {
        long now = System.currentTimeMillis();
        try {
            long lastTimeChat = (long) user.getProperty(UserInforPropertiesKey.LAST_TIME_CHAT_INGAME);
            int interval = ServerConfig.getInstance().getChatInterval();
            if (now - lastTimeChat < interval) {
                long waitTime = (interval + lastTimeChat - now) / 1000;
                String msg = String.format(GameLanguage.getMessage(GameLanguage.CHAT_INTERVAL, Utils.getUserLocale(user)), waitTime);
                isfso = MessageController.getToastMessage(msg, 3);
                SmartFoxServer.getInstance().getAPIManager().getSFSApi().sendExtensionResponse(SFSCommand.CLIENT_REQUEST, isfso, user, null, false);
                return FilterAction.HALT;
            }
        } catch (Exception e) {
            trace("ChatInGameFilter", e);
        }
        user.setProperty(UserInforPropertiesKey.LAST_TIME_CHAT_INGAME, now);
        return FilterAction.CONTINUE;
    }
    
}
