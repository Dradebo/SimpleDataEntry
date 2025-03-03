package com.xavim.testsimpleact.domain.useCase

class ReopenDatasetInstanceUseCase @Inject constructor(
    private val repository: DatasetInstanceRepository
) {
    suspend operator fun invoke(
        datasetId: String,
        periodId: String,
        orgUnitId: String,
        attributeOptionComboId: String
    ): Flow<Result<Unit>> = repository.reopenDatasetInstance(
        datasetId, periodId, orgUnitId, attributeOptionComboId
    )
}