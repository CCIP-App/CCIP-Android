package app.opass.ccip.util

import app.opass.ccip.model.*
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.reflect.TypeToken
import org.intellij.lang.annotations.Language
import java.lang.reflect.Type

class ConfScheduleDeserializer : JsonDeserializer<ConfSchedule> {
    override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext): ConfSchedule? {
        json ?: return null
        val obj = json.asJsonObject
        if (obj.size() == 0) return null

        val speakers = deserializeList<Speaker>(obj["speakers"], context)
        val sessionTypes = deserializeList<SessionType>(obj["session_types"], context)
        val rooms = deserializeList<Room>(obj["rooms"], context)
        val sessionTags = deserializeList<SessionTag>(obj["tags"], context)
        val sessions = deserializeList<SessionTemp>(obj["sessions"], context).map { session ->
            Session(
                id = session.id,
                type = sessionTypes.find { session.type == it.id },
                room = rooms.find { session.room == it.id }!!,
                speakers = session.speakers.mapNotNull { id -> speakers.find { it.id == id } },
                tags = session.tags.mapNotNull { id -> sessionTags.find { it.id == id } },
                start = session.start,
                end = session.end,
                zh = session.zh,
                en = session.en,
                qa = session.qa,
                slide = session.slide,
                broadcast = session.broadcast,
                coWrite = session.coWrite,
                live = session.live,
                record = session.record,
                language = session.language
            )
        }

        return ConfSchedule(sessions, speakers, sessionTypes, rooms, sessionTags)
    }

    private inline fun <reified T> deserializeList(json: JsonElement, context: JsonDeserializationContext): List<T> {
        return context.deserialize(json, TypeToken.getParameterized(List::class.java, T::class.java).type)
    }
}

data class SessionTemp(
    val id: String,
    val type: String,
    val room: String,
    val start: String,
    val end: String,
    val zh: Zh,
    val en: En,
    val speakers: List<String>,
    val tags: List<String>,
    val qa: String?,
    val slide: String?,
    val broadcast: List<String>?,
    val coWrite: String?,
    val live: String?,
    val record: String?,
    val language: String?
)
