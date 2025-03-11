//package com.xavim.testsimpleact.domain.repository
//
//import com.xavim.testsimpleact.domain.model.DataEntrySection
//import com.xavim.testsimpleact.domain.model.DataValue
//import com.xavim.testsimpleact.domain.model.ValidationError
//import kotlinx.coroutines.flow.Flow
//
//interface DataEntryRepository {
//    fun getDataEntryForm(dataSetUid: String): Flow<List<DataEntrySection>>
//
//    fun getExistingDataValues(
//        datasetId: String,
//        periodId: String,
//        orgUnitId: String,
//        attributeOptionComboId: String
//    ): Flow<List<DataValue>>
//
//    fun saveDataValue(
//        datasetId: String,
//        periodId: String,
//        orgUnitId: String,
//        attributeOptionComboId: String,
//        dataElementId: String,
//        categoryOptionComboId: String,
//        value: String
//    ): Flow<Result<Unit>>
//
//    fun saveDataValues(
//        datasetId: String,
//        periodId: String,
//        orgUnitId: String,
//        attributeOptionComboId: String,
//        values: Map<String, String>
//    ): Flow<Result<Unit>>
//
////    fun validateDataValue(
////        dataElementId: String,
////        value: String
////    ): List<ValidationError>
//
////    fun validateAllValues(values: Map<String, String>): List<ValidationError>
////
////    // Helper to determine if a form has too many fields (for UI decisions)
////    fun getCategoryOptionComboCount(dataSetUid: String): Int
////
////    // Whether this is an existing entry or new
////    fun isExistingEntry(
////        datasetId: String,
////        periodId: String,
////        orgUnitId: String,
////        attributeOptionComboId: String
////    ): Boolean
//
//    // Generate required IDs for new entries
//    fun generatePeriodId(): String
//    fun getDefaultOrgUnitId(): String
//    fun getDefaultAttributeOptionComboId(): String
//}