package com.xavim.testsimpleact.domain.repository

import javax.inject.Inject

interface Logger {
    fun d(tag: String, message: String)
    fun i(tag: String, message: String)
    fun e(tag: String, message: String, throwable: Throwable? = null)
}