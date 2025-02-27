package com.xavim.testsimpleact.domain.useCase

import com.xavim.testsimpleact.domain.repository.DatasetInstanceRepository
import javax.inject.Inject

class SyncDatasetInstanceUseCase @Inject constructor(
    private val repository: DatasetInstanceRepository
) {
    suspend operator fun invoke(instanceId: String) =
        repository.syncInstance(instanceId)
}