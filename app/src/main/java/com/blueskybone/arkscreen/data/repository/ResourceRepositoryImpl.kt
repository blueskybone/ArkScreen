package com.blueskybone.arkscreen.data.repository

import android.content.Context
import android.util.Xml
import com.blueskybone.arkscreen.APP
import com.blueskybone.arkscreen.data.common.toAppError
import com.blueskybone.arkscreen.data.network.ApiService
import com.blueskybone.arkscreen.data.network.model.BiliResponse
import com.blueskybone.arkscreen.data.network.safeApiCall
import com.blueskybone.arkscreen.data.repository.mapper.readAsEntity
import com.blueskybone.arkscreen.data.repository.utils.appSign
import com.blueskybone.arkscreen.data.repository.utils.buildQuery
import com.blueskybone.arkscreen.data.repository.utils.createBiliHeader
import com.blueskybone.arkscreen.data.repository.utils.safeResultSync
import com.blueskybone.arkscreen.domain.AppError
import com.blueskybone.arkscreen.domain.model.DownloadStatus
import com.blueskybone.arkscreen.domain.model.AppUpdateInfo
import com.blueskybone.arkscreen.domain.model.BiliVideo
import com.blueskybone.arkscreen.domain.model.ConfigType
import com.blueskybone.arkscreen.domain.model.recruit.RecruitDatabase
import com.blueskybone.arkscreen.domain.repository.ResourceRepository
import com.blueskybone.arkscreen.util.readFileAsJsonNode
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import org.xmlpull.v1.XmlPullParser
import timber.log.Timber
import java.io.File
import java.io.StringReader
import java.net.URL

/**
 * Created by blueskybone
 * Date: 2026/3/9
 */
class ResourceRepositoryImpl(

    private val context: Context = APP,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
    private val api: ApiService
) : ResourceRepository {

    private var cachedI18nDb: Map<String, String>? = null
    private val announceUrl =
        "https://gitee.com/blueskybone/ArkScreen/raw/master/resource/announce.json"

    private data class ResourceUpdateInfo(
        var versionCode: Float = 0F,
        var version: String = "",
        var date: String = "",
        var content: String = "",
        var link: String = ""
    )

    //更新资源
    override suspend fun syncResource(type: ConfigType): Result<Unit> = safeResultSync {
        withContext(Dispatchers.IO) {
            val remoteInfo = fetchUpdateInfo(type.xmlUrl)
            val remoteVersion = remoteInfo.version
            val localVersion = getLocalVersion(type)
            if (remoteVersion <= localVersion) {
                Result.success(Unit)
            }
            downloadConfig(type.fileName, remoteInfo.link)
        }
    }


    override suspend fun getRecruitDb(): Result<RecruitDatabase> = safeResultSync {
        withContext(Dispatchers.IO) {
            val file = getValidFile(ConfigType.RECRUIT_DB) // 获取文件（内部含 Assets 拷贝逻辑）
            file.readAsEntity(RecruitDatabase::class.java)
        }
    }

    override suspend fun getI18nDb(): Result<Map<String, String>> = safeResultSync {
        withContext(Dispatchers.IO) {
            if (cachedI18nDb == null) {
                val map = mutableMapOf<String, String>()
                val file = getValidFile(ConfigType.I18N_DB)
                val rootNode = readFileAsJsonNode(file.absolutePath)
                rootNode["mapInfo"]?.fields()?.forEach { (key, value) ->
                    map[key] = value.asText()
                }
                map
            } else cachedI18nDb!!
        }
    }

    override suspend fun getCharInfoMap(): Result<JsonNode> = safeResultSync {
        withContext(Dispatchers.IO) {
            val file = getValidFile(ConfigType.CHAR_MAP)
            readFileAsJsonNode(file.absolutePath)["charInfoMap"]
                ?: throw okio.FileNotFoundException("char_info_map文件缺失")
        }
    }

    override suspend fun checkAppUpdate(): Result<AppUpdateInfo> = safeResultSync {
        withContext(Dispatchers.IO) {
            val remoteInfo = fetchUpdateInfo(ConfigType.APP_INFO.xmlUrl)
            AppUpdateInfo(
                version = remoteInfo.version,
                versionCode = remoteInfo.versionCode,
                date = remoteInfo.date,
                content = remoteInfo.content,
                link = remoteInfo.link
            )
        }
    }

    override suspend fun downloadFile(url: URL): Flow<DownloadStatus> {
        TODO("Not yet implemented")
    }

    override suspend fun fetchAnnounce(): Result<String> = safeResultSync {
        withContext(Dispatchers.IO) {
            // 使用简化的网络请求流
            val connection = URL(announceUrl).openConnection()
            val jsonText = connection.getInputStream().bufferedReader().use { it.readText() }
            val om = ObjectMapper()
            val node = om.readTree(jsonText)
            node.get("content").asText()
        }
    }


    override suspend fun getBiliVideo(): Result<List<BiliVideo>> = safeResultSync {
        withContext(Dispatchers.IO) {
            val timestamp = System.currentTimeMillis() / 1000
            val params = mutableMapOf(
                "vmid" to "161775300",
                "order" to "pubdate",
                "ps" to "3",
                "ts" to timestamp.toString()
            )
            val signedParams = appSign(params = params)
            val query = buildQuery(signedParams)

            val resp = safeApiCall(
                call = {
                    api.getBiliVideo(query = query, headers = createBiliHeader())
                }, errorMessage = "获取B站视频列表失败"
            )
            handleBiliResponse(resp)
        }
    }

    private fun handleBiliResponse(body: BiliResponse): List<BiliVideo> {
        if (body.code != 0) throw Exception(body.msg)
        val biliList = arrayListOf<BiliVideo>()
        val om = ObjectMapper()
        val tree = om.readTree(body.data.toString())
        val vList = tree["data"]["item"]
        for (v in vList) {
            biliList.add(
                BiliVideo(
                    v["cover"].asText(), v["bvid"].asText()
                )
            )
        }
        return biliList
    }


    private fun getValidFile(type: ConfigType): File {
        // 确定目标路径 (externalCacheDir)
        val targetFile = File(context.externalCacheDir, type.fileName)
        // 如果文件不存在，则从 Assets 自动拷贝（首次运行或被清理后）
        if (!targetFile.exists()) {
            try {
                context.assets.open(type.fileName).use { input ->
                    targetFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
                Timber.Forest.d("Success copied ${type.fileName} from assets to cache.")
            } catch (e: Exception) {
                Timber.Forest.e(e, "Failed to copy asset file: ${type.fileName}")
            }
        }
        return targetFile
    }

    //TODO：调用IO线程，或者改成= withContext(ioDispatcher)
    private suspend fun downloadConfig(fileName: String, link: String): Result<Unit> {
        return try {
            val targetFile = File(context.externalCacheDir, fileName)
            // 先下载到临时文件，下载成功后再重命名覆盖，防止下载中断导致文件损坏
            val tempFile = File(context.externalCacheDir, "${fileName}.tmp")

            URL(link).openStream().use { input ->
                tempFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            if (tempFile.exists()) {
                if (targetFile.exists()) targetFile.delete()
                tempFile.renameTo(targetFile)
                Timber.Forest.d("Download and updated $fileName success.")
                Result.success(Unit)
            } else {
                Result.failure(AppError.Business(null, "tmp file $fileName does not exists."))
            }
        } catch (e: Exception) {
            Timber.Forest.e(e, "Download config failed: $fileName")
            Result.failure(e.toAppError())
        }
    }

    override suspend fun getResourceDate(type: ConfigType): Result<String> = safeResultSync {
        try {
            val file = getValidFile(type)
            val node = readFileAsJsonNode(file.absolutePath)
            node["update"]["date"]?.asText() ?: "0"
        } catch (e: Exception) {
            Timber.Forest.e(e, "Read local date failed for ${type.fileName}")
            "0"
        }
    }

    /**
     * 获取本地 JSON 文件中的版本号
     */
    private suspend fun getLocalVersion(type: ConfigType): String = withContext(Dispatchers.IO) {
        try {
            val file = getValidFile(type)
            val node = readFileAsJsonNode(file.absolutePath)
            node["update"]["version"]?.asText() ?: "0"
        } catch (e: Exception) {
            Timber.Forest.e(e, "Read local version failed for ${type.fileName}")
            "0"
        }
    }

    private suspend fun fetchUpdateInfo(xmlUrl: String): ResourceUpdateInfo =
        withContext(Dispatchers.IO) {
            try {
                // 使用简化的网络请求流
                val connection = URL(xmlUrl).openConnection()
                val xmlText = connection.getInputStream().bufferedReader().use { it.readText() }
                xmlText.parseResourceXml()
            } catch (e: Exception) {
                Timber.Forest.e(e, "Fetch remote info failed: $xmlUrl")
                ResourceUpdateInfo() // 返回默认空值，避免崩溃
            }
        }

    private fun String.parseResourceXml(): ResourceUpdateInfo {
        val info = ResourceUpdateInfo()
        val parser = Xml.newPullParser().apply {
            setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
            setInput(StringReader(this@parseResourceXml))
        }

        var eventType = parser.eventType
        while (eventType != XmlPullParser.END_DOCUMENT) {
            if (eventType == XmlPullParser.START_TAG) {
                when (parser.name) {
                    "version" -> info.version = parser.nextText().toString()
                    "versionCode" -> info.versionCode = parser.nextText().toFloat()
                    "update" -> info.date = parser.nextText()
                    "link" -> info.link = parser.nextText()
                    "content" -> info.content = parser.nextText()
                }
            }
            eventType = parser.next()
        }
        return info
    }
}