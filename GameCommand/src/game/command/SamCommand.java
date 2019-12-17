/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.command;

/**
 *
 * @author tuanp
 */
public class SamCommand {
    ///////////COMMAND/////////////////
    /**
     * Command báo Xâm
     */
    public static final byte XAM = 100;
    /**
     * Command báo Hủy Xâm
     */
    public static final byte SKIP_XAM = 101;
    /**
     * Command tới trắng
     */
    public static final byte FORCE_FINISH = 102;
    /**
     * Command chặn Xâm
     */
    public static final byte CHAN_XAM = 103;
    /**
     * Move error command
     */
    public static final byte MOVE_ERROR = 104;
    /**
     * báo khi user chỉ còn 1 lá bài
     */
    public static final byte HAVE_ONE_CARD = 105;
    /**
     * show bài thằng đền bài
     */
    public static final byte DEN_BAI=106;
    /**
     * Thời gian báo sâm
     */
    public static final byte XAM_TIME=107;
    
    //////////////KEY/////////////////
    public static final String IS_CLEAR_CARD="sClCrd";
    public static final String USER_ID_SAM="sIdUX";
    public static final String PENALTY="sPenalty";
    public static final String TIME_LIMIT="sTLimit";
    public static final String TIME_REMAINING="sTRemain";
    public static final String USER_ID_MOVED="sIdMoved";
    public static final String IS_BI_CHAT="sIsBiChat";
}
