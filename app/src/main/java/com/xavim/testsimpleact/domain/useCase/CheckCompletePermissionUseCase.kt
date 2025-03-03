package com.xavim.testsimpleact.domain.useCase

class CheckCompletePermissionUseCase @Inject constructor(
    private val repository: DatasetInstanceRepository
) {
    suspend operator fun invoke(datasetId: String): Boolean =
        repository.hasCompletePermission(datasetId)
}

class GetAvailablePeriodsUseCase @Inject constructor(
    private val repository: DatasetInstanceRepository
) {
    operator fun invoke(datasetId: String): Flow<List<Period>> =
        repository.getAvailablePeriods(datasetId)
}