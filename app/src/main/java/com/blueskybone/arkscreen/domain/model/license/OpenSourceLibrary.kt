package com.blueskybone.arkscreen.domain.model.license

data class OpenSourceLibrary(
    val id: String = "",
    val name: String = "",
    val version: String = "",
    val projectUrl: String = "",
    val licenses: List<OpenSourceLicense> = emptyList(),
)

data class OpenSourceLicense(
    val name: String = "",
    val url: String = "",
)
