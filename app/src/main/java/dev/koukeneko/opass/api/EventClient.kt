package dev.koukeneko.opass.api

import dev.koukeneko.opass.structs.Event
import dev.koukeneko.opass.structs.EventDate
import dev.koukeneko.opass.structs.EventListItem
import dev.koukeneko.opass.structs.Feature
import dev.koukeneko.opass.structs.PublishPeriod
import dev.koukeneko.opass.structs.WiFiInfo
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.*

class EventClient {
    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json()
        }
    }
    private var baseUrl = "https://portal.opass.app/"

    suspend fun getEventList(): List<EventListItem> {

        val response = client.get(baseUrl + "events/")
        val responseBody: String = response.body()

        val jsonElements = Json.parseToJsonElement(responseBody).jsonArray

        return jsonElements.mapNotNull { element ->
            val jsonObject = element.jsonObject
            val eventId = jsonObject["event_id"]?.jsonPrimitive?.content ?: return@mapNotNull null
            val displayNameEn = jsonObject["display_name"]?.jsonObject?.get("en")?.jsonPrimitive?.content ?: ""
            val displayNameZh = jsonObject["display_name"]?.jsonObject?.get("zh")?.jsonPrimitive?.content ?: ""
            val logoUrl = jsonObject["logo_url"]?.jsonPrimitive?.content ?: ""

            EventListItem(
                eventId = eventId,
                displayName = mapOf("en" to displayNameEn, "zh" to displayNameZh),
                logoUrl = logoUrl
            )
        }
    }

    suspend fun getEvent(eventId: String): Event {
        val response = client.get(baseUrl + "events/$eventId/")
        val responseBody: String = response.body()

        val jsonObject = Json.parseToJsonElement(responseBody).jsonObject

        // Assuming you have an appropriate constructor or method to parse these
        val eventDate = EventDate(
            start = jsonObject["event_date"]?.jsonObject?.get("start")?.jsonPrimitive?.content.orEmpty(),
            end = jsonObject["event_date"]?.jsonObject?.get("end")?.jsonPrimitive?.content.orEmpty()
        )

        val publish = PublishPeriod(
            start = jsonObject["publish"]?.jsonObject?.get("start")?.jsonPrimitive?.content ?: "",
            end = jsonObject["publish"]?.jsonObject?.get("end")?.jsonPrimitive?.content ?: ""
        )

        val features = jsonObject["features"]?.jsonArray?.mapNotNull { featureElement ->
            val featureObject = featureElement.jsonObject
            Feature(
                feature = featureObject["feature"]?.jsonPrimitive?.content ?: "",
                displayText = mapOf(
                    "en" to featureObject["display_text"]?.jsonObject?.get("en")?.jsonPrimitive?.content.orEmpty(),
                    "zh" to featureObject["display_text"]?.jsonObject?.get("zh")?.jsonPrimitive?.content.orEmpty()
                ),
                visibleRoles = featureObject["visible_roles"]?.jsonArray?.map { it.jsonPrimitive.content },
                url = featureObject["url"]?.jsonPrimitive?.content,
                icon = featureObject["icon"]?.jsonPrimitive?.content,
                wifi = featureObject["wifi"]?.jsonArray?.map { wifiElement ->
                    val wifiObject = wifiElement.jsonObject
                    return@map WiFiInfo( // Explicit return statement
                        ssid = wifiObject["SSID"]?.jsonPrimitive?.content.orEmpty(),
                        password = wifiObject["password"]?.jsonPrimitive?.content.orEmpty()
                    )
                }
                    ?: listOf<WiFiInfo>() // Specify the type to ensure the compiler understands the expected return type

            )
        } ?: listOf()

        return Event(
            eventId = eventId,
            displayName = mapOf(
                "en" to jsonObject["display_name"]?.jsonObject?.get("en")?.jsonPrimitive?.content.orEmpty(),
                "zh" to jsonObject["display_name"]?.jsonObject?.get("zh")?.jsonPrimitive?.content.orEmpty()
            ),
            logoUrl = jsonObject["logo_url"]?.jsonPrimitive?.content.orEmpty(),
            eventWebsite = jsonObject["event_website"]?.jsonPrimitive?.content.orEmpty(),
            eventDate = eventDate,
            publish = publish,
            features = features
        )
    }

}
