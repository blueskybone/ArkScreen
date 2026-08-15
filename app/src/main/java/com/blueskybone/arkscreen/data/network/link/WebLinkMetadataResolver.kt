package com.blueskybone.arkscreen.data.network.link

import com.blueskybone.arkscreen.data.common.HttpStatusException
import com.blueskybone.arkscreen.data.common.toAppError
import com.blueskybone.arkscreen.domain.service.LinkMetadataResolver
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.URL
import java.util.concurrent.TimeUnit

class WebLinkMetadataResolver(
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(5, TimeUnit.SECONDS)
        .callTimeout(8, TimeUnit.SECONDS)
        .followRedirects(true)
        .build(),
) : LinkMetadataResolver {

    override suspend fun resolveIcon(url: String): Result<String> = withContext(dispatcher) {
        try {
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", USER_AGENT)
                .build()
            val icon = client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    throw HttpStatusException(
                        response.code,
                        "网页请求失败：HTTP ${response.code}",
                    )
                }
                val body = response.body ?: return@use ""
                val source = body.source()
                source.request(MAX_HTML_BYTES)
                val byteCount = minOf(source.buffer.size, MAX_HTML_BYTES)
                val html = source.buffer.clone().readUtf8(byteCount)
                findIconUrl(response.request.url.toString(), html)
            }
            Result.success(icon)
        } catch (error: CancellationException) {
            throw error
        } catch (error: Exception) {
            Result.failure(error.toAppError())
        }
    }

    private fun findIconUrl(pageUrl: String, html: String): String {
        LINK_TAG.findAll(html).forEach { match ->
            val attributes = ATTRIBUTE.findAll(match.value).associate {
                val value = it.groups[2]?.value ?: it.groups[3]?.value
                    ?: it.groups[4]?.value.orEmpty()
                it.groupValues[1].lowercase() to value
            }
            val relValues = attributes["rel"]
                ?.lowercase()
                ?.split(Regex("\\s+"))
                .orEmpty()
            val href = attributes["href"]
            if (href != null && relValues.any { it == "icon" || it == "apple-touch-icon" }) {
                return URL(URL(pageUrl), href.replace("&amp;", "&")).toString()
            }
        }
        return URL(URL(pageUrl), "/favicon.ico").toString()
    }

    private companion object {
        const val MAX_HTML_BYTES = 512L * 1024L
        const val USER_AGENT =
            "Mozilla/5.0 (Linux; Android) AppleWebKit/537.36 Chrome/124 Mobile Safari/537.36"
        val LINK_TAG = Regex("""<link\b[^>]*>""", RegexOption.IGNORE_CASE)
        val ATTRIBUTE = Regex(
            """([:\w-]+)\s*=\s*(?:"([^"]*)"|'([^']*)'|([^\s"'=<>`]+))""",
            RegexOption.IGNORE_CASE,
        )
    }
}
