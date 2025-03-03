package com.xavim.testsimpleact.domain.repository

import android.content.Context

/**
 * Repository interface for system-level operations
 */
interface SystemRepository {
    /**
     * Initialize the DHIS2 SDK (D2)
     * @param context The application context
     * @throws Exception if initialization fails
     */
    suspend fun initializeD2(context: Context)

    /**
     * Check if the user has an active session
     * @return true if the user has an active session, false otherwise
     */
    fun isSessionActive(): Boolean

    /**
     * Get the current server URL
     * @return The server URL as a string
     */
    fun getServerUrl(): String

    /**
     * Get the current user's username
     * @return The username as a string
     */
    fun getUsername(): String

    /**
     * Log an error
     * @param tag The tag for the error
     * @param message The error message
     * @param throwable The throwable that caused the error
     */
    fun logError(tag: String, message: String, throwable: Throwable? = null)
}