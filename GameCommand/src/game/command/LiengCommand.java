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
public class LiengCommand {
    public static final byte BET = 100;
    public static final byte CALL = 101;
    public static final byte FOLD = 102;
    public static final byte SIT = 103;
    public static final byte STAND_UP = 104;
    public static final byte SET_MONEY = 105;
    public static final byte DEAL_CARD = 106;
    public static final byte VIEWER = 107;
    public static final byte NEXT_TURN = 108;
    public static final byte BOARD_BET = 109;
    public static final byte RESULT = 110;
    public static final byte BOARD_INFOR = 111;
    public static final byte CHECK = 112;
    public static final byte SET_PASS = 113;
    public static final byte LAST_ACTION = 114;
    
    //key
    public static final String  TOTAL_BET_MONEY="lTBM";
    public static final String  ROUND_KEY="lRound";
    public static final String  IS_FOLDED="lIsFol";
    public static final String  IS_STARTED="lIsSt";
    public static final String  TIME_LIMIT="lTLm";
    public static final String  ARRAY_ACTION="lArrAc";
    public static final String  ARRAY_BET="lArrB";
    public static final String  BET_MONEY="lBetM";
    public static final String  CALL_MONEY="lCallM";
    public static final String  ACTION="lAc";
           
    
}
