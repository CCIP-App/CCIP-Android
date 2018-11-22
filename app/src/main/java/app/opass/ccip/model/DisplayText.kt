package app.opass.ccip.model

import com.google.gson.annotations.SerializedName

data class DisplayText(
    @SerializedName("en-US") val enUS: String? = null,
    @SerializedName("zh-TW") val zhTW: String? = null
)