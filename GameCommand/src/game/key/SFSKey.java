/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.key;

/**
 *
 * @author hanv
 */
public class SFSKey {

    public static final String SERVICE_ID = "svid";
    public static final String SERVER_ID = "server_id";
    //command
    public static final String COMMAND = "cmd";
    //user info
    public static final String USER_INFOR = "uif";
    
    public static final String ROOM_INFOR = "rif";
    
    //Xử lý chức năng move trong game
    public static final String ACTION_INGAME = "acg";
    //Xử lý chức năng move trong game
    public static final String ACTION_INCORE = "acc";
    
    //mức cược trong game
    public static final String BET_BOARD = "bb";
    //mức cược max trong game
    public static final String MAX_BET_BOARD = "mbb";
    //thông tin card 
    public static final String ARRAY_INFOR_CARD = "aic";
    //thong tin owner
    public static final String OWNER ="owner";
    //infor card
    public static final String INFOR_CARD = "ic";
    //thong tin SFSArray
    public static final String ARRAY_SFS = "asfs";
    //thời gian lật bài
    public static final String OPEN_CARD_TIME = "oct";
    //user id
    public static final String USER = "user";
    public static final String USER_ID = "ui";
    //user id current
    public static final String USER_ID_CURRENT = "uicurr";
    // gui kieu tring VIETNAMESE
    public static final String STRING_MESSAGE_VI = "strmv";
    // gui kieu tring ENGLISH
    public static final String STRING_MESSAGE_EN = "strme";
     // gui kieu tring chinese
    public static final String STRING_MESSAGE_ZH = "strzh";
    //kieu string khong phan biet ngon ngu
    public static final String STRING_MESSAGE= "str";
    //so user trong phong
    public static final String COUNT_USER_INGAME = "cuig";
    //thời gian count down start ván
    public static final String COUNTDOWN_START_GAME_TIME = "cdsg";
    //key  name để get money của user
    public static final String MONEY_USER = "money";
    //số tiền user đặt cược
    public static final String MONEY_BET = "moneyb";
    //Money được bonus thêm
    public static final String MONEY_BONUS = "moneybn";
    //số lượng game
    public static final String COUNT_GAME = "cg";
    //name này dung chung cho tất cả các giá trị name
    public static final String NAME = "n";
    //name này dung chung cho tất cả các giá trị array name
    public static final String NAMES = "narr";
    //array name real money lobby
    public static final String NAMES_REAL_MONEY = "narrReal";
     //Hiển thị thông báo toast
    public static final String TOAST_MESSAGE = "tmsg";
    //Thời gian tồn tại 1 message thông báo trên màn hình
    public static final String TIME_EXIT = "tme";
    //type
    public static final String TYPE = "ty";
    //seat
    public static final String SEAT_USER = "seat";
    public static final String IS_WAITER = "isWaiter";
    //avatar
    public static final String AVATAR = "avatar";

    public static final String CODE = "code";
    public static final String MESSAGE = "message";
    public static final String DISPLAY_NAME = "display_name";
    public static final String ID = "id";
    public static final String IMAGE = "image";
    public static final String ANSWER = "answer";
    public static final String REQUEST = "request";
    public static final String BOARDS = "boards";
    public static final String STATUS = "status";
    public static final String PAGE = "page";
    
    public static final String SHUFFLE_GAMES = "shuffle_games";
    public static final String MONEY_TYPE = "moneyT";
    
    public static final String FINE_FACTOR = "fine_factor";
    public static final String GAME_POINT_INFO = "game_info";
    public static final String GAME_MONEY_INFO = "game_money_info";
    public static final String GAME_INFO = "game_info";
    public static final String GAME_MIN_MONEY_INFO = "gMinMoney";
    
    public static final String LOGIN_TYPE = "login_type";
    public static final String LOGIN_TOKEN = "login_token";
    public static final String MERCHANT_ID = "merchantId";
    public static final String CLIENT_INFO = "client_info";
    public static final String PROVIDER = "provider";
    public static final String GAME_ID = "game_id";

    //id database của user
    public static final String ID_DB_USER = "idDBUser";
    public static final String DATA = "data";
    //danh sách mức cược của games
    public static final String LIST_BET_BOARD = "bets";
    //mức tiền tối thiểu mua tẩy của từng game
     public static final String GAME_MIN_MONEY_FACTORY = "gameMoneyFactor";
     //số tiền mua tẩy
    public static final String MONEY_STACK = "moneyStack";
    //số tiền tối thiểu mua tẩy
    public static final String MIN_MONEY_STACK = "minStack";
    //số tiền tối đa mua tẩy
    public static final String MAX_MONEY_STACK = "maxStack";
    //token    
    public static final String TOKEN = "token";
    public static final String TOKEN_LOGIN = "tokenLogin";
    public static final String IS_OWNER = "isOwner";

    //thời gian chờ nhận điểm
    public static final String TIME_VIDEO = "time_video";
    public static final String TIME_FREE = "time_free";
    public static final String TIME_FREE_TOTAL = "time_free_total";
    public static final String TIME_WAIT = "time_wait";
    //số điểm nhận được
    public static final String POINT_FREE = "point_free";
    public static final String POINT_VIDEO = "point_video";
    //index page
    public static final String INDEX_PAGE = "indexPage";
    public static final String DATE_START = "startDate";
    public static final String DATE_END = "endDate";

    //payment
    public static final String IAP_ITEMS = "iap_items";
    public static final String URL = "url";
    public static final String URL_EN = "url_en";
    public static final String PAYMENT_URL = "payment_url";
    public static final String POINT = "point";
    public static final String MONEY = "money";
    public static final String VALUE = "value";
    public static final String PROMOTION = "promotion";
    public static final String PRODUCT_ID = "product_id";
    public static final String CLIENT_ID = "client_id";
    public static final String SPECIAL = "special";
    public static final String LEVEL = "level";
    public static final String ACTION = "action";
    public static final String PAYMENT = "payment";
    public static final String RECEIPT = "receipt";

    //vip
    public static final String VIP_INFO = "vipinfo";
    public static final String VIP_RANK = "currentRank";
    public static final String VIP_STEP = "currentStep";
    public static final String NEXT_RANK = "nextRank";
    public static final String NEXT_STEP = "nextStep";
    public static final String CURRENT_POINT = "currentPoint";
    public static final String TOTAL_POINT = "totalPoint";
    public static final String TOTAL_Z = "currentZ";
    public static final String CASHOUT_RATE = "cashoutRate";
    public static final String CASHOUT_MONTH_QUOTA = "cashoutQuta";
    public static final String CASHOUT_RESULT = "cashoutResult";
    public static final String CASHOUT_MSG = "cashoutMessage";
    public static final String TOTAL_Z_CASHOUT = "zCashout";
    public static final String CURRENT_IMG = "currentImage";
    public static final String NEXT_IMG = "nextImage";
    public static final String CURRENT_IMG_MINI = "currentImageMini";
    public static final String NEXT_IMG_MINI = "nextImageMini";
    public static final String UP_LEVEL_INFO = "upLevelInfo";
    public static final String BONUS_CASH_INFO = "bonusCashInfo";
    public static final String MIN_Z_CASHOUT = "minCashoutZ";

    // news
    public static final String NEWS = "news";
    public static final String POPUP = "popup";
    public static final String TITLE = "title";
    public static final String CONTENT = "content";
    public static final String BUTTON = "button";
    public static final String BUTTON1 = "btn1";
    public static final String BUTTON2 = "btn2";
    public static final String ICON = "icon";
    public static final String IMAGE_LARGE = "img_large";
    public static final String CATEGORY = "category";
    public static final String SCREEN_ID = "screen_id";
    public static final String TIME = "time";
    public static final String CAPTION = "caption";

    public static final String PIN = "pin";
    public static final String NEW_PIN = "new_pin";

    // taixiu
    public static final String BET_MONEY = "bet_money";
    public static final String CHOICE = "choice";
    public static final String MATCH_ID = "match_id";
    public static final String AUTO = "auto";
    public static final String DICE = "dice";
    
    public static final String FEE = "fee";
    public static final String INFO = "info";
    public static final String RATE = "rate";
    public static final String ADDRESS = "address";
    public static final String RESULT = "result";
    public static final String PROFILE = "profile";
    public static final String AMOUNT = "amount";
    public static final String NUMBER = "number";
    public static final String PROVINCE = "province";
    public static final String CITY = "city";
    public static final String BRANCH = "branch";
    public static final String MIN = "min";
    public static final String MAX = "max";
    public static final String REQUIRED = "required";
    
    public static final String SCREEN = "screen";
    public static final String SCREEN_SUB = "screen_sub";
}
