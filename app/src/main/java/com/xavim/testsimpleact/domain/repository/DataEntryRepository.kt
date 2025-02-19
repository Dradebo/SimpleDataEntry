package com.xavim.testsimpleact.domain.repository

import com.xavim.testsimpleact.domain.model.DataEntryForm
import com.xavim.testsimpleact.domain.model.DataEntrySection
import com.xavim.testsimpleact.domain.model.DatasetInstance
import kotlinx.coroutines.flow.Flow



//interface DataEntryRepository {
//    /**
//     * Fetches sections and data elements for a specific dataset instance
//     * @param dataSetInstanceUid The UID of the dataset instance
//     * @return Flow of DataEntrySection list
//     */
//    fun getDataEntryForm(dataSetUid: String): Flow<List<DataEntrySection>>
//
//    /**
//     * Creates a new data entry instance
//     * @param datasetId The UID of the dataset
//     * @param periodId The period for the data entry
//     * @param orgUnitId The organisation unit UID
//     * @return Flow of the generated entry ID
//     */
//    //fun createDataEntry(datasetId: String, periodId: String, orgUnitId: String): Flow<String>
//
//    /**
//     * Updates a single data value in the data entry
//     * @param entryId The ID of the data entry instance
//     * @param dataElementId The UID of the data element to update
//     * @param value The new value to set
//     */
//    fun updateDataValue(entryId: String, dataElementId: String, value: String): Flow<Boolean>
//
//    /**
//     * Marks the data entry as complete
//     * @param datasetId The UID of the dataset
//     * @param entryId The ID of the data entry instance
//     */
//    fun markComplete(datasetId: String, entryId: String): Flow<Boolean>
//}


//interface DataEntryRepository {
//    suspend fun getFormStructure(datasetId: String): DataEntryForm
//    suspend fun saveDataValue(dataValue: DataEntryValue)
//    suspend fun markInstanceComplete(instanceId: String)
//}

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
}