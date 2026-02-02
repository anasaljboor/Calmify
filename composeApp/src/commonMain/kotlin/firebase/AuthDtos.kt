package firebase

import kotlinx.serialization.Serializable

@Serializable
data class AuthRequest(
    val email: String,
    val password: String,
    val username: String? = null
)


@Serializable
data class AuthResponse(val userId: String, val accessToken: String)


@kotlinx.serialization.Serializable
data class SimSample(
    val tsMs: Long,
    val hrBpm: Int? = null,
    val rmssd: Double? = null,
    val stress0to100: Int? = null
)

@kotlinx.serialization.Serializable
data class SimulationRequest(
    val startedAtMs: Long,
    val endedAtMs: Long,
    val samples: List<SimSample> = emptyList(),
    val summary: Map<String, Double>? = null
)

@kotlinx.serialization.Serializable
data class LatestSimulationResponse(
    val userId: String,
    val latest: LatestSimulation? = null
)

@kotlinx.serialization.Serializable
data class LatestSimulation(
    val id: String,
    val startedAtMs: Long,
    val endedAtMs: Long,
    val summary: Map<String, Double> = emptyMap()
)
