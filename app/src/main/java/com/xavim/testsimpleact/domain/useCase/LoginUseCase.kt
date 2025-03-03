package com.xavim.testsimpleact.domain.usecase

import com.xavim.testsimpleact.domain.repository.AuthRepository
import com.xavim.testsimpleact.domain.repository.LoginResult
import com.xavim.testsimpleact.domain.repository.SessionInfo
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class LoginUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(serverUrl: String, username: String, password: String): LoginResult =
        authRepository.login(serverUrl, username, password)
}

class LogoutUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke() = authRepository.logout()
}

class GetSessionInfoUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    operator fun invoke(): Flow<SessionInfo> = authRepository.getSessionInfo()
}

class ValidateSessionUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(): Boolean = authRepository.validateSession()
}

class VerifyCredentialsUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(serverUrl: String, username: String, password: String): Boolean {
        return when (authRepository.login(serverUrl, username, password)) {
            LoginResult.SUCCESS -> true
            else -> false
        }
    }

    suspend fun verifyStoredCredentials(): Boolean = authRepository.verifyStoredCredentials()
}

class GetStoredCredentialsUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(): Triple<String?, String?, String?> =
        authRepository.getStoredCredentials()
}