package com.blueskybone.arkscreen

import android.util.Xml
import com.blueskybone.arkscreen.UpdateResource.Companion.site
import com.blueskybone.arkscreen.network.HttpConnectionUtils.Companion.downloadToLocal
import com.blueskybone.arkscreen.network.HttpConnectionUtils.Companion.httpResponseConnection
import com.blueskybone.arkscreen.network.RequestMethod
import com.blueskybone.arkscreen.util.readFileAsJsonNode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.xmlpull.v1.XmlPullParser
import java.io.File
import java.io.FileOutputStream
import java.net.URL

/**
 *   Created by blueskybone
 *   Date: 2025/1/21
 */

sealed interface UpdateResource {
    val filename: String
    val url: URL
    val path: String

    suspend fun update() {
        val localVersion = getLocalVersion()
        val remoteInfo = getUpdateInfo()
        if (remoteInfo.version > localVersion) {
            downloadToLocal("${path}/${filename}", URL(remoteInfo.link))
        }
    }

    fun getResourceFilepath(): String {
        return copyAssetsFile(path, filename)
    }

    private fun getLocalVersion(): Int {
        val filepath = copyAssetsFile(path, filename)
        val node = readFileAsJsonNode(filepath)
        return node["update"]["version"].asInt()
    }

    fun getLocalUpdate(): String {
        val filepath = copyAssetsFile(path, filename)
        val node = readFileAsJsonNode(filepath)
        return node["update"]["date"].asText()
    }

    //如果缓存文件没有，从Assets复制出来
    private fun copyAssetsFile(filepath: String, filename: String): String {
        val fileStorePath = "${filepath}/${filename}"
        val file = File(filepath, filename)
        if (!file.exists()) {
            APP.assets.open(filename).use { inputStream ->
                FileOutputStream(file).use { outputStream ->
                    val buf = ByteArray(4096)
                    var len: Int
                    while (inputStream.read(buf).also { len = it } > 0) {
                        outputStream.write(buf, 0, len)
                    }
                }
            }
        }
        return fileStorePath
    }

    private suspend fun getUpdateInfo(): UpdateInfo {
        val updateInfo = UpdateInfo()
        val cn = httpResponseConnection(url, null, RequestMethod.GET)
        val inputStream = cn.inputStream
        val xmlPullParser = Xml.newPullParser()
        xmlPullParser.setInput(inputStream, "utf-8")
        var eventType = xmlPullParser.eventType
        while (eventType != XmlPullParser.END_DOCUMENT) {
            if (eventType == XmlPullParser.START_TAG) {
                when (xmlPullParser.name) {
                    "version" -> {
                        xmlPullParser.next()
                        updateInfo.version = xmlPullParser.text.toFloat()
                    }

                    "update" -> {
                        xmlPullParser.next()
                        updateInfo.date = xmlPullParser.text
                    }

                    "link" -> {
                        xmlPullParser.next()
                        updateInfo.link = xmlPullParser.text
                    }

                    "content" -> {
                        xmlPullParser.next()
                        updateInfo.content = xmlPullParser.text
                    }

                    else -> {

                    }
                }
            }
            eventType = xmlPullParser.next()
        }
        withContext(Dispatchers.IO) {
            inputStream.close()
        }
        cn.disconnect()
        return updateInfo
    }

    data class UpdateInfo(
        var version: Float = 0F,
        var date: String = "",
        var content: String = "",
        var link: String = ""
    )

    companion object {
        const val site: String =
            "https://gitee.com/blueskybone/ArkScreen/raw/master/resource/"
    }
}

data object CharAllMap : UpdateResource {
    override val filename = "char_info_map.json"
    override val url: URL =
        URL(site + "char_info_map_version.xml")
    override val path = "${APP.externalCacheDir}"
}

data object I18n : UpdateResource {
    override val filename = "i18n.json"
    override val url: URL =
        URL(site + "i18n_version.xml")
    override val path = "${APP.externalCacheDir}"
}

data object RecruitDb : UpdateResource {
    override val filename = "recruit_db.json"
    override val url: URL =
        URL(site + "recruit_version.xml")
    override val path = "${APP.externalCacheDir}"
}

data object AppUpdate {
    val url: URL = URL(site + "app_version.xml")

    data class AppUpdateInfo(
        var versionCode: Float = 0F,
        var version: String = "",
        var date: String = "",
        var content: String = "",
        var link: String = ""
    )

    suspend fun getUpdateInfo(): AppUpdateInfo {
        val updateInfo = AppUpdateInfo()
        val cn = httpResponseConnection(url, null, RequestMethod.GET)
        val inputStream = cn.inputStream
        val xmlPullParser = Xml.newPullParser()
        xmlPullParser.setInput(inputStream, "utf-8")
        var eventType = xmlPullParser.eventType
        while (eventType != XmlPullParser.END_DOCUMENT) {
            if (eventType == XmlPullParser.START_TAG) {
                when (xmlPullParser.name) {
                    "version" -> {
                        xmlPullParser.next()
                        updateInfo.version = xmlPullParser.text
                    }

                    "versionCode" -> {
                        xmlPullParser.next()
                        updateInfo.versionCode = xmlPullParser.text.toFloat()
                    }

                    "link" -> {
                        xmlPullParser.next()
                        updateInfo.link = xmlPullParser.text
                    }

                    "content" -> {
                        xmlPullParser.next()
                        updateInfo.content = xmlPullParser.text
                    }

                    else -> {

                    }
                }
            }
            eventType = xmlPullParser.next()
        }
        withContext(Dispatchers.IO) {
            inputStream.close()
        }
        cn.disconnect()
        return updateInfo
    }
}



