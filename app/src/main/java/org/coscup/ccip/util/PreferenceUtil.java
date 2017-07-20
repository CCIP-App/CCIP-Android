package org.coscup.ccip.util;

import android.content.Context;
import android.content.SharedPreferences;
import com.google.gson.reflect.TypeToken;

import org.coscup.ccip.model.Submission;

import java.util.ArrayList;
import java.util.List;

public class PreferenceUtil {
    private static final String PREF_AUTH = "auth";
    private static final String PREF_IS_NEW_TOKEN = "is_new_token";
    private static final String PREF_AUTH_TOKEN = "token";
    private static final String PREF_SCHEDULE = "schedule";
    private static final String PREF_SCHEDULE_PROGRAMS = "programs";
    private static final String PREF_SCHEDULE_STARS = "stars";

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

    public static void savePrograms(Context context, List<Submission> submissions) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_SCHEDULE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putString(PREF_SCHEDULE_PROGRAMS, JsonUtil.toJson(submissions));
        editor.commit();
    }

    public static List<Submission> loadPrograms(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_SCHEDULE, Context.MODE_PRIVATE);
        String programsJson = sharedPreferences.getString(PREF_SCHEDULE_PROGRAMS, null);

        return JsonUtil.fromJson(programsJson, new TypeToken<ArrayList<Submission>>(){}.getType());
    }

    public static void saveStars(Context context, List<Submission> submissions) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_SCHEDULE_STARS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putString(PREF_SCHEDULE_STARS, JsonUtil.toJson(submissions));
        editor.apply();
    }

    public static List<Submission> loadStars(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_SCHEDULE_STARS, Context.MODE_PRIVATE);
        String starsJson = sharedPreferences.getString(PREF_SCHEDULE_STARS, "[]");

        return JsonUtil.fromJson(starsJson, new TypeToken<List<Submission>>(){}.getType());
    }
}
