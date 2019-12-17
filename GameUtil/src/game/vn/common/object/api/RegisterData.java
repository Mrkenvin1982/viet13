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
public class RegisterData {
    private String sessionId;
    private long createAt = System.currentTimeMillis() / 1000;
    private String requestId;
    private String playerId;
    private String playerName;
    private String email;
    private int isActive;
    private int connectionId;
    private String channel;

    public RegisterData(String playerId) {
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

    public void setEmail(String email) {
        this.email = email;
    }

    public void setIsActive(int isActive) {
        this.isActive = isActive;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public int getConnectionId() {
        return connectionId;
    }

    public void setConnectionId(int connectionId) {
        this.connectionId = connectionId;
    }
}
