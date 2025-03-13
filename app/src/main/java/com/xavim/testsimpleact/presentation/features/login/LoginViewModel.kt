package com.xavim.testsimpleact.presentation.features.login

import android.util.Log
import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xavim.testsimpleact.domain.repository.LoginResult
import com.xavim.testsimpleact.domain.repository.SessionInfo
import com.xavim.testsimpleact.domain.usecase.GetSessionInfoUseCase
import com.xavim.testsimpleact.domain.usecase.GetStoredCredentialsUseCase
import com.xavim.testsimpleact.domain.usecase.LoginUseCase
import com.xavim.testsimpleact.domain.usecase.LogoutUseCase
import com.xavim.testsimpleact.domain.usecase.ValidateSessionUseCase
import com.xavim.testsimpleact.domain.usecase.VerifyCredentialsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import javax.inject.Inject

sealed class NavigationEvent {
    object NavigateToDatasets : NavigationEvent()
    object NavigateToLogin : NavigationEvent()
    data class ShowMessage(val message: String) : NavigationEvent()
}

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val logoutUseCase: LogoutUseCase,
    private val getSessionInfoUseCase: GetSessionInfoUseCase,
    private val validateSessionUseCase: ValidateSessionUseCase,
    private val verifyCredentialsUseCase: VerifyCredentialsUseCase,
    private val getStoredCredentialsUseCase: GetStoredCredentialsUseCase
) : ViewModel() {

    private val _loginState = MutableStateFlow<LoginState>(LoginState.Initial)
    val loginState: StateFlow<LoginState> = _loginState.asStateFlow()

    private val _navigationEvent = MutableSharedFlow<NavigationEvent>(extraBufferCapacity = 1)
    val navigationEvent = _navigationEvent.asSharedFlow()

    private val _serverUrl = MutableStateFlow("")
    val serverUrl: StateFlow<String> = _serverUrl.asStateFlow()

    private val _username = MutableStateFlow("")
    val username: StateFlow<String> = _username.asStateFlow()

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password.asStateFlow()

    private val _isFormValid = MutableStateFlow(false)
    val isFormValid: StateFlow<Boolean> = _isFormValid.asStateFlow()

    private val _sessionInfo = MutableStateFlow<SessionInfo?>(null)

    init {
        viewModelScope.launch {
            getSessionInfoUseCase().collectLatest { info ->
                _sessionInfo.value = info

                if (info.isLoggedIn) {

                    Log.d("LoginViewModel", "Session is active, checking validity")
                    // If we have an active session, try to validate it
                    val isValid = validateSessionUseCase()
                    if (isValid) {
                        _navigationEvent.emit(NavigationEvent.NavigateToDatasets)
                    } else {
                        // Session existed but is no longer valid (e.g., expired)
                        // We'll show login but pre-fill credentials
                        loadStoredCredentials()
                        _navigationEvent.emit(NavigationEvent.NavigateToLogin)
                    }
                } else {
                    // No active session, load stored credentials if available
                    loadStoredCredentials()
                }
            }
        }
    }

    fun checkActiveSession() {
        viewModelScope.launch {
            val isSessionValid = validateSessionUseCase()

            Log.d("LoginViewModel", "Session valid: $isSessionValid")
            if (isSessionValid) {

                _loginState.value = LoginState.Success
                _navigationEvent.emit(NavigationEvent.NavigateToDatasets)
                Log.d("LoginViewModel", "Navigation event emitted: NavigateToDatasets")
            }
        }
    }

    private fun loadStoredCredentials() {
        viewModelScope.launch {
            try {
                val (storedServerUrl, storedUsername, _) = getStoredCredentialsUseCase()

                if (!storedServerUrl.isNullOrEmpty()) {
                    _serverUrl.value = storedServerUrl
                }

                if (!storedUsername.isNullOrEmpty()) {
                    _username.value = storedUsername
                }

                validateForm()
            } catch (e: Exception) {
                Log.e("LoginViewModel", "Error loading stored credentials", e)
            }
        }
    }

    fun onServerUrlChanged(url: String) {
        _serverUrl.value = url
        validateForm()
    }

    fun onUsernameChanged(username: String) {
        _username.value = username
        validateForm()
    }

    fun onPasswordChanged(password: String) {
        _password.value = password
        validateForm()
    }

    private fun validateForm() {
        val isUrlValid = isValidUrl(_serverUrl.value)
        val isUsernameValid = _username.value.isNotBlank()
        val isPasswordValid = _password.value.isNotBlank()

        _isFormValid.value = isUrlValid && isUsernameValid && isPasswordValid
    }

    private fun isValidUrl(url: String): Boolean {
        return url.isNotBlank() &&
                Patterns.WEB_URL.matcher(url).matches() &&
                url.toHttpUrlOrNull() != null
    }

    fun login() {



        // Check form validity; if not valid, emit a message and return.
        if (!_isFormValid.value) {
            viewModelScope.launch {
                _navigationEvent.emit(
                    NavigationEvent.ShowMessage("Please fill all fields with valid values")
                )
            }
            return
        }

        // Launch a coroutine for the login process.
        viewModelScope.launch {
            _loginState.value = LoginState.Loading

            Log.d("LoginViewModel", "Attempting login")
            if ((_sessionInfo.value?.isLoggedIn == true) && verifyCredentialsUseCase(_serverUrl.value, _username.value, _password.value)){

                viewModelScope.launch {
                _loginState.value = LoginState.Success
                _navigationEvent.emit(NavigationEvent.NavigateToDatasets)

                }
                return@launch
            }

            // Call your suspend login use case (e.g., loginUseCase might be a suspend function)
            val result = loginUseCase(_serverUrl.value, _username.value, _password.value)

            when (result) {
                LoginResult.SUCCESS -> {
                    _loginState.value = LoginState.Success
                    _navigationEvent.emit(NavigationEvent.NavigateToDatasets)
                    Log.d("LoginViewModel", "Navigation event emitted: NavigateToDatasets")
                }
                LoginResult.INVALID_CREDENTIALS -> {
                    _loginState.value = LoginState.Error("Invalid username or password")
                    _navigationEvent.emit(NavigationEvent.ShowMessage("Invalid username or password"))
                }
                LoginResult.NETWORK_ERROR -> {
                    _loginState.value = LoginState.Error("Network error. Please check your connection")
                    _navigationEvent.emit(NavigationEvent.ShowMessage("Network error. Please check your connection"))
                }
                LoginResult.SERVER_ERROR -> {
                    _loginState.value = LoginState.Error("Server error. Please try again later")
                    _navigationEvent.emit(NavigationEvent.ShowMessage("Server error. Please try again later"))
                }
                LoginResult.ACCOUNT_LOCKED -> {
                    _loginState.value = LoginState.Error("Account locked due to too many failed attempts")
                    _navigationEvent.emit(NavigationEvent.ShowMessage("Account locked due to too many failed attempts. Please try again later"))
                }
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            try {
                logoutUseCase()
                // Clear password but retain server and username for convenience
                _password.value = ""
                _loginState.value = LoginState.Initial
            } catch (e: Exception) {
                Log.e("LoginViewModel", "Logout error", e)
                _navigationEvent.emit(NavigationEvent.ShowMessage("Error logging out: ${e.message}"))
            }
        }
    }

}