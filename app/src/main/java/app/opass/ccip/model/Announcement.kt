package app.opass.ccip.model

import com.google.gson.annotations.SerializedName

data class Announcement(
    @SerializedName("_id") val id: Id,
    val datetime: Int,
    val msgEn: String,
    val msgZh: String,
    val uri: String
)