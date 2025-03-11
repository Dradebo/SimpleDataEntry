//package com.xavim.testsimpleact.presentation.features.datasetInstances
//
//import android.util.Log
//import androidx.lifecycle.SavedStateHandle
//import androidx.lifecycle.ViewModel
//import androidx.lifecycle.viewModelScope
//import com.xavim.testsimpleact.domain.model.DatasetInstance
//import com.xavim.testsimpleact.domain.model.DatasetInstanceState
//import com.xavim.testsimpleact.domain.useCase.CheckCompletePermissionUseCase
//import com.xavim.testsimpleact.domain.useCase.CheckDatasetInstanceEditableUseCase
//import com.xavim.testsimpleact.domain.useCase.CompleteDatasetInstanceUseCase
//import com.xavim.testsimpleact.domain.useCase.ReopenDatasetInstanceUseCase
//import com.xavim.testsimpleact.domain.useCase.SyncDatasetInstanceUseCase
//import dagger.hilt.android.lifecycle.HiltViewModel
//import kotlinx.coroutines.flow.MutableStateFlow
//import kotlinx.coroutines.flow.StateFlow
//import kotlinx.coroutines.flow.asStateFlow
//import kotlinx.coroutines.launch
//import javax.inject.Inject
//
//
//@HiltViewModel
//class DatasetInstanceDetailViewModel @Inject constructor(
//    private val checkDatasetInstanceEditableUseCase: CheckDatasetInstanceEditableUseCase,
//    private val checkCompletePermissionUseCase: CheckCompletePermissionUseCase,
//    private val completeDatasetInstanceUseCase: CompleteDatasetInstanceUseCase,
//    private val reopenDatasetInstanceUseCase: ReopenDatasetInstanceUseCase,
//    private val syncDatasetInstanceUseCase: SyncDatasetInstanceUseCase,
//    savedStateHandle: SavedStateHandle
//) : ViewModel() {
//
//    private val datasetId: String = checkNotNull(savedStateHandle["datasetId"])
//    private val periodId: String = checkNotNull(savedStateHandle["periodId"])
//    private val orgUnitId: String = checkNotNull(savedStateHandle["orgUnitId"])
//    private val attributeOptionComboId: String = checkNotNull(savedStateHandle["attributeOptionComboId"])
//
//    private val _state = MutableStateFlow<DatasetInstanceDetailState>(DatasetInstanceDetailState.Loading)
//    val state: StateFlow<DatasetInstanceDetailState> = _state.asStateFlow()
//
//    private val _isProcessing = MutableStateFlow(false)
//    val isProcessing: StateFlow<Boolean> = _isProcessing.asStateFlow()
//
//    private val _message = MutableStateFlow<String?>(null)
//    val message: StateFlow<String?> = _message.asStateFlow()
//
//    private var currentInstance: DatasetInstance? = null
//
//    init {
//        loadInstanceDetails()
//    }
//
//    fun loadInstanceDetails() {
//        viewModelScope.launch {
//            try {
//                _state.value = DatasetInstanceDetailState.Loading
//                _isProcessing.value = true
//
//                // In a real implementation, we would fetch the instance details
//                // For now, we'll create a placeholder instance
//                val instance = DatasetInstance(
//                    instanceUid = "${datasetId}_${periodId}_${orgUnitId}_${attributeOptionComboId}",
//                    datasetId = datasetId,
//                    periodId = periodId,
//                    organisationUnitUid = orgUnitId,
//                    attributeOptionComboUid = attributeOptionComboId,
//                    state = DatasetInstanceState.OPEN,
//                    syncState = com.xavim.testsimpleact.domain.model.SyncState.SYNCED,
//                    lastUpdated = java.util.Date(),
//                    createdAt = java.util.Date()
//                )
//
//                currentInstance = instance
//
//                val isEditable = checkDatasetInstanceEditableUseCase(
//                    datasetId, periodId, orgUnitId, attributeOptionComboId
//                )
//
//                val hasCompletePermission = checkCompletePermissionUseCase(datasetId)
//
//                _state.value = DatasetInstanceDetailState.Success(
//                    instance = instance,
//                    isEditable = isEditable,
//                    hasCompletePermission = hasCompletePermission
//                )
//            } catch (e: Exception) {
//                Log.e("Error loading dataset instance details",  e.toString())
//                _state.value = DatasetInstanceDetailState.Error(e.message ?: "Unknown error")
//            } finally {
//                _isProcessing.value = false
//            }
//        }
//    }
//
//    fun completeDatasetInstance() {
//        viewModelScope.launch {
//            try {
//                _isProcessing.value = true
//                completeDatasetInstanceUseCase(
//                    datasetId, periodId, orgUnitId, attributeOptionComboId
//                ).collect { result: Result<Unit> ->
//                    result.onSuccess {
//                        _message.value = "Dataset completed successfully"
//                        loadInstanceDetails()
//                    }.onFailure { error: Throwable -> // Explicitly specify the type as Throwable
//                        Log.e("Error completing dataset instance", error.toString())
//                        _message.value = "Error completing dataset: ${error.message}"
//                    }
//                }
//            } catch (e: Exception) {
//                Log.e("Error completing dataset instance",  e.toString())
//                _message.value = "Error completing dataset: ${e.message}"
//            } finally {
//                _isProcessing.value = false
//            }
//        }
//    }
//
//    fun reopenDatasetInstance() {
//        viewModelScope.launch {
//            try {
//                _isProcessing.value = true
//                reopenDatasetInstanceUseCase(
//                    datasetId, periodId, orgUnitId, attributeOptionComboId
//                ).collect { result: Result<Unit> -> // Explicitly specify the type as Result<Unit>
//                    result.onSuccess {
//                        _message.value = "Dataset reopened successfully"
//                        loadInstanceDetails()
//                    }.onFailure { error: Throwable -> // Explicitly specify the type as Throwable
//                        Log.e("Error reopening dataset instance", error.toString())
//                        _message.value = "Error reopening dataset: ${error.message}"
//                    }
//                }
//            } catch (e: Exception) {
//                Log.e("Error reopening dataset instance", e.toString())
//                _message.value = "Error reopening dataset: ${e.message}"
//            } finally {
//                _isProcessing.value = false
//            }
//        }
//    }
//
//    fun syncDatasetInstance() {
//        viewModelScope.launch {
//            try {
//                _isProcessing.value = true
//                syncDatasetInstanceUseCase(
//                    datasetId, periodId, orgUnitId, attributeOptionComboId
//                ).collect { result: Result<Unit> -> // Explicitly specify the type as Result<Unit>
//                    result.onSuccess {
//                        _message.value = "Dataset synced successfully"
//                        loadInstanceDetails()
//                    }.onFailure { error: Throwable -> // Explicitly specify the type as Throwable
//                        Log.e("Error syncing dataset instance", error.toString())
//                        _message.value = "Error syncing dataset: ${error.message}"
//                    }
//                }
//            } catch (e: Exception) {
//                Log.e("Error syncing dataset instance", e.toString())
//                _message.value = "Error syncing dataset: ${e.message}"
//            } finally {
//                _isProcessing.value = false
//            }
//        }
//    }
//
//    fun clearMessage() {
//        _message.value = null
//    }
//}