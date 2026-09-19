package app.opass.ccip.util

import android.content.Context
import androidx.core.content.edit
import app.opass.ccip.model.ConfSchedule
import app.opass.ccip.model.EventConfig
import app.opass.ccip.push.IdentitySnapshot
import java.util.UUID

object PreferenceUtil {
    private const val PREF_EVENT = "event"
    private const val PREF_CURRENT_EVENT = "current_event"

    private const val PREF_AUTH = "auth"
    private const val PREF_AUTH_TOKEN = "token"
    private const val PREF_AUTH_ROLE = "role"

    private const val PREF_SCHEDULE = "schedule"
    private const val PREF_SCHEDULE_SCHEDULE = "schedule"
    private const val PREF_SCHEDULE_STARS = "stars"

    private const val DEFAULT_SHARED_PREFS = "default_shared_prefs"
    private const val NOTIFICATION = "notification"

    fun setCurrentEvent(context: Context, eventConfig: EventConfig) {
        context.getSharedPreferences(PREF_EVENT, Context.MODE_PRIVATE)
            .edit(true) {
                putString(PREF_CURRENT_EVENT, JsonUtil.toJson(eventConfig))
                putString("config_" + eventConfig.eventId, JsonUtil.toJson(eventConfig))
            }
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

    fun getToken(context: Context): String? {
        return captureIdentity(context, getCurrentEvent(context).eventId).token
    }

    @Synchronized
    fun captureIdentity(context: Context, eventId: String): IdentitySnapshot {
        val prefs = context.getSharedPreferences(PREF_AUTH, Context.MODE_PRIVATE)
        return IdentitySnapshot(eventId, prefs.getString(eventId + PREF_AUTH_TOKEN, null),
            prefs.getString(eventId + "_revision", "")!!)
    }

    @Synchronized
    fun isCurrent(context: Context, snapshot: IdentitySnapshot) = captureIdentity(context, snapshot.eventId) == snapshot

    @Synchronized
    fun updateIdentity(context: Context, snapshot: IdentitySnapshot, token: String?, role: String?,
                       newLogin: Boolean = false, beforeCommit: (IdentitySnapshot) -> Unit): Boolean {
        return snapshot.applyIfCurrent(captureIdentity(context, snapshot.eventId)) {
            val prefs = context.getSharedPreferences(PREF_AUTH, Context.MODE_PRIVATE)
            val changed = newLogin || token != snapshot.token || role != prefs.getString(snapshot.eventId + PREF_AUTH_ROLE, null)
            val next = snapshot.copy(token = token, revision = if (changed) UUID.randomUUID().toString() else snapshot.revision)
            // A crash between these writes leaves an unmatched revision, requiring revalidation.
            beforeCommit(next)
            check(prefs.edit()
                .putString(snapshot.eventId + PREF_AUTH_TOKEN, token)
                .putString(snapshot.eventId + PREF_AUTH_ROLE, role)
                .putString(snapshot.eventId + "_revision", next.revision).commit())
        }
    }

    @Synchronized
    fun loggedInIdentities(context: Context): List<IdentitySnapshot> =
        context.getSharedPreferences(PREF_AUTH, Context.MODE_PRIVATE).all
            .filter { (key, value) -> key.endsWith(PREF_AUTH_TOKEN) && value is String && value.isNotEmpty() }
            .keys.map { captureIdentity(context, it.removeSuffix(PREF_AUTH_TOKEN)) }

    fun getEvent(context: Context, eventId: String): EventConfig? {
        val json = context.getSharedPreferences(PREF_EVENT, Context.MODE_PRIVATE)
            .getString("config_$eventId", null)
        return if (json != null) JsonUtil.fromJson(json, EventConfig::class.java)
        else getCurrentEvent(context).takeIf { it.eventId == eventId }
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

    fun shouldPromptForNotification(context: Context): Boolean {
        val sharedPreferences =
            context.getSharedPreferences(DEFAULT_SHARED_PREFS, Context.MODE_PRIVATE)
        return sharedPreferences.getBoolean(NOTIFICATION, true)
    }

    fun shouldPromptForNotification(context: Context, bool: Boolean) {
        val sharedPreferences =
            context.getSharedPreferences(DEFAULT_SHARED_PREFS, Context.MODE_PRIVATE)
        sharedPreferences.edit { putBoolean(NOTIFICATION, bool) }
    }
}
