package firebase

sealed class AuthState {
    data object Loading : AuthState()
    data object Unauthenticated : AuthState()

    data class Authenticated(
        val userId: String,
        val accessToken: String,
        val username: String? = null,
        val email: String? = null
    ) : AuthState()


    data class Error(val message: String) : AuthState()
}
