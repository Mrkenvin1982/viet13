/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.vn.common.handler.request;

import com.smartfoxserver.v2.entities.User;
import com.smartfoxserver.v2.entities.data.ISFSObject;
import com.smartfoxserver.v2.extensions.BaseClientRequestHandler;
import game.key.SFSKey;
import game.vn.common.GameExtension;
import game.vn.util.Utils;

/**
 * Xử ly tất cả các request client gửi lên để
 * xử lý trong game
 * @author tuanp
 */
public class ClientGameRequest extends BaseClientRequestHandler {

    @Override
    public void handleClientRequest(User user, ISFSObject isfso) {
        GameExtension gameExt = (GameExtension) getParentExtension();
        if (gameExt.getParentRoom().isGame()) {
            //put vào để trong game controller có thể dựa vao message để get ra sender
            String userId = Utils.getIdDBOfUser(user);
//            trace(userId, "send ingame message", isfso.getDump());
            isfso.putUtfString(SFSKey.ID_DB_USER, userId);
            gameExt.gameController.addMessage(isfso);
        }
    }
}
