package com.xavim.testsimpleact.domain.useCase

import com.xavim.testsimpleact.domain.model.DataEntrySection
import com.xavim.testsimpleact.domain.repository.DataEntryRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

//class GetDataEntryFormUseCase(
//    private val repository: DataEntryRepository
//) {
//    operator fun invoke(dataSetUID: String): Flow<List<DataEntrySection>> {
//        return repository.getDataEntryForm(dataSetUID)
//    }
//}

class GetDataEntryFormUseCase @Inject constructor(
    private val repository: DataEntryRepository
) {
    suspend operator fun invoke(datasetId: String): Flow<List<DataEntrySection>> =
        repository.getDataEntryForm(datasetId)
}