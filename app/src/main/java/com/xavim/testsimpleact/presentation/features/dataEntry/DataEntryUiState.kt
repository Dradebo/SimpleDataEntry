package com.xavim.testsimpleact.presentation.features.dataEntry

import com.xavim.testsimpleact.domain.model.DataEntrySection
sealed class DataEntryState {
    object Loading : DataEntryState()
    data class Success(val sections: List<DataEntrySection>) : DataEntryState()
    data class Error(val message: String) : DataEntryState()
}