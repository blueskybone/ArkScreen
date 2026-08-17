package com.blueskybone.arkscreen.data.network

import com.blueskybone.arkscreen.data.network.model.AttendanceEndfieldResponse
import com.blueskybone.arkscreen.data.network.model.AttendanceRequest
import com.blueskybone.arkscreen.data.network.model.AttendanceResponse
import com.blueskybone.arkscreen.data.network.model.BasicInfoResponse
import com.blueskybone.arkscreen.data.network.model.BiliResponse
import com.blueskybone.arkscreen.data.network.model.BindingResponse
import com.blueskybone.arkscreen.data.network.model.CredRequest
import com.blueskybone.arkscreen.data.network.model.CredResponse
import com.blueskybone.arkscreen.data.network.model.GachaCateResponse
import com.blueskybone.arkscreen.data.network.model.GachaResponse
import com.blueskybone.arkscreen.data.network.model.GrantRequest
import com.blueskybone.arkscreen.data.network.model.GrantResponse
import com.blueskybone.arkscreen.data.network.model.LoginRequest
import com.blueskybone.arkscreen.data.network.model.LoginResponse
import com.blueskybone.arkscreen.data.network.model.PlayerInfoResp
import com.fasterxml.jackson.databind.JsonNode
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.HeaderMap
import retrofit2.http.POST
import retrofit2.http.Query
import retrofit2.http.QueryMap
import retrofit2.http.Streaming

interface ApiService {
    @GET("/api/v1/game/player/info")
    suspend fun getServerTimestamp(): Response<JsonNode>

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
    suspend fun signAk(
        @Body request: AttendanceRequest,
        @HeaderMap headers: Map<String, String>
    ): Response<AttendanceResponse>

    @POST("/web/v1/game/endfield/attendance")
    suspend fun signEf(
        @HeaderMap headers: Map<String, String>
    ): Response<AttendanceEndfieldResponse>

//    // 获取玩家信息
//    @Streaming
//    @GET("/api/v1/game/player/info")
//    suspend fun getPlayerInfo(
//        @Query("uid") uid: String,
//        @HeaderMap headers: Map<String, String>
//    ): Response<ResponseBody>

    // 获取玩家基础信息 -- skland.com
    @Streaming
    @GET("/api/v1/game/player/info")
    suspend fun getPlayerInfo(
        @Query("uid") uid: String,
        @HeaderMap headers: Map<String, String>
    ): Response<PlayerInfoResp>

    // 获取玩家基础信息 -- ak.hyper
    @GET("/user/api/role/info")
    suspend fun getBasicInfo(
        @Query("source_from") source: String,
        @Query("share_type") type: String,
        @Query("share_by") by: String,
        @HeaderMap headers: Map<String, String>
    ): Response<BasicInfoResponse>

    // 获取卡池分类
    @GET("/user/api/inquiry/gacha/cate")
    suspend fun getGachaCate(
        @Query("uid") uid: String,
        @HeaderMap headers: Map<String, String>
    ):Response<GachaCateResponse>

    // 获取抽卡记录
    //size可以换20
    @GET("/user/api/inquiry/gacha/history")
    suspend fun getGachaRecords(
        @Query("uid") uid: String,
        @Query("category") category: String,
        @Query("size") size: Int = 10,
        @HeaderMap headers: Map<String, String>
    ): Response<GachaResponse>

    // 获取更多抽卡记录
    @GET("/user/api/inquiry/gacha/history")
    suspend fun getGachaRecordsMore(
        @Query("uid") uid: String,
        @Query("category") category: String,
        @Query("pos") pos: Int = 0,         //位移标记，pos=1，返回数据整体往更新的方向移动1。
        @Query("gachaTs") gachaTs: Long,
        @Query("size") size: Int = 10,
        @HeaderMap headers: Map<String, String>
    ): Response<GachaResponse>

    // 通过手机号和密码登录获取鹰角 token
    @POST("/user/auth/v1/token_by_phone_password")
    suspend fun loginByPassword(
        @Body request: LoginRequest,
        @HeaderMap headers: Map<String, String>
    ): Response<LoginResponse>

//    // 登出
//    @POST("/user/info/v1/logout")
//    suspend fun logout(
//        @Body request: LogoutRequest,
//        @HeaderMap headers: Map<String, String>
//    ): Response<LogoutResponse>

    @GET("/x/v2/space/archive/cursor")
    suspend fun getBiliVideo(
        @QueryMap query: Map<String, String>,
        @HeaderMap headers: Map<String, String>
    ): Response<BiliResponse>
}
