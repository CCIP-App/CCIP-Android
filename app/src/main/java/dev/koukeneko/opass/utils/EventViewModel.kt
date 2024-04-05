package dev.koukeneko.opass.utils

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.koukeneko.opass.api.EventClient
import dev.koukeneko.opass.api.ScheduleClient
import dev.koukeneko.opass.structs.Event
import dev.koukeneko.opass.structs.EventListItem
import dev.koukeneko.opass.structs.ScheduleItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class EventUiState(
    val currentEvent: Event? = null,
    val eventList: List<EventListItem> = emptyList(),
    val currentEventId: String? = null,
    val sessionList: List<ScheduleItem> = emptyList()
)

class EventViewModel : ViewModel() {

    // Expose screen UI state
    private val _uiState = MutableStateFlow(EventUiState())
    val uiState: StateFlow<EventUiState> = _uiState.asStateFlow()

    init {
        // Launch a coroutine in the ViewModel's scope
        viewModelScope.launch {
            // Fetch event list data and set the first event as current event
//            getCurrentEvent(getEventList().first().eventId).let { it ->
//                // set current event id
//                setCurrentEventId(it.eventId)
//                setSessionList(it.features.find {it.feature == "schedule" }?.url.orEmpty())
//            }

            getEventList()
        }
    }

    // Handle event logic

    // Get Current Event
    suspend fun setCurrentEvent(eventId: String): Event {
        // Fetch event data
        val event = EventClient().getEvent(eventId)
        // Update UI state
        _uiState.value = _uiState.value.copy(currentEvent = event, currentEventId = event.eventId)
        return event
    }

    // Get Event List
    suspend fun getEventList(): List<EventListItem> {
        try {
            // Fetch event list data
            val eventList = EventClient().getEventList()
            // Update UI state
            _uiState.value = _uiState.value.copy(eventList = eventList)
            return eventList
        }catch (e: Exception) {
            // Log the error or handle it as necessary
            Log.e("EventViewModel", e.message.orEmpty())
            return emptyList()
        }
    }

    suspend fun setCurrentEventId(eventId: String) {
        if (eventId.isNullOrEmpty()) {
            viewModelScope.launch {
                setCurrentEvent(getEventList().first().eventId)
                _uiState.value = _uiState.value.copy(currentEventId = getEventList().first().eventId)
            }
        }else{
            _uiState.value = _uiState.value.copy(currentEventId = eventId)
            // Fetch current event data when event id is set
            viewModelScope.launch {
                setCurrentEvent(eventId)
            }
            viewModelScope.launch {
                // set session list when event id is set
                setSessionList(setCurrentEvent(eventId).features.find {it.feature == "schedule"}?.url.orEmpty())
            }
        }

    }

    // Get Session List
    suspend fun setSessionList(url: String): List<ScheduleItem> {
        try {
            // Fetch session list data
            val sessionList = ScheduleClient().getSchedule(url)
            // Update UI state
            _uiState.value = _uiState.value.copy(sessionList = sessionList)
            val temp = uiState.value.currentEventId.orEmpty()
            Log.d("EventViewModel", "Set New Sessions for $temp , url: $url, sessionList: $sessionList")
            return sessionList
        }catch (e: Exception) {
            // Log the error or handle it as necessary
            Log.e("EventViewModel", e.message.orEmpty())
            return emptyList()
        }

    }

}