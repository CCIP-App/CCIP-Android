package app.opass.ccip.model

import com.google.gson.JsonElement

data class Attendee(
    var eventId: String,
    var token: String,
    var userId: String,
    var attr: JsonElement,
    var firstUse: Int,
    var role: String,
    var scenarios: ArrayList<Scenario>
)
