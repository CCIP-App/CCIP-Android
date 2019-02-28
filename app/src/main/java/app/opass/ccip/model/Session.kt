package app.opass.ccip.model

import android.content.Context
import android.content.res.Resources
import app.opass.ccip.R
import app.opass.ccip.util.LocaleUtil
import java.util.*

data class Session(
    val id: String,
    val room: String,
    val start: String?,
    val end: String,
    val type: String,
    val zh: Zh,
    val en: En,
    val speakers: ArrayList<Speaker>
) {
    companion object {
        fun getTypeString(type: String): Int {
            return when (type) {
                "K" -> R.string.keynote
                "L" -> R.string.lightning_talk
                "P" -> R.string.panel_discussion
                "S" -> R.string.short_talk
                "T" -> R.string.talk
                "U" -> R.string.unconf
                "E" -> R.string.event
                else -> throw Resources.NotFoundException("Unexpected type symbol")
            }
        }
    }

    fun getSessionDetail(context: Context): SessionDetail {
        return if (LocaleUtil.getCurrentLocale(context).language == Locale("zh").language) {
            zh
        } else {
            en
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }

        if (other == null || other !is Session) {
            return false
        }

        return this.id == other.id
    }

    override fun hashCode(): Int {
        var result = start.hashCode()
        result = 31 * result + end.hashCode()
        result = 31 * result + room.hashCode()

        return result
    }
}
