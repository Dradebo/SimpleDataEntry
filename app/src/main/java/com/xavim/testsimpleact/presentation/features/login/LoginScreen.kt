package com.xavim.testsimpleact.presentation.features.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    viewModel: LoginViewModel = hiltViewModel(),
    onLoginSuccess: () -> Unit
) {
    val loginState by viewModel.loginState.collectAsStateWithLifecycle()
    val navigationEvent by viewModel.navigationEvent.collectAsStateWithLifecycle()
    val serverUrl by viewModel.serverUrl.collectAsStateWithLifecycle()
    val username by viewModel.username.collectAsStateWithLifecycle()
    val password by viewModel.password.collectAsStateWithLifecycle()
    val isFormValid by viewModel.isFormValid.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    var passwordVisible by remember { mutableStateOf(false) }

    // Check if there's an active session when the screen is displayed
    LaunchedEffect(key1 = true) {
        viewModel.checkActiveSession()
    }

    // Handle navigation events
    LaunchedEffect(navigationEvent) {
        when (val event = navigationEvent) {
            is NavigationEvent.NavigateToDatasets -> {
                onLoginSuccess()
                viewModel.clearNavigationEvent()
            }
            is NavigationEvent.ShowMessage -> {
                snackbarHostState.showSnackbar(event.message)
                viewModel.clearNavigationEvent()
            }
            else -> { /* No action needed */ }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("DHIS2 Login") }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center
        ) {
            when (loginState) {
                LoginState.Loading -> {
                    CircularProgressIndicator()
                }
                else -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        // Server URL field
                        OutlinedTextField(
                            value = serverUrl,
                            onValueChange = { viewModel.onServerUrlChanged(it) },
                            label = { Text("Server URL") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                            isError = serverUrl.isNotEmpty() && !isValidUrl(serverUrl),
                            supportingText = {
                                if (serverUrl.isNotEmpty() && !isValidUrl(serverUrl)) {
                                    Text("Invalid URL format")
                                }
                            },
                            trailingIcon = {
                                if (serverUrl.isNotEmpty() && !isValidUrl(serverUrl)) {
                                    Icon(Icons.Filled.Error, "error", tint = MaterialTheme.colorScheme.error)
                                }
                            }
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Username field
                        OutlinedTextField(
                            value = username,
                            onValueChange = { viewModel.onUsernameChanged(it) },
                            label = { Text("Username") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Password field with visibility toggle
                        OutlinedTextField(
                            value = password,
                            onValueChange = { viewModel.onPasswordChanged(it) },
                            label = { Text("Password") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            trailingIcon = {
                                val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(imageVector = image, contentDescription = "Toggle password visibility")
                                }
                            }
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        // Login button
                        Button(
                            onClick = { viewModel.login() },
                            enabled = isFormValid,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                        ) {
                            Text("Login")
                        }

                        // Error message
                        if (loginState is LoginState.Error) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = (loginState as LoginState.Error).message,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
        }
    }
}

// Helper function to validate URL
private fun isValidUrl(url: String): Boolean {
    return android.util.Patterns.WEB_URL.matcher(url).matches()
}