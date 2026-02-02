package Wearable_Hub

import kotlinx.coroutines.flow.Flow

interface WearableSource {
    val type: WearableSourceType
    fun stream(): Flow<BiometricSample>
}
