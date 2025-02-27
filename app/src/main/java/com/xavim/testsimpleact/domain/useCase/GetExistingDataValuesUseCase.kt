package com.xavim.testsimpleact.domain.useCase

import com.xavim.testsimpleact.domain.model.DataValue
import com.xavim.testsimpleact.domain.repository.DataEntryRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetExistingDataValuesUseCase @Inject constructor(
    private val repository: DataEntryRepository
) {
    operator fun invoke(
        datasetId: String,
        periodId: String,
        orgUnitId: String,
        attributeOptionComboId: String
    ): Flow<List<DataValue>> =
        repository.getExistingDataValues(
            datasetId,
            periodId,
            orgUnitId,
            attributeOptionComboId
        )
}