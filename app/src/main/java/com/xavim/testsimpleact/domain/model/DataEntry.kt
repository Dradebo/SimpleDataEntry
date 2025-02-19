package com.xavim.testsimpleact.domain.model

import org.hisp.dhis.android.core.category.CategoryCombo

//data class DataEntryForm(
//    val datasetId: String,
//    val name: String,
//    val sections: List<DataEntrySection>
//)
//
//data class DataEntrySection(
//    val uid: String,
//    val name: String,
//    val dataElements: List<DataEntryElement>
//)
//
//data class DataEntryElement(
//    val dataElementId: String,
//    val name: String,
//    val type: String,
//    val optionSetUid: String?,
//    val style: String?,
//    val value: String?
//
//)

data class DataEntrySection(
    val uid: String,
    val name: String,
    val dataElements: List<DataEntryElement>
)

data class DataEntryElement(
    val dataElementId: String,
    val name: String,
    val type: String,
    val value: String,
    val optionSetUid: String? = null,
    val style: String? = null
)

data class DataValue(
    val dataElementId: String,
    val value: String
)