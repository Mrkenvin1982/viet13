/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.vn.game.sam.lang;

import game.vn.util.GlobalsUtil;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 *
 * @author
 */
public class SamLanguage {

    public static final String TWO = "two";
    public static final String PAIR = "pair";
    public static final String FOUR_OF_A_KIND = "fourOfAKind";
    public static final String UNUSED = "unused";
    public static final String DEFEAT = "defeat";
    public static final String STRAIGHT_DRAGON = "straightDragon";
    public static final String FIVE_PAIR = "fivePairs";
    public static final String THREE_TRIPLES = "threeTriples";
    public static final String INSTANT_LOSE = "instantLose";
    public static final String TEN_CARDS_SAME_COLOR = "tenCardsSameColor";
    public static final String FOUR_2 = "fourPig";
    public static final String BONUS = "bonus";
    public static final String LOSE = "lose";
    public static final String XAM_FAIL = "xamFail";
    public static final String XAM_SUCCESS = "xamSuccess";
    public static final String FORCE_FINISH = "forceFinish";
    public static final String FORCE_FINISH_LOSE = "forceFinishLose";
    public static final String BONUS_INSTANT_LOSE = "bonusInstantLose";
    public static final String NOT_ALLOW_HEO = "notAllowHeo";
    public static final String COMPENSATE = "compensate";
    public static final String DEFEAT_XAM = "defeatXam";
    public static final String DOUBLE_FOUR_OF_A_KIND = "doubleFourOfAKind";

    //key: message key - value: message content
    private static final Map<String, String> MAP_VI = new HashMap<>();
    private static final Map<String, String> MAP_EN = new HashMap<>();
    private static final Map<String, String> MAP_CHINESE = new HashMap<>();
    private static final Map<Locale, Map<String, String>> MAP = new HashMap<>();    
    static {
        MAP_VI.put(TWO, "Heo ");
        MAP_VI.put(PAIR, "Đôi");
        MAP_VI.put(FOUR_OF_A_KIND, "Tứ quí ");
        MAP_VI.put(UNUSED, "Úng ");
        MAP_VI.put(DEFEAT, "Chặt ");
        MAP_VI.put(STRAIGHT_DRAGON, "Sảnh rồng ");
        MAP_VI.put(FIVE_PAIR, "5 đôi ");
        MAP_VI.put(THREE_TRIPLES, "3 Sám cô ");
        MAP_VI.put(INSTANT_LOSE, "Thua cóng ");
        MAP_VI.put(TEN_CARDS_SAME_COLOR, "10 lá đồng màu ");
        MAP_VI.put(FOUR_2, "Tứ quý heo");
        MAP_VI.put(BONUS, "Thưởng ");
        MAP_VI.put(LOSE, "Thua ");
        MAP_VI.put(XAM_FAIL, "Sâm thất bại. ");
        MAP_VI.put(XAM_SUCCESS, "Sâm thành công.");
        MAP_VI.put(FORCE_FINISH, "Tới trắng ");
        MAP_VI.put(FORCE_FINISH_LOSE, "Thua trắng ");
        MAP_VI.put(BONUS_INSTANT_LOSE, "Ăn cóng ");
        MAP_VI.put(NOT_ALLOW_HEO, "Heo không thể đánh ở lượt cuối.");
        MAP_VI.put(COMPENSATE, "Đền bài ");
        MAP_VI.put(DEFEAT_XAM, "chặn sâm.");
        MAP_VI.put(DOUBLE_FOUR_OF_A_KIND, "2 Tứ quí");
        MAP.put(new Locale(GlobalsUtil.VIETNAMESE), MAP_VI);

        MAP_EN.put(TWO, "2s card ");
        MAP_EN.put(PAIR, "Pair ");
        MAP_EN.put(FOUR_OF_A_KIND, "Four of a kind ");
        MAP_EN.put(UNUSED, "Wasted ");
        MAP_EN.put(DEFEAT, "Busted");
        MAP_EN.put(STRAIGHT_DRAGON, "Highest straight");
        MAP_EN.put(FIVE_PAIR, "Five consecutive pairs ");
        MAP_EN.put(THREE_TRIPLES, "Three of triples");
        MAP_EN.put(INSTANT_LOSE, "Instant lose ");
        MAP_EN.put(TEN_CARDS_SAME_COLOR, "Ten cards same color");
        MAP_EN.put(FOUR_2, "Four of 2s ");
        MAP_EN.put(BONUS, "Bonus ");
        MAP_EN.put(LOSE, "Lose ");
        MAP_EN.put(XAM_FAIL, "Sam fails.");
        MAP_EN.put(XAM_SUCCESS, "Sam successes.");
        MAP_EN.put(FORCE_FINISH, "Instant win");
        MAP_EN.put(FORCE_FINISH_LOSE, "Instant lose");
        MAP_EN.put(BONUS_INSTANT_LOSE, "Perfect Win");
        MAP_EN.put(NOT_ALLOW_HEO, "2s card cannot be played in final round.");
        MAP_EN.put(COMPENSATE, "compensate ");
        MAP_EN.put(DEFEAT_XAM, "Sam breaker");
        MAP_EN.put(DOUBLE_FOUR_OF_A_KIND, "Double four of a kind ");
        MAP.put(new Locale(GlobalsUtil.ENGLISH), MAP_EN);
        
        MAP_CHINESE.put(TWO, "2");
        MAP_CHINESE.put(PAIR, "1对 ");
        MAP_CHINESE.put(FOUR_OF_A_KIND, "四条 ");
        MAP_CHINESE.put(UNUSED, "没出过牌 ");
        MAP_CHINESE.put(DEFEAT, "砍");
        MAP_CHINESE.put(STRAIGHT_DRAGON, "一条龙");
        MAP_CHINESE.put(FIVE_PAIR, "5对牌 ");
        MAP_CHINESE.put(THREE_TRIPLES, "3张三条");
        MAP_CHINESE.put(INSTANT_LOSE, "输光猪 ");
        MAP_CHINESE.put(TEN_CARDS_SAME_COLOR, "10张同颜色牌");
        MAP_CHINESE.put(FOUR_2, "四条2 ");
        MAP_CHINESE.put(BONUS, "赏 ");
        MAP_CHINESE.put(LOSE, "输 ");
        MAP_CHINESE.put(XAM_FAIL, "Sam失败.");
        MAP_CHINESE.put(XAM_SUCCESS, "Sam成功.");
        MAP_CHINESE.put(FORCE_FINISH, "大赢 ");
        MAP_CHINESE.put(FORCE_FINISH_LOSE, "大输");
        MAP_CHINESE.put(BONUS_INSTANT_LOSE, "赢三家");
        MAP_CHINESE.put(NOT_ALLOW_HEO, "2不能在最后一轮打出。");
        MAP_CHINESE.put(COMPENSATE, "赔偿 ");
        MAP_CHINESE.put(DEFEAT_XAM, "阻挡sam.");
        MAP_CHINESE.put(DOUBLE_FOUR_OF_A_KIND, "2个四条 ");
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
