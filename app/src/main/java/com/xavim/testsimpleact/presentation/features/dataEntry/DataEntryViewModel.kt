package com.xavim.testsimpleact.presentation.features.dataEntry

import android.content.ContentValues.TAG
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xavim.testsimpleact.domain.model.DataEntryElement
import com.xavim.testsimpleact.domain.model.DataEntrySection
import com.xavim.testsimpleact.domain.repository.Logger
import com.xavim.testsimpleact.domain.useCase.CreateNewEntryUseCase
import com.xavim.testsimpleact.domain.useCase.GetDataEntryFormUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

//@HiltViewModel
//class DataEntryViewModel @Inject constructor(
//    private val createNewEntryUseCase: CreateNewEntryUseCase,
//    private val getDataEntryFormUseCase: GetDataEntryFormUseCase,
//    private val logger : Logger,
//    savedStateHandle: SavedStateHandle
//
//    ) : ViewModel() {
//
//        private val dataSetId: String = checkNotNull(savedStateHandle["datasetId"]) {
//        "datasetId is required"
//    }
//
//    private val periodId: String = checkNotNull(savedStateHandle["periodId"]) {
//        "periodId is required"
//    }
//
//    private val orgUnitId: String = checkNotNull(savedStateHandle["orgUnitId"]) {
//        "orgUnitId is required"
//    }
//
//    private val attributeOptionComboId: String = checkNotNull(savedStateHandle["attributeOptionComboId"]) {
//        "attributeOptionComboId is required"
//    }
//
//    private val _uiState = MutableStateFlow<DataEntryScreenState>(DataEntryScreenState.Loading)
//    val uiState: StateFlow<DataEntryScreenState> = _uiState
//
//    private val _expandedSections = MutableStateFlow<Set<String>>(emptySet())
//    val expandedSections: StateFlow<Set<String>> = _expandedSections
//
//
//    init {
//        initializeDataEntry(dataSetId,periodId,orgUnitId, attributeOptionComboId)
//    }
//
//    fun initializeDataEntry(
//        datasetId: String,
//        periodId: String,
//        orgUnitId: String,
//        attributeOptionComboId: String?
//    ) {
//        viewModelScope.launch {
//            try {
//
//                Log.d(TAG, "intialiseDataEntryForm called for datasetId: $datasetId")
//                val dataValues = getDataEntryFormUseCase(
//                    datasetId
//
//                ).collect { values ->
//                    // Transform values into DataEntryElement objects
//                    _uiState.value = DataEntryScreenState.Success(
//                        sections = getSectionsFromValues(values)
//                    )
//                }
//            } catch (e: Exception) {
//                _uiState.value = DataEntryScreenState.Error(e.message ?: "Unknown error")
//            }
//        }
//    }
//
//    fun onSectionHeaderClick(sectionUid: String) {
//        viewModelScope.launch {
//            val currentExpanded = _expandedSections.value
//            if (currentExpanded.contains(sectionUid)) {
//                _expandedSections.value = currentExpanded.minus(sectionUid)
//            } else {
//                _expandedSections.value = currentExpanded.plus(sectionUid)
//            }
//        }
//    }
//
//    fun onInputChange(dataElementUid: String, value: String) {
//        viewModelScope.launch {
//            val currentState = _uiState.value
//            if (currentState is DataEntryScreenState.Success) {
//                val updatedValues = currentState.sections
//                    .map { section ->
//                        section.dataElements.map { element ->
//                            if (element.dataElementId == dataElementUid) {
//                                element.copy(value = value)
//                            } else element
//                        }
//                    }
//                _uiState.value = currentState.copy(
//                    // sections = updatedValues
//                )
//            }
//        }
//    }
//
//    private fun getSectionsFromValues(values: List<DataEntrySection>): List<DataEntrySection> {
//
//        return values.map { entry ->
//            DataEntryElement(
//                dataElementId = entry.uid,
//                name = entry.name,
//                type = "text",
//                value = "",
//                optionSetUid = "Option",
//                style = "style"
//            )
//        }.let { elements ->
//            DataEntrySection(
//                uid = "",
//                name = "",
//                dataElements = elements
//            )
//        }.let { listOf(it) }
//    }
//}


@HiltViewModel
class DataEntryViewModel @Inject constructor(
    private val repository: DataEntryRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val datasetId: String = checkNotNull(savedStateHandle["datasetId"])
    private val periodId: String? = savedStateHandle["periodId"]
    private val orgUnitId: String? = savedStateHandle["orgUnitId"]
    private val attributeOptionComboId: String? = savedStateHandle["attributeOptionComboId"]

    private val _uiState = MutableStateFlow<DataEntryState>(DataEntryState.Loading)
    val uiState: StateFlow<DataEntryState> = _uiState.asStateFlow()

    private val _expandedSections = MutableStateFlow<Set<String>>(emptySet())
    val expandedSections: StateFlow<Set<String>> = _expandedSections.asStateFlow()

    private val _dataValues = MutableStateFlow<Map<String, String>>(emptyMap())

    init {
        loadDataEntry()
    }

    private fun loadDataEntry() {
        viewModelScope.launch {
            try {
                repository.getDataEntryForm(datasetId)
                    .combine(
                        if (isExistingEntry()) {
                            repository.getExistingDataValues(
                                datasetId,
                                periodId!!,
                                orgUnitId!!,
                                attributeOptionComboId!!
                            )
                        } else {
                            flow { emit(emptyList()) }
                        }
                    ) { formSections, existingValues ->
                        mergeSectionsWithValues(formSections, existingValues)
                    }
                    .collect { sections ->
                        _uiState.value = DataEntryState.Success(sections)
                    }
            } catch (e: Exception) {
                _uiState.value = DataEntryState.Error(e.message ?: "Unknown error occurred")
            }
        }
    }

    private fun isExistingEntry(): Boolean =
        periodId != null && orgUnitId != null && attributeOptionComboId != null

    private fun mergeSectionsWithValues(
        sections: List<DataEntrySection>,
        existingValues: List<DataValue>
    ): List<DataEntrySection> {
        return sections.map { section ->
            section.copy(
                dataElements = section.dataElements.map { element ->
                    element.copy(
                        value = existingValues.find {
                            it.dataElementId == element.dataElementId
                        }?.value ?: ""
                    )
                }
            )
        }
    }

    fun toggleSection(sectionId: String) {
        _expandedSections.value = if (_expandedSections.value.contains(sectionId)) {
            _expandedSections.value - sectionId
        } else {
            _expandedSections.value + sectionId
        }
    }

    fun updateDataValue(elementId: String, value: String) {
        _dataValues.value = _dataValues.value + (elementId to value)
        updateSection(elementId, value)
    }

    private fun updateSection(elementId: String, value: String) {
        val currentState = _uiState.value
        if (currentState is DataEntryState.Success) {
            _uiState.value = DataEntryState.Success(
                currentState.sections.map { section ->
                    section.copy(
                        dataElements = section.dataElements.map { element ->
                            if (element.dataElementId == elementId) {
                                element.copy(value = value)
                            } else {
                                element
                            }
                        }
                    )
                }
            )
        }
    }

    fun saveDataEntry() {
        viewModelScope.launch {
            try {
                if (isExistingEntry()) {
                    repository.updateDataValues(
                        datasetId,
                        periodId!!,
                        orgUnitId!!,
                        attributeOptionComboId!!,
                        _dataValues.value
                    )
                } else {
                    repository.createDataEntry(
                        datasetId,
                        periodId!!,
                        orgUnitId!!,
                        attributeOptionComboId!!,
                        _dataValues.value
                    )
                }
                // Handle success (e.g., show snackbar, navigate back)
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
}