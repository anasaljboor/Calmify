package Wearable_Hub


object Stress {
    fun score(hr: Int?, rmssd: Double?): Pair<Int, Double> {
        // confidence: RR-based > HR-only
        if (rmssd != null) {
            // Lower RMSSD => higher stress (simple mapping)
            // Clamp for stability
            val clamped = rmssd.coerceIn(10.0, 120.0)
            val stress = ((120.0 - clamped) / 110.0 * 100.0).toInt().coerceIn(0, 100)
            return stress to 0.9
        }
        if (hr != null) {
            // HR-only proxy (less reliable)
            val clamped = hr.coerceIn(50, 150)
            val stress = ((clamped - 50) / 100.0 * 100.0).toInt().coerceIn(0, 100)
            return stress to 0.45
        }
        return 0 to 0.0
    }
}
