/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.vn.util;

import java.util.Locale;

/**
 *
 * @author tuanp
 */
public class GlobalsUtil {
    public static final String VIETNAMESE = "vi";
    public static final String ENGLISH = "en";
    public static final String CHINESE = "zh";
    public static final Locale VIETNAMESE_LOCALE = new Locale(VIETNAMESE);
    public static final Locale ENGLISH_LOCALE = new Locale(ENGLISH);
    public static final Locale CHINESE_LOCALE = new Locale(CHINESE);
    public static final Locale DEFAULT_LOCALE = VIETNAMESE_LOCALE;
   

    static {
        Locale.setDefault(DEFAULT_LOCALE);
    }
}
