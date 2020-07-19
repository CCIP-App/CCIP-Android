package app.opass.ccip.ui.schedule

import android.app.Application
import androidx.lifecycle.*
import app.opass.ccip.model.ConfSchedule
import app.opass.ccip.model.Session
import app.opass.ccip.model.SessionTag
import app.opass.ccip.util.PreferenceUtil
import com.google.gson.internal.bind.util.ISO8601Utils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
    val schedule: MutableLiveData<ConfSchedule?> = MutableLiveData(null)

    val sessionsGroupedByDate: LiveData<Map<String, List<Session>>?>
    val groupedSessionsToShow: LiveData<Map<String, List<Session>>?>
    val tags: LiveData<List<SessionTag>?>
    val isScheduleReady: LiveData<Boolean>

    val showStarredOnly: MutableLiveData<Boolean> = MutableLiveData(false)
    val selectedTagIds = MutableLiveData<List<String>>(emptyList())
    val filtersActivated = MediatorLiveData<Boolean>().apply {
        val update = {
            val starredOnly = showStarredOnly.value!!
            val hasSelectedTags = selectedTagIds.value!!.isNotEmpty()
            value = starredOnly || hasSelectedTags
        }
        addSource(showStarredOnly) { update() }
        addSource(selectedTagIds) { update() }
    }

    init {
        viewModelScope.launch {
            schedule.value = getSchedule()
        }
        sessionsGroupedByDate = schedule.switchMap { schedule ->
            liveData(viewModelScope.coroutineContext + Dispatchers.Default) {
                emit(schedule?.sessions?.groupedByDate())
            }
        }
        groupedSessionsToShow = MediatorLiveData<Map<String, List<Session>>?>().apply {
            val update = update@{
                val sessions = sessionsGroupedByDate.value
                if (sessions == null) {
                    value = null
                    return@update
                }
                val starredOnly = showStarredOnly.value!!
                val selectedTagIds = selectedTagIds.value!!
                viewModelScope.launch(Dispatchers.Default) {
                    val filtered = if (starredOnly) sessions.let(::filterStarred) else sessions
                    val result = if (selectedTagIds.isNotEmpty()) {
                        filtered.mapValues { (_, sessions) ->
                            sessions.filter { session -> session.tags.any { tag -> selectedTagIds.any { id -> id == tag.id } } }
                        }
                    } else filtered
                    postValue(result)
                }
            }
            addSource(sessionsGroupedByDate) { update() }
            addSource(showStarredOnly) { update() }
            addSource(selectedTagIds) { update() }
        }
        isScheduleReady = groupedSessionsToShow.map { sessions -> sessions != null }
        tags = schedule.map { schedule -> schedule?.tags }
    }

    private suspend fun getSchedule() = withContext(Dispatchers.Default) { PreferenceUtil.loadSchedule(getApplication()) }

    fun reloadSchedule() {
        viewModelScope.launch {
            schedule.value = getSchedule()
        }
        val validTagIds = tags.value?.map { tag -> tag.id } ?: return
        selectedTagIds.value = selectedTagIds.value!!.filter { id -> validTagIds.contains(id) }
    }

    fun clearFilter() {
        showStarredOnly.value = false
        selectedTagIds.value = emptyList()
    }

    fun toggleFilterTag(idToToggle: String) {
        if (selectedTagIds.value!!.contains(idToToggle)) {
            selectedTagIds.value = selectedTagIds.value!!.filterNot { id -> id == idToToggle }
        } else {
            selectedTagIds.value = selectedTagIds.value!! + idToToggle
        }
    }

    fun toggleStarFilter() {
        showStarredOnly.value = showStarredOnly.value!!.not()
    }

    private fun filterStarred(sessions: Map<String, List<Session>>): Map<String, List<Session>> {
        val starredIds = PreferenceUtil.loadStarredIds(getApplication())
        return sessions.mapValues { (_, sessions) ->
            sessions.filter { s-> starredIds.contains(s.id) }
        }
    }
}
