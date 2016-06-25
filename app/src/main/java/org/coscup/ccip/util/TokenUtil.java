package org.coscup.ccip.util;

import android.content.Context;
import android.content.SharedPreferences;

public class TokenUtil {
    private static final String PREF_AUTH = "auth";
    private static final String PREF_AUTH_TOKEN = "token";

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
}
