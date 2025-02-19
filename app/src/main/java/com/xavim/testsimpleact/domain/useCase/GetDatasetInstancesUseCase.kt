package com.xavim.testsimpleact.domain.useCase

import com.xavim.testsimpleact.domain.model.DatasetInstance
import com.xavim.testsimpleact.domain.repository.DatasetInstanceRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject


//class GetDatasetInstancesUseCase @Inject constructor(
//    private val repository: DatasetInstanceRepository
//) {
//    /**
//     * Invoke function to get a flow of dataset instances for a dataset ID.
//     */
//    suspend operator fun invoke(datasetId: String): Flow<List<DatasetInstance>> {
//        return repository.getDatasetInstances(datasetId)
//    }
//}

class GetDatasetInstancesUseCase @Inject constructor(
    private val repository: DatasetInstanceRepository
) {
    suspend operator fun invoke(datasetId: String): Flow<List<DatasetInstance>> =
        repository.getDatasetInstances(datasetId)
}