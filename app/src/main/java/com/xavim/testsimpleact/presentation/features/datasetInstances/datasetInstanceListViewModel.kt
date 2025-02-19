package com.xavim.testsimpleact.presentation.features.datasetInstances

import android.util.Log
import androidx.compose.foundation.layout.size
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xavim.testsimpleact.domain.model.DatasetInstance
import com.xavim.testsimpleact.domain.useCase.GetDatasetInstancesUseCase
import com.xavim.testsimpleact.domain.useCase.GetDatasetMetadataUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch

//@HiltViewModel
//class DatasetInstanceListViewModel @Inject constructor(
//    private val getDatasetInstancesUseCase: GetDatasetInstancesUseCase,
//    savedStateHandle: SavedStateHandle
//) : ViewModel() {
//
//    val datasetId: String = checkNotNull(savedStateHandle["datasetId"]) {
//        "datasetId is required"
//    }
//
//    private val _state = MutableStateFlow<DatasetInstanceListState>(DatasetInstanceListState.Loading)
//    val state: StateFlow<DatasetInstanceListState> = _state
//
//    private val _sortOrder = MutableStateFlow(SortOrder.DATE_DESC)
//    val sortOrder: StateFlow<SortOrder> = _sortOrder
//
//    private val TAG = "DatasetInstanceListViewModel"
//
//    init {
//        loadEntries()
//    }
//
//    /**
//     * Loads entries from the use case, applies sorting, and updates the UI state.
//     */
//    private fun loadEntries() {
//        viewModelScope.launch {
//            try {
//                Log.d(TAG, "loadEntries called for datasetId: $datasetId")
//                getDatasetInstancesUseCase(datasetId)
//                    .combine(_sortOrder) { entries, sortOrder ->
//                        sortInstances(entries, sortOrder)
//                    }
//                    .catch { e ->
//                        Log.e(TAG, "Error fetching dataset instances", e)
//                        _state.value = DatasetInstanceListState.Error(
//                            e.message ?: "Failed to load dataset instances"
//                        )
//                    }
//                    .collect { sortedEntries ->
//                        Log.d(TAG, "Dataset instances collected successfully. Count: ${sortedEntries.size}")
//                        _state.value = DatasetInstanceListState.Success(
//                            entries = sortedEntries,
//                            canCreateNew = checkCanCreateNew()
//                        )
//                    }
//            } catch (e: Exception) {
//                Log.e(TAG, "Error in loadEntries", e)
//                _state.value = DatasetInstanceListState.Error(e.message ?: "Unknown error")
//            }
//        }
//    }
//
//    private fun sortInstances(
//        entries: List<DatasetInstance>,
//        sortOrder: SortOrder
//    ): List<DatasetInstance> {
//        return when (sortOrder) {
//            SortOrder.DATE_DESC -> entries.sortedByDescending { it.lastUpdated }
//            SortOrder.DATE_ASC -> entries.sortedBy { it.lastUpdated }
//            SortOrder.COMPLETION_STATUS -> {
//                entries.sortedBy { it.state }
//            }
//        }
//    }
//
//    fun updateSortOrder(newSortOrder: SortOrder) {
//        _sortOrder.value = newSortOrder
//    }
//
//    /**
//     * Example logic if creation of new entries depends on user permissions, or other checks.
//     */
//    private fun checkCanCreateNew(): Boolean {
//        // For demonstration, return true
//        return true
//    }
//
//    enum class SortOrder {
//        DATE_DESC, DATE_ASC, COMPLETION_STATUS
//    }
//}

@HiltViewModel
class DatasetInstanceListViewModel @Inject constructor(
    private val getDatasetInstancesUseCase: GetDatasetInstancesUseCase,
    private val getDatasetMetadataUseCase: GetDatasetMetadataUseCase, // New use case
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val datasetId: String = checkNotNull(savedStateHandle["datasetId"]) {
        "datasetId is required"
    }

    private val _state = MutableStateFlow<DatasetInstanceListState>(DatasetInstanceListState.Loading)
    val state: StateFlow<DatasetInstanceListState> = _state.asStateFlow()

    private val _sortOrder = MutableStateFlow(SortOrder.DATE_DESC)
    val sortOrder: StateFlow<SortOrder> = _sortOrder.asStateFlow()

    private val _canCreateNew = MutableStateFlow(false)
    val canCreateNew: StateFlow<Boolean> = _canCreateNew.asStateFlow()

    init {
        loadDatasetMetadata()
        loadEntries()
    }

    private fun loadDatasetMetadata() {
        viewModelScope.launch {
            try {
                getDatasetMetadataUseCase(datasetId)
                    .collect { metadata ->
                        _canCreateNew.value = metadata.canCreateNew
                    }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading dataset metadata", e)
            }
        }
    }

    private fun loadEntries() {
        viewModelScope.launch {
            try {
                _state.value = DatasetInstanceListState.Loading

                getDatasetInstancesUseCase(datasetId)
                    .combine(_sortOrder) { entries, sortOrder ->
                        sortInstances(entries, sortOrder)
                    }
                    .catch { e ->
                        Log.e(TAG, "Error fetching dataset instances", e)
                        _state.value = DatasetInstanceListState.Error(
                            e.message ?: "Failed to load dataset instances"
                        )
                    }
                    .collect { sortedEntries ->
                        _state.value = DatasetInstanceListState.Success(
                            entries = sortedEntries,
                            canCreateNew = _canCreateNew.value
                        )
                    }
            } catch (e: Exception) {
                Log.e(TAG, "Error in loadEntries", e)
                _state.value = DatasetInstanceListState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun updateSortOrder(newSortOrder: SortOrder) {
        _sortOrder.value = newSortOrder
        loadEntries() // Reload with new sort order
    }

    fun refresh() {
        loadEntries()
    }

    private fun sortInstances(
        entries: List<DatasetInstance>,
        sortOrder: SortOrder
    ): List<DatasetInstance> = when (sortOrder) {
        SortOrder.DATE_DESC -> entries.sortedByDescending { it.lastUpdated }
        SortOrder.DATE_ASC -> entries.sortedBy { it.lastUpdated }
        SortOrder.COMPLETION_STATUS -> entries.sortedWith(
            compareBy<DatasetInstance> { it.state }
                .thenByDescending { it.lastUpdated }
        )
    }

    companion object {
        private const val TAG = "DatasetInstanceListVM"
    }

        enum class SortOrder {
        DATE_DESC, DATE_ASC, COMPLETION_STATUS
    }
}