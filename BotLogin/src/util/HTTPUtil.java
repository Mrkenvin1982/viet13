/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package util;

import com.google.gson.JsonObject;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sfs2x.client.util.PasswordUtil;

/**
 *
 * @author hanv
 */
public class HTTPUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(HTTPUtil.class);
    public static final String API_SECRET_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9";
    public static final String API_ENV = "sandbox";
    public static final int API_CONNECTION_ID = 1;

    public static String request(String url, String params) {
        String checksum = PasswordUtil.MD5Password(params + API_SECRET_KEY);
        JsonObject json = new JsonObject();
        json.addProperty("c", API_CONNECTION_ID);
        json.addProperty("env", API_ENV);
        json.addProperty("checksum", checksum);
        String authenticate = Utils.base64Encode(json.toString());

        Map headers = new HashMap();
        headers.put("content-type", "application/json");
        headers.put("Accept", "application/json");
        headers.put("authenticate", authenticate);

        try {
            Document doc = Jsoup.connect(url).timeout(20000).ignoreContentType(true).headers(headers).requestBody(params).post();
            String response = doc.body().text();
            LOGGER.info(url + "?" + params + "\t" + response);
            return response;
        } catch (IOException e) {
            LOGGER.error("Error reading URL content: " + url + "?" + params, e);
        }
        return null;
    }
    
    public static String request(String url, String params, String accessToken) {
        String checksum = PasswordUtil.MD5Password(params + API_SECRET_KEY);
        JsonObject json = new JsonObject();
        json.addProperty("c", API_CONNECTION_ID);
        json.addProperty("env", API_ENV);
        json.addProperty("checksum", checksum);
        String authenticate = Utils.base64Encode(json.toString());
        
        Map headers = new HashMap();
        headers.put("content-type", "application/json");
        headers.put("accept", "application/json");
        headers.put("authenticate", authenticate);
        headers.put("Authorization", accessToken);

        try {
            Document doc = Jsoup.connect(url).timeout(20000).ignoreContentType(true).headers(headers).requestBody(params).post();
            String response = doc.body().text();
            LOGGER.info(url + "?" + params + "\t" + response);
            return response;
        } catch (IOException e) {
            LOGGER.error("Error reading URL content: " + url + "?" + params, e);
        }
        return null;
    }

    public static String request(String url) {

        try {
            Document doc = Jsoup.connect(url).timeout(20000).validateTLSCertificates(false).ignoreContentType(true).get();
            String response = doc.body().text();
            LOGGER.info(url + "\t" + response);
            return response;
        } catch (IOException e) {
            LOGGER.error("Error reading URL content: " + url, e);
        }
        return null;
    }

}