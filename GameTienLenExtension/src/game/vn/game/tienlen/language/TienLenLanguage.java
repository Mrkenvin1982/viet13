/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package game.vn.game.tienlen.language;

import game.vn.util.GlobalsUtil;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 *
 * @author 
 */
public class TienLenLanguage {

    public static final String BLACK_TWO = "blackTwo";
    public static final String RED_TWO = "redTwo";
    public static final String THREE_PAIRS_SEQ = "threePairsCont";
    public static final String FOUR_PAIRS_SEQ = "fourPairsCont";
    public static final String FOUR_OF_A_KIND = "fourOfAKind";
    public static final String UNUSED = "unused";
    public static final String UNUSED_UPPERCASE = "unusedUppercase";
    public static final String DEFEAT = "defeat";
    public static final String DEFEATED="defeated";
    public static final String STRAIGHT_DRAGON = "straightDragon";
    public static final String FIVE_PAIRS_SEQ = "fivePairsCont";
    public static final String SIX_PAIR = "sixPairs";
    public static final String FOUR_TWOCARDS = "fourTwos";
    public static final String FOUR_THREECARDS = "fourThrees";
    public static final String FOUR_TRIPLES = "fourTriples";
    public static final String INSTANT_LOSE = "instantLose";
    public static final String BONUS = "bonus";
    public static final String LOST = "lost";
    public static final String MOVE_ERROR_MESSAGE = "moveError";
    public static final String FORCE_FINISH = "forceFinish";
    public static final String NOT_ENOUGH_WIN = "notEnoughWin";
    public static final String COMPENSATE = "compensate";
    public static final String WIN = "win";
    public static final String THREE_PAIRS_CONT_WITH_3SPADE="THREE_PAIRS_CONT_WITH_3SPADE";
    public static final String INSTANT_WIN= "instantWin";
    public static final String INVALID_CARD = "invalidCard";
    public static final String LEAVE_ROOM = "leave";
    public static final String WIN3SPADES = "win3Spades";
    
    //key: message key - value: message content
    private static final Map<String, String> MAP_VI = new HashMap<>();
    private static final Map<String, String> MAP_EN = new HashMap<>();
    private static final Map<String, String> MAP_CHINESE = new HashMap<>();
    private static final Map<Locale, Map<String, String>> MAP = new HashMap<>();
    
    static {
        MAP_VI.put(BLACK_TWO, " heo đen");
        MAP_VI.put(RED_TWO, " heo đỏ");
        MAP_VI.put(THREE_PAIRS_SEQ, "3 đôi thông");
        MAP_VI.put(FOUR_PAIRS_SEQ, "4 đôi thông");
        MAP_VI.put(FOUR_OF_A_KIND, " tứ quý");
        MAP_VI.put(UNUSED, "úng");
        MAP_VI.put(UNUSED_UPPERCASE, "Úng");
        MAP_VI.put(DEFEAT, "Chặt");
        MAP_VI.put(DEFEATED, "Bị chặt ");
        MAP_VI.put(STRAIGHT_DRAGON, "Sảnh rồng");
        MAP_VI.put(FIVE_PAIRS_SEQ, "5 đôi thông");
        MAP_VI.put(SIX_PAIR, "Sáu đôi");
        MAP_VI.put(FOUR_TWOCARDS, "Tứ quý heo");
        MAP_VI.put(FOUR_THREECARDS, "Tứ quý 3");
        MAP_VI.put(FOUR_TRIPLES, "4 sám cô");
        MAP_VI.put(INSTANT_LOSE, " thua cóng");
        MAP_VI.put(BONUS, "Thưởng");
        MAP_VI.put(LOST, "Phạt");
        MAP_VI.put(MOVE_ERROR_MESSAGE, "Bạn phải đánh bài nhỏ nhất.");
        MAP_VI.put(FORCE_FINISH, "Tới trắng ");
        MAP_VI.put(NOT_ENOUGH_WIN, "Bạn phải có %s gấp %d lần tiền cược.");
        MAP_VI.put(COMPENSATE, "Đền bài");
        MAP_VI.put(WIN, "Tới");
        MAP_VI.put(THREE_PAIRS_CONT_WITH_3SPADE, "3 đôi thông có 3 bích");
        MAP_VI.put(INSTANT_WIN, " Ăn cóng");
        MAP_VI.put(INVALID_CARD, "Bài không hợp lệ ");
        MAP_VI.put(LEAVE_ROOM, "Rời bàn chơi.");
        MAP_VI.put(WIN3SPADES, "3 bích");
        MAP.put(new Locale(GlobalsUtil.VIETNAMESE), MAP_VI);

        MAP_EN.put(BLACK_TWO, " black Two");
        MAP_EN.put(RED_TWO, " red Two");
        MAP_EN.put(THREE_PAIRS_SEQ, "Three consecutive pairs");
        MAP_EN.put(FOUR_PAIRS_SEQ, "Four of 2s");
        MAP_EN.put(FOUR_OF_A_KIND, " four of a kind");
        MAP_EN.put(UNUSED, "wasted");
        MAP_EN.put(UNUSED_UPPERCASE, "Wasted");
        MAP_EN.put(DEFEAT, "Busted");
        MAP_EN.put(DEFEATED, "Being busted ");
        MAP_EN.put(STRAIGHT_DRAGON, "Highest straight");
        MAP_EN.put(FIVE_PAIRS_SEQ, "ive consecutive pairs");
        MAP_EN.put(SIX_PAIR, "6 pairs");
        MAP_EN.put(FOUR_TWOCARDS, "Four of 2s");
        MAP_EN.put(FOUR_THREECARDS, "Four of 3s");
        MAP_EN.put(FOUR_TRIPLES, "Four of triples");
        MAP_EN.put(INSTANT_LOSE, " instant lose");
        MAP_EN.put(BONUS, "Bonus");
        MAP_EN.put(LOST, "Lost");
        MAP_EN.put(MOVE_ERROR_MESSAGE, "You must play the lowest card.");
        MAP_EN.put(FORCE_FINISH, "Instant win ");
        MAP_EN.put(NOT_ENOUGH_WIN, "You must have %s by %d times minimum bet.");
        MAP_EN.put(COMPENSATE, "Compensate ");
        MAP_EN.put(WIN, "Win");
        MAP_EN.put(THREE_PAIRS_CONT_WITH_3SPADE, "Three consecutive pairs with 3s spade");
        MAP_EN.put(INSTANT_WIN, " Perfect win");
        MAP_EN.put(INVALID_CARD, "Can't play card.");
        MAP_EN.put(LEAVE_ROOM, "Leave table.");
        MAP_EN.put(WIN3SPADES, "Three spades");
        MAP.put(new Locale(GlobalsUtil.ENGLISH), MAP_EN);
        
        MAP_CHINESE.put(BLACK_TWO, " 黑2");
        MAP_CHINESE.put(RED_TWO, " 红2");
        MAP_CHINESE.put(THREE_PAIRS_SEQ, "3对连续牌");
        MAP_CHINESE.put(FOUR_PAIRS_SEQ, "4对连续牌");
        MAP_CHINESE.put(FOUR_OF_A_KIND, " 4条");
        MAP_CHINESE.put(UNUSED, "没出过牌 ");
        MAP_CHINESE.put(UNUSED_UPPERCASE, "没出过牌");
        MAP_CHINESE.put(DEFEAT, "砍");
        MAP_CHINESE.put(DEFEATED, "被砍 ");
        MAP_CHINESE.put(STRAIGHT_DRAGON, "一条龙");
        MAP_CHINESE.put(FIVE_PAIRS_SEQ, "5对连续牌");
        MAP_CHINESE.put(SIX_PAIR, "6对牌");
        MAP_CHINESE.put(FOUR_TWOCARDS, "4条2");
        MAP_CHINESE.put(FOUR_THREECARDS, "4条3");
        MAP_CHINESE.put(FOUR_TRIPLES, "4个3条");
        MAP_CHINESE.put(INSTANT_LOSE, " 输光猪");
        MAP_CHINESE.put(BONUS, "赏");
        MAP_CHINESE.put(LOST, "罚");
        MAP_CHINESE.put(MOVE_ERROR_MESSAGE, "您需要打出最小的。");
        MAP_CHINESE.put(FORCE_FINISH, "大赢 ");
        MAP_CHINESE.put(NOT_ENOUGH_WIN, "您需要 %s 比押金多 %d 倍。");
        MAP_CHINESE.put(COMPENSATE, "赔偿 ");
        MAP_CHINESE.put(WIN, "赢");
        MAP_CHINESE.put(THREE_PAIRS_CONT_WITH_3SPADE, "3对连续牌带有黑土3");
        MAP_CHINESE.put(INSTANT_WIN, "  赢三家");
        MAP_CHINESE.put(INVALID_CARD, "牌子无效");
        MAP_CHINESE.put(LEAVE_ROOM, "离开桌子。");
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
