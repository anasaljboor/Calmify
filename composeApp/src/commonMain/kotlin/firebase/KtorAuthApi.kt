package firebase

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.http.*

class KtorAuthApi(
    private val client: HttpClient,
    private val baseUrl: String
) : AuthApi {

    override suspend fun login(email: String, password: String): AuthResponse =
        safePost("$baseUrl/login", AuthRequest(email = email, password = password))

    override suspend fun signup(email: String, password: String, username: String): AuthResponse =
        safePost(
            "$baseUrl/signup",
            AuthRequest(email = email, password = password, username = username)
        )

    private suspend inline fun <reified T> safePost(url: String, bodyObj: Any): T {
        try {
            return client.post(url) {
                contentType(ContentType.Application.Json)
                setBody(bodyObj)
            }.body()
        } catch (e: ClientRequestException) {
            val msg = e.response.body<String>()
            throw IllegalStateException("HTTP ${e.response.status.value}: $msg")
        } catch (e: ServerResponseException) {
            val msg = e.response.body<String>()
            throw IllegalStateException("HTTP ${e.response.status.value}: $msg")
        }
    }


    override suspend fun me(token: String): MeResponse {
        return client.get("$baseUrl/me") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }.body()
    }
    override suspend fun postSimulation(token: String, req: SimulationRequest): Map<String, String> {
        return client.post("$baseUrl/simulation") {
            header(HttpHeaders.Authorization, "Bearer $token")
            contentType(ContentType.Application.Json)
            setBody(req)
        }.body()
    }

    override suspend fun latestSimulation(token: String): LatestSimulationResponse {
        return client.get("$baseUrl/simulation/latest") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }.body()
    }

    override suspend fun latestSimulationCharts(token: String): ChartsResponse {
        return client.get("$baseUrl/simulation/latest/charts") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }.body()
    }




}
