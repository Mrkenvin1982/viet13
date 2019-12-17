package game.vn.game.baicao.handler;

import com.smartfoxserver.v2.core.ISFSEvent;
import com.smartfoxserver.v2.core.SFSEventParam;
import com.smartfoxserver.v2.entities.Room;
import com.smartfoxserver.v2.entities.User;
import com.smartfoxserver.v2.exceptions.SFSException;
import com.smartfoxserver.v2.extensions.BaseServerEventHandler;

/**
 *
 * @author minhhnb
 */
public class PublicMessagehandler extends BaseServerEventHandler{

    @Override
    public void handleServerEvent(ISFSEvent isfse) throws SFSException {
        
//        trace("PublicMessageandler server event type "+isfse.getType());
        User user = (User) isfse.getParameter(SFSEventParam.USER);
        Room room = (Room) isfse.getParameter(SFSEventParam.ROOM);
    }
    
}
