/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.vn.common.handler;

import com.smartfoxserver.v2.core.ISFSEvent;
import com.smartfoxserver.v2.core.SFSEventParam;
import com.smartfoxserver.v2.entities.Room;
import com.smartfoxserver.v2.entities.User;
import com.smartfoxserver.v2.extensions.BaseServerEventHandler;
import game.vn.common.GameExtension;
import game.vn.common.lib.hazelcast.UserState;
import game.vn.util.HazelcastUtil;
import game.vn.util.Utils;

/**
 * Xử lý leave room
 *
 * @author tuanp
 */
public class LeaveRoomHandler extends BaseServerEventHandler {

    @Override
    public void handleServerEvent(ISFSEvent isfse) {
        User user = (User) isfse.getParameter(SFSEventParam.USER);
        Room room = (Room) isfse.getParameter(SFSEventParam.ROOM);
        GameExtension getExt = (GameExtension) getParentExtension();
        //get user state từ hazelcast ve de update                        
        String userId = Utils.getIdDBOfUser(user);
        user.setJoining(false);
        try {
            if (room.isGame()) {
                //rời khỏi room game
                if (getExt.gameController != null) {
                    getExt.gameController.leave(user);
                }
            } else {
                //rời khỏi room lobby
                UserState userState = HazelcastUtil.getUserState(userId);
                userState.setCurrentLobbyName("");
                HazelcastUtil.updateUserState(userState);
            }
        } catch (Exception e) {
            getExt.getLogger().error("SFSUtil LEAVE ROOM  error: ", e);
        }
    }
}
