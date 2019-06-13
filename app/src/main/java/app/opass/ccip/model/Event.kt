package app.opass.ccip.model

import com.google.gson.annotations.SerializedName


data class Event(
    @SerializedName("display_name")
    val displayName: LocalizedString,
    @SerializedName("event_id")
    val eventId: String,
    @SerializedName("logo_url")
    val logoUrl: String
)
