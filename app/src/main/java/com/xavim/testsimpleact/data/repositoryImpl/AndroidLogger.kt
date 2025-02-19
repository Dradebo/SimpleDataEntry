package com.xavim.testsimpleact.data.repositoryImpl

import android.util.Log
import com.xavim.testsimpleact.domain.repository.Logger
import javax.inject.Inject
class AndroidLogger @Inject constructor() : Logger{

        override fun d(tag: String, message: String) {
            Log.d(tag, message)
        }

        override fun i(tag: String, message: String) {
            Log.i(tag, message)
        }

        override fun e(tag: String, message: String, throwable: Throwable?) {
            Log.e(tag, message, throwable)
        }
    }
