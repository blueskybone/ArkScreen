package com.blueskybone.arkscreen.network

import com.blueskybone.arkscreen.network.model.AttendanceRequest
import com.blueskybone.arkscreen.network.model.AttendanceResponse
import com.blueskybone.arkscreen.network.model.BasicInfoRequest
import com.blueskybone.arkscreen.network.model.BasicInfoResponse
import com.blueskybone.arkscreen.network.model.BindingResponse
import com.blueskybone.arkscreen.network.model.CredRequest
import com.blueskybone.arkscreen.network.model.CredResponse
import com.blueskybone.arkscreen.network.model.GachaResponse
import com.blueskybone.arkscreen.network.model.GrantRequest
import com.blueskybone.arkscreen.network.model.GrantResponse
import com.blueskybone.arkscreen.network.model.PlayerInfoResp
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.HeaderMap
import retrofit2.http.POST
import retrofit2.http.Query
import retrofit2.http.Streaming

interface ApiService {
    // 获取凭证
    @POST("/api/v1/user/auth/generate_cred_by_code")
    suspend fun generateCredByCode(
        @Body request: CredRequest,
        @HeaderMap headers: Map<String, String>
    ): Response<CredResponse>

    // 获取授权码
    @POST("/user/oauth2/v2/grant")
    suspend fun getGrant(
        @Body request: GrantRequest,
        @HeaderMap headers: Map<String, String>
    ): Response<GrantResponse>

    // 获取绑定账号列表
    @GET("/api/v1/game/player/binding")
    suspend fun getPlayerBinding(
        @HeaderMap headers: Map<String, String>
    ): Response<BindingResponse>

    // 签到
    @POST("/api/v1/game/attendance")
    suspend fun attendance(
        @Body request: AttendanceRequest,
        @HeaderMap headers: Map<String, String>
    ): Response<AttendanceResponse>

    // 获取玩家信息
    @Streaming
    @GET("/api/v1/game/player/info")
    suspend fun getPlayerInfo(
        @Query("uid") uid: String,
        @HeaderMap headers: Map<String, String>
    ): Response<ResponseBody>


    @Streaming
    @GET("/api/v1/game/player/info")
    suspend fun getPlayerInfoJson(
        @Query("uid") uid: String,
        @HeaderMap headers: Map<String, String>
    ): Response<PlayerInfoResp>


    // 获取基础信息
    @POST("/u8/user/info/v1/basic")
    suspend fun getBasicInfo(
        @Body request: BasicInfoRequest,
        @HeaderMap headers: Map<String, String>
    ): Response<BasicInfoResponse>

    // 获取抽卡记录
    @GET("/user/api/inquiry/gacha")
    suspend fun getGachaRecords(
        @Query("page") page: Int,
        @Query("token") token: String,
        @Query("channelId") channelId: Int,
        @HeaderMap headers: Map<String, String>
    ): Response<GachaResponse>

//    // 登出
//    @POST("/user/info/v1/logout")
//    suspend fun logout(
//        @Body request: LogoutRequest,
//        @HeaderMap headers: Map<String, String>
//    ): Response<LogoutResponse>
}