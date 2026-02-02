package Wearable_Hub

import firebase.SimSample
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.time.Clock

sealed class StressEvent { data object TriggerBreathing : StressEvent() }

enum class SourceMode { BLE_ONLY, SIM_ONLY, BOTH_MERGE, AUTO_PREFER_BLE }

class StressStore(
    private val scope: CoroutineScope,
    private val bleSource: WearableSource?,
    private val simSource: WearableSource,
    private val pipeline: BlePipeline = BlePipeline(),
    private val spikeDetector: StressSpikeDetector = StressSpikeDetector(),
    private val modeFlow: StateFlow<SourceMode>
) {
    private val _state = MutableStateFlow(StressState.initial())
    val state: StateFlow<StressState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<StressEvent>(extraBufferCapacity = 8)
    val events = _events.asSharedFlow()

    private val _isRunning = MutableStateFlow(false)
    val isRunning: StateFlow<Boolean> = _isRunning.asStateFlow()

    private val _isLoggedIn = MutableStateFlow(false)
    private val _isAppOpened = MutableStateFlow(false)

    fun setLoggedIn(value: Boolean) { _isLoggedIn.value = value }
    fun setAppOpened(value: Boolean) { _isAppOpened.value = value }

    // ---- session capture ----
    private var collectJob: Job? = null
    private var _sessionStartedAtMs: Long = 0L

    private val sessionSamples = mutableListOf<SimSample>()
    private val pendingSamples = mutableListOf<SimSample>()
    private val samplesMutex = Mutex()

    fun tryStart() {
        if (_isRunning.value) return
        if (!_isLoggedIn.value) return
        if (!_isAppOpened.value) return

        _isRunning.value = true
        _sessionStartedAtMs = Clock.System.now().toEpochMilliseconds()

        collectJob?.cancel()
        collectJob = scope.launch {
            // reset buffers safely
            samplesMutex.withLock {
                sessionSamples.clear()
                pendingSamples.clear()
            }

            selectStream()
                .map { pipeline.process(it) }
                .collect { newState ->
                    _state.value = newState

                    val sample = SimSample(
                        tsMs = newState.tsMs,
                        hrBpm = newState.hrBpm,
                        rmssd = newState.rmssd,
                        stress0to100 = newState.stress0to100
                    )

                    // store safely (posting loop reads this too)
                    samplesMutex.withLock {
                        sessionSamples.add(sample)
                        pendingSamples.add(sample)
                    }

                    // spike trigger after 10s
                    val now = Clock.System.now().toEpochMilliseconds()
                    if (now - _sessionStartedAtMs < 10_000) return@collect

                    val spike = spikeDetector.onNewState(newState)
                    if (spike.shouldTriggerBreathing) {
                        _events.tryEmit(StressEvent.TriggerBreathing)
                    }
                }
        }
    }

    fun stopSession() {
        _isRunning.value = false
        collectJob?.cancel()
        collectJob = null
    }

    fun isSessionRunning(): Boolean = _isRunning.value

    fun sessionStartedAtMs(): Long = _sessionStartedAtMs

    // returns ONLY new samples since last call (safe)
    suspend fun drainPendingSamples(): List<SimSample> = samplesMutex.withLock {
        if (pendingSamples.isEmpty()) return emptyList()
        val out = pendingSamples.toList()
        pendingSamples.clear()
        out
    }

    // summary over ALL session samples (safe)
    suspend fun sessionSummaryForApi(): Map<String, Double> = samplesMutex.withLock {
        if (sessionSamples.isEmpty()) return emptyMap()

        val hr = sessionSamples.mapNotNull { it.hrBpm?.toDouble() }
        val stress = sessionSamples.mapNotNull { it.stress0to100?.toDouble() }
        val rmssd = sessionSamples.mapNotNull { it.rmssd }

        fun avg(xs: List<Double>) = if (xs.isEmpty()) 0.0 else xs.sum() / xs.size
        val maxStress = stress.maxOrNull() ?: 0.0

        buildMap {
            put("avgHr", avg(hr))
            put("avgStress", avg(stress))
            put("avgRmssd", avg(rmssd))
            put("maxStress", maxStress)
        }
    }

    private fun selectStream(): Flow<BiometricSample> {
        val ble = bleSource?.stream() ?: emptyFlow()
        val sim = simSource.stream()

        return modeFlow.flatMapLatest { mode ->
            when (mode) {
                SourceMode.BLE_ONLY -> ble
                SourceMode.SIM_ONLY -> sim
                SourceMode.BOTH_MERGE -> merge(ble, sim)
                SourceMode.AUTO_PREFER_BLE -> autoPreferBle(ble, sim)
            }
        }
    }

    private fun autoPreferBle(
        ble: Flow<BiometricSample>,
        sim: Flow<BiometricSample>
    ): Flow<BiometricSample> {
        val lastBleTs = MutableStateFlow(0L)
        val bleTracked = ble.onEach { lastBleTs.value = it.tsMs }

        return merge(
            bleTracked,
            sim.filter { sample ->
                val lastBle = lastBleTs.value
                lastBle == 0L || (sample.tsMs - lastBle) > 3_000
            }
        )
    }
}
