/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.vn.common.service;

import com.smartfoxserver.v2.SmartFoxServer;
import com.smartfoxserver.v2.entities.User;
import com.smartfoxserver.v2.entities.data.ISFSObject;
import game.vn.common.config.SFSConfig;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author hanv
 */
public class BroadcastService {

    private final static Logger LOGGER = LoggerFactory.getLogger(BroadcastService.class);
    
    public static void send(String command, ISFSObject param, User u) {
        SmartFoxServer.getInstance().getAPIManager().getSFSApi().sendExtensionResponse(command, param, u, null, false);
    }

    public static void send(String command, ISFSObject param, List<User> users) {
        SmartFoxServer.getInstance().getAPIManager().getSFSApi().sendExtensionResponse(command, param, users, null, false);
    }
    
    public static void broadcast(String command, ISFSObject param) {
        try {
            List<User> users = (List<User>) SFSConfig.getZone().getUserList();
            if (!users.isEmpty()) {
                SmartFoxServer.getInstance().getAPIManager().getSFSApi().sendExtensionResponse(command, param, users, null, false);
            }
        } catch (Exception e) {
            LOGGER.error("broadcast", e);
        }
    }

    public static void broadcast(String command, ISFSObject param, List<String> except) {
        try {
            List<User> users = (List<User>) SFSConfig.getZone().getUserList();
            if (!users.isEmpty()) {
                for (User user : users) {
                    if (!except.contains(user.getName())) {
                        SmartFoxServer.getInstance().getAPIManager().getSFSApi().sendExtensionResponse(command, param, user, null, false);
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.error("broadcast", e);
        }
    }

}
