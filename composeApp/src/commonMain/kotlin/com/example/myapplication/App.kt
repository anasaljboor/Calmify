import PiApi.PiConnectionManager
import Screens.*
import Wearable_Hub.AppGraph
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import com.example.myapplication.Route
import kotlinx.coroutines.launch

import kotlinx.coroutines.flow.collectLatest
import firebase.AuthState
import androidx.compose.runtime.collectAsState // <-- make sure this exists
import androidx.compose.ui.Modifier
import firebase.SimulationRequest
import kotlin.time.Clock

@Composable
fun AppRoot(graph: AppGraph) {
    var route by remember { mutableStateOf<Route>(Route.Login) }

    val scope = rememberCoroutineScope()
    var showMenu by remember { mutableStateOf(false) }
    val piConnected = remember { mutableStateOf(false) }
    val piManager = remember {
        PiConnectionManager("http://192.168.1.50:8000") // or the right ip for the raspberry pi
    }

    val authState by graph.repo.state.collectAsState(initial = AuthState.Unauthenticated)


    LaunchedEffect(Unit) {
        graph.stressStore.events.collectLatest { ev ->
            if (ev is Wearable_Hub.StressEvent.TriggerBreathing) {
                route = Route.Breathing
            }
        }
    }
    LaunchedEffect(Unit) {
        graph.stressStore.setAppOpened(true)
    }
    LaunchedEffect(piConnected.value) {
        if (piConnected.value) {
            // ready to control Pi
            println("Pi connected ")
        } else {
            println("Pi not reachable ")
        }
    }


    LaunchedEffect(authState) {
        if (authState is AuthState.Authenticated) {
            graph.stressStore.setLoggedIn(true)
            route = Route.Home
        }
    }
    LaunchedEffect(route, authState) {
        val s = authState
        if (s !is AuthState.Authenticated) return@LaunchedEffect

        // Only post while session is running (simulation active)
        while (true) {
            val running = graph.stressStore.isSessionRunning()
            if (!running) break

            val newSamples = graph.stressStore.drainPendingSamples()
            if (newSamples.isNotEmpty()) {
                val req = SimulationRequest(
                    startedAtMs = graph.stressStore.sessionStartedAtMs(),
                    endedAtMs = Clock.System.now().toEpochMilliseconds(),
                    samples = newSamples, // only new ones each second
                    summary = graph.stressStore.sessionSummaryForApi() // overall summary
                )

                runCatching {
                    graph.api.postSimulation(s.accessToken, req)
                    val latest = graph.api.latestSimulation(s.accessToken)
                    graph.latestSim.value = latest.latest
                }.onFailure {
                    println("POST EACH-SEC FAIL: ${it.message}")
                }
            }

            kotlinx.coroutines.delay(1000)
        }
    }



    when (route) {
        Route.Login -> LoginScreen(
            onLogin = { email, pass ->
                scope.launch {
                    graph.repo.login(email.trim(), pass)
                    // no need to manually check state here anymore
                }
            },
            onSignUp = { route = Route.SignUp }
        )

        Route.SignUp -> SignUpScreen(
            onSignUp = { email, pass, username ->
                scope.launch {
                    graph.repo.signup(email.trim(), pass, username.trim())
                }
            },
            onBackToLogin = { route = Route.Login }
        )


        Route.Home -> {
            Box(Modifier.fillMaxSize()) {
                HomeScreen(
                    graph = graph,
                    userName = when (val s = authState) {
                        is AuthState.Authenticated -> s.username ?: "User"
                        else -> "User"
                    },
                    onOpenMenu = { showMenu = true },
                    onOpenBreathing = { route = Route.Breathing },
                    onStartSimulation = {
                        graph.sourceMode.value = Wearable_Hub.SourceMode.SIM_ONLY
                        graph.stressStore.tryStart()

                        scope.launch {
                            kotlinx.coroutines.delay(5000) // 5s demo
                            route = Route.Breathing
                        }
                    }

                )

                if (showMenu) {
                    SideMenu(
                        name = when (val s = authState) {
                            is AuthState.Authenticated -> s.username ?: "User"
                            else -> "User"
                        },
                        email = when (val s = authState) {
                            is AuthState.Authenticated -> s.email ?: ""
                            else -> ""
                        },
                        onClose = { showMenu = false },
                        onLogout = {
                            scope.launch { graph.repo.logout() }
                            showMenu = false
                            route = Route.Login
                        }
                    )
                }
            }
        }


        Route.Breathing -> BreathingExerciseScreen(
            graph = graph,
            onBack = { route = Route.Home },
            onEndSession = {
                val s = authState
                if (s is AuthState.Authenticated) {
                    scope.launch {
                        runCatching {
                            // 1) stop collecting samples first
                            graph.stressStore.stopSession()

                            // 2) send any remaining samples that were not posted yet
                            val finalSamples = graph.stressStore.drainPendingSamples()

                            val req = SimulationRequest(
                                startedAtMs = graph.stressStore.sessionStartedAtMs(),
                                endedAtMs = Clock.System.now().toEpochMilliseconds(),
                                samples = finalSamples, // last batch (can be empty)
                                summary = graph.stressStore.sessionSummaryForApi()
                            )

                            // Post final batch (optional: only if you want)
                            graph.api.postSimulation(s.accessToken, req)

                            // refresh latest after post
                            val latest = graph.api.latestSimulation(s.accessToken)
                            graph.latestSim.value = latest.latest
                        }.onFailure {
                            println("FINAL POST FAIL: ${it.message}")
                        }

                        route = Route.Home
                    }
                } else {
                    route = Route.Home
                }
            }
        )

    }
}