package com.xavim.testsimpleact.domain.useCase

import com.xavim.testsimpleact.domain.model.DataEntrySection
import com.xavim.testsimpleact.domain.repository.DataEntryRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetDataEntryFormUseCase @Inject constructor(
    private val repository: DataEntryRepository
) {
    operator fun invoke(datasetId: String): Flow<List<DataEntrySection>> =
        repository.getDataEntryForm(datasetId)
}