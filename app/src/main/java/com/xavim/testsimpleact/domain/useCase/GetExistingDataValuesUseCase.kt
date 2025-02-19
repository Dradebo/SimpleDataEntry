package com.xavim.testsimpleact.domain.useCase

class GetExistingDataValuesUseCase @Inject constructor(
    private val repository: DataEntryRepository
) {
    suspend operator fun invoke(
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