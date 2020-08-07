package app.opass.ccip.ui.schedule

import android.app.Application
import androidx.lifecycle.*
import app.opass.ccip.extension.debounce
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

private const val KEY_SHOW_STARRED_ONLY = "showStarredOnly"
private const val KEY_SELECTED_TAG_IDS = "selectedTagIds"
private const val KEY_SHOW_SEARCH_PANEL = "showSearchPanel"

class ScheduleViewModel(application: Application, stateHandle: SavedStateHandle) : AndroidViewModel(application) {
    val schedule: MutableLiveData<ConfSchedule?> = MutableLiveData(null)
    val sessionsGroupedByDate: LiveData<Map<String, List<Session>>?> = schedule.switchMap { schedule ->
        liveData(viewModelScope.coroutineContext + Dispatchers.Default) {
            emit(schedule?.sessions?.groupedByDate())
        }
    }
    val tags: LiveData<List<SessionTag>?> = schedule.map { schedule -> schedule?.tags }

    val showStarredOnly = stateHandle.getLiveData(KEY_SHOW_STARRED_ONLY, false)
    val selectedTagIds = stateHandle.getLiveData(KEY_SELECTED_TAG_IDS, emptyList<String>())
    val shouldShowSearchPanel = stateHandle.getLiveData(KEY_SHOW_SEARCH_PANEL, false)

    private val searchTerm: MutableLiveData<String> = MutableLiveData("")
    private val filterConfig: LiveData<FilterConfig> = MediatorLiveData<FilterConfig>().apply {
        val update = {
            value = FilterConfig(sessionsGroupedByDate.value, showStarredOnly.value!!, selectedTagIds.value!!, searchTerm.value!!)
        }
        addSource(sessionsGroupedByDate) { update() }
        addSource(showStarredOnly) { update() }
        addSource(selectedTagIds) { update() }
        addSource(searchTerm) { update() }
    }.debounce(100)
    val groupedSessionsToShow: LiveData<Map<String, List<Session>>?> = filterConfig.switchMap { (sessions, starredOnly, selectedTagIds, searchText) ->
        liveData(viewModelScope.coroutineContext + Dispatchers.Default) {
            if (sessions == null) {
                emit(null)
                return@liveData
            }
            val filtered = if (starredOnly) sessions.let(::filterStarred) else sessions
            val result = if (selectedTagIds.isNotEmpty()) {
                filtered.mapValues { (_, sessions) ->
                    sessions.filter { session -> session.tags.any { tag -> selectedTagIds.any { id -> id == tag.id } } }
                }
            } else filtered

            if (hasSearchTerm()) {
                val searchResult = result.mapValues { (_, sessions) ->
                    sessions.filter { session ->
                        session.en.description.contains(searchText, ignoreCase = true) ||
                            session.en.title.contains(searchText, ignoreCase = true) ||
                            session.zh.description.contains(searchText, ignoreCase = true) ||
                            session.zh.title.contains(searchText, ignoreCase = true)
                    }
                }
                emit(searchResult)
            } else {
                emit(result)
            }
        }
    }
    val isScheduleReady: LiveData<Boolean> = groupedSessionsToShow.map { sessions -> sessions != null }
    val shouldFilterSheetCollapse = MediatorLiveData<Boolean>().apply {
        val update = {
            val scheduleReady = isScheduleReady.value ?: false
            val starredOnly = showStarredOnly.value ?: false
            val hasSelectedTags = selectedTagIds.value?.isNotEmpty() ?: false
            value = scheduleReady && (starredOnly || hasSelectedTags)
        }
        addSource(isScheduleReady) { update() }
        addSource(showStarredOnly) { update() }
        addSource(selectedTagIds) { update() }
    }
    val shouldShowFab = MediatorLiveData<Boolean>().apply {
        val update = {
            val scheduleReady = isScheduleReady.value ?: false
            val starredOnly = showStarredOnly.value ?: false
            val hasSelectedTags = selectedTagIds.value?.isNotEmpty() ?: false
            value = scheduleReady && !starredOnly && !hasSelectedTags
        }
        addSource(isScheduleReady) { update() }
        addSource(showStarredOnly) { update() }
        addSource(selectedTagIds) { update() }
    }

    init {
        viewModelScope.launch {
            schedule.value = getSchedule()
        }
    }

    private fun hasSearchTerm(): Boolean {
        return searchTerm.value.isNullOrEmpty().not()
    }

    private suspend fun getSchedule() = withContext(Dispatchers.Default) { PreferenceUtil.loadSchedule(getApplication()) }

    fun reloadSchedule() {
        viewModelScope.launch {
            schedule.value = getSchedule()

            val validTagIds = tags.value?.map { tag -> tag.id } ?: return@launch
            selectedTagIds.value = selectedTagIds.value!!.filter { id -> validTagIds.contains(id) }
        }
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

    fun search(key: String) {
        searchTerm.postValue(key)
    }

    fun toggleSearchPanel(show: Boolean) {
        shouldShowSearchPanel.value = show
    }

    private fun filterStarred(sessions: Map<String, List<Session>>): Map<String, List<Session>> {
        val starredIds = PreferenceUtil.loadStarredIds(getApplication())
        return sessions.mapValues { (_, sessions) ->
            sessions.filter { s-> starredIds.contains(s.id) }
        }
    }
}

private data class FilterConfig(
    val sessions: Map<String, List<Session>>?,
    val showStarredOnly: Boolean,
    val selectedFilterIds: List<String>,
    val searchTerm: String
)
