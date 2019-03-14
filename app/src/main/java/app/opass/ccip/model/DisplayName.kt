package app.opass.ccip.model

import android.content.Context
import app.opass.ccip.util.LocaleUtil
import com.google.gson.annotations.SerializedName
import java.util.*

data class DisplayName(
    @SerializedName("en")
    val en: String,
    @SerializedName("zh")
    val zh: String
) {
    fun getDisplayName(context: Context): String {
        return if (LocaleUtil.getCurrentLocale(context).language == Locale("zh").language) {
            zh
        } else {
            en
        }
    }
}

