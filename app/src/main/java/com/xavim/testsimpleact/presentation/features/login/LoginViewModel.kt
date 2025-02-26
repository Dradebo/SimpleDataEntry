package com.xavim.testsimpleact.presentation.features.login

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xavim.testsimpleact.domain.repository.AuthRepository
import com.xavim.testsimpleact.domain.repository.Logger
import com.xavim.testsimpleact.domain.repository.SystemRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val systemRepository: SystemRepository,
    private val logger: Logger,
    @ApplicationContext private val context: Context
) : ViewModel() {
    private val _loginState = MutableStateFlow<LoginState>(LoginState.Initial)
    val loginState = _loginState.asStateFlow()

    init {
        viewModelScope.launch {
            try {
                systemRepository.initializeD2(context)
            } catch (e: Exception) {
                logger.e("LoginViewModel", "System initialization failed", e)
                _loginState.value = LoginState.Error("System initialization failed")
            }
        }
    }

    fun login(serverUrl: String, username: String, password: String) {
        viewModelScope.launch {
            _loginState.value = LoginState.Loading
            try {
                val success = authRepository.login(serverUrl, username, password)
                _loginState.value = if (success && authRepository.isLoggedIn()) {
                    LoginState.Success
                } else {
                    LoginState.Error("Login failed")
                }
            } catch (e: Exception) {
                logger.e("LoginViewModel", "Login error", e)
                _loginState.value = LoginState.Error(e.message ?: "Unknown error")
            }
        }
    }
}