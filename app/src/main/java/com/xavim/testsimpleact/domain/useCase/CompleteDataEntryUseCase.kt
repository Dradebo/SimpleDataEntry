package com.xavim.testsimpleact.domain.useCase

import com.xavim.testsimpleact.domain.repository.DataEntryRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

//class CompleteDataEntryUseCase @Inject constructor(
//    private val repository: DataEntryRepository
//) {
//    suspend operator fun invoke(
//        datasetId: String,
//        periodId: String,
//        orgUnitId: String,
//        attributeOptionComboId: String
//    ): Flow<Boolean> =
//        repository.markComplete(
//            datasetId,
//            "${datasetId}_${periodId}_${orgUnitId}_${attributeOptionComboId}"
//        )
//}