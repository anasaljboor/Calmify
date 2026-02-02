package Wearable_Hub

data class BiometricSample(
    val tsMs: Long,
    val hrBpm: Int? = null,
    val rrMs: List<Int> = emptyList(), // RR intervals (ms)
    val source: WearableSourceType
)
