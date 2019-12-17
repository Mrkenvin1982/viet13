/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.vn.login.handler;

import com.smartfoxserver.v2.core.ISFSEvent;
import com.smartfoxserver.v2.core.SFSEventParam;
import com.smartfoxserver.v2.entities.Room;
import com.smartfoxserver.v2.extensions.BaseServerEventHandler;
import game.vn.common.lib.hazelcast.Board;
import game.vn.util.HazelcastUtil;

/**
 *
 * @author tuanp
 */
public class RemoveRoomHandler extends BaseServerEventHandler {

    @Override
    public void handleServerEvent(ISFSEvent isfse) {
        try {
            Room room = (Room) isfse.getParameter(SFSEventParam.ROOM);
            Board board = HazelcastUtil.getBoardInfor(room.getName());
            //remove room khoi hazelcast
            HazelcastUtil.removeBoardInfor(room.getName());
            HazelcastUtil.removeBoardWaitingInfor(board);
        } catch (Exception e) {
            this.getLogger().error("RemoveRoomHandler error: ", e);
        }
    }
    
}
