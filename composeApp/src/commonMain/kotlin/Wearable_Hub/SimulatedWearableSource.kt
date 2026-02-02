package Wearable_Hub

import kotlinx.coroutines.flow.Flow

class SimulatedWearableSource(
    private val sim: SimulatedHrSource = SimulatedHrSource(),
    private val modeProvider: () -> SimMode = { SimMode.NORMAL }
) : WearableSource {

    override val type: WearableSourceType = WearableSourceType.SIMULATED

    override fun stream(): Flow<BiometricSample> =
        sim.stream(modeFlow = modeProvider)
}
