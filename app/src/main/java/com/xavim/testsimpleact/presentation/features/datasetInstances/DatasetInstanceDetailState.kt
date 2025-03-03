package com.xavim.testsimpleact.presentation.features.datasetInstances

import com.xavim.testsimpleact.domain.model.DatasetInstance

sealed class DatasetInstanceDetailState {
    object Loading : DatasetInstanceDetailState()
    data class Success(
        val instance: DatasetInstance,
        val isEditable: Boolean,
        val hasCompletePermission: Boolean
    ) : DatasetInstanceDetailState()
    data class Error(val message: String) : DatasetInstanceDetailState()
}
