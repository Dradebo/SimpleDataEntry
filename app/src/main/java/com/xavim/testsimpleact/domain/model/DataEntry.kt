package com.xavim.testsimpleact.domain.model

import org.hisp.dhis.android.core.category.CategoryCombo

data class DataEntrySection(
    val uid: String,
    val name: String,
    val description: String? = null,
    val dataElements: List<DataEntryElement>
)

data class DataEntryElement(
    val dataElementId: String,
    val name: String,
    val description: String? = null,
    val type: String,
    val value: String,
    val optionSetUid: String? = null,
    val style: String? = null
)

data class DataValue(
    val dataElementId: String,
    val value: String
)

data class ValidationError(
    val elementId: String,
    val message: String
)