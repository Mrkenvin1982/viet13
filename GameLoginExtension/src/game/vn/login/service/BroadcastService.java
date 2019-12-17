/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.vn.login.service;

import com.smartfoxserver.v2.SmartFoxServer;
import com.smartfoxserver.v2.entities.User;
import com.smartfoxserver.v2.entities.data.ISFSObject;
import game.vn.common.config.SFSConfig;
import java.util.List;

/**
 *
 * @author hanv
 */
public class BroadcastService {

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
        }
    }

}