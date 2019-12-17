/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.vn.game.login.handler;

import com.smartfoxserver.bitswarm.sessions.ISession;
import com.smartfoxserver.v2.core.ISFSEvent;
import com.smartfoxserver.v2.core.SFSConstants;
import com.smartfoxserver.v2.core.SFSEventParam;
import com.smartfoxserver.v2.entities.data.ISFSObject;
import com.smartfoxserver.v2.entities.data.SFSObject;
import com.smartfoxserver.v2.exceptions.SFSErrorCode;
import com.smartfoxserver.v2.exceptions.SFSErrorData;
import com.smartfoxserver.v2.exceptions.SFSException;
import com.smartfoxserver.v2.exceptions.SFSLoginException;
import com.smartfoxserver.v2.extensions.BaseServerEventHandler;
import game.key.SFSKey;
import game.vn.common.config.RoomConfig;
import game.vn.common.lib.hazelcast.UserLoginToken;
import game.vn.common.properties.UserInforPropertiesKey;
import game.vn.game.login.constant.ExtensionConstant;
import game.vn.common.lib.hazelcast.UserState;
import game.vn.util.HazelcastUtil;
import game.vn.util.db.Database;

/**
 *
 * @author minhhnb
 */
public class LoginHandler extends BaseServerEventHandler {

    @Override
    public void handleServerEvent(ISFSEvent isfse) throws SFSException {
        String name = (String) isfse.getParameter(SFSEventParam.LOGIN_NAME);
        SFSObject params = (SFSObject) isfse.getParameter(SFSEventParam.LOGIN_IN_DATA);
        ISFSObject outData = (ISFSObject) isfse.getParameter(SFSEventParam.LOGIN_OUT_DATA);
        ISession session = (ISession) isfse.getParameter(SFSEventParam.SESSION);
        this.getLogger().debug("-----------LoginHandler game--------------");

        /**
         * client gui len name = token login da gui ve khi login thanh cong ben
         * loginExt
         */
        String userId = this.checkAccount(name, session, params, outData);
         // Đảm bảo user online server chính
        if (!HazelcastUtil.isOnlineUser(userId)) {
            SFSErrorData errData = new SFSErrorData(SFSErrorCode.LOGIN_INACTIVE_ZONE);
            errData.addParameter(name);
            throw new SFSLoginException(name + " LOGIN_INACTIVE_ZONE", errData);
        }
        
        //kiểm tra game có đang bảo trì
        if(RoomConfig.getInstance().isMaintainAllGame()) {
            SFSErrorData errData = new SFSErrorData(SFSErrorCode.LOGIN_INACTIVE_ZONE);
            errData.addParameter(name);
            throw new SFSLoginException(name + " LOGIN_INACTIVE_ZONE", errData);
        }

        UserState userstat = HazelcastUtil.getUserState(userId);
        //không có user stat tức chưa qua loginExt, sai
        if (userstat == null) {
            SFSErrorData errData = new SFSErrorData(SFSErrorCode.LOGIN_INACTIVE_ZONE);
            errData.addParameter(name);
            throw new SFSLoginException(name + " LOGIN_INACTIVE_ZONE", errData);
        }
    }
    
    /**
     * Check account & pass is valid
     *
     * @param name username or FB token
     * @param encryptedPass password
     * @param session session
     * @param params input parameters
     * @param outData output
     * @return username or null
     * @throws SFSLoginException
     */
    private String checkAccount(String name, ISession session, SFSObject params, ISFSObject outData) throws SFSLoginException {
        int loginType = params.getInt(SFSKey.LOGIN_TYPE);
        if (loginType != ExtensionConstant.TOKEN_LOGIN) {
            SFSErrorData errData = new SFSErrorData(SFSErrorCode.LOGIN_GUEST_NOT_ALLOWED);
            errData.addParameter(name);
            throw new SFSLoginException(name + " login fail user type", errData);
        }
        
        String loginToken = params.getUtfString(SFSKey.LOGIN_TOKEN);

        //user gửi len name = token de login thay vi gui username nhu bt
        UserLoginToken userToken = HazelcastUtil.getUserTokenData(loginToken);
        if (userToken == null) {
            SFSErrorData errData = new SFSErrorData(SFSErrorCode.LOGIN_BAD_USERNAME);
            errData.addParameter(name);
            throw new SFSLoginException(name + " login fail token", errData);
        }

        String userId = userToken.getUserId();
        
        this.getLogger().debug("Login by token... "+ userId+","+ loginToken);

        String displayName = Database.instance.getDisplayName(userId);
        if (displayName == null || displayName.isEmpty()) {
            displayName = name;
        }
        
        outData.putUtfString(SFSConstants.NEW_LOGIN_NAME, userId);

        session.setProperty(UserInforPropertiesKey.ID_DB_USER, userId);
        session.setProperty(UserInforPropertiesKey.DISPLAY_NAME, displayName);

        this.getLogger().debug("login by token success: "+ userId+","+ loginToken);
        return userId;
    }
}
