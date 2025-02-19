package com.xavim.testsimpleact.domain.repository

import android.content.Context

interface AuthRepository {
    suspend fun login(serverUrl: String, username: String, password: String): Boolean
    fun isLoggedIn(): Boolean
}

interface SystemRepository {
    suspend fun initializeD2(context: Context)
}