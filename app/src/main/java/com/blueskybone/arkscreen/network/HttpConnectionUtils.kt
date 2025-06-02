package com.blueskybone.arkscreen.network

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.ByteArrayInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.Charset
import java.util.concurrent.TimeUnit
import java.util.zip.GZIPInputStream
import javax.net.ssl.HttpsURLConnection

/**
 *   Created by blueskybone
 *   Date: 2025/1/14
 */

class HttpConnectionUtils {
    companion object {
        private const val CONNECT_TIMEOUT = 5000 // 连接超时时间
        private const val READ_TIMEOUT = 5000 // 读取超时时间

        private val okHttpClient:OkHttpClient = OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS) // 替换你的 CONNECT_TIMEOUT
            .readTimeout(15, TimeUnit.SECONDS)
            .build()

        suspend fun httpResponse(
            url: URL,
            jsonInput: String?,
            header: Map<String, String>,
            method: RequestMethod
        ): Response {
            return try {

                println("=== HTTP 请求信息 ===")
                println("URL: $url")
                println("Method: $method")
                println("Headers: $header")
                println("Request Body: $jsonInput")


                val request = Request.Builder()
                    .url(url)
                    .apply {
                        when (method) {
                            RequestMethod.GET -> get()
                            RequestMethod.POST -> {
                                val body =
                                    jsonInput?.toRequestBody("application/json".toMediaType())
                                post(body ?: "".toRequestBody(null))
                            }
                        }
                        header.forEach { (key, value) ->
                            addHeader(key, value)
                        }
                    }
                    .build()


                val okHttpResponse = withContext(Dispatchers.IO) {
                    okHttpClient.newCall(request).execute()
                }


                println("\n=== HTTP 响应信息 ===")
                println("Response Code: ${okHttpResponse.code}")
                println("Response Message: ${okHttpResponse.message}")
                println("Response Headers:")
                okHttpResponse.headers.forEach { (name, value) ->
                    println("  $name: $value")
                }


                val responseBytes = okHttpResponse.body?.bytes() ?: byteArrayOf()
                println(
                    "\nResponse Raw Bytes (Hex): ${
                        responseBytes.joinToString(" ") {
                            "%02x".format(
                                it
                            )
                        }
                    }"
                )
                println("Response Raw Bytes (Length): ${responseBytes.size} bytes")

                if (okHttpResponse.header("Content-Encoding") == "gzip") {
                    val unzipped =
                        withContext(Dispatchers.IO) {
                            GZIPInputStream(ByteArrayInputStream(responseBytes)).bufferedReader()
                        }.readText()
                    Response(
                        responseCode = okHttpResponse.code,
                        responseContent = unzipped
                    )
                }else{
                    val responseBodyString = try {
                        String(responseBytes, Charset.forName("UTF-8"))
                    } catch (e: Exception) {
                        "无法将响应体解码为UTF-8字符串: ${e.message}"
                    }
                    Response(
                        responseCode = okHttpResponse.code,
                        responseContent = responseBodyString
                    )
                }

            } catch (e: Exception) {
                println("\n=== 请求发生异常 ===")
                println("异常类型: ${e.javaClass.name}")
                println("异常信息: ${e.message}")
                e.printStackTrace()
                Response(responseCode = -1, responseContent = "请求失败: ${e.message}")
            }
        }

        private fun readStream(inputStream: InputStream?): String {
            return inputStream?.bufferedReader()?.use { it.readText() } ?: "No Response"
        }

        suspend fun httpResponseConnection(
            url: URL,
            header: Map<String, String>? = null,
            method: RequestMethod
        ): HttpsURLConnection {
            val httpsConn = withContext(Dispatchers.IO) {
                url.openConnection()
            } as HttpsURLConnection
            httpsConn.apply {
                requestMethod = method.toString()
                connectTimeout = CONNECT_TIMEOUT
                readTimeout = READ_TIMEOUT
                doOutput = method == RequestMethod.POST
                doInput = true
                instanceFollowRedirects = true
            }

            // 设置请求头
            header?.forEach { (key, value) ->
                httpsConn.setRequestProperty(key, value)
            }

            return try {
                withContext(Dispatchers.IO) {
                    httpsConn.connect()
                }
                if (httpsConn.responseCode == HttpURLConnection.HTTP_OK) {
                    httpsConn
                } else {
                    throw HttpException("HTTP error: ${httpsConn.responseCode} - ${httpsConn.responseMessage}")
                }
            } catch (e: Exception) {
                httpsConn.disconnect()
                throw HttpException("Failed to connect: ${e.message}", e)
            }
        }

        private fun getResponseStream(url: URL): InputStream? {
            return try {
                val httpsConn = url.openConnection() as HttpsURLConnection
                httpsConn.connectTimeout = 5000
                httpsConn.requestMethod = "GET"
                httpsConn.doInput = true
                httpsConn.doOutput = false
                httpsConn.instanceFollowRedirects = true
                httpsConn.connect()
                if (httpsConn.responseCode == HttpURLConnection.HTTP_OK) {
                    val isr = httpsConn.inputStream
                    isr
                } else {
                    httpsConn.disconnect()
                    throw Exception("connect error" + httpsConn.responseMessage)
                }
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
                throw Exception(e.message)
            }
        }

        fun downloadToLocal(localPath: String, url: URL) {
            val isr = getResponseStream(
                url
            ) ?: return
            var index: Int
            val bytes = ByteArray(1024)
            val downloadFile = FileOutputStream(localPath)
            while (isr.read(bytes).also { index = it } != -1) {
                downloadFile.write(bytes, 0, index)
                downloadFile.flush()
            }
            isr.close()
            downloadFile.close()
        }

    }

    class HttpException(message: String, cause: Throwable? = null) : Exception(message, cause)
}

//deprecated
//        fun writeToLocal(destination: String?, input: InputStream) {
//            var index: Int
//            val bytes = ByteArray(1024)
//            val downloadFile = FileOutputStream(destination)
//            while (input.read(bytes).also { index = it } != -1) {
//                downloadFile.write(bytes, 0, index)
//                downloadFile.flush()
//            }
//            input.close()
//            downloadFile.close()
//        }

