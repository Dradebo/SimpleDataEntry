package com.xavim.testsimpleact.data.repositoryImpl

import android.util.Log
import com.xavim.testsimpleact.domain.model.DatasetInstance
import com.xavim.testsimpleact.domain.model.DatasetInstanceState
import com.xavim.testsimpleact.domain.model.OrganisationUnit
import com.xavim.testsimpleact.domain.model.Period
import com.xavim.testsimpleact.domain.model.SyncState
import com.xavim.testsimpleact.domain.repository.DatasetInstanceRepository
import com.xavim.testsimpleact.domain.repository.DatasetMetadata
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.arch.repositories.scope.RepositoryScope
import org.hisp.dhis.android.core.dataset.DataSetInstance
import org.hisp.dhis.android.core.maintenance.D2Error
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

class DatasetInstanceRepositoryImpl @Inject constructor(
    private val d2: D2,
) : DatasetInstanceRepository {
    override fun getDatasetInstances(
        datasetId: String,
        orgUnitId: String?,
        periodId: String?,
        state: DatasetInstanceState?,
        syncState: SyncState?
    ): Flow<List<DatasetInstance>> {
        flow() {
            try {
                val dataSetInstanceRepository = d2.dataSetModule().dataSetInstances()
                    .byDataSetUid().eq(datasetId)

                // Apply filters if provided
                val filteredRepo = dataSetInstanceRepository.apply {
                    orgUnitId?.let { byOrganisationUnitUid().eq(it) }
                    periodId?.let { byPeriod().eq(it) }
                    state?.let {
                        when (it) {
                            DatasetInstanceState.COMPLETED -> byCompleted().isTrue
                            DatasetInstanceState.OPEN -> byCompleted().isFalse
                            else -> this // No filter for APPROVED or LOCKED yet
                        }
                    }
                    // No direct mapping for SyncState in SDK
                }

                // Get instances and map to domain model
                val instances = filteredRepo
                    .orderByLastUpdated(RepositoryScope.OrderByDirection.DESC)
                    .blockingGet()
                    .map { it.toDomainModel() }

                emit(instances)
            } catch (e: Exception) {
                Log.e("Error fetching dataset instances", e.toString())
                emit(emptyList())
            }
        }
        return flow
    }

    private fun DataSetInstance.toDomainModel(): DatasetInstance {
        val state = if (completed() == true) {
            DatasetInstanceState.COMPLETED
        } else {
            DatasetInstanceState.OPEN
        }



        // Get completion details if completed
        val completionDetails = if (state == DatasetInstanceState.COMPLETED) {
            try {
                d2.dataSetModule().dataSetCompleteRegistrations()
                    .byDataSetUid().eq(dataSetUid())
                    .byPeriod().eq(period())
                    .byOrganisationUnitUid().eq(organisationUnitUid())
                    .byAttributeOptionComboUid().eq(attributeOptionComboUid())
                    .one().blockingGet()
            } catch (e: Exception) {
                null
            }
        } else null

        return DatasetInstance(
            instanceUid = "${dataSetUid()}_${period()}_${organisationUnitUid()}_${attributeOptionComboUid()}",
            datasetId = dataSetUid() ?: "",
            periodId = period() ?: "",
            organisationUnitUid = organisationUnitUid() ?: "",
            attributeOptionComboUid = attributeOptionComboUid() ?: "",
            state = state,
            lastUpdated = lastUpdated() ?: Date(),
            completedBy = completionDetails?.storedBy(),
            completedDate = completionDetails?.date(),
            valueCount = valueCount() ?: 0
        )
    }

    override fun getDatasetMetadata(datasetId: String): Flow<DatasetMetadata> = flow {
        try {
            val dataset = d2.dataSetModule().dataSets()
                .uid(datasetId)
                .blockingGet()

            if (dataset != null) {
                val metadata = DatasetMetadata(
                    datasetId = dataset.uid(),
                    name = dataset.displayName() ?: dataset.name() ?: "",
                    description = dataset.description(),
                    periodType = dataset.periodType()?.name ?: "",
                    canCreateNew = true, // Default to true, could be determined by permissions
                    lastSync = dataset.lastUpdated()
                )
                emit(metadata)
            } else {
                throw Exception("Dataset not found")
            }
        } catch (e: Exception) {
            Log.e("Error fetching dataset metadata", e.toString())
            throw e
        }
    }.flowOn(dispatcher)

    override suspend fun isDatasetInstanceEditable(
        datasetId: String,
        periodId: String,
        orgUnitId: String,
        attributeOptionComboId: String
    ): Boolean = withContext(dispatcher) {
        try {
            val isCompleted = d2.dataSetModule().dataSetCompleteRegistrations()
                .byDataSetUid().eq(datasetId)
                .byPeriod().eq(periodId)
                .byOrganisationUnitUid().eq(orgUnitId)
                .byAttributeOptionComboUid().eq(attributeOptionComboId)
                .one().blockingExists()

            // Check if period is open
            val period = d2.periodModule().periods()
                .byPeriodId().eq(periodId)
                .one().blockingGet()

            val isPeriodOpen = period?.endDate()?.after(Date()) ?: true

            // Check if user has write permission
            val hasWritePermission = d2.dataSetModule().dataSets()
                .uid(datasetId)
                .blockingGet()
                ?.access()
                ?.data()
                ?.write() ?: false

            // Editable if not completed, period is open, and user has write permission
            !isCompleted && isPeriodOpen && hasWritePermission
        } catch (e: Exception) {
            Log.e("Error checking if dataset instance is editable", e.toString())
            false
        }
    }

    override suspend fun completeDatasetInstance(
        datasetId: String,
        periodId: String,
        orgUnitId: String,
        attributeOptionComboId: String,
        completedBy: String?
    ): Flow<Result<Unit>> = flow {
        try {

            // Complete the dataset
            d2.dataSetModule().dataSetCompleteRegistrations()
                .value(
                    periodId,
                    orgUnitId,
                    datasetId,
                    attributeOptionComboId
                )
                .blockingSet()

            emit(Result.success(Unit))
        } catch (e: D2Error) {
            Log.e("Error completing dataset instance", e.toString())
            emit(Result.failure(Exception("Failed to complete: ${e.errorDescription()}")))
        } catch (e: Exception) {
            Log.e("Error completing dataset instance", e.toString())
            emit(Result.failure(Exception("Failed to complete: ${e.message}")))
        }
    }.flowOn(dispatcher)



    override suspend fun reopenDatasetInstance(
        datasetId: String,
        periodId: String,
        orgUnitId: String,
        attributeOptionComboId: String
    ): Flow<Result<Unit>> = flow {
        try {
            // Delete the complete registration
            d2.dataSetModule().dataSetCompleteRegistrations()
                .byDataSetUid().eq(datasetId)
                .byPeriod().eq(periodId)
                .byOrganisationUnitUid().eq(orgUnitId)
                .byAttributeOptionComboUid().eq(attributeOptionComboId)
                .blockingUpload()

            emit(Result.success(Unit))
        } catch (e: D2Error) {
            Log.e("Error reopening dataset instance", e.toString())
            emit(Result.failure(Exception("Failed to reopen: ${e.errorDescription()}")))
        } catch (e: Exception) {
            Log.e("Error reopening dataset instance", e.toString())
            emit(Result.failure(Exception("Failed to reopen: ${e.message}")))
        }
    }.flowOn(dispatcher)

    override suspend fun syncDatasetInstance(
        datasetId: String,
        periodId: String,
        orgUnitId: String,
        attributeOptionComboId: String
    ): Flow<Result<Unit>> = flow {
        try {
            // Sync data values
            d2.dataValueModule().dataValues()
                .byDataSetUid(datasetId)
                .byPeriod().eq(periodId)
                .byOrganisationUnitUid().eq(orgUnitId)
                .byAttributeOptionComboUid().eq(attributeOptionComboId)
                .upload()


            // Sync complete registration if exists
            val completeRegistration = d2.dataSetModule().dataSetCompleteRegistrations()
                .byDataSetUid().eq(datasetId)
                .byPeriod().eq(periodId)
                .byOrganisationUnitUid().eq(orgUnitId)
                .byAttributeOptionComboUid().eq(attributeOptionComboId)
                .one().blockingGet()

            if (completeRegistration != null) {
                d2.dataSetModule().dataSetCompleteRegistrations()
                    .upload()
            }

            emit(Result.success(Unit))
        } catch (e: D2Error) {
            Log.e("Error syncing dataset instance", e.toString())
            emit(Result.failure(Exception("Failed to sync: ${e.errorDescription()}")))
        } catch (e: Exception) {
            Log.e("Error syncing dataset instance", e.toString())
            emit(Result.failure(Exception("Failed to sync: ${e.message}")))
        }
    }.flowOn(dispatcher)

    override suspend fun hasCompletePermission(datasetId: String): Boolean = withContext(dispatcher) {
        try {
            val dataset = d2.dataSetModule().dataSets()
                .uid(datasetId)
                .blockingGet()

            dataset?.access()?.data()?.write() ?: false
        } catch (e: Exception) {
            Log.e("Error checking complete permission", e.toString())
            false
        }
    }

    override fun getAvailablePeriods(datasetId: String): Flow<List<Period>> = flow {
        try {
            val dataset = d2.dataSetModule().dataSets()
                .uid(datasetId)
                .blockingGet()

            val periodType = dataset?.periodType()
            if (periodType != null) {
                val periods = d2.periodModule().periods()
                    .byPeriodType().eq(periodType)
                    .blockingGet()
                    .map {
                        Period(
                            id = it.periodId() ?: "",
                            startDate = it.startDate() ?: Date(),
                            endDate = it.endDate() ?: Date(),
                            periodType = it.periodType()?.name ?: "",
                            displayName = formatPeriodName(it.startDate(), it.endDate(), periodType)
                        )
                    }
                    .sortedByDescending { it.startDate }

                emit(periods)
            } else {
                emit(emptyList())
            }
        } catch (e: Exception) {
            Log.e("Error fetching available periods", e.toString())
            emit(emptyList())
        }
    }.flowOn(dispatcher)

    private fun formatPeriodName(startDate: Date?, endDate: Date?, periodType: PeriodType): String {
        if (startDate == null) return ""

        val dateFormat = when (periodType) {
            PeriodType.Daily -> SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            PeriodType.Weekly, PeriodType.WeeklySaturday, PeriodType.WeeklySunday, PeriodType.WeeklyThursday, PeriodType.WeeklyWednesday -> {
                if (endDate != null) {
                    "Week ${SimpleDateFormat("w", Locale.getDefault()).format(startDate)}, ${SimpleDateFormat("yyyy", Locale.getDefault()).format(startDate)}"
                } else {
                    SimpleDateFormat("'Week' w, yyyy", Locale.getDefault()).format(startDate)
                }
            }
            PeriodType.Monthly -> SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(startDate)
            PeriodType.BiMonthly -> {
                val month = SimpleDateFormat("MM", Locale.getDefault()).format(startDate).toInt()
                val biMonth = (month - 1) / 2 + 1
                "BiMonth $biMonth, ${SimpleDateFormat("yyyy", Locale.getDefault()).format(startDate)}"
            }
            PeriodType.Quarterly -> {
                val month = SimpleDateFormat("MM", Locale.getDefault()).format(startDate).toInt()
                val quarter = (month - 1) / 3 + 1
                "Q$quarter ${SimpleDateFormat("yyyy", Locale.getDefault()).format(startDate)}"
            }
            PeriodType.SixMonthly, PeriodType.SixMonthlyApril -> {
                val month = SimpleDateFormat("MM", Locale.getDefault()).format(startDate).toInt()
                val semester = if (month <= 6) "S1" else "S2"
                "$semester ${SimpleDateFormat("yyyy", Locale.getDefault()).format(startDate)}"
            }
            PeriodType.Yearly, PeriodType.FinancialApril, PeriodType.FinancialJuly, PeriodType.FinancialOct -> {
                SimpleDateFormat("yyyy", Locale.getDefault()).format(startDate)
            }
            else -> SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(startDate)
        }

        return if (dateFormat is String) dateFormat else dateFormat.format(startDate)
    }

    override fun getAvailableOrgUnits(datasetId: String): Flow<List<OrganisationUnit>> = flow {
        try {
            val orgUnits = d2.organisationUnitModule().organisationUnits()
                .byDataSetUids(listOf(datasetId))
                .byOrganisationUnitScope(Scope.SCOPE_DATA_CAPTURE)
                .blockingGet()
                .map {
                    OrganisationUnit(
                        id = it.uid(),
                        name = it.displayName() ?: it.name() ?: "",
                        code = it.code(),
                        level = it.level(),
                        parent = it.parent()?.uid()
                    )
                }

            emit(orgUnits)
        } catch (e: Exception) {
            Log.e("Error fetching available org units", e.toString())
            emit(emptyList())
        }
    }.flowOn(dispatcher)
}