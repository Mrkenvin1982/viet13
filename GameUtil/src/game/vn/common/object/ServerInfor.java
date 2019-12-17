/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.vn.common.object;

import java.io.Serializable;

/**
 *
 * @author binhnt
 */
public class ServerInfor implements Serializable {
    private final String name;
    private final String ip;
    private final String ipWS;
    private final String zone;
    private final int port;
    private int portWS;
    private byte type;

    public ServerInfor(String name, String ip, String ipWS, int port, int portWS, String zone) {
        this.name = name;
        this.ip = ip;
        this.ipWS = ipWS;
        this.port = port;
        this.portWS = portWS;
        this.zone = zone;
    }

    public String getName() {
        return name;
    }

    public String getIp() {
        return ip;
    }

    public String getIpWS() {
        return ipWS;
    }

    public int getPort() {
        return port;
    }

    public byte getType() {
        return type;
    }

    public void setType(byte type) {
        this.type = type;
    }

    public int getPortWS() {
        return portWS;
    }

    public void setPortWS(int portWS) {
        this.portWS = portWS;
    }

    public String getZone() {
        return zone;
    }

}
