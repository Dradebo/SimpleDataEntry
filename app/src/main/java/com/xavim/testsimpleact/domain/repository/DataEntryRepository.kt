package com.xavim.testsimpleact.domain.repository

import com.xavim.testsimpleact.domain.model.DataEntrySection
import com.xavim.testsimpleact.domain.model.DataValue
import com.xavim.testsimpleact.domain.model.DatasetInstance
import kotlinx.coroutines.flow.Flow



interface DataEntryRepository {
    fun getDataEntryForm(dataSetUid: String): Flow<List<DataEntrySection>>

    fun getExistingDataValues(
        datasetId: String,
        periodId: String,
        orgUnitId: String,
        attributeOptionComboId: String
    ): Flow<List<DataValue>>

    suspend fun updateDataValues(
        datasetId: String,
        periodId: String,
        orgUnitId: String,
        attributeOptionComboId: String,
        values: Map<String, String>
    )

    suspend fun createDataEntry(
        datasetId: String,
        periodId: String,
        orgUnitId: String,
        attributeOptionComboId: String,
        values: Map<String, String>
    )

    suspend fun markComplete(
        datasetId: String,
        instanceId: String
    ): Flow<Boolean>
}

