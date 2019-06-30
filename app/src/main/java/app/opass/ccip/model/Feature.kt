package app.opass.ccip.model

import com.google.gson.annotations.SerializedName

data class Feature(
    @SerializedName("feature")
    val feature: FeatureType,
    @SerializedName("icon")
    val icon: String?,
    @SerializedName("display_text")
    val displayText: LocalizedString,
    @SerializedName("url")
    val url: String?,
    @SerializedName("visible_roles")
    val visibleRoles: List<String>?
)

enum class FeatureType(val type: String) {
    @SerializedName("fastpass")
    FAST_PASS("fastpass"),
    @SerializedName("schedule")
    SCHEDULE("schedule"),
    @SerializedName("announcement")
    ANNOUNCEMENT("announcement"),
    @SerializedName("puzzle")
    PUZZLE("puzzle"),
    @SerializedName("ticket")
    TICKET("ticket"),
    @SerializedName("telegram")
    TELEGRAM("telegram"),
    @SerializedName("im")
    IM("im"),
    @SerializedName("sponsors")
    SPONSORS("sponsors"),
    @SerializedName("staffs")
    STAFFS("staffs"),
    @SerializedName("venue")
    VENUE("venue"),
    @SerializedName("webview")
    WEBVIEW("webview");

    companion object {
        private val map = values().associateBy(FeatureType::type)
        fun fromString(type: String) = map[type]
    }
}
