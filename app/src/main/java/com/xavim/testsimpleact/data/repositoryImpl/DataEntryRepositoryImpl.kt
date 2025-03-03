package com.xavim.testsimpleact.data.repositoryImpl

import android.util.Log
import com.xavim.testsimpleact.domain.model.CategoryOptionCombo
import com.xavim.testsimpleact.domain.model.DataElementValueType
import com.xavim.testsimpleact.domain.model.DataEntryElement
import com.xavim.testsimpleact.domain.model.DataEntrySection
import com.xavim.testsimpleact.domain.model.DataValue
import com.xavim.testsimpleact.domain.model.ValidationError
import com.xavim.testsimpleact.domain.repository.DataEntryRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.common.ValueType
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

class DataEntryRepositoryImpl @Inject constructor(
    private val d2: D2,
    private val dispatcher: CoroutineDispatcher
) : DataEntryRepository {

    override fun getDataEntryForm(dataSetUid: String): Flow<List<DataEntrySection>> = flow {
        try {
            // Get data set details
            val dataSet = d2.dataSetModule().dataSets()
                .withSections()
                .uid(dataSetUid)
                .blockingGet()

            // Get sections with data elements
            val sections = if (dataSet?.sections()?.isEmpty() == false) {
                // If dataset has defined sections, use them
                dataSet.sections()?.map { section ->
                    val sectionDetails = d2.dataSetModule().sections()
                        .withDataElements()
                        .uid(section.uid())
                        .blockingGet()

                    // Transform each section to our domain model
                    DataEntrySection(
                        uid = section.uid() ?: "",
                        name = section.displayName() ?: "",
                        description = section.description(),
                        dataElements = sectionDetails?.dataElements()?.map { dataElement ->
                            transformDataElement(dataElement.uid())
                        } ?: emptyList()
                    )
                }
            } else {
                // If no sections, create a default section with all data elements
                val dataElements = d2.dataSetModule().dataSets()
                    .withDataSetElements()
                    .uid(dataSetUid)
                    .blockingGet()
                    ?.dataSetElements()?.mapNotNull { it.dataElement()?.uid() }
                    ?.map { dataElementUid -> transformDataElement(dataElementUid) }
                    ?: emptyList()

                listOf(
                    DataEntrySection(
                        uid = "default",
                        name = dataSet?.displayName() ?: "Data Entry",
                        dataElements = dataElements
                    )
                )
            }

            emit(sections ?: emptyList())
        } catch (e: Exception) {
            Log.e( "Error fetching data entry form", e.toString())
            emit(emptyList())
        }
    }.flowOn(dispatcher)

    private fun transformDataElement(dataElementUid: String): DataEntryElement {
        val dataElement = d2.dataElementModule().dataElements()
            .uid(dataElementUid)
            .blockingGet()

        // Get category option combos for this data element
        val categoryComboUid = dataElement?.categoryCombo()?.uid()
        val categoryOptionCombos = if (categoryComboUid != null) {
            d2.categoryModule().categoryOptionCombos()
                .byCategoryComboUid().eq(categoryComboUid)
                .blockingGet()
                .map { combo ->
                    CategoryOptionCombo(
                        uid = combo.uid(),
                        name = combo.displayName() ?: "",
                        isDefault = combo.displayName()?.equals("default", ignoreCase = true) ?: false
                    )
                }
        } else {
            emptyList()
        }

        return DataEntryElement(
            dataElementId = dataElement?.uid() ?: "",
            name = dataElement?.displayName() ?: "",
            description = dataElement?.description(),
            valueType = mapValueType(dataElement?.valueType()),
            optionSetUid = dataElement?.optionSet()?.uid(),
            categoryOptionCombos = categoryOptionCombos,
            mandatory = dataElement?.fieldIsRequired() ?: false
        )
    }

    override fun getExistingDataValues(
        datasetId: String,
        periodId: String,
        orgUnitId: String,
        attributeOptionComboId: String
    ): Flow<List<DataValue>> = flow {
        try {
            val dataValues = d2.dataValueModule().dataValues()
                .byDataSetUid(datasetId)
                .byPeriod().eq(periodId)
                .byOrganisationUnitUid().eq(orgUnitId)
                .byAttributeOptionComboUid().eq(attributeOptionComboId)
                .blockingGet()

            val mappedValues = dataValues.map { value ->
                DataValue(
                    dataElementId = value.dataElement() ?: "",
                    categoryOptionComboId = value.categoryOptionCombo() ?: "",
                    value = value.value() ?: "",
                    storedBy = value.storedBy(),
                    lastUpdated = value.lastUpdated()
                )
            }

            emit(mappedValues)
        } catch (e: Exception) {
            Log.e( "Error fetching existing values", e.toString())
            emit(emptyList())
        }
    }.flowOn(dispatcher)

    override fun saveDataValue(
        datasetId: String,
        periodId: String,
        orgUnitId: String,
        attributeOptionComboId: String,
        dataElementId: String,
        categoryOptionComboId: String,
        value: String
    ): Flow<Result<Unit>> = flow {
        try {
            // First validate
            val errors = validateDataValue(dataElementId, value)
            if (errors.isNotEmpty()) {
                emit(Result.failure(Exception(errors.first().message)))
                return@flow
            }

            // Then save
            d2.dataValueModule().dataValues().value(
                dataElementId,
                periodId,
                orgUnitId,
                categoryOptionComboId,
                attributeOptionComboId
            ).blockingSet(value)

            emit(Result.success(Unit))
        } catch (e: Exception) {
            Log.e( "Error saving data value", e.toString())
            emit(Result.failure(e))
        }
    }.flowOn(dispatcher)

    override fun saveDataValues(
        datasetId: String,
        periodId: String,
        orgUnitId: String,
        attributeOptionComboId: String,
        values: Map<String, String>
    ): Flow<Result<Unit>> = flow {
        try {
            // First validate all
            val errors = validateAllValues(values)
            if (errors.isNotEmpty()) {
                emit(Result.failure(Exception("Validation failed")))
                return@flow
            }

            // Then save all
            values.forEach { (key, value) ->
                val parts = key.split("_")
                if (parts.size >= 2) {
                    val dataElementId = parts[0]
                    val categoryOptionComboId = parts[1]

                    d2.dataValueModule().dataValues().value(
                        dataElementId,
                        periodId,
                        orgUnitId,
                        categoryOptionComboId,
                        attributeOptionComboId
                    ).blockingSet(value)
                }
            }

            emit(Result.success(Unit))
        } catch (e: Exception) {
            Log.e("Error saving multiple values", e.toString())
            emit(Result.failure(e))
        }
    }.flowOn(dispatcher)

    override fun validateDataValue(
        dataElementId: String,
        value: String
    ): List<ValidationError> {
        try {
            val dataElement = d2.dataElementModule().dataElements()
                .uid(dataElementId)
                .blockingGet()

            val errors = mutableListOf<ValidationError>()

            // Check if empty and mandatory
            if (dataElement?.fieldIsRequired() == true && value.isEmpty()) {
                errors.add(ValidationError(dataElementId, "This field is required"))
            }

            // Check value type
            if (dataElement?.valueType()?.isNumeric == true && value.isNotEmpty()) {
                if (!value.matches(Regex("^-?\\d+(\\.\\d+)?$"))) {
                    errors.add(ValidationError(dataElementId, "This field requires a numeric value"))
                } else {
                    // Check min/max
                    val numericValue = value.toDoubleOrNull()
                    if (numericValue != null) {
                        dataElement.minimumValue()?.toDoubleOrNull()?.let { min ->
                            if (numericValue < min) {
                                errors.add(ValidationError(dataElementId, "Value must be at least $min"))
                            }
                        }

                        dataElement.maximumValue()?.toDoubleOrNull()?.let { max ->
                            if (numericValue > max) {
                                errors.add(ValidationError(dataElementId, "Value must not exceed $max"))
                            }
                        }
                    }
                }
            }

            return errors
        } catch (e: Exception) {
            Log.e("Error validating data value", e.toString())
            return listOf(ValidationError(dataElementId, "Validation error: ${e.message}"))
        }
    }

    override fun validateAllValues(values: Map<String, String>): List<ValidationError> {
        val allErrors = mutableListOf<ValidationError>()

        // Validate each value
        values.forEach { (key, value) ->
            val parts = key.split("_")
            if (parts.size >= 2) {
                val dataElementId = parts[0]
                val errors = validateDataValue(dataElementId, value)
                allErrors.addAll(errors)
            }
        }

        return allErrors
    }

    override fun getCategoryOptionComboCount(dataSetUid: String): Int {
        var totalCount = 0
        try {
            val dataElements = d2.dataSetModule().dataSets()
                .withDataSetElements()
                .uid(dataSetUid)
                .blockingGet()
                ?.dataSetElements()
                ?.mapNotNull { it.dataElement()?.uid() }
                ?: emptyList()

            // Count all category option combos across data elements
            dataElements.forEach { dataElementUid ->
                val dataElement = d2.dataElementModule().dataElements()
                    .uid(dataElementUid)
                    .blockingGet()

                val categoryComboUid = dataElement?.categoryCombo()?.uid()
                if (categoryComboUid != null) {
                    val count = d2.categoryModule().categoryOptionCombos()
                        .byCategoryComboUid().eq(categoryComboUid)
                        .blockingCount()
                    totalCount += count
                }
            }
        } catch (e: Exception) {
            Log.e("Error counting category option combos", e.toString())
        }
        return totalCount
    }

    override fun isExistingEntry(
        datasetId: String,
        periodId: String,
        orgUnitId: String,
        attributeOptionComboId: String
    ): Boolean {
        return try {
            val count = d2.dataValueModule().dataValues()
                .byDataSetUid(datasetId)
                .byPeriod().eq(periodId)
                .byOrganisationUnitUid().eq(orgUnitId)
                .byAttributeOptionComboUid().eq(attributeOptionComboId)
                .blockingCount()
            count > 0
        } catch (e: Exception) {
            Log.e( "Error checking if entry exists", e.toString())
            false
        }
    }

    override fun generatePeriodId(): String {
        // Generate a period ID in YYYYMM format for monthly periods
        val dateFormat = SimpleDateFormat("yyyyMM", Locale.US)
        return dateFormat.format(Date())
    }

    override fun getDefaultOrgUnitId(): String {
        // Get the first org unit available to the user
        return try {
            d2.organisationUnitModule().organisationUnits()
                .byOrganisationUnitScope(org.hisp.dhis.android.core.organisationunit.OrganisationUnit.Scope.SCOPE_DATA_CAPTURE)
                .blockingGet()
                .firstOrNull()
                ?.uid() ?: ""
        } catch (e: Exception) {
            Log.e( "Error getting default org unit", e.toString())
            ""
        }
    }

    override fun getDefaultAttributeOptionComboId(): String {
        // Get the default attribute option combo (usually "default")
        return try {
            d2.categoryModule().categoryOptionCombos()
                .blockingGet()
                .find { it.displayName()?.equals("default", ignoreCase = true) == true }
                ?.uid() ?: ""
        } catch (e: Exception) {
            Log.e( "Error getting default attribute option combo", e.toString())
            ""
        }
    }

    private fun mapValueType(valueType: ValueType?): DataElementValueType {
        return when (valueType) {
            ValueType.TEXT -> DataElementValueType.TEXT
            ValueType.LONG_TEXT -> DataElementValueType.LONG_TEXT
            ValueType.NUMBER -> DataElementValueType.NUMBER
            ValueType.INTEGER -> DataElementValueType.INTEGER
            ValueType.INTEGER_POSITIVE -> DataElementValueType.INTEGER_POSITIVE
            ValueType.INTEGER_NEGATIVE -> DataElementValueType.INTEGER_NEGATIVE
            ValueType.INTEGER_ZERO_OR_POSITIVE -> DataElementValueType.INTEGER_ZERO_OR_POSITIVE
            ValueType.PERCENTAGE -> DataElementValueType.PERCENTAGE
            ValueType.UNIT_INTERVAL -> DataElementValueType.UNIT_INTERVAL
            ValueType.BOOLEAN -> DataElementValueType.BOOLEAN
            ValueType.TRUE_ONLY -> DataElementValueType.TRUE_ONLY
            ValueType.DATE -> DataElementValueType.DATE
            ValueType.DATETIME -> DataElementValueType.DATETIME
            ValueType.TIME -> DataElementValueType.TIME
            ValueType.COORDINATE -> DataElementValueType.COORDINATE
            ValueType.PHONE_NUMBER -> DataElementValueType.PHONE_NUMBER
            ValueType.EMAIL -> DataElementValueType.EMAIL
            ValueType.URL -> DataElementValueType.URL
            ValueType.FILE_RESOURCE -> DataElementValueType.FILE_RESOURCE
            ValueType.IMAGE -> DataElementValueType.IMAGE
            ValueType.TRACKER_ASSOCIATE -> DataElementValueType.TRACKER_ASSOCIATE
            ValueType.USERNAME -> DataElementValueType.USERNAME
            else -> DataElementValueType.TEXT
        }
    }
}