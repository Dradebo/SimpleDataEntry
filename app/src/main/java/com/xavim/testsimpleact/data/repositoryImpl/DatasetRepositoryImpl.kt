package com.xavim.testsimpleact.data.repositoryImpl

import android.util.Log
import com.xavim.testsimpleact.domain.model.Dataset
import com.xavim.testsimpleact.domain.repository.DatasetRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.dataset.DataSet

class DatasetRepositoryImpl(
    private val d2: D2
) : DatasetRepository {

    /**
     * Fetches all datasets from DHIS2, converting them to domain models.
     * Emits the result as a Flow.
     */
    override fun getDatasets(): Flow<List<Dataset>> = flow {

        Log.d("DatasetRepository", "Fetching datasets from DHIS2")



        val dataSets = d2.dataSetModule()
            .dataSets()
            .blockingGet()
            .map { it.toDomainModel() }
        emit(dataSets)
    }
}

/**
 * Extension function converting the DHIS2 DataSet object into our domain Dataset model.
 */
fun DataSet.toDomainModel(): Dataset {
    return Dataset(
        uid = uid(),
        displayName = displayName() ?: name() ?: "",
        periodType = periodType()?.name ?: "",
        categoryCombo = categoryCombo()?.uid() ?: ""
    )
}