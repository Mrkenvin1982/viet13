/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.vn.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.hazelcast.config.Config;
import com.hazelcast.config.FileSystemXmlConfig;
import com.hazelcast.config.MapConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.core.Member;
import game.vn.common.config.RoomConfig;
import game.vn.common.config.SFSConfig;
import game.vn.common.config.ServerConfig;
import game.vn.common.constant.MoneyContants;
import game.vn.common.lib.hazelcast.Board;
import game.vn.common.lib.hazelcast.PlayingBoardManager;
import game.vn.common.lib.hazelcast.UserInfor;
import game.vn.common.lib.hazelcast.UserLoginToken;
import game.vn.common.lib.hazelcast.UserState;
import game.vn.common.object.ServerInfor;
import java.io.FileNotFoundException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author binhnt
 */
public class HazelcastUtil {

    private static final String SERVER_NAME = "ServerName";
    private static final String SERVER_IP = "ServerIp";
    private static final String SERVER_IP_WS = "ServerIpWs";
    private static final String SERVER_PORT = "ServerPort";
    private static final String SERVER_TYPE = "ServerType";
    private static final String SERVER_PORT_WS = "ServerPortWs";
    private static final String SERVER_ZONE = "ServerZone";
    private static final String SERVER_MERCHANT = "ServerMerchant";
    
    //thong tin lien quan server login
    private static final String ACTIVE_USER_LIST = "ActiveUserList";
    //Thông tin của của user trong (money, point,...)
    public static final String USER_STATE_LIST = "UserStateList";
    private static final String USER_LOGIN_TOKEN_LIST = "UserLoginTokenList";
    private static final String LIST_BOARD = "BoardList";
    private static final String LIST_BOARD_WAITING = "BoardListWaiting";
    //Thong tin van dang choi
    private static final String PLAYING_BOARD_OF_USER = "PlayingBoardOfUser";
    private static final String TOURNAMENT_SPIN_GO ="TournamentSpinGo";

    private static HazelcastInstance instance;
    public static int serverType;

    // create HazelcastInstance instance with config 
    static {
        try {
            instance = Hazelcast.newHazelcastInstance(new FileSystemXmlConfig("conf/hazelcast.xml"));
        } catch (FileNotFoundException e) {
        }
    }

    public static void initBoardMap() {
        Config config = instance.getConfig();
        initWaitingBoardMap(config, MoneyContants.POINT);
        initWaitingBoardMap(config, MoneyContants.MONEY);
        initTournamentBoardMap(config);
    }

    public static IMap<String, UserState> getUserStates(){
        return  instance.getMap(USER_STATE_LIST);
    }
    
    /**
     * Khi start server smartfox Sẽ remove tất cả các board của server đó trên
     * hazelcast theo ip
     *
     * @param ip
     */
    public static void removeAllBoardFromIP(String ip) {
        if (ip == null || ip.isEmpty()) {
            return;
        }
        //point game
        String gamesconfig = RoomConfig.getInstance().getPointGames();

        if (!gamesconfig.isEmpty()) {
            JsonObject json = GsonUtil.parse(gamesconfig).getAsJsonObject();
            if (json != null) {
                for (Map.Entry<String, JsonElement> entry : json.entrySet()) {
                    removeWaitingBoardInLobby(ip, entry.getKey(), MoneyContants.POINT);
                }
            }
        }
        
        //money
        gamesconfig=RoomConfig.getInstance().getMoneyGames();
        if (!gamesconfig.isEmpty()) {
            JsonObject json = GsonUtil.parse(gamesconfig).getAsJsonObject();
            if (json != null) {
                for (Map.Entry<String, JsonElement> entry : json.entrySet()) {
                    removeWaitingBoardInLobby(ip, entry.getKey(), MoneyContants.MONEY);
                }
            }
        }

        //board infor
        IMap<String, Board> boardList= instance.getMap(LIST_BOARD);
        Iterator<String> itBoard = boardList.keySet().iterator();
        String keyBoard;
        while (itBoard.hasNext()) {
            keyBoard = itBoard.next();
            if(ip.equals(boardList.get(keyBoard).getIp())) {
                boardList.lock(keyBoard);
                boardList.remove(keyBoard);
                boardList.unlock(keyBoard);
            }
        } 
    }
    
    private static void initWaitingBoardMap(Config hazelcastCfg, int moneyType) {
         //point game
        String gamesconfig = RoomConfig.getInstance().getPointGames();
        if(moneyType == MoneyContants.MONEY){
            gamesconfig=RoomConfig.getInstance().getMoneyGames();
        }
        if (!gamesconfig.isEmpty()) {
            JsonObject json = GsonUtil.parse(gamesconfig).getAsJsonObject();
            if (json != null) {
                for (Map.Entry<String, JsonElement> entry : json.entrySet()) {
                    initWaitingBoardMap(hazelcastCfg, entry.getKey(), moneyType);
                }
            }
        }
    }

    private static void initWaitingBoardMap(Config hazelcastCfg, String lobby, int moneyType) {
        byte serviceId = Utils.getServiceId(lobby);
        String[] listBet = RoomConfig.getInstance().getListBet(lobby).split(",");
        for (String bet : listBet) {
            if(bet.isEmpty()){
                continue;
            }
            initWaitingBoardMap(serviceId,Double.valueOf(bet),moneyType, hazelcastCfg);
        }
        
        //init board waiting không theo mức cược
        MapConfig boardWaitingStateMapConfig = new MapConfig();
        boardWaitingStateMapConfig.setBackupCount(0);
        boardWaitingStateMapConfig.setStatisticsEnabled(false);
        boardWaitingStateMapConfig.setName(LIST_BOARD_WAITING + "_" + serviceId+"_"+moneyType);
        hazelcastCfg.addMapConfig(boardWaitingStateMapConfig);
    }
    
    private static void initWaitingBoardMap(byte serviceId, double betMoney, int moneyType, Config hazelcastCfg) {
        MapConfig boardWaitingStateMapConfig = new MapConfig();
        boardWaitingStateMapConfig.setBackupCount(0);
        boardWaitingStateMapConfig.setStatisticsEnabled(false);
        boardWaitingStateMapConfig.setName(LIST_BOARD_WAITING + "_" + serviceId+"_"+moneyType+ "_" + betMoney);
        hazelcastCfg.addMapConfig(boardWaitingStateMapConfig);
    }
    
    /**
     * lấy số bàn trong 1 game theo loại tiền
     * @param serviceId
     * @param moneyType
     * @return 
     */
    public static int getBoardCount(int serviceId, byte moneyType) {
        return instance.getMap(LIST_BOARD_WAITING + "_" + serviceId + "_" + moneyType).size();
    }

    /**
     * lock key of IMap ACTIVE_USER_LIST You get a lock whether the key is
     * present in the map or not.
     *
     * @param userId
     */
    public static void lockUser(String userId) {
        if (instance.getMap(ACTIVE_USER_LIST) != null){
            instance.getMap(ACTIVE_USER_LIST).lock(userId,15,TimeUnit.SECONDS);
        }
    }
    
    
    /**
     * unlock key of IMap ACTIVE_USER_LIST
     *
     * @param userId
     */
    public static void unlockUser(String userId) {
        if(instance.getMap(ACTIVE_USER_LIST) == null){
            return;
        }
        
        if(!instance.getMap(ACTIVE_USER_LIST).isLocked(userId)){
            return ;
        }
        instance.getMap(ACTIVE_USER_LIST).unlock(userId); 
    }
    
    /**
     * 
     * @param userId 
     */
    public static void lockUserState(String userId) {
        if (instance.getMap(USER_STATE_LIST) != null){
            instance.getMap(USER_STATE_LIST).lock(userId, 10, TimeUnit.SECONDS);
        }  
    }
    
    /**
     * 
     * @param userId 
     */
    public static void unlockUserState(String userId) {
        if(instance.getMap(USER_STATE_LIST) == null){
            return;
        }
        
        if(!instance.getMap(USER_STATE_LIST).isLocked(userId)){
            return;
        }
        instance.getMap(USER_STATE_LIST).unlock(userId);
    }

    /**
     * get server info: name, ip & port
     *
     * @return ServerInfor
     */
    private static ServerInfor getServerInfo() {
        String serverName = SFSConfig.getServerName();
        String ip = ServerConfig.getInstance().getIP();
        String ipWS = ServerConfig.getInstance().getIPWS();
        int port = SFSConfig.getPort();
        int portWS = SFSConfig.getWsPort();
        String zone = SFSConfig.getZoneName();

        return new ServerInfor(serverName, ip, ipWS, port, portWS, zone);
    }

    /**
     * khi login, add user stat vao list để control
     *
     * @param userState
     * @return
     */
    public static boolean addUserState(UserState userState) {
        if (userState == null) {
            return false;
        }

        instance.getMap(USER_STATE_LIST).put(userState.getUserId(), userState);
        return true;
    }

    /**
     * remove user info from USER_STATE_LIST
     *
     * @param userState
     * @return true/false
     */
    public static boolean removeUserState(UserState userState) {
        if (userState == null) {
            return false;
        }

        return removeUserState(userState.getUserId());
    }

    /**
     * remove user info from USER_STATE_LIST
     *
     * @param userId
     * @return true/false
     */
    public static boolean removeUserState(String userId) {
        IMap<String, UserState> activeList = instance.getMap(USER_STATE_LIST);
        UserState currentInfo = (UserState) activeList.get(userId);
        if (currentInfo != null && userId.equals(currentInfo.getUserId())) {
            activeList.lock(userId);
            activeList.remove(userId);
            activeList.unlock(userId);
        }

        return true;
    }

    /**
     * update user info to USER_STATE_LIST
     *
     * @param userState
     * @return true/false
     */
    public static boolean updateUserState(UserState userState) {
        if (userState == null) {
            return false;
        }
        IMap<String, UserState> activeList = instance.getMap(USER_STATE_LIST);
        activeList.put(userState.getUserId(), userState);
        return true;
    }

    /**
     * get user stat from USER_STATE_LIST
     *
     * @param userId
     * @return userStat
     */
    public static UserState getUserState(String userId) {
        IMap<String, UserState> activeList = instance.getMap(USER_STATE_LIST);
        UserState userState = (UserState) activeList.get(userId);
        return userState;
    }
    /**
     * Add thông tin board
     * @param board
     * @return 
     */
    private static boolean addBoardInfor(Board board){
        if (board == null) {
            return false;
        }
        instance.getMap(LIST_BOARD).put(board.getName(), board);
        return true;
        
    }
    /**
     * remove board
     * @param boardName 
     */
    public static void removeBoardInfor(String boardName){
        IMap<String, Board> boardList = instance.getMap(LIST_BOARD);
        Board boardInfo = (Board) boardList.get(boardName);
        if (boardInfo != null) {
            boardList.lock(boardName);
            boardList.remove(boardName);
            boardList.unlock(boardName);
        }
    }
    
    /**
     * get thông tin board
     * @param boardName
     * @return 
     */
    public static Board getBoardInfor(String boardName){
        IMap<String, Board> boardList = instance.getMap(LIST_BOARD);
        return boardList.get(boardName);
    }
    
    /**
     * Add thông tin board waiting
     * @param board
     * @return 
     */
    public static boolean addBoardWaitingInfor(Board board){
        if (board == null) {
            return false;
        }
        instance.getMap(LIST_BOARD_WAITING + "_" + board.getServiceId()+"_"+board.getMoneyType() + "_" + board.getBetMoney()).put(board.getName(), board);
        instance.getMap(LIST_BOARD_WAITING + "_" + board.getServiceId()+"_"+board.getMoneyType()).put(board.getName(), board);
        HazelcastUtil.addBoardInfor(board);
        return true;

    }
    /**
     * remove board
     * @param board 
     */
    public static void removeBoardWaitingInfor(Board board){
        if(board==null){
            return;
        }
        IMap<String, Board> boardList = instance.getMap(LIST_BOARD_WAITING + "_" + board.getServiceId() +"_"+board.getMoneyType() + "_" + board.getBetMoney());
        Board boardInfo = boardList.get(board.getName());
        if (boardInfo != null) {
            boardList.lock(board.getName());
            boardList.remove(board.getName());
            boardList.unlock(board.getName());
        }
        
        boardList = instance.getMap(LIST_BOARD_WAITING + "_" + board.getServiceId() +"_"+board.getMoneyType());
        boardInfo = boardList.get(board.getName());
        if (boardInfo != null) {
            boardList.lock(board.getName());
            boardList.remove(board.getName());
            boardList.unlock(board.getName());
        }
    }
    
    /**
     * Check if the server is active
     *
     * @param serverName
     * @return true/false
     */
    private static boolean isActiveServer(String serverName) {
        if (serverName == null) {
            return false;
        }

        Set<Member> members = instance.getCluster().getMembers();
        if (members == null || members.isEmpty()) {
            return false;
        }

        String name;
        for (Member member : members) {
            name = member.getStringAttribute(SERVER_NAME);
            if (serverName.equals(name)) {
                return true;
            }
        }

        return false;
    }
    
    /**
     * add server info into member attribute
     *
     * @param serverInfo
     * @return true/false
     */
    private static boolean addServerInfo(ServerInfor serverInfo) {
        if (serverInfo == null) {
            return false;
        }

        // add server name
        instance.getCluster().getLocalMember().setStringAttribute(SERVER_NAME, serverInfo.getName());
        instance.getCluster().getLocalMember().setStringAttribute(SERVER_IP, serverInfo.getIp());
        instance.getCluster().getLocalMember().setStringAttribute(SERVER_IP_WS, serverInfo.getIpWS());
        instance.getCluster().getLocalMember().setIntAttribute(SERVER_PORT, serverInfo.getPort());
        instance.getCluster().getLocalMember().setByteAttribute(SERVER_TYPE, serverInfo.getType());
        instance.getCluster().getLocalMember().setIntAttribute(SERVER_PORT_WS, serverInfo.getPortWS());
        instance.getCluster().getLocalMember().setStringAttribute(SERVER_ZONE, serverInfo.getZone());
        instance.getCluster().getLocalMember().setStringAttribute(SERVER_MERCHANT, ServerConfig.getInstance().getMerchantId());
        return true;
    }
    
    /**
     * remove all user info, related with current server and add server info
     * into member attribute
     *
     * @param serverType
     * @return true/false
     */
    public static boolean addServerInfo(byte serverType) {
        HazelcastUtil.serverType = serverType;
        ServerInfor serverInfo = getServerInfo();
        if (serverInfo == null) {
            return false;
        }
        serverInfo.setType(serverType);

        removeAllRelatedUser(serverInfo.getName());
        return addServerInfo(serverInfo);
    }
    
    private static void removeAllRelatedUser(String serverName) {
        if (serverName == null) {
            return;
        }

        IMap<String, UserInfor> activeList = instance.getMap(ACTIVE_USER_LIST);
        Iterator<String> it = activeList.keySet().iterator();
        String key;
        while (it.hasNext()) {
            key = it.next();
            if (serverName.equals(activeList.get(key).getServerName())) {
                activeList.lock(key);
                activeList.remove(key);
                activeList.unlock(key);
            }
        }
    }
    
    /**
     *  remove user khỏi user state
     */
    public static void removeAllUserState() {
        IMap<String, UserInfor> activeList = instance.getMap(USER_STATE_LIST);
        Iterator<String> it = activeList.keySet().iterator();
        String key;
        while (it.hasNext()) {
            key = it.next();
            PlayingBoardManager playingBoard = HazelcastUtil.getPlayingBoard(key);
            if (playingBoard == null) {
                activeList.lock(key);
                activeList.remove(key);
                activeList.unlock(key);
            }
        }
    }
    
    /**
     * check if user is online
     *
     * @param userId
     * @return true/false
     */
    public static boolean isOnlineUser(String userId) {
        if(!instance.getMap(ACTIVE_USER_LIST).containsKey(userId)) {
            return false;
        }

        UserInfor userInfo = (UserInfor) instance.getMap(ACTIVE_USER_LIST).get(userId);
        return userInfo != null && isActiveServer(userInfo.getServerName());
    }
    
    public static UserInfor getUserInfor(String userId){
        UserInfor userInfo = (UserInfor) instance.getMap(ACTIVE_USER_LIST).get(userId);
        return userInfo;
    }
    
    /**
     * Kiểm tra user chỉ có thể login trên 1 server login
     * - Khi thoát ra trên 1 server login hiện tại mới có thể login vào 1 server khác
     * - Trong trường hợp đang trong bàn chơi thì phải bắt buộc quay lại server login đã login
     * @param userId
     * @return true/false
     */
    public static boolean isOnlineOtherLoginServer(String userId) {
        if(!instance.getMap(ACTIVE_USER_LIST).containsKey(userId)) {
            return false;
        }

        UserInfor userInfo = (UserInfor) instance.getMap(ACTIVE_USER_LIST).get(userId);
        if(userInfo == null){
            return false;
        }
        
        return !userInfo.getServerName().equals(SFSConfig.getServerName());
    }

    /**
     * Kiem tra phải login trên cùng 1 device
     * @param userId
     * @param deviceId
     * @return 
     */
    public static boolean isExitDevice(String userId, String deviceId) {
        if (!instance.getMap(ACTIVE_USER_LIST).containsKey(userId)) {
            return false;
        }

        UserInfor userInfo = (UserInfor) instance.getMap(ACTIVE_USER_LIST).get(userId);
        return userInfo != null && !deviceId.equals(userInfo.getIdDevice());
    }
    

    /**
     * add user info into ACTIVE_USER_LIST
     *
     * @param userInfo
     * @return true/false
     */
    public static boolean addUserInfo(UserInfor userInfo) {
        if (userInfo == null) {
            return false;
        }
        instance.getMap(ACTIVE_USER_LIST).put(userInfo.getUserId(), userInfo);
        return true;
    }

    /**
     * remove user info into ACTIVE_USER_LIST
     *
     * @param userInfo
     * @return true/false
     */
    public static boolean removeUserInfo(UserInfor userInfo) {
        if (userInfo == null) {
            return false;
        }

        return removeUserInfo(userInfo.getUserId());
    }

    /**
     * remove user info into ACTIVE_USER_LIST
     *
     * @param userId
     * @return true/false
     */
    public static boolean removeUserInfo(String userId) {
        IMap<String, UserInfor> activeList = instance.getMap(ACTIVE_USER_LIST);
        UserInfor currentInfo = (UserInfor) activeList.get(userId);
        if (currentInfo != null && userId.equals(currentInfo.getUserId())) {
            activeList.lock(userId);
            activeList.remove(userId);
            activeList.unlock(userId);
        }

        return true;
    }
    
    /**
     * Check if it's current user.
     *
     * @param serverName
     * @return true/false
     */
    private static boolean isCurrentServer(String serverName) {
        String name = SFSConfig.getServerName();
        return name != null && name.equals(serverName);
    }
    
    /**
     * khi login, add user để chuyển token cho cac server game handle
     *
     * @param userLoginToken
     * @return
     */
    public static boolean addUserLoginToken(UserLoginToken userLoginToken) {
        if (userLoginToken == null) {
            return false;
        }

        instance.getMap(USER_LOGIN_TOKEN_LIST).put(userLoginToken.getToken(), userLoginToken);
        return true;
    }

    /**
     * remove user info from USER_LOGIN_TOKEN_LIST
     *
     * @param userLoginToken
     * @return true/false
     */
    public static boolean removeUserLoginToken(UserLoginToken userLoginToken) {
        if (userLoginToken == null) {
            return false;
        }

        return removeUserLoginToken(userLoginToken.getToken());
    }

    public static boolean removeUserLoginToken(String userLoignTokenStr) {
        IMap<String, UserLoginToken> tokenList = instance.getMap(USER_LOGIN_TOKEN_LIST);
        UserLoginToken userToken = (UserLoginToken) tokenList.get(userLoignTokenStr);
        if (userToken != null && userLoignTokenStr.equals(userToken.getToken())) {
            tokenList.lock(userLoignTokenStr);
            tokenList.remove(userLoignTokenStr);
            tokenList.unlock(userLoignTokenStr);
        }
        return true;
    }
    
    public static UserLoginToken getUserTokenData(String userLoignTokenStr) {
        IMap<String, UserLoginToken> activeList = instance.getMap(USER_LOGIN_TOKEN_LIST);
        UserLoginToken userState = (UserLoginToken) activeList.get(userLoignTokenStr);
        return userState;
    }

    /**
     * Remove tất cả bàn them ip và name bàn
     * @param ip
     * @param name 
     */
    private static void removeWaitingBoard(String ip, String name){
       //board watting
        IMap<String, Board> boardWatting = instance.getMap(name);
        Iterator<String> it = boardWatting.keySet().iterator();
        String key;
        while (it.hasNext()) {
            key = it.next();
            if(ip.equals(boardWatting.get(key).getIp())) {
                boardWatting.lock(key);
                boardWatting.remove(key);
                boardWatting.unlock(key);
            }
        }  
    }
    /**
     * Remove tất cả bàn trên hazelcast theo ip và lobby
     * @param ip
     * @param lobby 
     */
    private static void removeWaitingBoardInLobby(String ip, String lobby, int moneyType) {
        byte serviceId = Utils.getServiceId(lobby);
        String[] listBet = RoomConfig.getInstance().getListBet(lobby).split(",");
        for (String bet : listBet) {
            if(bet.isEmpty()){
                continue;
            }
            String name= LIST_BOARD_WAITING + "_" + serviceId+"_"+moneyType+ "_" + bet;
            removeWaitingBoard(ip,name);
            name= LIST_BOARD_WAITING + "_" + serviceId+"_"+moneyType;
            removeWaitingBoard(ip,name);
        }
    }
    /**
     * Khi user join board sẽ add vào
     *
     * @param playingBoard
     * @return
     */
    public static boolean addPlayingBoard(PlayingBoardManager playingBoard) {
        if (playingBoard == null) {
            return false;
        }

        instance.getMap(PLAYING_BOARD_OF_USER).put(playingBoard.getUserId(), playingBoard);
        return true;
    }

    /**
     * 
     *
     * @param playingBoard
     * @return true/false
     */
    public static boolean removePlayingBoard(PlayingBoardManager playingBoard) {
        if (playingBoard == null) {
            return false;
        }

        return removePlayingBoard(playingBoard.getUserId());
    }

    /**
     * 
     *
     * @param userId
     * @return true/false
     */
    public static boolean removePlayingBoard(String userId) {
        IMap<String, PlayingBoardManager> activeList = instance.getMap(PLAYING_BOARD_OF_USER);
        PlayingBoardManager currentInfo = (PlayingBoardManager) activeList.get(userId);
        if (currentInfo != null && userId.equals(currentInfo.getUserId())) {
            activeList.lock(userId);
            activeList.remove(userId);
            activeList.unlock(userId);
        }

        return true;
    }

    /**
     * update user info to USER_STATE_LIST
     *
     * @param playingBoard
     * @return true/false
     */
    public static boolean updatePlayingBoard(PlayingBoardManager playingBoard) {
        if (playingBoard == null) {
            return false;
        }
        IMap<String, PlayingBoardManager> activeList = instance.getMap(PLAYING_BOARD_OF_USER);
        activeList.put(playingBoard.getUserId(), playingBoard);
        return true;
    }

    /**
     * get user stat from USER_STATE_LIST
     *
     * @param userId
     * @return userStat
     */
    public static PlayingBoardManager getPlayingBoard(String userId) {
        IMap<String, PlayingBoardManager> activeList = instance.getMap(PLAYING_BOARD_OF_USER);
        PlayingBoardManager playingBoard = (PlayingBoardManager) activeList.get(userId);
        return playingBoard;
    }
    /**
     * 
     * @param userId 
     */
    public static void lockPlayingBoard(String userId) {
//        if(instance.getMap(PLAYING_BOARD_OF_USER)!=null){
//            instance.getMap(PLAYING_BOARD_OF_USER).lock(userId);
//        }
    }
    
    /**
     * 
     * @param userId 
     */
    public static void unlockPlayingboard(String userId){
//        if(instance.getMap(PLAYING_BOARD_OF_USER)!=null){
//           instance.getMap(PLAYING_BOARD_OF_USER).unlock(userId); 
//        } 
    }
    
    private static void initTournamentBoardMap(Config hazelcastCfg) {
        List<String> tourLobbyNames = RoomConfig.getInstance().getTournamentNameGames();
        for (String lobbyName : tourLobbyNames) {
            initTournamentByService(lobbyName, hazelcastCfg);
        }
    }

    /**
     * Init thông tin tournament
     * @param lobby
     * @param hazelcastCfg 
     */
    private static void initTournamentByService(String lobby, Config hazelcastCfg){
        byte serviceId = Utils.getServiceId(lobby);
        MapConfig boardWaitingStateMapConfig = new MapConfig();
        boardWaitingStateMapConfig.setBackupCount(0);
        boardWaitingStateMapConfig.setStatisticsEnabled(false);
        boardWaitingStateMapConfig.setName(TOURNAMENT_SPIN_GO + "_" + serviceId);
        hazelcastCfg.addMapConfig(boardWaitingStateMapConfig);
    }
    
    /**
     * Danh sách tổng tất cả các giải của 1 chu kỳ tournamnet 
     * theo từng mức cược(key:mức cược, value: thông tin giải)
     * @param serviceId 
     * @param sumMoney 
     */
    public static void putBonusInfor(byte serviceId, double sumMoney) {
        lockTourByBetBoard(serviceId);
        if (!instance.getMap(TOURNAMENT_SPIN_GO + "_" + serviceId).containsKey(serviceId)) {
            instance.getMap(TOURNAMENT_SPIN_GO + "_" + serviceId).put(serviceId, sumMoney);
        }
        unlockTourByBetBoard(serviceId);
    }
    
    /**
     * Update tổng tiền thu được theo  mức cược
     * @param serviceId
     * @param sumMoney 
     */
    public static void updateBonusInfor(byte serviceId, double sumMoney){
        instance.getMap(TOURNAMENT_SPIN_GO + "_" + serviceId).put(serviceId, sumMoney);
    }
    
    public static double getSumMoneyByBetBoard(byte serviceId){
        IMap<Byte, Double> bonus = instance.getMap(TOURNAMENT_SPIN_GO + "_" + serviceId);
        if(!bonus.containsKey(serviceId)){
            return 0;
        }
        return (double) instance.getMap(TOURNAMENT_SPIN_GO + "_" + serviceId).get(serviceId);
    }
    
    /**
     * @param serviceId 
     */
    public static void lockTourByBetBoard(byte serviceId){
        if(instance.getMap(TOURNAMENT_SPIN_GO + "_" + serviceId)!=null){
            instance.getMap(TOURNAMENT_SPIN_GO + "_" + serviceId).lock(serviceId, 10, TimeUnit.SECONDS);
        }
    }
    
    public static void unlockTourByBetBoard(byte serviceId){
        if(instance.getMap(TOURNAMENT_SPIN_GO + "_" + serviceId) == null ){
            return ;
        }
        if(!instance.getMap(TOURNAMENT_SPIN_GO + "_" + serviceId).isLocked(serviceId)){
            return ;
        }
        instance.getMap(TOURNAMENT_SPIN_GO + "_" + serviceId).unlock(serviceId);
    }
}
