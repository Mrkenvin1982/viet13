/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package util;

/**
 *
 * @author hanv
 */
public class MauBinhConfig {
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
}
