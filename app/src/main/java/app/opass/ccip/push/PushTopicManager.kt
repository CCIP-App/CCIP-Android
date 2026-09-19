package app.opass.ccip.push

import android.content.Context
import android.os.Build
import android.util.AtomicFile
import android.util.Log
import androidx.core.app.LocaleManagerCompat
import app.opass.ccip.extension.asyncExecute
import app.opass.ccip.extension.getFastPassUrl
import app.opass.ccip.model.Attendee
import app.opass.ccip.network.CCIPClient
import app.opass.ccip.network.PortalClient
import app.opass.ccip.util.JsonUtil
import app.opass.ccip.util.PreferenceUtil
import com.google.android.gms.tasks.Task
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import retrofit2.Response
import java.io.File
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class PushTopicManager private constructor(private val context: Context) {
    private data class EventState(val identity: String? = null, val role: String? = null, val topics: TopicState = TopicState())
    private data class State(val events: MutableMap<String, EventState> = mutableMapOf(), var registration: String? = null)
    // noBackupFilesDir is excluded from both cloud backup and device-to-device restore.
    private val file = AtomicFile(File(context.noBackupFilesDir, "push-topics.json"))
    private val state: State by lazy {
        if (file.baseFile.exists()) JsonUtil.fromJson(file.readFully().toString(Charsets.UTF_8), State::class.java) else State()
    }
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    // Serialize SDK changes so topic transitions cannot overlap.
    private val sdkLock = Mutex()
    private var languageTag: String? = null

    private fun save() {
        val stream = file.startWrite()
        try {
            stream.write(JsonUtil.toJson(state).toByteArray(Charsets.UTF_8))
            file.finishWrite(stream)
            check(file.readFully().contentEquals(JsonUtil.toJson(state).toByteArray(Charsets.UTF_8)))
        } catch (error: Throwable) {
            file.failWrite(stream)
            throw error
        }
    }

    private fun updateEvent(eventId: String, next: EventState) {
        val previous = state.events[eventId]
        state.events[eventId] = next
        try { save() } catch (error: Throwable) {
            if (previous == null) state.events.remove(eventId) else state.events[eventId] = previous
            throw error
        }
    }

    fun applyVerifiedIdentity(snapshot: IdentitySnapshot, token: String, attendee: Attendee, newLogin: Boolean = false): Boolean {
        if (attendee.eventId != snapshot.eventId || attendee.token != token || !PushContract.hasValidTopicIdentifiers(attendee.eventId, attendee.role)) return false
        val updated = PreferenceUtil.updateIdentity(context, snapshot, token, attendee.role, newLogin) { next ->
            val previous = state.events[snapshot.eventId] ?: EventState()
            updateEvent(snapshot.eventId, previous.copy(identity = next.fingerprint(), role = attendee.role))
        }
        if (updated) sync()
        return updated
    }

    fun invalidateIdentity(snapshot: IdentitySnapshot): Boolean {
        val updated = PreferenceUtil.updateIdentity(context, snapshot, null, null) {
            val previous = state.events[snapshot.eventId] ?: EventState()
            updateEvent(snapshot.eventId, previous.copy(identity = null, role = null))
        }
        if (updated) sync()
        return updated
    }

    fun isInvalidCredentials(response: Response<*>): Boolean {
        if (response.code() != 400) return false
        val message = runCatching {
            JsonUtil.GSON.fromJson(response.errorBody()?.string(), com.google.gson.JsonObject::class.java)?.get("message")?.asString
        }.getOrNull()
        return PushContract.invalidCredentials(response.code(), message)
    }

    fun refreshAll() {
        sync()
        scope.launch {
            try {
                PreferenceUtil.loggedInIdentities(context).forEach { snapshot ->
                    launch {
                        try {
                            val event = PreferenceUtil.getEvent(context, snapshot.eventId)
                                ?: PortalClient.get().getEventConfig(snapshot.eventId).asyncExecute().body()
                            if (event?.eventId != snapshot.eventId) return@launch
                            val url = event.getFastPassUrl() ?: return@launch
                            val response = CCIPClient.withBaseUrl(url).status(snapshot.token).asyncExecute()
                            if (response.isSuccessful) response.body()?.let { applyVerifiedIdentity(snapshot, snapshot.token!!, it) }
                            else if (isInvalidCredentials(response)) invalidateIdentity(snapshot)
                        } catch (_: Exception) { /* Keep verified state on transient/read failures. */ }
                    }
                }
            } catch (_: Exception) { Log.w("OPassPush", "Unable to read login state; keeping subscriptions") }
        }
    }

    fun localeChanged(tag: String) {
        languageTag = tag
        sync()
    }

    fun configurationChanged() {
        // Framework per-app locales can change outside the App on Android 13+.
        if (Build.VERSION.SDK_INT >= 33) languageTag = null
        sync()
    }

    fun registered(installationId: String) {
        scope.launch {
            try {
                sdkLock.withLock {
                    val fingerprint = PushContract.digest(installationId)
                    if (state.registration != fingerprint) {
                        val previousEvents = state.events.toMap()
                        val previousRegistration = state.registration
                        state.events.replaceAll { _, event ->
                            event.copy(topics = event.topics.copy(pending = event.topics.pending + listOfNotNull(event.topics.applied)))
                        }
                        state.registration = fingerprint
                        try { save() } catch (error: Throwable) {
                            state.events.clear()
                            state.events.putAll(previousEvents)
                            state.registration = previousRegistration
                            throw error
                        }
                    }
                }
                sync()
            } catch (_: Exception) { Log.w("OPassPush", "Registration reconciliation pending") }
        }
    }

    private fun target(eventId: String): String? {
        val event = state.events.getValue(eventId)
        val identity = PreferenceUtil.captureIdentity(context, eventId)
        if (identity.token == null || event.role == null) return null
        // Imported credentials cannot authorize a new subscription. Keep the old installation state until validation.
        if (identity.fingerprint() != event.identity) return event.topics.applied
        val systemTag = LocaleManagerCompat.getSystemLocales(context)[0]?.toLanguageTag() ?: "en"
        val tag = languageTag ?: LocaleManagerCompat.getApplicationLocales(context)[0]?.toLanguageTag() ?: "x-default"
        return PushContract.topic(eventId, event.role, PushContract.resolvePushLocale(tag, systemTag))
    }

    fun sync() {
        scope.launch {
            try {
                sdkLock.withLock {
                    state.events.keys.toList().forEach { eventId ->
                        try {
                            val event = state.events.getValue(eventId)
                            val identity = PreferenceUtil.captureIdentity(context, eventId)
                            if (event.role != null && identity.token != null && identity.fingerprint() != event.identity) return@forEach
                            reconcileTopic(
                                target = { target(eventId) },
                                read = { state.events.getValue(eventId).topics },
                                write = { topics ->
                                    updateEvent(eventId, state.events.getValue(eventId).copy(topics = topics))
                                },
                                unsubscribe = { FirebaseMessaging.getInstance().unsubscribeFromTopic(it).awaitCompletion() },
                                subscribe = { FirebaseMessaging.getInstance().subscribeToTopic(it).awaitCompletion() },
                            )
                        } catch (_: Exception) { Log.w("OPassPush", "Topic synchronization pending") }
                    }
                }
            } catch (_: Exception) { Log.w("OPassPush", "Unable to read subscription state; keeping subscriptions") }
        }
    }

    companion object {
        @Volatile private var instance: PushTopicManager? = null
        fun get(context: Context): PushTopicManager = instance ?: synchronized(this) {
            instance ?: PushTopicManager(context.applicationContext).also { instance = it }
        }
    }
}

private suspend fun Task<Void>.awaitCompletion(): Unit = suspendCancellableCoroutine { continuation ->
    addOnCompleteListener { task ->
        if (continuation.isActive) {
            if (task.isSuccessful) continuation.resume(Unit)
            else continuation.resumeWithException(task.exception ?: IllegalStateException("FCM operation failed"))
        }
    }
}
