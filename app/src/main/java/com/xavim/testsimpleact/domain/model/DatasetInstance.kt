//package com.xavim.testsimpleact.domain.model
//
//import java.util.Date
//
//// Enhanced Dataset Instance model with state management
//data class DatasetInstance(
//    val instanceUid: String,
//    val datasetId: String,
//    val periodId: String,
//    val organisationUnitUid: String,
//    val attributeOptionComboUid: String,
//    val state: DatasetInstanceState = DatasetInstanceState.OPEN,
//    val lastUpdated: Date,
//    val completedBy: String? = null,
//    val completedDate: Date? = null,
//    val valueCount: Int = 0,
//
//)
//
//// Enum for dataset instance state
//enum class DatasetInstanceState {
//    OPEN,
//    COMPLETED,
//    APPROVED,
//    LOCKED;
//
//    fun isCompleted(): Boolean = this != OPEN
//}
//
//// Enum for sync state
//enum class SyncState {
//    SYNCED,
//    TO_UPDATE,
//    TO_POST,
//    ERROR,
//    UPLOADING,
//    WARNING,
//    UNKNOWN // Added for handling unrecognized states or null
//}
//
//// Result classes for completion
//sealed class CompleteResult {
//    object Success : CompleteResult()
//    data class Error(val message: String) : CompleteResult()
//    data class ValidationFailed(val errors: List<ValidationError>) : CompleteResult()
//}
//
//sealed class ReopenResult {
//    object Success : ReopenResult()
//    data class Error(val message: String) : ReopenResult()
//}
//
//sealed class SyncResult {
//    object Success : SyncResult()
//    data class Error(val message: String) : SyncResult()
//}