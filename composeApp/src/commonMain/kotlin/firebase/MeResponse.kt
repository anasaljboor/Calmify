package firebase

@kotlinx.serialization.Serializable
data class MeResponse(
    val userId: String,
    val email: String? = null,
    val username: String? = null
)
