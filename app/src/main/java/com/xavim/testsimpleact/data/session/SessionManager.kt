package com.xavim.testsimpleact.data.session

import android.content.Context
import com.xavim.testsimpleact.data.repositoryImpl.AndroidLogger
import com.xavim.testsimpleact.domain.model.Dhis2Config
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.D2Configuration
import org.hisp.dhis.android.core.D2Manager
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class SessionManager @Inject constructor(
    private val logger: AndroidLogger
) {
    private var d2: D2? = null

    suspend fun initD2(context: Context) = withContext(Dispatchers.IO) {
        if (d2 == null) {
            try {
                val config = D2Configuration.builder()
                    .context(context)
                    .appName("Simple Data Entry")
                    .appVersion("1.0")
                    .readTimeoutInSeconds(30)
                    .writeTimeoutInSeconds(30)
                    .build()
                d2 = D2Manager.blockingInstantiateD2(config)
                d2?.metadataModule()?.blockingDownload()
                d2?.aggregatedModule()?.data()?.blockingDownload()
            } catch (e: Exception) {
                logger.e("SessionManager", "D2 initialization failed", e)
                throw e
            }
        }
    }

    suspend fun login(dhis2Config: Dhis2Config) = withContext(Dispatchers.IO) {
        try {
            if (isSessionActive()) {
                logger.d("SessionManager", "Session already active")
                return@withContext
            }

            d2?.userModule()?.blockingLogIn(
                dhis2Config.username,
                dhis2Config.password,
                dhis2Config.serverUrl
            ) ?: throw IllegalStateException("D2 not initialized")

            logger.i("SessionManager", "Login successful for ${dhis2Config.username}")
        } catch (e: Exception) {
            logger.e("SessionManager", "Login failed", e)
            throw e
        }
    }


    /**
     * Checks if the current user session is still active.
     */
    fun isSessionActive(): Boolean {
        return d2?.userModule()?.isLogged()?.blockingGet() ?: false
    }

    /**
     * Logs the current user out of DHIS2 (if active session).
     */
    fun logout() {
        try {
            d2?.userModule()?.blockingLogOut()
        } catch (e: Exception) {
            logger.e("SessionManager", "Logout error: ${e.message}", e)
        }
    }

    /**
     * Provides the D2 object to other layers if needed.
     */
    fun getD2(): D2? = d2
}
