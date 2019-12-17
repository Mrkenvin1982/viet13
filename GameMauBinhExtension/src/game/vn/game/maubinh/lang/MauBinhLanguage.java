/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.vn.game.maubinh.lang;

import game.vn.util.GlobalsUtil;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author 
 */
public class MauBinhLanguage {
    
    public static final String DUPLICATE_CARD = "DuplicateCard";
    public static final String MISS_CARD = "MissCard";
    public static final String MULTI_KWIN = "MultiKWin";
    public static final String MULTI_KLOSE = "MultiKLose";
    public static final String LEAVE_GAME = "LeaveGame";
    

    //key: message key - value: message content
    private static final Map<String, String> MAP_VI = new HashMap<>();
    private static final Map<String, String> MAP_EN = new HashMap<>();
    private static final Map<String, String> MAP_CHINESE = new HashMap<>();
    private static final Map<Locale, Map<String, String>> MAP = new HashMap<>();    
    static {
        MAP_VI.put(DUPLICATE_CARD, "Gởi trùng quân bài.");
        MAP_VI.put(MISS_CARD, "Gửi thiếu bài. ");
        MAP_VI.put(MULTI_KWIN, "Ăn sập hầm %s, tổng %d chi,");
        MAP_VI.put(MULTI_KLOSE, "Thua sập hầm %s, tổng %d chi,");
        MAP_VI.put(LEAVE_GAME, "Rời bàn chơi.");
        MAP.put(new Locale(GlobalsUtil.VIETNAMESE), MAP_VI);

        MAP_EN.put(DUPLICATE_CARD, "Duplicate card. ");
        MAP_EN.put(MISS_CARD, "Missing card.");
        MAP_EN.put(MULTI_KWIN, "Scoop win %s, total %d units,");
        MAP_EN.put(MULTI_KLOSE, "Scoop lose %s, total %d units,");
        MAP_EN.put(LEAVE_GAME, "Leave table.");
        MAP.put(new Locale(GlobalsUtil.ENGLISH), MAP_EN);
        
        MAP_CHINESE.put(DUPLICATE_CARD, "牌子发放重复。");
        MAP_CHINESE.put(MISS_CARD, "牌子发放缺少.");
        MAP_CHINESE.put(MULTI_KWIN, "赢三家 %s, 一共 %d 注,,");
        MAP_CHINESE.put(MULTI_KLOSE, "输三家 %s, 一共 %d 注,");
        MAP_CHINESE.put(LEAVE_GAME, "离开桌子。");
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


