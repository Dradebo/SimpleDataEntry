package com.xavim.testsimpleact.data.repositoryImpl

import com.xavim.testsimpleact.domain.model.DatasetInstance
import com.xavim.testsimpleact.domain.repository.DatasetInstanceRepository
import com.xavim.testsimpleact.domain.repository.DatasetMetadata
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.dataset.DataSet
import org.hisp.dhis.android.core.dataset.DataSetInstance
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit
import javax.inject.Inject

//class DatasetInstanceRepositoryImpl(
//    private val d2: D2
//) : DatasetInstanceRepository {
//
//    /**
//     * Retrieves all “instances” of a particular dataset from DHIS2.
//     * For demonstration, we interpret an instance as each unique (dataset, period, orgUnit, attributeOptionCombo).
//     */
//    override suspend fun getDatasetInstances(datasetId: String): Flow<List<DatasetInstance>> = flow {
//
////        val instances = d2.dataSetModule().dataSetInstances().byDataSetUid().eq(datasetId)
////            .blockingGet().map { it.toDomainModel() }
//
//
////      Prospective solution
//        val datasetUid = datasetId
//        val userOrgUnits = d2.organisationUnitModule().organisationUnits()
//            .byOrganisationUnitScope(OrganisationUnit.Scope.SCOPE_DATA_CAPTURE)
//            .blockingGet()
//        val userOrgUnitUid = userOrgUnits.firstOrNull()?.uid()
//
//        val instances = d2.dataSetModule().dataSetInstances()
//            .byDataSetUid().eq(datasetUid)
//            .byOrganisationUnitUid().eq(userOrgUnitUid)
//            .blockingGet().map { it.toDomainModel() }
//
//
//        emit(instances)
//    }
//}
//
//fun DataSetInstance.toDomainModel(): DatasetInstance {
//    return DatasetInstance(
//        dataSetUid = dataSetUid(),
//        periodId = period(),
//        organisationUnitUid = organisationUnitUid(),
//        attributeOptionComboUid = attributeOptionComboUid(),
//        state = completed(),
//        instanceUid = id(),
//        lastUpdated = lastUpdated().toString(),
//    )
//}

class DatasetInstanceRepositoryImpl @Inject constructor(
    private val d2: D2,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : DatasetInstanceRepository {

    override suspend fun getDatasetInstances(datasetId: String): Flow<List<DatasetInstance>> = flow {
        withContext(dispatcher) {
            try {
                val userOrgUnits = d2.organisationUnitModule().organisationUnits()
                    .byOrganisationUnitScope(OrganisationUnit.Scope.SCOPE_DATA_CAPTURE)
                    .blockingGet()

                val instances = userOrgUnits.flatMap { orgUnit ->
                    d2.dataSetModule().dataSetInstances()
                        .byDataSetUid().eq(datasetId)
                        .byOrganisationUnitUid().eq(orgUnit.uid())
                        .blockingGet()
                }.map { it.toDomainModel() }

                emit(instances)
            } catch (e: Exception) {
                throw DatasetInstanceRepositoryException(
                    "Failed to fetch dataset instances",
                    e
                )
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
                throw DatasetInstanceRepositoryException(
                    "Failed to fetch dataset metadata",
                    e
                )
            }
        }
    }

//    override suspend fun deleteInstance(instanceId: String) {
//        withContext(dispatcher) {
//            try {
//                d2.dataSetModule().dataSetInstances()
//                    .uid(instanceId)
//                    .blockingDelete()
//            } catch (e: Exception) {
//                throw DatasetInstanceRepositoryException(
//                    "Failed to delete dataset instance",
//                    e
//                )
//            }
//        }
//    }

//    override suspend fun syncInstance(instanceId: String) {
//        withContext(dispatcher) {
//            try {
//                d2.dataSetModule().dataSetInstances()
//                    .uid(instanceId)
//                    .blockingSync()
//            } catch (e: Exception) {
//                throw DatasetInstanceRepositoryException(
//                    "Failed to sync dataset instance",
//                    e
//                )
//            }
//        }
//    }

    private fun checkCanCreateNew(dataset: DataSet?): Boolean {
        return try {
            // Add your business logic here
            // For example: check user permissions, dataset configuration, etc.
            true
        } catch (e: Exception) {
            false
        }
    }

}

fun DataSetInstance.toDomainModel(): DatasetInstance {
    return DatasetInstance(
        dataSetUid = dataSetUid(),
        periodId = period(),
        organisationUnitUid = organisationUnitUid(),
        attributeOptionComboUid = attributeOptionComboUid(),
        state = completed(),
        instanceUid = id(),
        lastUpdated = lastUpdated().toString(),
    )
}

class DatasetInstanceRepositoryException(
    message: String,
    cause: Throwable? = null
) : Exception(message, cause)