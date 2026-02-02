package Wearable_Hub

import firebase.AuthRepository
import firebase.AuthState
import firebase.ChartsResponse
import firebase.FastApiAuthRepository
import firebase.KtorAuthApi
import firebase.LatestSimulation
import firebase.buildHttpClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AppGraph(platformContext: Any? = null) {
    val appScope = AppScope()
    val latestSim = MutableStateFlow<LatestSimulation?>(null)
    private val _dailyChart = MutableStateFlow(List(24) { 0 })
    val dailyChart: StateFlow<List<Int>> = _dailyChart.asStateFlow()

    private val _weeklyChart = MutableStateFlow(List(7) { 0 })
    val weeklyChart: StateFlow<List<Int>> = _weeklyChart.asStateFlow()

    // Auth
    val client = buildHttpClient()
    val api = KtorAuthApi(client, baseUrl = "http://192.168.0.104:8000") // emulator example
    val repo: AuthRepository = FastApiAuthRepository(api)

    // Sources
    private var simMode: SimMode = SimMode.NORMAL
    val simulatedSource: WearableSource =
        SimulatedWearableSource(modeProvider = { simMode })

    val bleSource: WearableSource? =
        runCatching { createBleSource(platformContext) }.getOrNull()

    // Choose mode at runtime
    val sourceMode = MutableStateFlow(SourceMode.AUTO_PREFER_BLE)

    // Store
    val stressStore = StressStore(
        scope = appScope.scope,
        bleSource = bleSource,
        simSource = simulatedSource,
        pipeline = BlePipeline(),
        spikeDetector = StressSpikeDetector(),
        modeFlow = sourceMode
    )


    fun setSimMode(mode: SimMode) { simMode = mode }

    fun refreshChartsIfLoggedIn() {
        val s = repo.state.value
        if (s !is AuthState.Authenticated) return

        appScope.scope.launch {
            runCatching {
                val res: ChartsResponse = api.latestSimulationCharts(s.accessToken)

                // keep sizes safe
                val daily = res.dailyStressByHour.take(24).let { list ->
                    if (list.size == 24) list else list + List(24 - list.size) { 0 }
                }
                val weekly = res.weeklyStressByDay.take(7).let { list ->
                    if (list.size == 7) list else list + List(7 - list.size) { 0 }
                }

                _dailyChart.value = daily
                _weeklyChart.value = weekly
            }
        }
    }
}


