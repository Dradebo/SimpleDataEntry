package com.xavim.testsimpleact.data.repositoryImpl

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.xavim.testsimpleact.domain.repository.LoginResult
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.maintenance.D2Error
import org.hisp.dhis.android.core.maintenance.D2ErrorCode
import org.hisp.dhis.android.core.user.UserModule
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import io.reactivex.Single
import java.io.IOException

class AuthRepositoryImplTest {

    @Mock
    private lateinit var context: Context

    @Mock
    private lateinit var d2: D2

    @Mock
    private lateinit var userModule: UserModule

    @Mock
    private lateinit var sharedPreferences: SharedPreferences

    @Mock
    private lateinit var editor: SharedPreferences.Editor

    private lateinit var authRepository: AuthRepositoryImpl

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)

        // Mock D2 provider
        val d2Provider: () -> D2? = { d2 }

        // Mock SharedPreferences
        `when`(context.getSharedPreferences(anyString(), anyInt())).thenReturn(sharedPreferences)
        `when`(sharedPreferences.edit()).thenReturn(editor)
        `when`(editor.putString(anyString(), anyString())).thenReturn(editor)
        `when`(editor.putLong(anyString(), anyLong())).thenReturn(editor)
        `when`(editor.putInt(anyString(), anyInt())).thenReturn(editor)
        `when`(editor.remove(anyString())).thenReturn(editor)

        // Mock D2 and UserModule
        `when`(d2.userModule()).thenReturn(userModule)
        `when`(userModule.isLogged()).thenReturn(Single.just(false))

        // Create repository with mocked dependencies
        authRepository = AuthRepositoryImpl(context, d2Provider)
    }

    @Test
    fun `login with valid credentials returns SUCCESS`() = runTest {
        // Setup
        doNothing().`when`(userModule).blockingLogIn(anyString(), anyString(), anyString())

        // Execute
        val result = authRepository.login("https://play.dhis2.org", "admin", "district")

        // Verify
        assertEquals(LoginResult.SUCCESS, result)
    }

    @Test
    fun `login when already logged in returns SUCCESS`() = runTest {
        // Setup
        `when`(userModule.isLogged()).thenReturn(Single.just(true))

        // Execute
        val result = authRepository.login("https://play.dhis2.org", "admin", "district")

        // Verify
        assertEquals(LoginResult.SUCCESS, result)
        // Verify login was not attempted since already logged in
        verify(userModule, never()).blockingLogIn(anyString(), anyString(), anyString())
    }

    @Test
    fun `login with invalid credentials returns INVALID_CREDENTIALS`() = runTest {
        // Setup
        val d2Error = mock(D2Error::class.java)
        `when`(d2Error.errorCode()).thenReturn(D2ErrorCode.LOGIN_PASSWORD_NULL)

        doThrow(RuntimeException(d2Error))
            .`when`(userModule).blockingLogIn(anyString(), anyString(), anyString())

        // Execute
        val result = authRepository.login("https://play.dhis2.org", "admin", "wrongpass")

        // Verify
        assertEquals(LoginResult.INVALID_CREDENTIALS, result)
    }

    @Test
    fun `login with network error returns NETWORK_ERROR`() = runTest {
        // Setup
        doThrow(IOException("Network error"))
            .`when`(userModule).blockingLogIn(anyString(), anyString(), anyString())

        // Execute
        val result = authRepository.login("https://play.dhis2.org", "admin", "district")

        // Verify
        assertEquals(LoginResult.NETWORK_ERROR, result)
    }
}