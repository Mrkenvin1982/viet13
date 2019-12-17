/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.vn.common.config;

import game.vn.common.tournament.Tournament;
import game.vn.util.GlobalsUtil;
import game.vn.util.watchservice.PropertyConfigurator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 *
 * @author tuanp
 */
public class RoomConfig extends PropertyConfigurator {

    private final static RoomConfig INSTANCE = new RoomConfig("conf/", "roomConfig.properties");
    private static final String IDLE_TIME = "game.idleTime";

    public static final String PENNALIZE_LEAVER_NAME_LOBBY = "penalize.leaver.";
    public static final String GAME_MIN_JOIN_NAME_LOBBY = "game.minjoin.";
    public static final String MAX_BET_NAME_LOBBY = "game.maxbet.";
    public static final String NO_PLAYER_NAME_LOBBY = "game.noplayer.";
    public static final String WAITING_TIME_NAME_LOBBY = "game.waitingtime.";
    public static final String PLAYING_TIME_NAME_LOBBY = "game.playingtime.";
    public static final String RESULT_TIME_NAME_LOBBY = "game.resulttime.";
    public static final String MIN_JOIN_OWNER_NAME_LOBBY = "game.minjoin.owner.";
    public static final String POINT_GAMES = "game.active";
    public static final String MONEY_GAMES = "game.real.active";
    public static final String TAX_GAME = "game.tax.";
    public static final String PRIORITY_GAME = "game.priority.";
    public static final String DEFAULT_MONEY_MULTIPLIER = "game.defaultMoneyMultiplier.";
    public static final String MIN_MONEY_TIME_AUTO_JOIN = "game.minMoneyTimeAutoJoin";
    public static final String MAINTAIN_GAME = "game.maintain.";
    public static final String MAINTAIN_ALL_GAME = "game.maintain.allGame";
    public static final String MAINTAIN_INFOR = "game.maintain.infor";
    public static final String MAINTAIN_INFOR_EN = "game.maintain.infor_en";
    public static final String GAME_WINNING = "game.tournament.countWinning.";
    public static final String GAME_TOURNAMNET = "game.tournament.";
    public static final String ROTATE_TIME = "game.rotateTime.";
    public static final String LEAVE_ROOM_DELAY_TIME = "game.delaytime";
    
    private static final String TOURNAMNET_NAME_GAMES = "game.tournamentnames";
    private static final String MAX_NO_ACTION_TIME = "server.maxNoActionTime";
    private static final String TAX_THRESHOLD ="tax.threshold";

    private List<String> tournamentNames;

    public static RoomConfig getInstance() {
        return INSTANCE;
    }

    public RoomConfig(String path, String nameFile) {
        super(path, nameFile);
    }

    @Override
    protected void doChanged() {
        super.doChanged();
    }

    /**
     * Lấy ra danh sách point game
     *
     * @return
     */
    public String getPointGames() {
        return this.getStringAttribute(POINT_GAMES, "");
    }

    /**
     * Lấy ra danh sách money game
     *
     * @return
     */
    public String getMoneyGames() {
        return this.getStringAttribute(MONEY_GAMES, "");
    }

    /**
     * get danh sách mức cược trong bàn
     *
     * @param groupId
     * @return
     */
    public String getListBet(String groupId) {
        return this.getStringAttribute(groupId, "");
    }

    /**
     * Thời gian limit de kick user ra khỏi game nếu không thao tác gì
     *
     * @return
     */
    public int getIdleTimeLimit() {
        return getIntAttribute(IDLE_TIME);
    }

    /**
     * Số lần tiền tối thiểu join bàn
     *
     * @param nameLobby
     * @return
     */
    public int getMinJoinGame(String nameLobby) {
        return getIntAttribute(GAME_MIN_JOIN_NAME_LOBBY + nameLobby, 20);
    }

    /**
     * Lấy ra số lần đặt cược max cho những game đặt cược
     *
     * @param nameLobby
     * @return
     */
    public int getMaxBetGame(String nameLobby) {
        return getIntAttribute(MAX_BET_NAME_LOBBY + nameLobby, 0);
    }

    /**
     * Số lương user tối đa cho 1 bàn
     *
     * @param nameLobby
     * @return
     */
    public int getNoPlayer(String nameLobby) {
        return getIntAttribute(NO_PLAYER_NAME_LOBBY + nameLobby, 4);
    }

    /**
     * Thời gian chờ bắt đầu ván
     *
     * @param nameLobby
     * @return
     */
    public int getWaitingTime(String nameLobby) {
        return getIntAttribute(WAITING_TIME_NAME_LOBBY + nameLobby, 20);
    }

    /**
     * Thời gian 1 lượt
     *
     * @param nameLobby
     * @return
     */
    public int getPlayingTime(String nameLobby) {
        return getIntAttribute(PLAYING_TIME_NAME_LOBBY + nameLobby, 20);
    }

    /**
     * Thời gian show kết quả trong game
     *
     * @param nameLobby
     * @return
     */
    public int getResultTime(String nameLobby) {
        return getIntAttribute(RESULT_TIME_NAME_LOBBY + nameLobby, 10);
    }

    /**
     * lấy tỉ lệ phạt rời bàn theo từng game, dạng json gui ve cho client
     *
     * @param nameLobby
     * @return
     */
    public int getPennalizeFactor(String nameLobby) {
        return getIntAttribute(PENNALIZE_LEAVER_NAME_LOBBY + nameLobby, 10);
    }

    /**
     * Số lần tiền tối thiểu owner join vào
     *
     * @param nameLobby
     * @return
     */
    public int getMinJoinOwner(String nameLobby) {
        return getIntAttribute(MIN_JOIN_OWNER_NAME_LOBBY + nameLobby, 20);
    }

    /**
     * thuế của từng game
     *
     * @param nameLobby
     * @return
     */
    public int getTax(String nameLobby) {
        return getIntAttribute(TAX_GAME + nameLobby, 5);
    }

    /**
     * lấy ra thứ tự uu tiên của game
     *
     * @param nameLobby
     * @return
     */
    public int getPriority(String nameLobby) {
        return getIntAttribute(PRIORITY_GAME + nameLobby, 0);
    }

    /**
     *
     * @param lobbyName
     * @return
     */
    public int getDefaultMoneyMultiplier(String lobbyName) {
        return getIntAttribute(DEFAULT_MONEY_MULTIPLIER + lobbyName, 30);
    }

    /**
     * Số lần mức tiền tối thiểu mua tẩy vào bàn khi auto join
     *
     * @return
     */
    public int getMinMoneyTimeAutoJoin() {
        return getIntAttribute(MIN_MONEY_TIME_AUTO_JOIN, 5);
    }

    /**
     * Config bảo trì game theo id service
     *
     * @param idService
     * @return
     */
    public boolean isMaintainGame(int idService) {
        return getBooleanAttribute(MAINTAIN_GAME + idService);
    }

    /**
     * Config bảo trì game theo id service
     *
     * @return
     */
    public boolean isMaintainAllGame() {
        return getBooleanAttribute(MAINTAIN_ALL_GAME);
    }

    public String getMaintainInfor(Locale lo) {
        if (lo.equals(GlobalsUtil.VIETNAMESE_LOCALE)) {
            return getStringAttribute(MAINTAIN_INFOR, "Bảo trì");
        }
        return getStringAttribute(MAINTAIN_INFOR_EN, "Maintain");
    }

    /**
     * Lấy thông tin tournament từ file config
     *
     * @param serviceId
     * @return
     */
    public String getInforTournamnet(byte serviceId) {
        return getStringAttribute(GAME_TOURNAMNET + String.valueOf(serviceId), "250,1;25,5;10,10;6,50;4,150;2,284");
    }

    /**
     * Đếm số bàn thắng tối đa trong tournament
     *
     * @param serviceId
     * @return
     */
    public int getWinning(int serviceId) {
        return getIntAttribute(GAME_WINNING + String.valueOf(serviceId), 0);
    }

    /**
     * Thời gian chờ để xoay vòng trúng thưởng
     *
     * @param serviceId
     * @return
     */
    public int getRotateTime(int serviceId) {
        return getIntAttribute(ROTATE_TIME + String.valueOf(serviceId), 10);
    }
 
    /**
     * Số lần không action tối đa trong game
     *
     * @return
     */
    public int getMaxNoActionTime() {
        return this.getIntAttribute(MAX_NO_ACTION_TIME, 1);
    }
    
    /**
     * mức tiền thắng tối thiểu để tính thuế được config
     * @return 
     */
    public double getTaxThreshold() {
        return getDoubleAttribute(TAX_THRESHOLD, 0.00);
    }
    
    public int getLeaveRoomDelayTime() {
        return this.getIntAttribute(LEAVE_ROOM_DELAY_TIME, 5);
    }
    
    /**
     * Lấy ra danh sách game tournamnet
     *
     * @return
     */
    public List<String> getTournamentNameGames() {
        if (tournamentNames == null) {
            tournamentNames = new ArrayList<>();
            String stringValue = getStringAttribute(TOURNAMNET_NAME_GAMES, "");
            String[] arrayvalue = stringValue.split(",");
            tournamentNames.addAll(Arrays.asList(arrayvalue));
        }
        return tournamentNames;
    }

}
