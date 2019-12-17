/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.vn.common.object;

/**
 *
 * @author anlh
 */
public class W88VerifyAccessTokenRequest {

    /**
     * @return the secretKey
     */
    public String getSecretKey() {
        return secretKey;
    }

    /**
     * @param secretKey the secretKey to set
     */
    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }
    
    private String accessToken;
    private String secretKey;

    public W88VerifyAccessTokenRequest(String accessToken) {
        this.accessToken = accessToken;
    }    
    

    /**
     * @return the accessToken
     */
    public String getAccessToken() {
        return accessToken;
    }

    /**
     * @param accessToken the accessToken to set
     */
    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }
    
}
