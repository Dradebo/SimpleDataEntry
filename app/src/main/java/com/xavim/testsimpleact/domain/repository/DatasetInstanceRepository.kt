package com.xavim.testsimpleact.domain.repository

import com.xavim.testsimpleact.domain.model.DatasetInstance
import kotlinx.coroutines.flow.Flow
import java.util.Date


//interface DatasetInstanceRepository {
//    /**
//     * Returns a flow of all dataset instances for a given dataset ID.
//     */
//    suspend fun getDatasetInstances(datasetId: String): Flow<List<DatasetInstance>>
//}

interface DatasetInstanceRepository {
    suspend fun getDatasetInstances(datasetId: String): Flow<List<DatasetInstance>>
    suspend fun getDatasetMetadata(datasetId: String): Flow<DatasetMetadata>
//    suspend fun deleteInstance(instanceId: String)
//    suspend fun syncInstance(instanceId: String)
}

data class DatasetMetadata(
    val datasetId: String,
    val name: String,
    val description: String?,
    val periodType: String,
    val canCreateNew: Boolean,
    val lastSync: Date?
)