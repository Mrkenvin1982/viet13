package game.vn.common.handler;

import com.smartfoxserver.v2.core.ISFSEvent;
import com.smartfoxserver.v2.core.SFSEventParam;
import com.smartfoxserver.v2.entities.Room;
import com.smartfoxserver.v2.entities.User;
import com.smartfoxserver.v2.entities.data.SFSObject;
import com.smartfoxserver.v2.extensions.BaseServerEventHandler;
import game.command.SFSCommand;
import game.vn.common.GameExtension;
import game.vn.common.config.RoomConfig;
import game.vn.common.config.ServerConfig;
import game.vn.common.constant.MoneyContants;
import game.vn.common.lang.GameLanguage;
import game.vn.common.lib.hazelcast.PlayingBoardManager;
import game.vn.common.lib.hazelcast.UserState;
import game.vn.common.message.MessageController;
import game.vn.common.object.UserJoinGameInfo;
import game.vn.common.properties.RoomInforPropertiesKey;
import game.vn.util.HazelcastUtil;
import game.vn.common.properties.UserInforPropertiesKey;
import game.vn.common.queue.QueueNotify;
import game.vn.util.Utils;
import java.util.Locale;

/**
 * join game room or lobby room
 * @author TuanP
 */
public class JoinRoomHandler extends BaseServerEventHandler {

    @Override
    public void handleServerEvent(ISFSEvent isfse) {
        User user = (User) isfse.getParameter(SFSEventParam.USER);
        Room room = (Room) isfse.getParameter(SFSEventParam.ROOM);
        user.setJoining(true);
        try {
//            trace(" id user join: " + user.getName() + ": " + user.getId());
            int roomType = room.getVariable(RoomInforPropertiesKey.MONEY_TYPE).getIntValue();
            //nếu đóng game tiền thật thì không cho join vào lobby real
            if(roomType == MoneyContants.MONEY && ServerConfig.getInstance().isCloseRealMoney()){
                if (room.isGame()) {
                    this.getApi().kickUser(user, null, "",1);
                }else{
                    this.getApi().leaveRoom(user, room);
                }
                return;
            }
            if ( room.isGame()) {
                //join room game
                if(user.isPlayer()){
                    processJoinGame(room, user);
                }else{
                   this.getApi().kickUser(user, null, "",1); 
                }
            } else {
                // join lobby
                processJoinLobby(room,user);   
            }

        } catch (Exception e) {
            this.getLogger().error("SFSUtil JOIN ROOM  error: ", e);
            this.getApi().kickUser(user,null, "",1);
        }
    }
    
    /**
     * Xử lý khi user join normal lobby chổ này
     * @param room
     * @param user 
     */
    private void processJoinLobby(Room room, User user) {
        try {
            int roomType = room.getVariable(RoomInforPropertiesKey.MONEY_TYPE).getIntValue();
            int moneyTypeOfUser = Utils.getMoneyTypeOfUser(user);
            String idDBUser = Utils.getIdDBOfUser(user);
            Locale localeUser = Utils.getUserLocale(user);
            
            //kiểm tra có vào đúng type room lobby
            if(roomType != moneyTypeOfUser){
                this.getLogger().info(idDBUser+ " không cùng moneyType error: user="+moneyTypeOfUser+", room= "+roomType);
                SFSObject ob = MessageController.getToastMessage(GameLanguage.getMessage(GameLanguage.JOIN_ROOM_ERROR,localeUser),3);
                getParentExtension().send(SFSCommand.CLIENT_REQUEST, ob, user);
                this.getApi().leaveRoom(user, room);
                return;
            }

            // join lobby tiền ảo
            byte serviceId = Utils.getServiceId(room.getName());

            //kiểm tra game có đang bảo trì
            if(RoomConfig.getInstance().isMaintainGame(serviceId) || RoomConfig.getInstance().isMaintainAllGame()){
                this.getApi().leaveRoom(user, room);
                SFSObject ob = MessageController.getToastMessage(RoomConfig.getInstance().getMaintainInfor(localeUser),3);
                getParentExtension().send(SFSCommand.CLIENT_REQUEST, ob, user);
                return;
            }
            user.setProperty(UserInforPropertiesKey.SERVICE_ID, serviceId);
            HazelcastUtil.lockUserState(idDBUser);
            UserState userState = HazelcastUtil.getUserState(idDBUser);
            if (userState == null) {
                userState = new UserState();
            }
            userState.setCurrentLobbyName(room.getName());
            HazelcastUtil.updateUserState(userState);
            HazelcastUtil.unlockUserState(idDBUser);
        } catch (Exception e) {
            this.getLogger().error("processJoinLobby()  error: ", e);
            this.getApi().leaveRoom(user, room);
        }
    }
    /**
     * Xử lý khi user join game
     */
    private void processJoinGame(Room room, User user) {
        GameExtension getExt = (GameExtension) getParentExtension();  
        String userId = Utils.getIdDBOfUser(user);
        Locale localeUser = Utils.getUserLocale(user);
        
        try {
            if (getExt.gameController == null) {
                this.getLogger().info(userId+ " getExt.gameController == null error");
                this.getApi().kickUser(user,null, GameLanguage.getMessage(GameLanguage.JOIN_ROOM_ERROR,localeUser),1);
                return;
            }
            int moneyTypeOfRoom = room.getVariable(RoomInforPropertiesKey.MONEY_TYPE).getIntValue();
            int moneyTypeOfUser = Utils.getMoneyTypeOfUser(user);
            
            /**
             * Nếu đã tồn tại user trong phòng thì đang on return game
             */
            if (getExt.gameController.isReconnect(user)) {
                getExt.gameController.onReturnGame(user);
                return;
            } 
             
            //kick user khi user o không đúng loại phòng
            if (moneyTypeOfRoom != moneyTypeOfUser) {
                this.getLogger().info(userId+ " không cùng moneyType error: user="+moneyTypeOfUser+", room= "+moneyTypeOfRoom);
                this.getApi().kickUser(user,null, GameLanguage.getMessage(GameLanguage.JOIN_ROOM_ERROR,localeUser),1);
                return;
            }
            
            
            //check xem có chơi bàn nào không, nếu có thi phai reconnect khong cho thao tac khac
            PlayingBoardManager playingBoard= HazelcastUtil.getPlayingBoard(userId);
            if (playingBoard != null) {
                if (playingBoard.getBoardPlaying() != null) {
                    this.getApi().kickUser(user,null, GameLanguage.getMessage(GameLanguage.JOIN_ROOM_ERROR,localeUser),1);
                    return;
                }
            }

            if (getExt.gameController.join(user, "")) {
                UserJoinGameInfo info = new UserJoinGameInfo();
                info.setUserId(userId);
                info.setUserType(Utils.getUserType(user));
                info.setRoomName(room.getName());
                info.setBetBoard(room.getVariable(RoomInforPropertiesKey.BET_BOARD).getDoubleValue());
                info.setIdOwner(room.getVariable(RoomInforPropertiesKey.ID_OWNER).getStringValue());
                info.setIsTournament(room.getVariable(RoomInforPropertiesKey.IS_TOURNAMENT).getBoolValue());
                info.setMaxBetBoard(room.getVariable(RoomInforPropertiesKey.MAX_BET_BOARD).getDoubleValue());
                info.setMode(room.getVariable(RoomInforPropertiesKey.MODE).getIntValue());
                info.setMoneyType(room.getVariable(RoomInforPropertiesKey.MONEY_TYPE).getIntValue());
                info.setServiceId(room.getVariable(RoomInforPropertiesKey.SERVICE_ID).getIntValue());
                QueueNotify.getInstance().notifyUserJoinGame(info);
            } else {
                this.getApi().kickUser(user, null, GameLanguage.getMessage(GameLanguage.JOIN_ROOM_ERROR, localeUser), 1);
                if (getExt.gameController.isCanRemoveRoom()) {
                    this.getApi().removeRoom(room);
                }
            }
        } catch (Exception e) {
            this.getLogger().error("processJoinGame error: ", e);
            this.getApi().kickUser(user,null, GameLanguage.getMessage(GameLanguage.JOIN_ROOM_ERROR,localeUser),1);
        }
    }
}
