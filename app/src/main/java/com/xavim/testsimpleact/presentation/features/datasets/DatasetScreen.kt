package com.xavim.testsimpleact.presentation.features.datasets

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import org.hisp.dhis.mobile.ui.designsystem.component.AdditionalInfoItem
import org.hisp.dhis.mobile.ui.designsystem.component.ListCard
import org.hisp.dhis.mobile.ui.designsystem.component.ListCardTitleModel
import org.hisp.dhis.mobile.ui.designsystem.component.state.rememberAdditionalInfoColumnState
import org.hisp.dhis.mobile.ui.designsystem.component.state.rememberListCardState

@Composable
fun DatasetScreen(
    onDatasetClick: (Any?) -> Unit,
    onLogout: () -> Unit
) {
    val viewModel: DatasetGridViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsState()

    when (val currentState = uiState) {
        is DatasetScreenState.Loading -> {
            CircularProgressIndicator()
        }
        is DatasetScreenState.Success -> {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(currentState.datasets.size) { index ->
                    val dataset = currentState.datasets[index]
                    val additionalInfoList = listOf(
                        AdditionalInfoItem(value = "Period Type: ${dataset.periodType}"),
                        AdditionalInfoItem(value = "Category Combo: ${dataset.categoryCombo}")
                    )
                    val listCardState = rememberListCardState(
                        title = ListCardTitleModel(text = dataset.displayName),
                        additionalInfoColumnState = rememberAdditionalInfoColumnState(
                            additionalInfoList = additionalInfoList,
                            syncProgressItem = AdditionalInfoItem(value = "sync progress")
                        )
                    )
                    ListCard(
                        listCardState = listCardState,
                        onCardClick = { onDatasetClick(dataset.uid) }
                    )
                }
            }
        }
        is DatasetScreenState.Error -> {
            androidx.compose.material3.Text("Error: ${currentState.message}")
        }
    }
}