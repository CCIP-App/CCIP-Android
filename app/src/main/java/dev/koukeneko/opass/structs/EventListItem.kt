package dev.koukeneko.opass.structs

import kotlinx.serialization.Serializable

@Serializable
data class EventListItem(
    val eventId: String,
    val displayName: Map<String, String>,
    val logoUrl: String,
) {
    companion object
}
