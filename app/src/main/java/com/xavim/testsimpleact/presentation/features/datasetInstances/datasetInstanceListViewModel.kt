package com.xavim.testsimpleact.presentation.features.datasetInstances

import android.util.Log
import androidx.compose.foundation.layout.size
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xavim.testsimpleact.domain.model.DatasetInstance
import com.xavim.testsimpleact.domain.useCase.GetDatasetInstancesUseCase
import com.xavim.testsimpleact.domain.useCase.GetDatasetMetadataUseCase
import com.xavim.testsimpleact.domain.useCase.SyncDatasetInstanceUseCase
import com.xavim.testsimpleact.presentation.features.datasetInstances.DatasetInstanceListState.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch

@HiltViewModel
class DatasetInstanceListViewModel @Inject constructor(
    private val getDatasetInstancesUseCase: GetDatasetInstancesUseCase,
    private val getDatasetMetadataUseCase: GetDatasetMetadataUseCase,
    private val syncDatasetInstanceUseCase: SyncDatasetInstanceUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _datasetId: String = checkNotNull(savedStateHandle["datasetId"]) {
        "datasetId is required"
    }
    val datasetId: String get() = _datasetId

    private val _state = MutableStateFlow<DatasetInstanceListState>(Loading)
    val state: StateFlow<DatasetInstanceListState> = _state.asStateFlow()

    private val _sortOrder = MutableStateFlow(SortOrder.DATE_DESC)
    val sortOrder: StateFlow<SortOrder> = _sortOrder.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            try {
                _state.value = Loading

                combine(
                    getDatasetMetadataUseCase(_datasetId),
                    getDatasetInstancesUseCase(_datasetId).combine(_sortOrder) { instances, order ->
                        sortInstances(instances, order)
                    }
                ) { metadata, instances ->
                    Success(
                        entries = instances,
                        canCreateNew = metadata.canCreateNew
                    )
                }.catch { e ->
                    Log.e(TAG, "Error loading data", e)

                }.collect {
                    _state.value = it
                    _isRefreshing.value = false
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error in loadData", e)
                _state.value = Error(e.message ?: "Unknown error occurred")
                _isRefreshing.value = false
            }
        }
    }

    fun updateSortOrder(newSortOrder: SortOrder) {
        if (_sortOrder.value != newSortOrder) {
            _sortOrder.value = newSortOrder
        }
    }

    fun refresh() {
        _isRefreshing.value = true
        loadData()
    }

    fun syncInstance(instanceId: String) {
        viewModelScope.launch {
            try {
                syncDatasetInstanceUseCase(instanceId)
                refresh()
            } catch (e: Exception) {
                Log.e(TAG, "Error syncing instance", e)
                // Show error notification if needed
            }
        }
    }

    private fun sortInstances(
        instances: List<DatasetInstance>,
        sortOrder: SortOrder
    ): List<DatasetInstance> = when (sortOrder) {
        SortOrder.DATE_DESC -> instances.sortedByDescending { it.lastUpdated }
        SortOrder.DATE_ASC -> instances.sortedBy { it.lastUpdated }
        SortOrder.COMPLETION_STATUS -> instances.sortedWith(
            compareBy<DatasetInstance> { !it.state }
                .thenByDescending { it.lastUpdated }
        )
    }

    companion object {
        private const val TAG = "DatasetInstanceListViewModel"
    }

    enum class SortOrder {
        DATE_DESC, DATE_ASC, COMPLETION_STATUS
    }
}