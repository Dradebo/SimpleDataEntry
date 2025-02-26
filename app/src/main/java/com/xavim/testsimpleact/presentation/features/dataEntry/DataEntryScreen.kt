package com.xavim.testsimpleact.presentation.features.dataEntry

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.xavim.testsimpleact.domain.model.DataEntryElement
import com.xavim.testsimpleact.domain.model.DataEntrySection
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

//@Composable
//fun DataEntryScreen(
//    datasetId: String,
//    periodId: String? = null,
//    orgUnitId: String? = null,
//    attributeOptionComboId: String? = null,
//    viewModel: DataEntryViewModel = hiltViewModel()
//) {
//    val uiState by viewModel.uiState.collectAsState()
//    val expandedSections by viewModel.expandedSections.collectAsState()
//
//    when (val currentState = uiState) {
//        DataEntryScreenState.Loading -> {
//            CircularProgressIndicator()
//        }
//
//        is DataEntryScreenState.Success -> {
//            Column(modifier = Modifier.padding(16.dp)) {
//                currentState.sections.forEach { section ->
//                    AccordionItem(
//                        title = section.name,
//                        expanded = expandedSections.contains(section.uid),
//                        onHeaderClick = { viewModel.onSectionHeaderClick(section.uid) }
//                    ) {
//                        SectionContent(
//                            section = section,
//                            onInputChange = { dataElementUid, value ->
//                                viewModel.onInputChange(dataElementUid, value)
//                            }
//                        )
//                    }
//                }
//            }
//        }
//
//        is DataEntryScreenState.Error -> {
//            Text("Error: ${currentState.message}")
//        }
//    }
//}
//
//@Composable
//fun SectionContent(
//    section: DataEntrySection,
//    onInputChange: (String, String) -> Unit
//) {
//    Column {
//        section.dataElements.forEach { element ->
//            DataElementField(element = element, onValueChange = { value ->
//                onInputChange(element.dataElementId, value)
//            })
//        }
//    }
//}
//
//@Composable
//fun DataElementField(
//    element: DataEntryElement,
//    onValueChange: (String) -> Unit
//) {
//    Column(
//        modifier = Modifier
//            .padding(8.dp)
//    ) {
//        if (element.type == "text") {
//            var text by remember { mutableStateOf(element.value ?: "") }
//            OutlinedTextField(
//                value = text,
//                onValueChange = {
//                    text = it
//                    onValueChange(it)
//                },
//                label = { Text(element.name) },
//                modifier = Modifier.padding(8.dp)
//            )
//        }
//    }
//}
//
//@Composable
//fun AccordionItem(
//    title: String,
//    expanded: Boolean,
//    onHeaderClick: () -> Unit,
//    content: @Composable () -> Unit
//) {
//    var isExpanded by remember { mutableStateOf(expanded) }
//
//    Card(
//        modifier = Modifier
//            .fillMaxWidth()
//            .padding(8.dp)
//    ) {
//        Column(
//            modifier = Modifier
//                .fillMaxWidth()
//        ) {
//            Row(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .clickable {
//                        isExpanded = !isExpanded
//                        onHeaderClick()
//                    }
//                    .padding(16.dp),
//                verticalAlignment = Alignment.CenterVertically
//            ) {
//                Text(
//                    text = title,
//                    modifier = Modifier.weight(1f)
//                )
//                Icon(
//                    imageVector = Icons.Filled.ArrowDropDown,
//                    contentDescription = "Expand/Collapse",
//                    modifier = Modifier.clickable {
//                        isExpanded = !isExpanded
//                        onHeaderClick()
//                    }
//                )
//            }
//            androidx.compose.animation.AnimatedVisibility(visible = isExpanded) {
//                Column(modifier = Modifier.padding(16.dp)) {
//                    content()
//                }
//            }
//        }
//    }
//}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DataEntryScreen(
    viewModel: DataEntryViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val expandedSections by viewModel.expandedSections.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Data Entry") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.saveDataEntry() }) {
                        Icon(Icons.Default.Save, contentDescription = "Save")
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
                    items(state.sections) { section ->
                        DataEntrySection(
                            section = section,
                            isExpanded = expandedSections.contains(section.uid),
                            onSectionClick = { viewModel.toggleSection(section.uid) },
                            onValueChange = { elementId, value ->
                                viewModel.updateDataValue(elementId, value)
                            }
                        )
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
                    Text(
                        text = state.message,
                        color = Color.Red
                    )
                }
            }
        }
    }
}

@Composable
private fun DataEntrySection(
    section: DataEntrySection,
    isExpanded: Boolean,
    onSectionClick: () -> Unit,
    onValueChange: (String, String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 6.dp,
            pressedElevation = 12.dp
        )
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
                Text(
                    text = section.name,
                    style = MaterialTheme.typography.titleMedium
                )
                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (isExpanded) "Collapse" else "Expand"
                )
            }

            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    section.dataElements.forEach { element ->
                        DataElementInput(
                            element = element,
                            onValueChange = { value -> onValueChange(element.dataElementId, value) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DataElementInput(
    element: DataEntryElement,
    onValueChange: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(
            text = element.name,
            style = MaterialTheme.typography.labelSmall
        )

        when (element.type) {
            "NUMBER" -> NumberInput(
                value = element.value,
                onValueChange = onValueChange
            )
            "TEXT" -> TextInput(
                value = element.value,
                onValueChange = onValueChange
            )
            // Add more input types as needed
        }
    }
}

@Composable
private fun NumberInput(
    value: String,
    onValueChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = { newValue ->
            if (newValue.isEmpty() || newValue.toDoubleOrNull() != null) {
                onValueChange(newValue)
            }
        },
        modifier = Modifier.fillMaxWidth(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
    )
}

@Composable
private fun TextInput(
    value: String,
    onValueChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth()
    )
}