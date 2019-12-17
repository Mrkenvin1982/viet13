/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.vn.common.lib.hazelcast;

import com.google.gson.JsonObject;
import game.vn.common.lib.contants.MoneyContants;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Các thông tin trạng thái của user trong game
 * @author hanv
 */
public class UserState implements Serializable {
    
    static final long serialVersionUID = 1l;
    /**
     * userid, primary key
     */
    private String userId;
    /**
     * tên hien thi trong game, trong ban
     */
    private String displayName;

    private String ip;
    /**
     * thời điểm login
     */
    private Date loginDate;
    /**
     * token login ban đầu để user chuyen server
     */
    private String loginToken;
    /**
     * loại tiền
     */
    private int moneyType=MoneyContants.POINT;

    /**
     * tiền mua tẩy tiền ảo
     */
    private double pointStack;
    /**
     * tiền mua tẩy tiền thật
     */
    private double moneyStack;
    
    //mức cược trong của board
    private double betBoard;
    
    /**
     * Luu danh sách server id user đang join
     */
    private List<Integer> listServerId = new ArrayList<>();
    
    private String currentLobbyName="";
    //trạng thái logout game
    private boolean isLogoutGame=false;
    private Locale locale;
    private int maxPlayer;
    private List<Integer> shuffleGames; // danh sách game đang chơi ở mode shuffle
    private String sessionId;
    private String email;
    private boolean isUpdateMoneySum = false;
    
    public UserState() {
        listServerId = new ArrayList<>();
        locale = new Locale("vi");
    }

    /**
     * @return the userId
     */
    public String getUserId() {
        return userId;
    }

    /**
     * @param userId the userId to set
     */
    public void setUserId(String userId) {
        this.userId = userId;
    }

    /**
     * @return the displayName
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * @param displayName the displayName to set
     */
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    /**
     * @return the ip
     */
    public String getIp() {
        return ip;
    }

    /**
     * @param ip the ip to set
     */
    public void setIp(String ip) {
        this.ip = ip;
    }

    /**
     * @return the loginDate
     */
    public Date getLoginDate() {
        return loginDate;
    }

    /**
     * @param loginDate the loginDate to set
     */
    public void setLoginDate(Date loginDate) {
        this.loginDate = loginDate;
    }

    /**
     * @return the listServerGameId
     */
    public List<Integer> getListServerId() {
        return listServerId;
    }

    /**
     * @param listServerGameId the listServerGameId to set
     */
    public void setListServerId(List<Integer> listServerGameId) {
        this.listServerId = listServerGameId;
    }
    
    public void addListServerId(int serverId){
        this.listServerId.add(serverId);
    }
    
    /**
     * Kiem tra user đang online server hiện tại
     *
     * @param serverId
     * @return
     */
    public boolean isOnlineCurrentServer(int serverId) {
        return listServerId.contains(serverId);
    }

    public void removeListServerId( int serverId) {
        listServerId.remove(Integer.valueOf(serverId));
    }

    /**
     * @return the loginToken
     */
    public String getLoginToken() {
        return loginToken;
    }

    /**
     * @param loginToken the loginToken to set
     */
    public void setLoginToken(String loginToken) {
        this.loginToken = loginToken;
    }

    public String getCurrentLobbyName() {
        return currentLobbyName;
    }

    public void setCurrentLobbyName(String currentLobbyName) {
        this.currentLobbyName = currentLobbyName;
    }

    public int getMoneyType() {
        return moneyType;
    }

    public void setMoneyType(int moneyType) {
        this.moneyType = moneyType;
    }

    public double getPointStack() {
        return pointStack;
    }

    public void setPointStack(double pointStack) {
        this.pointStack = pointStack;
    }

    public double getMoneyStack() {
        return moneyStack;
    }

    public void setMoneyStack(double moneyStack) {
        this.moneyStack = moneyStack;
    }

    public boolean isIsLogoutGame() {
        return isLogoutGame;
    }

    public void setIsLogoutGame(boolean isLogoutGame) {
        this.isLogoutGame = isLogoutGame;
    }

    public Locale getLocale() {
        return locale;
    }

    public void setLocale(Locale locale) {
        this.locale = locale;
    }

    public double getBetBoard() {
        return betBoard;
    }

    public void setBetBoard(double betBoard) {
        this.betBoard = betBoard;
    }

    public int getMaxPlayer() {
        return maxPlayer;
    }

    public void setMaxPlayer(int maxPlayer) {
        this.maxPlayer = maxPlayer;
    }

    public List<Integer> getShuffleGames() {
        return shuffleGames;
    }

    public void setShuffleGames(List<Integer> shuffleGames) {
        this.shuffleGames = shuffleGames;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isIsUpdateMoneySum() {
        return isUpdateMoneySum;
    }

    public void setIsUpdateMoneySum(boolean isUpdateMoneySum) {
        this.isUpdateMoneySum = isUpdateMoneySum;
    }

    @Override
    public String toString() {
        JsonObject obj = new JsonObject();
        obj.addProperty("userid", String.valueOf(userId));
        obj.addProperty("displayName", displayName);
        obj.addProperty("ip", ip);
        obj.addProperty("loginDate", loginDate.toGMTString());
        obj.addProperty("pointStack", pointStack);
        obj.addProperty("moneyStack", moneyStack);
        String listString = "";
        for (int s : listServerId) {
            listString += s + "\t";
        }
        obj.addProperty("listServerGameId", listString);

        return obj.toString();
    }
}
