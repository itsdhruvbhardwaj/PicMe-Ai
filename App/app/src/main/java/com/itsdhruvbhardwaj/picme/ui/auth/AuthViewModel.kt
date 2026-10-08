package com.itsdhruvbhardwaj.picme.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.itsdhruvbhardwaj.picme.data.model.User
import com.itsdhruvbhardwaj.picme.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel responsible for managing authentication state and session restoration.
 * Communicates with [AuthRepository] to perform authentication actions.
 */
class AuthViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.CheckingSession)
    
    /**
     * Observable stream of the current authentication state.
     */
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    init {
        checkSession()
    }

    /**
     * Checks for an existing session and attempts restoration if a refresh token exists.
     * Handles access token expiration by attempting a refresh automatically.
     */
    fun checkSession() {
        viewModelScope.launch {
            _authState.value = AuthState.CheckingSession
            
            val userResult = authRepository.getCurrentUser()
            
            if (userResult.isSuccess) {
                _authState.value = AuthState.Authenticated(userResult.getOrThrow())
            } else {
                // If getCurrentUser fails, it might be an expired token or missing session.
                // We attempt to refresh the session.
                val refreshResult = authRepository.refreshSession()
                
                if (refreshResult.isSuccess) {
                    // Retry fetching user after successful token rotation.
                    val retryUserResult = authRepository.getCurrentUser()
                    if (retryUserResult.isSuccess) {
                        _authState.value = AuthState.Authenticated(retryUserResult.getOrThrow())
                    } else {
                        // Profile fetch failed even after refresh.
                        _authState.value = AuthState.Unauthenticated
                    }
                } else {
                    // Refresh failed (token expired/revoked) or no token exists.
                    _authState.value = AuthState.Unauthenticated
                }
            }
        }
    }

    /**
     * Performs standard email/password login.
     */
    fun login(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Authenticating
            val result = authRepository.login(email, password)
            handleAuthResult(result)
        }
    }

    /**
     * Performs authentication using a Google ID token.
     */
    fun loginWithGoogle(idToken: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Authenticating
            val result = authRepository.googleAuth(idToken)
            handleAuthResult(result)
        }
    }

    /**
     * Logs out the user from the current session and clears local state.
     */
    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            _authState.value = AuthState.Unauthenticated
        }
    }

    /**
     * Revokes all active sessions for the user and clears local state.
     */
    fun logoutAll() {
        viewModelScope.launch {
            authRepository.logoutAll()
            _authState.value = AuthState.Unauthenticated
        }
    }

    private fun handleAuthResult(result: Result<User>) {
        result.fold(
            onSuccess = { user ->
                _authState.value = AuthState.Authenticated(user)
            },
            onFailure = { error ->
                _authState.value = AuthState.Error(error.message ?: "Authentication failed")
            }
        )
    }
}
