package com.xavim.testsimpleact.data.session

import android.content.Context
import android.util.Log
import com.xavim.testsimpleact.data.repositoryImpl.AndroidLogger
import com.xavim.testsimpleact.domain.model.Dhis2Config
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.D2Configuration
import org.hisp.dhis.android.core.D2Manager
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionManager @Inject constructor(
    private val logger: AndroidLogger,
    private val preferences: PreferenceProvider
) {
    private var d2: D2? = null

    // Session timeout duration in milliseconds (30 minutes)
    private val SESSION_TIMEOUT_DURATION = 30 * 60 * 1000

    /**
     * Initialize the DHIS2 SDK (D2)
     * @param context The application context
     * @throws Exception if initialization fails
     */
    suspend fun initD2(context: Context) {
        withContext(Dispatchers.IO) {
            try {
                if (d2 == null) {
                    logger.d("SessionManager", "Initializing D2")
                    val d2Config = D2Configuration.builder()
                        .appName("SimpleDataEntry")
                        .appVersion("1.0.0")
                        .readTimeoutInSeconds(30)
                        .connectTimeoutInSeconds(30)
                        .writeTimeoutInSeconds(30)
                        .context(context)
                        .build()

                    d2 = D2Manager.blockingInstantiateD2(d2Config)
                    logger.d("SessionManager", "D2 initialized successfully")
                }
            } catch (e: Exception) {
                logger.e("SessionManager", "Failed to initialize D2", e)
                throw e
            }
        }
    }

    /**
     * Get the D2 instance
     * @return The D2 instance
     * @throws IllegalStateException if D2 is not initialized
     */
    fun getD2(): D2 {
        return d2 ?: throw IllegalStateException("D2 is not initialized")
    }

    /**
     * Login to DHIS2
     * @param serverUrl The server URL
     * @param username The username
     * @param password The password
     * @return Flow emitting true if login was successful, false otherwise
     */
    fun login(serverUrl: String, username: String, password: String): Flow<Boolean> = flow {
        try {
            val loginSuccessful = d2?.userModule()?.blockingLogIn(username, password, serverUrl) ?: false
            if (loginSuccessful as Boolean) {
                onUserInteraction() // Set initial interaction time
                preferences.setValue(Preference.SERVER_URL.toString(), serverUrl)
                preferences.setValue(Preference.USERNAME.toString(), username)
                Log.d("SessionManager", "Login successful")
            }
            emit(loginSuccessful as Boolean)
        } catch (e: Exception) {
            logger.e("SessionManager", "Login failed", e)
            emit(false)
        }
    }

    /**
     * Logout from DHIS2
     * @return Flow emitting true if logout was successful, false otherwise
     */
    fun logout(): Flow<Boolean> = flow {
        try {
            d2?.userModule()?.blockingLogOut()
            preferences.setValue(Preference.SESSION_LOCKED.toString(), false)
            preferences.remove(Preference.PIN_ENABLED.toString())
            emit(true)
        } catch (e: Exception) {
            logger.e("SessionManager", "Logout failed", e)
            emit(false)
        }
    }

    /**
     * Check if the user is logged in
     * @return true if the user is logged in, false otherwise
     */
//    fun isSessionActive(): Boolean {
//        return try {
//            d2?.userModule()?.isLogged()?.blockingGet() ?: false
//        } catch (e: Exception) {
//            logger.e("SessionManager", "Error checking if user is logged in", e)
//            false
//        }
//    }


    fun isSessionActive(): Boolean {
        return try {
            d2?.userModule()?.isLogged()?.blockingGet() ?: false
        } catch (e: Exception) {
            logger.e("SessionManager", "Error checking if user is logged in", e)
            false
        }
    }


    /**
     * Record user interaction to reset the session timeout
     */
    fun onUserInteraction() {
        preferences.setValue(Preference.LAST_USER_INTERACTION.toString(), System.currentTimeMillis())
    }

    /**
     * Check if the session has timed out
     * @return true if the session has timed out, false otherwise
     */
    fun checkSessionTimeout(): Boolean {
        val currentTime = System.currentTimeMillis()
        val lastInteraction = preferences.getLong(Preference.LAST_USER_INTERACTION.toString(), 0L) ?: 0L
        val pinEnabled = preferences.getBoolean(Preference.PIN_ENABLED.toString(), false) ?: false

        return currentTime - lastInteraction > SESSION_TIMEOUT_DURATION && !pinEnabled
    }

    /**
     * Get the server URL
     * @return The server URL or null if not set
     */
    fun getServerUrl(): String? {
        return preferences.getString(Preference.SERVER_URL.toString(), null)
    }

    /**
     * Get the username
     * @return The username or null if not set
     */
    fun getUsername(): String? {
        return preferences.getString(Preference.USERNAME.toString(), null)
    }

    /**
     * Refresh the session
     * @return true if the session was refreshed successfully, false otherwise
     */
    fun refreshSession(): Boolean {
        return try {
            // In a real implementation, this would refresh the session token
            // For now, we just update the last interaction time
            onUserInteraction()
            true
        } catch (e: Exception) {
            logger.e("SessionManager", "Error refreshing session", e)
            false
        }
    }

    /**
     * Check if the session is expired
     * @return true if the session is expired, false otherwise
     */
    fun isSessionExpired(): Boolean {
        return checkSessionTimeout()
    }
}

/**
 * Preference keys used by the SessionManager
 */
enum class Preference {
    SERVER_URL,
    USERNAME,
    LAST_USER_INTERACTION,
    SESSION_LOCKED,
    PIN_ENABLED,
    FLAG,
    THEME,
    TIME_META,
    TIME_DATA
}