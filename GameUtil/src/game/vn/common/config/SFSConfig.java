/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.vn.common.config;

import com.smartfoxserver.v2.SmartFoxServer;
import com.smartfoxserver.v2.config.ServerSettings;
import com.smartfoxserver.v2.entities.Zone;
import java.util.List;

/**
 *
 * @author hanv
 */
public class SFSConfig {
    private static Zone zone;
    private static int port;

    public static void init(Zone zone) {
        SFSConfig.zone = zone;
        List<ServerSettings.SocketAddress> adds = SmartFoxServer.getInstance().getConfigurator().getServerSettings().socketAddresses;
        for (ServerSettings.SocketAddress add : adds) {
            if (add.type.equals(ServerSettings.SocketAddress.TYPE_TCP)) {
                SFSConfig.port = add.port;
            }
        }
    }

    public static Zone getZone() {
        return zone;
    }

    public static String getZoneName() {
        return zone.getName();
    }
    
    public static int getPort() {
        return port;
    }

    public static int getWsPort() {
        return SmartFoxServer.getInstance().getConfigurator().getServerSettings().webServer.gHttpPort;
    }

    public static String getServerName() {
        return SmartFoxServer.getInstance().getConfigurator().getServerSettings().serverName;
    }
}
