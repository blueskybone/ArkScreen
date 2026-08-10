package com.blueskybone.arkscreen.legacy

/**
 *   Created by blueskybone
 *   Date: 2025/5/19
 */


/**
 *
 * 重构结束了，这个文件可以删了
 *
 * **/
//@Deprecated("重构结束后删除")
//class RetrofitUtils {
//    companion object {
//        private const val APP_CODE = "4ca99fa6b56cc2ba"
//
//        suspend fun loginByPassword(
//            phone: String,
//            password: String,
//            dId: String
//        ): Response<LoginResponse> {
//            val request = LoginRequest(phone, password)
//            val headers = createLoginHeaders().toMutableMap().apply {
//                put("dId", dId)
//                put("platform", "3")
//                put("vName", "1.0.0")
//            }
//            return RetrofitClient.hypergryphService.loginByPassword(
//                request,
//                headers
//            )
//        }
//
//        suspend fun getGrantByToken(token: String, dId: String): String {
//            val request = GrantRequest(appCode = APP_CODE, token = token, type = 0)
//            val headers = createLoginHeaders().toMutableMap().apply {
//                put("dId", dId)
//                put("platform", "3")
//                put("vName", "1.0.0")
//            }
//            val response = RetrofitClient.hypergryphService.getGrant(
//                request,
//                headers
//            )
//            return if (response.isSuccessful) {
//                response.body()?.data?.code ?: throw Exception("Empty response data")
//            } else {
//                throw Exception("API error: ${response.errorBody()?.string()}")
//            }
//        }
//
//        suspend fun getCredByGrant(grantCode: String, dId: String): CredAndToken {
//            val request = CredRequest(code = grantCode, kind = 1)
//            val headers = createLoginHeaders().toMutableMap().apply {
//                put("dId", dId)
//            }
//            val response = RetrofitClient.apiService.generateCredByCode(request, headers)
//            return if (response.isSuccessful) {
//                response.body()?.data?.let { data ->
//                    CredAndToken(data.cred, data.token)
//                } ?: throw Exception("Empty response data")
//            } else {
//                throw Exception("API error: ${response.errorBody()?.string()}")
//            }
//        }
//
//
//        suspend fun getPlayerBinding(
//            cred: String,
//            credToken: String,
//            dId: String
//        ): Response<BindingResponse> {
//            val api = "/api/v1/game/player/binding"
//            val ts = getCurrentTs().toString()
//            val sign = generateSign(api, "", credToken, ts, dId)
//            val headers = createSignHeaders(cred, sign, ts, dId)
//            return RetrofitClient.apiService.getPlayerBinding(headers)
//        }
//
//        suspend fun doAttendanceForArk(
//            cred: String,
//            credToken: String,
//            uid: String,
//            channelMasterId: String,
//            dId: String
//        ): Response<AttendanceResponse> {
//            val api = "/api/v1/game/attendance"
//            val ts = getCurrentTs().toString()
//            val json = "{\"gameId\":$channelMasterId,\"uid\":\"$uid\"}"
//            val sign = generateSign(api, json, credToken, ts, dId)
//            val headers = createSignHeaders(cred, sign, ts, dId)
//            return RetrofitClient.apiService.attendance(
//                AttendanceRequest(
//                    channelMasterId.toInt(),
//                    uid
//                ), headers
//            )
//        }
//
//        suspend fun doAttendanceForEndfield(
//            cred: String,
//            credToken: String,
//            dId: String,
//            roleId: String,
//            serverId: String
//        ): Response<AttendanceEndfieldResponse> {
//            val api = "/web/v1/game/endfield/attendance"
//            val ts = getCurrentTs().toString()
//            val sign = generateSign(api, "", credToken, ts, dId)
//            val headers = createSignHeaders(cred, sign, ts, dId).toMutableMap().apply {
//                put("sk-game-role", "3_${roleId}_${serverId}")
//                put("referer", "https://game.skland.com/")
//                put("origin", "https://game.skland.com/")
//            }
//            return RetrofitClient.apiService.attendanceEndfield(headers)
//        }
//
//
//        suspend fun getGachaCate(
//            accountGc: AccountGc
//        ): List<String> {
//            val response = RetrofitClient.akHypergryphService.getGachaCate(
//                accountGc.uid,
//                createAkHeader(accountGc.akUserCenter, accountGc.token, accountGc.xrToken)
//            )
//            return if (response.isSuccessful) {
//                response.body()?.data?.map { it.id } ?: emptyList()
//            } else {
//                throw Exception("API error: ${response.errorBody()?.string()}")
//            }
//        }
//
//        suspend fun getFirstPageRecords(
//            accountGc: AccountGc,
//            cate: String
//        ): GachaResponse {
//            val response = RetrofitClient.akHypergryphService.getGachaRecords(
//                uid = accountGc.uid,
//                category = cate,
//                size = 10,
//                createAkHeader(accountGc.akUserCenter, accountGc.token, accountGc.xrToken)
//            )
//            return if (response.isSuccessful) {
//                response.body() ?: throw Exception("API error: ${response.errorBody()?.string()}")
//            } else {
//                throw Exception("API error: ${response.errorBody()?.string()}")
//            }
//        }
//
//        suspend fun getMorePageRecords(
//            accountGc: AccountGc,
//            cate: String,
//            pos: Int,
//            ts: Long,
//        ): GachaResponse {
//            val response = RetrofitClient.akHypergryphService.getGachaRecordsMore(
//                uid = accountGc.uid,
//                category = cate,
//                pos = pos,
//                gachaTs = ts,
//                size = 10,
//                createAkHeader(accountGc.akUserCenter, accountGc.token, accountGc.xrToken)
//            )
//            return if (response.isSuccessful) {
//                response.body() ?: throw Exception("API error: ${response.errorBody()?.string()}")
//            } else {
//                throw Exception("API error: ${response.errorBody()?.string()}")
//            }
//        }
//
//
////        suspend fun getFirstPageRecords(
////            uid: String,
////            akUserCenter: String,
////            token: String,
////            xrToken: String,
////            cate: String
////        ): Response<GachaResponse> {
////            return RetrofitClient.akHypergryphService.getGachaRecords(
////                uid = uid,
////                category = cate,
////                size = 10,
////                createAkHeader(akUserCenter, token, xrToken)
////            )
////        }
//
////        suspend fun getMorePageRecords(
////            uid: String,
////            akUserCenter: String,
////            token: String,
////            xrToken: String,
////            cate: String,
////            pos: Int,
////            ts: Long,
////        ): Response<GachaResponse> {
////            return RetrofitClient.akHypergryphService.getGachaRecordsMore(
////                uid = uid,
////                category = cate,
////                pos = pos,
////                gachaTs = ts,
////                size = 10,
////                createAkHeader(akUserCenter, token, xrToken)
////            )
////        }
//
//        suspend fun doAttendance(
//            cred: String,
//            credToken: String,
//            uid: String,
//            channelMasterId: String,
//            dId: String
//        ): String {
//            val timeStamp = getCurrentTs().toString()
//            val jsonInputString = "{\"gameId\":$channelMasterId,\"uid\":\"$uid\"}"
//            val sign = generateSign(
//                "/api/v1/game/attendance",
//                jsonInputString,
//                credToken,
//                timeStamp,
//                dId
//            )
//            val headers = createSignHeaders(cred, sign, timeStamp, dId)
//            val response = RetrofitClient.apiService.attendance(
//                AttendanceRequest(
//                    channelMasterId.toInt(),
//                    uid
//                ), headers
//            )
//            return if (response.isSuccessful) {
//                try {
//                    response.body()?.data?.awards?.joinToString("  ") {
//                        "${it.resource.name}×${it.count}"
//                    } ?: "response content is empty"
//                } catch (e: Exception) {
//                    Timber.e(response.body().toString())
//                    "content error: can't analysis response body"
//                }
//            } else {
//                try {
//                    response.errorBody()?.string()?.let { errorBody ->
//                        getJsonContent(errorBody, "message")
//                    } ?: "API error: no error body"
//                } catch (e: Exception) {
//                    Timber.e(response.errorBody()?.string())
//                    "API error: ${response.errorBody()?.string()}"
//                }
//            }
//        }
//
//        suspend fun getGameInfo(
//            credAndToken: CredAndToken,
//            uid: String,
//            dId: String
//        ): Response<PlayerInfoResp> {
//            val timeStamp = getCurrentTs().toString()
//            val sign = generateSign(
//                "/api/v1/game/player/info",
//                "uid=$uid",
//                credAndToken.token,
//                timeStamp,
//                dId
//            )
//            val headers = createSignHeaders(credAndToken.cred, sign, timeStamp, dId)
//            return RetrofitClient.apiService.getPlayerInfoJson(
//                uid, headers
//            )
//        }
//
//        suspend fun getBasicInfo(
//            token: String,
//            akUserCenter: String,
//            xrToken: String
//        ): Response<BasicInfoResponse> {
//            return RetrofitClient.akHypergryphService.getBasicInfo(
//                "", "", "",
//                createAkHeader(akUserCenter, token, xrToken)
//            )
//        }
//
//        private fun createLoginHeaders(): Map<String, String> {
//            return mapOf(
//                "User-Agent" to "Skland/1.0.1 (com.hypergryph.skland; build:100001014; Android 31; ) Okhttp/4.11.0",
//                "Accept-Encoding" to "gzip",
//                "Connection" to "close",
//                "Content-Type" to "application/json"
//            )
//        }
//
//        private fun createAkHeader(
//            cookie: String,
//            token: String,
//            xrToken: String
//        ): Map<String, String> {
//            val baseHeaders = mutableMapOf(
//                "accept" to "application/json, text/plain, */*",
//                "accept-language" to "zh-CN,zh;q=0.9,en;q=0.8,en-US;q=0.7,zh-TW;q=0.6",
//                "cookie" to "ak-user-center=$cookie",
//                "referer" to "https://ak.hypergryph.com/user/headhunting",
//                "user-agent" to "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/138.0.0.0 Safari/537.36",
//                "x-role-token" to xrToken
//            )
//            if (token != "null") {
//                baseHeaders["x-account-token"] = token
//            }
//            return baseHeaders
//        }
//
//        private fun createSignHeaders(
//            cred: String,
//            sign: String,
//            timestamp: String,
//            dId: String = ""
//        ): Map<String, String> {
//            return mapOf(
//                "cred" to cred,
//                "User-Agent" to "Skland/1.0.1 (com.hypergryph.skland; build:100001014; Android 31; ) Okhttp/4.11.0",
////                "Accept-Encoding" to "gzip",
//                "Connection" to "close",
//                "Content-Type" to "application/json",
//                "sign" to sign,
//                "platform" to "",
//                "timestamp" to timestamp,
//                "dId" to dId,
//                "vName" to ""
//            )
//        }
//    }
//}