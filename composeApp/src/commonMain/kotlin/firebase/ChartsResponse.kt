package firebase


import kotlinx.serialization.Serializable

@Serializable
data class ChartsResponse(
    val userId: String,
    val simId: String? = null,
    val dailyStressByHour: List<Int> = emptyList(),  // 24 ints
    val weeklyStressByDay: List<Int> = emptyList()   // 7 ints
)