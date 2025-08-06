package app.opass.ccip.model

import android.content.Context
import app.opass.ccip.util.LocaleUtil
import com.google.gson.annotations.SerializedName
import java.util.Locale

data class Session(
    val id: String,
    val room: Room,
    val start: String?,
    val end: String,
    val type: SessionType?,
    val uri: String?,
    val zh: Zh,
    val en: En,
    val speakers: List<Speaker>,
    val qa: String?,
    val slide: String?,
    val broadcast: List<String>?,
    val live: String?,
    val record: String?,
    val language: String?,
    @SerializedName("co_write")
    val coWrite: String?,
    val tags: List<SessionTag>
) {
    fun getSessionDetail(context: Context): SessionDetail {
        return when (LocaleUtil.getCurrentLocale(context).language) {
            "nan", "zh" -> zh
            else -> en
        }
    }
}
