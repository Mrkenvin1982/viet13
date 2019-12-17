/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.vn.game.login.handler;

import com.smartfoxserver.v2.core.ISFSEvent;
import com.smartfoxserver.v2.core.SFSEventParam;
import com.smartfoxserver.v2.entities.User;
import com.smartfoxserver.v2.entities.data.SFSArray;
import com.smartfoxserver.v2.entities.data.SFSObject;
import com.smartfoxserver.v2.entities.variables.SFSUserVariable;
import com.smartfoxserver.v2.entities.variables.UserVariable;
import com.smartfoxserver.v2.exceptions.SFSException;
import com.smartfoxserver.v2.extensions.BaseServerEventHandler;
import game.command.SFSAction;
import game.command.SFSCommand;
import game.key.SFSKey;
import game.vn.common.config.ServerConfig;
import game.vn.common.constant.MoneyContants;
import game.vn.common.lang.GameLanguage;
import game.vn.common.lib.hazelcast.UserState;
import game.vn.common.properties.UserInforPropertiesKey;
import game.vn.common.user.state.MagagerUserState;
import game.vn.util.HazelcastUtil;
import game.vn.util.Utils;
import game.vn.util.db.Database;
import java.util.ArrayList;
import java.util.List;

/**
 * The event is fired after a successful User login.
 * 
 * @author tuanp
 */
public class JoinZoneEventHandler extends BaseServerEventHandler{

    @Override
    public void handleServerEvent(ISFSEvent isfse) throws SFSException {
        User user = (User) isfse.getParameter(SFSEventParam.USER);
        try {
            this.getLogger().debug("-----------JoinZoneEventHandler--------------");
            //xét tạm tiền cho user
            String userId = (String) user.getSession().getProperty(UserInforPropertiesKey.ID_DB_USER);
            String displayName = String.valueOf(user.getSession().getProperty(UserInforPropertiesKey.DISPLAY_NAME));
//            boolean isForceLogout=(boolean)user.getSession().getProperty(UserInforPropertiesKey.IS_FORCE_LOGOUT);

            UserState userState = HazelcastUtil.getUserState(userId);
            //loại tiền user dang chọn
            int moneyTypeOfUser = userState.getMoneyType();
            
            if (moneyTypeOfUser == MoneyContants.MONEY && ServerConfig.getInstance().isCloseRealMoney()) {
                String infor = GameLanguage.getMessage(GameLanguage.NOT_EXIST_MONEY_BOARD, Utils.getUserLocale(user));
                getApi().kickUser(user, null, infor, 1);
                return;
            }
            /**
             * Kiem tra đang online server hiện tại
             */
            if (!userState.isOnlineCurrentServer(ServerConfig.getInstance().getServerId())) {
                //join zone normal
                joinZoneNormal(userId);
            }
            String avatar = Database.instance.getUserAvatar(userId);
            int userType = Database.instance.getUserType(userId);

            user.setProperty(UserInforPropertiesKey.LOCALE_USER, userState.getLocale());
            user.setProperty(UserInforPropertiesKey.IN_TURN, false);
            user.setProperty(UserInforPropertiesKey.USER_TYPE, userType);
            user.setProperty(UserInforPropertiesKey.AVATAR, avatar);
            user.setProperty(UserInforPropertiesKey.MONEY_STACK, 0);
            user.setProperty(UserInforPropertiesKey.SEAT_USER, -1);
            user.setProperty(UserInforPropertiesKey.USER_STATE, new MagagerUserState());
            user.setProperty(UserInforPropertiesKey.IS_WAITER, false);
            user.setProperty(UserInforPropertiesKey.ID_DB_USER, userId);
            user.setProperty(UserInforPropertiesKey.ADVANTAGE_RATIO, 0);
            user.setProperty(UserInforPropertiesKey.FIRST_RATIO, 0);
            user.setProperty(UserInforPropertiesKey.GROUP_ID, 0);

//            String userAvatar = AvatarService.instance.getUserAvatar(userId);
            List<UserVariable> vers = user.getVariables();
            vers.add(new SFSUserVariable(UserInforPropertiesKey.MONEY_STACK, (double) 0));
            vers.add(new SFSUserVariable(UserInforPropertiesKey.SEAT_USER, -1));
            vers.add(new SFSUserVariable(UserInforPropertiesKey.QUICK_PLAY, false));
            vers.add(new SFSUserVariable(UserInforPropertiesKey.ID_DB_USER, userId));
            vers.add(new SFSUserVariable(UserInforPropertiesKey.AVATAR, avatar));
            vers.add(new SFSUserVariable(UserInforPropertiesKey.MONEY_TYPE, moneyTypeOfUser));
            vers.add(new SFSUserVariable(UserInforPropertiesKey.DISPLAY_NAME, displayName));
            vers.add(new SFSUserVariable(UserInforPropertiesKey.USER_TYPE, userType));
            List<Integer> shuffleGames = userState.getShuffleGames();
            if (shuffleGames == null) {
                shuffleGames = new ArrayList<>();
            }
            SFSArray arr = new SFSArray();
            for (int gameId : shuffleGames) {
                arr.addInt(gameId);
            }
            vers.add(new SFSUserVariable(UserInforPropertiesKey.SHUFFLE_GAMES, arr));
            user.setVariables(vers);
            getParentExtension().send(SFSCommand.CLIENT_REQUEST, getUserInfoSucessMessage(true), user);
            
        } catch (Exception e) {
            getLogger().error("JoinZoneEventHandler error: ", e);
            getApi().kickUser(user, null, "", 1);
        }
    }
    
    /**
     * Join zone bình thường
     * @param user
     * @param moneyTypeOfUser 
     */
    private void joinZoneNormal(String userId) {
        HazelcastUtil.lockUserState(userId);
        UserState userState= HazelcastUtil.getUserState(userId);
        if (userState != null) {
            userState.addListServerId(ServerConfig.getInstance().getServerId());
            HazelcastUtil.updateUserState(userState);
        }
        HazelcastUtil.unlockUserState(userId);
    }
    
    /**
     * Gửi về thông tin user join zone thành công cho client
     * client bắt chổ này để đảm bảo user đã update thành công data
     * mới cho join vao phòng
     * @param isReconnect
     * @return 
     */
    private SFSObject getUserInfoSucessMessage(boolean isReconnect) {
        SFSObject ojBoardInfo = new SFSObject();
        try {
            ojBoardInfo.putInt(SFSKey.ACTION_INCORE, SFSAction.JOIN_ZONE_SUCCESS);
            ojBoardInfo.putBool(SFSKey.STATUS, isReconnect);
        } catch (Exception e) {
            this.getLogger().error("getBoardInfoMessage erro: ", e);
        }
        return ojBoardInfo;
    }
}
