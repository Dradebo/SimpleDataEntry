package com.xavim.testsimpleact.presentation.features.dataEntry

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.xavim.testsimpleact.domain.model.*
import com.xavim.testsimpleact.presentation.core.DateInputField
import com.xavim.testsimpleact.presentation.core.NumericInputField
import com.xavim.testsimpleact.presentation.core.OptionSetField
import com.xavim.testsimpleact.presentation.core.TextInputField
import android.os.Handler
import android.os.Looper
import android.widget.Toast


object DataEntryScreen {
    const val DATASET_ID_KEY = "datasetId"
    const val PERIOD_ID_KEY = "periodId"
    const val ORG_UNIT_ID_KEY = "orgUnitId"
    const val ATTRIBUTE_OPTION_COMBO_ID_KEY = "attributeOptionComboId"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DataEntryScreen(
    viewModel: DataEntryViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val expandedSections by viewModel.expandedSections.collectAsState()
    val expandedCategoryOptionComboSections by viewModel.expandedCategoryOptionComboSections.collectAsState()
    val validationErrors by viewModel.validationErrors.collectAsState()
    val hasUnsavedChanges by viewModel.hasUnsavedChanges.collectAsState()

    val context = LocalContext.current

    var showDiscardDialog by remember { mutableStateOf(false) }
    var showSaveSuccessMessage by remember { mutableStateOf(false) }

    // Handle back navigation with unsaved changes
    BackHandler(enabled = hasUnsavedChanges) {
        showDiscardDialog = true
    }

    // Discard changes dialog
    if (showDiscardDialog) {
        AlertDialog(
            onDismissRequest = { showDiscardDialog = false },
            title = { Text("Unsaved Changes") },
            text = { Text("You have unsaved changes. Are you sure you want to discard them?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDiscardDialog = false
                        onNavigateBack()
                    }
                ) {
                    Text("Discard")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDiscardDialog = false }
                ) {
                    Text("Continue Editing")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Data Entry") },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            if (hasUnsavedChanges) {
                                showDiscardDialog = true
                            } else {
                                onNavigateBack()
                            }
                        }
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        viewModel.resetToOriginalValues()
                    }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Reset"
                        )
                    }
                    IconButton(onClick = {
                        viewModel.validateAndSave(
                            onSuccess = {
                                showSaveSuccessMessage = true
                                // Show success message
                                Toast.makeText(context, "Data saved successfully", Toast.LENGTH_SHORT).show()
                                // Navigate back after short delay
                                Handler(Looper.getMainLooper()).postDelayed({
                                    onNavigateBack()
                                }, 1500)
                            },
                            onError = { errorMessage ->
                                Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
                            }
                        )
                    }) {
                        Icon(
                            imageVector = Icons.Default.Save,
                            contentDescription = "Save"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        when (val state = uiState) {
            is DataEntryState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            is DataEntryState.Success -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    item {
                        if (validationErrors.isNotEmpty()) {
                            ValidationErrorsCard(validationErrors)
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }

                    items(state.sections) { section ->
                        DataEntrySection(
                            section = section,
                            isExpanded = expandedSections.contains(section.uid),
                            onSectionClick = { viewModel.toggleSection(section.uid) },
                            onCategoryOptionComboSectionClick = { viewModel.toggleCategoryOptionComboSection(it) },
                            expandedCategoryOptionComboSections = expandedCategoryOptionComboSections,
                            onValueChange = { dataElementId, categoryOptionComboId, value ->
                                viewModel.updateDataValue(dataElementId, categoryOptionComboId, value)
                            },
                            validationErrors = validationErrors,
                            shouldUseNestedAccordion = { dataElementId ->
                                viewModel.shouldUseNestedAccordion(dataElementId)
                            }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }

            is DataEntryState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = state.message,
                            color = MaterialTheme.colorScheme.error
                        )
                        Button(onClick = { viewModel.loadDataEntry() }) {
                            Text("Retry")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ValidationErrorsCard(errors: List<ValidationError>) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Please fix the following errors:",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
            Spacer(modifier = Modifier.height(8.dp))
            errors.forEach { error ->
                Text(
                    text = "• ${error.message}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }
    }
}

@Composable
private fun DataEntrySection(
    section: DataEntrySection,
    isExpanded: Boolean,
    onSectionClick: () -> Unit,
    onCategoryOptionComboSectionClick: (String) -> Unit,
    expandedCategoryOptionComboSections: Set<String>,
    onValueChange: (String, String, String) -> Unit,
    validationErrors: List<ValidationError>,
    shouldUseNestedAccordion: (String) -> Boolean
) {
    // Find errors related to this section
    val sectionErrors = validationErrors.filter { error ->
        section.dataElements.any { it.dataElementId == error.elementId }
    }

    // Determine if section has errors
    val hasSectionErrors = sectionErrors.isNotEmpty()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        border = if (hasSectionErrors)
            BorderStroke(1.dp, color = MaterialTheme.colorScheme.error)
        else
            null
    ) {
        Column {
            // Section header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onSectionClick)
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = section.name,
                        style = MaterialTheme.typography.titleMedium
                    )
                    if (hasSectionErrors) {
                        Text(
                            text = "${sectionErrors.size} error(s)",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (isExpanded) "Collapse" else "Expand"
                )
            }

            // Section content (only visible when expanded)
            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            start = 16.dp,
                            end = 16.dp,
                            bottom = 16.dp
                        )
                ) {
                    section.dataElements.forEach { element ->
                        // Get errors for this data element
                        val elementErrors = validationErrors.filter { it.elementId == element.dataElementId }

                        // Check if we should use a nested accordion for this element's category options
                        if (shouldUseNestedAccordion(element.dataElementId)) {
                            NestedCategoryOptionsAccordion(
                                element = element,
                                isExpanded = expandedCategoryOptionComboSections.contains(element.dataElementId),
                                onToggleExpand = { onCategoryOptionComboSectionClick(element.dataElementId) },
                                onValueChange = { categoryOptionComboId, value ->
                                    onValueChange(element.dataElementId, categoryOptionComboId, value)
                                },
                                errors = elementErrors
                            )
                        } else {
                            // If few category options, display them directly
                            element.categoryOptionCombos.forEach { combo ->
                                // For default category option combo, don't show its name
                                val fieldLabel = if (combo.isDefault) element.name
                                else "${element.name} - ${combo.name}"

                                DataElementField(
                                    dataElementId = element.dataElementId,
                                    categoryOptionComboId = combo.uid,
                                    label = fieldLabel,
                                    value = combo.value,
                                    valueType = element.valueType,
                                    mandatory = element.mandatory,
                                    optionSetUid = element.optionSetUid,
                                    onValueChange = { value ->
                                        onValueChange(element.dataElementId, combo.uid, value)
                                    },
                                    error = elementErrors.firstOrNull()?.message
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun NestedCategoryOptionsAccordion(
    element: DataEntryElement,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onValueChange: (String, String) -> Unit,
    errors: List<ValidationError>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        border = if (errors.isNotEmpty())
            BorderStroke(1.dp, color = MaterialTheme.colorScheme.error)
        else
            null
    ) {
        Column {
            // Element header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onToggleExpand)
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = element.name,
                        style = MaterialTheme.typography.titleSmall
                    )
                    if (errors.isNotEmpty()) {
                        Text(
                            text = "${errors.size} error(s)",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (isExpanded) "Collapse" else "Expand"
                )
            }

            // Category option combos (only visible when expanded)
            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            start = 12.dp,
                            end = 12.dp,
                            bottom = 12.dp
                        )
                ) {
                    element.categoryOptionCombos.forEach { combo ->
                        val fieldLabel = if (combo.isDefault) "" else combo.name

                        DataElementField(
                            dataElementId = element.dataElementId,
                            categoryOptionComboId = combo.uid,
                            label = fieldLabel,
                            value = combo.value,
                            valueType = element.valueType,
                            mandatory = element.mandatory,
                            optionSetUid = element.optionSetUid,
                            onValueChange = { value ->
                                onValueChange(combo.uid, value)
                            },
                            error = errors.firstOrNull()?.message
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun DataElementField(
    dataElementId: String,
    categoryOptionComboId: String,
    label: String,
    value: String,
    valueType: DataElementValueType,
    mandatory: Boolean,
    optionSetUid: String? = null,
    onValueChange: (String) -> Unit,
    error: String? = null
) {
    val displayLabel = if (mandatory) "$label *" else label

    when {
        optionSetUid != null -> {
            OptionSetField(
                label = displayLabel,
                value = value,
                options = emptyList(), // This would need to be populated from ViewModel
                onValueChange = onValueChange,
                error = error
            )
        }
        valueType == DataElementValueType.DATE || valueType == DataElementValueType.DATETIME -> {
            DateInputField(
                label = displayLabel,
                value = value,
                onValueChange = onValueChange,
                error = error
            )
        }
        valueType.isNumeric -> {
            NumericInputField(
                label = displayLabel,
                value = value,
                onValueChange = onValueChange,
                error = error,
                valueType = valueType
            )
        }
        else -> {
            TextInputField(
                label = displayLabel,
                value = value,
                onValueChange = onValueChange,
                error = error,
                multiline = valueType == DataElementValueType.LONG_TEXT
            )
        }
    }
}