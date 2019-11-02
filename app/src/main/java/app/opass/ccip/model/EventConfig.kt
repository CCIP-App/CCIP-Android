package app.opass.ccip.model

import com.google.gson.annotations.SerializedName


data class EventConfig(
    @SerializedName("display_name")
    val displayName: LocalizedString,
    @SerializedName("event_id")
    val eventId: String,
    @SerializedName("features")
    val features: List<Feature>,
    @SerializedName("logo_url")
    val logoUrl: String,
    @SerializedName("event_date")
    val eventDate: TimeRange,
    @SerializedName("publish")
    val publish: TimeRange,
    @SerializedName("schedule_url")
    val scheduleUrl: String,
    @SerializedName("server_base_url")
    val serverBaseUrl: String
)

data class TimeRange(
    @SerializedName("end")
    val end: String,
    @SerializedName("start")
    val start: String
)
