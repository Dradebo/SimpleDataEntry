package com.xavim.testsimpleact.presentation.features.dataEntry

import android.content.ContentValues.TAG
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xavim.testsimpleact.domain.model.DataEntryElement
import com.xavim.testsimpleact.domain.model.DataEntrySection
import com.xavim.testsimpleact.domain.model.DataValue
import com.xavim.testsimpleact.domain.model.ValidationError
import com.xavim.testsimpleact.domain.repository.DataEntryRepository
import com.xavim.testsimpleact.domain.repository.Logger
import com.xavim.testsimpleact.domain.useCase.CreateNewEntryUseCase
import com.xavim.testsimpleact.domain.useCase.GetDataEntryFormUseCase
import com.xavim.testsimpleact.domain.useCase.GetExistingDataValuesUseCase
import com.xavim.testsimpleact.domain.useCase.SaveDataEntryUseCase
import com.xavim.testsimpleact.domain.useCase.ValidateDataEntryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class DataEntryViewModel @Inject constructor(
    private val getDataEntryFormUseCase: GetDataEntryFormUseCase,
    private val getExistingDataValuesUseCase: GetExistingDataValuesUseCase,
    private val saveDataEntryUseCase: SaveDataEntryUseCase,
    private val validateDataEntryUseCase: ValidateDataEntryUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val datasetId: String = checkNotNull(savedStateHandle[DataEntryScreen.DATASET_ID_KEY])
    private val periodId: String? = savedStateHandle[DataEntryScreen.PERIOD_ID_KEY]
    private val orgUnitId: String? = savedStateHandle[DataEntryScreen.ORG_UNIT_ID_KEY]
    private val attributeOptionComboId: String? = savedStateHandle[DataEntryScreen.ATTRIBUTE_OPTION_COMBO_ID_KEY]

    private val _uiState = MutableStateFlow<DataEntryState>(DataEntryState.Loading)
    val uiState: StateFlow<DataEntryState> = _uiState.asStateFlow()

    private val _expandedSections = MutableStateFlow<Set<String>>(emptySet())
    val expandedSections: StateFlow<Set<String>> = _expandedSections.asStateFlow()

    private val _dataValues = MutableStateFlow<Map<String, String>>(emptyMap())
    private val _originalValues = MutableStateFlow<Map<String, String>>(emptyMap())

    private val _validationErrors = MutableStateFlow<List<ValidationError>>(emptyList())
    val validationErrors: StateFlow<List<ValidationError>> = _validationErrors.asStateFlow()

    private val _hasUnsavedChanges = MutableStateFlow(false)
    val hasUnsavedChanges: StateFlow<Boolean> = _hasUnsavedChanges.asStateFlow()

    init {
        loadDataEntry()
    }

    fun loadDataEntry() {
        viewModelScope.launch {
            try {
                _uiState.value = DataEntryState.Loading

                val formFlow = getDataEntryFormUseCase(datasetId)
                val valuesFlow = if (isExistingEntry()) {
                    getExistingDataValuesUseCase(
                        datasetId,
                        periodId!!,
                        orgUnitId!!,
                        attributeOptionComboId!!
                    )
                } else {
                    flow { emit(emptyList<DataValue>()) }
                }

                formFlow.combine(valuesFlow) { sections, values ->
                    val mergedSections = mergeSectionsWithValues(sections, values)

                    // Initialize first section as expanded
                    if (mergedSections.isNotEmpty() && _expandedSections.value.isEmpty()) {
                        _expandedSections.value = setOf(mergedSections.first().uid)
                    }

                    // Store original values for reset functionality
                    val originalValueMap = values.associate { it.dataElementId to it.value }
                    _originalValues.value = originalValueMap
                    _dataValues.value = originalValueMap.toMutableMap()

                    DataEntryState.Success(mergedSections)
                }.catch { e ->
                    _uiState.value = DataEntryState.Error(e.message ?: "Unknown error occurred")
                }.collect {
                    _uiState.value = it
                    _hasUnsavedChanges.value = false
                }
            } catch (e: Exception) {
                _uiState.value = DataEntryState.Error(e.message ?: "Unknown error occurred")
            }
        }
    }

    private fun isExistingEntry(): Boolean =
        !periodId.isNullOrEmpty() && !orgUnitId.isNullOrEmpty() && !attributeOptionComboId.isNullOrEmpty()

    private fun mergeSectionsWithValues(
        sections: List<DataEntrySection>,
        values: List<DataValue>
    ): List<DataEntrySection> {
        val valueMap = values.associateBy { it.dataElementId }

        return sections.map { section ->
            section.copy(
                dataElements = section.dataElements.map { element ->
                    element.copy(
                        value = valueMap[element.dataElementId]?.value ?: ""
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
        _hasUnsavedChanges.value = _dataValues.value != _originalValues.value
        updateSectionWithValue(elementId, value)

        // Validate the field immediately
        validateField(elementId, value)
    }

    private fun updateSectionWithValue(elementId: String, value: String) {
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

    private fun validateField(elementId: String, value: String) {
        val currentErrors = _validationErrors.value.filter { it.elementId != elementId }

        val newErrors = validateDataEntryUseCase.validateField(elementId, value)

        _validationErrors.value = currentErrors + newErrors
    }

    fun validateAndSave(onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val allErrors = validateDataEntryUseCase(_dataValues.value)

                if (allErrors.isEmpty()) {
                    val requiredPeriodId = periodId ?: generatePeriodId()
                    val requiredOrgUnitId = orgUnitId ?: getCurrentOrgUnitId()
                    val requiredAttributeOptionComboId = attributeOptionComboId ?: getDefaultAttributeOptionComboId()

                    saveDataEntryUseCase(
                        datasetId,
                        requiredPeriodId,
                        requiredOrgUnitId,
                        requiredAttributeOptionComboId,
                        _dataValues.value,
                        isNewEntry = !isExistingEntry()
                    )

                    _hasUnsavedChanges.value = false
                    _originalValues.value = _dataValues.value
                    onSuccess()
                } else {
                    _validationErrors.value = allErrors
                    onError("Please fix validation errors before saving")
                }
            } catch (e: Exception) {
                onError(e.message ?: "Unknown error occurred")
            }
        }
    }

    fun resetToOriginalValues() {
        viewModelScope.launch {
            val currentState = _uiState.value
            if (currentState is DataEntryState.Success) {
                // Update UI with original values
                _uiState.value = DataEntryState.Success(
                    mergeSectionsWithValues(
                        currentState.sections,
                        _originalValues.value.map { (id, value) -> DataValue(id, value) }
                    )
                )

                // Reset data values
                _dataValues.value = _originalValues.value.toMap()
                _hasUnsavedChanges.value = false
                _validationErrors.value = emptyList()
            }
        }
    }

    private fun generatePeriodId(): String {
        // Implementation depends on your period generation logic
        val sdf = SimpleDateFormat("yyyyMMdd", Locale.US)
        return sdf.format(Date())
    }

    private fun getCurrentOrgUnitId(): String {
        // Implementation depends on how you determine the current org unit
        throw IllegalStateException("Organization unit ID is required")
    }

    private fun getDefaultAttributeOptionComboId(): String {
        // Implementation depends on your default attribute option combo logic
        throw IllegalStateException("Attribute option combo ID is required")
    }
}