/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.vn.common.lib.event;

/**
 *
 * @author hanv
 */
public class EventData {

    /**
     * @return the lang
     */
    public String getLang() {
        return lang;
    }

    /**
     * @param lang the lang to set
     */
    public void setLang(String lang) {
        this.lang = lang;
    }

    /**
     * @param cmd the cmd to set
     */
    public void setCmd(int cmd) {
        this.cmd = cmd;
    }
    public static final int EVENT_CMD_LIST_PROMOTION = 0;
    public static final int EVENT_CMD_ACCEPT = 1;
    public static final int EVENT_CMD_GIFT_HISTORY = 2;
    public static final int EVENT_CMD_GET_LUCKY_SPIN_INFO = 3;
    public static final int EVENT_CMD_SPIN = 4;
    public static final int EVENT_CMD_SUBMIT = 5;
    public static final int EVENT_CMD_NOTIFY_CLIENT = 6;
    public static final int EVENT_CMD_SUBMIT_GIFTCODE = 7;
    public static final int EVENT_CMD_GET_CATEGORY_LIST = 8;
    public static final int EVENT_CMD_QUEST_LIST_BY_CATEGORY_ID = 9;
    public static final int EVENT_CMD_GET_LIST_RANKING = 10;
    public static final int EVENT_CMD_GET_LIST_RANKING_DATA = 11;
    public static final int EVENT_CMD_GET_SUBMIT_BOX_INFO = 12;
    public static final int EVENT_CMD_SUBMIT_INFO = 14;

    private int cmd;
    private final String userId;
    private final String username;
    private int serverId;
    private String lang;
    private String data;


    public EventData(int cmd, String userId, String username) {
        this.userId = userId;
        this.username = username;
        this.cmd = cmd;
    }

    public int getCmd() {
        return cmd;
    }

    public String getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public int getServerId() {
        return serverId;
    }

    public void setServerId(int serverId) {
        this.serverId = serverId;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }
}
