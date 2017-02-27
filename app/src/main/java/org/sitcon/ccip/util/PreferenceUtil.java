package org.sitcon.ccip.util;

import android.content.Context;
import android.content.SharedPreferences;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;

public class PreferenceUtil {
    private static final String PREF_AUTH = "auth";
    private static final String PREF_IS_NEW_TOKEN = "is_new_token";
    private static final String PREF_AUTH_TOKEN = "token";
    private static final String PREF_SCHEDULE = "schedule";
    private static final String PREF_SCHEDULE_PROGRAMS = "programs";

    public static void setIsNewToken(Context context, boolean isNewToken) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_AUTH, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putBoolean(PREF_IS_NEW_TOKEN, isNewToken);
        editor.commit();
    }

    public static boolean getIsNewToken(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_AUTH, Context.MODE_PRIVATE);
        return sharedPreferences.getBoolean(PREF_IS_NEW_TOKEN, false);
    }

    public static void setToken(Context context, String token) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_AUTH, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putString(PREF_AUTH_TOKEN, token);
        editor.commit();
    }

    public static String getToken(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_AUTH, Context.MODE_PRIVATE);
        return sharedPreferences.getString(PREF_AUTH_TOKEN, null);
    }

    public static void savePrograms(Context context, List<Program> programs) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_SCHEDULE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putString(PREF_SCHEDULE_PROGRAMS, JsonUtil.toJson(programs));
        editor.commit();
    }

    public static List<Program> loadPrograms(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_SCHEDULE, Context.MODE_PRIVATE);
        String programsJson = sharedPreferences.getString(PREF_SCHEDULE_PROGRAMS, null);

        return JsonUtil.fromJson(programsJson, new TypeToken<ArrayList<Program>>(){}.getType());
    }
}
