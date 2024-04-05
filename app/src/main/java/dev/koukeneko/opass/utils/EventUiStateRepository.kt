package dev.koukeneko.opass.utils

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dev.koukeneko.opass.structs.Event
import dev.koukeneko.opass.structs.EventListItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class EventUiStateRepository(private val context: Context) {
    companion object {
        private val Context.dataStore: DataStore<Preferences> by preferencesDataStore("currentEvent")
        private val currentEventIdKey = stringPreferencesKey("currentEventId")
        private val currentEvent = stringPreferencesKey("currentEvent")
        private val eventList = stringPreferencesKey("eventList")
    }
    val getCurrentEventId: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[currentEventIdKey].orEmpty()
    }

    val getCurrentEvent: Flow<Event> = context.dataStore.data.map { preferences ->
        Event.fromString(preferences[currentEvent].orEmpty())
    }

//    val getEventList: Flow<List<EventListItem>> = context.dataStore.data.map { preferences ->
//        preferences[eventList].orEmpty().split(", ").map { it ->
//            EventListItem.fromString(it)
//        }
//    }
//

    suspend fun saveEventList(eventList: List<EventListItem>) {
        context.dataStore.edit { preferences ->
            preferences[EventUiStateRepository.eventList] = eventList.toString()
        }
        Log.d("EventUiStateRepository", "saveEventList: $eventList")
    }

    suspend fun saveCurrentEventId(token: String) {
        context.dataStore.edit { preferences ->
            preferences[currentEventIdKey] = token
        }
        Log.d("EventUiStateRepository", "saveCurrentEventId: $token")
    }

    suspend fun saveCurrentEvent(event: Event) {
        context.dataStore.edit { preferences ->
            preferences[currentEvent] = event.toString()
        }
        Log.d("EventUiStateRepository", "saveCurrentEvent: $event")
    }
}