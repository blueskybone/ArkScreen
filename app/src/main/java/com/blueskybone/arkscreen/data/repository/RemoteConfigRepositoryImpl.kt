package com.blueskybone.arkscreen.data.repository

import com.blueskybone.arkscreen.domain.model.AppRemoteConfig
import com.blueskybone.arkscreen.domain.repository.RemoteConfigRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

class RemoteConfigRepositoryImpl(
    private val parser: AppRemoteConfigParser,
    private val dispatcher: CoroutineDispatcher,
) : RemoteConfigRepository {

    override suspend fun fetchAppConfig(): Result<AppRemoteConfig> = runCatching {
        withContext(dispatcher) {
            val connection = (URL(APP_CONFIG_URL).openConnection() as HttpURLConnection).apply {
                connectTimeout = REQUEST_TIMEOUT_MS
                readTimeout = REQUEST_TIMEOUT_MS
                requestMethod = "GET"
                useCaches = false
            }
            try {
                if (connection.responseCode !in 200..299) {
                    throw IllegalStateException("远端配置请求失败：${connection.responseCode}")
                }
                connection.inputStream.bufferedReader().use { reader ->
                    parser.parse(reader.readText())
                }
            } finally {
                connection.disconnect()
            }
        }
    }

    private companion object {
        const val APP_CONFIG_URL =
            "https://gitee.com/blueskybone/ArkScreen/raw/master/resource/app_config.json"
        const val REQUEST_TIMEOUT_MS = 5_000
    }
}
