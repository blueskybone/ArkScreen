package com.blueskybone.arkscreen.data.license

import android.content.Context
import com.blueskybone.arkscreen.domain.model.license.OpenSourceLibrary
import com.blueskybone.arkscreen.domain.model.license.OpenSourceLicense
import org.json.JSONArray

/** Reads the Maven license metadata bundled with the app. */
class GeneratedLicenseCatalog(private val context: Context) {
    fun load(): List<OpenSourceLibrary> {
        val libraries = context.assets.open("third_party_licenses.json").bufferedReader().use {
            JSONArray(it.readText())
        }
        return List(libraries.length()) { index ->
            val library = libraries.getJSONObject(index)
            val licenses = library.getJSONArray("licenses")
            OpenSourceLibrary(
                id = library.getString("id"),
                name = library.getString("name"),
                version = library.getString("version"),
                projectUrl = library.getString("projectUrl"),
                licenses = List(licenses.length()) { licenseIndex ->
                    val license = licenses.getJSONObject(licenseIndex)
                    OpenSourceLicense(license.getString("name"), license.getString("url"))
                },
            )
        }
    }
}
