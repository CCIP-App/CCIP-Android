package org.pycontw.ccip.util;

import com.google.gson.Gson;

import java.lang.reflect.Type;

public class JsonUtil {
    private static final Gson GSON = new Gson();

    private JsonUtil() {
    }

    public static <T> T fromJson(String json, Class<T> clazz) {
        return GSON.fromJson(json, clazz);
    }

    public static <T> T fromJson(String json, Type typeOfT) {
        return GSON.fromJson(json, typeOfT);
    }

    public static String toJson(Object obj) {
        return GSON.toJson(obj);
    }
}
