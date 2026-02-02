package Wearable_Hub

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

class AppScope {
    private val supervisor = SupervisorJob()
    val scope = CoroutineScope(supervisor + Dispatchers.Default)

    fun close() {
        supervisor.cancel()
    }
}
