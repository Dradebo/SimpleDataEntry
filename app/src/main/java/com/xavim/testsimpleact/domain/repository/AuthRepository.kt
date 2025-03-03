package com.xavim.testsimpleact.domain.repository

import kotlinx.coroutines.flow.Flow

enum class LoginResult {
    SUCCESS,
    INVALID_CREDENTIALS,
    SERVER_ERROR,
    NETWORK_ERROR,
    ACCOUNT_LOCKED
}

data class SessionInfo(
    val isLoggedIn: Boolean,
    val username: String? = null,
    val serverUrl: String? = null,
    val lastLoginTime: Long? = null
)

interface AuthRepository {
    suspend fun login(serverUrl: String, username: String, password: String): LoginResult
    suspend fun logout()

    fun getSessionInfo(): Flow<SessionInfo>
    suspend fun validateSession(): Boolean

    suspend fun storeCredentials(serverUrl: String, username: String, password: String)
    suspend fun getStoredCredentials(): Triple<String?, String?, String?>
    suspend fun clearStoredCredentials()

    suspend fun verifyStoredCredentials(): Boolean

    // For adding failed login attempts tracking
    suspend fun recordFailedLoginAttempt(username: String)
    suspend fun getFailedLoginAttempts(username: String): Int
    suspend fun resetFailedLoginAttempts(username: String)

    // For session timeout management
    suspend fun isSessionExpired(): Boolean
    suspend fun refreshSession()
}