package com.itsdhruvbhardwaj.picme.data.repository

import com.itsdhruvbhardwaj.picme.data.local.AuthTokenStorage
import com.itsdhruvbhardwaj.picme.data.model.*
import com.itsdhruvbhardwaj.picme.data.remote.AuthApiService
import retrofit2.Response
import java.io.IOException

/**
 * Repository responsible for managing authenticated sessions for PicMe.
 * Orchestrates calls between [AuthApiService] and [AuthTokenStorage].
 */
class AuthRepository(
    private val apiService: AuthApiService,
    private val tokenStorage: AuthTokenStorage
) {

    suspend fun login(email: String, password: String): Result<User> {
        return safeApiCall(
            call = { apiService.login(LoginRequest(email, password)) },
            onSuccess = { response ->
                val user = response.user
                val accessToken = response.accessToken
                val refreshToken = response.refreshToken

                if (user != null && accessToken != null && refreshToken != null) {
                    tokenStorage.saveTokens(accessToken, refreshToken)
                    Result.success(user)
                } else {
                    Result.failure(Exception("Incomplete response from server"))
                }
            }
        )
    }

    suspend fun googleAuth(idToken: String): Result<User> {
        return safeApiCall(
            call = { apiService.googleAuth(GoogleAuthRequest(idToken)) },
            onSuccess = { response ->
                val user = response.user
                val accessToken = response.accessToken
                val refreshToken = response.refreshToken

                if (user != null && accessToken != null && refreshToken != null) {
                    tokenStorage.saveTokens(accessToken, refreshToken)
                    Result.success(user)
                } else {
                    Result.failure(Exception("Incomplete response from server"))
                }
            }
        )
    }

    suspend fun refreshSession(): Result<Unit> {
        val refreshToken = tokenStorage.getRefreshToken()
            ?: return Result.failure(Exception("No refresh token available"))

        return safeApiCall(
            call = { apiService.refreshToken(RefreshTokenRequest(refreshToken)) },
            onSuccess = { response ->
                val newAccessToken = response.accessToken
                val newRefreshToken = response.refreshToken

                if (newAccessToken != null && newRefreshToken != null) {
                    // Replace BOTH stored tokens (token rotation)
                    tokenStorage.saveTokens(newAccessToken, newRefreshToken)
                    Result.success(Unit)
                } else {
                    Result.failure(Exception("Failed to rotate tokens"))
                }
            }
        )
    }

    suspend fun getCurrentUser(): Result<User> {
        val accessToken = tokenStorage.getAccessToken()
            ?: return Result.failure(Exception("No access token available"))

        return safeApiCall(
            call = { apiService.getCurrentUser("Bearer $accessToken") },
            onSuccess = { response ->
                Result.success(response.user)
            }
        )
    }

    suspend fun logout(): Result<Unit> {
        val accessToken = tokenStorage.getAccessToken()
        
        // Best effort backend logout
        if (accessToken != null) {
            try {
                apiService.logout("Bearer $accessToken")
            } catch (e: Exception) {
                // Ignore backend failure for logout
            }
        }

        // Always clear local tokens
        tokenStorage.clearTokens()
        return Result.success(Unit)
    }

    suspend fun logoutAll(): Result<Unit> {
        val accessToken = tokenStorage.getAccessToken()

        // Best effort backend logout-all
        if (accessToken != null) {
            try {
                apiService.logoutAll("Bearer $accessToken")
            } catch (e: Exception) {
                // Ignore backend failure
            }
        }

        // Always clear local tokens
        tokenStorage.clearTokens()
        return Result.success(Unit)
    }

    /**
     * Helper to safely execute API calls and map responses to [Result].
     */
    private suspend fun <T, R> safeApiCall(
        call: suspend () -> Response<T>,
        onSuccess: (T) -> Result<R>
    ): Result<R> {
        return try {
            val response = call()
            val body = response.body()

            if (response.isSuccessful && body != null) {
                onSuccess(body)
            } else {
                val errorMessage = response.errorBody()?.string() ?: "Unknown error"
                Result.failure(Exception("API Error ${response.code()}: $errorMessage"))
            }
        } catch (e: IOException) {
            Result.failure(Exception("Network error: Check your connection"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
