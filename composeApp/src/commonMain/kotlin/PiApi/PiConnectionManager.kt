package PiApi

import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.serialization.kotlinx.json.json

class PiConnectionManager(
    private val baseUrl: String
) {
    private val client = HttpClient {
        install(ContentNegotiation) { json() }
    }

    suspend fun connect(): Boolean {
        return try {
            client.get("$baseUrl/ping")
            true
        } catch (e: Exception) {
            false
        }
    }
}
