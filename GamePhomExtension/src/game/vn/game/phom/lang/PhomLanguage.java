/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.vn.game.phom.lang;

import game.vn.util.GlobalsUtil;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 *
 * @author 
 */
public class PhomLanguage {

    
    public static final String RUMMY = "rummy";//Ù
    public static final String LOSE_RUMMY = "loseRummy"; // Thua Ù
    public static final String RUMMY_KHAN = "rummyKhan";//Ù khan
    public static final String LOSE_RUMMY_KHAN = "loseRummyKhan"; // Thua Ù khan
    public static final String HIT_COTINUOUSLY = "hitContinuously"; // Thắng ù đền
    public static final String BEING_HIT_CONTINUOUSLY = "beingHitContinuously";//Ù đền
    public static final String LAY_OFF_RND_1 = "layOffRound1"; // Ăn gà lượt 1
    public static final String LAY_OFF_RND_2 = "layOffRound2"; // Ăn gà lượt 2
    public static final String LAY_OFF_RND_3 = "layOffRound3"; // Ăn gà lượt 3
    public static final String HITTING_A_CARD = "anChot"; // Ăn chốt
    public static final String LAY_DOWN_NOT_SUCCESS = "lay_down_not_success";
    public static final String INVALID_CARDS = "invalid_cards";
    public static final String LAY_OFF_NOT_SUCCESS = "lay_off_not_success";
    public static final String CANNOT_GET_CARD = "cannotGetCard";
    public static final String LAID_OFF_RND_1 = "laidOffRound1"; // Bị ăn gà lượt 1
    public static final String LAID_OFF_RND_2 = "laidOffRound2"; // Bị ăn gà lượt 2
    public static final String LAID_OFF_RND_3 = "laidOffRound3"; // Bị ăn gà lượt 3
    public static final String BEING_HIT = "biAnChot"; // Bị ăn chốt
    public static final String CANNOT_SEND_CARD = "canNotSendCard";
    public static final String EMPTY_PHOM = "emptyPhom";
        
    //key: message key - value: message content
    private static final Map<String, String> MAP_VI = new HashMap<>();
    private static final Map<String, String> MAP_EN = new HashMap<>();
    private static final Map<String, String> MAP_CHINESE = new HashMap<>();
    private static final Map<Locale, Map<String, String>> MAP = new HashMap<>();    
    static {
        MAP_VI.put(RUMMY, "Ù");
        MAP_VI.put(LOSE_RUMMY, "Thua Ù");
        MAP_VI.put(RUMMY_KHAN, "Ù khan");
        MAP_VI.put(LOSE_RUMMY_KHAN, "Thua Ù Khan");
        MAP_VI.put(HIT_COTINUOUSLY, "Ù");
        MAP_VI.put(BEING_HIT_CONTINUOUSLY, "Ù đền");
        MAP_VI.put(LAY_OFF_RND_1, "Ăn gà lần 1");
        MAP_VI.put(LAY_OFF_RND_2, "Ăn gà lần 2");
        MAP_VI.put(LAY_OFF_RND_3, "Ăn gà lần 3");
        MAP_VI.put(HITTING_A_CARD, "Ăn chốt");
        MAP_VI.put(LAY_DOWN_NOT_SUCCESS, "Hạ phỏm không thành công.");
        MAP_VI.put(INVALID_CARDS, "Đổi phỏm nếu có để ăn bài.");
        MAP_VI.put(LAY_OFF_NOT_SUCCESS, "Đổi phỏm không thành công.");
        MAP_VI.put(CANNOT_GET_CARD, "Không được phép rút bài");
        MAP_VI.put(LAID_OFF_RND_1, "Bị ăn gà lần 1");
        MAP_VI.put(LAID_OFF_RND_2, "Bị ăn gà lần 2");
        MAP_VI.put(LAID_OFF_RND_3, "Bị ăn gà lần 3");
        MAP_VI.put(BEING_HIT, "Bị ăn chốt");
        MAP_VI.put(CANNOT_SEND_CARD, "Không gửi được bài.");
        MAP_VI.put(EMPTY_PHOM, "Cháy.");
        MAP.put(new Locale(GlobalsUtil.VIETNAMESE), MAP_VI);

        MAP_EN.put(RUMMY, "Rummy");
        MAP_EN.put(LOSE_RUMMY, "Rummy lose");
        MAP_EN.put(RUMMY_KHAN, "Khan Rummy");
        MAP_EN.put(LOSE_RUMMY_KHAN, "Khan Rummy lose");
        MAP_EN.put(HIT_COTINUOUSLY, "Rummy");
        MAP_EN.put(BEING_HIT_CONTINUOUSLY, "Compensation Rummy");
        MAP_EN.put(LAY_OFF_RND_1, "Win laying off round 1");
        MAP_EN.put(LAY_OFF_RND_2, "Win laying off round 2");
        MAP_EN.put(LAY_OFF_RND_3, "Win laying off round 3");
        MAP_EN.put(HITTING_A_CARD, "Win last card");
        MAP_EN.put(LAY_DOWN_NOT_SUCCESS, "Cannot meld.");
        MAP_EN.put(INVALID_CARDS, "Invalid meld");
        MAP_EN.put(LAY_OFF_NOT_SUCCESS, "Cannot change meld");
        MAP_EN.put(CANNOT_GET_CARD, "Cannot draw card.");
        MAP_EN.put(LAID_OFF_RND_1, "Lose laying off round 1");
        MAP_EN.put(LAID_OFF_RND_2, "Lose laying off round 2");
        MAP_EN.put(LAID_OFF_RND_3, "Lose laying off round 3");
        MAP_EN.put(BEING_HIT, "Lose last card");
        MAP_EN.put(CANNOT_SEND_CARD, "Cannot send card.");
        MAP_EN.put(EMPTY_PHOM, "Empty meld.");
        MAP.put(new Locale(GlobalsUtil.ENGLISH), MAP_EN);
        
        MAP_CHINESE.put(RUMMY, "湖");
        MAP_CHINESE.put(LOSE_RUMMY, "输湖");
        MAP_CHINESE.put(RUMMY_KHAN, "khan湖");
        MAP_CHINESE.put(LOSE_RUMMY_KHAN, "输Khan湖");
        MAP_CHINESE.put(HIT_COTINUOUSLY, "湖");
        MAP_CHINESE.put(BEING_HIT_CONTINUOUSLY, "赔湖");
        MAP_CHINESE.put(LAY_OFF_RND_1, "吃鸡1次");
        MAP_CHINESE.put(LAY_OFF_RND_2, "吃鸡2次");
        MAP_CHINESE.put(LAY_OFF_RND_3, "吃鸡3次");
        MAP_CHINESE.put(HITTING_A_CARD, "吃最后牌");
        MAP_CHINESE.put(LAY_DOWN_NOT_SUCCESS, "拉米不成功。");
        MAP_CHINESE.put(INVALID_CARDS, "如果吃牌可以换拉米。");
        MAP_CHINESE.put(LAY_OFF_NOT_SUCCESS, "换拉米不成功");
        MAP_CHINESE.put(CANNOT_GET_CARD, "不可以抽牌");
        MAP_CHINESE.put(LAID_OFF_RND_1, "被吃鸡1次");
        MAP_CHINESE.put(LAID_OFF_RND_2, "被吃鸡2次");
        MAP_CHINESE.put(LAID_OFF_RND_3, "被吃鸡3次");
        MAP_CHINESE.put(BEING_HIT, "被吃最后牌");
        MAP_CHINESE.put(CANNOT_SEND_CARD, "不能寄牌");
        MAP_CHINESE.put(EMPTY_PHOM, "输光.");
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
