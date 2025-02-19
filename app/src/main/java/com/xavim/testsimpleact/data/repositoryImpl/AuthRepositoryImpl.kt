package com.xavim.testsimpleact.data.repositoryImpl

import android.content.Context
import com.xavim.testsimpleact.domain.model.Dhis2Config
import com.xavim.testsimpleact.data.session.SessionManager
import com.xavim.testsimpleact.domain.repository.AuthRepository
import com.xavim.testsimpleact.domain.repository.SystemRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val sessionManager: SessionManager,
    private val logger: AndroidLogger
) : AuthRepository {

    override suspend fun login(serverUrl: String, username: String, password: String): Boolean {
        logger.d("AuthRepository", "Attempting login for $username")
        return try {
            sessionManager.login(Dhis2Config(serverUrl, username, password))
            true
        } catch (e: Exception) {
            logger.e("AuthRepository", "Login failed for $username", e)
            false
        }
    }

    override fun isLoggedIn(): Boolean = sessionManager.isSessionActive()
}

@Singleton
class SystemRepositoryImpl @Inject constructor(
    private val sessionManager: SessionManager,
    private val logger: AndroidLogger
) : SystemRepository {

    override suspend fun initializeD2(context: Context) {
        logger.d("SystemRepository", "Initializing D2")
        try {
            sessionManager.initD2(context)
        } catch (e: Exception) {
            logger.e("SystemRepository", "D2 initialization failed", e)
            throw e
        }
    }
}