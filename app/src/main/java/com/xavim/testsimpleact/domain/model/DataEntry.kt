//package com.xavim.testsimpleact.domain.model
//
//import java.util.Date
//
//// Enhanced Data Entry Section that knows how to handle many category options
//data class DataEntrySection(
//    val uid: String,
//    val name: String,
//    val description: String? = null,
//    val dataElements: List<DataEntryElement>,
//    val isExpanded: Boolean = false,
//    val totalFields: Int = 0,
//    val completedFields: Int = 0
//)
//
//// Enhanced Data Entry Element with category option combo support
//data class DataEntryElement(
//    val dataElementId: String,
//    val name: String,
//    val description: String? = null,
//    val valueType: DataElementValueType,
//    val optionSetUid: String? = null,
//    val categoryOptionCombos: List<CategoryOptionCombo> = emptyList(),
//    val value: String = "",
//    val mandatory: Boolean = false,
//    val editable: Boolean = true,
//    val hasError: Boolean = false,
//    val errorMessage: String? = null
//)
//
//// Structure to represent category option combo relationships
//data class CategoryOptionCombo(
//    val uid: String,
//    val name: String,
//    val value: String = "",
//    val isDefault: Boolean = false
//)
//
//// Improved ValueType enum with isNumeric helper
//enum class DataElementValueType {
//    TEXT, LONG_TEXT, NUMBER, INTEGER, INTEGER_POSITIVE, INTEGER_NEGATIVE,
//    INTEGER_ZERO_OR_POSITIVE, PERCENTAGE, UNIT_INTERVAL, BOOLEAN, TRUE_ONLY,
//    DATE, DATETIME, TIME, COORDINATE, PHONE_NUMBER, EMAIL, URL,
//    FILE_RESOURCE, IMAGE, TRACKER_ASSOCIATE, USERNAME;
//
//    val isNumeric: Boolean
//        get() = this == NUMBER || this == INTEGER || this == INTEGER_POSITIVE ||
//                this == INTEGER_NEGATIVE || this == INTEGER_ZERO_OR_POSITIVE ||
//                this == PERCENTAGE || this == UNIT_INTERVAL
//}
//
//// Model for validation errors
//data class ValidationError(
//    val elementId: String,
//    val message: String
//)
//
//// Data value model
//data class DataValue(
//    val dataElementId: String,
//    val categoryOptionComboId: String,
//    val value: String,
//    val storedBy: String? = null,
//    val lastUpdated: Date? = null
//)
//
//
//
//// Result class for validation
//sealed class ValidationResult {
//    object Success : ValidationResult()
//    data class Error(val errors: List<ValidationError>) : ValidationResult()
//}
//
