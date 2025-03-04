package com.xavim.testsimpleact.domain.useCase

import com.xavim.testsimpleact.domain.repository.DatasetInstanceRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SyncDatasetInstanceUseCase @Inject constructor(
    private val repository: DatasetInstanceRepository
) {
    suspend operator fun invoke(
        datasetId: String,
        periodId: String,
        orgUnitId: String,
        attributeOptionComboId: String
    ): Flow<Result<Unit>> = repository.syncDatasetInstance(
        datasetId, periodId, orgUnitId, attributeOptionComboId
    )
}