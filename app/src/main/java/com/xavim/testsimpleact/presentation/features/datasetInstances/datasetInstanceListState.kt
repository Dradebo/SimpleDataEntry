//package com.xavim.testsimpleact.presentation.features.datasetInstances
//
//import com.xavim.testsimpleact.domain.model.DatasetInstance
//
//sealed class DatasetInstanceListState {
//    object Loading : DatasetInstanceListState()
//    data class Success(val entries: List<DatasetInstance>) : DatasetInstanceListState()
//    data class Error(val message: String) : DatasetInstanceListState()
//}