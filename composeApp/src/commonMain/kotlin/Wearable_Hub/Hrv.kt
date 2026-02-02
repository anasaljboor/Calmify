package Wearable_Hub

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlin.math.sqrt

object Hrv {
    fun rmssd(rrMs: List<Int>): Double? {
        if (rrMs.size < 3) return null
        var sumSq = 0.0
        var count = 0
        for (i in 1 until rrMs.size) {
            val diff = (rrMs[i] - rrMs[i - 1]).toDouble()
            sumSq += diff * diff
            count++
        }
        return sqrt(sumSq / count)
    }
}





