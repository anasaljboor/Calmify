package firebase

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class FastApiAuthRepository(
    private val api: AuthApi
) : AuthRepository {

    private val _state = MutableStateFlow<AuthState>(AuthState.Unauthenticated)
    override val state: StateFlow<AuthState> = _state.asStateFlow()

    override suspend fun login(email: String, password: String) {
        _state.value = AuthState.Loading
        try {
            val res = api.login(email, password)

            val me = api.me(res.accessToken)

            _state.value = AuthState.Authenticated(
                userId = res.userId,
                accessToken = res.accessToken,
                username = me.username,
                email = me.email
            )
        } catch (t: Throwable) {
            _state.value = AuthState.Error(t.message ?: "Login failed")
            _state.value = AuthState.Unauthenticated
        }
    }



    override suspend fun signup(email: String, password: String, username: String) {
        _state.value = AuthState.Loading
        try {
            val res = api.signup(email, password, username)

            val me = api.me(res.accessToken)

            _state.value = AuthState.Authenticated(
                userId = res.userId,
                accessToken = res.accessToken,
                username = me.username ?: username, // fallback
                email = me.email ?: email
            )
        } catch (t: Throwable) {
            _state.value = AuthState.Error(t.message ?: "Signup failed")
            _state.value = AuthState.Unauthenticated
        }
    }




    override suspend fun logout() {
        _state.value = AuthState.Unauthenticated
    }
}
