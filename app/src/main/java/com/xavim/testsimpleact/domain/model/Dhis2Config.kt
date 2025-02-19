package com.xavim.testsimpleact.domain.model

import javax.inject.Inject

data class Dhis2Config (
    val serverUrl: String,
    val username: String,
    val password: String
)