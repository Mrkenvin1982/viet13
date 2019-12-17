/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.vn.game.xito.lang;

import game.vn.util.GlobalsUtil;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Class này luu toàn bộ ngôn ngữ của game xì tố
 * @author 
 */
public class XiToLanguage {
    
    public static final String NOT_ENOUGH_WIN_TO_PLAY = "notEnoughWinToPlay";
    public static final String CALL = "call";
    public static final String BET = "bet";
    public static final String RAISE = "raise";
    public static final String ALL_IN = "allin";
    public static final String CHECK = "check";
    public static final String FOLD = "fold";
    public static final String STRAIGHT_FLUSH = "straightFlush";
    public static final String FOUR_OF_A_KIND = "fourofakind";
    public static final String FULL_HOUSE = "fullHouse";
    public static final String FLUSH = "flush";
    public static final String STRAIGHT = "straight";
    public static final String THREE_OF_A_KIND = "threeofakind";
    public static final String TWO_PAIRS = "twoPairs";
    public static final String PAIR = "pair";
    public static final String HIGH_CARD = "highCard";
    public static final String NOT_ENOUGH_STACK_TO_RAISE = "notEnoughStackToRaise";
    public static final String NOT_ENOUGH_STACK_TO_BET = "notEnoughStackToBet";
    public static final String INVALID_RAISE_STACK = "invalidRaiseStack";

    //key: message key - value: message content
    private static final Map<String, String> MAP_VI = new HashMap<>();
    private static final Map<String, String> MAP_EN = new HashMap<>();
    private static final Map<String, String> MAP_CHINESE = new HashMap<>();
    public static final Map<Locale, Map<String, String>> MAP = new HashMap<>();    
    static {
        MAP_VI.put(NOT_ENOUGH_WIN_TO_PLAY, "Bạn không đủ %s để chơi ván này!");
        MAP_VI.put(CALL, "Theo %s");
        MAP_VI.put(BET, "Tố %s");
        MAP_VI.put(RAISE, "Nâng %s");
        MAP_VI.put(ALL_IN, "Tố hết %s");
        MAP_VI.put(CHECK, "Nhường tố");
        MAP_VI.put(FOLD, "Úp bỏ");
        MAP_VI.put(STRAIGHT_FLUSH, "Thùng phá sảnh");
        MAP_VI.put(FOUR_OF_A_KIND, "Tứ quí");
        MAP_VI.put(FULL_HOUSE, "Cù lủ");
        MAP_VI.put(FLUSH, "Thùng");
        MAP_VI.put(STRAIGHT, "Sảnh");
        MAP_VI.put(THREE_OF_A_KIND, "Xám chi");
        MAP_VI.put(TWO_PAIRS, "Hai đôi");
        MAP_VI.put(PAIR, "Một đôi");
        MAP_VI.put(HIGH_CARD, "Mậu thầu");
        MAP_VI.put(NOT_ENOUGH_STACK_TO_RAISE, "Bạn không đủ tẩy để nâng");
        MAP_VI.put(NOT_ENOUGH_STACK_TO_BET, "Bạn không đủ tẩy để tố");
        MAP_VI.put(INVALID_RAISE_STACK, "Mức nâng không hợp lệ");
        MAP.put(new Locale(GlobalsUtil.VIETNAMESE), MAP_VI);

        MAP_EN.put(NOT_ENOUGH_WIN_TO_PLAY, "You have not enough %s to play!");
        MAP_EN.put(CALL, "Call %s");
        MAP_EN.put(BET, "Bet %s");
        MAP_EN.put(RAISE, "Raise %s");
        MAP_EN.put(ALL_IN, "All in %s");
        MAP_EN.put(CHECK, "Check");
        MAP_EN.put(FOLD, "Fold");
        MAP_EN.put(STRAIGHT_FLUSH, "Straight Flush");
        MAP_EN.put(FOUR_OF_A_KIND, "Four of a kind");
        MAP_EN.put(FULL_HOUSE, "Full House");
        MAP_EN.put(FLUSH, "Flush");
        MAP_EN.put(STRAIGHT, "Straight");
        MAP_EN.put(THREE_OF_A_KIND, "Three of a kind");
        MAP_EN.put(TWO_PAIRS, "Two Pairs");
        MAP_EN.put(PAIR, "Pair");
        MAP_EN.put(HIGH_CARD, "High Card");
        MAP_EN.put(NOT_ENOUGH_STACK_TO_RAISE, "You have not enough Stack to raise");
        MAP_EN.put(NOT_ENOUGH_STACK_TO_BET, "You have not enough Stack to bet");
        MAP_EN.put(INVALID_RAISE_STACK, "Invalid raise stack");
        MAP.put(new Locale(GlobalsUtil.ENGLISH), MAP_EN);
        
        MAP_CHINESE.put(NOT_ENOUGH_WIN_TO_PLAY, "您不够 %s 赌这一局！");
        MAP_CHINESE.put(CALL, "跟进 %s");
        MAP_CHINESE.put(BET, "押注 %s");
        MAP_CHINESE.put(RAISE, "加注 %s");
        MAP_CHINESE.put(ALL_IN, "梭哈 %s");
        MAP_CHINESE.put(CHECK, "让牌");
        MAP_CHINESE.put(FOLD, "放弃");
        MAP_CHINESE.put(STRAIGHT_FLUSH, "同花顺");
        MAP_CHINESE.put(FOUR_OF_A_KIND, "四条");
        MAP_CHINESE.put(FULL_HOUSE, "葫芦");
        MAP_CHINESE.put(FLUSH, "同花");
        MAP_CHINESE.put(STRAIGHT, "顺子");
        MAP_CHINESE.put(THREE_OF_A_KIND, "三条");
        MAP_CHINESE.put(TWO_PAIRS, "两对");
        MAP_CHINESE.put(PAIR, "一对");
        MAP_CHINESE.put(HIGH_CARD, "散牌");
        MAP_CHINESE.put(NOT_ENOUGH_STACK_TO_RAISE, "您不够筹码加注");
        MAP_CHINESE.put(NOT_ENOUGH_STACK_TO_BET, "您不够筹码押注");
        MAP_CHINESE.put(INVALID_RAISE_STACK, "加注额度无效");
        MAP.put(new Locale(GlobalsUtil.CHINESE), MAP_CHINESE);
    }
    
    public static String getMessage(String key, Locale locale) {
        Map<String, String> map = MAP.get(locale);
        if (map != null) {
            return map.get(key);
        }
        return null;
    }
}
