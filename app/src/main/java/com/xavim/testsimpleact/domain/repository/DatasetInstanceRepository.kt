package com.xavim.testsimpleact.domain.repository

import com.xavim.testsimpleact.domain.model.DatasetInstance
import com.xavim.testsimpleact.domain.model.DatasetInstanceState
import com.xavim.testsimpleact.domain.model.OrganisationUnit
import com.xavim.testsimpleact.domain.model.Period
import com.xavim.testsimpleact.domain.model.SyncState
import kotlinx.coroutines.flow.Flow
import java.util.Date


data class DatasetMetadata(
    val datasetId: String,
    val name: String,
    val description: String?,
    val periodType: String,
    val canCreateNew: Boolean,
    val lastSync: Date?
)

interface DatasetInstanceRepository {
    /**
     * Gets dataset instances based on provided filters
     */
    fun getDatasetInstances(
        datasetId: String,
        orgUnitId: String? = null,
        periodId: String? = null,
        state: DatasetInstanceState? = null,
        syncState: SyncState? = null
    ): Flow<List<DatasetInstance>>

    /**
     * Gets metadata for a dataset
     */
    fun getDatasetMetadata(datasetId: String): Flow<DatasetMetadata>

    /**
     * Checks if a dataset instance is editable
     */
    suspend fun isDatasetInstanceEditable(
        datasetId: String,
        periodId: String,
        orgUnitId: String,
        attributeOptionComboId: String
    ): Boolean

    /**
     * Completes a dataset instance
     */
    suspend fun completeDatasetInstance(
        datasetId: String,
        periodId: String,
        orgUnitId: String,
        attributeOptionComboId: String,
        completedBy: String? = null
    ): Flow<Result<Unit>>

    /**
     * Reopens a dataset instance (removes completion)
     */
    suspend fun reopenDatasetInstance(
        datasetId: String,
        periodId: String,
        orgUnitId: String,
        attributeOptionComboId: String
    ): Flow<Result<Unit>>

    /**
     * Syncs a dataset instance with the server
     */
    suspend fun syncDatasetInstance(
        datasetId: String,
        periodId: String,
        orgUnitId: String,
        attributeOptionComboId: String
    ): Flow<Result<Unit>>

    /**
     * Checks if user has permission to complete a dataset
     */
    suspend fun hasCompletePermission(datasetId: String): Boolean

    /**
     * Gets available periods for a dataset
     */
    fun getAvailablePeriods(datasetId: String): Flow<List<Period>>

    /**
     * Gets available organization units for a dataset
     */
    fun getAvailableOrgUnits(datasetId: String): Flow<List<OrganisationUnit>>
}