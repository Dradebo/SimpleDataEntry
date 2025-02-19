package com.xavim.testsimpleact.domain.useCase

import com.xavim.testsimpleact.domain.repository.DataEntryRepository
import kotlinx.coroutines.flow.Flow

class CreateNewEntryUseCase(
    private val repository: DataEntryRepository
) {
//    operator fun invoke(
//        datasetId: String,
//        periodId: String,
//        orgUnitId: String
//    ): Flow<String> {
//        return repository.createDataEntry(datasetId, periodId, orgUnitId)
//    }
}