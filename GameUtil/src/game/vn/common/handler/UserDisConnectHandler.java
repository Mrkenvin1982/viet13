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
import com.smartfoxserver.v2.extensions.BaseServerEventHandler;import com.smartfoxserver.v2.util.ClientDisconnectionReason;
import game.vn.common.GameExtension;

/**
 *
 * @author tuanp
 */
public class UserDisConnectHandler extends BaseServerEventHandler{

    @Override
    public void handleServerEvent(ISFSEvent isfse){
        User user = (User) isfse.getParameter(SFSEventParam.USER);
        Room room = (Room) isfse.getParameter(SFSEventParam.ROOM);
//        trace("----------UserDisConnectHandler--------------", user.getName());
        GameExtension gameExt = (GameExtension) getParentExtension();       
        ClientDisconnectionReason reason =(ClientDisconnectionReason)isfse.getParameter(SFSEventParam.DISCONNECTION_REASON);
        user.setJoining(false);
        try {
            //trường hợp là lobby thì không xử lý leave game
            if (room != null && !room.isGame()) {
                return;
            }
            if (gameExt.gameController == null) {
                return;
            }
            
            if (reason == ClientDisconnectionReason.UNKNOWN) {
                if (gameExt.gameController!=null) {
                    //nếu ván đang không playing thì khi user disconnect thì kick ra lun
                    if (!gameExt.gameController.isPlaying()) {
                        gameExt.gameController.leave(user);
                    }else{
                        //là user waiter thì không reconnect
                        if(!gameExt.gameController.isPlayerState(user)){
                            gameExt.gameController.leave(user);
                        }
                    }
                }
                return;
            }
            /**
             * ClientDisconnectionReason.UNKNOWN= client kill app trong khoang
             * thoi gian cố dinh không reconnect lại do sever set trong "User
             * reconnect timeframe" thì sẽ nhảy vào đây sẽ cho leave game trong
             * tất cả các trường hợp
             */
            //nếu không phải reconnect thì remove hazelcast chổ này
            gameExt.gameController.leave(user);
        } catch (Exception e) {
            gameExt.getLogger().error("UserDisConnectHandler error: ", e);
        }
    }
}
