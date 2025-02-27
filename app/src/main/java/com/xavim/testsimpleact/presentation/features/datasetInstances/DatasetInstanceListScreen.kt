@file:OptIn(ExperimentalMaterial3Api::class)

package com.xavim.testsimpleact.presentation.features.datasetInstances
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import com.xavim.testsimpleact.domain.model.DatasetInstance
import org.hisp.dhis.mobile.ui.designsystem.component.AdditionalInfoItem
import org.hisp.dhis.mobile.ui.designsystem.component.ListCard
import org.hisp.dhis.mobile.ui.designsystem.component.ListCardTitleModel
import org.hisp.dhis.mobile.ui.designsystem.component.state.rememberAdditionalInfoColumnState
import org.hisp.dhis.mobile.ui.designsystem.component.state.rememberListCardState
import java.text.SimpleDateFormat
import java.util.Locale
import androidx.compose.material3.Text as Text

@Composable
fun DatasetInstanceListScreen(
    viewModel: DatasetInstanceListViewModel = hiltViewModel(),
    onNewEntryClick: (String) -> Unit,
    onEntryClick: (String, String, String, String) -> Unit,
    onNavigateBack: () -> Unit,
    datasetId: String
) {
    val state by viewModel.state.collectAsState()
    val sortOrder by viewModel.sortOrder.collectAsState()
    val refreshing by viewModel.isRefreshing.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Data Entries") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.updateSortOrder(getSortOrderToggle(sortOrder)) }) {
                        Icon(
                            imageVector = when(sortOrder) {
                                DatasetInstanceListViewModel.SortOrder.DATE_DESC -> Icons.Default.ArrowDownward
                                DatasetInstanceListViewModel.SortOrder.DATE_ASC -> Icons.Default.ArrowUpward
                                DatasetInstanceListViewModel.SortOrder.COMPLETION_STATUS -> Icons.Default.Done
                            },
                            contentDescription = "Sort"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            if ((state as? DatasetInstanceListState.Success)?.canCreateNew == true) {
                FloatingActionButton(onClick = { onNewEntryClick(datasetId) }) {
                    Icon(Icons.Default.Add, contentDescription = "New Entry")
                }
            }
        }
    ) { paddingValues ->
        SwipeRefresh(
            state = rememberSwipeRefreshState(refreshing),
            onRefresh = { viewModel.refresh() },
            modifier = Modifier.padding(paddingValues)
        ) {
            when (val currentState = state) {
                is DatasetInstanceListState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                is DatasetInstanceListState.Success -> {
                    val entries = currentState.entries
                    if (entries.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Text("No data entries found")
                                Button(onClick = { viewModel.refresh() }) {
                                    Text("Refresh")
                                }
                            }
                        }
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp)
                        ) {
                            items(entries.size) { instance ->
                                InstanceListItem(
                                    instance = instance,
                                    onClick = {
                                        onEntryClick(
                                            datasetId,
                                            instance.periodId,
                                            instance.organisationUnitUid,
                                            instance.attributeOptionComboUid
                                        )
                                    },
                                    onSyncClick = {
                                        viewModel.syncInstance(instance.instanceUid.toString())
                                    }
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }
                    }
                }

                is DatasetInstanceListState.Error -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                text = currentState.message,
                                color = Color.Red
                            )
                            Button(onClick = { viewModel.refresh() }) {
                                Text("Retry")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun InstanceListItem(
    instance: DatasetInstance,
    onClick: () -> Unit,
    onSyncClick: () -> Unit
) {
    ListCard(
        listCardState = rememberListCardState(
            title = ListCardTitleModel(
                text = instance.periodId,
                style = MaterialTheme.typography.titleMedium
            ),
            additionalInfoColumnState = rememberAdditionalInfoColumnState(
                additionalInfoList = listOf(
                    AdditionalInfoItem(value = "Organization Unit: ${instance.organisationUnitUid}"),
                    AdditionalInfoItem(
                        value = "Status: ${if (instance.state) "Completed" else "In Progress"}",
                        style = TextStyle(
                            color = if (instance.state)
                                Color.Cyan
                            else
                                Color.Gray
                        )
                    ),
                    AdditionalInfoItem(value = "Last Updated: ${formatDate(instance.lastUpdated)}")
                ),
                syncProgressItem = TODO(),
                expandLabelText = TODO(),
                shrinkLabelText = TODO(),
                minItemsToShow = TODO(),
                scrollableContent = TODO()
            )
        ),
        onCardClick = onClick
    )
}

private fun formatDate(dateString: String): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.US)
        val outputFormat = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.US)
        val date = inputFormat.parse(dateString)
        outputFormat.format(date)
    } catch (e: Exception) {
        dateString
    }
}

private fun getSortOrderToggle(current: DatasetInstanceListViewModel.SortOrder): DatasetInstanceListViewModel.SortOrder {
    return when (current) {
        DatasetInstanceListViewModel.SortOrder.DATE_DESC -> DatasetInstanceListViewModel.SortOrder.DATE_ASC
        DatasetInstanceListViewModel.SortOrder.DATE_ASC -> DatasetInstanceListViewModel.SortOrder.COMPLETION_STATUS
        DatasetInstanceListViewModel.SortOrder.COMPLETION_STATUS -> DatasetInstanceListViewModel.SortOrder.DATE_DESC
    }
}