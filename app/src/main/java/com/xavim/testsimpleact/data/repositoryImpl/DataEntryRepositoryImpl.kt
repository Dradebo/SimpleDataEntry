package com.xavim.testsimpleact.data.repositoryImpl


import com.xavim.testsimpleact.domain.model.DataEntryElement
import com.xavim.testsimpleact.domain.model.DataEntrySection
import com.xavim.testsimpleact.domain.model.DataValue
import com.xavim.testsimpleact.domain.repository.DataEntryRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.category.CategoryCombo
import org.hisp.dhis.android.core.dataset.DataSetInstance
import javax.inject.Inject

class DataEntryRepositoryImpl @Inject constructor(
    private val d2: D2,
    private val dispatcher: CoroutineDispatcher
) : DataEntryRepository {

    override fun getDataEntryForm(dataSetUid: String): Flow<List<DataEntrySection>> = flow {
        withContext(dispatcher) {
            try {
                val sections = d2.dataSetModule().sections()
                    .byDataSetUid().eq(dataSetUid)
                    .withDataElements()
                    .blockingGet()

                val domainSections = sections.map { section ->
                    DataEntrySection(
                        uid = section.uid(),
                        name = section.displayName() ?: section.name() ?: "",
                        description = section.description(),
                        dataElements = section.dataElements()?.map { dataElement ->
                            val elementDetails = d2.dataElementModule().dataElements()
                                .uid(dataElement.uid()).blockingGet()

                            DataEntryElement(
                                dataElementId = dataElement.uid(),
                                name = elementDetails?.displayName() ?: "",
                                description = elementDetails?.description(),
                                type = elementDetails?.valueType()?.name ?: "TEXT",
                                value = "",
                                optionSetUid = elementDetails?.optionSetUid(),
                                style = elementDetails?.style()?.toString()
                            )
                        } ?: emptyList()
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
        withContext(dispatcher) {
            try {
                val dataValues = d2.dataValueModule().dataValues()
                    .byDataSetUid(datasetId)
                    .byPeriod().eq(periodId)
                    .byOrganisationUnitUid().eq(orgUnitId)
                    .byAttributeOptionComboUid().eq(attributeOptionComboId)
                    .blockingGet()

                emit(dataValues.map { value ->
                    DataValue(
                        dataElementId = value.dataElement() ?: "",
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
        withContext(dispatcher) {
            try {
                values.forEach { (elementId, value) ->
                    d2.dataValueModule().dataValues().value(
                        periodId,
                        orgUnitId,
                        elementId,
                        attributeOptionComboId,
                        value

                    ).blockingSet(value)
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
        withContext(dispatcher) {
            try {
                // First create or get the instance
                val dataSetInstance = d2.dataSetModule().dataSetInstances()
                    .byDataSetUid().eq(datasetId)
                    .byPeriod().eq(periodId)
                    .byOrganisationUnitUid().eq(orgUnitId)
                    .byAttributeOptionComboUid().eq(attributeOptionComboId)
                    .one().blockingGet()

                // Then set all values
                values.forEach { (elementId, value) ->
                    d2.dataValueModule().dataValues().value(
                        periodId,
                        orgUnitId,
                        elementId,
                        attributeOptionComboId,
                        value
                    ).blockingSet(value)
                }
            } catch (e: Exception) {
                throw DataEntryRepositoryException("Failed to create data entry", e)
            }
        }
    }

    override suspend fun markComplete(
        datasetId: String,
        instanceId: String
    ): Flow<Boolean> = flow {
        withContext(dispatcher) {
            try {
//                val result = d2.dataSetModule().dataSetCompleteRegistrations()
//                    .value(
//                        instanceId,
//                        true // completed
//                    )
//                    .blockingSet()
//
//                emit(result != null)
            } catch (e: Exception) {
                throw DataEntryRepositoryException("Failed to mark as complete", e)
            }
        }
    }
}

class DataEntryRepositoryException(message: String, cause: Throwable? = null) : Exception(message, cause)