/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.vn.util;

import java.text.NumberFormat;

/**
 *
 * @author tuanp
 */
public class FormatUtil {

    /**
     * format number by GlobalsUtil.DEFAULT_LOCALE
     *
     * @param number 10000
     * @return 10.000
     */
    public static String formatNumber(Object number) {
        return NumberFormat.getInstance(GlobalsUtil.DEFAULT_LOCALE).format(number);
    }

}
