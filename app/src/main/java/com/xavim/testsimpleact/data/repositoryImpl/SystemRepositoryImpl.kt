package com.xavim.testsimpleact.data.repositoryImpl

import android.content.Context
import com.xavim.testsimpleact.data.session.SessionManager
import com.xavim.testsimpleact.domain.repository.SystemRepository
import javax.inject.Inject
import javax.inject.Singleton

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

    override fun isSessionActive(): Boolean {
        return sessionManager.isSessionActive()
    }

    override fun getServerUrl(): String {
        return sessionManager.getServerUrl() ?: ""
    }

    override fun getUsername(): String {
        return sessionManager.getUsername() ?: ""
    }

    override fun logError(tag: String, message: String, throwable: Throwable?) {
        logger.e(tag, message, throwable)
    }
}