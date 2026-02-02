package Wearable_Hub

import android.content.Context
import com.example.calmify.BleHeartRateSource

actual fun createBleSource(platformContext: Any?): WearableSource {
    val ctx = platformContext as? Context
        ?: error("Android context required for BLE source")
    return BleHeartRateSource(ctx)
}
