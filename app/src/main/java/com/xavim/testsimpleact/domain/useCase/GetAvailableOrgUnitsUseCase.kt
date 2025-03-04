package com.xavim.testsimpleact.domain.useCase

import com.xavim.testsimpleact.domain.repository.DatasetInstanceRepository
import kotlinx.coroutines.flow.Flow
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit
import javax.inject.Inject

class GetAvailableOrgUnitsUseCase @Inject constructor(
    private val repository: DatasetInstanceRepository
) {
    operator fun invoke(datasetId: String): Flow<List<OrganisationUnit>> =
        repository.getAvailableOrgUnits(datasetId)
}