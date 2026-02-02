package Wearable_Hub

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlin.math.PI
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
import kotlin.random.Random
import kotlin.time.Clock

enum class SimMode { CALM, NORMAL, STRESSED, RECOVERY }

class SimulatedHrSource(
    private val seed: Int = 42
) {
    private val rng = Random(seed)

    fun stream(modeFlow: () -> SimMode = { SimMode.NORMAL }): Flow<BiometricSample> = flow {
        val rrHistory = ArrayDeque<Int>()
        val historySize = 30

        fun targetHr(mode: SimMode) = when (mode) {
            SimMode.CALM -> 58
            SimMode.NORMAL -> 72
            SimMode.STRESSED -> 95
            SimMode.RECOVERY -> 75
        }

        fun clamp(x: Double, lo: Double, hi: Double) = max(lo, min(hi, x))
        fun hrToRrMs(hr: Double) = (60000.0 / hr).toInt()

        var hr = 72.0
        var t = 0.0

        while (true) {
            val mode = modeFlow()

            val hrTarget = targetHr(mode).toDouble()
            hr += (hrTarget - hr) * 0.05

            val breathingWave = sin(t * 0.12) * when (mode) {
                SimMode.CALM -> 2.5
                SimMode.NORMAL -> 1.8
                SimMode.STRESSED -> 0.9
                SimMode.RECOVERY -> 1.4
            }

            val noise = rng.nextDouble(-1.2, 1.2)

            val hrNow = clamp(hr + breathingWave + noise, 45.0, 130.0)
            val rrBase = hrToRrMs(hrNow)

            val rrJitter = (rng.nextGaussian() * 10.0).toInt()
            val rr = (rrBase + rrJitter).coerceIn(350, 1400)

            rrHistory.addLast(rr)
            if (rrHistory.size > historySize) rrHistory.removeFirst()

            emit(
                BiometricSample(
                    tsMs = Clock.System.now().toEpochMilliseconds(),
                    hrBpm = hrNow.toInt(),
                    rrMs = rrHistory.toList(),
                    source = WearableSourceType.SIMULATED
                )
            )

            t += 1.0
            delay(rr.toLong())
        }
    }
}

private fun Random.nextGaussian(): Double {
    var u = 0.0
    var v = 0.0
    while (u == 0.0) u = nextDouble()
    while (v == 0.0) v = nextDouble()
    return kotlin.math.sqrt(-2.0 * kotlin.math.ln(u)) * kotlin.math.cos(2.0 * PI * v)
}



