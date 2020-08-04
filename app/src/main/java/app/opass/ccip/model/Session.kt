package app.opass.ccip.model

import android.content.Context
import app.opass.ccip.util.LocaleUtil
import java.util.*

data class Session(
    val id: String,
    val room: Room,
    val start: String?,
    val end: String,
    val type: SessionType?,
    val zh: Zh,
    val en: En,
    val speakers: List<Speaker>,
    val qa: String?,
    val slide: String?,
    val broadcast: List<String>?,
    val live: String?,
    val record: String?,
    val tags: List<SessionTag>
) {
    fun getSessionDetail(context: Context): SessionDetail {
        return if (LocaleUtil.getCurrentLocale(context).language == Locale("zh").language) {
            zh
        } else {
            en
        }
    }
}
