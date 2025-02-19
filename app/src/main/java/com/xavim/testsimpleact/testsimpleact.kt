package com.xavim.testsimpleact

import android.app.Application
import android.util.Log
import com.xavim.testsimpleact.domain.repository.SystemRepository
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@HiltAndroidApp
class testsimpleact: Application() {

    @Inject lateinit var systemRepository: SystemRepository

    override fun onCreate() {
        super.onCreate()
        initializeApp()
    }

    private fun initializeApp() {
        // No longer blocking login here - just initialize D2
        runBlocking {
            try {
                systemRepository.initializeD2(this@testsimpleact)
                Log.d("Application", "D2 initialized successfully")
            } catch (e: Exception) {
                Log.e("Application", "D2 initialization failed", e)
            }
        }
    }
}