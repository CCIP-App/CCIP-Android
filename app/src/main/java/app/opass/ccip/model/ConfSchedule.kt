package app.opass.ccip.model

data class ConfSchedule(
    val sessions: List<Session>,
    val speakers: List<Speaker>,
    val sessionTypes: List<SessionType>,
    val rooms: List<Room>,
    val tags: List<SessionTag>
)
