package com.xavim.testsimpleact.domain.useCase

class GetAvailableOrgUnitsUseCase @Inject constructor(
    private val repository: DatasetInstanceRepository
) {
    operator fun invoke(datasetId: String): Flow<List<OrganisationUnit>> =
        repository.getAvailableOrgUnits(datasetId)
}