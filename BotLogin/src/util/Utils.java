/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package util;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.util.Random;
import org.apache.commons.codec.binary.Base64;
import sfs2x.client.entities.User;

/**
 *
 * @author hanv
 */
public class Utils {
    private static final JsonParser PARSER = new JsonParser();
    private static final Random RANDOM = new Random();
    
    public static String base64Encode(String raw) {
        byte[] rawBytes = raw.getBytes();
        String base64 = new Base64().encodeToString(rawBytes);
        return base64;
    }
    
    public static JsonObject parse(String s) {
        return PARSER.parse(s).getAsJsonObject();
    }
    
    public static int nextInt() {
        return RANDOM.nextInt();
    }
    
    public static int nextInt(int bound) {
        return RANDOM.nextInt(bound);
    }
    
    public static int nextInt(int from, int to) {
        return from + RANDOM.nextInt(to - from);
    }
    
    public static long nextLong() {
        return RANDOM.nextLong();
    }

    public static boolean nextBoolean() {
        return RANDOM.nextBoolean();
    }
    
    public static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ex) {
        }
    }
    
    public static void sleepRandom(int millis) {
        try {
            Thread.sleep(nextInt(millis));
        } catch (InterruptedException ex) {
        }
    }
    
    public static void sleepRandom(int from, int to) {
        try {
            Thread.sleep(from + nextInt(to - from));
        } catch (InterruptedException ex) {
        }
    }

    public static boolean isBot(User user) {
        if (user != null && user.containsVariable("userType")) {
            int userType = user.getVariable("userType").getIntValue();
            return userType > 0;
        }
        return false;
    }
}
