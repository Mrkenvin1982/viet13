/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.vn.login.http;

import com.smartfoxserver.v2.entities.data.SFSObject;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;
import game.command.SFSAction;
import game.command.SFSCommand;
import game.key.SFSKey;
import game.vn.login.service.BroadcastService;
import java.util.Deque;
import java.util.Map;

/**
 *
 * @author hanv
 */
public class HttpServerHandler implements HttpHandler {

    private static final String SERVER_MAINTAIN = "SERVER_MAINTAIN";

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        try {
            Map<String, Deque<String>> params = exchange.getQueryParameters();
            exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/plain; charset=utf-8");
            String controller = exchange.getRequestPath().replace("/", "").toLowerCase().trim();
            String res = "";
            if (controller.equalsIgnoreCase("admin")) {
                res = process(params);
            }
            exchange.getResponseSender().send(res);
        } catch (Exception e) {
        }
    }
    
    private String process(Map<String, Deque<String>> params) {
        if (params == null) {
            return "";
        }
        
        Deque<String> action = params.get("action");
        String request = action.poll();
        if (request == null) {
            return "";
        }

        switch (request) {
            case SERVER_MAINTAIN:
                String msg = "Server sẽ bảo trì trong 5 phút nữa. Bạn vui lòng đăng nhập lại";
                SFSObject obj = new SFSObject();
                obj.putInt(SFSKey.ACTION_INCORE, SFSAction.SYSTEM_MESSAGE);
                obj.putUtfString(SFSKey.MESSAGE, msg);
                BroadcastService.broadcast(SFSCommand.CLIENT_REQUEST, obj);
                break;
        }

        return "";
    }

}