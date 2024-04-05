package dev.koukeneko.opass.api

import dev.koukeneko.opass.structs.ScheduleItem
import dev.koukeneko.opass.structs.SessionDetails
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

class ScheduleClient {
    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json()
        }
    }

    suspend fun getSchedule(baseUrl: String): List<ScheduleItem> {

        //Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.4 Safari/605.1.15
        var headers = mapOf(
            "User-Agent" to "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.4 Safari/605.1.15",
            "Accept" to "application/json, text/plain, */*",
            "Connection" to "keep-alive",
            "Sec-Fetch-Mode" to "cors",
            "Sec-Fetch-Site" to "same-origin",
        )

        val response = client.get(baseUrl) {
            headers.forEach { (key, value) ->
                header(key, value)
            }
        }
        val responseBody: String = response.body()

        // notice url
        val rootJson = Json.parseToJsonElement(responseBody).jsonObject
        val sessionsArray = rootJson["sessions"]?.jsonArray
            ?: throw IllegalArgumentException("The 'sessions' array is missing in the response")

        return sessionsArray.mapNotNull { element ->
            val jsonObject = element.jsonObject

            val broadcast = jsonObject["broadcast"]?.let {
                if (it is kotlinx.serialization.json.JsonNull) null // Check for JsonNull explicitly
                else it.jsonArray.map { broadcastElement -> broadcastElement.jsonPrimitive.content }
            }

            val speakers = jsonObject["speakers"]?.let {
                if (it is kotlinx.serialization.json.JsonNull) null
                else it.jsonArray.map { speakerElement -> speakerElement.jsonPrimitive.content }
            }

            val tags = jsonObject["tags"]?.let {
                if (it is kotlinx.serialization.json.JsonNull) null
                else it.jsonArray.map { tagElement -> tagElement.jsonPrimitive.content }
            }

            ScheduleItem(
                id = jsonObject["id"]?.jsonPrimitive?.content ?: return@mapNotNull null,
                type = jsonObject["type"]?.jsonPrimitive?.content ?: "",
                room = jsonObject["room"]?.jsonPrimitive?.content ?: "",
                broadcast = broadcast ?: emptyList(), // Use emptyList if null
                start = jsonObject["start"]?.jsonPrimitive?.content ?: "",
                end = jsonObject["end"]?.jsonPrimitive?.content ?: "",
                qa = jsonObject["qa"]?.jsonPrimitive?.content,
                slide = jsonObject["slide"]?.jsonPrimitive?.content,
                co_write = jsonObject["co_write"]?.jsonPrimitive?.content,
                live = jsonObject["live"]?.jsonPrimitive?.content,
                record = jsonObject["record"]?.jsonPrimitive?.content,
                language = jsonObject["language"]?.jsonPrimitive?.content,
                uri = jsonObject["uri"]?.jsonPrimitive?.content ?: "",
                zh = SessionDetails(
                    title = jsonObject["zh"]?.jsonObject?.get("title")?.jsonPrimitive?.content ?: "",
                    description = jsonObject["zh"]?.jsonObject?.get("description")?.jsonPrimitive?.content ?: ""
                ),
                en = SessionDetails(
                    title = jsonObject["en"]?.jsonObject?.get("title")?.jsonPrimitive?.content ?: "",
                    description = jsonObject["en"]?.jsonObject?.get("description")?.jsonPrimitive?.content ?: ""
                ),
                speakers = speakers ?: emptyList(),
                tags = tags ?: emptyList()
            )
        }
    }

}