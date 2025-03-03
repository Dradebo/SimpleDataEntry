package com.xavim.testsimpleact.presentation.features.datasetInstances

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.xavim.testsimpleact.domain.model.DatasetInstance
import com.xavim.testsimpleact.domain.model.DatasetInstanceState
import com.xavim.testsimpleact.presentation.core.ErrorState
import com.xavim.testsimpleact.presentation.core.LoadingState
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatasetInstanceDetailScreen(
    datasetId: String,
    periodId: String,
    orgUnitId: String,
    attributeOptionComboId: String,
    viewModel: DatasetInstanceDetailViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onNavigateToDataEntry: (String, String, String, String) -> Unit
) {
    val state by viewModel.state.collectAsState()
    val isProcessing by viewModel.isProcessing.collectAsState()
    val message by viewModel.message.collectAsState()

    var showConfirmCompleteDialog by remember { mutableStateOf(false) }
    var showConfirmReopenDialog by remember { mutableStateOf(false) }

    // Show message as snackbar
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(message) {
        message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Dataset Details") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val currentState = state) {
                is DatasetInstanceDetailState.Loading -> {
                    LoadingState()
                }
                is DatasetInstanceDetailState.Success -> {
                    DatasetInstanceDetailContent(
                        instance = currentState.instance,
                        isEditable = currentState.isEditable,
                        hasCompletePermission = currentState.hasCompletePermission,
                        onEditClick = {
                            onNavigateToDataEntry(
                                datasetId,
                                periodId,
                                orgUnitId,
                                attributeOptionComboId
                            )
                        },
                        onCompleteClick = { showConfirmCompleteDialog = true },
                        onReopenClick = { showConfirmReopenDialog = true },
                        onSyncClick = { viewModel.syncDatasetInstance() }
                    )
                }
                is DatasetInstanceDetailState.Error -> {
                    ErrorState(
                        message = currentState.message,
                        onRetry = { viewModel.loadInstanceDetails() }
                    )
                }
            }

            // Loading overlay
            if (isProcessing) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }
    }

    // Complete confirmation dialog
    if (showConfirmCompleteDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmCompleteDialog = false },
            title = { Text("Complete Dataset") },
            text = { Text("Are you sure you want to complete this dataset? This will mark it as completed and lock it for editing.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.completeDatasetInstance()
                        showConfirmCompleteDialog = false
                    }
                ) {
                    Text("Complete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmCompleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Reopen confirmation dialog
    if (showConfirmReopenDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmReopenDialog = false },
            title = { Text("Reopen Dataset") },
            text = { Text("Are you sure you want to reopen this dataset? This will allow editing again.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.reopenDatasetInstance()
                        showConfirmReopenDialog = false
                    }
                ) {
                    Text("Reopen")
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmReopenDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun DatasetInstanceDetailContent(
    instance: DatasetInstance,
    isEditable: Boolean,
    hasCompletePermission: Boolean,
    onEditClick: () -> Unit,
    onCompleteClick: () -> Unit,
    onReopenClick: () -> Unit,
    onSyncClick: () -> Unit
) {
    val dateFormat = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Header with status
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = instance.periodDisplayName ?: instance.periodId,
                style = MaterialTheme.typography.headlineMedium
            )
            StatusChip(instance.state)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Organisation unit
        DetailItem(
            label = "Organisation Unit",
            value = instance.organisationUnitDisplayName ?: "Unknown"
        )

        // Period
        DetailItem(
            label = "Period",
            value = instance.periodDisplayName ?: instance.periodId
        )

        // Last updated
        DetailItem(
            label = "Last Updated",
            value = dateFormat.format(instance.lastUpdated)
        )

        // Created
        DetailItem(
            label = "Created",
            value = dateFormat.format(instance.createdAt)
        )

        // Completed info (if completed)
        if (instance.state == DatasetInstanceState.COMPLETED && instance.completedDate != null) {
            DetailItem(
                label = "Completed Date",
                value = dateFormat.format(instance.completedDate)
            )

            instance.completedBy?.let {
                DetailItem(
                    label = "Completed By",
                    value = it
                )
            }
        }

        // Value count
        DetailItem(
            label = "Values",
            value = "${instance.valueCount} data values"
        )

        // Sync status
        DetailItem(
            label = "Sync Status",
            value = instance.syncState.name,
            icon = {
                SyncStateIcon(instance.syncState)
            }
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Action buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            // Edit button
            Button(
                onClick = onEditClick,
                enabled = isEditable,
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.Edit, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Edit")
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Complete/Reopen button
            if (hasCompletePermission) {
                if (instance.state == DatasetInstanceState.OPEN) {
                    Button(
                        onClick = onCompleteClick,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Done, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Complete")
                    }
                } else {
                    Button(
                        onClick = onReopenClick,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Reopen")
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Sync button
            Button(
                onClick = onSyncClick,
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.Sync, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Sync")
            }
        }
    }
}

@Composable
fun DetailItem(
    label: String,
    value: String,
    icon: @Composable (() -> Unit)? = null
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f)
            )
            icon?.invoke()
        }

        Divider(modifier = Modifier.padding(top = 8.dp))
    }
}