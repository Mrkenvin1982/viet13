/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.vn.util;

import java.util.Calendar;

/**
 *
 * @author binhnt
 */
public class NameUtil {
    public static String GetBoardName() {
        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR) - 2000;
        int month = cal.get(Calendar.MONTH) + 1;
        int day = cal.get(Calendar.DAY_OF_MONTH);
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        int minute = cal.get(Calendar.MINUTE);
        int second = cal.get(Calendar.SECOND);
        int milisecond = cal.get(Calendar.MILLISECOND);
        return String.format("%s%s%s%s%s%s%s%s",
                RandomUtil.getBase62Number(year, 1),       // YEAR:         17 --> H
                RandomUtil.getBase62Number(month, 1),      // MONTH:        03 --> 3
                RandomUtil.getBase62Number(day, 1),        // DAY:          01 --> 1
                RandomUtil.getBase62Number(hour, 1),       // HOUR:         08 --> 8
                RandomUtil.getBase62Number(minute, 1),     // MINUTE:       51 --> q
                RandomUtil.getBase62Number(second, 1),     // SECOND:       30 --> U
                RandomUtil.getBase62Number(milisecond, 2), // MILISECOND:  621 --> A1
                RandomUtil.randomString(2)
                );
    }

    public static String GetBoardName(String prefix) {
        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR) - 2000;
        int month = cal.get(Calendar.MONTH) + 1;
        int day = cal.get(Calendar.DAY_OF_MONTH);
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        int minute = cal.get(Calendar.MINUTE);
        int second = cal.get(Calendar.SECOND);
        int milisecond = cal.get(Calendar.MILLISECOND);
        String processPrefix;
        if (prefix == null || prefix.isEmpty()) {
            processPrefix = "__";
        } else if (prefix.length() == 1) {
            processPrefix = "_" + prefix;
        } else {
            processPrefix = prefix.substring(0, 2);
        }

        return String.format("%s%s%s%s%s%s%s%s",
                processPrefix,
                RandomUtil.getBase62Number(year, 1),      // YEAR:         17 --> H
                RandomUtil.getBase62Number(month, 1),     // MONTH:        03 --> 3
                RandomUtil.getBase62Number(day, 1),       // DAY:          01 --> 1
                RandomUtil.getBase62Number(hour, 1),      // HOUR:         08 --> 8
                RandomUtil.getBase62Number(minute, 1),    // MINUTE:       51 --> q
                RandomUtil.getBase62Number(second, 1),    // SECOND:       30 --> U
                RandomUtil.getBase62Number(milisecond, 2) // MILISECOND:  621 --> A1
                );
    }
    
    public static String GetBoardNameLength10(String prefix) {
        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR) - 2000;
        int month = cal.get(Calendar.MONTH) + 1;
        int day = cal.get(Calendar.DAY_OF_MONTH);
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        int minute = cal.get(Calendar.MINUTE);
        int second = cal.get(Calendar.SECOND);
        int milisecond = cal.get(Calendar.MILLISECOND);

        return String.format("%s_%s%s%s%s%s%s%s%s",
                prefix,
                RandomUtil.getBase62Number(year, 1),      // YEAR:         17 --> H
                RandomUtil.getBase62Number(month, 1),     // MONTH:        03 --> 3
                RandomUtil.getBase62Number(day, 1),       // DAY:          01 --> 1
                RandomUtil.getBase62Number(hour, 1),      // HOUR:         08 --> 8
                RandomUtil.getBase62Number(minute, 1),    // MINUTE:       51 --> q
                RandomUtil.getBase62Number(second, 1),    // SECOND:       30 --> U
                RandomUtil.getBase62Number(milisecond, 2), // MILISECOND:  621 --> A1
                RandomUtil.randomString(3)
                );
    }
}
