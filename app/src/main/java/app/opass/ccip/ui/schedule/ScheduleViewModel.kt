package app.opass.ccip.ui.schedule

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.liveData
import androidx.lifecycle.map
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import app.opass.ccip.CCIPApplication
import app.opass.ccip.R
import app.opass.ccip.extension.debounce
import app.opass.ccip.model.ConfSchedule
import app.opass.ccip.model.Session
import app.opass.ccip.model.SessionLang
import app.opass.ccip.model.SessionTag
import app.opass.ccip.model.SessionType
import app.opass.ccip.util.PreferenceUtil
import com.google.gson.internal.bind.util.ISO8601Utils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.ParseException
import java.text.ParsePosition
import java.text.SimpleDateFormat
import java.util.Locale

private val SDF_DATE = SimpleDateFormat("MM/dd", Locale.US)
private val SDF_TIME = SimpleDateFormat("HH:mm", Locale.TAIWAN)

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
private const val KEY_SELECTED_TYPE_IDS = "selectedTypeIds"
private const val KEY_SELECTED_LANG_IDS = "selectedLangIds"
private const val KEY_SHOW_SEARCH_PANEL = "showSearchPanel"

class ScheduleViewModel(application: Application, stateHandle: SavedStateHandle) : AndroidViewModel(application) {
    val schedule: MutableLiveData<ConfSchedule?> = MutableLiveData(null)
    val sessionsGroupedByDate: LiveData<Map<String, List<Session>>?> = schedule.switchMap { schedule ->
        liveData(viewModelScope.coroutineContext + Dispatchers.Default) {
            emit(schedule?.sessions?.groupedByDate())
        }
    }
    val tags: LiveData<List<SessionTag>?> = schedule.map { schedule -> schedule?.tags }
    val types: LiveData<List<SessionType>?> = schedule.map { schedule -> schedule?.sessionTypes }
    val langList: LiveData<List<SessionLang>?> = schedule.map { schedule ->
        val langList = schedule?.sessions?.mapNotNull { it.language }?.distinct()
        if (langList.isNullOrEmpty()) {
            null
        } else {
            langList.map { lang ->
                SessionLang(lang, SessionLang.Zh(lang), SessionLang.En(lang))
            }
        }
    }

    val showStarredOnly = stateHandle.getLiveData(KEY_SHOW_STARRED_ONLY, false)
    val selectedTagIds = stateHandle.getLiveData(KEY_SELECTED_TAG_IDS, emptyList<String>())
    val selectedTypeIds = stateHandle.getLiveData(KEY_SELECTED_TYPE_IDS, emptyList<String>())
    val selectedLangIds = stateHandle.getLiveData(KEY_SELECTED_LANG_IDS, emptyList<String>())
    val shouldShowSearchPanel = stateHandle.getLiveData(KEY_SHOW_SEARCH_PANEL, false)

    private val searchTerm: MutableLiveData<String> = MutableLiveData("")
    private val filterConfig: LiveData<FilterConfig> = MediatorLiveData<FilterConfig>().apply {
        val update = {
            value = FilterConfig(
                sessionsGroupedByDate.value,
                showStarredOnly.value!!,
                selectedTagIds.value!!,
                selectedTypeIds.value!!,
                selectedLangIds.value!!,
                searchTerm.value!!
            )
        }
        addSource(sessionsGroupedByDate) { update() }
        addSource(showStarredOnly) { update() }
        addSource(selectedTagIds) { update() }
        addSource(selectedTypeIds) { update() }
        addSource(selectedLangIds) { update() }
        addSource(searchTerm) { update() }
    }.debounce(100)
    val groupedSessionsToShow: LiveData<Map<String, List<Session>>?> = filterConfig.switchMap {
            (sessions, starredOnly, selectedTagIds, selectedTypeIds, selectedLangIds, searchText) ->
        liveData(viewModelScope.coroutineContext + Dispatchers.Default) {
            if (sessions == null) {
                emit(null)
                return@liveData
            }
            val filtered = if (starredOnly) sessions.let(::filterStarred) else sessions
            var result = filtered
            if (selectedTagIds.isNotEmpty()) {
                result = result.mapValues { (_, sessions) ->
                    sessions.filter { session ->
                        session.tags.any { tag -> selectedTagIds.any { id -> id == tag.id } }
                    }
                }
            }

            if (selectedTypeIds.isNotEmpty()) {
                result = result.mapValues { (_, sessions) ->
                    sessions.filter { session ->
                        selectedTypeIds.any { id -> session.type?.id == id }
                    }
                }
            }

            if (selectedLangIds.isNotEmpty()) {
                result = result.mapValues { (_, sessions) ->
                    sessions.filter { session ->
                        selectedLangIds.any { id -> session.language == id }
                    }
                }
            }

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
    val hasStarredSessions: MutableLiveData<Boolean> = MutableLiveData(PreferenceUtil.loadStarredIds(getApplication()).isNotEmpty())
    val hasAnyFilter = MediatorLiveData<Boolean>().apply {
        val update = {
            val starredOnly = showStarredOnly.value ?: false
            val hasSelectedTags = selectedTagIds.value?.isNotEmpty() ?: false
            val hasSelectedTypes = selectedTypeIds.value?.isNotEmpty() ?: false
            val hasSelectedLangList = selectedLangIds.value?.isNotEmpty() ?: false
            value = starredOnly || hasSelectedTags || hasSelectedTypes || hasSelectedLangList
        }
        addSource(showStarredOnly) { update() }
        addSource(selectedTagIds) { update() }
        addSource(selectedTypeIds) { update() }
        addSource(selectedLangIds) { update() }
    }
    val shouldFilterSheetCollapse = MediatorLiveData<Boolean>().apply {
        val update = {
            val scheduleReady = isScheduleReady.value ?: false
            val hasFilter = hasAnyFilter.value ?: false
            value = scheduleReady && hasFilter
        }
        addSource(isScheduleReady) { update() }
        addSource(hasAnyFilter) { update() }
    }
    val shouldShowFab = MediatorLiveData<Boolean>().apply {
        val update = {
            val scheduleReady = isScheduleReady.value ?: false
            val starredOnly = showStarredOnly.value ?: false
            val hasSelectedTags = selectedTagIds.value?.isNotEmpty() ?: false
            val hasSelectedTypes = selectedTypeIds.value?.isNotEmpty() ?: false
            val hasSelectedLangList = selectedLangIds.value?.isNotEmpty() ?: false
            value = scheduleReady && !starredOnly && !hasSelectedTags && !hasSelectedTypes && !hasSelectedLangList
        }
        addSource(isScheduleReady) { update() }
        addSource(showStarredOnly) { update() }
        addSource(selectedTagIds) { update() }
        addSource(selectedTypeIds) { update() }
        addSource(selectedLangIds) { update() }
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

            tags.value?.map(SessionTag::id)?.let { validTagIds ->
                selectedTagIds.value = selectedTagIds.value!!.filter { id -> validTagIds.contains(id) }
            }

            types.value?.map(SessionType::id)?.let { validTypeIds ->
                selectedTypeIds.value = selectedTypeIds.value!!.filter { id -> validTypeIds.contains(id) }
            }

            langList.value?.map(SessionLang::id)?.let { validLangIds ->
                selectedLangIds.value = selectedLangIds.value!!.filter { id -> validLangIds.contains(id) }
            }
        }
    }

    fun clearFilter() {
        showStarredOnly.value = false
        selectedTagIds.value = emptyList()
        selectedTypeIds.value = emptyList()
        selectedLangIds.value = emptyList()
    }

    fun toggleFilterTag(idToToggle: String) {
        if (selectedTagIds.value!!.contains(idToToggle)) {
            selectedTagIds.value = selectedTagIds.value!!.filterNot { id -> id == idToToggle }
        } else {
            selectedTagIds.value = selectedTagIds.value!! + idToToggle
        }
    }

    fun toggleFilterType(idToToggle: String) {
        if (selectedTypeIds.value!!.contains(idToToggle)) {
            selectedTypeIds.value = selectedTypeIds.value!!.filterNot { id -> id == idToToggle }
        } else {
            selectedTypeIds.value = selectedTypeIds.value!! + idToToggle
        }
    }

    fun toggleLangType(idToToggle: String) {
        if (selectedLangIds.value!!.contains(idToToggle)) {
            selectedLangIds.value = selectedLangIds.value!!.filterNot { id -> id == idToToggle }
        } else {
            selectedLangIds.value = selectedLangIds.value!! + idToToggle
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


    /** Pattern Example：
    ${event name} is about to start!
    ${event website}

    My most interesting agenda this year is

    Day${eventDayIndex} ${date}

    ${session_start_time} ~ ${session_end_time}
    ${session title}
    ${session uri}
    ...
    ${session_start_time} ~ ${session_end_time}
    ${session title}
    ${session uri}

    Use OPass to make your own agenda together!
    https://opass.app/
     */
    fun getShareSessionString(): String {
        return StringBuilder().apply {
            val starredIds = PreferenceUtil.loadStarredIds(getApplication())
            val starredSessions = schedule.value?.sessions?.filter { starredIds.contains(it.id) }
            val event = PreferenceUtil.getCurrentEvent(getApplication())
            getApplication<CCIPApplication>().resources?.let { resources ->
                appendLine(
                    resources.getString(
                        R.string.is_about_to_start,
                        event.displayName.findBestMatch(getApplication())
                    )
                )
                appendLine("${event.eventWebsite}\n")
                appendLine("${resources.getString(R.string.my_favorite_session_this_year_is)}\n")
                val allDates = sessionsGroupedByDate.value!!.keys.toList()
                starredSessions?.groupedByDate()?.forEach { (date, sessions) ->
                    appendLine("Day${allDates.indexOf(date) + 1} $date\n")
                    sessions.forEach { session ->
                        val startDate = ISO8601Utils.parse(session.start, ParsePosition(0))
                        val endDate = ISO8601Utils.parse(session.start, ParsePosition(0))
                        appendLine("${SDF_TIME.format(startDate)} ~ ${SDF_TIME.format(endDate)}")
                        appendLine(session.getSessionDetail(getApplication()).title)
                        if (session.uri != null) {
                            appendLine("${session.uri}\n")
                        } else {
                            appendLine()
                        }
                    }
                }
                appendLine(resources.getString(R.string.use_opass_to_make_your_own_agenda_together))
                appendLine("https://opass.app/")
            }
        }.toString()
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
    val selectedTagIds: List<String>,
    val selectedTypeIds: List<String>,
    val selectedLangList: List<String>,
    val searchTerm: String
)
