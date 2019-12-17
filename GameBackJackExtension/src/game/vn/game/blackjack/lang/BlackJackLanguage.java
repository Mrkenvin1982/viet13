/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.vn.game.blackjack.lang;

import game.vn.util.GlobalsUtil;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 *
 * @author 
 */
public class BlackJackLanguage {
    
    //key: message key - value: message content
    public static final String POINT = "point";
    public static final String NOT_ENOUGH_POINT = "notEnoughPoint";
    public static final String NOT_ENOUGH_MONEYBOARD = "notEnoughMoneyBoard";
    public static final String OWNER_NOT_ENOGH_MONEY = "ownerNotEnoughMoney";
    
    public static final String BL_CHECK_1 = "blXetBai1";
    public static final String QUAC = "quac";
    public static final String XIDACH = "xidach";
    public static final String XIBANG = "xibang";
    public static final String NGULINH = "ngulinh";
    public static final String SET_OWNER="setOwner";
    public static final String OVER_TIME_SET_MONEY="notEnoughTimeSetMoney";
    public static final String ERRO_SET_MONEY="erroSetMoney";
    public static final String BET_MONEY = "betMoney";

    //key: message key - value: message content
    private static final Map<String, String> MAP_VI = new HashMap<>();
    private static final Map<String, String> MAP_EN = new HashMap<>();
    private static final Map<String, String> MAP_CHINESE = new HashMap<>();
    private static final Map<Locale, Map<String, String>> MAP = new HashMap<>();   
    
    static {
        MAP_VI.put(POINT, "điểm");
        MAP_VI.put(NOT_ENOUGH_POINT, "Chưa đủ tuổi");
        MAP_VI.put(NOT_ENOUGH_MONEYBOARD, "Bạn phải đặt cược lớn hơn ");
        MAP_VI.put(OWNER_NOT_ENOGH_MONEY, "Hiện tại chủ bàn không đủ %s cược, xin vui lòng vào bàn khác.");
        MAP_VI.put(BL_CHECK_1, "Cái phải trên %d tuổi để xét.");
        MAP_VI.put(QUAC, "Quắc");
        MAP_VI.put(XIDACH, "Xì Dách");
        MAP_VI.put(XIBANG, "Xì Bàng");
        MAP_VI.put(NGULINH, "Ngũ Linh");
        MAP_VI.put(SET_OWNER, "%s được trở thành nhà cái.");
        MAP_VI.put(OVER_TIME_SET_MONEY, "Hết thời gian đổi cược");
        MAP_VI.put(ERRO_SET_MONEY, "Bạn chỉ được đổi cược mỗi ván 1 lần.");
        MAP_VI.put(BET_MONEY, "Bạn chỉ có thể đặt tối đa %d lần mức cược.");
        MAP.put(new Locale(GlobalsUtil.VIETNAMESE), MAP_VI);

        MAP_EN.put(POINT, "points");
        MAP_EN.put(NOT_ENOUGH_POINT, "Not enough points");
        MAP_EN.put(NOT_ENOUGH_MONEYBOARD, "You have to deal money than");
        MAP_EN.put(OWNER_NOT_ENOGH_MONEY, "Banker does not have enough %s, please find another table.");
        MAP_EN.put(BL_CHECK_1, "Banker must over %d points to check.");
        MAP_EN.put(QUAC, "Busted");
        MAP_EN.put(XIDACH, "Blackjack");
        MAP_EN.put(XIBANG, "Double A");
        MAP_EN.put(NGULINH, "Over five");
        MAP_EN.put(SET_OWNER, "%s becomes the banker.");
        MAP_EN.put(OVER_TIME_SET_MONEY, "No more bet.");
        MAP_EN.put(ERRO_SET_MONEY, "You can only change your bet once each hand.");
        MAP_EN.put(BET_MONEY, "You can only bet maximum by %d times minimum bet.");
        MAP.put(new Locale(GlobalsUtil.ENGLISH), MAP_EN);
        
        MAP_CHINESE.put(POINT, "点");
        MAP_CHINESE.put(NOT_ENOUGH_POINT, "不够点");
        MAP_CHINESE.put(NOT_ENOUGH_MONEYBOARD, "您需要押注更高的金额");
        MAP_CHINESE.put(OWNER_NOT_ENOGH_MONEY, "当前房主不够 %s 押注，请选择别的桌子。");
        MAP_CHINESE.put(BL_CHECK_1, "庄家必须要 %d 分以上。");
        MAP_CHINESE.put(QUAC, "超点");
        MAP_CHINESE.put(XIDACH, "二十一点");
        MAP_CHINESE.put(XIBANG, "两个A");
        MAP_CHINESE.put(NGULINH, "五龙");
        MAP_CHINESE.put(SET_OWNER, "%s 成为了庄家。");
        MAP_CHINESE.put(OVER_TIME_SET_MONEY, "更换押注时间已结束.");
        MAP_CHINESE.put(ERRO_SET_MONEY, "每一局只能更换一次押注。");
        MAP_CHINESE.put(BET_MONEY, "您每次最多只能押注 %d 。");
        MAP.put(new Locale(GlobalsUtil.CHINESE), MAP_CHINESE);
    }
    
    public static String getMessage(String key, Locale locale) {
        Map<String, String> map = MAP.get(locale);
        if (map != null) {
            return map.get(key);
        }
        return "";
    }

}
