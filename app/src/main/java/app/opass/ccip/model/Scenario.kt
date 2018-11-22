package app.opass.ccip.model

import com.google.gson.JsonElement

data class Scenario(
    val displayText: DisplayText,
    val order: Int,
    val id: String,
    val availableTime: Int,
    val attr: JsonElement,
    val expireTime: Int,
    val countdown: Int,
    val used: Int?,
    val disabled: String?
)
