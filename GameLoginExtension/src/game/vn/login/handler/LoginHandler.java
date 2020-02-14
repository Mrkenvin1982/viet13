/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.vn.login.handler;

import com.restfb.DefaultFacebookClient;
import com.restfb.FacebookClient;
import com.restfb.FacebookClient.DebugTokenInfo;
import com.restfb.Parameter;
import com.restfb.Version;
import com.smartfoxserver.bitswarm.sessions.ISession;
import com.smartfoxserver.v2.core.ISFSEvent;
import com.smartfoxserver.v2.core.SFSConstants;
import com.smartfoxserver.v2.core.SFSEventParam;
import com.smartfoxserver.v2.entities.User;
import com.smartfoxserver.v2.entities.data.ISFSObject;
import com.smartfoxserver.v2.entities.data.SFSObject;
import com.smartfoxserver.v2.exceptions.SFSErrorCode;
import com.smartfoxserver.v2.exceptions.SFSErrorData;
import com.smartfoxserver.v2.exceptions.SFSException;
import com.smartfoxserver.v2.exceptions.SFSLoginException;
import com.smartfoxserver.v2.extensions.BaseServerEventHandler;
import com.smartfoxserver.v2.extensions.ExtensionLogLevel;
import com.smartfoxserver.v2.util.MD5;
import game.key.SFSKey;
import game.vn.common.config.RoomConfig;
import game.vn.common.config.SFSConfig;
import game.vn.common.config.ServerConfig;
import game.vn.common.config.UrlConfig;
import game.vn.common.constant.ExtensionConstant;
import game.vn.common.lib.hazelcast.UserLoginToken;
import game.vn.common.object.ClientInfo;
import game.vn.common.object.UserInfo;
import game.vn.common.object.VerifyResponseData;
import game.vn.common.properties.UserInforPropertiesKey;
import game.vn.util.GsonUtil;
import game.vn.util.HazelcastUtil;
import game.vn.util.db.Database;
import java.util.Random;
import org.apache.commons.lang.RandomStringUtils;

/**
 *
 * @author minhhnb
 */
public class LoginHandler extends BaseServerEventHandler {

    @Override
    public void handleServerEvent(ISFSEvent isfse) throws SFSException {
        SFSObject params = (SFSObject) isfse.getParameter(SFSEventParam.LOGIN_IN_DATA);
        ISFSObject outData = (ISFSObject) isfse.getParameter(SFSEventParam.LOGIN_OUT_DATA);
        ISession session = (ISession) isfse.getParameter(SFSEventParam.SESSION);

        String token = params.getUtfString(SFSKey.LOGIN_TOKEN);
        byte loginType = params.getByte(SFSKey.LOGIN_TYPE);
        String clientInfo = params.getUtfString(SFSKey.CLIENT_INFO);
        this.getLogger().info("Client new login :" + token + " - " + clientInfo);

        ClientInfo clientInfoObj = null;

        try {
            clientInfoObj = GsonUtil.fromJson(clientInfo, ClientInfo.class);
        } catch (Exception ex) {
            this.getLogger().error("Client info parse error", ex.getMessage(), ex);
        }

        String userId = authenticate(session, token, loginType, outData, clientInfoObj);

//        User user = getApi().getUserByName(userId);
        // Đảm bảo user online server chính
//        if (user != null && HazelcastUtil.isExitDevice(userId, clientInfoObj.getUdid())) {
//            SFSErrorData errData = new SFSErrorData(SFSErrorCode.LOGIN_ALREADY_LOGGED);
//            errData.addParameter(userId);
//            throw new SFSLoginException(userId + " LOGIN_INACTIVE_ZONE", errData);
//        }

        // Đảm bảo phải login và reconnect trên cùng 1 server login
        if (HazelcastUtil.isOnlineOtherLoginServer(userId)) {
            SFSErrorData errData = new SFSErrorData(SFSErrorCode.LOGIN_ALREADY_LOGGED);
            errData.addParameter(userId);
            throw new SFSLoginException(userId + " LOGIN_INACTIVE_ZONE", errData);
        }

        if (RoomConfig.getInstance().isMaintainAllGame()) {
            SFSErrorData errData = new SFSErrorData(SFSErrorCode.LOGIN_INACTIVE_ZONE);
            errData.addParameter(userId);
            throw new SFSLoginException(userId + " LOGIN_INACTIVE_ZONE", errData);
        }

        try {
            String displayName = String.valueOf(session.getProperty(UserInforPropertiesKey.DISPLAY_NAME));
            String userLoginToken = MD5.getInstance().getHash(userId + System.currentTimeMillis());
            UserLoginToken userToken = new UserLoginToken(userLoginToken, displayName, userId);

            session.setProperty(UserInforPropertiesKey.USER_TOKEN, userToken);
            session.setProperty(UserInforPropertiesKey.CLIENT_INFOR, clientInfoObj);
            session.setProperty(UserInforPropertiesKey.PLATFORM, clientInfoObj.getPlatform());
            session.setProperty(UserInforPropertiesKey.VERSION, clientInfoObj.getApp_version());
            if (session.getProperty(UserInforPropertiesKey.CHANNEL) == null) {
                session.setProperty(UserInforPropertiesKey.CHANNEL, clientInfoObj.getChannel());
            }
            outData.putUtfString(SFSKey.TOKEN_LOGIN, userLoginToken);
        } catch (Exception ex) {
            this.getLogger().error("userLoginToken error", ex.getMessage(), ex);
        }
    }

    /**
     *
     * @param session
     * @param token
     * @param outData
     * @return
     * @throws SFSLoginException
     */
    private String authenticate(ISession session, String token, byte loginType, ISFSObject outData, ClientInfo clientInfoObj) throws SFSLoginException {
        session.setProperty(UserInforPropertiesKey.LOGIN_TYPE, loginType);

        try {
            String userId = token;
            String displayName = null;
            String avatar = "";
            String email = "";
            String socialId = "";
            switch (loginType) {
                case ExtensionConstant.LOGIN_TYPE_FB:
                    com.restfb.types.User user = loginFB(token);
                    if (user == null) {
                        SFSErrorData errData = new SFSErrorData(SFSErrorCode.LOGIN_BAD_PASSWORD);
                        errData.addParameter(token);
                        throw new SFSLoginException("LOGIN_BAD_PASSWORD", errData);
                    }
                    socialId = user.getId();
                    userId = Database.instance.getUserIdBySocialId(socialId);
                    if (userId == null) {
                        do {
                            userId = RandomStringUtils.randomNumeric(12);
                            if (!Database.instance.checkUserIdExist(userId)) {
                                avatar = String.valueOf(new Random().nextInt(6));
                                break;
                            }
                        } while (true);
                    } else {
                        avatar = Database.instance.getUserAvatar(userId);
                    }
                    displayName = user.getName();
                    if (user.getEmail() != null) {
                        email = user.getEmail();
                    }
//                    if (user.getPicture() != null) {
//                        avatar = user.getPicture().getUrl();
//                    }
                    break;
                case ExtensionConstant.LOGIN_TYPE_GG:
                    userId = loginGG(token);
                    break;
                default:
                    if (userId.isEmpty()) {
                        do {
                            userId = RandomStringUtils.randomNumeric(12);
                            if (!Database.instance.checkUserIdExist(userId)) {
                                avatar = String.valueOf(new Random().nextInt(6));
                                break;
                            }
                        } while (true);
                        displayName = "test " + userId;
                    } else {
                        if (Database.instance.checkUserIdExist(userId)) {
                            displayName = Database.instance.getDisplayName(userId);
                            avatar = Database.instance.getUserAvatar(userId);
                        } else {
                            SFSErrorData errData = new SFSErrorData(SFSErrorCode.LOGIN_BAD_PASSWORD);
                            errData.addParameter(token);
                            throw new SFSLoginException("LOGIN_BAD_PASSWORD", errData);
                        }
                    }
            }
            
            outData.putUtfString(SFSConstants.NEW_LOGIN_NAME, userId);
            session.setProperty(UserInforPropertiesKey.ID_DB_USER, userId);
            session.setProperty(UserInforPropertiesKey.DISPLAY_NAME, displayName);
            session.setProperty(UserInforPropertiesKey.AVATAR, avatar);
            session.setProperty(UserInforPropertiesKey.EMAIL, email);
            session.setProperty(UserInforPropertiesKey.SOCIAL_ID, socialId);
            session.setProperty(UserInforPropertiesKey.LOGIN_TYPE, loginType);
            session.setProperty(UserInforPropertiesKey.ACCESS_TOKEN, token);
            return userId;

        } catch (SFSLoginException ex) {
            this.getLogger().error("authenticate: ", ex);
            SFSErrorData errData = new SFSErrorData(SFSErrorCode.LOGIN_BAD_PASSWORD);
            errData.addParameter(token);
            throw new SFSLoginException("LOGIN EXCEPTON", errData);
        }
    }

    private com.restfb.types.User loginFB(String token) {
        String accessToken = ServerConfig.getInstance().getFBAppId() + "|" + ServerConfig.getInstance().getFBAppSecret();
        FacebookClient fbClient = new DefaultFacebookClient(accessToken, ServerConfig.getInstance().getFBAppSecret(), Version.LATEST);
        DebugTokenInfo debugTokenInfo = fbClient.debugToken(token);
        if (debugTokenInfo == null || !debugTokenInfo.isValid()) {
            trace(ExtensionLogLevel.INFO, "fb login fail", token);
            return null;
        }
        
        fbClient = fbClient.createClientWithAccessToken(token);
        com.restfb.types.User user = fbClient.fetchObject("me", com.restfb.types.User.class, Parameter.with("fields", "id, name, email, picture"));

        return user;
    }
    
    private String loginGG(String token) {
        return null;
    }
    
    private String createGuestAccount(ISession session, ISFSObject outData) throws SFSLoginException {
        if (!SFSConfig.getZone().isGuestUserAllowed()) {
            SFSErrorData errData = new SFSErrorData(SFSErrorCode.LOGIN_GUEST_NOT_ALLOWED);
            throw new SFSLoginException("LOGIN_GUEST_NOT_ALLOWED", errData);
        }

        try {
            String userId = RandomStringUtils.randomNumeric(20);
            String displayName = "Guest " + RandomStringUtils.randomAlphabetic(10);
            Database.instance.createGuest(userId, displayName, ServerConfig.getInstance().getMerchantId());
            outData.putUtfString(SFSConstants.NEW_LOGIN_NAME, userId);
            session.setProperty(UserInforPropertiesKey.ID_DB_USER, userId);
            session.setProperty(UserInforPropertiesKey.DISPLAY_NAME, displayName);
            session.setProperty(UserInforPropertiesKey.AVATAR, UrlConfig.getInstance().getDefaultAvatarUrl());
            session.setProperty(UserInforPropertiesKey.EMAIL, "");
            session.setProperty(UserInforPropertiesKey.MERCHANT_ID, ServerConfig.getInstance().getMerchantId());
            session.setProperty(UserInforPropertiesKey.CURRENCY, "");
            VerifyResponseData data = new VerifyResponseData();
            data.setAccountId(userId);
            data.setDisplayName(displayName);
            data.setAvatar(UrlConfig.getInstance().getDefaultAvatarUrl());
            session.setProperty(UserInforPropertiesKey.PROFILE, data);
            return userId;
        } catch (Exception e) {
            trace(ExtensionLogLevel.ERROR, e);
            SFSErrorData errData = new SFSErrorData(SFSErrorCode.LOGIN_GUEST_NOT_ALLOWED);
            throw new SFSLoginException("LOGIN GUEST FAIL", errData);
        }
    }

    private String loginBot(ClientInfo info, ISession session, ISFSObject outData) throws SFSLoginException {
        try {
            String email = info.getEmail();
            UserInfo userInfo = Database.instance.getUserInfo(email);
            String userId = userInfo.getUserId();
            outData.putUtfString(SFSConstants.NEW_LOGIN_NAME, userId);
            session.setProperty(UserInforPropertiesKey.ID_DB_USER, userId);
            session.setProperty(UserInforPropertiesKey.DISPLAY_NAME, userInfo.getDisplayName());
            session.setProperty(UserInforPropertiesKey.AVATAR, userInfo.getAvatar());
            session.setProperty(UserInforPropertiesKey.EMAIL, email);
            session.setProperty(UserInforPropertiesKey.MERCHANT_ID, userInfo.getMerchantId());
            session.setProperty(UserInforPropertiesKey.CURRENCY, userInfo.getCurrency());
            session.setProperty(UserInforPropertiesKey.CHANNEL, info.getChannel());
            return userId;
        } catch (Exception e) {
            trace(ExtensionLogLevel.ERROR, "error login bot", e);
            SFSErrorData errData = new SFSErrorData(SFSErrorCode.LOGIN_INACTIVE_ZONE);
            throw new SFSLoginException("LOGIN BOT FAIL", errData);
        }
    }
}
