package Wearable_Hub

data class StressState(
    val tsMs: Long,
    val hrBpm: Int?,
    val rmssd: Double?,
    val stress0to100: Int,
    val confidence0to1: Double,
    val source: WearableSourceType = WearableSourceType.BLE

) {
    companion object {
        fun initial() = StressState(
            tsMs = 0L,
            hrBpm = null,
            rmssd = null,
            stress0to100 = 0,
            confidence0to1 = 0.0,
            source = WearableSourceType.BLE
        )
    }
}
