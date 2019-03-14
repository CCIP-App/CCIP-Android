package app.opass.ccip.model

import com.google.gson.annotations.SerializedName


data class EventConfig(
    @SerializedName("custom_features")
    val customFeatures: List<Any>,
    @SerializedName("display_name")
    val displayName: DisplayName?,
    @SerializedName("event_id")
    val eventId: String,
    @SerializedName("features")
    val features: Features,
    @SerializedName("logo_url")
    val logoUrl: String,
    @SerializedName("publish")
    val publish: Publish,
    @SerializedName("schedule_url")
    val scheduleUrl: String,
    @SerializedName("server_base_url")
    val serverBaseUrl: String
)

data class Features(
    @SerializedName("irc")
    val irc: String,
    @SerializedName("partners")
    val partners: String,
    @SerializedName("puzzle")
    val puzzle: String,
    @SerializedName("sponsors")
    val sponsors: String,
    @SerializedName("staffs")
    val staffs: String,
    @SerializedName("telegram")
    val telegram: String,
    @SerializedName("venue")
    val venue: String
)

data class Publish(
    @SerializedName("end")
    val end: String,
    @SerializedName("start")
    val start: String
)
