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
public class MauBinhCommand {

    public static final int INTERFACE_ERROR = 101;
    public static final int TABLE_INFO = 103;
    public static final int AUTO_ARRANGE = 104;
    public static final int FINISH = 105;
    //cmd gui bai cho truong hop time out, sv tu dong binh
    public static final int SEND_CARDS = 106;
    //cmd thông tin sập hầm trong ván
    public static final int DEC_SAP_HAM = 107;
    //thông tin win của user khi kết thúc ván
    public static final int USER_MONEY_INFO = 108;
    public static final int SORT_BY_ORDER = 109;
    public static final int SORT_BY_TYPE = 110;
    
    /////////////KEY///////////////////
    public static final String LIMIT_TIME = "lt";
    public static final String LIMIT_TIME_TYPE = "ltt";
    public static final String REST_TIME = "rt";
    public static final String MAUBINH_TYPE = "mbt";
    public static final String ERR_COMMAND = "ercmd";
    public static final String ERR_MESSAGE = "ermsg";
    public static final String GAME_STATUS = "gs";
    public static final String AUTO_START_TIME = "ast";
    public static final String WIN_MONEY = "wm";
    public static final String WIN_CHI = "wc";
    public static final String WIN_CHI_MAUBINH = "wcmb";
    public static final String WIN_CHI_01 = "wc01";
    public static final String WIN_CHI_02 = "wc02";
    public static final String WIN_CHI_03 = "wc03";
    public static final String WIN_CHI_ACE = "wca";
    public static final String MULTI_K = "mtk";
    public static final String IS_FINISHED = "isFinished";
}
