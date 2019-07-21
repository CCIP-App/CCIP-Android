package app.opass.ccip.util

import android.content.Context
import androidx.core.content.edit
import app.opass.ccip.model.ConfSchedule
import app.opass.ccip.model.EventConfig
import app.opass.ccip.model.Session
import com.google.gson.reflect.TypeToken

object PreferenceUtil {
    private const val PREF_BEACON = "beacon"
    private const val PREF_BEACON_PERMISSION_REQUESTED = "permission_requested"
    private const val PREF_BEACON_NOTIFIED = "notified"

    private const val PREF_EVENT = "event"
    private const val PREF_CURRENT_EVENT = "current_event"

    private const val PREF_AUTH = "auth"
    private const val PREF_IS_NEW_TOKEN = "is_new_token"
    private const val PREF_AUTH_TOKEN = "token"

    private const val PREF_SCHEDULE = "schedule"
    private const val PREF_SCHEDULE_SCHEDULE = "schedule"
    private const val PREF_SCHEDULE_STARS = "stars"

    fun setBeaconPermissionRequested(context: Context) {
        context.getSharedPreferences(PREF_BEACON, Context.MODE_PRIVATE)
            .edit(true) { putBoolean(PREF_BEACON_PERMISSION_REQUESTED, true) }
    }

    fun isBeaconPermissionRequested(context: Context): Boolean {
        val sharedPreferences = context.getSharedPreferences(PREF_BEACON, Context.MODE_PRIVATE)
        return sharedPreferences.getBoolean(PREF_BEACON_PERMISSION_REQUESTED, false)
    }

    fun setBeaconNotified(context: Context) {
        context.getSharedPreferences(PREF_BEACON, Context.MODE_PRIVATE)
            .edit(true) { putBoolean(getCurrentEvent(context).eventId + PREF_BEACON_NOTIFIED, true) }
    }

    fun isBeaconNotified(context: Context): Boolean {
        val sharedPreferences = context.getSharedPreferences(PREF_BEACON, Context.MODE_PRIVATE)
        return sharedPreferences.getBoolean(getCurrentEvent(context).eventId + PREF_BEACON_NOTIFIED, false)
    }

    fun setCurrentEvent(context: Context, eventConfig: EventConfig) {
        context.getSharedPreferences(PREF_EVENT, Context.MODE_PRIVATE)
            .edit(true) { putString(PREF_CURRENT_EVENT, JsonUtil.toJson(eventConfig)) }
    }

    fun getCurrentEvent(context: Context): EventConfig {
        val sharedPreferences = context.getSharedPreferences(PREF_EVENT, Context.MODE_PRIVATE)
        val currentEvent = sharedPreferences.getString(PREF_CURRENT_EVENT, "{\"event_id\": \"\"}")

        return JsonUtil.fromJson(currentEvent, object : TypeToken<EventConfig>() {}.type)
    }

    fun setIsNewToken(context: Context, isNewToken: Boolean) {
        context.getSharedPreferences(PREF_AUTH, Context.MODE_PRIVATE)
            .edit(true) { putBoolean(getCurrentEvent(context).eventId + PREF_IS_NEW_TOKEN, isNewToken) }
    }

    fun getIsNewToken(context: Context): Boolean {
        val sharedPreferences = context.getSharedPreferences(PREF_AUTH, Context.MODE_PRIVATE)
        return sharedPreferences.getBoolean(getCurrentEvent(context).eventId + PREF_IS_NEW_TOKEN, false)
    }

    fun setToken(context: Context, token: String?) {
        context.getSharedPreferences(PREF_AUTH, Context.MODE_PRIVATE)
            .edit(true) { putString(getCurrentEvent(context).eventId + PREF_AUTH_TOKEN, token) }
    }

    fun getToken(context: Context): String? {
        val sharedPreferences = context.getSharedPreferences(PREF_AUTH, Context.MODE_PRIVATE)
        return sharedPreferences.getString(getCurrentEvent(context).eventId + PREF_AUTH_TOKEN, null)
    }

    fun saveSchedule(context: Context, scheduleJson: String) {
        context.getSharedPreferences(PREF_SCHEDULE, Context.MODE_PRIVATE)
            .edit(true) { putString(getCurrentEvent(context).eventId + PREF_SCHEDULE_SCHEDULE, scheduleJson) }
    }

    fun loadSchedule(context: Context): ConfSchedule? {
        val sharedPreferences = context.getSharedPreferences(PREF_SCHEDULE, Context.MODE_PRIVATE)
        val scheduleJson =
            sharedPreferences.getString(getCurrentEvent(context).eventId + PREF_SCHEDULE_SCHEDULE, "{}")!!

        return JsonUtil.fromJson(scheduleJson, ConfSchedule::class.java)
    }

    fun saveStars(context: Context, sessions: List<Session>) {
        context.getSharedPreferences(PREF_SCHEDULE_STARS, Context.MODE_PRIVATE)
            .edit { putString(getCurrentEvent(context).eventId + PREF_SCHEDULE_STARS, JsonUtil.toJson(sessions)) }
    }

    fun loadStars(context: Context): MutableList<Session> {
        val sharedPreferences = context.getSharedPreferences(PREF_SCHEDULE_STARS, Context.MODE_PRIVATE)
        val starsJson = sharedPreferences.getString(getCurrentEvent(context).eventId + PREF_SCHEDULE_STARS, "[]")!!

        return JsonUtil.fromJson(starsJson, object : TypeToken<MutableList<Session>>() {}.type)
    }
}
