//package com.xavim.testsimpleact.presentation.features.datasetInstances
//
//import androidx.compose.animation.AnimatedVisibility
//import androidx.compose.animation.fadeIn
//import androidx.compose.animation.fadeOut
//import androidx.compose.foundation.clickable
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.lazy.LazyColumn
//import androidx.compose.foundation.lazy.items
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.*
//import androidx.compose.material3.*
//import androidx.compose.runtime.*
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.text.style.TextOverflow
//import androidx.compose.ui.unit.dp
//import androidx.hilt.navigation.compose.hiltViewModel
//import com.xavim.testsimpleact.domain.model.DatasetInstance
//import com.xavim.testsimpleact.domain.model.DatasetInstanceState
//import com.xavim.testsimpleact.domain.model.SyncState
//import com.xavim.testsimpleact.presentation.core.EmptyState
//import com.xavim.testsimpleact.presentation.core.ErrorState
//import com.xavim.testsimpleact.presentation.core.LoadingState
//import java.text.SimpleDateFormat
//import java.util.*
//
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun DatasetInstanceListScreen(
//    datasetId: String,
//    viewModel: DatasetInstanceListViewModel = hiltViewModel(),
//    onNavigateBack: () -> Unit,
//    onItemClick: (String, String, String, String) -> Unit,
//    onNewEntryClick: (String) -> Unit
//) {
//    val state by viewModel.state.collectAsState()
//    val datasetName by viewModel.datasetName.collectAsState()
//    val isRefreshing by viewModel.isRefreshing.collectAsState()
//    val sortOrder by viewModel.sortOrder.collectAsState()
//
//    var showFilterDialog by remember { mutableStateOf(false) }
//    var showSortDialog by remember { mutableStateOf(false) }
//
//    Scaffold(
//        topBar = {
//            TopAppBar(
//                title = {
//                    Text(
//                        text = datasetName ?: "Dataset Instances",
//                        maxLines = 1,
//                        overflow = TextOverflow.Ellipsis
//                    )
//                },
//                navigationIcon = {
//                    IconButton(onClick = onNavigateBack) {
//                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
//                    }
//                },
//                actions = {
//                    IconButton(onClick = { showSortDialog = true }) {
//                        Icon(Icons.Default.Sort, contentDescription = "Sort")
//                    }
//                    IconButton(onClick = { showFilterDialog = true }) {
//                        Icon(Icons.Default.FilterList, contentDescription = "Filter")
//                    }
//                    IconButton(onClick = { viewModel.refresh() }) {
//                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
//                    }
//                }
//            )
//        },
//        floatingActionButton = {
//            FloatingActionButton(
//                onClick = { onNewEntryClick(datasetId) }
//            ) {
//                Icon(Icons.Default.Add, contentDescription = "Add new entry")
//            }
//        }
//    ) { paddingValues ->
//        Box(
//            modifier = Modifier
//                .fillMaxSize()
//                .padding(paddingValues)
//        ) {
//            when (val currentState = state) {
//                is DatasetInstanceListState.Loading -> {
//                    LoadingState()
//                }
//                is DatasetInstanceListState.Success -> {
//                    if (currentState.entries.isEmpty()) {
//                        EmptyState(
//                            message = "No dataset instances found",
//                            icon = Icons.Default.DataArray
//                        )
//                    } else {
//                        DatasetInstanceList(
//                            instances = currentState.entries,
//                            onItemClick = { instance ->
//                                onItemClick(
//                                    instance.datasetId,
//                                    instance.periodId,
//                                    instance.organisationUnitUid,
//                                    instance.attributeOptionComboUid
//                                )
//                            }
//                        )
//                    }
//                }
//                is DatasetInstanceListState.Error -> {
//                    ErrorState(
//                        message = currentState.message,
//                        onRetry = { viewModel.refresh() }
//                    )
//                }
//            }
//
//            // Loading indicator
//            AnimatedVisibility(
//                visible = isRefreshing,
//                enter = fadeIn(),
//                exit = fadeOut()
//            ) {
//                Box(
//                    modifier = Modifier.fillMaxSize(),
//                    contentAlignment = Alignment.Center
//                ) {
//                    CircularProgressIndicator()
//                }
//            }
//        }
//    }
//
//    // Sort dialog
//    if (showSortDialog) {
//        AlertDialog(
//            onDismissRequest = { showSortDialog = false },
//            title = { Text("Sort By") },
//            text = {
//                Column {
//                    SortOption(
//                        title = "Date (Newest first)",
//                        selected = sortOrder == SortOrder.DATE_DESC,
//                        onClick = {
//                            viewModel.setSortOrder(SortOrder.DATE_DESC)
//                            showSortDialog = false
//                        }
//                    )
//                    SortOption(
//                        title = "Date (Oldest first)",
//                        selected = sortOrder == SortOrder.DATE_ASC,
//                        onClick = {
//                            viewModel.setSortOrder(SortOrder.DATE_ASC)
//                            showSortDialog = false
//                        }
//                    )
//                    SortOption(
//                        title = "Organisation Unit (A-Z)",
//                        selected = sortOrder == SortOrder.ORG_UNIT_ASC,
//                        onClick = {
//                            viewModel.setSortOrder(SortOrder.ORG_UNIT_ASC)
//                            showSortDialog = false
//                        }
//                    )
//                    SortOption(
//                        title = "Organisation Unit (Z-A)",
//                        selected = sortOrder == SortOrder.ORG_UNIT_DESC,
//                        onClick = {
//                            viewModel.setSortOrder(SortOrder.ORG_UNIT_DESC)
//                            showSortDialog = false
//                        }
//                    )
//                }
//            },
//            confirmButton = {
//                TextButton(onClick = { showSortDialog = false }) {
//                    Text("Cancel")
//                }
//            }
//        )
//    }
//
//    // Filter dialog
//    if (showFilterDialog) {
//        FilterDialog(
//            onDismiss = { showFilterDialog = false },
//            onApplyFilters = { orgUnit, period, state, syncState ->
//                viewModel.setOrgUnitFilter(orgUnit)
//                viewModel.setPeriodFilter(period)
//                viewModel.setStateFilter(state)
//                viewModel.setSyncStateFilter(syncState)
//                showFilterDialog = false
//            },
//            onClearFilters = {
//                viewModel.clearFilters()
//                showFilterDialog = false
//            }
//        )
//    }
//}
//
//@Composable
//fun DatasetInstanceList(
//    instances: List<DatasetInstance>,
//    onItemClick: (DatasetInstance) -> Unit
//) {
//    LazyColumn(
//        modifier = Modifier.fillMaxSize(),
//        contentPadding = PaddingValues(16.dp),
//        verticalArrangement = Arrangement.spacedBy(8.dp)
//    ) {
//        items(instances) { instance ->
//            DatasetInstanceItem(
//                instance = instance,
//                onClick = { onItemClick(instance) }
//            )
//        }
//    }
//}
//
//@Composable
//fun DatasetInstanceItem(
//    instance: DatasetInstance,
//    onClick: () -> Unit
//) {
//    val dateFormat = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
//
//    Card(
//        modifier = Modifier
//            .fillMaxWidth()
//            .clickable(onClick = onClick),
//        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
//    ) {
//        Column(
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(16.dp)
//        ) {
//            Row(
//                modifier = Modifier.fillMaxWidth(),
//                horizontalArrangement = Arrangement.SpaceBetween,
//                verticalAlignment = Alignment.CenterVertically
//            ) {
//                Text(
//                    text = instance.periodDisplayName ?: instance.periodId,
//                    style = MaterialTheme.typography.titleMedium
//                )
//                StatusChip(instance.state)
//            }
//
//            Spacer(modifier = Modifier.height(8.dp))
//
//            Text(
//                text = instance.organisationUnitDisplayName ?: "Unknown Organisation Unit",
//                style = MaterialTheme.typography.bodyMedium
//            )
//
//            Spacer(modifier = Modifier.height(4.dp))
//
//            Row(
//                modifier = Modifier.fillMaxWidth(),
//                horizontalArrangement = Arrangement.SpaceBetween,
//                verticalAlignment = Alignment.CenterVertically
//            ) {
//                Text(
//                    text = "Last updated: ${dateFormat.format(instance.lastUpdated)}",
//                    style = MaterialTheme.typography.bodySmall
//                )
//                SyncStateIcon(instance.syncState)
//            }
//        }
//    }
//}
//
//@Composable
//fun StatusChip(state: DatasetInstanceState) {
//    val (color, text) = when (state) {
//        DatasetInstanceState.OPEN -> Pair(MaterialTheme.colorScheme.primary, "Open")
//        DatasetInstanceState.COMPLETED -> Pair(MaterialTheme.colorScheme.tertiary, "Completed")
//        DatasetInstanceState.APPROVED -> Pair(MaterialTheme.colorScheme.secondary, "Approved")
//        DatasetInstanceState.LOCKED -> Pair(MaterialTheme.colorScheme.error, "Locked")
//    }
//
//    Surface(
//        color = color.copy(alpha = 0.2f),
//        shape = MaterialTheme.shapes.small
//    ) {
//        Text(
//            text = text,
//            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
//            color = color,
//            style = MaterialTheme.typography.labelSmall
//        )
//    }
//}
//
//@Composable
//fun SyncStateIcon(syncState: SyncState) {
//    val (icon, tint, contentDescription) = when (syncState) {
//        SyncState.SYNCED -> Triple(
//            Icons.Default.CloudDone,
//            MaterialTheme.colorScheme.primary,
//            "Synced"
//        )
//        SyncState.TO_UPDATE, SyncState.TO_POST -> Triple(
//            Icons.Default.CloudUpload,
//            MaterialTheme.colorScheme.tertiary,
//            "Pending sync"
//        )
//        SyncState.ERROR -> Triple(
//            Icons.Default.CloudOff,
//            MaterialTheme.colorScheme.error,
//            "Sync error"
//        )
//        SyncState.WARNING -> Triple(
//            Icons.Default.Warning,
//            MaterialTheme.colorScheme.error,
//            "Sync warning"
//        )
//    }
//
//    Icon(
//        imageVector = icon,
//        contentDescription = contentDescription,
//        tint = tint
//    )
//}
//
//@Composable
//fun SortOption(
//    title: String,
//    selected: Boolean,
//    onClick: () -> Unit
//) {
//    Row(
//        modifier = Modifier
//            .fillMaxWidth()
//            .clickable(onClick = onClick)
//            .padding(vertical = 12.dp),
//        verticalAlignment = Alignment.CenterVertically
//    ) {
//        RadioButton(
//            selected = selected,
//            onClick = onClick
//        )
//        Spacer(modifier = Modifier.width(8.dp))
//        Text(text = title)
//    }
//}
//
//@Composable
//fun FilterDialog(
//    onDismiss: () -> Unit,
//    onApplyFilters: (String?, String?, DatasetInstanceState?, SyncState?) -> Unit,
//    onClearFilters: () -> Unit
//) {
//    var selectedOrgUnit by remember { mutableStateOf<String?>(null) }
//    var selectedPeriod by remember { mutableStateOf<String?>(null) }
//    var selectedState by remember { mutableStateOf<DatasetInstanceState?>(null) }
//    var selectedSyncState by remember { mutableStateOf<SyncState?>(null) }
//
//    AlertDialog(
//        onDismissRequest = onDismiss,
//        title = { Text("Filter Instances") },
//        text = {
//            Column {
//                Text("Organisation Unit", style = MaterialTheme.typography.titleSmall)
//                // In a real app, this would be a dropdown with org units
//                OutlinedTextField(
//                    value = selectedOrgUnit ?: "",
//                    onValueChange = { selectedOrgUnit = it.takeIf { it.isNotEmpty() } },
//                    modifier = Modifier.fillMaxWidth(),
//                    placeholder = { Text("Select Organisation Unit") }
//                )
//
//                Spacer(modifier = Modifier.height(8.dp))
//
//                Text("Period", style = MaterialTheme.typography.titleSmall)
//                // In a real app, this would be a dropdown with periods
//                OutlinedTextField(
//                    value = selectedPeriod ?: "",
//                    onValueChange = { selectedPeriod = it.takeIf { it.isNotEmpty() } },
//                    modifier = Modifier.fillMaxWidth(),
//                    placeholder = { Text("Select Period") }
//                )
//
//                Spacer(modifier = Modifier.height(8.dp))
//
//                Text("Status", style = MaterialTheme.typography.titleSmall)
//                // Status options
//                Row(
//                    modifier = Modifier.fillMaxWidth(),
//                    horizontalArrangement = Arrangement.spacedBy(8.dp)
//                ) {
//                    FilterChip(
//                        selected = selectedState == DatasetInstanceState.OPEN,
//                        onClick = {
//                            selectedState = if (selectedState == DatasetInstanceState.OPEN) null
//                            else DatasetInstanceState.OPEN
//                        },
//                        label = { Text("Open") }
//                    )
//                    FilterChip(
//                        selected = selectedState == DatasetInstanceState.COMPLETED,
//                        onClick = {
//                            selectedState = if (selectedState == DatasetInstanceState.COMPLETED) null
//                            else DatasetInstanceState.COMPLETED
//                        },
//                        label = { Text("Completed") }
//                    )
//                }
//
//                Spacer(modifier = Modifier.height(8.dp))
//
//                Text("Sync Status", style = MaterialTheme.typography.titleSmall)
//                // Sync status options
//                Row(
//                    modifier = Modifier.fillMaxWidth(),
//                    horizontalArrangement = Arrangement.spacedBy(8.dp)
//                ) {
//                    FilterChip(
//                        selected = selectedSyncState == SyncState.SYNCED,
//                        onClick = {
//                            selectedSyncState = if (selectedSyncState == SyncState.SYNCED) null
//                            else SyncState.SYNCED
//                        },
//                        label = { Text("Synced") }
//                    )
//                    FilterChip(
//                        selected = selectedSyncState == SyncState.TO_UPDATE,
//                        onClick = {
//                            selectedSyncState = if (selectedSyncState == SyncState.TO_UPDATE) null
//                            else SyncState.TO_UPDATE
//                        },
//                        label = { Text("To Sync") }
//                    )
//                }
//            }
//        },
//        confirmButton = {
//            TextButton(
//                onClick = {
//                    onApplyFilters(selectedOrgUnit, selectedPeriod, selectedState, selectedSyncState)
//                }
//            ) {
//                Text("Apply")
//            }
//        },
//        dismissButton = {
//            Row {
//                TextButton(onClick = onClearFilters) {
//                    Text("Clear All")
//                }
//                TextButton(onClick = onDismiss) {
//                    Text("Cancel")
//                }
//            }
//        }
//    )
//}