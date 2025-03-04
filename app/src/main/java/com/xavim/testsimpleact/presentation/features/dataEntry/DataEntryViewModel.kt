package com.xavim.testsimpleact.presentation.features.dataEntry

import CreateNewEntryUseCase
import GetDataEntryFormUseCase
import GetExistingDataValuesUseCase
import SaveDataEntryUseCase
import com.xavim.testsimpleact.domain.useCase.ValidateDataEntryUseCase
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xavim.testsimpleact.domain.model.DataEntrySection
import com.xavim.testsimpleact.domain.model.DataValue
import com.xavim.testsimpleact.domain.model.ValidationError
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import javax.inject.Inject



@HiltViewModel
class DataEntryViewModel @Inject constructor(
    private val getDataEntryFormUseCase: GetDataEntryFormUseCase,
    private val getExistingDataValuesUseCase: GetExistingDataValuesUseCase,
    private val saveDataEntryUseCase: SaveDataEntryUseCase,
    private val validateDataEntryUseCase: ValidateDataEntryUseCase,
    private val createNewEntryUseCase: CreateNewEntryUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    // Navigation arguments
    private val datasetId: String = checkNotNull(savedStateHandle[DataEntryScreen.DATASET_ID_KEY])
    private val periodId: String? = savedStateHandle[DataEntryScreen.PERIOD_ID_KEY]
    private val orgUnitId: String? = savedStateHandle[DataEntryScreen.ORG_UNIT_ID_KEY]
    private val attributeOptionComboId: String? = savedStateHandle[DataEntryScreen.ATTRIBUTE_OPTION_COMBO_ID_KEY]

    // UI state
    private val _uiState = MutableStateFlow<DataEntryState>(DataEntryState.Loading)
    val uiState: StateFlow<DataEntryState> = _uiState.asStateFlow()

    // Section expansion state
    private val _expandedSections = MutableStateFlow<Set<String>>(emptySet())
    val expandedSections: StateFlow<Set<String>> = _expandedSections.asStateFlow()

    // Validation errors
    private val _validationErrors = MutableStateFlow<List<ValidationError>>(emptyList())
    val validationErrors: StateFlow<List<ValidationError>> = _validationErrors.asStateFlow()

    // Track unsaved changes
    private val _hasUnsavedChanges = MutableStateFlow(false)
    val hasUnsavedChanges: StateFlow<Boolean> = _hasUnsavedChanges.asStateFlow()

    // Store values for data elements and their category option combos
    private val _dataValues = MutableStateFlow<Map<String, String>>(emptyMap())
    private val _originalValues = MutableStateFlow<Map<String, String>>(emptyMap())

    // Track expanded category option combos within sections
    private val _expandedCategoryOptionComboSections = MutableStateFlow<Set<String>>(emptySet())
    val expandedCategoryOptionComboSections: StateFlow<Set<String>> = _expandedCategoryOptionComboSections.asStateFlow()

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
                    flow { emit(emptyList()) }
                }

                // Combine form structure with values
                formFlow.combine(valuesFlow) { sections: List<DataEntrySection>, values: List<DataValue> ->
                    // Convert values to a map for easier lookup
                    val valueMap = values.associate {
                        "${it.dataElementId}_${it.categoryOptionComboId}" to it.value
                    }

                    // Store original values
                    _originalValues.value = valueMap
                    _dataValues.value = valueMap.toMutableMap()

                    // Update the UI model with values
                    val mergedSections = mergeSectionsWithValues(sections, valueMap)

                    // Initialize the first section as expanded by default
                    if (mergedSections.isNotEmpty() && _expandedSections.value.isEmpty()) {
                        _expandedSections.value = setOf(mergedSections.first().uid)
                    }

                    DataEntryState.Success(mergedSections)
                }.catch { error: Throwable ->
                    Log.e( "Error loading data entry", error.toString())
                    _uiState.value = DataEntryState.Error(error.message ?: "Unknown error occurred")
                }.collect {
                    _uiState.value = it
                    _hasUnsavedChanges.value = false
                }
            } catch (e: Exception) {
                Log.e("Error in loadDataEntry", e.toString())
                _uiState.value = DataEntryState.Error(e.message ?: "Unknown error occurred")
            }
        }
    }

    private fun isExistingEntry(): Boolean =
        !periodId.isNullOrEmpty() && !orgUnitId.isNullOrEmpty() && !attributeOptionComboId.isNullOrEmpty()

    private fun mergeSectionsWithValues(
        sections: List<DataEntrySection>,
        valueMap: Map<String, String>
    ): List<DataEntrySection> {
        return sections.map { section ->
            section.copy(
                dataElements = section.dataElements.map { element ->
                    // For each data element, check all its category option combos for values
                    element.copy(
                        categoryOptionCombos = element.categoryOptionCombos.map { combo ->
                            val key = "${element.dataElementId}_${combo.uid}"
                            combo.copy(value = valueMap[key] ?: "")
                        }
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

    fun toggleCategoryOptionComboSection(sectionKey: String) {
        _expandedCategoryOptionComboSections.value = if (_expandedCategoryOptionComboSections.value.contains(sectionKey)) {
            _expandedCategoryOptionComboSections.value - sectionKey
        } else {
            _expandedCategoryOptionComboSections.value + sectionKey
        }
    }

    fun updateDataValue(
        dataElementId: String,
        categoryOptionComboId: String,
        value: String
    ) {
        val key = "${dataElementId}_${categoryOptionComboId}"
        val currentValues = _dataValues.value.toMutableMap()
        currentValues[key] = value
        _dataValues.value = currentValues

        // Check if there are unsaved changes
        _hasUnsavedChanges.value = _dataValues.value != _originalValues.value

        // Update UI state to reflect the new value
        updateSectionWithValue(dataElementId, categoryOptionComboId, value)

        // Validate the field immediately
        validateField(dataElementId, value)
    }

    private fun updateSectionWithValue(
        dataElementId: String,
        categoryOptionComboId: String,
        value: String
    ) {
        val currentState = _uiState.value
        if (currentState is DataEntryState.Success) {
            _uiState.value = DataEntryState.Success(
                currentState.sections.map { section ->
                    section.copy(
                        dataElements = section.dataElements.map { element ->
                            if (element.dataElementId == dataElementId) {
                                element.copy(
                                    categoryOptionCombos = element.categoryOptionCombos.map { combo ->
                                        if (combo.uid == categoryOptionComboId) {
                                            combo.copy(value = value)
                                        } else {
                                            combo
                                        }
                                    }
                                )
                            } else {
                                element
                            }
                        }
                    )
                }
            )
        }
    }

    private fun validateField(dataElementId: String, value: String) {
        viewModelScope.launch {
            val currentErrors = _validationErrors.value.filter { it.elementId != dataElementId }
            val newErrors = validateDataEntryUseCase.validateField(dataElementId, value)
            _validationErrors.value = currentErrors + newErrors
        }
    }

    fun validateAndSave(onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val allErrors = validateDataEntryUseCase(_dataValues.value)

                if (allErrors.isEmpty()) {
                    val requiredPeriodId = periodId ?: createNewEntryUseCase.generatePeriodId()
                    val requiredOrgUnitId = orgUnitId ?: createNewEntryUseCase.getDefaultOrgUnitId()
                    val requiredAttributeOptionComboId = attributeOptionComboId ?:
                    createNewEntryUseCase.getDefaultAttributeOptionComboId()

                    saveDataEntryUseCase(
                        datasetId,
                        requiredPeriodId,
                        requiredOrgUnitId,
                        requiredAttributeOptionComboId,
                        _dataValues.value,
                        isNewEntry = !isExistingEntry()
                    ).collect { result: Result<Unit> -> // Explicitly specify the type as Result<Unit>
                        result.onSuccess {
                            _hasUnsavedChanges.value = false
                            _originalValues.value = _dataValues.value
                            onSuccess()
                        }.onFailure { error: Throwable -> // Explicitly specify the type as Throwable
                            onError(error.message ?: "Error saving data")
                        }
                    }
                } else {
                    _validationErrors.value = allErrors
                    onError("Please fix validation errors before saving")
                }
            } catch (e: Exception) {
                Log.e("Error in validateAndSave", e.toString())
                onError(e.message ?: "Unknown error occurred while saving")
            }
        }
    }

    fun resetToOriginalValues() {
        _dataValues.value = _originalValues.value
        _hasUnsavedChanges.value = false
        refreshDataDisplay()
    }

    private fun refreshDataDisplay() {
        val currentState = _uiState.value
        if (currentState is DataEntryState.Success) {
            _uiState.value = DataEntryState.Success(
                mergeSectionsWithValues(currentState.sections, _dataValues.value)
            )
        }
    }

    // Helper to determine if a data element has too many category options to display inline
    fun shouldUseNestedAccordion(dataElementId: String): Boolean {
        val currentState = _uiState.value
        if (currentState is DataEntryState.Success) {
            val element = currentState.sections
                .flatMap { it.dataElements }
                .find { it.dataElementId == dataElementId }

            // If more than 5 category options, use nested accordion
            return element?.categoryOptionCombos?.size ?: 0 > 5
        }
        return false
    }
}