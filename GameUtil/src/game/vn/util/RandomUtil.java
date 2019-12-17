/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.vn.util;

import java.security.SecureRandom;

/**
 *
 * @author binhnt
 */
public class RandomUtil {

    private static final String AZ09 = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String AZaz09 = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    private static SecureRandom rnd = new SecureRandom();

    public static String randomUpperString(int len) {
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) {
            sb.append(AZ09.charAt(rnd.nextInt(AZ09.length())));
        }

        return sb.toString();
    }

    public static String randomString(int len) {
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) {
            sb.append(AZaz09.charAt(rnd.nextInt(AZaz09.length())));
        }

        return sb.toString();
    }

    public static String getBase62Number(int number, int len) {
        if (AZaz09.length() < 62) {
            return "";
        }

        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) {
            sb.insert(0, AZaz09.charAt(number % 62));
            number = number / 62;
        }

        return sb.toString();
    }
}
