package com.xavim.testsimpleact.presentation.features.login

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.xavim.testsimpleact.domain.repository.LoginResult
import com.xavim.testsimpleact.domain.repository.SessionInfo
import com.xavim.testsimpleact.domain.usecase.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations

@ExperimentalCoroutinesApi
class LoginViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Mock
    private lateinit var loginUseCase: LoginUseCase

    @Mock
    private lateinit var logoutUseCase: LogoutUseCase

    @Mock
    private lateinit var getSessionInfoUseCase: GetSessionInfoUseCase

    @Mock
    private lateinit var validateSessionUseCase: ValidateSessionUseCase

    @Mock
    private lateinit var verifyCredentialsUseCase: VerifyCredentialsUseCase

    @Mock
    private lateinit var getStoredCredentialsUseCase: GetStoredCredentialsUseCase

    private lateinit var viewModel: LoginViewModel
    private val testDispatcher = StandardTestDispatcher()
    private val sessionInfoFlow = MutableStateFlow(SessionInfo(isLoggedIn = false, lastLoginTime = 0L))

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        Dispatchers.setMain(testDispatcher)

        `when`(getSessionInfoUseCase.invoke()).thenReturn(sessionInfoFlow)

        viewModel = LoginViewModel(
            loginUseCase,
            logoutUseCase,
            getSessionInfoUseCase,
            validateSessionUseCase,
            verifyCredentialsUseCase,
            getStoredCredentialsUseCase
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `login with valid credentials sets Success state and emits NavigateToDatasets event`() = runTest {
        // Setup
        `when`(loginUseCase.invoke(anyString(), anyString(), anyString())).thenReturn(LoginResult.SUCCESS)

        // Set form values
        viewModel.onServerUrlChanged("https://play.dhis2.org")
        viewModel.onUsernameChanged("admin")
        viewModel.onPasswordChanged("district")

        // Advance time to process form validation
        testScheduler.advanceUntilIdle()

        // Trigger login
        viewModel.login()

        // Advance time to process login
        testScheduler.advanceUntilIdle()

        // Verify state and navigation
        assertEquals(LoginState.Success, viewModel.loginState.value)
        assertEquals(NavigationEvent.NavigateToDatasets, viewModel.navigationEvent.equals(NavigationEvent.NavigateToDatasets))
    }

    @Test
    fun `login with invalid credentials sets Error state and emits ShowMessage event`() = runTest {
        // Setup
        `when`(loginUseCase.invoke(anyString(), anyString(), anyString())).thenReturn(LoginResult.INVALID_CREDENTIALS)

        // Set form values
        viewModel.onServerUrlChanged("https://play.dhis2.org")
        viewModel.onUsernameChanged("admin")
        viewModel.onPasswordChanged("wrongpassword")

        // Advance time to process form validation
        testScheduler.advanceUntilIdle()

        // Trigger login
        viewModel.login()

        // Advance time to process login
        testScheduler.advanceUntilIdle()

        // Verify state and navigation
        assertTrue(viewModel.loginState.value is LoginState.Error)
        assertTrue(viewModel.navigationEvent.equals(NavigationEvent.ShowMessage("Invalid username or password")) )

    }

    @Test
    fun `checkActiveSession navigates to datasets when session is valid`() = runTest {
        // Setup
        `when`(validateSessionUseCase.invoke()).thenReturn(true)

        // Call the function
        viewModel.checkActiveSession()

        // Advance time to process
        testScheduler.advanceUntilIdle()

        // Verify navigation
        assertEquals(NavigationEvent.NavigateToDatasets, viewModel.navigationEvent.equals(NavigationEvent.NavigateToDatasets))
    }

    @Test
    fun `existing active session should navigate to datasets without login attempt`() = runTest {
        // Setup active session
        sessionInfoFlow.value = SessionInfo(isLoggedIn = true, username = "admin", lastLoginTime = System.currentTimeMillis())
        `when`(validateSessionUseCase.invoke()).thenReturn(true)

        // Create new viewModel to trigger the init block with active session
        val newViewModel = LoginViewModel(
            loginUseCase,
            logoutUseCase,
            getSessionInfoUseCase,
            validateSessionUseCase,
            verifyCredentialsUseCase,
            getStoredCredentialsUseCase
        )

        // Advance time to process
        testScheduler.advanceUntilIdle()

        // Verify navigation event is emitted without calling login
        assertEquals(NavigationEvent.NavigateToDatasets, newViewModel.navigationEvent.equals(NavigationEvent.NavigateToDatasets))
        verify(loginUseCase, never()).invoke(anyString(), anyString(), anyString())
    }
}