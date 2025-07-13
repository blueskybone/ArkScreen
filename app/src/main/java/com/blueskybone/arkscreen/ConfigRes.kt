package com.blueskybone.arkscreen

import android.util.Xml
import com.blueskybone.arkscreen.network.downloadFile
import com.blueskybone.arkscreen.network.makeSuspendRequest
import com.blueskybone.arkscreen.util.readFileAsJsonNode
import org.xmlpull.v1.XmlPullParser
import java.io.File
import java.io.FileOutputStream
import java.io.StringReader
import java.net.URL

sealed interface ConfigRes {
    val filename: String    //Assets.name
    val url: URL
    val filepath: String    //full path include filename

    //public:
    suspend fun updateFile() {
        val localVersion = version()
        val remoteInfo = remoteInfo()
        if (remoteInfo.version > localVersion) {
            downloadFile(remoteInfo.link, filepath)
        }
    }

    fun updateTime(): String {
        val filepath = copyAssetsFile()
        val node = readFileAsJsonNode(filepath)
        return node["update"]["date"].asText()
    }

    fun getFilePath(): String {
        copyAssetsFile()
        return filepath
    }
    
    //private:
    suspend fun remoteInfo(): UpdateInfo {
        val updateInfo = UpdateInfo()
        try {
            val response = makeSuspendRequest(url)
            val parser = Xml.newPullParser().apply {
                setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
                setInput(StringReader(response))
            }
            var eventType = parser.eventType
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG) {
                    when (parser.name) {
                        "version" -> updateInfo.version = parser.nextText().toFloat()
                        "update" -> updateInfo.date = parser.nextText()
                        "link" -> updateInfo.link = parser.nextText()
                        "content" -> updateInfo.content = parser.nextText()
                        else -> parser.next()
                    }
                }
                eventType = parser.next()
            }
            return updateInfo
        } catch (e: Exception) {
            val errMsg = "error occur in getUpdateInfo: url=${url}, ${e.message}"
            return updateInfo
        }
    }

    private fun version(): Int {
        val filepath = copyAssetsFile()
        val node = readFileAsJsonNode(filepath)
        return node["update"]["version"].asInt()
    }

    private fun copyAssetsFile(): String {
        val file = File(filepath)
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
        return filepath
    }

    data class UpdateInfo(
        var version: Float = 0F,
        var date: String = "",
        var content: String = "",
        var link: String = ""
    )
}

data object CharAllMap : ConfigRes {
    override val filename = "char_info_map.json"
    override val url: URL =
        URL("https://gitee.com/blueskybone/ArkScreen/raw/master/resource/char_info_map_version.xml")
    override val filepath = "${APP.externalCacheDir}/$filename"
}

data object I18n : ConfigRes {
    override val filename = "i18n.json"
    override val url: URL =
        URL("https://gitee.com/blueskybone/ArkScreen/raw/master/resource/i18n_version.xml")
    override val filepath = "${APP.externalCacheDir}/$filename"
}

data object RecruitDb : ConfigRes {
    override val filename = "recruit_db.json"
    override val url: URL =
        URL("https://gitee.com/blueskybone/ArkScreen/raw/master/resource/recruit_version.xml")
    override val filepath = "${APP.externalCacheDir}/$filename"
}

data object AppUpdateInfo {
    private val url =
        URL("https://gitee.com/blueskybone/ArkScreen/raw/master/resource/app_version.xml")

    //    val versionCode = BuildConfig.VERSION_CODE
    //    val versionName = BuildConfig.VERSION_NAME
    suspend fun remoteInfo(): UpdateInfo {
        val updateInfo = UpdateInfo()
        try {
            val response = makeSuspendRequest(url)
            val parser = Xml.newPullParser().apply {
                setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
                setInput(StringReader(response))
            }
            var eventType = parser.eventType
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG) {
                    when (parser.name) {
                        "versionCode" -> updateInfo.version = parser.nextText().toFloat()
                        "version" -> updateInfo.version = parser.nextText().toFloat()
                        "update" -> updateInfo.date = parser.nextText()
                        "link" -> updateInfo.link = parser.nextText()
                        "content" -> updateInfo.content = parser.nextText()
                        else -> parser.next()
                    }
                }
                eventType = parser.next()
            }
            return updateInfo
        } catch (e: Exception) {
            val errMsg = "error occur in getUpdateInfo: url=$url, ${e.message}"
            return updateInfo
        }
    }

    data class UpdateInfo(
        var versionCode: Float = 0F,
        var version: Float = 0F,
        var date: String = "",
        var content: String = "",
        var link: String = ""
    )
}