package Wearable_Hub

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

actual fun createBleSource(platformContext: Any?): WearableSource {
    // iOS BLE not implemented yet -> safe no-op source
    return object : WearableSource {
        override val type: WearableSourceType = WearableSourceType.BLE

        override fun stream(): Flow<BiometricSample> = emptyFlow()
    }
}
