package com.xavim.testsimpleact.data.repositoryImpl


import com.xavim.testsimpleact.domain.model.DataEntryElement
import com.xavim.testsimpleact.domain.model.DataEntrySection
import com.xavim.testsimpleact.domain.model.DataValue
import com.xavim.testsimpleact.domain.repository.DataEntryRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.category.CategoryCombo
import org.hisp.dhis.android.core.dataset.DataSetInstance
import javax.inject.Inject

//class DataEntryRepositoryImpl(
//    private val d2: D2
//) : DataEntryRepository {
//
//    private var dataSetInstance: DataSetInstance? = null
//
//
//
//    override fun getDataEntryForm(dataSetUid: String): Flow<List<DataEntrySection>> = flow {
//        withContext(Dispatchers.IO) {
//            try {
//                val sections = d2.dataSetModule().sections()
//                    .byDataSetUid().eq(dataSetUid)
//                    .withDataElements()
//                    .withGreyedFields()
//                    .blockingGet()
//
//                val domainSections = sections.map { section ->
//                    DataEntrySection(
//                        uid = section.uid(),
//                        name = section.displayName() ?: section.name() ?: "",
//                        dataElements = section.dataElements()!!.map { dataElement ->
//                            val elementDetails = d2.dataElementModule().dataElements()
//                                .uid(dataElement.uid()).blockingGet()
//
//                            val categoryCombos = d2.categoryModule().categoryOptionCombos()
//                                .byCategoryComboUid().eq(elementDetails!!.categoryComboUid())
//                                .blockingGet()
//
//                            categoryCombos.map { combo ->
//                                val dataValue = d2.dataValueModule().dataValues().value(
//                                    dataSetInstance!!.period(),
//                                    dataSetInstance!!.organisationUnitUid(),
//                                    dataElement.uid(),
//                                    combo.uid(),
//                                    dataSetInstance!!.attributeOptionComboUid()
//                                ).blockingGet()
//
//                                DataEntryElement(
//                                    dataElementId = "${dataElement.uid()}_${combo.uid()}",
//                                    name = "${elementDetails.displayName()} - ${combo.displayName()}",
//                                    type = elementDetails.valueType()!!.name,
//                                    value = dataValue?.value() ?: "",
//                                    optionSetUid = elementDetails.optionSetUid(),
//                                    style = elementDetails.style().toString()
//                                )
//                            }
//                        }.flatten()
//                    )
//                }
//                emit(domainSections)
//            } catch (e: Exception) {
//                throw DataEntryRepositoryException("Failed to fetch sections", e)
//            }
//        }
//    }

//    override fun createDataEntry(
//        datasetId: String,
//        periodId: String,
//        orgUnitId: String,
//        attrOptComb: String
//    ): Flow<String> = flow {
//        withContext(Dispatchers.IO) {
//            try {
//                val newInstance = d2.dataSetModule().dataSetInstances()
//                    .byDataSetUid().eq(datasetId)
//                    .byOrganisationUnitUid().eq(orgUnitId)
//                    .byPeriod().`in`("201901", "201902")
//                    .get()
//
//                emit("${datasetId}_${periodId}_${orgUnitId}_${attrOptComb}")
//            } catch (e: Exception) {
//                throw DataEntryRepositoryException("Failed to create data entry", e)
//            }
//        }
//    }



//    override fun updateDataValue(
//        entryId: String,
//        dataElementComboUid: String,
//        value: String
//    ): Flow<Boolean> = flow {
//        withContext(Dispatchers.IO) {
//            try {
//                val parts = entryId.split("_")
//                if (parts.size != 4) throw IllegalArgumentException("Invalid entry ID format")
//
//                val (dataSetUid, periodId, orgUnitUid, attrOptComb) = parts
//                val (dataElementUid, comboUid) = dataElementComboUid.split("_")
//
//                d2.dataValueModule().dataValues().value(
//                    periodId,
//                    orgUnitUid,
//                    dataElementUid,
//                    comboUid,
//                    attrOptComb
//                ).blockingSet(value)
//
//                emit(true)
//            } catch (e: Exception) {
//                throw DataEntryRepositoryException("Failed to update data value", e)
//            }
//        }
//    }
//
//    override fun markComplete(dataSetUid: String, entryId: String): Flow<Boolean> = flow {
//        withContext(Dispatchers.IO) {
//            try {
//                val parts = entryId.split("_")
//                if (parts.size != 4) throw IllegalArgumentException("Invalid entry ID format")
//                val (_, periodId, orgUnitUid, attrOptComb) = parts
//
//                d2.dataSetModule().dataSetCompleteRegistrations()
//                    .value(dataSetUid, periodId, orgUnitUid, attrOptComb)
//                    .blockingSet()
//
//                emit(true)
//            } catch (e: Exception) {
//                throw DataEntryRepositoryException("Failed to mark as complete", e)
//            }
//        }
//    }
//}
//
//class DataEntryRepositoryException(message: String, cause: Throwable?) : Exception(message, cause)


//class DataEntryRepositoryImpl @Inject constructor(
//    private val d2: D2
//) : DataEntryRepository {
//
//    override suspend fun getFormStructure(datasetId: String): DataEntryForm {
//        val dataSet = d2.dataSetModule().dataSets().uid(datasetId).blockingGet()
//
//
//        val sections = d2.dataSetModule().sections()
//                    .byDataSetUid().eq(datasetId)
//                    .withDataElements()
//                    .withGreyedFields()
//                    .blockingGet()
//                    .map { section ->
//            DataEntrySection(
//                id = section.uid(),
//                name = section.displayName()!!,
//                categoryCombos = section.categoryCombo()?.let { combo ->
//                    getCategoryComboStructure(combo.uid())
//                } ?: emptyList()
//            )
//        }
//        if (dataSet != null) {
//            return DataEntryForm(
//                datasetId = datasetId,
//                name = dataSet.displayName()!!,
//                sections = sections
//            )
//        }
//    }
//
//    override suspend fun saveDataValue(dataValue: DataEntryValue) {
//        d2.dataValueModule().dataValues().value(
//            dataValue.dataElementId,
//            dataValue.period,
//            dataValue.orgUnitId,
//            dataValue.categoryOptionComboId,
//            dataValue.value
//        ).blockingSet()
//    }
//
//    private fun getCategoryComboStructure(comboId: String): List<CategoryCombo> {
//        // Implementation using d2.categoryModule()
//    }
//}


class DataEntryRepositoryImpl @Inject constructor(
    private val d2: D2
) : DataEntryRepository {

    override fun getDataEntryForm(dataSetUid: String): Flow<List<DataEntrySection>> = flow {
        withContext(Dispatchers.IO) {
            try {
                val sections = d2.dataSetModule().sections()
                    .byDataSetUid().eq(dataSetUid)
                    .withDataElements()
                    .withGreyedFields()
                    .blockingGet()

                val domainSections = sections.map { section ->
                    DataEntrySection(
                        uid = section.uid(),
                        name = section.displayName() ?: section.name() ?: "",
                        dataElements = section.dataElements()!!.map { dataElement ->
                            val elementDetails = d2.dataElementModule().dataElements()
                                .uid(dataElement.uid()).blockingGet()

                            DataEntryElement(
                                dataElementId = dataElement.uid(),
                                name = elementDetails?.displayName() ?: "",
                                type = elementDetails?.valueType()?.name ?: "TEXT",
                                value = "",
                                optionSetUid = elementDetails?.optionSetUid(),
                                style = elementDetails?.style().toString()
                            )
                        }
                    )
                }
                emit(domainSections)
            } catch (e: Exception) {
                throw DataEntryRepositoryException("Failed to fetch sections", e)
            }
        }
    }

    override fun getExistingDataValues(
        datasetId: String,
        periodId: String,
        orgUnitId: String,
        attributeOptionComboId: String
    ): Flow<List<DataValue>> = flow {
        withContext(Dispatchers.IO) {
            try {
                val values = d2.dataValueModule().dataValues()
                    .byPeriod().eq(periodId)
                    .byOrganisationUnitUid().eq(orgUnitId)
                    .byAttributeOptionComboUid().eq(attributeOptionComboId)
                    .blockingGet()

                emit(values.map { value ->
                    DataValue(
                        dataElementId = value.dataElement()!!,
                        value = value.value() ?: ""
                    )
                })
            } catch (e: Exception) {
                throw DataEntryRepositoryException("Failed to fetch existing values", e)
            }
        }
    }

    override suspend fun updateDataValues(
        datasetId: String,
        periodId: String,
        orgUnitId: String,
        attributeOptionComboId: String,
        values: Map<String, String>
    ) {
        withContext(Dispatchers.IO) {
            try {
                values.forEach { (elementId, value) ->
                    d2.dataValueModule().dataValues()
                        .value(
                            periodId,
                            orgUnitId,
                            elementId,
                            attributeOptionComboId,
                            value
                        )
                        .blockingSet(value)
                }
            } catch (e: Exception) {
                throw DataEntryRepositoryException("Failed to update values", e)
            }
        }
    }

    override suspend fun createDataEntry(
        datasetId: String,
        periodId: String,
        orgUnitId: String,
        attributeOptionComboId: String,
        values: Map<String, String>
    ) {
        withContext(Dispatchers.IO) {
            try {
                // Create new dataset instance if needed
                values.forEach { (elementId, value) ->
                    d2.dataValueModule().dataValues()
                        .value(
                            periodId,
                            orgUnitId,
                            elementId,
                            attributeOptionComboId,
                            value
                        )
                        .blockingSet(value)
                }
            } catch (e: Exception) {
                throw DataEntryRepositoryException("Failed to create data entry", e)
            }
        }
    }
}

class DataEntryRepositoryException(message: String, cause: Throwable? = null) : Exception(message, cause)