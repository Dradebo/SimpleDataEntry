package com.xavim.testsimpleact.domain.useCase

import com.xavim.testsimpleact.domain.repository.DatasetInstanceRepository
import kotlinx.coroutines.flow.Flow
import org.hisp.dhis.android.core.analytics.aggregated.Dimension
import org.hisp.dhis.android.core.period.Period
import javax.inject.Inject

class CheckCompletePermissionUseCase @Inject constructor(
    private val repository: DatasetInstanceRepository
) {
    suspend operator fun invoke(datasetId: String): Boolean =
        repository.hasCompletePermission(datasetId)
}

//class GetAvailablePeriodsUseCase @Inject constructor(
//    private val repository: DatasetInstanceRepository
//) {
//    operator fun invoke(datasetId: String): Flow<List<Period>> =
//        repository.getAvailablePeriods(datasetId)
//}