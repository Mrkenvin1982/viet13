/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.vn.game.login.handler.request;

import com.smartfoxserver.v2.entities.Room;
import com.smartfoxserver.v2.entities.User;
import com.smartfoxserver.v2.entities.data.ISFSObject;
import com.smartfoxserver.v2.entities.match.BoolMatch;
import com.smartfoxserver.v2.entities.match.MatchExpression;
import com.smartfoxserver.v2.entities.match.NumberMatch;
import com.smartfoxserver.v2.entities.match.RoomProperties;
import com.smartfoxserver.v2.extensions.BaseClientRequestHandler;
import game.command.SFSAction;
import game.key.SFSKey;
import game.vn.common.config.RoomConfig;
import game.vn.common.config.ServerConfig;
import game.vn.common.constant.MoneyContants;
import game.vn.common.constant.Service;
import game.vn.common.lang.GameLanguage;
import game.vn.common.lib.hazelcast.PlayingBoardManager;
import game.vn.common.lib.hazelcast.UserState;
import game.vn.common.properties.RoomInforPropertiesKey;
import game.vn.common.tournament.TournamentManager;
import game.vn.util.HazelcastUtil;
import game.vn.util.Utils;
import game.vn.util.db.Database;
import java.util.List;
import java.util.Locale;

/**
 * xử lý những command client gui len trong extension login
 *
 * @author tuanp
 */
public class CommonClientRequest extends BaseClientRequestHandler {

    @Override
    public void handleClientRequest(User user, ISFSObject isfso) {
        processClientMessage(user, isfso);
    }

    /**
     * Xử lý các message liên quan tới lobby chổ này
     */
    private void processClientMessage(User user, ISFSObject isfso) {
        try {
            //is lobby
            int action = isfso.getInt(SFSKey.ACTION_INCORE);
            trace(user.getName() + " send common client request action: " + action);
            switch (action) {
                case SFSAction.CREATE_BOARD:
                    String idDB = Utils.getIdDBOfUser(user);
                    HazelcastUtil.lockUserState(idDB);
                    this.findAndCreateRoom(user);
                    HazelcastUtil.unlockUserState(idDB);
                    break;
            }
        } catch (Exception e) {
            this.getLogger().error("CommonClientRequest.processClientMessage() error: ", e);
            getApi().kickUser(user, null, "join room eror", 1);
        }
    }
   
    /**
     * User join vào phòng theo mức cược nếu không tim được phòng thì sẽ tao
     * phòng theo mức cược
     *
     * @param user
     * @param bet
     */
    private void findAndCreateRoom(User user) {
        try {
            String userId =  Utils.getIdDBOfUser(user);
            Locale localeOfUser = Utils.getUserLocale(user);
            
            //loại tiền user chọn chơi: tiền thiệt va tiền ảo
            int moneyTypeOfUser= Utils.getMoneyTypeOfUser(user);
            if(moneyTypeOfUser==MoneyContants.MONEY && ServerConfig.getInstance().isCloseRealMoney()){
                String infor=GameLanguage.getMessage(GameLanguage.NOT_EXIST_MONEY_BOARD, localeOfUser);
                getApi().kickUser(user, null, infor, 1);
                return;
            }
            
            UserState userState = HazelcastUtil.getUserState(userId);
            if(userState == null){
                String infor=GameLanguage.getMessage(GameLanguage.JOIN_ROOM_ERROR, localeOfUser);
                getApi().kickUser(user, null, infor, 1);
                throw new Exception("----userState null in gameLogin ");
            }
            if (userState.getCurrentLobbyName().isEmpty()) {
                String infor = GameLanguage.getMessage(GameLanguage.JOIN_ROOM_ERROR, localeOfUser);
                getApi().kickUser(user, null, infor, 1);
                this.getLogger().error("----getCurrentLobbyName is empty in gameLogin error ");
                return;
            }
           
            List<Double> listBet;
            boolean isTournament = RoomConfig.getInstance().getTournamentNameGames().contains(userState.getCurrentLobbyName());
            if (isTournament) {
                listBet = TournamentManager.getInstance().getTicketValues();
            } else {
                //lấy ra danh sách mức cược trong lobby game
                String strBets = RoomConfig.getInstance().getListBet(userState.getCurrentLobbyName());
                //không tồn tại lobby request tạo bàn
                if (strBets.isEmpty()) {
                    String infor = GameLanguage.getMessage(GameLanguage.JOIN_ROOM_ERROR, localeOfUser);
                    getApi().kickUser(user, null, infor, 1);
                    throw new Exception(infor + "----strBets is empty in gameLogin ");
                }
                //không  tồn tại mức cược gửi lên tạo bàn
                listBet = Utils.convertToListDouble(strBets);
            }

            
            if(!listBet.contains(userState.getBetBoard())){
                String infor = GameLanguage.getMessage(GameLanguage.NOT_EXIST_BET_BOARD, localeOfUser);
                getApi().kickUser(user, null, infor, 1);
                return;
            }
            
            //check xem có chơi bàn nào không, nếu có cho vao khong tạo nữa
            PlayingBoardManager playingBoard= HazelcastUtil.getPlayingBoard(userId);
            if (playingBoard != null) {
                if (playingBoard.getBoardPlaying() != null) {
                    Room room = getParentExtension().getParentZone().getRoomByName(playingBoard.getBoardPlaying().getName());
                    if (room != null) {
                        getApi().joinRoom(user, room);
                        return;
                    }
                    String infor = GameLanguage.getMessage(GameLanguage.JOIN_ROOM_ERROR, localeOfUser);
                    getApi().kickUser(user, null, infor, 1);
                    throw new Exception(infor + "----can not comback playing game :"+playingBoard.getBoardPlaying().getName());
                }
            }
            
            //số lần mức cược tối thiểu mua tẩy
            int moneyFactory = RoomConfig.getInstance().getMinJoinOwner(userState.getCurrentLobbyName());
            //số tiền tối thiểu mua tẩy
            double minBetBoard =Utils.multiply(userState.getBetBoard(), moneyFactory) ;
            
            //xét tiền cho user chổ này
            double money = 0;
            if (moneyTypeOfUser == MoneyContants.MONEY) {
                money = userState.getMoneyStack();
                if (money < minBetBoard && Database.instance.getUserMoney(userId) >= minBetBoard) {
                    money = minBetBoard;
                    userState.setMoneyStack(money);
                }
            } else {
                money = userState.getPointStack();
                if (money < minBetBoard && Database.instance.getUserPoint(userId) >= minBetBoard) {
                    money = minBetBoard;
                    userState.setPointStack(money);
                }
            }
            HazelcastUtil.updateUserState(userState);
            
            if (money < minBetBoard) {
                String infor = GameLanguage.getMessage(GameLanguage.NO_MONEY_USER, localeOfUser);
                infor = String.format(infor, moneyFactory);
                getApi().kickUser(user, null, infor, 1);
                return;
            }
            
            //khi vào handler join room thi chặn không cho tạo bàn nữa
            if (!user.isJoining()) {
                byte serviceId = Utils.getServiceId(userState.getCurrentLobbyName());
                byte mode = Utils.getUserPlayMode(user, serviceId);
                
                if (serviceId == Service.SYSTEM) {
                    String infor = GameLanguage.getMessage(GameLanguage.JOIN_ROOM_ERROR, Utils.getUserLocale(user));
                    getApi().kickUser(user, null, infor, 1);
                } else {
                    Room room = createBoard(userState.getBetBoard(), serviceId, moneyTypeOfUser, mode, isTournament);
                    // phải sử dụng getApi().joinRoom mới nhảy vào event join room
                    getApi().joinRoom(user, room);
                }
            }
        } catch (Exception e) {
            this.getLogger().error("userJoinRoom erro: ", e);
            String infor = GameLanguage.getMessage(GameLanguage.JOIN_ROOM_ERROR, Utils.getUserLocale(user));
            getApi().kickUser(user, null, infor, 1);
        }
    }
    /**
     * Tìm phòng theo mức cược
     *
     * @param bet
     * @return
     */
    private List<Room> findRoom(double bet,int serviceId, int moneyType) {
        MatchExpression exp = new MatchExpression(RoomProperties.IS_GAME, BoolMatch.EQUALS, true)
                .and(RoomProperties.HAS_FREE_PLAYER_SLOTS, BoolMatch.EQUALS, true)
                .and(RoomInforPropertiesKey.BET_BOARD, NumberMatch.EQUALS, bet).and(RoomInforPropertiesKey.MONEY_TYPE, NumberMatch.EQUALS, moneyType);

        // Search Rooms
        List<Room> joinableRooms = getApi().findRooms(getParentExtension().getParentZone().getRoomListFromGroup(Utils.getGroupIdFromLobby(serviceId)), exp, 0);
        this.getLogger().debug("joinableRooms " + joinableRooms);
        return joinableRooms;
    }

    /**
     * tạo bàn
     * @param bet
     * @param serviceId
     * @param moneyType
     * @param mode
     * @param isTournament
     * @return 
     */
    private Room createBoard(double bet, int serviceId, int moneyType, byte mode, boolean isTournament) {
        return Utils.createBoardGame(bet, serviceId, getParentExtension().getParentZone(), moneyType, mode, isTournament);
    }
}
