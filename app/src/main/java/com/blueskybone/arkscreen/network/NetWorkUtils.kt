package com.blueskybone.arkscreen.network

import com.blueskybone.arkscreen.network.HttpConnectionUtils.Companion.httpResponse
import com.blueskybone.arkscreen.network.HttpConnectionUtils.Companion.httpResponseConnection
import com.blueskybone.arkscreen.room.AccountGc
import com.blueskybone.arkscreen.room.AccountSk
import com.blueskybone.arkscreen.room.Gacha
import com.blueskybone.arkscreen.util.SignUtils
import com.blueskybone.arkscreen.util.TimeUtils.getCurrentTs
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import javax.net.ssl.HttpsURLConnection

/**
 *   Created by blueskybone
 *   Date: 2025/1/14
 */
class NetWorkUtils {
    open class CredAndToken(val cred: String, val token: String)

    companion object {
        private const val app_code = "4ca99fa6b56cc2ba"
        private const val user_agent =
            "Skland/1.0.1 (com.hypergryph.skland; build:100001014; Android 31; ) Okhttp/4.11.0"

        private const val skland_url = "https://zonai.skland.com"
        private const val cred_code_api = "/api/v1/user/auth/generate_cred_by_code"
        private const val game_info_api = "/api/v1/game/player/info"
        private const val binding_api = "/api/v1/game/player/binding"
        private const val sign_api = "/api/v1/game/attendance"

        private const val as_hyper_url = "https://as.hypergryph.com"
        private const val basic_api = "/u8/user/info/v1/basic"
        private const val grant_api = "/user/oauth2/v2/grant"
        private const val logout_api = "/user/info/v1/logout"

        private const val ak_hyper_url = "https://ak.hypergryph.com"
        private const val gacha_api = "/user/api/inquiry/gacha"

        private const val resource_url =
            "https://gitee.com/blueskybone/ArkScreen/raw/master/resource/"
        private const val pool_api = "pool_type.json"
        private const val announce_api = "announce.json"


        private val headerNormal: HashMap<String, String> = object : HashMap<String, String>() {
            init {
                put("User-Agent", user_agent)
                put("Connection", "close")
            }
        }

        private val headerLogin: HashMap<String, String> = object : HashMap<String, String>() {
            init {
                put("User-Agent", user_agent)
                put("Accept-Encoding", "gzip")
                put("Connection", "close")
                put("Content-Type", "application/json")
                put("dId", "")
            }
        }

        private val headerSign: MutableMap<String, String> = mutableMapOf(
            "cred" to "",
            "User-Agent" to user_agent,
            "Accept-Encoding" to "gzip",
            "Connection" to "close",
            "Content-Type" to "application/json",
            "sign" to "",
            "platform" to "",
            "timestamp" to "",
            "dId" to "",
            "vName" to ""
        )

        suspend fun getPoolType(): List<String> {
            val url = URL(resource_url + pool_api)
            val resp = httpResponse(url, null, headerNormal, RequestMethod.GET)
            val poolList = mutableListOf<String>()
            return if (resp.responseCode == HttpURLConnection.HTTP_OK) {
                try {
                    val list = ObjectMapper().readTree(resp.responseContent).at("/fes")
                    for (item in list) poolList.add(item.asText())
                    poolList
                } catch (e: Exception) {
                    e.printStackTrace()
                    poolList
                }
            } else {
                poolList
            }
        }

        suspend fun getAnnounce(): String {
            val url = URL(resource_url + announce_api)
            println(url)
            val resp = httpResponse(url, null, headerNormal, RequestMethod.GET)
            return if (resp.responseCode == HttpURLConnection.HTTP_OK) {
                try {
                    val content = ObjectMapper().readTree(resp.responseContent).at("/content")
                    return content.asText()
                } catch (e: Exception) {
                    e.printStackTrace()
                    "json formats wrong in getAnnounce() "
                }
            } else {
                "公告获取失败: code " + resp.responseCode
            }
        }

        suspend fun getGachaRecords(
            page: Int,
            token: String,
            channelMasterId: Int,
            uid: String
        ): List<Gacha>? {
            val tokenU = withContext(Dispatchers.IO) {
                URLEncoder.encode(token, "UTF-8")
            }
            val url =
                URL("$ak_hyper_url$gacha_api?page=${page}&token=${tokenU}&channelId=${channelMasterId}")
            val resp = httpResponse(url, null, headerNormal, RequestMethod.GET)
            if (resp.responseCode == HttpURLConnection.HTTP_OK) {
                try {
                    val list = ObjectMapper().readTree(resp.responseContent).at("/data/list")
                    if (list.isEmpty) {
                        return null
                    } else {
                        val gachaList = mutableListOf<Gacha>()
                        for (item in list) {
                            val ts = item["ts"].asLong()
                            val pool = item["pool"].asText()
                            val chars = item["chars"]
                            val recordStr = StringBuilder()
                            for (char in chars) {
                                recordStr.append(char["name"].asText()).append("-")
                                    .append(char["rarity"]).append("-")
                                    .append(char["isNew"]).append("@")
                            }
                            recordStr.deleteCharAt(recordStr.length - 1)
                            gachaList.add(
                                Gacha(
                                    uid = uid,
                                    ts = ts,
                                    pool = pool,
                                    record = recordStr.toString()
                                )
                            )
                        }
                        return gachaList
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    throw Exception("json formats wrong in getGachaRecords()")
                }
            } else {
                try {
                    val msg = getJsonContent(resp.responseContent, "msg") ?: "message null"
                    throw Exception(msg)
                } catch (e: Exception) {
                    throw Exception("json get message fault in getGachaRecords()")
                }
            }
        }

        suspend fun logOutByToken(token: String): Boolean {
            val url = URL(as_hyper_url + logout_api)
            val jsonInputString = "{\"token\":\"$token\"}"
            val resp: Response =
                httpResponse(url, jsonInputString, headerLogin, RequestMethod.POST)
            return getJsonContent(resp.responseContent, "msg") == "OK"
        }

        suspend fun getGrantByToken(token: String): String {
            val url = URL(as_hyper_url + grant_api)
            val jsonInputString =
                "{\"appCode\":\"$app_code\", \"token\":\"$token\", \"type\":0}"
            val resp = httpResponse(url, jsonInputString, headerLogin, RequestMethod.POST)
            if (resp.responseCode == HttpURLConnection.HTTP_OK) {
                try {
                    val tree = ObjectMapper().readTree(resp.responseContent)
                    return tree.at("/data/code").asText()
                } catch (e: Exception) {
                    throw Exception("json format wrong :" + resp.responseContent)
                }
            } else {
                throw Exception("api error: " + resp.responseContent)
            }
        }

        suspend fun getCredByGrant(grantCode: String, dId: String): CredAndToken {
            val url = URL(skland_url + cred_code_api)
            val jsonInputString = "{\"code\":\"$grantCode\", \"kind\":1}"
            headerLogin["dId"] = dId
            val resp = httpResponse(url, jsonInputString, headerLogin, RequestMethod.POST)
            if (resp.responseCode == HttpURLConnection.HTTP_OK) {
                try {
                    val tree = ObjectMapper().readTree(resp.responseContent)
                    val cred = tree.at("/data/cred").asText()
                    val token = tree.at("/data/token").asText()
                    return CredAndToken(cred, token)
                } catch (e: Exception) {
                    throw Exception("json format wrong :" + resp.responseContent)
                }
            } else {
                throw Exception("api error: " + resp.responseContent)
            }
        }

        //理论上应该放在Task层
        suspend fun createAccountSkList(
            cred: String,
            credToken: String,
            token: String,
            dId: String
        ): List<AccountSk> {
            val url = URL(skland_url + binding_api)
            val timeStamp = getCurrentTs().toString()
            val sign = SignUtils.generateSign(binding_api, "", credToken, timeStamp)
            headerSign["cred"] = cred
            headerSign["sign"] = sign
            headerSign["timestamp"] = timeStamp
            val resp = httpResponse(url, null, headerSign, RequestMethod.GET)
            if (resp.responseCode == HttpURLConnection.HTTP_OK) {
                try {
                    val list = ObjectMapper().readTree(resp.responseContent).at("/data/list")
                    return generateAccountSkList(list, token, dId)
                } catch (e: Exception) {
                    throw Exception("json format wrong :" + resp.responseContent)
                }
            } else {
                throw Exception("api error: " + resp.responseContent)
            }
        }

        private fun generateAccountSkList(
            node: JsonNode,
            token: String,
            dId: String
        ): List<AccountSk> {
            val accountSkList: MutableList<AccountSk> = ArrayList()
            for (item in node) {
                if (item["appCode"].asText() == "arknights") {
                    val bindingList = item["bindingList"]
                    for (user in bindingList) {
                        val account = AccountSk(
                            token = token,
                            dId = dId,
                            nickName = user["nickName"].asText(),
                            channelMasterId = user["channelMasterId"].asText(),
                            uid = user["uid"].asText(),
                            official = user["isOfficial"].asBoolean()
                        )
                        accountSkList.add(account)
                    }
                }
            }
            return accountSkList
        }

        private fun getJsonContent(jsonStr: String?, key: String?): String? {
            return if (jsonStr == null) {
                null
            } else try {
                val om = ObjectMapper()
                val tree = om.readTree(jsonStr)
                val keys = tree.findValues(key)
                keys[0].asText()
            } catch (e: java.lang.Exception) {
                null
            }
        }

        suspend fun getServerTs(): Long? {
            val url = URL(skland_url + game_info_api)
            val resp = httpResponse(url, null, headerLogin, RequestMethod.GET)
            return try {
                val om = ObjectMapper()
                val node = om.readTree(resp.responseContent)
                node["timestamp"].asLong()
            } catch (e: java.lang.Exception) {
                throw Exception(e.message)
            }
        }

        suspend fun logAttendance(
            cred: String,
            credToken: String,
            uid: String,
            channelMasterId: String
        ): String {
            val url = URL(skland_url + sign_api)
            val timeStamp = getCurrentTs().toString()
            val jsonInputString = "{\"gameId\": $channelMasterId, \"uid\": \"$uid\"}"
            val sign = SignUtils.generateSign(sign_api, jsonInputString, credToken, timeStamp)
            headerSign["cred"] = cred
            headerSign["sign"] = sign
            headerSign["timestamp"] = timeStamp
            val resp = httpResponse(url, jsonInputString, headerSign, RequestMethod.POST)
            return if (resp.responseCode == HttpURLConnection.HTTP_OK) {
                try {
                    val list = ObjectMapper().readTree(resp.responseContent).at("/data/awards")
                    val awards = StringBuilder()
                    for (item in list) {
                        awards.append(item["resource"]["name"].asText() + "×" + item["count"].asInt() + " ")
                    }
                    awards.toString()
                } catch (e: Exception) {
                    e.printStackTrace()
                    throw Exception("json formats wrong in logAttendance()")
                }
            } else {
                getJsonContent(resp.responseContent, "message") ?: "message null"
            }
        }

        suspend fun getBasicInfo(channelMasterId: Int, token: String): AccountGc {
            val url = URL(as_hyper_url + basic_api)
            val jsonInputString =
                if (channelMasterId == 1) {
                    """{"appId":1,"channelMasterId":1,"channelToken":{"token":"$token"}}"""
                } else {
                    """{"token":"$token"}"""
                }

            val resp = httpResponse(url, jsonInputString, headerLogin, RequestMethod.POST)
            if (resp.responseCode == HttpURLConnection.HTTP_OK) {
                try {
                    val node = ObjectMapper().readTree(resp.responseContent).at("/data")
                    return AccountGc(
                        uid = node["uid"].asText(),
                        channelMasterId = node["channelMasterId"].asInt(),
                        nickName = node["nickName"].asText(),
                        token = token,
                        official = channelMasterId == 1
                    )
                } catch (e: Exception) {
                    throw Exception("json format wrong :" + resp.responseContent)
                }
            } else {
                throw Exception("api error: " + resp.responseContent)
            }
        }

        suspend fun getGameInfoConnection(
            credAndToken: CredAndToken,
            uid: String
        ): HttpsURLConnection {
            val url = URL("$skland_url$game_info_api?uid=$uid")
            val timeStamp = getCurrentTs().toString()
            val sign =
                SignUtils.generateSign(game_info_api, "uid=$uid", credAndToken.token, timeStamp)
            headerSign["cred"] = credAndToken.cred
            headerSign["sign"] = sign
            headerSign["timestamp"] = timeStamp
            return httpResponseConnection(url, headerSign, RequestMethod.GET)
        }

        suspend fun getCharsAllConnection(
            credAndToken: CredAndToken,
            uid: String
        ): HttpsURLConnection {
            val url = URL("$skland_url$game_info_api?uid=$uid")
            val timeStamp = getCurrentTs().toString()
            val sign =
                SignUtils.generateSign(game_info_api, "uid=$uid", credAndToken.token, timeStamp)
            headerSign["cred"] = credAndToken.cred
            headerSign["sign"] = sign
            headerSign["timestamp"] = timeStamp
            return httpResponseConnection(url, headerSign, RequestMethod.GET)
        }

        suspend fun getSpaceTitleImageUrl(): String {
            val resp = httpResponse(URL(biliSettingUrl), null, headerNormal, RequestMethod.GET)
            if (resp.responseCode == HttpURLConnection.HTTP_OK) {
                try {
                    val path = titleImageUrl + getJsonContent(resp.responseContent, "s_img")
                    println(path)
                    return path
                } catch (e: Exception) {
                    throw Exception("json format wrong :" + resp.responseContent)
                }
            } else {
                throw Exception("api error: " + resp.responseContent)
            }
        }
    }
}