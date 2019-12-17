/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.vn.login.handler;

import com.smartfoxserver.bitswarm.sessions.ISession;
import com.smartfoxserver.v2.core.ISFSEvent;
import com.smartfoxserver.v2.core.SFSConstants;
import com.smartfoxserver.v2.core.SFSEventParam;
import com.smartfoxserver.v2.entities.User;
import com.smartfoxserver.v2.entities.data.ISFSObject;
import com.smartfoxserver.v2.entities.data.SFSObject;
import com.smartfoxserver.v2.exceptions.IErrorCode;
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
import game.vn.common.constant.MerchantConstant;
import game.vn.common.lib.hazelcast.UserLoginToken;
import game.vn.common.object.ClientInfo;
import game.vn.common.object.UserInfo;
import game.vn.common.object.VerifyAccessTokenRequest;
import game.vn.common.object.VerifyAccessTokenResponse;
import game.vn.common.object.VerifyResponseData;
import game.vn.common.object.W88VerifyAccessTokenRequest;
import game.vn.common.object.W88VerifyAccessTokenResponse;
import game.vn.common.properties.UserInforPropertiesKey;
import game.vn.common.queue.QueueServiceEvent;
import game.vn.util.APIUtils;
import game.vn.util.GsonUtil;
import game.vn.util.HazelcastUtil;
import game.vn.util.db.Database;
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

        String token = params.containsKey(SFSKey.LOGIN_TOKEN) ? params.getUtfString(SFSKey.LOGIN_TOKEN) : null;
        String merchantId = params.containsKey(SFSKey.MERCHANT_ID) ? params.getUtfString(SFSKey.MERCHANT_ID) : null;
        String clientInfo = params.getUtfString("client_info");
        this.getLogger().info("Client new login :" + token + " - " + merchantId);

        ClientInfo clientInfoObj = null;

        try {
            clientInfoObj = GsonUtil.fromJson(clientInfo, ClientInfo.class);
        } catch (Exception ex) {
            this.getLogger().error("Client info parse error", ex.getMessage(), ex);
        }

        String userId = authenticate(session, token, merchantId, outData, clientInfoObj);

        User user = getApi().getUserByName(userId);
        // Đảm bảo user online server chính
        if (user != null && HazelcastUtil.isExitDevice(userId, clientInfoObj.getUdid())) {
            SFSErrorData errData = new SFSErrorData(SFSErrorCode.LOGIN_ALREADY_LOGGED);
            errData.addParameter(userId);
            throw new SFSLoginException(userId + " LOGIN_INACTIVE_ZONE", errData);
        }

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
     * @param accessToken
     * @param outData
     * @return
     * @throws SFSLoginException
     */
    private String authenticate(ISession session, String accessToken, String merchantId, ISFSObject outData, ClientInfo clientInfoObj) throws SFSLoginException {
        if (accessToken == null) {
            int userType = Database.instance.getUserTypeByEmail(clientInfoObj.getEmail());
            if (userType > 0) {
                return loginBot(clientInfoObj, session, outData);
            } else {
                return createGuestAccount(session, outData);
            }
        }

        IErrorCode errorCode = SFSErrorCode.LOGIN_BAD_PASSWORD;

        try {
            if (merchantId != null && !merchantId.isEmpty()) {
                merchantId = merchantId.toLowerCase();

                String key = ServerConfig.getInstance().getMerchantAuthenticateKey(merchantId);
                String url = UrlConfig.getInstance().getMerchantVerifyAccessTokenUrl(merchantId);

                W88VerifyAccessTokenRequest verifyAccessTokenRequest = new W88VerifyAccessTokenRequest(accessToken);
                if (key != null && !key.isEmpty()) {
                    verifyAccessTokenRequest.setSecretKey(key);
                }

                String jsonString = GsonUtil.toJson(verifyAccessTokenRequest);

                String responseStr = APIUtils.request(url, jsonString);
                this.getLogger().info("Request : " + url + " - " + jsonString + " - response :" + responseStr);
                W88VerifyAccessTokenResponse response = GsonUtil.fromJson(responseStr, W88VerifyAccessTokenResponse.class);
                if (response.getCode() == 0) {
                    String userId = response.getData().getPlayerId();
                    int[] result = Database.instance.checkExitDisplayName(userId);
                    if (result[1] == -1) {
                        SFSErrorData errData = new SFSErrorData(SFSErrorCode.LOGIN_BANNED_USER);
                        errorCode = SFSErrorCode.LOGIN_BANNED_USER;
                        errData.addParameter(accessToken);
                        throw new SFSLoginException("LOGIN_BANNED_USER", errData);
                    }

                    String displayName = response.getData().getDisplayName();
                    if (displayName.length() > 100) {
                        displayName = displayName.substring(0, 100);
                    }
                    if (result[0] == -1) {
                        Database.instance.updateDisplayName(userId, displayName);
                    }

                    /**
                     * set user name cho user là id DB, nếu không xét sẽ phát
                     * sinh user guest đảm bảo chỉ login 1 tài khoản userId va
                     * server (nếu xet force logout thì khi có user mới và sẽ
                     * kick user cũ trên cùng tài khoản)
                     */
                    String avatar = response.getData().getAvatar();
                    if (avatar == null || avatar.isEmpty()) {
                        avatar = Database.instance.getUserAvatar(userId);
                        response.getData().setAvatar(avatar);
                    }

                    if (response.getData().isVerify()) {
                        QueueServiceEvent.getInstance().sendUserVerifyInfo(response.getData());
                    }

                    outData.putUtfString(SFSConstants.NEW_LOGIN_NAME, userId);
                    session.setProperty(UserInforPropertiesKey.ID_DB_USER, userId);
                    session.setProperty(UserInforPropertiesKey.DISPLAY_NAME, displayName);
                    session.setProperty(UserInforPropertiesKey.AVATAR, avatar);
                    session.setProperty(UserInforPropertiesKey.EMAIL, response.getData().getEmail() != null ? response.getData().getEmail() : "");
                    session.setProperty(UserInforPropertiesKey.MERCHANT_ID, merchantId);
                    session.setProperty(UserInforPropertiesKey.CURRENCY, response.getData().getCurrency());
                    session.setProperty(UserInforPropertiesKey.ACCESS_TOKEN, accessToken);
                    session.setProperty(UserInforPropertiesKey.PROFILE, response.getData());
                    return userId;

                } else {
                    SFSErrorData errData = new SFSErrorData(SFSErrorCode.LOGIN_BAD_PASSWORD);
                    errData.addParameter(accessToken);
                    throw new SFSLoginException("INVALID ACCESS TOKEN", errData);
                }

            } else {
                String url = UrlConfig.getInstance().getVerifyAccessTokenUrl();
                VerifyAccessTokenRequest verifyAccessTokenRequest = new VerifyAccessTokenRequest(accessToken);

                String jsonString = GsonUtil.toJson(verifyAccessTokenRequest);

                String responseStr = APIUtils.request(url, jsonString);
                VerifyAccessTokenResponse response = GsonUtil.fromJson(responseStr, VerifyAccessTokenResponse.class);
                if (response.getCode() == ServerConfig.getInstance().getVerifyAccessTokenSuccessCode()) {
                    String userId = response.getData().getAccountId();
                    int[] result = Database.instance.checkExitDisplayName(userId);
                    if (result[1] == -1) {
                        SFSErrorData errData = new SFSErrorData(SFSErrorCode.LOGIN_BANNED_USER);
                        errorCode = SFSErrorCode.LOGIN_BANNED_USER;
                        errData.addParameter(accessToken);
                        throw new SFSLoginException("LOGIN_BANNED_USER", errData);
                    }

                    /**
                     * set user name cho user là id DB, nếu không xét sẽ phát
                     * sinh user guest đảm bảo chỉ login 1 tài khoản userId va
                     * server (nếu xet force logout thì khi có user mới và sẽ
                     * kick user cũ trên cùng tài khoản)
                     */
                    String avatar = response.getData().getAvatar();
                    if (avatar == null || avatar.isEmpty()) {
                        avatar = UrlConfig.getInstance().getDefaultAvatarUrl();
                        response.getData().setAvatar(avatar);
                    }

                    if (response.getData().isVerify()) {
                        QueueServiceEvent.getInstance().sendUserVerifyInfo(response.getData());
                        Database.instance.updateVerify(userId);
                    } else if (response.getData().getType().equalsIgnoreCase("facebook")) {
                        Database.instance.updateVerify(userId);
                    }

                    String displayName = response.getData().getDisplayName();
                    if (displayName.length() > 100) {
                        displayName = displayName.substring(0, 100);
                    }

                    outData.putUtfString(SFSConstants.NEW_LOGIN_NAME, userId);
                    session.setProperty(UserInforPropertiesKey.ID_DB_USER, userId);
                    session.setProperty(UserInforPropertiesKey.DISPLAY_NAME, displayName);
                    session.setProperty(UserInforPropertiesKey.AVATAR, avatar);
                    session.setProperty(UserInforPropertiesKey.EMAIL, response.getData().getEmail());
                    session.setProperty(UserInforPropertiesKey.MERCHANT_ID, response.getData().getMerchantId());
                    session.setProperty(UserInforPropertiesKey.CURRENCY, response.getData().getCurrency());
                    session.setProperty(UserInforPropertiesKey.ACCESS_TOKEN, accessToken);
                    session.setProperty(UserInforPropertiesKey.PROFILE, response.getData());
                    if (response.getData().getChannel() != null) {
                        session.setProperty(UserInforPropertiesKey.CHANNEL, response.getData().getChannel());
                    }
                    return userId;

                } else {
                    SFSErrorData errData = new SFSErrorData(SFSErrorCode.LOGIN_BAD_PASSWORD);
                    errData.addParameter(accessToken);
                    throw new SFSLoginException("INVALID ACCESS TOKEN", errData);
                }
            }

        } catch (Exception ex) {
            this.getLogger().error("authenticate: ", ex);
            SFSErrorData errData = new SFSErrorData(errorCode);
            errData.addParameter(accessToken);
            throw new SFSLoginException("INVALID ACCESS TOKEN", errData);
        }
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
