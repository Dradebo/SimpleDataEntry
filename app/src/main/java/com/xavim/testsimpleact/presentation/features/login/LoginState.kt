package com.xavim.testsimpleact.presentation.features.login

sealed class LoginState {
    object Initial : LoginState()
    object Loading : LoginState()
    data class Error(val message: String) : LoginState()
    object Success : LoginState()
}