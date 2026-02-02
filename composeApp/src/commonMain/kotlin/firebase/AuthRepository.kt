package firebase

import kotlinx.coroutines.flow.StateFlow

interface AuthRepository {
    val state: StateFlow<AuthState>

    suspend fun login(email: String, password: String)
    suspend fun signup(email: String, password: String, username: String)
    suspend fun logout()
}
