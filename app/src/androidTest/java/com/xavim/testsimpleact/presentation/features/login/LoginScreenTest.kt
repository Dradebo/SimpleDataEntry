package com.xavim.testsimpleact.presentation.features.login

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.xavim.testsimpleact.domain.repository.LoginResult
import com.xavim.testsimpleact.domain.repository.SessionInfo
import com.xavim.testsimpleact.domain.usecase.*
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations

@RunWith(AndroidJUnit4::class)
class LoginScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

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
    private val sessionInfoFlow = MutableStateFlow(SessionInfo(isLoggedIn = false, lastLoginTime = 0L))

    @Before
    suspend fun setup() {
        MockitoAnnotations.openMocks(this)

        `when`(getSessionInfoUseCase.invoke()).thenReturn(sessionInfoFlow)
        `when`(getStoredCredentialsUseCase.invoke()).thenReturn(Triple(null, null, null))

        viewModel = LoginViewModel(
            loginUseCase,
            logoutUseCase,
            getSessionInfoUseCase,
            validateSessionUseCase,
            verifyCredentialsUseCase,
            getStoredCredentialsUseCase
        )
    }

    @Test
    fun loginScreen_displays_all_required_elements() {
        // Set up the compose content
        composeTestRule.setContent {
            LoginScreen(
                viewModel = viewModel,
                onLoginSuccess = {}
            )
        }

        // Verify all elements are displayed
        composeTestRule.onNodeWithText("DHIS2 Login").assertIsDisplayed()
        composeTestRule.onNodeWithText("Server URL").assertIsDisplayed()
        composeTestRule.onNodeWithText("Username").assertIsDisplayed()
        composeTestRule.onNodeWithText("Password").assertIsDisplayed()
        composeTestRule.onNodeWithText("Login").assertIsDisplayed()
    }

    @Test
    fun login_button_is_disabled_when_form_is_invalid() {
        // Set up the compose content
        composeTestRule.setContent {
            LoginScreen(
                viewModel = viewModel,
                onLoginSuccess = {}
            )
        }

        // Initially button should be disabled
        composeTestRule.onNodeWithText("Login").assertIsNotEnabled()

        // Enter invalid URL
        composeTestRule.onNodeWithText("Server URL")
            .performTextInput("invalid-url")

        composeTestRule.onNodeWithText("Username")
            .performTextInput("admin")

        composeTestRule.onNodeWithText("Password")
            .performTextInput("district")

        // Button should still be disabled with invalid URL
        composeTestRule.onNodeWithText("Login").assertIsNotEnabled()
    }

    @Test
    fun login_button_is_enabled_when_form_is_valid() {
        // Mock the form validation to return true
        viewModel.onServerUrlChanged("https://play.dhis2.org")
        viewModel.onUsernameChanged("admin")
        viewModel.onPasswordChanged("district")

        // Set up the compose content
        composeTestRule.setContent {
            LoginScreen(
                viewModel = viewModel,
                onLoginSuccess = {}
            )
        }

        // Button should be enabled with valid form
        composeTestRule.onNodeWithText("Login").assertIsEnabled()
    }

    @Test
    suspend fun successful_login_triggers_onLoginSuccess_callback() {
        // Setup for successful login
        `when`(loginUseCase.invoke(anyString(), anyString(), anyString())).thenReturn(LoginResult.SUCCESS)

        var loginSuccessCalled = false

        // Set up the compose content
        composeTestRule.setContent {
            LoginScreen(
                viewModel = viewModel,
                onLoginSuccess = { loginSuccessCalled = true }
            )
        }

        // Fill form with valid data
        viewModel.onServerUrlChanged("https://play.dhis2.org")
        viewModel.onUsernameChanged("admin")
        viewModel.onPasswordChanged("district")

        // Click login button
        composeTestRule.onNodeWithText("Login").performClick()

        // Wait for the login process to complete
        composeTestRule.waitForIdle()

        // Verify navigation event was triggered
        assert(viewModel.navigationEvent.equals(NavigationEvent.NavigateToDatasets))
    }
}