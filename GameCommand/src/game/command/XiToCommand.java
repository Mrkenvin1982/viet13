/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.command;

/**
 * các command AND KEY riêng trong game xì tố
 * @author tuanp
 */
public class XiToCommand {
    //command
    public static final byte PRE_FLOP = 100;
    public static final byte FLOP = 101;
    public static final byte BUY_STACK = 102;
    public static final byte SIT = 103;
    public static final byte STAND_UP = 104;
    public static final byte BET = 105;
    public static final byte RAISE = 106;
    public static final byte CALL = 107;
    public static final byte FOLD = 108;
    public static final byte ALL_IN = 109;
    public static final byte CHECK = 110;
    public static final byte SET_MONEY = 111;
    public static final byte BET_MONEY_INFO = 112;
    public static final byte SHOW_ONE_CARD = 113;
    public static final byte STACK = 114;
    public static final byte NEXT_TURN = 115;
    public static final byte UPDATE_POT = 116;
    public static final byte RESULT = 117;
    public static final byte HAND_EVAL = 118;
    public static final byte VIEWER = 119;
    public static final byte CONTINUE = 120;
    public static final byte LAST_ACTION = 121;
    //key
    public static final String STACK_KEY= "xS";
    public static final String MIN_RAISE= "xMR";
    public static final String WIN_STACK= "xWS";
    public static final String TIME_LIMIT="xTL";
    public static final String XI_TO_ACTION_ARRAY="xAcArr";
    public static final String RESULT_CARDS_VALUE="xRCV";
    public static final String PRE_FLOG_TIME="xpreT";
    public static final String TOTAL_BET_STACK="xTbst";
    public static final String IS_PLAYING="xIspl";
    public static final String MAX_BET="xMxB";
    public static final String MIM_BET="xMnB";
    public static final String ACTION_KEY="xActn";
    public static final String BUY_STACK_KEY="xBStack";
}
