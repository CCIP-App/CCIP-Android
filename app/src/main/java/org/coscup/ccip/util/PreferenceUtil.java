package org.coscup.ccip.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.coscup.ccip.model.Program;

import java.util.ArrayList;
import java.util.List;

public class PreferenceUtil {
    private static final String PREF_AUTH = "auth";
    private static final String PREF_AUTH_TOKEN = "token";
    private static final String PREF_SCHEDULE = "schedule";
    private static final String PREF_SCHEDULE_PROGRAMS = "programs";

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

        Gson gson = new Gson();
        editor.putString(PREF_SCHEDULE_PROGRAMS, gson.toJson(programs));
        editor.commit();
    }

    public static List<Program> loadPrograms(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_SCHEDULE, Context.MODE_PRIVATE);
        String programsJson = sharedPreferences.getString(PREF_SCHEDULE_PROGRAMS, null);

        Gson gson = new Gson();
        return gson.fromJson(programsJson, new TypeToken<ArrayList<Program>>(){}.getType());
    }
}
