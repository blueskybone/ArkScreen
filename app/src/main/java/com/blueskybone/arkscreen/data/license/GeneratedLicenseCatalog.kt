package com.blueskybone.arkscreen.data.license

import android.content.Context
import com.blueskybone.arkscreen.domain.model.license.OpenSourceLibrary
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper

/**
 * Reads the dependency metadata generated from Maven POM files during the Gradle build.
 */
class GeneratedLicenseCatalog(
    private val context: Context,
    private val objectMapper: ObjectMapper = ObjectMapper(),
) {
    fun load(): List<OpenSourceLibrary> {
        return context.assets.open(ASSET_NAME).use { input ->
            objectMapper.readValue(
                input,
                object : TypeReference<List<OpenSourceLibrary>>() {},
            )
        }
    }

    private companion object {
        const val ASSET_NAME = "third_party_licenses.json"
    }
}
