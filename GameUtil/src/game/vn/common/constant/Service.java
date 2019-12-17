/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.vn.common.constant;

import game.vn.util.GlobalsUtil;
import java.util.Locale;

/**
 *
 * @author hanv
 */
public class Service {

    public static final byte SYSTEM = 0;
    public static final byte BLACKJACK = 1;
    public static final byte TIENLEN = 2;
    public static final byte XI_TO = 3;
    public static final byte PHOM = 4;
    public static final byte BAI_CAO = 5;
    public static final byte MAUBINH = 6;
    public static final byte LIENG = 7;
    public static final byte SAM = 9;
    public static final byte TIEN_LEN_DEM_LA = 10;
    public static final byte TIEN_LEN_TOUR = 11;
    public static final byte TLDL_SOLO = 12;

    /**
     * not game service id
     */
    public static final byte AVATAR1 = -1;
    public static final byte AVATAR2 = -2;
    public static final byte TRANSFER_MONEY = -3; //chuyển tiền
    public static final byte BONUS_MONEY_DAILY = -4; //tặng tiền mỗi ngày login
    public static final byte GIFT = -5; //tặng quà trong game
    public static final byte LIXI = -6; //lì xì
    public static final byte QUEST = -7; // For quests.
    public static final byte INGAME_MONEY = -8; //tiền trong game
    public static final byte FACEBOOK_REQUEST = -9;
    public static final byte TAI_XIU = -10;

    /**
     * declare name of game.
     */
    private static final String TIENLEN_NAME = "Tiến lên Miền Nam";
    private static final String PHOM_NAME = "Phỏm";
    private static final String CAO_NAME = "Bài cào";
    private static final String BLACKJACK_NAME = "Xì dách";
    private static final String POKER_HK_NAME = "Xì tố";
    private static final String MAUBINH_NAME = "Mậu binh";
    private static final String LIENG_NAME = "Liêng";
    private static final String TAIXIU_NAME = "Tài xỉu";
    private static final String SAM_NAME = "Sâm";
    private static final String TIEN_LEN_DEM_LA_NAME = "Tiến lên đếm lá";
    private static final String TIEN_LEN_TOUR_NAME = "Tiến Lên Miền Nam Giải đấu Spin & Go ";
    private static final String TLDL_SOLO_NAME = "Tiến lên đếm lá solo";

    //english
    private static final String TIENLEN_NAME_EN = "Killer 13";
    private static final String PHOM_NAME_EN = "Rummy";
    private static final String CAO_NAME_EN = "3 Cards";
    private static final String BLACKJACK_NAME_EN = "Blackjack";
    private static final String POKER_HK_NAME_EN = "Hong Kong Poker";
    private static final String MAUBINH_NAME_EN = "Chinese Poker";
    private static final String LIENG_NAME_EN = "Lieng";
    private static final String TAIXIU_NAME_EN = "Big Small";
    private static final String SAM_NAME_EN = "Sam";
    private static final String TIEN_LEN_DEM_LA_NAME_EN = "Killer 39";
    private static final String TIEN_LEN_TOUR_NAME_EN = "Killer Tour";
    private static final String TLDL_SOLO_NAME_EN = "Killer 39 solo";

    private static String getServiceNameEn(byte serviceID) {
        String returnString = null;
        switch (serviceID) {
            case TIENLEN:
                returnString = TIENLEN_NAME_EN;
                break;
            case PHOM:
                returnString = PHOM_NAME_EN;
                break;
            case BAI_CAO:
                returnString = CAO_NAME_EN;
                break;
            case BLACKJACK:
                returnString = BLACKJACK_NAME_EN;
                break;
            case XI_TO:
                returnString = POKER_HK_NAME_EN;
                break;
            case SYSTEM:
                returnString = "SYSTEM";
                break;
            case MAUBINH:
                returnString = MAUBINH_NAME_EN;
                break;
            case LIENG:
                returnString = LIENG_NAME_EN;
                break;
            case TAI_XIU:
                returnString = TAIXIU_NAME_EN;
                break;
            case SAM:
                returnString = SAM_NAME_EN;
                break;
            case TIEN_LEN_DEM_LA:
                returnString = TIEN_LEN_DEM_LA_NAME_EN;
                break;
             case TIEN_LEN_TOUR:
                returnString = TIEN_LEN_TOUR_NAME_EN;
                break;
            case TLDL_SOLO:
                returnString = TLDL_SOLO_NAME_EN;
                break;
            default:
                returnString = "";
                break;
        }
        return returnString;
    }

    /**
     *
     * @param serviceID
     * @return
     */
    public static String getSeviceName(byte serviceID) {
        String returnString = null;
        switch (serviceID) {
            case TIENLEN:
                returnString = TIENLEN_NAME;
                break;
            case PHOM:
                returnString = PHOM_NAME;
                break;
            case BAI_CAO:
                returnString = CAO_NAME;
                break;
            case BLACKJACK:
                returnString = BLACKJACK_NAME;
                break;
            case XI_TO:
                returnString = POKER_HK_NAME;
                break;
            case SYSTEM:
                returnString = "SYSTEM";
                break;
            case MAUBINH:
                returnString = MAUBINH_NAME;
                break;
            case LIENG:
                returnString = LIENG_NAME;
                break;
            case TAI_XIU:
                returnString = TAIXIU_NAME;
                break;
            case SAM:
                returnString = SAM_NAME;
                break;
            case TIEN_LEN_DEM_LA:
                returnString = TIEN_LEN_DEM_LA_NAME;
                break;
            case TIEN_LEN_TOUR:
                returnString = TIEN_LEN_TOUR_NAME;
                break;
            case TLDL_SOLO:
                returnString = TLDL_SOLO_NAME;
                break;
            default:
                returnString = "";
                break;
        }
        return returnString;
    }
    
    public static String getServiceNameByLanguage(byte serviceID, Locale locale) {
        String result = "";
        switch (locale.getLanguage()) {
            case GlobalsUtil.ENGLISH:
                result = getServiceNameEn(serviceID);
                break;
            default:
                result = getSeviceName(serviceID);
        }
        return result;
    }
}
