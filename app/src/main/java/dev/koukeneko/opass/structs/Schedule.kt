package dev.koukeneko.opass.structs

import kotlinx.serialization.Serializable

@Serializable
data class ScheduleItem(
    val id: String,
    val type: String,
    val room: String,
    val broadcast: List<String>?,
    val start: String,
    val end: String,
    val qa: String?,
    val slide: String?,
    val co_write: String?,
    val live: String?,
    val record: String?,
    val language: String?,
    val uri: String,
    val zh: SessionDetails,
    val en: SessionDetails,
    val speakers: List<String>,
    val tags: List<String>
)

@Serializable
data class SessionDetails(
    val title: String,
    val description: String
)

