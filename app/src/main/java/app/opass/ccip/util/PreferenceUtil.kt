package app.opass.ccip.util

import android.content.Context
import androidx.core.content.edit
import app.opass.ccip.model.ConfSchedule
import app.opass.ccip.model.EventConfig

object PreferenceUtil {
    private const val PREF_EVENT = "event"
    private const val PREF_CURRENT_EVENT = "current_event"

    private const val PREF_AUTH = "auth"
    private const val PREF_AUTH_TOKEN = "token"
    private const val PREF_AUTH_ROLE = "role"

    private const val PREF_SCHEDULE = "schedule"
    private const val PREF_SCHEDULE_SCHEDULE = "schedule"
    private const val PREF_SCHEDULE_STARS = "stars"

    fun setCurrentEvent(context: Context, eventConfig: EventConfig) {
        context.getSharedPreferences(PREF_EVENT, Context.MODE_PRIVATE)
            .edit(true) { putString(PREF_CURRENT_EVENT, JsonUtil.toJson(eventConfig)) }
    }

    fun getCurrentEvent(context: Context): EventConfig {
        val sharedPreferences = context.getSharedPreferences(PREF_EVENT, Context.MODE_PRIVATE)
        val currentEvent = sharedPreferences.getString(PREF_CURRENT_EVENT, "{\"event_id\": \"\"}")
        return try {
            JsonUtil.fromJson(currentEvent!!, EventConfig::class.java)
        } catch (t: Throwable) {
            JsonUtil
                .fromJson("{\"event_id\": \"\"}", EventConfig::class.java)
                .also { setCurrentEvent(context, it) }
        }
    }

    fun setToken(context: Context, token: String?) {
        context.getSharedPreferences(PREF_AUTH, Context.MODE_PRIVATE)
            .edit(true) { putString(getCurrentEvent(context).eventId + PREF_AUTH_TOKEN, token) }
    }

    fun getToken(context: Context): String? {
        val sharedPreferences = context.getSharedPreferences(PREF_AUTH, Context.MODE_PRIVATE)
        return sharedPreferences.getString(getCurrentEvent(context).eventId + PREF_AUTH_TOKEN, null)
    }

    fun setRole(context: Context, role: String?) {
        context.getSharedPreferences(PREF_AUTH, Context.MODE_PRIVATE)
            .edit(true) { putString(getCurrentEvent(context).eventId + PREF_AUTH_ROLE, role)}
    }

    fun getRole(context: Context): String? {
        val sharedPreferences = context.getSharedPreferences(PREF_AUTH, Context.MODE_PRIVATE)
        return sharedPreferences.getString(getCurrentEvent(context).eventId + PREF_AUTH_ROLE, null)
    }

    fun saveSchedule(context: Context, scheduleJson: String) {
        context.getSharedPreferences(PREF_SCHEDULE, Context.MODE_PRIVATE)
            .edit(true) { putString(getCurrentEvent(context).eventId + PREF_SCHEDULE_SCHEDULE, scheduleJson) }
    }

    fun loadSchedule(context: Context): ConfSchedule? {
        val scheduleJson = loadRawSchedule(context)

        return try {
            JsonUtil.fromJson(scheduleJson, ConfSchedule::class.java)
        } catch (t: Throwable) {
            saveSchedule(context, "{}")
            JsonUtil.fromJson("{}", ConfSchedule::class.java)
        }
    }

    fun loadRawSchedule(context: Context): String {
        val prefs = context.getSharedPreferences(PREF_SCHEDULE, Context.MODE_PRIVATE)
        return prefs.getString(getCurrentEvent(context).eventId + PREF_SCHEDULE_SCHEDULE, "{}")!!
    }

    fun saveStarredIds(context: Context, sessionIds: List<String>) {
        context.getSharedPreferences(PREF_SCHEDULE_STARS, Context.MODE_PRIVATE)
            .edit { putStringSet(getCurrentEvent(context).eventId + PREF_SCHEDULE_STARS, sessionIds.toSet()) }
    }

    fun loadStarredIds(context: Context): List<String> {
        val sharedPreferences = context.getSharedPreferences(PREF_SCHEDULE_STARS, Context.MODE_PRIVATE)
        return try {
            sharedPreferences.getStringSet(getCurrentEvent(context).eventId + PREF_SCHEDULE_STARS, emptySet())!!
                .toList()
        } catch (t: Throwable) {
            emptyList<String>().also { saveStarredIds(context, it) }
        }
    }
}
