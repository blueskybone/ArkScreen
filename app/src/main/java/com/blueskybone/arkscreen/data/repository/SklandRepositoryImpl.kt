package com.blueskybone.arkscreen.data.repository

import com.blueskybone.arkscreen.data.local.pref.PrefManager
import com.blueskybone.arkscreen.data.network.ApiService
import com.blueskybone.arkscreen.data.network.auth.HeaderProvider
import com.blueskybone.arkscreen.data.network.model.AttendanceEndfieldResponse
import com.blueskybone.arkscreen.data.network.model.AttendanceRequest
import com.blueskybone.arkscreen.data.network.model.AttendanceResponse
import com.blueskybone.arkscreen.data.network.model.PlayerInfoResp
import com.blueskybone.arkscreen.data.network.safeApiCall
import com.blueskybone.arkscreen.data.repository.mapper.OperatorMapper
import com.blueskybone.arkscreen.data.repository.mapper.RealTimeMapper
import com.blueskybone.arkscreen.data.repository.utils.fetchCredInfo
import com.blueskybone.arkscreen.data.repository.utils.safeResultSync
import com.blueskybone.arkscreen.domain.model.account.AccountEf
import com.blueskybone.arkscreen.domain.model.account.AccountSk
import com.blueskybone.arkscreen.domain.model.cache.ApCache
import com.blueskybone.arkscreen.domain.model.cache.LaborCache
import com.blueskybone.arkscreen.domain.model.cache.MeetCache
import com.blueskybone.arkscreen.domain.model.cache.RecruitCache
import com.blueskybone.arkscreen.domain.model.cache.RefreshCache
import com.blueskybone.arkscreen.domain.model.cache.TrainCache
import com.blueskybone.arkscreen.domain.model.operator.Operator
import com.blueskybone.arkscreen.domain.model.realtime.RealTimeData
import com.blueskybone.arkscreen.domain.repository.SklandRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Created by blueskybone
 * Date: 2026/3/10
 */
class SklandRepositoryImpl(
    private val api: ApiService,
    private val headerProvider: HeaderProvider = HeaderProvider,
    private val prefManager: PrefManager
) : SklandRepository {


    private suspend fun fetchArkAttendance(
        cred: String,
        credToken: String,
        uid: String,
        channelMasterId: String,
        dId: String
    ): AttendanceResponse {
        return safeApiCall(
            call = {
                val headers = headerProvider.createSignHeaders(
                    "/api/v1/game/attendance",
                    cred,
                    credToken,
                    "{\"gameId\":$channelMasterId,\"uid\":\"$uid\"}",
                    dId
                )
                val request = AttendanceRequest(
                    channelMasterId.toInt(),
                    uid
                )
                api.signAk(request, headers)
            },
            errorMessage = "明日方舟森空岛签到失败"
        )
    }

    private fun handleAttendanceResp(resp: AttendanceResponse): String {
        return resp.data.awards.joinToString("  ") {
            "${it.resource.name}×${it.count}"
        }
    }

    //TODO:看一下具体的返回值
    private fun handleAttendanceEfResp(resp: AttendanceEndfieldResponse): String {
        return resp.data.awardIds.joinToString("  ") {
            "${it.id}×${it.type}"
        }
    }


    private suspend fun fetchEfAttendance(
        cred: String,
        credToken: String,
        roleId: String,
        serverId: String,
        dId: String
    ): AttendanceEndfieldResponse {
        return safeApiCall(
            call = {
                val headers = headerProvider.createEfSignHeaders(
                    "/web/v1/game/endfield/attendance",
                    cred,
                    credToken,
                    roleId,
                    serverId,
                    dId,
                )
                api.signEf(headers)
            },
            errorMessage = "终末地森空岛签到失败"
        )
    }


    suspend fun fetchGameData(
        cred: String,
        credToken: String,
        uid: String,
        dId: String
    ): PlayerInfoResp {
        return safeApiCall(
            call = {
                val headers = headerProvider.createSignHeaders(
                    "/api/v1/game/player/info",
                    cred, credToken, "uid=$uid", dId
                )
                api.getPlayerInfo(uid, headers)
            },
            errorMessage = "获取游戏数据失败"
        )
    }


    override suspend fun fetchRealTimeData(account: AccountSk): Result<RealTimeData> = safeResultSync {
        withContext(Dispatchers.IO) {
            val cred = fetchCredInfo(account.token, account.dId, headerProvider, api)
            val playerInfoResp = fetchGameData(cred.cred, cred.token, account.uid, account.dId)
            RealTimeMapper.toDomain(playerInfoResp)
        }
    }

    override suspend fun fetchCharAssets(account: AccountSk): Result<List<Operator>> = safeResultSync {
        withContext(Dispatchers.IO) {
            val cred = fetchCredInfo(account.token, account.dId, headerProvider, api)
            val playerInfoResp = fetchGameData(cred.cred, cred.token, account.uid, account.dId)
            OperatorMapper.toDomain(playerInfoResp)
        }
    }

    override suspend fun fetchAkCheckResult(account: AccountSk): Result<String> = safeResultSync {
        withContext(Dispatchers.IO) {
            val cred = fetchCredInfo(account.token, account.dId, headerProvider, api)
            val resp = fetchArkAttendance(
                cred.cred,
                cred.token,
                account.uid,
                account.channelMasterId,
                account.dId
            )
            handleAttendanceResp(resp)
        }
    }

    override suspend fun fetchEfCheckResult(account: AccountEf): Result<String> = safeResultSync {
        withContext(Dispatchers.IO) {
            val cred = fetchCredInfo(account.token, account.dId, headerProvider, api)
            val resp = fetchEfAttendance(
                cred.cred,
                cred.token,
                account.roleId,
                account.serverId,
                account.dId
            )
            handleAttendanceEfResp(resp)
        }
    }

    override fun setRealTimeCache(realTimeData: RealTimeData) {
        val apCache = ApCache(
            realTimeData.currentTs,
            realTimeData.apInfo.remainSecs,
            realTimeData.apInfo.recoverTime,
            realTimeData.apInfo.max,
            realTimeData.apInfo.current,
            false
        )
        val laborCache = LaborCache(
            realTimeData.currentTs,
            realTimeData.labor.remainSecs,
            realTimeData.labor.max,
            realTimeData.labor.current,
            false
        )
        val trainCache = TrainCache(
            realTimeData.currentTs,
            realTimeData.train.trainee,
            realTimeData.train.status,
            realTimeData.train.completeTime,
            realTimeData.train.isNull
        )
        val recruitCache = RecruitCache(
            realTimeData.currentTs,
            realTimeData.recruits.max,
            realTimeData.recruits.complete,
            realTimeData.recruits.completeTime,
            realTimeData.recruits.isNull
        )
        val refreshCache = RefreshCache(
            realTimeData.currentTs,
            realTimeData.hire.max,
            realTimeData.hire.count,
            realTimeData.hire.completeTime,
            realTimeData.hire.isNull,
        )

        val meetCache = MeetCache(
            realTimeData.currentTs,
            realTimeData.meeting.completeTime,
            realTimeData.meeting.status,
            realTimeData.meeting.isNull
        )

        prefManager.apCache.set(apCache)
        prefManager.laborCache.set(laborCache)
        prefManager.trainCache.set(trainCache)
        prefManager.recruitCache.set(recruitCache)
        prefManager.refreshCache.set(refreshCache)
        prefManager.meetCache.set(meetCache)
    }

    override fun getApCache(): ApCache {
        return prefManager.apCache.get()
    }

    override fun getTrainCache(): TrainCache {
        return prefManager.trainCache.get()
    }

    override fun getLaborCache(): LaborCache {
        return prefManager.laborCache.get()
    }

    override fun getRecruitCache(): RecruitCache {
        return prefManager.recruitCache.get()
    }

    override fun getRefreshCache(): RefreshCache {
        return prefManager.refreshCache.get()
    }

    override fun getMeetCache(): MeetCache {
        return prefManager.meetCache.get()
    }
}