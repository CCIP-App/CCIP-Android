package app.opass.ccip.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class Feature(
    @SerializedName("icon")
    val icon: String?,
    @SerializedName("display_name")
    val displayName: LocalizedString,
    @SerializedName("url")
    val url: String,
    @Expose(serialize = false, deserialize = false)
    val iconDrawable: Int?,
    @Expose(serialize = false, deserialize = false)
    val isEmbedded: Boolean = true,
    @Expose(serialize = false, deserialize = false)
    val shouldUseBuiltinZoomControls: Boolean = false
)
