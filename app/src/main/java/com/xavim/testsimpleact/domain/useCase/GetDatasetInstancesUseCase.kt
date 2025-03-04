package com.xavim.testsimpleact.domain.useCase

import com.xavim.testsimpleact.domain.model.DatasetInstance
import com.xavim.testsimpleact.domain.model.DatasetInstanceState
import com.xavim.testsimpleact.domain.model.SyncState
import com.xavim.testsimpleact.domain.repository.DatasetInstanceRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject


class GetDatasetInstancesUseCase @Inject constructor(
    private val repository: DatasetInstanceRepository
) {
    suspend operator fun invoke(
        datasetId: String,
        orgUnitId: String? = null,
        periodId: String? = null,
        state: DatasetInstanceState? = null,
        syncState: SyncState? = null
    ): Flow<List<DatasetInstance>> = repository.getDatasetInstances(
        datasetId, orgUnitId, periodId, state, syncState
    )
}