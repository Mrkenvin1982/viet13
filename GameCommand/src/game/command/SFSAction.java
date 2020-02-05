/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.command;

/**
 *
 * @author hanv
 */
public class SFSAction {

    //////////ACTION IN GAME: value <=100////////////////////////////////  
    //tổng hợp các loại command  kieu int giua client -server sử dụng trong GAME
    //move card
    public static final int MOVE = 1;
    //bỏ lượt
    public static final int SKIP = 2;
    //đặt cược
    public static final int BET = 3;

    public static final int START_GAME = 4;
    //ket thuc van
    public static final int STOP_GAME = 5;
    //rời bàn
    public static final int LEAVE_GAME = 6;
    // ket qua tra ve
    public static final int RESULT = 7;
    //thông tin thời gian start ván
    public static final int BOARD_INFO = 8;
    //join board
    public static final int JOIN_BOARD = 9;
    //create new board
    public static final int CREATE_BOARD = 10;
    //bonus money
    public static final int BONUS_MONEY = 11;
    //gửi về thông báo thời gian start ván
    public static final int TIME_START_BOARD = 12;
    //reconnect đến ván
    public static final int ON_RETURN_GAME = 13;
    //thong tin ván dang playing
    public static final int PLAYING = 14;
    //xét bài
    public static final int CHECK_CARD = 15;
    //chơi nhanh
    public static final int QUICK_PLAY = 16;
    //chuyển lượt
    public static final int NEXT_TURN = 17;
    //auto buy-in
    public static final int AUTO_BUY_IN = 18;

    //Lịch sử ván chơi
    public static final int HISTORY_BOARD= 20;
    //mua tẩy trong game
    public static final int BUY_STACK_IN_GAME= 21;
    public static final int CONTINUE_GAME= 22;
    public static final int UPDATE_VARIABLE_USER = 23;
    public static final int ADD_PLAYER = 24;
    public static final int NO_MONEY = 25;
    public static final int START_GAME_VIEWER = 26;
    public static final int MESSAGE_IN_BOARD = 27;
    public static final int GET_REMAIN_TIME = 28;
    public static final int TOURNAMNET_INFOR = 29;
    public static final int ROTATE_TIME_TOURNAMENT = 30;
    public static final int RESULT_TOURNAMENT = 31;
    public static final int STATUS_WIN_LOSE = 32;
    public static final int SIT_OUT = 33;
    public static final int SIT_OUT_JOIN_BOARD = 34;
    public static final int STAND_UP = 35;
    public static final int REQUEST_BUY_STACK = 36;
    public static final int AUTO_MUCK_HAND = 37;
    public static final int GET_LAST_HAND = 38;
    public static final byte RAISE = 39;
    public static final byte CALL = 40;
    public static final byte FOLD = 41;
    public static final byte ALL_IN = 42;
    public static final byte CHECK = 43;
    public static final int SHUFFLE = 44;
    public static final int LEAVE_GAME_WHEN_PLAYING = 45;
    //bot request thông tin bài của user trong bàn
    public static final int BOT_REQUEST_INFOR_CARDS = 46;
    public static final int AUTO_LEAVE_GAME = 47;

    //////////ACTION IN CORE////////////////////////////////  
    //gửi về thông tin tất cả game khi user join game
    public static final int REQUEST_INFOR_ALL_GAME = 1;
    //hien thi câu thong báo
    public static final int MESSAGE_ERROR = 2;
    // cap nhat ten hien thi
    public static final int UPDATE_PROFILE = 3;
    public static final int CREATE_PIN_CODE = 4;
    public static final int VERIFY_PIN = 5;
    //check join vao zone thanh cong
    public static final int JOIN_ZONE_SUCCESS = 6;
    public static final int FORCE_ACTIVATE_PIN = 7;
    public static final int UPDATE_PIN_CODE = 8;
    public static final int VERIFY_PIN_ON_CHANGE = 9;
    
    public static final int PLAY_TAIXIU = 10;

    // ranking
    public static final int RANKING_GET_LEADER_BOARD_INFO = 11;

    // thông báo hệ thống
    public static final int SYSTEM_MESSAGE = 12;

    // point
    public static final int GET_POINT_INFO = 13;
    public static final int RECEIVE_POINT = 14;

    // charge
    public static final int GET_CHARGE_INFO = 15;

    // tin tức
    public static final int GET_NEWS = 16;
    public static final int GET_POPUP = 17;

    // find board
    public static final int FIND_BOARD = 18;
    //action xet type money
    public static final int SET_MONEY_TYPE = 19;
    // đếm số bàn, số người
    public static final int GET_BOARD_USER_COUNT = 20;    
    //mua tẩy vào chơi game
    public static final int BUY_STACK_IN_LOBBY= 21;
    //get profile
    public static final int GET_PROFILE= 22;
    //request  lịch sử ván chơi
    public static final int REQUEST_HISTORY=23;
    //request lịch sử giao dịch
    public static final int REQUEST_TRANSACTION_HISTORY=24;
    //gửi về danh sách mức cược trong màn hinh lobby game cash out
    public static final int LOBBY_LIST_COUNTER= 25;
    //request danh sách VIP INFO
    public static final int VERIFY_GG_IAP = 26;
    //request thong tin Vip cua User
    public static final int GET_USER_VIP_INFO = 27;
    //request Z cua user, so Z con doi dc, ty le quy doi
    public static final int GET_CASHOUT_Z_INFO = 28;
    //request doi Z thanh cash
    public static final int CASHOUT_Z_POINT = 29;
    //gui ve tong tien,point của user
    public static final int UPDATE_MONEY_POINT = 30;
    //gửi về danh sách bàn đang playing cua user(reconect)
    public static final int INFOR_BOARD_PLAYING = 31;
    public static final int RECONNECT_GAME = 32;
    //notify up level VIP cho client
    public static final int NOTIFY_UP_LEVEL_VIP = 33;
    //notify cong tien cho user khi cashoutZ thanh cong
    public static final int NOTIFY_BONUS_MONEY = 34;
    //xet ngôn ngữ trong game
    public static final int SET_LOCALE =35;
    //tự động tìm bàn nhanh
    public static final int AUTO_JOIN =36;

    // lấy thông tin nạp/rút btc
    public static final int GET_BTC_PAYMENT_INFO = 37;
    // cập nhật mode chơi normal/shuffle
    public static final int UPDATE_PLAY_MODE = 38;
    public static final int GET_TRANSFER_INFO = 39;
    public static final int TRANSFER_MONEY = 40;
    public static final int BUY_TICKET_TOURNAMENT= 41;
    public static final int LOBBY_POKER_LIST_COUNTER_TOURNAMENT= 42;

    public static final int WITHDRAW = 43;
    public static final int PING = 44;
    public static final int WITHDRAW_BANKING = 45;
    public static final int GET_WITHDRAW_BANKING_INFO = 46;
    public static final int GET_TRANSFER_QUOTA = 47;
    public static final int REQUEST_EVENT = 48;
    public static final int BROADCAST = 49;
    public static final int UPDATE_CLIENT = 50;
    public static final int RECHARGE_CARD = 51;
    public static final int RECONNECT_POKER = 52;
    public static final int GET_POINT_CONVERT_CONFIG = 53;
    public static final int CONVERT_POINT_2_MONEY = 54; 
    public static final int CONVERT_POINT_2_MONEY_MESSAGE = 55;
    public static final int LINK_FACEBOOK = 56;
    public static final int GET_GAME_LIST_MOON = 57;
    public static final int GET_GAME_LIST_HANDICAP = 58;
    public static final int CHAT = 59;
}
