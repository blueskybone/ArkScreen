package com.blueskybone.arkscreen

import android.content.Context
import android.util.Xml
import com.blueskybone.arkscreen.network.HttpConnectionUtils
import com.blueskybone.arkscreen.network.HttpConnectionUtils.downloadToLocal
import com.blueskybone.arkscreen.network.HttpConnectionUtils.httpResponseConnection
import com.blueskybone.arkscreen.util.readFileAsJsonNode
import org.xmlpull.v1.XmlPullParser
import java.io.File
import java.io.FileOutputStream
import java.net.URL

/**
 *   Created by blueskybone
 *   Date: 2024/7/26
 */
class UpdateResource {

    enum class Resource(
        name: String,
        private val filename: String,
        private val url: URL,
        private val path: String
    ) {
        CharInfoMap(
            "charInfoMap",
            charInfoMapFileName,
            URL(charInfoMapVersionUrl),
            charInfoMapFilePath
        ),
        I18n("i18n", i18nFileName, URL(i18nVersionUrl), i18nFilePath),
        RecruitDb(
            "recruitDb",
            recruitDbFileName,
            URL(recruitDbVersionUrl),
            recruitDbFilePath
        );

        fun getUrl(): URL {
            return url
        }

        fun getPath(): String {
            return path
        }

        fun getFilename(): String {
            return filename
        }
    }

    companion object {
        private const val charInfoMapFileName = "char_info_map.json"
        private const val i18nFileName = "i18n.json"
        private const val recruitDbFileName = "recruit_db.json"

        private val charInfoMapFilePath = "${APP.externalCacheDir}"
        private val i18nFilePath = "${APP.externalCacheDir}"
        private val recruitDbFilePath = "${APP.externalCacheDir}"

        private const val charInfoMapVersionUrl =
            "https://gitee.com/blueskybone/ArkScreen/raw/master/resource/char_info_map_version.xml"
        private const val recruitDbVersionUrl =
            "https://gitee.com/blueskybone/ArkScreen/raw/master/resource/recruit_version.xml"
        private const val i18nVersionUrl =
            "https://gitee.com/blueskybone/ArkScreen/raw/master/resource/i18n_version.xml"

    }

    fun update(res: Resource): UpdateInfo {
        val localVersion = getLocalVersion(res)
        val remoteInfo = getUpdateInfo(res)
        if (remoteInfo.version > localVersion) {
            downloadToLocal("${res.getPath()}/${res.getFilename()}", URL(remoteInfo.link))
        }
        return remoteInfo
    }

    fun getResourceFilepath(res: Resource): String {
        return copyAssetsFile(res.getPath(), res.getFilename())
    }

    private fun getLocalVersion(res: Resource): Int {
        val filepath = copyAssetsFile(res.getPath(), res.getFilename())
        val node = readFileAsJsonNode(filepath)
        return node["update"]["version"].asInt()
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

    private fun getUpdateInfo(res: Resource): UpdateInfo {
        val updateInfo = UpdateInfo()
        val cn = httpResponseConnection(res.getUrl(), null, HttpConnectionUtils.RequestMethod.GET)
            ?: throw Exception("连接失败")
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
        inputStream.close()
        cn.disconnect()
        return updateInfo
    }

    data class UpdateInfo(
        var version: Float = 0F,
        var date: String = "",
        var content: String = "",
        var link: String = ""
    )
}
