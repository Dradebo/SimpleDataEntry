package com.xavim.testsimpleact.domain.useCase

import com.xavim.testsimpleact.domain.repository.DatasetInstanceRepository
import com.xavim.testsimpleact.domain.repository.DatasetMetadata
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetDatasetMetadataUseCase @Inject constructor(
    private val repository: DatasetInstanceRepository
) {
    suspend operator fun invoke(datasetId: String): Flow<DatasetMetadata> =
        repository.getDatasetMetadata(datasetId)
}