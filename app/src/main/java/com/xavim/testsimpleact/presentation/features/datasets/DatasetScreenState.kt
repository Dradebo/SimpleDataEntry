package com.xavim.testsimpleact.presentation.features.datasets

import com.xavim.testsimpleact.domain.model.Dataset

sealed class DatasetScreenState {
    object Loading : DatasetScreenState()
    data class Success(val datasets: List<Dataset>) : DatasetScreenState()
    data class Error(val message: String) : DatasetScreenState()
}