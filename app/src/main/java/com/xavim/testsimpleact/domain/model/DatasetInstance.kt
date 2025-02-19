package com.xavim.testsimpleact.domain.model


data class DatasetInstance(
    val dataSetUid: String,
    val periodId: String,
    val organisationUnitUid: String,
    val attributeOptionComboUid: String,
    val state: Boolean,
    val lastUpdated: String,
    val instanceUid: Long?,

    ) {
    enum class State {
        SYNCED,
        TO_UPDATE,
        TO_POST,
        ERROR,
        WARNING,
        COMPLETED,
        INCOMPLETE
    }
}
