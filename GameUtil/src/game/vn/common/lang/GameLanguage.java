/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.vn.common.lang;

import java.util.Locale;
import java.util.ResourceBundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Các câu thông báo trong game
 * @author tuanp
 */
public class GameLanguage {
    
    private final static Logger LOGGER = LoggerFactory.getLogger(GameLanguage.class);
     //key: message key - value: message content
    //game chung
    public static final String TYPE_POINT = "typePoint";
    public static final String TYPE_MONEY = "typeMoney";
    public static final String NAME_POINT = "namePoint";
    public static final String NAME_MONEY = "nameMoney";
    public static final String NO_MONEY_OWNER = "noMoneyOwner";
    public static final String NO_MONEY_USER = "noMoneyUser";
    public static final String SET_USER_OK = "setUserOK";
    public static final String SET_USER_FAIL = "setUserFail";
    public static final String SET_MONEY_1 = "setMoney1";
    public static final String SET_MONEY_2 = "setMoney2";
    public static final String SET_MONEY_3 = "setMoney3";
    public static final String SET_MONEY_4 = "setMoney4";
    public static final String SET_MONEY_5 = "setMoney5";
    public static final String SET_MONEY_6 = "setMoney6";
    public static final String SET_MONEY_7 = "setMoney7";
    public static final String SET_MONEY_8 = "setMoney8";
    public static final String SET_MONEY_9 = "setMoney9";
    public static final String ERROR_TRY_AGAIN = "errorTryAgain";
    public static final String ERROR_DISPLAY_NAME_MIN_LENGTH = "errorDisplayNameMinLength";
    public static final String ERRO_RECONECT = "erroReconect";
    public static final String NO_ACTION_CONFIRM = "noActionConfirm";
    public static final String NO_ACTION_IN_GAME = "noActionInGame";
    public static final String FUNCTION_MAINTAIN = "functionMaintain";
    public static final String JOIN_ROOM_ERROR = "joinRoomError";
    public static final String NOT_EXIST_BET_BOARD = "noExistBetBoard";
    public static final String NOT_EXIST_MONEY_BOARD = "noExistMoneyBoard";
    public static final String IS_FULL_BOARD = "isFullBoard";
    public static final String INFOR_BUY_STACK = "inforBuyStack";
    public static final String NO_SUM_MONEY = "noSumMoney";
    public static final String NO_BUY_STACK = "noBuyStack";
    public static final String CAN_NOT_PLAY_GAME = "cannotPlayGame";
    public static final String UPDATE_SUCCESS = "updateSuccess";
    public static final String INVALID_PIN_CODE = "invalidPinCode";
    public static final String WRONG_PIN_CODE = "wrongPinCode";
    public static final String LOCKED_PIN_CODE = "lockedPinCode";
    public static final String LOCKED_PIN_CODE_2 = "lockedPinCode2";
    public static final String BTC_CHARGE = "btcCharge";
    public static final String CANT_LEAVE_GAME_NOW = "cantLeaveGameNow";
    public static final String TRANSFER_MONEY_SUCCESS_1 = "transferMoneySuccess1";
    public static final String TRANSFER_MONEY_SUCCESS_2 = "transferMoneySuccess2";
    public static final String TRANSFER_MONEY_FAIL_1 = "transferMoneyFail1";
    public static final String TRANSFER_MONEY_FAIL_2 = "transferMoneyFail2";
    public static final String TRANSFER_MONEY_FAIL_3 = "transferMoneyFail3";
    public static final String TRANSFER_MONEY_FAIL_4 = "transferMoneyFail4";
    public static final String TRANSFER_MONEY_FAIL_5 = "transferMoneyFail5";
    public static final String TRANSFER_MONEY_FAIL_6 = "transferMoneyFail6";
    public static final String BETTING_TIME_OVER = "bettingTimeOver";
    public static final String BUY_TICKET_TIME_OVER = "buyTicketTimeOver";
    public static final String WAIT_FOR_NEXT_BET = "waitForNextBet";
    public static final String BET_ONE_SIDE_PER_MATCH = "betOneSidePerMatch";
    public static final String NOT_ENOUGH_MONEY_BET = "notEnoughMoneyBet";
    public static final String NOT_ENOUGH_MONEY = "notEnoughMoney";
     public static final String NOT_ENOUGH_SUM_MONEY = "notEnoughSumMoney";
    public static final String INVALID_BET_MONEY = "invalidBetMoney";
    public static final String INAPP_PURCHASE = "inappPurchase";
    public static final String FREE = "free";
    public static final String CARD = "card";
    public static final String SATOSHI = "satoshi";
    public static final String TRANSFER = "transfer";
    public static final String DEPOSIT = "deposit";
    public static final String WITHDRAW = "withdraw";
    public static final String REFUND = "refund";
    public static final String UNKNOWN = "unknown";
    public static final String CAN_NOT_PLAY_GAME_TOUR = "cannotPlayGameTour";
    public static final String WIN_CONVERT_VND_SUCCESS = "winConvertVndSuccess";
    public static final String CONVERT = "convert";
    public static final String APPROVAL = "approval";
    public static final String CONVERT_LIMIT = "convertLimit";
    public static final String CHAT_INTERVAL = "chatInterval";

    //ngôn ngữ trong game
    public static final String BET_GAME = "betGame";
    public static final String GIVE_UP = "giveUp";
    public static final String LOSE = "lose";
    public static final String WIN = "win";
    public static final String DRAW = "draw";
    public static final String INVALID_CARD = "invalidCard";
    public static final String REBUY_STACK = "reBuyStack";

    public static final String SUCCESS = "success";
    public static final String WAITING = "waiting";
    public static final String FAIL = "fail";
    public static final String UPDATE_CLIENT_WARNING = "notifyUpdateClientWarning";
    public static final String UPDATE_CLIENT_BLOCK = "notifyUpdateClientBlock";
    public static final String EVENT_NOTIFY = "eventNotify";
    

    public static String getMessage(String property,Locale locale) {
        try {
            return ResourceBundle.getBundle(GameLanguage.class.getName(), locale).getString(property);
        } catch (Exception ex) {
            LOGGER.error("getMessage() error:", ex);
            return null;
        }
    }
}
