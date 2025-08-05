package app.opass.ccip.model

import android.content.Context
import app.opass.ccip.util.LocaleUtil
import java.util.Locale

data class Speaker(
    val id: String,
    val avatar: String,
    val zh: Zh_,
    val en: En_
) {
    fun getSpeakerDetail(context: Context): SpeakerDetail {
        return when (LocaleUtil.getCurrentLocale(context).language) {
            "nan", "zh" -> zh
            else -> en
        }
    }
}
