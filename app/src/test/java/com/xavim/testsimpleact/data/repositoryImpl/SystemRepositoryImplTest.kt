package com.xavim.testsimpleact.data.repositoryImpl

import com.xavim.testsimpleact.data.session.SessionManager
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import android.content.Context

class SystemRepositoryImplTest {

    @Mock
    private lateinit var sessionManager: SessionManager

    @Mock
    private lateinit var logger: AndroidLogger

    @Mock
    private lateinit var context: Context

    private lateinit var systemRepository: SystemRepositoryImpl

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)

        systemRepository = SystemRepositoryImpl(sessionManager, logger)
    }

    @Test
    fun `initializeD2 calls sessionManager initD2`() = runTest {
        // Execute
        systemRepository.initializeD2(context)

        // Verify
        verify(sessionManager).initD2(context)
    }

    @Test
    fun `isSessionActive returns value from sessionManager`() {
        // Setup
        `when`(sessionManager.isSessionActive()).thenReturn(true)

        // Execute
        val result = systemRepository.isSessionActive()

        // Verify
        assertTrue(result)
        verify(sessionManager).isSessionActive()
    }

    @Test
    fun `getServerUrl returns value from sessionManager`() {
        // Setup
        `when`(sessionManager.getServerUrl()).thenReturn("https://play.dhis2.org")

        // Execute
        val result = systemRepository.getServerUrl()

        // Verify
        assertEquals("https://play.dhis2.org", result)
        verify(sessionManager).getServerUrl()
    }

    @Test
    fun `getUsername returns value from sessionManager`() {
        // Setup
        `when`(sessionManager.getUsername()).thenReturn("admin")

        // Execute
        val result = systemRepository.getUsername()

        // Verify
        assertEquals("admin", result)
        verify(sessionManager).getUsername()
    }

    @Test
    fun `logError calls logger e method`() {
        // Setup
        val tag = "TestTag"
        val message = "Test message"
        val throwable = RuntimeException("Test exception")

        // Execute
        systemRepository.logError(tag, message, throwable)

        // Verify
        verify(logger).e(tag, message, throwable)
    }
}