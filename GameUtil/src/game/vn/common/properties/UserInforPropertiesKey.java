/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.vn.common.properties;

/**
 * Chứa tất cả key properties cua user
 * @author tuanp
 */
public class UserInforPropertiesKey {
    
    //VARIABLE
    //tiền thiệt(var)
    public static final String MONEY_USER ="moneyUser";
    //tiền ảo của user(var)
    public static final String POINT_USER ="pointUser";
    //tẩy trong bàn chơi (var)
    public static final String MONEY_STACK ="moneyStack";
    //chổ ngồi (var)
    public static final String SEAT_USER ="seatUser";
    //chơi nhanh (var)
    public static final String QUICK_PLAY = "quickPlay";
    //id database của user (var)
    public static final String ID_DB_USER ="idDBUser";
    //avatar (var)
    public static final String AVATAR = "avatar";
    public static final String MERCHANT_ID = "merchantId";
    // loại tiền đang choi: 1= tiền ảo, 2= tiền thiệt (var)
    public static final String MONEY_TYPE = "moneyType";
    //là user ngồi chờ
    public static final String IS_WAITER ="isWaiter";
    //tồn tại user trong phòng thì ép nó ra
    public static final String IS_FORCE_LOGOUT ="isForceLogout";
    //ti le chi bai dep(danh cho bot)
    public static final String ADVANTAGE_RATIO ="advantageRatio";
    //lợi thế đi nhất 
    public static final String FIRST_RATIO ="firstRatio";
    public static final String GROUP_ID ="groupId";
    
    //PROPERTY
    //ngôn ngữ (pro)
    public static final String LOCALE_USER ="localeUser";
    //id game (pro)
    public static final String SERVICE_ID = "serviceId";
    //user state
    public static final String USER_STATE = "userState";
    //có đang chơi trong ván (pro)
    public static final String IN_TURN = "IN_TURN";
    //thời gian chat in-game cuối cùng
    public static final String LAST_TIME_CHAT_INGAME = "lastTimeChatIngame";
    public static final String PLATFORM = "platform";
    public static final String CHANNEL = "channel";
    public static final String VERSION = "version";
    //client infor
    public static final String CLIENT_INFOR = "clientInfor";
    public static final String USER_TOKEN = "userToken";
    public static final String DISPLAY_NAME = "displayName";
    //điếm số lượt user không action
    public static final String COUNT_NO_ACTION_TIME = "countNoActionTime";
    public static final String DEVICE = "device";
    public static final String SESSION_ID ="sessionId";

    // danh sách các game đang bật shuffle của user
    public static final String SHUFFLE_GAMES = "shuffleGames";
    public static final String ON_SHUFFLE = "onShuffle";
    
    public static final String ACCESS_TOKEN = "accessToken";
    public static final String PROFILE = "profile";
    public static final String USER_TYPE = "userType";
    public static final String EMAIL = "email";
    public static final String CURRENCY = "currency";
}
