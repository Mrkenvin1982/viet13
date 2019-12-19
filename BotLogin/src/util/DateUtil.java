/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import org.apache.log4j.Logger;

/**
 *
 * @author hanv
 */
public class DateUtil {
    private static final Logger LOG = Logger.getLogger(DateUtil.class);
    
    /**
     * Convert ngay hien tai thanh string voi format yyyyMMdd
     * @return 
     */
    public static String getDateString() {
        Calendar c = Calendar.getInstance();
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
        return format.format(c.getTime());
    }
    
    /**
     * Convert ngay hom qua thanh string voi format yyyyMMdd
     * @return 
     */
    public static String getYesterdayString() {
        Calendar c = Calendar.getInstance();
        c.add(Calendar.DATE, -1);
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
        return format.format(c.getTime());
    }
    
    /**
     * Convert ngay truyen vao thanh string theo format pattern
     * @param c
     * @param pattern
     * @return 
     */
    public static String getDateString(Calendar c, String pattern) {
        SimpleDateFormat format = new SimpleDateFormat(pattern);
        return format.format(c.getTime());
    }
    
    /**
     * Convert ngay truyen vao thanh string theo format pattern
     * @param date
     * @param pattern
     * @return 
     */
    public static String getDateString(Date date, String pattern) {
        SimpleDateFormat format = new SimpleDateFormat(pattern);
        return format.format(date);
    }
    
    /**
     * Parse string theo format pattern thành Date.
     * @param dateString
     * @param formatPattern
     * @return 
     */
    public static Date parseString(String dateString, String formatPattern) {
        try {
            SimpleDateFormat format = new SimpleDateFormat(formatPattern);
            return format.parse(dateString);
        } catch (ParseException e) {
            LOG.error("Parse Date exceptionL", e);
        }
        return null;
    }
    
    /**
     * parse date string yyyyMMdd to date object
     * 
     * @param dateString
     * @return null if error
     */
    public static Date parseString(String dateString){
        try {
            SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
            return format.parse(dateString);
        } catch (ParseException e) {
            LOG.error("Parse Date exceptionL", e);
        }
        return null;
    }
    
    /**
     * Lay thoi diem cuoi cua 1 ngay
     * @param date
     * @return 
     */
    public static Date getEndOfDay(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);
        return calendar.getTime();
    }
    
    /**
     * Lay thoi diem cuoi cua 1 ngay
     * 
     * @param date
     * @return 
     */
    public static Date getEndOfDay(String date) {
        return getEndOfDay(parseString(date, "yyyyMMdd"));
    }

    /**
     * Lay thoi diem bat dau cua 1 ngay
     * @param date
     * @return 
     */
    public static Date getStartOfDay(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }
    
    public static Date parseFullString(String dateString) {
        try {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
            return format.parse(dateString);
        } catch (ParseException e) {
            LOG.error("Parse Date exceptionL", e);
        }
        return null;
    }
    
    public static long getDateDiff(Date date1, Date date2, TimeUnit timeUnit) {
        long diffInMillies = date2.getTime() - date1.getTime();
        return timeUnit.convert(diffInMillies, TimeUnit.MILLISECONDS);
    }
    
    public static Date getDate(Date date, String pattern) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(pattern);
            String dateHour = sdf.format(date);
            return sdf.parse(dateHour);
        } catch (ParseException e) {
        }
        return null;
    }

}
