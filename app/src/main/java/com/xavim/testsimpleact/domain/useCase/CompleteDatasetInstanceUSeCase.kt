package com.xavim.testsimpleact.domain.useCase

class CompleteDatasetInstanceUseCase @Inject constructor(
    private val repository: DatasetInstanceRepository
) {
    suspend operator fun invoke(
        datasetId: String,
        periodId: String,
        orgUnitId: String,
        attributeOptionComboId: String,
        completedBy: String? = null
    ): Flow<Result<Unit>> = repository.completeDatasetInstance(
        datasetId, periodId, orgUnitId, attributeOptionComboId, completedBy
    )
}