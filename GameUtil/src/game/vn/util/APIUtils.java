/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.vn.util;

import com.google.gson.JsonObject;
import game.vn.common.config.ServerConfig;
import game.vn.common.config.UrlConfig;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.Map;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author tuanp
 */
public class APIUtils {

    private final static Logger LOGGER = LoggerFactory.getLogger(APIUtils.class);

    public static String request(String url, String data) {
        Map headers = new HashMap();
        headers.put("content-type", "application/json");
        headers.put("Accept", "application/json");
        headers.put("Authenticate", "eyJjIjo3LCJjaGVja3N1bSI6ImY4ZWIyNjgwYzgwYjE5ODlmNmFmYWEwOWRlNzNhMzYwIn0=");

        try {
            Document doc = Jsoup.connect(url).timeout(ServerConfig.getInstance().apiTimeoutRequest()).ignoreContentType(true).headers(headers).requestBody(data).post();
            String response = doc.body().text();
            LOGGER.info(url + "?" + data + "\t" + response);
            return response;
        } catch (IOException e) {
            LOGGER.error("Error reading URL content: " + url + "?" + data, e);
        }
        return null;
    }

    public static String requestCharge(String url, String data, String accessToken, String lang){
        Map headers = new HashMap();
        headers.put("content-type", "application/json");
        headers.put("Accept", "application/json");
        headers.put("Authenticate", "eyJjIjo3LCJjaGVja3N1bSI6ImY4ZWIyNjgwYzgwYjE5ODlmNmFmYWEwOWRlNzNhMzYwIn0=");
        headers.put("Authorization", accessToken);
        headers.put("lang", lang);

        try {
            Document doc = Jsoup.connect(url).timeout(ServerConfig.getInstance().apiTimeoutCharge()).ignoreContentType(true).headers(headers).requestBody(data).post();
            String response = doc.body().text();
            LOGGER.info(url + "?" + data + "\t" + response);
            return response;
        } catch (IOException e) {
            LOGGER.error("Error reading URL content: " + url + "?" + data, e);
        }
        return null;
    }

    public static String requestFacebook(String url, String data, String accessToken, String lang) {
        Map headers = new HashMap();
        headers.put("content-type", "application/json");
        headers.put("Accept", "application/json");
        headers.put("Authenticate", "eyJjIjo3LCJjaGVja3N1bSI6ImY4ZWIyNjgwYzgwYjE5ODlmNmFmYWEwOWRlNzNhMzYwIn0=");
        headers.put("Authorization", accessToken);
        headers.put("lang", lang);

        try {
            Document doc = Jsoup.connect(url).timeout(ServerConfig.getInstance().apiTimeoutFacebook()).ignoreContentType(true).headers(headers).requestBody(data).post();
            String response = doc.body().text();
            LOGGER.info(url + "?" + data + "\t" + response);
            return response;
        } catch (IOException e) {
            LOGGER.error("Error reading URL content: " + url + "?" + data, e);
        }
        return null;
    }
    
    public static String requestGetGameListMoon(String url, String data) {
        Map headers = new HashMap();
        headers.put("Content-Type", "application/json");
        headers.put("Accept", "application/json");

        try {
            Document doc = Jsoup.connect(url).timeout(ServerConfig.getInstance().apiTimeoutGetGameListMoon()).ignoreContentType(true).headers(headers).requestBody(data).post();
            String response = doc.body().text();
            LOGGER.info(url + "?" + data + "\t" + response);
            return response;
        } catch (IOException e) {
            LOGGER.error("Error reading URL content: " + url + "?" + data, e);
        }
        return null;
    }
    
    public static String requestGetGameListHandicap(String url, String data) {
        Map headers = new HashMap();
        headers.put("Content-Type", "application/json");
        headers.put("Accept", "application/json");

        try {
            Document doc = Jsoup.connect(url).timeout(ServerConfig.getInstance().apiTimeoutGetGameListHandicap()).ignoreContentType(true).headers(headers).requestBody(data).post();
            String response = doc.body().text();
            LOGGER.info(url + "?" + data + "\t" + response);
            return response;
        } catch (IOException e) {
            LOGGER.error("Error reading URL content: " + url + "?" + data, e);
        }
        return null;
    }

    public static String requestPassportConfig() {
        try {
            String url = UrlConfig.getInstance().getUrlPassportConfig();
            Document doc = Jsoup.connect(url).timeout(ServerConfig.getInstance().apiTimeoutPassportConfig()).ignoreContentType(true).get();
            String response = doc.body().text();
            LOGGER.info(url + "?" + "\t" + response);
            return response;
        } catch (IOException e) {
            LOGGER.error("requestPassportConfig", e);
        }
        return null;
    }
    
    public static String requestPassportPaymentList(String accessToken) {
        Map headers = new HashMap();
        headers.put("content-type", "application/json");
        headers.put("Accept", "application/json");
        headers.put("Authorization", accessToken);
        try {
            String url = UrlConfig.getInstance().getUrlPassportPaymentList();
            Document doc = Jsoup.connect(url).timeout(30000).ignoreContentType(true).headers(headers).get();
            String response = doc.body().text();
            LOGGER.info(url + "?" + "\t" + response);
            return response;
        } catch (IOException e) {
            LOGGER.error("", e);
        }
        return null;
    }

    public static String requestPassportWithdraw(String accessToken, String asset, String address, double amount, String lang) {
        Map headers = new HashMap();
        headers.put("accept", "application/json");
        headers.put("content-type", "application/json");
        headers.put("Authorization", accessToken);
        headers.put("lang", lang);

        JsonObject data = new JsonObject();
        data.addProperty("asset", asset);
        data.addProperty("address", address);
        data.addProperty("amount", amount);
        data.addProperty("tag", "");
        try {
            String url = UrlConfig.getInstance().getUrlPassportWithdraw();
            Document doc = Jsoup.connect(url).timeout(ServerConfig.getInstance().apiTimeoutPassportWithdraw()).ignoreContentType(true).headers(headers).requestBody(data.toString()).post();
            String response = doc.body().text();
            LOGGER.info(url + "?" + data.toString() + "\t" + response);
            return response;
        } catch (IOException e) {
            LOGGER.error("requestPassportWithdraw", e);
        }
        return null;
    }

    public static String requestPassportBankingWithdraw(String accessToken, double amount, String accountName, String accountNumber,
            String bankCode, String bankProvince, String bankCity, String bankBranch, String lang) {
        Map headers = new HashMap();
        headers.put("accept", "application/json");
        headers.put("content-type", "application/json");
        headers.put("Authorization", accessToken);
        headers.put("lang", lang);

        JsonObject data = new JsonObject();
        data.addProperty("accountName", accountName);
        data.addProperty("accountNumber", accountNumber);
        data.addProperty("amount", amount);
        data.addProperty("bankCode", bankCode);
        data.addProperty("bankProvince", bankProvince);
        data.addProperty("bankCity", bankCity);
        data.addProperty("bankBranch", bankBranch);

        try {
            String url = UrlConfig.getInstance().getUrlPassportBankingWithdraw();
            Document doc = Jsoup.connect(url).timeout(ServerConfig.getInstance().apiTimeoutPassportBankingWithdraw()).ignoreContentType(true).headers(headers).requestBody(data.toString()).post();
            String response = doc.body().text();
            LOGGER.info(url + "?" + data.toString() + "\t" + response);
            return response;
        } catch (IOException e) {
            LOGGER.error("requestPassportBankingWithdraw", e);
        }
        return null;
    }

    public static String requestW88(String url, String data) {
        Map headers = new HashMap();
        headers.put("content-type", "application/json");

        try {
            Document doc = Jsoup.connect(url).timeout(20000).ignoreContentType(true).ignoreHttpErrors(true)
                    .headers(headers).requestBody(data).get();
            String response = doc.body().text();
            LOGGER.info(url + "?" + data + "\t" + response);
            return response;
        } catch (IOException e) {
            LOGGER.error("Error reading URL content: " + url + "?" + data, e);
        }
        return null;
    }
    
    public static String requestPaymentInfo(String url, String accessToken) {
        Map headers = new HashMap();
        headers.put("content-type", "application/json");
        headers.put("Accept", "application/json");       
        headers.put("Authorization", accessToken);      

        try {
            Document doc = Jsoup.connect(url).timeout(20000).ignoreContentType(true).ignoreHttpErrors(true)
                    .headers(headers).get();
            String response = doc.body().text();
            LOGGER.info(url + "?\t" + response);
            return response;
        } catch (IOException e) {
            LOGGER.error("Error reading URL content: " + url + "?", e);
        }
        return null;
    }
}
