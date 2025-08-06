package app.opass.ccip.model

import android.content.Context
import app.opass.ccip.util.LocaleUtil
import java.util.Locale

data class SessionType(
    val id: String,
    val zh: Zh,
    val en: En
) {
    fun getDetails(context: Context) =
        when (LocaleUtil.getCurrentLocale(context).language) {
            "nan", "zh" -> zh
            else -> en
        }

    interface LocalizedDetail {
        val name: String
    }

    data class Zh(override val name: String) : LocalizedDetail
    data class En(override val name: String) : LocalizedDetail
}
