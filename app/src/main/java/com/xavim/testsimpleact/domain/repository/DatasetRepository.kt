package com.xavim.testsimpleact.domain.repository

import com.xavim.testsimpleact.domain.model.Dataset
import kotlinx.coroutines.flow.Flow

interface DatasetRepository {
    /**
     * Fetches all datasets from the remote or local DHIS2 store, returning a Flow.
     */
    fun getDatasets(): Flow<List<Dataset>>
}