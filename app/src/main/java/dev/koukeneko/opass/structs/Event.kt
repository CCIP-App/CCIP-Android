package dev.koukeneko.opass.structs

import kotlinx.serialization.Serializable

@Serializable
data class Event(
    val eventId: String,
    val displayName: Map<String, String>,
    val logoUrl: String,
    val eventWebsite: String,
    val eventDate: EventDate,
    val publish: PublishPeriod,
    val features: List<Feature>
) {
    companion object {
        fun fromString(orEmpty: String): Event {
            return Event(
                eventId = orEmpty,
                displayName = mapOf("en" to orEmpty, "zh" to orEmpty),
                logoUrl = orEmpty,
                eventWebsite = orEmpty,
                eventDate = EventDate.fromString(orEmpty),
                publish = PublishPeriod.fromString(orEmpty),
                features = listOf()
            )
        }
    }
}

private fun PublishPeriod.Companion.fromString(orEmpty: String): PublishPeriod {
    return PublishPeriod(
        start = orEmpty,
        end = orEmpty
    )
}

private fun EventDate.Companion.fromString(orEmpty: String): EventDate {
    return EventDate(
        start = orEmpty,
        end = orEmpty
    )
}

@Serializable
data class EventDate(
    val start: String,
    val end: String
) {
    companion object
}

@Serializable
data class PublishPeriod(
    val start: String,
    val end: String
) {
    companion object
}

@Serializable
data class Feature(
    val feature: String,
    val displayText: Map<String, String>,
    val visibleRoles: List<String>? = null,
    val url: String? = null,
    val icon: String? = null,
    val wifi: List<WiFiInfo>? = null
)

@Serializable
data class WiFiInfo(
    val ssid: String,
    val password: String
)
