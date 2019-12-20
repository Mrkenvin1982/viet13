/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.vn.login.handler;

import com.smartfoxserver.v2.core.ISFSEvent;
import com.smartfoxserver.v2.core.SFSEventParam;
import com.smartfoxserver.v2.entities.User;
import com.smartfoxserver.v2.entities.data.ISFSObject;
import com.smartfoxserver.v2.entities.data.SFSArray;
import com.smartfoxserver.v2.entities.data.SFSObject;
import com.smartfoxserver.v2.entities.variables.SFSUserVariable;
import com.smartfoxserver.v2.entities.variables.UserVariable;
import com.smartfoxserver.v2.exceptions.SFSException;
import com.smartfoxserver.v2.exceptions.SFSLoginException;
import com.smartfoxserver.v2.extensions.BaseServerEventHandler;
import game.command.SFSAction;
import game.command.SFSCommand;
import game.key.SFSKey;
import game.vn.common.config.QueueConfig;
import game.vn.common.config.SFSConfig;
import game.vn.common.config.ServerConfig;
import game.vn.common.constant.MoneyContants;
import game.vn.common.device.Device;
import game.vn.common.device.Version;
import game.vn.common.lang.GameLanguage;
import game.vn.common.lib.hazelcast.PlayingBoardManager;
import game.vn.common.lib.hazelcast.UserInfor;
import game.vn.common.lib.hazelcast.UserLoginToken;
import game.vn.common.lib.hazelcast.UserState;
import game.vn.common.lib.news.News;
import game.vn.common.object.ClientInfo;
import game.vn.common.object.api.LoginData;
import game.vn.common.object.api.RegisterData;
import game.vn.common.properties.UserInforPropertiesKey;
import game.vn.common.queue.QueueServiceApi;
import game.vn.util.HazelcastUtil;
import game.vn.util.GlobalsUtil;
import game.vn.util.Utils;
import game.vn.util.db.Database;
import java.util.Date;
import java.util.List;

/**
 * The event is fired after a successful User login.
 *
 * @author tuanp
 */
public class JoinZoneEventHandler extends BaseServerEventHandler {

    @Override
    public void handleServerEvent(ISFSEvent isfse) throws SFSException {
        //xét tạm tiền cho user
        User user = (User) isfse.getParameter(SFSEventParam.USER);
        String userId = String.valueOf(user.getSession().getProperty(UserInforPropertiesKey.ID_DB_USER));
        boolean isNewUser = false;
        try {

            String displayName = String.valueOf(user.getSession().getProperty(UserInforPropertiesKey.DISPLAY_NAME));
            String avatar = String.valueOf(user.getSession().getProperty(UserInforPropertiesKey.AVATAR));
            String email = String.valueOf(user.getSession().getProperty(UserInforPropertiesKey.EMAIL));
            String socialId = String.valueOf(user.getSession().getProperty(UserInforPropertiesKey.EMAIL));
            byte loginType = (byte) user.getSession().getProperty(UserInforPropertiesKey.LOGIN_TYPE);
            ClientInfo clientInfo = (ClientInfo) user.getSession().getProperty(UserInforPropertiesKey.CLIENT_INFOR);

            // check version
            String version = clientInfo.getApp_version();
            String platform = clientInfo.getPlatform();
            String bundleId = clientInfo.getBundle_id();

//            if (!platform.equalsIgnoreCase("web")) {
//                ClientVersionUpdate cvu = Database.instance.getVersionConfig(bundleId, platform, version);
//                if (cvu == null) {
//                    trace(ExtensionLogLevel.WARN, "unknow client version", version, platform, bundleId);
//                    getApi().kickUser(user, null, "unknow client version", 1);
//                    return;
//                } else {
//                    switch (cvu.getAction()) {
//                        case ClientVersionUpdate.WARNING:
//                            sendUpdateClientMessage(user, cvu.getLink(), false);
//                            break;
//                        case ClientVersionUpdate.BLOCK:
//                            trace("old version:", version, platform, bundleId);
//                            sendUpdateClientMessage(user, cvu.getLink(), true);
//                            getApi().kickUser(user, null, "force update", 1);
//                            return;
//                    }
//                }
//            }

            //trace("email:" + email);
            double money = Database.instance.getUserMoney(userId);
            double point = Database.instance.getUserPoint(userId);
            if (money < 0 || point < 0) {    // first login
//                trace("*********** insert user on first login", userId, displayName, avatar, "***********");
                boolean newUser = Database.instance.insertNewUser(userId, socialId, displayName, avatar, email, platform, loginType);
                if (newUser) {
                    isNewUser = true;
                    if (money < 0) {
                        money = 0;
                    }
                    if (point < 0) {
                        point = 0;
                    }
                } else {
                    trace("error insert new user:", userId, displayName, avatar, email);
                    getApi().kickUser(user, null, "", 1);
                    return;
                }
            }

            int userType = Database.instance.getUserType(userId);
            user.setProperty(UserInforPropertiesKey.LOCALE_USER, GlobalsUtil.DEFAULT_LOCALE);
            user.setProperty(UserInforPropertiesKey.IN_TURN, false);
            user.setProperty(UserInforPropertiesKey.USER_TYPE, userType);
            user.setProperty(UserInforPropertiesKey.MONEY_USER, money);
            user.setProperty(UserInforPropertiesKey.POINT_USER, point);
            user.setProperty(UserInforPropertiesKey.ID_DB_USER, userId);

            List<UserVariable> vers = user.getVariables();
            vers.add(new SFSUserVariable(UserInforPropertiesKey.MONEY_USER, money));
            vers.add(new SFSUserVariable(UserInforPropertiesKey.POINT_USER, point));
            vers.add(new SFSUserVariable(UserInforPropertiesKey.ID_DB_USER, userId));
            vers.add(new SFSUserVariable(UserInforPropertiesKey.DISPLAY_NAME, displayName));
            vers.add(new SFSUserVariable(UserInforPropertiesKey.USER_TYPE, userType));
            vers.add(new SFSUserVariable(UserInforPropertiesKey.AVATAR, avatar));
            getApi().setUserVariables(user, vers);

            HazelcastUtil.lockUser(userId);
            if (HazelcastUtil.isOnlineOtherLoginServer(userId)) {
                getApi().kickUser(user, null, "", 1);
            }
            // login success then add user info into hazelcast
            UserInfor userInfo = new UserInfor();
            userInfo.setUserId(userId);
            userInfo.setServerName(SFSConfig.getServerName());
            userInfo.setIdDevice(clientInfo.getUdid());
            HazelcastUtil.addUserInfo(userInfo);

            //set device cho user
            Device device = new Device();
            device.setClientInfor(clientInfo);
            device.setVersion(new Version(clientInfo.getApp_version()));
            device.setBundleId(clientInfo.getBundle_id());
            user.setProperty(UserInforPropertiesKey.DEVICE, device);
            PlayingBoardManager playingBoard = HazelcastUtil.getPlayingBoard(userId);
            if (playingBoard == null) {
                //login vao bình thường
                if (!joinZoneNormal(user, displayName, clientInfo, avatar)) {
                    getApi().kickUser(user, null, "", 1);
                }
            } else {
                //set trang thái reconnect
                if (!doReconect(user)) {
                    getApi().kickUser(user, null, "", 1);
                }
            }

        } catch (Exception e) {
            getLogger().error("JoinZoneEventHandler error: ", e);
            getApi().kickUser(user, null, "", 1);
        }
        HazelcastUtil.unlockUser(userId);
//        trace("Client join zone sucess");
    }

    private void sendPopup(User user, List<News> popups) {
        ISFSObject isfso = new SFSObject();
        isfso.putInt(SFSKey.ACTION_INCORE, SFSAction.GET_POPUP);
        SFSArray arr = new SFSArray();
        if (popups != null && !popups.isEmpty()) {
            for (News news : popups) {
                SFSObject obj = new SFSObject();
                obj.putUtfString(SFSKey.TITLE, news.getTitle());
                obj.putUtfString(SFSKey.CONTENT, news.getContent());
                if (news.getButton1() != null) {
                    SFSObject btn = new SFSObject();
                    btn.putUtfString(SFSKey.CAPTION, news.getButton1().getCaption());
                    btn.putUtfString(SFSKey.DATA, news.getButton1().getData());
                    btn.putByte(SFSKey.TYPE, news.getButton1().getType());
                    obj.putSFSObject(SFSKey.BUTTON1, btn);
                }
                if (news.getButton2() != null) {
                    SFSObject btn = new SFSObject();
                    btn.putUtfString(SFSKey.CAPTION, news.getButton2().getCaption());
                    btn.putUtfString(SFSKey.DATA, news.getButton2().getData());
                    btn.putByte(SFSKey.TYPE, news.getButton2().getType());
                    obj.putSFSObject(SFSKey.BUTTON2, btn);
                }

                obj.putUtfString(SFSKey.ICON, news.getIcon());
                obj.putUtfString(SFSKey.IMAGE, news.getImage());
                obj.putUtfString(SFSKey.IMAGE_LARGE, news.getImageLarge());
                obj.putByte(SFSKey.CATEGORY, (byte) 0);
                obj.putLong(SFSKey.TIME, (System.currentTimeMillis() - news.getStartTime()) / 1000);
                arr.addSFSObject(obj);
            }
        }

        isfso.putSFSArray(SFSKey.POPUP, arr);
        getParentExtension().send(SFSCommand.CLIENT_REQUEST, isfso, user);
    }

    /**
     * join vao join bình thường
     *
     * @param user
     */
    private boolean joinZoneNormal(User user, String displayName, ClientInfo clientInfoObj, String avatar) {
        try {
            String userId = String.valueOf(user.getSession().getProperty(UserInforPropertiesKey.ID_DB_USER));
//            trace("---- userId", userId);
            //add thông tin user đến hazelcast
            if (!addInfoUserToHazelcast(userId, user, displayName, clientInfoObj)) {
                return false;
            }

            user.setProperty(UserInforPropertiesKey.LOCALE_USER, GlobalsUtil.DEFAULT_LOCALE);
            user.setProperty(UserInforPropertiesKey.IN_TURN, false);
            user.setProperty(UserInforPropertiesKey.SESSION_ID, clientInfoObj.getSessionId());

            int moneyType = MoneyContants.MONEY;
            List<UserVariable> vers = user.getVariables();
            vers.add(new SFSUserVariable(UserInforPropertiesKey.MONEY_TYPE, moneyType));
            vers.add(new SFSUserVariable(UserInforPropertiesKey.AVATAR, avatar));
            getApi().setUserVariables(user, vers);

            sendJoinZoneSuccessMessage(user, userId);
            onLoginSuccess(userId);
//            List<News> popups = Database.instance.getListPopup(GlobalsUtil.DEFAULT_LOCALE.getLanguage(), true);
//            if (popups != null && !popups.isEmpty()) {
//                sendPopup(user, popups);
//            }
            UserState userState = HazelcastUtil.getUserState(userId);
            if (userState != null) {
                userState.setMoneyType(moneyType);
                HazelcastUtil.updateUserState(userState);
            }
            return true;
        } catch (Exception e) {
            this.getLogger().error("joinZoneNormal error: ", e);
        }
        this.getLogger().debug("---- joinZoneNormal DONE");
        return false;
    }

    /**
     * Reconnect vao join
     */
    private boolean doReconect(User user) {

        String userId = String.valueOf(user.getSession().getProperty(UserInforPropertiesKey.ID_DB_USER));
        try {
            UserState userState = HazelcastUtil.getUserState(userId);
            if (userState == null) {
                return false;
            }
            ClientInfo clientInfoObj = (ClientInfo) user.getSession().getProperty(UserInforPropertiesKey.CLIENT_INFOR);
            if (clientInfoObj != null) {
                userState.setShuffleGames(clientInfoObj.getShuffleGames());
                userState.setSessionId(clientInfoObj.getSessionId());
                userState.setEmail(String.valueOf(user.getSession().getProperty(UserInforPropertiesKey.EMAIL)));
            }
            userState.setIp(user.getSession().getAddress());

            List<UserVariable> vers = user.getVariables();
            vers.add(new SFSUserVariable(UserInforPropertiesKey.MONEY_TYPE, userState.getMoneyType()));
            getApi().setUserVariables(user, vers);

            UserLoginToken userToken = (UserLoginToken) user.getSession().getProperty(UserInforPropertiesKey.USER_TOKEN);
            HazelcastUtil.addUserLoginToken(userToken);
            userState.setLoginToken(userToken.getToken());

            HazelcastUtil.updateUserState(userState);

            sendJoinZoneSuccessMessage(user, userId);
            return true;
        } catch (Exception e) {
            this.getLogger().error("doReconect error: ", e);
        }
        return false;
    }

    private boolean addInfoUserToHazelcast(String userId, User user, String displayName, ClientInfo clientInfoObj) {
        try {
            // add userState to hazelcast
            UserState userstat = new UserState();
            userstat.setUserId(userId);
            userstat.setDisplayName(displayName);
            userstat.setIp(user.getSession().getAddress());
            userstat.setLocale(Utils.getUserLocale(user));

            if (clientInfoObj != null) {
                userstat.setShuffleGames(clientInfoObj.getShuffleGames());
                userstat.setSessionId(clientInfoObj.getSessionId());
                userstat.setEmail(String.valueOf(user.getSession().getProperty(UserInforPropertiesKey.EMAIL)));
            }
            userstat.setLoginDate(new Date());
            userstat.addListServerId(ServerConfig.getInstance().getServerId());
            UserLoginToken userToken = (UserLoginToken) user.getSession().getProperty(UserInforPropertiesKey.USER_TOKEN);

            HazelcastUtil.addUserLoginToken(userToken);
            userstat.setLoginToken(userToken.getToken());

            HazelcastUtil.addUserState(userstat);
            return true;
        } catch (Exception e) {
            getLogger().error("JoinZoneEventHandler error: ", e);
        }
        return false;
    }

    /**
     * process after account checking
     *
     * @param userId
     * @param outData output
     * @throws SFSLoginException
     */
    private void onLoginSuccess(String userId) {
        Database.instance.updateLastLogin(userId);
//        QueueNotify.getInstance().notifyUserLogin(userId);
    }

    /**
     * gửi message thông báo join zone thành công, gửi kèm popup nếu có
     *
     * @param user
     */
    private void sendJoinZoneSuccessMessage(User user, String userId) {
        SFSObject sfsObj = new SFSObject();
        sfsObj.putInt(SFSKey.SERVER_ID, ServerConfig.getInstance().getServerId());
        sfsObj.putIntArray(SFSKey.SHUFFLE_GAMES, ServerConfig.getInstance().getListShuffleGame());
        sfsObj.putInt(SFSKey.ACTION_INCORE, SFSAction.JOIN_ZONE_SUCCESS);
        getParentExtension().send(SFSCommand.CLIENT_REQUEST, sfsObj, user);
//        trace("---- sendJoinZoneSuccessMessage", userId);
    }

    private void sendUpdateClientMessage(User user, String url, boolean required) {
        SFSObject sfsObj = new SFSObject();
        sfsObj.putInt(SFSKey.ACTION_INCORE, SFSAction.UPDATE_CLIENT);
        sfsObj.putUtfString(SFSKey.URL, url);
        if (required) {
            sfsObj.putUtfString(SFSKey.MESSAGE, GameLanguage.getMessage(GameLanguage.UPDATE_CLIENT_BLOCK, GlobalsUtil.DEFAULT_LOCALE));
        } else {
            sfsObj.putUtfString(SFSKey.MESSAGE, GameLanguage.getMessage(GameLanguage.UPDATE_CLIENT_WARNING, GlobalsUtil.DEFAULT_LOCALE));
        }
        sfsObj.putBool(SFSKey.REQUIRED, required);
        getParentExtension().send(SFSCommand.CLIENT_REQUEST, sfsObj, user);
    }

    /**
     * gửi thông tin login qua ttkt
     *
     * @param userId
     */
    private void sendLoginDataApi(User user, String userId, String displayName, String sessionId, int authorizeType) {
        LoginData data = new LoginData(userId);
        data.setServerId(ServerConfig.getInstance().getServerId());
        data.setServerName(SFSConfig.getServerName());
        data.setPlayerName(displayName);
        data.setSessionId(sessionId);
        data.setRequestId(userId + System.currentTimeMillis());
        data.setAuthorizeType(authorizeType);
        data.setIsBot(Utils.isBot(user));
        data.setConnectionId(ServerConfig.getInstance().getConnectionId());
        data.setChannel(String.valueOf(user.getSession().getProperty(UserInforPropertiesKey.CHANNEL)));
        QueueServiceApi.getInstance().sendData(QueueConfig.getInstance().getKeyLogin(), true, data);
    }

    /**
     * gửi thông tin register qua ttkt
     *
     * @param userId
     * @param displayName
     * @param sessionId
     * @param isActive
     */
    private void sendRegisterDataApi(String userId, String displayName, String sessionId, String email, String channel) {
        RegisterData data = new RegisterData(userId);
        data.setPlayerName(displayName);
        data.setSessionId(sessionId);
        data.setRequestId(userId + System.currentTimeMillis());
        data.setEmail(email);
        data.setIsActive(1);
        data.setChannel(channel);
        data.setConnectionId(ServerConfig.getInstance().getConnectionId());
        QueueServiceApi.getInstance().sendData(QueueConfig.getInstance().getKeyRegister(), true, data);
    }

}
