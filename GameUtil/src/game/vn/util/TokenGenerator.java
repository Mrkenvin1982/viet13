/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.vn.util;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import org.apache.commons.codec.binary.Base64;

/**
 *
 * @author hanv
 */
public class TokenGenerator {
    public static final String BTC_PAYMENT_KEY = "4IOXsYj0vQ3Lfdghsg";

    private static String md5String(String sourceString) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("MD5");
        md.update(sourceString.getBytes());
        byte[] encryptDatas = md.digest();
        String returnData = new BigInteger(1, encryptDatas).toString(16);
        while (returnData.length() < 32) {
            returnData = "0" + returnData;
        }
        return returnData;
    }

    public static String generateToken(String... valueRequests) {
        StringBuilder valueRequest = new StringBuilder();
        for (String value : valueRequests) {
            if (value != null) {
                valueRequest.append(value);
            }
        }

        try {
            return md5String(valueRequest.toString());
        } catch (NoSuchAlgorithmException ex) {
        }
        return null;
    }
    
    public static String generateToken(List<String> valueRequests) {
        StringBuilder valueRequest = new StringBuilder();
        for (String value : valueRequests) {
            if (value != null) {
                valueRequest.append(value);
            }
        }

        try {
            return md5String(valueRequest.toString());
        } catch (NoSuchAlgorithmException ex) {
        }
        return null;
    }
    
    public static String generateBTCPaymentToken(String... valueRequests) {
        StringBuilder valueRequest = new StringBuilder();
        for (String value : valueRequests) {
            if (value != null) {
                valueRequest.append(value);
            }
        }

        try {
            return md5String(valueRequest.toString() + BTC_PAYMENT_KEY);
        } catch (NoSuchAlgorithmException ex) {
        }
        return null;
    }
    
    public static String base64Encode(String raw) {
        byte[] rawBytes = raw.getBytes();
        String base64 = new Base64().encodeToString(rawBytes);
        return base64;
    }
    
    public static String base64Decode(String raw) {
        byte[] base64 = new Base64().decode(raw);
        return new String(base64);
    }
}
