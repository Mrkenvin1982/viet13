/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.vn.common.lib.hazelcast;

import java.io.Serializable;
import java.util.Date;

/**
 *
 * @author hanv
 */
public class UserLoginToken implements Serializable {
    private String token;
    private String username;
    private String userId;
    private Date loginTime;
    private Date expiredTime; 
    public static final int EXPIRED_TOKEN_TIME = 5 * 60 * 1000;

    public UserLoginToken() {
    }

    public UserLoginToken(String token, String username, String userId) {
        this.token = token;
        this.username = username;
        this.loginTime = new Date();
        this.expiredTime = new Date(loginTime.getTime() + EXPIRED_TOKEN_TIME);
        this.userId = userId;
    }    
    

    /**
     * @return the token
     */
    public String getToken() {
        return token;
    }

    /**
     * @param token the token to set
     */
    public void setToken(String token) {
        this.token = token;
    }

    /**
     * @return the username
     */
    public String getUsername() {
        return username;
    }

    /**
     * @param username the username to set
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * @return the loginTime
     */
    public Date getLoginTime() {
        return loginTime;
    }

    /**
     * @param loginTime the loginTime to set
     */
    public void setLoginTime(Date loginTime) {
        this.loginTime = loginTime;
    }

    /**
     * @return the expiredTime
     */
    public Date getExpiredTime() {
        return expiredTime;
    }

    /**
     * @param expiredTime the expiredTime to set
     */
    public void setExpiredTime(Date expiredTime) {
        this.expiredTime = expiredTime;
    }

    /**
     * @return the userId
     */
    public String getUserId() {
        return userId;
    }

    /**
     * @param userId the userId to set
     */
    public void setUserId(String userId) {
        this.userId = userId;
    }
}
