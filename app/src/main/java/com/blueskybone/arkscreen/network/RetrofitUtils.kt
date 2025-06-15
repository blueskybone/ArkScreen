package com.blueskybone.arkscreen.network

import com.blueskybone.arkscreen.network.model.AttendanceRequest
import com.blueskybone.arkscreen.network.model.BasicInfoRequest
import com.blueskybone.arkscreen.network.model.CredRequest
import com.blueskybone.arkscreen.network.model.GrantRequest
import com.blueskybone.arkscreen.room.AccountGc
import com.blueskybone.arkscreen.room.AccountSk
import com.blueskybone.arkscreen.room.Gacha
import com.blueskybone.arkscreen.util.SignUtils
import com.blueskybone.arkscreen.util.TimeUtils.getCurrentTs
import com.fasterxml.jackson.databind.ObjectMapper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.ResponseBody
import retrofit2.Response
import timber.log.Timber
import java.io.IOException
import java.net.URLEncoder

/**
 *   Created by blueskybone
 *   Date: 2025/5/19
 */

class RetrofitUtils {
    companion object {
        private const val app_code = "4ca99fa6b56cc2ba"

        suspend fun getGrantByToken(token: String): String {
            val request = GrantRequest(appCode = app_code, token = token, type = 0)
            val response = RetrofitClient.hypergryphService.getGrant(
                request,
                createLoginHeaders()
            )
            return if (response.isSuccessful) {
                response.body()?.data?.code ?: throw Exception("Empty response data")
            } else {
                throw Exception("API error: ${response.errorBody()?.string()}")
            }
        }

        suspend fun getCredByGrant(grantCode: String, dId: String): CredAndToken {
            val request = CredRequest(code = grantCode, kind = 1)
            val headers = createLoginHeaders().toMutableMap().apply {
                put("dId", dId)
            }
            val response = RetrofitClient.apiService.generateCredByCode(request, headers)
            return if (response.isSuccessful) {
                response.body()?.data?.let { data ->
                    CredAndToken(data.cred, data.token)
                } ?: throw Exception("Empty response data")
            } else {
                throw Exception("API error: ${response.errorBody()?.string()}")
            }
        }

        suspend fun createAccountSkList(
            cred: String,
            credToken: String,
            token: String,
            dId: String
        ): List<AccountSk> {
            val timeStamp = getCurrentTs().toString()
            val sign =
                SignUtils.generateSign("/api/v1/game/player/binding", "", credToken, timeStamp)
            val headers = createSignHeaders(cred, sign, timeStamp)
            val response = RetrofitClient.apiService.getPlayerBinding(headers)
            return if (response.isSuccessful) {
                Timber.i("getPlayerBinding response.isSuccessful")
                response.body()?.data?.list?.flatMap { item ->
                    if (item.appCode == "arknights") {
                        item.bindingList.map { user ->
                            AccountSk(
                                token = token,
                                dId = dId,
                                nickName = user.nickName,
                                channelMasterId = user.channelMasterId,
                                uid = user.uid,
                                official = user.isOfficial
                            )
                        }
                    } else {
                        emptyList()
                    }
                } ?: emptyList()
            } else {
                throw Exception("API error: ${response.errorBody()?.string()}")
            }
        }

        suspend fun getGachaRecords(
            page: Int,
            token: String,
            channelMasterId: Int,
            uid: String
        ): List<Gacha>? {
            val encodedToken = withContext(Dispatchers.IO) {
                URLEncoder.encode(token, "UTF-8")
            }
            val response = RetrofitClient.akHypergryphService.getGachaRecords(
                page = page,
                token = encodedToken,
                channelId = channelMasterId,
                headers = createNormalHeaders()
            )
            return if (response.isSuccessful) {
                response.body()?.data?.recordList?.map { item ->
                    Gacha(
                        uid = uid,
                        ts = item.ts,
                        pool = item.pool,
                        record = item.charsList.joinToString("@") { char ->
                            "${char.name}-${char.rarity}-${char.isNew}"
                        }
                    )
                }
            } else {
                throw Exception("API error: ${response.errorBody()?.string()}")
            }
        }

        suspend fun doAttendance(
            cred: String,
            credToken: String,
            uid: String,
            channelMasterId: String
        ): String {
            val timeStamp = getCurrentTs().toString()
            val jsonInputString = "{\"gameId\":$channelMasterId,\"uid\":\"$uid\"}"
            val sign = SignUtils.generateSign(
                "/api/v1/game/attendance",
                jsonInputString,
                credToken,
                timeStamp
            )
            val headers = createSignHeaders(cred, sign, timeStamp)
            val response = RetrofitClient.apiService.attendance(
                AttendanceRequest(
                    channelMasterId.toInt(),
                    uid
                ), headers
            )
            return if (response.isSuccessful) {
                //TODO:检查response header 是否存在
//                if (response.headers()["Content-Encoding"] == "gzip") {
//                    val unzipped =
//                        withContext(Dispatchers.IO) {
//                            GZIPInputStream(ByteArrayInputStream()).bufferedReader()
//                        }.readText()
//                    Response(
//                        responseCode = okHttpResponse.code,
//                        responseContent = unzipped
//                    )
//                }

                val builder = StringBuilder()
                response.body()?.data?.awards?.let { list ->
                    for (item in list) {
                        builder.append(item.resource.name).append("×").append(item.count)
                            .append("  ")
                    }
                    builder.toString()
                } ?: "response content is empty"
            } else {
                return "API error: ${response.errorBody()?.string()}"
            }
        }


        suspend fun getGameInfoConnection(
            credAndToken: CredAndToken,
            uid: String
        ): Response<ResponseBody> {
            val timeStamp = getCurrentTs().toString()
            val sign = SignUtils.generateSign(
                "/api/v1/game/player/info",
                "uid=$uid",
                credAndToken.token,
                timeStamp
            )
            val headers = createSignHeaders(credAndToken.cred, sign, timeStamp)
            return RetrofitClient.apiService.getPlayerInfo(
                uid, headers
            )
        }

        suspend fun getBasicInfo(channelMasterId: Int, token: String): AccountGc? {
            val requestBody: BasicInfoRequest = if (channelMasterId == 1) {
                BasicInfoRequest.OfficialRequest(1, channelMasterId, token)
            } else {
                BasicInfoRequest.BiliRequest(token)
            }
            val response = RetrofitClient.hypergryphService.getBasicInfo(
                requestBody,
                createLoginHeaders()
            )
            return if (response.isSuccessful) {
                response.body()?.data?.let { item ->
                    AccountGc(
                        uid = item.uid,
                        nickName = item.nickName,
                        channelMasterId = item.channelMasterId,
                        token = token,
                        official = channelMasterId == 1
                    )
                }
            } else {
                throw Exception("API error: ${response.errorBody()?.string()}")
            }
        }

        suspend fun getSpaceTitleImageUrl(): String {
            print("getSpaceTitleImageUrl")
            val client = OkHttpClient()
            val request = Request.Builder()
                .url(biliSettingUrl)
                .build()

            return withContext(Dispatchers.IO) {
                client.newCall(request).execute().use { response ->
                    print(response)
                    if (!response.isSuccessful) throw IOException("Unexpected code $response")
                    else {
                        try {
                            val node = ObjectMapper().readTree(response.body?.string())
                            titleImageUrl + node["data"]["toutu"]["s_img"]

                        } catch (e: Exception) {
                            throw e
                        }
                    }
                }
            }
        }

        private fun createNormalHeaders(): Map<String, String> {
            return mapOf(
                "User-Agent" to "Skland/1.0.1 (com.hypergryph.skland; build:100001014; Android 31; ) Okhttp/4.11.0",
                "Connection" to "close"
            )
        }

        private fun createLoginHeaders(): Map<String, String> {
            return mapOf(
                "User-Agent" to "Skland/1.0.1 (com.hypergryph.skland; build:100001014; Android 31; ) Okhttp/4.11.0",
                "Accept-Encoding" to "gzip",
                "Connection" to "close",
                "Content-Type" to "application/json"
            )
        }

        private fun createSignHeaders(
            cred: String,
            sign: String,
            timestamp: String,
        ): Map<String, String> {
            return mapOf(
                "cred" to cred,
                "User-Agent" to "Skland/1.0.1 (com.hypergryph.skland; build:100001014; Android 31; ) Okhttp/4.11.0",
                "Accept-Encoding" to "gzip",
                "Connection" to "close",
                "Content-Type" to "application/json",
                "sign" to sign,
                "platform" to "",
                "timestamp" to timestamp,
                "dId" to "",
                "vName" to ""
            )
        }
    }
}