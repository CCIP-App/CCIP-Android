package app.opass.ccip.model

import com.google.gson.JsonElement
import com.google.gson.annotations.SerializedName

data class Attendee(
    @SerializedName("_id") private var id: Id,
    var eventId: String,
    var token: String,
    var userId: String,
    var attr: JsonElement,
    var firstUse: Int,
    var type: String,
    var scenarios: ArrayList<Scenario>
)