@file:OptIn(ExperimentalMaterial3Api::class)

package com.xavim.testsimpleact.presentation.features.datasetInstances
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material3.ExperimentalMaterial3Api
import org.hisp.dhis.mobile.ui.designsystem.component.AdditionalInfoItem
import org.hisp.dhis.mobile.ui.designsystem.component.ListCard
import org.hisp.dhis.mobile.ui.designsystem.component.ListCardTitleModel
import org.hisp.dhis.mobile.ui.designsystem.component.state.rememberAdditionalInfoColumnState
import org.hisp.dhis.mobile.ui.designsystem.component.state.rememberListCardState
import androidx.compose.material3.Text as Text

//@Composable
//fun DatasetInstanceListScreen(
//    viewModel: DatasetInstanceListViewModel = hiltViewModel(),
//    onNewEntryClick: () -> Unit,
//    onEntryClick: () -> Unit,
//    onNavigateBack: () -> Unit,
//    datasetId: String
//) {
//    val state by viewModel.state.collectAsState()
//    val datasetId = viewModel.datasetId
//
//    Scaffold(
//        topBar = {
//            TopAppBar(
//                title = { Text(text = "Data Entries") },
//                navigationIcon = {
//                    IconButton(onClick = onNavigateBack) {
//                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
//                    }
//                }
//            )
//        },
//        floatingActionButton = {
//            FloatingActionButton(onClick = { onNewEntryClick() }) {
//                Icon(Icons.Default.Add, contentDescription = "New Entry")
//            }
//        }
//    ) { paddingValues ->
//        when (val currentState = state) {
//
//            is DatasetInstanceListState.Loading -> {
//                /**
//                 * Instead of a custom LoadingIndicator(), use the built-in Compose CircularProgressIndicator().
//                 */
//                CircularProgressIndicator(modifier = Modifier.padding(paddingValues))
//            }
//
//            is DatasetInstanceListState.Success -> {
//                val entries = currentState.entries
//                /**
//                 * Instead of custom EmptyState(), display a simple Text
//                 * when there are no entries to show.
//                 */
//                if (entries.isEmpty()) {
//                    Text(
//                        text = "No entries found.",
//                        modifier = Modifier.padding(paddingValues).padding(16.dp)
//                    )
//                } else {
//                    /**
//                     * Replace DataEntryListLazyColumn() with a standard LazyColumn,
//                     * and use items() from LazyColumn to iterate over the list.
//                     */
//                    LazyColumn(
//                        modifier = Modifier.padding(paddingValues),
//                        contentPadding = PaddingValues(16.dp)
//                    ) {
//                        items(currentState.entries.size) { index ->
//                            val datasetInstance = currentState.entries[index]
//                            val additionalInfoList = listOf(
//                                AdditionalInfoItem(value = "Period Type: ${datasetInstance.dataSetUid}"),
//                                AdditionalInfoItem(value = "Category Combo: ${datasetInstance.organisationUnitUid}")
//                            )
//                            val listCardState = rememberListCardState(
//                                title = ListCardTitleModel(text = datasetInstance.periodId),
//                                additionalInfoColumnState = rememberAdditionalInfoColumnState(
//                                    additionalInfoList = additionalInfoList,
//                                    syncProgressItem = AdditionalInfoItem(value = "sync progress")
//                                )
//                            )
//                            ListCard(
//                                listCardState = listCardState,
//                                onCardClick = { onEntryClick(datasetInstance.periodId, datasetInstance.organisationUnitUid, datasetInstance.attributeOptionComboUid)
//                                }
//                            )
//                        }
//
//
////                        { entries ->
////                            /**
////                             * Instead of a custom DataEntryItem(),
////                             * we’ll just print each entry as text for clarity.
////                             * You can replace with your own Card or Row implementation if needed.
////                             */
////                            Text(
////                                text = "Period: ${entry.periodId}, OrgUnit: ${entry.organisationUnitUid}",
////                                modifier = Modifier
////                                    .padding(vertical = 8.dp)
////                                    .padding(horizontal = 4.dp)
////                            )
////                            /**
////                             * If you need a click, you can wrap the row in something clickable
////                             * or call a lambda:
////                             * onClick: { onEntryClick(datasetId, entry.dataSetUid) }
////                             */
////                        }
//                    }
//                }
//            }
//
//            /**
//             * The compiler complaint was about a missing "Error" branch,
//             * or that the 'when' was not exhaustive. So we do:
//             */
//            is DatasetInstanceListState.Error -> {
//                /**
//                 * Instead of ErrorMessage(), display a simple Text with the error message.
//                 */
//                Text(
//                    text = "Error: ${currentState.message}",
//                    modifier = Modifier.padding(paddingValues).padding(16.dp)
//                )
//            }
//        }
//    }
//}

@Composable
fun DatasetInstanceListScreen(
    viewModel: DatasetInstanceListViewModel = hiltViewModel(),
    onNewEntryClick: () -> Unit,
    onEntryClick: (String, String, String) -> Unit, // Updated signature
    onNavigateBack: () -> Unit,
    datasetId: String
) {
    // ... existing code ...

    LazyColumn(
        modifier = Modifier.padding(paddingValues),
        contentPadding = PaddingValues(16.dp)
    ) {
        items(currentState.entries) { datasetInstance ->
            val additionalInfoList = listOf(
                AdditionalInfoItem(value = "Period: ${datasetInstance.periodId}"),
                AdditionalInfoItem(value = "Organization Unit: ${datasetInstance.organisationUnitUid}"),
                AdditionalInfoItem(value = "Status: ${if (datasetInstance.state) "Completed" else "In Progress"}")
            )

            ListCard(
                listCardState = rememberListCardState(
                    title = ListCardTitleModel(text = datasetInstance.periodId),
                    additionalInfoColumnState = rememberAdditionalInfoColumnState(
                        additionalInfoList = additionalInfoList
                    )
                ),
                onCardClick = {
                    onEntryClick(
                        datasetInstance.periodId,
                        datasetInstance.organisationUnitUid,
                        datasetInstance.attributeOptionComboUid
                    )
                }
            )
        }
    }
}