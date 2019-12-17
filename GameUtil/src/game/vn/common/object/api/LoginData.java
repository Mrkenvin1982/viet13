/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.vn.common.object.api;

/**
 *
 * @author hanv
 */
public class LoginData {

    /**
     * @param connectionId the connectionId to set
     */
    public void setConnectionId(int connectionId) {
        this.connectionId = connectionId;
    }

    private String sessionId;
    private long createAt = System.currentTimeMillis() / 1000;
    private String requestId;
    private String playerId;
    private String playerName;
    private int authorizeType;
    private int serverId;
    private String serverName;
    private int connectionId = 1;
    private boolean isBot = false;
    private String channel;

    public LoginData(String playerId) {
        this.playerId = playerId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public void setCreateAt(long createAt) {
        this.createAt = createAt;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public void setPlayerId(String playerId) {
        this.playerId = playerId;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public void setAuthorizeType(int authorizeType) {
        this.authorizeType = authorizeType;
    }

    public void setServerId(int serverId) {
        this.serverId = serverId;
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    public boolean isBot() {
        return isBot;
    }

    public void setIsBot(boolean isBot) {
        this.isBot = isBot;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

}
