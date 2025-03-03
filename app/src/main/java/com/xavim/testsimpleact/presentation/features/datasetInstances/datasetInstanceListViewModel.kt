package com.xavim.testsimpleact.presentation.features.datasetInstances

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xavim.testsimpleact.domain.model.DatasetInstance
import com.xavim.testsimpleact.domain.model.DatasetInstanceState
import com.xavim.testsimpleact.domain.model.SyncState
import com.xavim.testsimpleact.domain.useCase.GetDatasetInstancesUseCase
import com.xavim.testsimpleact.domain.useCase.GetDatasetMetadataUseCase
import com.xavim.testsimpleact.domain.useCase.SyncDatasetInstanceUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject



enum class SortOrder {
    DATE_ASC,
    DATE_DESC,
    ORG_UNIT_ASC,
    ORG_UNIT_DESC
}

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

    private val _state = MutableStateFlow<DatasetInstanceListState>(DatasetInstanceListState.Loading)
    val state: StateFlow<DatasetInstanceListState> = _state.asStateFlow()

    private val _sortOrder = MutableStateFlow(SortOrder.DATE_DESC)
    val sortOrder: StateFlow<SortOrder> = _sortOrder.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    // Filters
    private val _orgUnitFilter = MutableStateFlow<String?>(null)
    private val _periodFilter = MutableStateFlow<String?>(null)
    private val _stateFilter = MutableStateFlow<DatasetInstanceState?>(null)
    private val _syncStateFilter = MutableStateFlow<SyncState?>(null)

    // Dataset metadata
    private val _datasetName = MutableStateFlow<String?>(null)
    val datasetName: StateFlow<String?> = _datasetName.asStateFlow()

    // Selected instance for actions
    private val _selectedInstance = MutableStateFlow<DatasetInstance?>(null)
    val selectedInstance: StateFlow<DatasetInstance?> = _selectedInstance.asStateFlow()

    init {
        loadData()
        loadDatasetMetadata()
    }

    private fun loadData() {
        viewModelScope.launch {
            try {
                _state.value = DatasetInstanceListState.Loading

                getDatasetInstancesUseCase(
                    _datasetId,
                    _orgUnitFilter.value,
                    _periodFilter.value,
                    _stateFilter.value,
                    _syncStateFilter.value
                ).combine(_sortOrder) { instances, order ->
                    sortInstances(instances, order)
                }.catch { e ->
                    Log.e( "Error loading dataset instances", e.toString())
                    _state.value = DatasetInstanceListState.Error(e.message ?: "Unknown error")
                }.collect { instances ->
                    _state.value = DatasetInstanceListState.Success(instances)
                    _isRefreshing.value = false
                }
            } catch (e: Exception) {
                Log.e("Error in loadData", e.toString())
                _state.value = DatasetInstanceListState.Error(e.message ?: "Unknown error")
                _isRefreshing.value = false
            }
        }
    }

    private fun loadDatasetMetadata() {
        viewModelScope.launch {
            try {
                getDatasetMetadataUseCase(_datasetId)
                    .catch { e ->
                        Log.e("Error loading dataset metadata", e.toString())
                    }
                    .collect { metadata ->
                        _datasetName.value = metadata.name
                    }
            } catch (e: Exception) {
                Log.e( "Error in loadDatasetMetadata", e.toString())
            }
        }
    }

    private fun sortInstances(instances: List<DatasetInstance>, order: SortOrder): List<DatasetInstance> {
        return when (order) {
            SortOrder.DATE_ASC -> instances.sortedBy { it.lastUpdated }
            SortOrder.DATE_DESC -> instances.sortedByDescending { it.lastUpdated }
            SortOrder.ORG_UNIT_ASC -> instances.sortedBy { it.organisationUnitDisplayName }
            SortOrder.ORG_UNIT_DESC -> instances.sortedByDescending { it.organisationUnitDisplayName }
        }
    }

    fun refresh() {
        _isRefreshing.value = true
        loadData()
    }

    fun setSortOrder(order: SortOrder) {
        _sortOrder.value = order
    }

    fun setOrgUnitFilter(orgUnitId: String?) {
        _orgUnitFilter.value = orgUnitId
        loadData()
    }

    fun setPeriodFilter(periodId: String?) {
        _periodFilter.value = periodId
        loadData()
    }

    fun setStateFilter(state: DatasetInstanceState?) {
        _stateFilter.value = state
        loadData()
    }

    fun setSyncStateFilter(syncState: SyncState?) {
        _syncStateFilter.value = syncState
        loadData()
    }

    fun clearFilters() {
        _orgUnitFilter.value = null
        _periodFilter.value = null
        _stateFilter.value = null
        _syncStateFilter.value = null
        loadData()
    }

    fun selectInstance(instance: DatasetInstance) {
        _selectedInstance.value = instance
    }

    fun clearSelectedInstance() {
        _selectedInstance.value = null
    }

    fun syncInstance(instance: DatasetInstance) {
        viewModelScope.launch {
            try {
                syncDatasetInstanceUseCase(
                    instance.datasetId,
                    instance.periodId,
                    instance.organisationUnitUid,
                    instance.attributeOptionComboUid
                ).collect { result ->
                    result.onSuccess {
                        refresh()
                    }.onFailure { error ->
                        Log.e("Error syncing dataset instance", error.toString())
                    }
                }
            } catch (e: Exception) {
                Log.e( "Error in syncInstance", e.toString())
            }
        }
    }
}