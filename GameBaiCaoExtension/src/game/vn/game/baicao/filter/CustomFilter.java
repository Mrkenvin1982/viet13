/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.vn.game.baicao.filter;

import com.smartfoxserver.v2.core.ISFSEvent;
import com.smartfoxserver.v2.entities.User;
import com.smartfoxserver.v2.entities.data.ISFSObject;
import com.smartfoxserver.v2.extensions.SFSExtension;
import com.smartfoxserver.v2.extensions.filter.FilterAction;
import com.smartfoxserver.v2.extensions.filter.SFSExtensionFilter;

/*
 * @author minhhnb
 */
public class CustomFilter extends SFSExtensionFilter {

    @Override
    public void init(SFSExtension ext) {
        super.init(ext);
//        trace("Filter inited!");
    }

    @Override
    public void destroy() {
//        trace("Filter destroyed!");
    }

    @Override
    public FilterAction handleClientRequest(String cmd, User sender, ISFSObject params) {
//        trace(cmd+sender+params);
        // If something goes wrong you can stop the execution chain here!
        if (cmd.equals("BadRequest")) {
            return FilterAction.HALT;
        } else {
            return FilterAction.CONTINUE;
        }
    }

    @Override
    public FilterAction handleServerEvent(ISFSEvent event) {
        return FilterAction.CONTINUE;
    }

}
