/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.vn.game.lieng.lang;

import game.vn.util.GlobalsUtil;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 *
 * @author 
 */
public class LiengLanguage {
    
    public static final String SAP = "sap";
    public static final String LIENG = "lieng";
    public static final String ANH = "anh";
    public static final String SCORE = "score";
    public static final String CALL = "call";
    public static final String BET = "bet";
    public static final String FOLD = "fold";
    public static final String CHECK = "check";
    public static final String NOT_ENOUGH_WIN_TO_BET = "notEnoughWinToBet";
    public static final String NOT_ENOUGH_WIN_TO_CREATE = "NOT_ENOUGH_WIN_TO_CREATE";

    //key: message key - value: message content
    private static final Map<String, String> MAP_VI = new HashMap<>();
    private static final Map<String, String> MAP_EN = new HashMap<>();
    private static final Map<String, String> MAP_CHINESE = new HashMap<>();
    private static final Map<Locale, Map<String, String>> MAP = new HashMap<>();    
    static {
        MAP_VI.put(SAP, "Sáp");
        MAP_VI.put(LIENG, "Liêng");
        MAP_VI.put(ANH, "Ảnh");
        MAP_VI.put(SCORE, "%d điểm");
        MAP_VI.put(CALL, "Theo");
        MAP_VI.put(BET, "Theo %s tố thêm %s");
        MAP_VI.put(FOLD, "");
        MAP_VI.put(CHECK, "");
        MAP_VI.put(NOT_ENOUGH_WIN_TO_BET, "Bạn không đủ %s để tố");
        MAP_VI.put(NOT_ENOUGH_WIN_TO_CREATE, "Bạn phải có %s gấp %d lần tiền cược.");
        MAP.put(new Locale(GlobalsUtil.VIETNAMESE), MAP_VI);

        MAP_EN.put(SAP, "Sap");
        MAP_EN.put(LIENG, "Lieng");
        MAP_EN.put(ANH, "Anh");
        MAP_EN.put(SCORE, "%d points");
        MAP_EN.put(CALL, "");
        MAP_EN.put(BET, "Call %s and raise %s.");
        MAP_EN.put(FOLD, "");
        MAP_EN.put(CHECK, "");
        MAP_EN.put(NOT_ENOUGH_WIN_TO_BET, "You don't have enough %s to bet.");
        MAP_EN.put(NOT_ENOUGH_WIN_TO_CREATE, "You must have %s by %d times minimum bet.");
        MAP.put(new Locale(GlobalsUtil.ENGLISH), MAP_EN);
        
        MAP_CHINESE.put(SAP, "Sap");
        MAP_CHINESE.put(LIENG, "Lieng");
        MAP_CHINESE.put(ANH, "Anh");
        MAP_CHINESE.put(SCORE, "%d 点");
        MAP_CHINESE.put(CALL, "");
        MAP_CHINESE.put(BET, "跟 %s 多加 %s");
        MAP_CHINESE.put(FOLD, "");
        MAP_CHINESE.put(CHECK, "");
        MAP_CHINESE.put(NOT_ENOUGH_WIN_TO_BET, "您不够 %s 来押注");
        MAP_CHINESE.put(NOT_ENOUGH_WIN_TO_CREATE, "您需要 %s 比押金多 %d 倍。");
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
