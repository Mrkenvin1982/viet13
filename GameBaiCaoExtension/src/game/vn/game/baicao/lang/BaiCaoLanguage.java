/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.vn.game.baicao.lang;

import game.vn.util.GlobalsUtil;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Các câu thông báo trong game
 * @author 
 */
public class BaiCaoLanguage {
     //key: message key - value: message content
    public static final String THREE_FACES = "threeFaces";
    public static final String ZERO_POINT = "zeroPoint";
    public static final String POINT = "point";
    public static final String OWNER_NOT_ENOUGH_WIN = "ownerNotEnoughWin";
    public static final String SET_OWNER="setOwner";
    public static final String OVER_TIME_SET_MONEY="notEnoughTimeSetMoney";
    public static final String BET_MONEY = "betMoney";

    //key: message key - value: message content
    private static final Map<String, String> MAP_VI = new HashMap<>();
    private static final Map<String, String> MAP_EN = new HashMap<>();
    private static final Map<String, String> MAP_CHINESE = new HashMap<>();
    private static final Map<Locale, Map<String, String>> MAP = new HashMap<>();    
    static {
        MAP_VI.put(THREE_FACES, "3 tiên");
        MAP_VI.put(ZERO_POINT, "Bù");
        MAP_VI.put(POINT, "nút");
        MAP_VI.put(OWNER_NOT_ENOUGH_WIN, "Hiện tại chủ bàn không đủ %s cược, xin vui lòng vào bàn khác.");
        MAP_VI.put(SET_OWNER, "%s được trở thành nhà cái.");
        MAP_VI.put(OVER_TIME_SET_MONEY, "Hết thời gian đổi cược.");
        MAP_VI.put(BET_MONEY, "Bạn chỉ có thể đặt tối đa %d lần mức cược.");
        MAP.put(new Locale(GlobalsUtil.VIETNAMESE), MAP_VI);

        MAP_EN.put(THREE_FACES, "Three Face");
        MAP_EN.put(ZERO_POINT, "Zero");
        MAP_EN.put(POINT, "points");
        MAP_EN.put(OWNER_NOT_ENOUGH_WIN, "Banker does not have enough %s, please find another table.");
        MAP_EN.put(SET_OWNER, "%s becomes the banker.");
        MAP_EN.put(OVER_TIME_SET_MONEY, "No more bet.");
        MAP_EN.put(BET_MONEY, "You can only bet maximum by %d times minimum bet.");
        MAP.put(new Locale(GlobalsUtil.ENGLISH), MAP_EN);
        
        MAP_CHINESE.put(THREE_FACES, "三公");
        MAP_CHINESE.put(ZERO_POINT, "零点");
        MAP_CHINESE.put(POINT, "点");
        MAP_CHINESE.put(OWNER_NOT_ENOUGH_WIN, "当前房主不够 %s 押注，请选择别的桌子。");
        MAP_CHINESE.put(SET_OWNER, "%s 成为了庄家。");
        MAP_CHINESE.put(OVER_TIME_SET_MONEY, "更换押注时间已结束.");
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

