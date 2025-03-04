package com.xavim.testsimpleact.domain.model

data class OrganisationUnit(

    val uid: String,
    val name: String,
    val level: Int,
    val parent: OrganisationUnit? = null,
    val children: List<OrganisationUnit> = emptyList(),



)