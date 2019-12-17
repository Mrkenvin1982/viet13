/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.vn.common.filter;

import com.smartfoxserver.v2.controllers.filter.SysControllerFilter;
import com.smartfoxserver.v2.entities.Room;
import com.smartfoxserver.v2.entities.User;
import com.smartfoxserver.v2.entities.data.ISFSObject;
import com.smartfoxserver.v2.exceptions.SFSException;
import com.smartfoxserver.v2.extensions.filter.FilterAction;
import game.vn.common.GameExtension;
import game.vn.common.lang.GameLanguage;
import game.vn.util.Utils;

/**
 *
 * @author hanv
 */
public class LeaveRoomFilter extends SysControllerFilter {

    @Override
    public FilterAction handleClientRequest(User user, ISFSObject isfso) throws SFSException {
        if (isfso.containsKey("r")) {
            int roomId = isfso.getInt("r");
            Room room = user.getZone().getRoomById(roomId);
            if (room != null && room.isGame()) {
                GameExtension gameExt = (GameExtension) room.getExtension();
                if (!gameExt.gameController.isCanLeave(user)) {
                    String message = GameLanguage.getMessage(GameLanguage.CANT_LEAVE_GAME_NOW, Utils.getUserLocale(user));
                    gameExt.gameController.sendToastMessage(message, user, 3);
                    return FilterAction.HALT;
                }
            }
        }

        return FilterAction.CONTINUE;
    }

}