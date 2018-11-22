package app.opass.ccip.model

data class En(
    override val subject: String,
    override val summary: String
) : SubmissionDetail