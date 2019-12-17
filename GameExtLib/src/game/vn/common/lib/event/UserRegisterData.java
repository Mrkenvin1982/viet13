/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.vn.common.lib.event;

/**
 *
 * @author hanv
 */
public class UserRegisterData {

    public UserRegisterData(String sessionId, String playerId, String playerName, String email, int isActive) {
        this.sessionId = sessionId;
        this.playerId = playerId;
        this.playerName = playerName;
        this.email = email;
        this.isActive = isActive;
    }

    public UserRegisterData() {
    }

    /**
     * @return the sessionId
     */
    public String getSessionId() {
        return sessionId;
    }

    /**
     * @param sessionId the sessionId to set
     */
    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    /**
     * @return the createAt
     */
    public long getCreateAt() {
        return createAt;
    }

    /**
     * @return the playerId
     */
    public String getPlayerId() {
        return playerId;
    }

    /**
     * @param playerId the playerId to set
     */
    public void setPlayerId(String playerId) {
        this.playerId = playerId;
    }

    /**
     * @return the playerName
     */
    public String getPlayerName() {
        return playerName;
    }

    /**
     * @param playerName the playerName to set
     */
    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    /**
     * @return the email
     */
    public String getEmail() {
        return email;
    }

    /**
     * @param email the email to set
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * @return the isActive
     */
    public int getIsActive() {
        return isActive;
    }

    /**
     * @param isActive the isActive to set
     */
    public void setIsActive(int isActive) {
        this.isActive = isActive;
    }

    private String sessionId;
    private long createAt = System.currentTimeMillis() / 1000;
    private String playerId;
    private String playerName;
    private String email;
    private int isActive;
    private String deviceId;
    

}
