package app.opass.ccip.push

import java.net.URI
import java.security.MessageDigest

object PushContract {
    private val identifier = Regex("[A-Za-z0-9_-]{1,64}")
    private val pushId = Regex("[0-9a-fA-F]{8}(-[0-9a-fA-F]{4}){3}-[0-9a-fA-F]{12}")

    fun hasValidTopicIdentifiers(eventId: String?, role: String?) =
        eventId != null && role != null && identifier.matches(eventId) && identifier.matches(role) && role != "all"

    fun resolvePushLocale(appLanguageTag: String, systemLanguageTag: String): String {
        val resolved = (if (appLanguageTag == "x-default") systemLanguageTag else appLanguageTag).lowercase()
        return when {
            listOf("zh", "nan-hant", "nan-latn").any { resolved == it || resolved.startsWith("$it-") } -> "zh-Hant"
            else -> "en"
        }
    }

    fun topic(eventId: String, role: String, locale: String): String {
        require(hasValidTopicIdentifiers(eventId, role) && locale in listOf("en", "zh-Hant"))
        return "opass-v1.$eventId.$role.$locale"
    }

    // CCIP-Server's /status uses Error("invalid token"), HTTP 400. 403 is Wi-Fi gating.
    fun invalidCredentials(status: Int, message: String?) = status == 400 && message == "invalid token"

    fun digest(value: String): String = MessageDigest.getInstance("SHA-256")
        .digest(value.toByteArray(Charsets.UTF_8)).joinToString("") { "%02x".format(it) }

    data class Route(val pushId: String, val eventId: String, val uri: String?)

    fun route(data: Map<String, String>): Route? {
        val push = data["push_id"] ?: return null
        val event = data["event_id"] ?: return null
        if (!pushId.matches(push) || !identifier.matches(event)) return null
        val link = data["uri"]
        if (link != null) {
            val parsed = runCatching { URI(link) }.getOrNull() ?: return null
            if (parsed.scheme != "https" || runCatching { parsed.toURL().host }.getOrNull().isNullOrEmpty()) return null
        }
        return Route(push, event, link)
    }
}

data class IdentitySnapshot(val eventId: String, val token: String?, val revision: String) {
    fun applyIfCurrent(current: IdentitySnapshot, update: () -> Unit): Boolean {
        if (this != current) return false
        update()
        return true
    }

    fun fingerprint() = PushContract.digest("$eventId\u0000$revision\u0000${token.orEmpty()}")
}

data class TopicState(val applied: String? = null, val pending: Set<String> = emptySet())

/** The caller serializes SDK operations. Persist uncertainty before touching the SDK. */
suspend fun reconcileTopic(
    target: () -> String?,
    read: () -> TopicState,
    write: (TopicState) -> Unit,
    unsubscribe: suspend (String) -> Unit,
    subscribe: suspend (String) -> Unit,
) {
    while (true) {
        val state = read()
        val desired = target()
        if (state.pending.isNotEmpty()) {
            (state.pending + listOfNotNull(state.applied)).forEach { unsubscribe(it) }
            write(TopicState())
            continue
        }
        if (state.applied == desired) return
        write(TopicState(state.applied, listOfNotNull(state.applied, desired).toSet()))
        state.applied?.let { unsubscribe(it) }
        if (target() != desired) continue
        desired?.let { subscribe(it) }
        write(TopicState(desired))
    }
}
