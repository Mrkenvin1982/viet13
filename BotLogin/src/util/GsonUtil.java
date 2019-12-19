/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package util;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import java.lang.reflect.Type;

/**
 *
 * @author anlh
 */
public class GsonUtil {
    
    private static final JsonParser PARSER = new JsonParser();
    
    private static final Gson GSON = new Gson();

    public static JsonElement parse(String s) {
        return PARSER.parse(s);
    }

    public static String toJson(Object o) {
        return GSON.toJson(o);
    }

    public static <T extends Object> T fromJson(String json, Class<T> classOfT) {
        return GSON.fromJson(json, classOfT);
    }
    
    public static JsonElement toJsonTree(Object src) {
        return GSON.toJsonTree(src);
    }

    public static String toJson(Object src, Type typeOfSrc) {
        return GSON.toJson(src, typeOfSrc);
    }
}