package firebase

interface AuthApi {
    suspend fun login(email: String, password: String): AuthResponse
    suspend fun signup(email: String, password: String, username: String): AuthResponse
    suspend fun me(token: String): MeResponse

    suspend fun postSimulation(token: String, req: SimulationRequest): Map<String, String>
    suspend fun latestSimulation(token: String): LatestSimulationResponse


    suspend fun latestSimulationCharts(token: String): ChartsResponse

}
