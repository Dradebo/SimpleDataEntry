package com.xavim.testsimpleact.domain.useCase

import com.xavim.testsimpleact.domain.model.Dataset
import com.xavim.testsimpleact.domain.repository.DatasetRepository
import kotlinx.coroutines.flow.Flow

class GetDataSetsUseCase(
    private val repository: DatasetRepository
) {
    /**
     * Invoke function for fetching datasets as a flow.
     */
    suspend operator fun invoke(): Flow<List<Dataset>> {
        return repository.getDatasets()
    }
}