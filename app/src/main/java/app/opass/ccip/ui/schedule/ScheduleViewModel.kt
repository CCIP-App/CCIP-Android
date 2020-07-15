package app.opass.ccip.ui.schedule

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import app.opass.ccip.model.Session
import app.opass.ccip.util.PreferenceUtil
import com.google.gson.internal.bind.util.ISO8601Utils
import java.text.ParseException
import java.text.ParsePosition
import java.text.SimpleDateFormat
import java.util.*

private val SDF_DATE = SimpleDateFormat("MM/dd", Locale.US)

fun getDateOrNull(date: String): String? =
    try {
        SDF_DATE.format(ISO8601Utils.parse(date, ParsePosition(0)))
    } catch (e: ParseException) {
        e.printStackTrace()
        null
    }

fun List<Session>.groupedByDate(): Map<String, List<Session>> =
    this.groupBy { getDateOrNull(it.start!!) }
        .filterKeys { it != null }
        .toSortedMap(Comparator { start1, start2 -> start1!!.compareTo(start2!!) })

class ScheduleViewModel(application: Application) : AndroidViewModel(application) {
    val showStarredOnly: MutableLiveData<Boolean> = MutableLiveData(false)
    val isScheduleReady: MutableLiveData<Boolean> = MutableLiveData(false)
    val sessionsGroupedByDate: MutableLiveData<Map<String, List<Session>>?> = MutableLiveData(null)
    val starredSessionGroupedByDate: MutableLiveData<Map<String, List<Session>>?> = MutableLiveData(null)

    init {
        reloadSessions()
    }

    fun reloadSessions() {
        sessionsGroupedByDate.value = getGroupedSessions()
        starredSessionGroupedByDate.value = sessionsGroupedByDate.value?.let(::filterStarred)
        isScheduleReady.value = sessionsGroupedByDate.value != null
    }

    private fun filterStarred(sessions: Map<String, List<Session>>): Map<String, List<Session>>? {
        val starredIds = PreferenceUtil.loadStarredIds(getApplication())
        return sessions.mapValues { (_, sessions) ->
            sessions.filter { s-> starredIds.contains(s.id) }
        }
    }

    private fun getGroupedSessions(): Map<String, List<Session>>? {
        val sessions = PreferenceUtil.loadSchedule(getApplication())?.sessions ?: return null
        return sessions.groupedByDate()
    }
}
