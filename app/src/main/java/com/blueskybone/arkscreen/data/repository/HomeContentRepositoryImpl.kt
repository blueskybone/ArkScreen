package com.blueskybone.arkscreen.data.repository

import com.blueskybone.arkscreen.data.common.HttpStatusException
import com.blueskybone.arkscreen.data.network.ApiService
import com.blueskybone.arkscreen.data.network.safeApiCall
import com.blueskybone.arkscreen.data.repository.utils.appSign
import com.blueskybone.arkscreen.data.repository.utils.createBiliHeader
import com.blueskybone.arkscreen.data.common.repositoryResultOf
import com.blueskybone.arkscreen.domain.model.BiliVideo
import com.blueskybone.arkscreen.domain.repository.HomeContentRepository
import com.fasterxml.jackson.databind.ObjectMapper
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

class HomeContentRepositoryImpl(
    private val api: ApiService,
    private val objectMapper: ObjectMapper,
    private val dispatcher: CoroutineDispatcher,
) : HomeContentRepository {
    override suspend fun fetchAnnouncement(): Result<String> = repositoryResultOf {
        withContext(dispatcher) {
            val connection = URL(ANNOUNCEMENT_URL).openConnection() as HttpURLConnection
            try {
                val statusCode = connection.responseCode
                if (statusCode !in 200..299) {
                    throw HttpStatusException(statusCode, "公告请求失败：HTTP $statusCode")
                }
                connection.inputStream.bufferedReader().use { reader ->
                    objectMapper.readTree(reader)["content"]?.asText()
                        ?: throw IllegalStateException("公告内容为空")
                }
            } finally {
                connection.disconnect()
            }
        }
    }

    override suspend fun fetchBiliVideos(): Result<List<BiliVideo>> = repositoryResultOf {
        withContext(dispatcher) {
            val params = mutableMapOf(
                "vmid" to "161775300",
                "order" to "pubdate",
                "ps" to "3",
                "ts" to (System.currentTimeMillis() / 1000).toString(),
            )
            val response = safeApiCall(
                call = {
                    api.getBiliVideo(
                        query = appSign(params),
                        headers = createBiliHeader(),
                    )
                },
                errorMessage = "获取B站视频列表失败",
            )
            if (response.code != 0) {
                throw IllegalStateException(response.msg.ifBlank { "B站接口返回错误：${response.code}" })
            }
            response.data?.item
                ?.map { video -> BiliVideo(video.cover, video.bvid) }
                ?: throw IllegalStateException("B站视频列表为空")
        }
    }

    private companion object {
        const val ANNOUNCEMENT_URL =
            "https://gitee.com/blueskybone/ArkScreen/raw/master/resource/announce.json"
    }
}
