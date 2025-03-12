package com.xavim.testsimpleact.presentation.features.datasets

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xavim.testsimpleact.data.session.SessionManager
import com.xavim.testsimpleact.domain.repository.AuthRepository
import com.xavim.testsimpleact.domain.useCase.GetDataSetsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DatasetGridViewModel @Inject constructor(
    private val sessionManager: SessionManager,
    private val authRepository: AuthRepository,
    private val getDataSetsUseCase: GetDataSetsUseCase,
    @ApplicationContext val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow<DatasetScreenState>(DatasetScreenState.Loading)
    val uiState: StateFlow<DatasetScreenState> = _uiState

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            try {
                // Check if we need to initialize

                fetchDataSets()
            } catch (e: Exception) {
                Log.e("DatasetViewModel", "Failed to initialize/login", e)
                _uiState.value = DatasetScreenState.Error(
                    "Failed to initialize: ${e.message}"
                )
            }
        }
    }

    private suspend fun fetchDataSets() {
        try {
            getDataSetsUseCase()
                .catch { e ->
                    Log.e("DatasetViewModel", "Error fetching datasets", e)
                    _uiState.value = DatasetScreenState.Error(
                        e.message ?: "Failed to load datasets"
                    )
                }
                .collect { datasets ->
                    _uiState.value = DatasetScreenState.Success(datasets)
                }
        } catch (e: Exception) {
            Log.e("DatasetViewModel", "Error in fetchDataSets", e)
            _uiState.value = DatasetScreenState.Error(e.message ?: "Unknown error")
        }
    }

    fun refreshDatasets() {
        viewModelScope.launch {
            _uiState.value = DatasetScreenState.Loading
            fetchDataSets()
        }
    }

    fun logout() {
        viewModelScope.launch {
            try {
                authRepository.logout()
                // Optionally update UI state or trigger navigation
            } catch (e: Exception) {
                Log.e("DatasetViewModel", "Logout failed", e)
            }
        }
    }
}