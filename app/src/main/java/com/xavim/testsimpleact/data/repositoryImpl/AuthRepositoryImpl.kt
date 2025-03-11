package com.xavim.testsimpleact.data.repositoryImpl

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.xavim.testsimpleact.domain.repository.AuthRepository
import com.xavim.testsimpleact.domain.repository.LoginResult
import com.xavim.testsimpleact.domain.repository.SessionInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.maintenance.D2Error
import org.hisp.dhis.android.core.maintenance.D2ErrorCode
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val context: Context,
    private val d2Provider: () -> D2?
) : AuthRepository {

    companion object {
        private const val PREFS_NAME = "dhis2_secure_prefs"
        private const val KEY_SERVER_URL = "server_url"
        private const val KEY_USERNAME = "username"
        private const val KEY_PASSWORD = "password"
        private const val KEY_LAST_LOGIN_TIME = "last_login_time"
        private const val KEY_FAILED_ATTEMPTS_PREFIX = "failed_attempts_"
        private const val MAX_FAILED_ATTEMPTS = 5
        private const val SESSION_TIMEOUT_MINUTES = 30L
    }

    private val securePreferences: SharedPreferences by lazy {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        EncryptedSharedPreferences.create(
            context,
            PREFS_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    private val _sessionInfo = MutableStateFlow(SessionInfo(isLoggedIn = false))

    init {
        // Initialize session state
        val serverUrl = securePreferences.getString(KEY_SERVER_URL, null)
        val username = securePreferences.getString(KEY_USERNAME, null)
        val isLoggedIn = (d2Provider()?.userModule()?.blockingIsLogged() == true)

        _sessionInfo.value = SessionInfo(
            isLoggedIn = isLoggedIn,
            username = username,
            serverUrl = serverUrl,
            lastLoginTime = securePreferences.getLong(KEY_LAST_LOGIN_TIME, 0)
        )
    }

    override suspend fun login(serverUrl: String, username: String, password: String): LoginResult {
        return withContext(Dispatchers.IO) {
            try {
                // Check for too many failed attempts
                val attempts = getFailedLoginAttempts(username)
                if (attempts >= MAX_FAILED_ATTEMPTS) {
                    return@withContext LoginResult.ACCOUNT_LOCKED
                }

                val d2 = d2Provider() ?: return@withContext LoginResult.SERVER_ERROR

                try {
                    d2.userModule().blockingLogIn(username, password, serverUrl)

                    // If login successful, store credentials and reset failed attempts
                    storeCredentials(serverUrl, username, password)
                    resetFailedLoginAttempts(username)

                    val lastLoginTime = System.currentTimeMillis()
                    securePreferences.edit().putLong(KEY_LAST_LOGIN_TIME, lastLoginTime).apply()

                    _sessionInfo.value = SessionInfo(
                        isLoggedIn = true,
                        username = username,
                        serverUrl = serverUrl,
                        lastLoginTime = lastLoginTime
                    )

                    return@withContext LoginResult.SUCCESS
                } catch (e: Exception) {
                    Log.e("AuthRepository", "Login error", e)

                    // Record failed attempt unless it's an "already authenticated" error
                    if (!isAlreadyAuthenticatedError(e)) {
                        recordFailedLoginAttempt(username)
                    } else {
                        // If already authenticated, consider it a success
                        return@withContext LoginResult.SUCCESS
                    }

                    // Process specific D2Errors
                    when {
                        isAlreadyAuthenticatedError(e) -> return@withContext LoginResult.SUCCESS
                        isBadCredentialsError(e) -> return@withContext LoginResult.INVALID_CREDENTIALS
                        isServerError(e) -> return@withContext LoginResult.SERVER_ERROR
                        isNetworkError(e) -> return@withContext LoginResult.NETWORK_ERROR
                        else -> return@withContext LoginResult.SERVER_ERROR
                    }
                }
            } catch (e: Exception) {
                Log.e("AuthRepository", "Unexpected login error", e)
                return@withContext LoginResult.SERVER_ERROR
            }
        }
    }

    // Helper methods to check error types
    private fun isAlreadyAuthenticatedError(e: Exception): Boolean {
        val message = e.toString().lowercase()
        return message.contains("already_authenticated") ||
                message.contains("already authenticated")
    }

    private fun isBadCredentialsError(e: Exception): Boolean {
        val message = e.toString().lowercase()
        return message.contains("bad_credentials") ||
                message.contains("invalid credentials")
    }

    private fun isServerError(e: Exception): Boolean {
        val message = e.toString().lowercase()
        return message.contains("socket_timeout") ||
                message.contains("api_response_process_error") ||
                message.contains("no_dhis2_server")
    }

    private fun isNetworkError(e: Exception): Boolean {
        val message = e.toString().lowercase()
        return message.contains("unknown_host") ||
                message.contains("network_error")
    }
    override suspend fun logout() {
        withContext(Dispatchers.IO) {
            try {
                d2Provider()?.userModule()?.blockingLogOut()
                clearStoredCredentials()
                _sessionInfo.value = SessionInfo(isLoggedIn = false)
            } catch (e: Exception) {
                Log.e("AuthRepositoryImpl", "Logout error", e)
            }
        }
    }

    override fun getSessionInfo(): Flow<SessionInfo> = _sessionInfo.asStateFlow()

    override suspend fun validateSession(): Boolean {
        return withContext(Dispatchers.IO) {
            val d2 = d2Provider() ?: return@withContext false
            val isLogged = d2.userModule().blockingIsLogged()

            // Update our session info if the state has changed
            if (isLogged != _sessionInfo.value.isLoggedIn) {
                _sessionInfo.value = _sessionInfo.value.copy(isLoggedIn = isLogged)
            }

            return@withContext isLogged && !isSessionExpired()
        }
    }

    override suspend fun storeCredentials(serverUrl: String, username: String, password: String) {
        withContext(Dispatchers.IO) {
            securePreferences.edit().apply {
                putString(KEY_SERVER_URL, serverUrl)
                putString(KEY_USERNAME, username)
                putString(KEY_PASSWORD, password)
            }.apply()
        }
    }

    override suspend fun getStoredCredentials(): Triple<String?, String?, String?> {
        return withContext(Dispatchers.IO) {
            val serverUrl = securePreferences.getString(KEY_SERVER_URL, null)
            val username = securePreferences.getString(KEY_USERNAME, null)
            val password = securePreferences.getString(KEY_PASSWORD, null)
            Triple(serverUrl, username, password)
        }
    }

    override suspend fun clearStoredCredentials() {
        withContext(Dispatchers.IO) {
            securePreferences.edit().apply {
                remove(KEY_SERVER_URL)
                remove(KEY_USERNAME)
                remove(KEY_PASSWORD)
                // Don't remove failed attempts to prevent brute force attacks
            }.apply()
        }
    }

    override suspend fun verifyStoredCredentials(): Boolean {
        val (serverUrl, username, password) = getStoredCredentials()

        if (serverUrl == null || username == null || password == null) {
            return false
        }

        return when (login(serverUrl, username, password)) {
            LoginResult.SUCCESS -> true
            else -> false
        }
    }

    override suspend fun recordFailedLoginAttempt(username: String) {
        withContext(Dispatchers.IO) {
            val key = KEY_FAILED_ATTEMPTS_PREFIX + username
            val currentAttempts = securePreferences.getInt(key, 0)
            securePreferences.edit().putInt(key, currentAttempts + 1).apply()
        }
    }

    override suspend fun getFailedLoginAttempts(username: String): Int {
        return withContext(Dispatchers.IO) {
            val key = KEY_FAILED_ATTEMPTS_PREFIX + username
            securePreferences.getInt(key, 0)
        }
    }

    override suspend fun resetFailedLoginAttempts(username: String) {
        withContext(Dispatchers.IO) {
            val key = KEY_FAILED_ATTEMPTS_PREFIX + username
            securePreferences.edit().remove(key).apply()
        }
    }

    override suspend fun isSessionExpired(): Boolean {
        val lastLoginTime = _sessionInfo.value.lastLoginTime ?: return true
        val currentTime = System.currentTimeMillis()
        val sessionTimeoutMillis = TimeUnit.MINUTES.toMillis(SESSION_TIMEOUT_MINUTES)

        return (currentTime - lastLoginTime) > sessionTimeoutMillis
    }

    override suspend fun refreshSession() {
        withContext(Dispatchers.IO) {
            securePreferences.edit()
                .putLong(KEY_LAST_LOGIN_TIME, System.currentTimeMillis())
                .apply()

            _sessionInfo.value = _sessionInfo.value.copy(
                lastLoginTime = System.currentTimeMillis()
            )
        }
    }
}