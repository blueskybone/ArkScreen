package com.blueskybone.arkscreen.data.resource

/**
 * Created by blueskybone
 * Date: 2026/7/9
 */

import android.util.Xml
import com.blueskybone.arkscreen.domain.model.ResourceUpdateInfo
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import org.xmlpull.v1.XmlPullParser
import java.io.StringReader
import java.net.URL

class ResourceUpdateChecker(
    private val dispatcher: CoroutineDispatcher,
) {

    suspend fun fetchUpdateInfo(xmlUrl: String): ResourceUpdateInfo =
        withContext(dispatcher) {
            val xmlText = fetchText(xmlUrl)
            parseResourceXml(xmlText)
        }

    private fun fetchText(url: String): String {
        val connection = URL(url).openConnection().apply {
            connectTimeout = 10_000
            readTimeout = 10_000
        }

        return connection.getInputStream()
            .bufferedReader()
            .use { it.readText() }
    }

    private fun parseResourceXml(xmlText: String): ResourceUpdateInfo {
        val parser = Xml.newPullParser().apply {
            setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
            setInput(StringReader(xmlText))
        }

        var versionCode = 0L
        var version = ""
        var date = ""
        var content = ""
        var link = ""

        var eventType = parser.eventType

        while (eventType != XmlPullParser.END_DOCUMENT) {
            if (eventType == XmlPullParser.START_TAG) {
                when (parser.name) {
                    "versionCode" -> versionCode = parser.nextText().toLongOrNull() ?: 0L
                    "version" -> version = parser.nextText()
                    "update" -> date = parser.nextText()
                    "content" -> content = parser.nextText()
                    "link" -> link = parser.nextText()
                }
            }

            eventType = parser.next()
        }

        if (version.isBlank()) {
            throw IllegalStateException("update xml missing version")
        }

        if (link.isBlank()) {
            throw IllegalStateException("update xml missing link")
        }

        return ResourceUpdateInfo(
            versionCode = versionCode,
            version = version,
            date = date,
            content = content,
            link = link,
        )
    }
}
