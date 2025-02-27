package com.xavim.testsimpleact.presentation.features.dataEntry

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.xavim.testsimpleact.domain.model.DataEntryElement
import com.xavim.testsimpleact.domain.model.DataEntrySection
import com.xavim.testsimpleact.domain.model.ValidationError
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DataEntryScreen(
    viewModel: DataEntryViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val expandedSections by viewModel.expandedSections.collectAsState()
    val validationErrors by viewModel.validationErrors.collectAsState()
    val hasUnsavedChanges by viewModel.hasUnsavedChanges.collectAsState()

    val context = LocalContext.current

    var showDiscardDialog by remember { mutableStateOf(false) }
    var showSaveSuccessMessage by remember { mutableStateOf(false) }

    BackHandler(enabled = hasUnsavedChanges) {
        showDiscardDialog = true
    }

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
                            onValueChange = { elementId, value ->
                                viewModel.updateDataValue(elementId, value)
                            },
                            validationErrors = validationErrors.filter { error ->
                                section.dataElements.any { it.dataElementId == error.elementId }
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
                            text = state.message
                           // color = MaterialTheme.colors.error
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
            .padding(horizontal = 16.dp)
        //backgroundColor = MaterialTheme.colors.error.copy(alpha = 0.1f),
        //elevation =
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Please fix the following errors:",
                //style = MaterialTheme.typography.subtitle1,
                //color = MaterialTheme.colors.error
            )
            Spacer(modifier = Modifier.height(8.dp))
            errors.forEach { error ->
                Text(
                    text = "• ${error.message}",
                    //style = MaterialTheme.typography.body2,
                    //color = MaterialTheme.colors.error
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
    onValueChange: (String, String) -> Unit,
    validationErrors: List<ValidationError>
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        //elevation = 2.dp,
        border = if (validationErrors.isNotEmpty())
            BorderStroke(1.dp, color = Color.Red)
        else
            null
    ) {
        Column {
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
                    if (validationErrors.isNotEmpty()) {
                        Text(
                            text = "${validationErrors.size} error(s)",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.Red
                        )
                    }
                }
                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (isExpanded) "Collapse" else "Expand"
                )
            }

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
                        val elementError = validationErrors.find { it.elementId == element.dataElementId }
                        DataElementInput(
                            element = element,
                            onValueChange = { value -> onValueChange(element.dataElementId, value) },
                            error = elementError
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun DataElementInput(
    element: DataEntryElement,
    onValueChange: (String) -> Unit,
    error: ValidationError?
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = element.name,
            style = MaterialTheme.typography.titleSmall
        )

        when (element.type) {
            "NUMBER" -> NumberInput(
                value = element.value,
                onValueChange = onValueChange,
                error = error
            )
            "TEXT" -> TextInput(
                value = element.value,
                onValueChange = onValueChange,
                error = error
            )
            "BOOLEAN" -> BooleanInput(
                value = element.value,
                onValueChange = onValueChange,
                error = error
            )
            "DATE" -> DateInput(
                value = element.value,
                onValueChange = onValueChange,
                error = error
            )
            // Add other input types as needed
            else -> TextInput(
                value = element.value,
                onValueChange = onValueChange,
                error = error
            )
        }

        if (error != null) {
            Text(
                text = error.message,
                style = MaterialTheme.typography.labelMedium,
                color = Color.Red,
                modifier = Modifier.padding(start = 8.dp, top = 4.dp)
            )
        }
    }
}

@Composable
private fun NumberInput(
    value: String,
    onValueChange: (String) -> Unit,
    error: ValidationError?
) {
    OutlinedTextField(
        value = value,
        onValueChange = { newValue ->
            if (newValue.isEmpty() || newValue.toDoubleOrNull() != null) {
                onValueChange(newValue)
            }
        },
        modifier = Modifier.fillMaxWidth(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        isError = error != null,
        singleLine = true
    )
}

@Composable
private fun TextInput(
    value: String,
    onValueChange: (String) -> Unit,
    error: ValidationError?
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        isError = error != null,
        singleLine = true
    )
}

@Composable
private fun BooleanInput(
    value: String,
    onValueChange: (String) -> Unit,
    error: ValidationError?
) {
    val isChecked = value.equals("true", ignoreCase = true)

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = isChecked,
            onCheckedChange = { checked ->
                onValueChange(checked.toString())
            }
        )
        Text(
            text = if (isChecked) "Yes" else "No",
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}

@Composable
private fun DateInput(
    value: String,
    onValueChange: (String) -> Unit,
    error: ValidationError?
) {
    var showDatePicker by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val dateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    val date = remember(value) {
        try {
            if (value.isNotEmpty()) dateFormatter.parse(value) else null
        } catch (e: Exception) {
            null
        }
    }

    OutlinedTextField(
        value = if (date != null) dateFormatter.format(date) else "",
        onValueChange = { /* Read-only */ },
        modifier = Modifier.fillMaxWidth(),
        isError = error != null,
        singleLine = true,
        readOnly = true,
        trailingIcon = {
            IconButton(onClick = { showDatePicker = true }) {
                Icon(Icons.Default.DateRange, contentDescription = "Pick date")
            }
        }
    )

    if (showDatePicker && Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        val calendar = Calendar.getInstance()
        date?.let { calendar.time = it }

        val datePicker = DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                calendar.set(year, month, dayOfMonth)
                onValueChange(dateFormatter.format(calendar.time))
                showDatePicker = false
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )

        DisposableEffect(Unit) {
            datePicker.show()
            onDispose {
                datePicker.dismiss()
            }
        }
    }
}