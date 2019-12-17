/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.vn.game.baicao.handler;

import com.smartfoxserver.v2.core.ISFSEvent;
import com.smartfoxserver.v2.core.SFSEventParam;
import com.smartfoxserver.v2.entities.Room;
import com.smartfoxserver.v2.entities.User;
import com.smartfoxserver.v2.exceptions.SFSException;
import com.smartfoxserver.v2.extensions.BaseServerEventHandler;
import game.vn.game.baicao.BaiCaoGame;

/**
 *
 * @author tuanp
 */
public class ReconnectHandler extends BaseServerEventHandler{

    @Override
    public void handleServerEvent(ISFSEvent isfse) throws SFSException {
        trace(" BAI CAO RECONNECT server event type " + isfse.getType());
         BaiCaoGame getExt = (BaiCaoGame) getParentExtension();   
        
        Room room = (Room) getExt.getParentRoom();
        User user = (User) isfse.getParameter(SFSEventParam.USER);
    }
    
}
