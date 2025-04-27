package com.blueskybone.arkscreen.network

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.DataOutputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import javax.net.ssl.HttpsURLConnection

/**
 *   Created by blueskybone
 *   Date: 2025/1/14
 */

class HttpConnectionUtils {
    companion object {
        private const val CONNECT_TIMEOUT = 5000 // 连接超时时间
        private const val READ_TIMEOUT = 5000 // 读取超时时间
        suspend fun httpResponse(
            url: URL,
            jsonInput: String?,
            header: Map<String, String>,
            method: RequestMethod
        ): Response {
            var httpsConn: HttpsURLConnection? = null
            return try {
                httpsConn = withContext(Dispatchers.IO) {
                    url.openConnection() as HttpsURLConnection
                }
                httpsConn.connectTimeout = CONNECT_TIMEOUT
                httpsConn.requestMethod = method.toString()

                header.forEach { (key, value) ->
                    httpsConn.setRequestProperty(key, value)
                }
                httpsConn.doInput = true
                httpsConn.doOutput = method == RequestMethod.POST

                jsonInput?.let {
                    DataOutputStream(httpsConn.outputStream).use { dataOs ->
                        withContext(Dispatchers.IO) {
                            dataOs.writeBytes(it)
                            dataOs.flush()
                        }
                    }
                }

                withContext(Dispatchers.IO) {
                    httpsConn.connect()
                }

                val respCode = httpsConn.responseCode
                val response = if (respCode == HttpURLConnection.HTTP_OK) {
                    val inputStream = httpsConn.inputStream
                    readStream(inputStream)
                } else {
                    val errorStream = httpsConn.errorStream
                    readStream(errorStream)
                }
                Response(respCode, response)
            } catch (e: Exception) {
                e.printStackTrace()
                val respCode = httpsConn?.responseCode ?: -1
                val errorStream = httpsConn?.errorStream
                val errorResponse = readStream(errorStream)
                Response(respCode, errorResponse)
            } finally {
                httpsConn?.disconnect()
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

