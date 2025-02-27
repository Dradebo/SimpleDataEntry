package com.xavim.testsimpleact.data.repositoryImpl

import com.xavim.testsimpleact.domain.model.DatasetInstance
import com.xavim.testsimpleact.domain.repository.DatasetInstanceRepository
import com.xavim.testsimpleact.domain.repository.DatasetMetadata
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.dataset.DataSet
import org.hisp.dhis.android.core.dataset.DataSetInstance
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit
import javax.inject.Inject

class DatasetInstanceRepositoryImpl @Inject constructor(
    private val d2: D2,
    private val dispatcher: CoroutineDispatcher
) : DatasetInstanceRepository {

    override suspend fun getDatasetInstances(datasetId: String): Flow<List<DatasetInstance>> = flow {
        withContext(dispatcher) {
            try {
                val userOrgUnits = d2.organisationUnitModule().organisationUnits()
                    .byOrganisationUnitScope(OrganisationUnit.Scope.SCOPE_DATA_CAPTURE)
                    .blockingGet()

                val instances = mutableListOf<DatasetInstance>()

                for (orgUnit in userOrgUnits) {
                    val orgUnitInstances = d2.dataSetModule().dataSetInstances()
                        .byDataSetUid().eq(datasetId)
                        .byOrganisationUnitUid().eq(orgUnit.uid())
                        .blockingGet()
                        .map { it.toDomainModel() }

                    instances.addAll(orgUnitInstances)
                }

                emit(instances)
            } catch (e: Exception) {
                throw DatasetRepositoryException("Failed to fetch dataset instances", e)
            }
        }
    }

    override suspend fun getDatasetMetadata(datasetId: String): Flow<DatasetMetadata> = flow {
        withContext(dispatcher) {
            try {
                val dataset = d2.dataSetModule().dataSets()
                    .uid(datasetId)
                    .blockingGet()

                val metadata = DatasetMetadata(
                    datasetId = dataset!!.uid(),
                    name = dataset.displayName() ?: dataset.name() ?: "",
                    description = dataset.description(),
                    periodType = dataset.periodType()?.name ?: "",
                    canCreateNew = checkCanCreateNew(dataset),
                    lastSync = dataset.lastUpdated()
                )

                emit(metadata)
            } catch (e: Exception) {
                throw DatasetRepositoryException("Failed to fetch dataset metadata", e)
            }
        }
    }



//    override suspend fun syncInstance(instanceId: String) {
//        withContext(dispatcher) {
//            try {
//                // Implementation depends on the D2 SDK capabilities
//                d2.dataSetModule().dataSetInstances()
//                    .byDataSetUid().eq(instanceId)
//                    .blockingSync()
//
//                d2.dataSetModule().dataSetInstances()
//                    .byDataSetUid().eq("dataSetUid")
//                    .byPeriod().eq("period")
//                    .byOrganisationUnitUid().eq("orgUnitUid")
//                    .upload()
//                    .subscribe(
//                        {},
//                        { error -> }
//                    )
//            } catch (e: Exception) {
//                throw DatasetRepositoryException("Failed to sync dataset instance", e)
//            }
//        }
//    }

    private fun checkCanCreateNew(dataset: DataSet?): Boolean {
        // Logic to determine if a new instance can be created
        // This could check permissions, expiration dates, etc.
        return true
    }
}

fun DataSetInstance.toDomainModel(): DatasetInstance {
    return DatasetInstance(
        dataSetUid = dataSetUid(),
        periodId = period(),
        organisationUnitUid = organisationUnitUid(),
        attributeOptionComboUid = attributeOptionComboUid(),
        state = completed(),
        lastUpdated = lastUpdated().toString(),
        instanceUid = id()
    )
}

class DatasetRepositoryException(message: String, cause: Throwable? = null) : Exception(message, cause)