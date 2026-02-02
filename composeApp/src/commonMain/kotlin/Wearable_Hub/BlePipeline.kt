package Wearable_Hub

class BlePipeline {
    fun process(sample: BiometricSample): StressState {
        val rmssd = Hrv.rmssd(sample.rrMs)
        val (stress, conf) = Stress.score(sample.hrBpm, rmssd)

        return StressState(
            tsMs = sample.tsMs,
            hrBpm = sample.hrBpm,
            rmssd = rmssd,
            stress0to100 = stress,
            confidence0to1 = conf,
            source = sample.source
        )
    }
}
