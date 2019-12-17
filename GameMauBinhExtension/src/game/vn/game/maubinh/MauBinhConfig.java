/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package game.vn.game.maubinh;

import game.vn.util.watchservice.PropertyConfigurator;

/**
 *
 * @author binhnt
 */
public class MauBinhConfig extends PropertyConfigurator {
    
    public static final String CONFIG_FILE_PATH = "conf/";
    public static final String CONFIG_FILE_NAME = "maubinh.properties";

    public static final int DEFAULT_NUMBER_CARD = 13;
    public static final int DEFAULT_NUMBER_PLAYER = 4;
    public static final int MIN_NUMBER_PLAYER = 2;
    public static final int MAX_NUMBER_PLAYER = 4;
    public static final int DEFAULT_NUMBER_TYPE = 4; // Heart, diamond, club, spade.
    
    public static final int NUMBER_CARD_SMALL_SET = 3;
    public static final int NUMBER_CARD_BIG_SET = 5;

    public static final int ONE_SECOND = 1000;

    public static final int RESULT_WIN = 1;
    public static final int RESULT_DRAW = 0;
    public static final int RESULT_LOSE = -1;
    public static final int RESULT_ERROR = Integer.MIN_VALUE;

    public static final int STATUS_WAIT = -1;
    public static final int STATUS_NOT_FINISH = 0;
    public static final int STATUS_FINISH = 1;
    
    public static final int FAILED_ARRANGEMENT = -2;

    public static final int CHI_DEFAULT = 1;
    
    public static final int TIME_LIMIT_TYPE_DEFAULT = 1;
    public static final int TIME_LIMIT_TYPE_SLOW = 0;
    public static final int TIME_LIMIT_TYPE_FAST = 2;

    public static final int XP_WIN = 3;
    public static final int XP_DRAW = 2;
    public static final int XP_LOSE = 1;
    public static final int XP_LEAVE = 0;
    
    public static final String AUTO_BINHDAN_PRICE = "game.maubinh.AutoArrangement.BinhDanPrice";
    //thời gian tự động bắt đầu (ms)
    public static final String COMPARE_TIME = "game.maubinh.compareTime";
    
    public static final String MAX_LOSE_CHI = "game.maubinh.MaxLoseChi";
    // Add to remaining players if one player leaves from game.
    public static final String CHI_LEAVE_BONUS = "game.maubinh.ChiLeaveBonus";
    
    public static final String CHI_LAST_FOUR_OF_KIND = "game.maubinh.ChiLastFourOfKind";
    public static final String CHI_LAST_FOUR_OF_KIND_ACE = "game.maubinh.ChiLastFourOfKindAce";
    public static final String CHI_LAST_STRAIGHT_FLUSH = "game.maubinh.ChiLastStraightFlush";
    public static final String CHI_LAST_STRAIGHT_FLUSH_A2345 = "game.maubinh.ChiLastStraightFlushA2345";
    public static final String CHI_LAST_STRAIGHT_FLUSH_10JQKA = "game.maubinh.ChiLastStraightFlush10JQKA";
    public static final String CHI_MIDDLE_FOUR_OF_KIND = "game.maubinh.ChiMiddleFourOfKind";
    public static final String CHI_MIDDLE_FOUR_OF_KIND_ACE = "game.maubinh.ChiMiddleFourOfKindAce";
    public static final String CHI_MIDDLE_STRAIGHT_FLUSH = "game.maubinh.ChiMiddleStraightFlush";
    public static final String CHI_MIDDLE_STRAIGHT_FLUSH_A2345 = "game.maubinh.ChiMiddleStraightFlushA2345";
    public static final String CHI_MIDDLE_STRAIGHT_FLUSH_10JQKA = "game.maubinh.ChiMiddleStraightFlush10JQKA";
    public static final String CHI_MIDDLE_FULL_HOUSE = "game.maubinh.ChiMiddleFullHouse";
    public static final String CHI_FIRST_THREE_OF_KIND = "game.maubinh.ChiFirstThreeOfKind";
    public static final String CHI_FIRST_THREE_OF_KIND_ACE = "game.maubinh.ChiFirstThreeOfKindAce";

    public static final String CHI_MAUBINH_SIX_PAIR = "game.maubinh.ChiMauBinhSixPair";
    public static final String CHI_MAUBINH_THREE_STRAIGHT = "game.maubinh.ChiMauBinhThreeStraight";
    public static final String CHI_MAUBINH_THREE_FLUSH = "game.maubinh.ChiMauBinhThreeFlush";
    public static final String CHI_MAUBINH_SAME_COLOR_12 = "game.maubinh.ChiMauBinhSameColor12";
    public static final String CHI_MAUBINH_SIX_PAIR_WITH_THREE = "game.maubinh.ChiMauBinhSixPairWithThree";
    public static final String CHI_MAUBINH_STRAIGHT_13 = "game.maubinh.ChiMauBinhStraight13";
    public static final String CHI_MAUBINH_FOUR_OF_THREE = "game.maubinh.ChiMauBinhFourOfThree";
    public static final String CHI_MAUBINH_SAME_COLOR_13 = "game.maubinh.ChiMauBinhSameColor13";

    public static final String CHI_WIN_THREE_SET_RATE = "game.maubinh.ChiWinThreeSetRate";
    public static final String CHI_WIN_ALL_BY_THREE_SET_RATE = "game.maubinh.ChiWinAllByThreeSetRate";
    public static final String TYPE_CACULATE_MONEY = "game.maubinh.isCaculateMoneyOld";
    private static final String MAX_VIEWER= "game.maubinh.MaxViewer";

    private volatile static MauBinhConfig INSTANCE;

    /**
     * Private Constructor.
     */
    private MauBinhConfig() {
        super(CONFIG_FILE_PATH, CONFIG_FILE_NAME);
    }

    /**
     * Singleton class. Get an instance of MauBinhConfig.
     *
     * @return An instance of ServerInfo.
     */
    public static MauBinhConfig getInstance() {
        if (INSTANCE == null) {
            synchronized (MauBinhConfig.class) {
                if (INSTANCE == null) {
                    INSTANCE = new MauBinhConfig();
                }
            }
        }
        
        return INSTANCE;
    }
    
    public int getMaxLoseChi() {
        return this.getIntAttribute(MauBinhConfig.MAX_LOSE_CHI, 192);
    }
    
    public int getChiLeaveBonus() {
        return this.getIntAttribute(MauBinhConfig.CHI_LEAVE_BONUS, 30);
    }
    
    public int getChiWinThreeSetRate() {
        return this.getIntAttribute(MauBinhConfig.CHI_WIN_THREE_SET_RATE, 2);
    }
    
    public int getChiWinAllByThreeSetRate() {
        return this.getIntAttribute(MauBinhConfig.CHI_WIN_ALL_BY_THREE_SET_RATE, 4);
    }

    public int getChiLastFourOfKind() {
        return this.getIntAttribute(MauBinhConfig.CHI_LAST_FOUR_OF_KIND, 4);
    }

    public int getChiLastFourOfKindAce() {
        return this.getIntAttribute(MauBinhConfig.CHI_LAST_FOUR_OF_KIND_ACE, 12);
    }

    public int getChiLastStraightFlush() {
        return this.getIntAttribute(MauBinhConfig.CHI_LAST_STRAIGHT_FLUSH, 5);
    }

    public int getChiLastStraightFlushA2345() {
        return this.getIntAttribute(MauBinhConfig.CHI_LAST_STRAIGHT_FLUSH_A2345, 7);
    }

    public int getChiLastStraightFlush10JQKA() {
        return this.getIntAttribute(MauBinhConfig.CHI_LAST_STRAIGHT_FLUSH_10JQKA, 10);
    }

    public int getChiMiddleFourOfKind() {
        return this.getIntAttribute(MauBinhConfig.CHI_MIDDLE_FOUR_OF_KIND, 8);
    }

    public int getChiMiddleFourOfKindAce() {
        return this.getIntAttribute(MauBinhConfig.CHI_MIDDLE_FOUR_OF_KIND_ACE, 24);
    }

    public int getChiMiddleStraightFlush() {
        return this.getIntAttribute(MauBinhConfig.CHI_MIDDLE_STRAIGHT_FLUSH, 10);
    }

    public int getChiMiddleStraightFlushA2345() {
        return this.getIntAttribute(MauBinhConfig.CHI_MIDDLE_STRAIGHT_FLUSH_A2345, 14);
    }

    public int getChiMiddleStraightFlush10JQKA() {
        return this.getIntAttribute(MauBinhConfig.CHI_MIDDLE_STRAIGHT_FLUSH_10JQKA, 10);
    }

    public int getChiMiddleFullHouse() {
        return this.getIntAttribute(MauBinhConfig.CHI_MIDDLE_FULL_HOUSE, 2);
    }
    
    public int getChiFirstThreeOfKind() {
        return this.getIntAttribute(MauBinhConfig.CHI_FIRST_THREE_OF_KIND, 3);
    }
    
    public int getChiFirstThreeOfKindAce() {
        return this.getIntAttribute(MauBinhConfig.CHI_FIRST_THREE_OF_KIND_ACE, 9);
    }

    public int getChiMauBinhSixPair() {
        return this.getIntAttribute(MauBinhConfig.CHI_MAUBINH_SIX_PAIR, 6);
    }

    public int getChiMauBinhThreeStraight() {
        return this.getIntAttribute(MauBinhConfig.CHI_MAUBINH_THREE_STRAIGHT, 6);
    }

    public int getChiMauBinhThreeFlush() {
        return this.getIntAttribute(MauBinhConfig.CHI_MAUBINH_THREE_FLUSH, 6);
    }

    public int getChiMauBinhSameColor12() {
        return this.getIntAttribute(MauBinhConfig.CHI_MAUBINH_SAME_COLOR_12, 6);
    }

    public int getChiMauBinhSixPairWithThree() {
        return this.getIntAttribute(MauBinhConfig.CHI_MAUBINH_SIX_PAIR_WITH_THREE, 8);
    }
    
    public int getChiMauBinhStraight13() {
        return this.getIntAttribute(MauBinhConfig.CHI_MAUBINH_STRAIGHT_13, 13);
    }
    
    public int getChiMauBinhFourOfThree() {
        return this.getIntAttribute(MauBinhConfig.CHI_MAUBINH_FOUR_OF_THREE, 25);
    }

    public int getChiMauBinhSameColor13() {
        return this.getIntAttribute(MauBinhConfig.CHI_MAUBINH_SAME_COLOR_13, 26);
    }

    public int getAutoArrangementBinhDanPrice() {
        return this.getIntAttribute(MauBinhConfig.AUTO_BINHDAN_PRICE,0);
    }

    public int getCompareTime() {
        return getIntAttribute(COMPARE_TIME, 10000);
    }

    /**
     * số viewer tối đa cho game 
     *
     * @return
     */
    public int getMaxViewer() {
        return getIntAttribute(MAX_VIEWER, 0);
    }
    public boolean isTest() {
        return getBooleanAttribute("IS_TEST");
    }

    int getTestCase() {
        return getIntAttribute("TESTCASE", 0);
    }
    
    public int getIntLastSet(){
         return getIntAttribute("lastset", 1);
    }
    
    public int getIntMiddleSet(){
         return getIntAttribute("middleset", 1);
    }
    
    public int getIntSmallSet(){
         return getIntAttribute("smallset", 1);
    }
}
