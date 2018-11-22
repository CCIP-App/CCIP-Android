package app.opass.ccip.util

import android.content.Context
import app.opass.ccip.model.Submission
import com.google.gson.reflect.TypeToken
import java.util.*

class PreferenceUtil {
    companion object {
        private const val PREF_AUTH = "auth"
        private const val PREF_IS_NEW_TOKEN = "is_new_token"
        private const val PREF_AUTH_TOKEN = "token"
        private const val PREF_SCHEDULE = "schedule"
        private const val PREF_SCHEDULE_PROGRAMS = "programs"
        private const val PREF_SCHEDULE_STARS = "stars"

        fun setIsNewToken(context: Context, isNewToken: Boolean) {
            val sharedPreferences = context.getSharedPreferences(PREF_AUTH, Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()

            editor.putBoolean(PREF_IS_NEW_TOKEN, isNewToken)
            editor.commit()
        }

        fun getIsNewToken(context: Context): Boolean {
            val sharedPreferences = context.getSharedPreferences(PREF_AUTH, Context.MODE_PRIVATE)
            return sharedPreferences.getBoolean(PREF_IS_NEW_TOKEN, false)
        }

        fun setToken(context: Context, token: String?) {
            val sharedPreferences = context.getSharedPreferences(PREF_AUTH, Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()

            editor.putString(PREF_AUTH_TOKEN, token)
            editor.commit()
        }

        fun getToken(context: Context): String? {
            val sharedPreferences = context.getSharedPreferences(PREF_AUTH, Context.MODE_PRIVATE)
            return sharedPreferences.getString(PREF_AUTH_TOKEN, null)
        }

        fun savePrograms(context: Context, submissions: List<Submission>) {
            val sharedPreferences = context.getSharedPreferences(PREF_SCHEDULE, Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()

            editor.putString(PREF_SCHEDULE_PROGRAMS, JsonUtil.toJson(submissions))
            editor.commit()
        }

        fun loadPrograms(context: Context): List<Submission>? {
            val sharedPreferences = context.getSharedPreferences(PREF_SCHEDULE, Context.MODE_PRIVATE)
            val programsJson = sharedPreferences.getString(PREF_SCHEDULE_PROGRAMS, "[]")!!

            return JsonUtil.fromJson<List<Submission>>(programsJson, object : TypeToken<ArrayList<Submission>>() {

            }.type)
        }

        fun saveStars(context: Context, submissions: List<Submission>) {
            val sharedPreferences = context.getSharedPreferences(PREF_SCHEDULE_STARS, Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()

            editor.putString(PREF_SCHEDULE_STARS, JsonUtil.toJson(submissions))
            editor.apply()
        }

        fun loadStars(context: Context): MutableList<Submission> {
            val sharedPreferences = context.getSharedPreferences(PREF_SCHEDULE_STARS, Context.MODE_PRIVATE)
            val starsJson = sharedPreferences.getString(PREF_SCHEDULE_STARS, "[]")!!

            return JsonUtil.fromJson(starsJson, object : TypeToken<MutableList<Submission>>() {}.type)
        }
    }
}
