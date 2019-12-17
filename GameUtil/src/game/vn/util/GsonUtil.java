/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.vn.util;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import java.lang.reflect.Type;

/**
 *
 * @author anlh
 */
public class GsonUtil {
    
    private static final JsonParser parser = new JsonParser();
    
    private static final Gson gson = new Gson();

    public static JsonElement parse(String s) {
        return parser.parse(s);
    }

    public static String toJson(Object o) {
        return gson.toJson(o);
    }

    public static <T extends Object> T fromJson(String json, Class<T> classOfT) {
        return gson.fromJson(json, classOfT);
    }
    
    public static JsonElement toJsonTree(Object src) {
        return gson.toJsonTree(src);
    }

    public static String toJson(Object src, Type typeOfSrc) {
        return gson.toJson(src, typeOfSrc);
    }
}