package Wearable_Hub

import kotlin.math.roundToInt

class StressSpikeDetector(
    private val triggerThreshold: Int = 75,
    private val minSamplesAbove: Int = 3,
    private val cooldownMs: Long = 60_000
) {
    private val window = ArrayDeque<Int>() // stress scores
    private val maxWindow = 12

    private var lastTriggerAt: Long = 0L

    data class SpikeResult(
        val shouldTriggerBreathing: Boolean,
        val confidence: Float,
        val reason: String
    )

    fun onNewState(state: StressState): SpikeResult {
        val now = state.tsMs
        val score = state.stress0to100

        // rolling window
        window.addLast(score)
        while (window.size > maxWindow) window.removeFirst()

        // cooldown gate
        if (now - lastTriggerAt < cooldownMs) {
            return SpikeResult(
                shouldTriggerBreathing = false,
                confidence = 0f,
                reason = "cooldown"
            )
        }

        val aboveCount = window.takeLast(minSamplesAbove).count { it >= triggerThreshold }
        val baseline = window.dropLast(3).averageOrNull() ?: score.toDouble()
        val delta = score - baseline.toFloat()

        val confidence =
            (aboveCount.toFloat() / minSamplesAbove).coerceIn(0f, 1f) * 0.7f +
                    (delta / 25f).coerceIn(0f, 1f) * 0.3f

        val shouldTrigger = aboveCount >= minSamplesAbove && confidence >= 0.75f

        if (shouldTrigger) lastTriggerAt = now

        val confRounded = (confidence * 100).roundToInt() / 100f

        return SpikeResult(
            shouldTriggerBreathing = shouldTrigger,
            confidence = confidence,
            reason = "score=$score baseline=${baseline.toInt()} delta=${delta.toInt()} conf=$confRounded"
        )
    }

    private fun Iterable<Int>.averageOrNull(): Double? {
        val list = this.toList()
        if (list.isEmpty()) return null
        return list.sum().toDouble() / list.size
    }

    private fun <T> ArrayDeque<T>.takeLast(n: Int): List<T> {
        if (n <= 0) return emptyList()
        return this.toList().takeLast(n)
    }
}
