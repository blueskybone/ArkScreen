package com.blueskybone.arkscreen.data.repository

import com.blueskybone.arkscreen.data.local.pref.CachePrefManager
import com.blueskybone.arkscreen.data.network.ApiService
import com.blueskybone.arkscreen.data.network.auth.HeaderProvider
import com.blueskybone.arkscreen.data.network.auth.SklandAuthRemoteDataSource
import com.blueskybone.arkscreen.data.network.model.AttendanceEndfieldResponse
import com.blueskybone.arkscreen.data.network.model.Awards
import com.fasterxml.jackson.databind.JsonNode
import com.blueskybone.arkscreen.data.network.model.AttendanceRequest
import com.blueskybone.arkscreen.data.network.model.AttendanceResponse
import com.blueskybone.arkscreen.data.network.model.PlayerInfoResp
import com.blueskybone.arkscreen.data.network.safeApiCall
import com.blueskybone.arkscreen.data.repository.mapper.OperatorMapper
import com.blueskybone.arkscreen.data.repository.mapper.RealTimeMapper
import com.blueskybone.arkscreen.data.common.repositoryResultOf
import com.blueskybone.arkscreen.domain.model.account.AccountEf
import com.blueskybone.arkscreen.domain.model.account.AccountSk
import com.blueskybone.arkscreen.domain.model.cache.ApCache
import com.blueskybone.arkscreen.domain.model.cache.CacheAccountInfo
import com.blueskybone.arkscreen.domain.model.cache.LaborCache
import com.blueskybone.arkscreen.domain.model.cache.MeetCache
import com.blueskybone.arkscreen.domain.model.cache.RecruitCache
import com.blueskybone.arkscreen.domain.model.cache.RefreshCache
import com.blueskybone.arkscreen.domain.model.cache.TrainCache
import com.blueskybone.arkscreen.domain.model.operator.Operator
import com.blueskybone.arkscreen.domain.model.operator.OperatorAssets
import com.blueskybone.arkscreen.domain.model.realtime.RealTimeData
import com.blueskybone.arkscreen.domain.repository.SklandRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.io.IOException
import java.net.SocketTimeoutException

/**
 * Created by blueskybone
 * Date: 2026/3/10
 */
class SklandRepositoryImpl(
    private val api: ApiService,
    private val headerProvider: HeaderProvider,
    private val authRemoteDataSource: SklandAuthRemoteDataSource,
    private val prefManager: CachePrefManager
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
        if (resp.code != 0) {
            if (isAlreadyAttended(resp.code, resp.message)) return ALREADY_ATTENDED_MESSAGE
            throw IllegalStateException(resp.message)
        }
        return resp.data.awards.joinToString("  ") {
            "${it.resource.name}×${it.count}"
        }
    }

    private fun handleAttendanceEfResp(resp: AttendanceEndfieldResponse): String {
        if (resp.code != 0) {
            if (isAlreadyAttended(resp.code, resp.message)) return ALREADY_ATTENDED_MESSAGE
            throw IllegalStateException(resp.message)
        }
        return resp.data.awardIds
            .groupBy { it.id to it.type }
            .values
            .joinToString("  ") { matchingAwards ->
                val award = matchingAwards.first()
                val resource = findEndfieldRewardNode(resp.data.resourceInfoMap, award)
                val name = extractRewardName(resource) ?: award.id
                val count = resource
                    ?.get("count")
                    ?.asInt()
                    ?.takeIf { it > 0 }
                    ?: matchingAwards.size
                "$name×$count"
            }
            .ifBlank { "签到成功" }
    }

    private fun isAlreadyAttended(code: Int?, message: String): Boolean {
        if (code == ALREADY_ATTENDED_CODE) return true
        val normalized = message.trim()
        return ALREADY_ATTENDED_MESSAGE_MARKERS.any(normalized::contains)
    }

    private fun findEndfieldRewardNode(resourceMap: JsonNode?, award: Awards): JsonNode? {
        if (resourceMap == null || resourceMap.isNull) return null
        val candidates = sequenceOf(
            resourceMap.path(award.id),
            resourceMap.path(award.type.toString()).path(award.id),
            resourceMap.path("${award.type}_${award.id}"),
        ) + listOfNotNull(findNodeByKey(resourceMap, award.id)).asSequence()

        return candidates
            .firstOrNull { node -> !node.isMissingNode && !node.isNull }
    }

    private fun findNodeByKey(node: JsonNode, key: String): JsonNode? {
        if (!node.isContainerNode) return null
        node.get(key)?.let { return it }
        return node.elements().asSequence()
            .mapNotNull { child -> findNodeByKey(child, key) }
            .firstOrNull()
    }

    private fun extractRewardName(node: JsonNode?): String? {
        if (node == null) return null
        if (node.isTextual) return node.asText().takeIf { it.isNotBlank() }
        return sequenceOf("name", "itemName", "displayName")
            .mapNotNull { field -> node.get(field)?.asText() }
            .firstOrNull { it.isNotBlank() }
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


    override suspend fun fetchRealTimeData(account: AccountSk): Result<RealTimeData> =
        repositoryResultOf {
            withContext(Dispatchers.IO) {
                val cred = authRemoteDataSource.fetchCredential(account.token, account.dId)
                val playerInfoResp = fetchGameData(cred.cred, cred.token, account.uid, account.dId)
                RealTimeMapper.toDomain(playerInfoResp)
            }
        }

    override suspend fun fetchCharAssets(account: AccountSk): Result<OperatorAssets> =
        repositoryResultOf {
            withContext(Dispatchers.IO) {
                val cred = authRemoteDataSource.fetchCredential(account.token, account.dId)
                val playerInfoResp = fetchGameData(cred.cred, cred.token, account.uid, account.dId)
                OperatorAssets(
                    operators = OperatorMapper.toDomain(playerInfoResp),
                    avatar = RealTimeMapper.toDomain(playerInfoResp).avatar,
                )
            }
        }

    override suspend fun fetchAkCheckResult(account: AccountSk): Result<String> = repositoryResultOf {
        withContext(Dispatchers.IO) {
            retryTransientAttendance {
                val cred = authRemoteDataSource.fetchCredential(account.token, account.dId)
                try {
                    val resp = fetchArkAttendance(
                        cred.cred,
                        cred.token,
                        account.uid,
                        account.channelMasterId,
                        account.dId
                    )
                    handleAttendanceResp(resp)
                } catch (error: Exception) {
                    if (isAlreadyAttended(null, error.message.orEmpty())) {
                        ALREADY_ATTENDED_MESSAGE
                    } else {
                        throw error
                    }
                }
            }
        }
    }

    override suspend fun fetchEfCheckResult(account: AccountEf): Result<String> = repositoryResultOf {
        withContext(Dispatchers.IO) {
            retryTransientAttendance {
                val cred = authRemoteDataSource.fetchCredential(account.token, account.dId)
                try {
                    val resp = fetchEfAttendance(
                        cred.cred,
                        cred.token,
                        account.roleId,
                        account.serverId,
                        account.dId
                    )
                    handleAttendanceEfResp(resp)
                } catch (error: Exception) {
                    // Endfield reports an already completed attendance as HTTP 403 with
                    // business code 10001, so safeApiCall throws before the body handler.
                    if (isAlreadyAttended(null, error.message.orEmpty())) {
                        ALREADY_ATTENDED_MESSAGE
                    } else {
                        throw error
                    }
                }
            }
        }
    }

    private suspend fun <T> retryTransientAttendance(block: suspend () -> T): T {
        var lastError: Exception? = null
        repeat(ATTENDANCE_MAX_ATTEMPTS) { attempt ->
            try {
                return block()
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                lastError = error
                if (attempt == ATTENDANCE_MAX_ATTEMPTS - 1 || !error.isTransientNetworkError()) {
                    throw error
                }
                delay(ATTENDANCE_RETRY_DELAY_MS)
            }
        }
        throw checkNotNull(lastError)
    }

    private fun Throwable.isTransientNetworkError(): Boolean {
        val hasNetworkCause = generateSequence(this) { it.cause }
            .any { it is SocketTimeoutException || it is IOException }
        if (hasNetworkCause) return true
        val normalized = message.orEmpty().lowercase()
        return normalized.contains("timeout") ||
            normalized.contains("socket closed") ||
            normalized.contains("socket is closed")
    }

    override fun setRealTimeCache(account: AccountSk, realTimeData: RealTimeData) {
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
        // Written last: consumers only switch the displayed owner after the snapshot is complete.
        prefManager.accountInfo.set(
            CacheAccountInfo(
                uid = account.uid,
                nickname = account.nickName,
                official = account.official,
            )
        )
    }

    override fun getApCache(): ApCache {
        return prefManager.apCache.get()
    }

    override fun getCacheAccountInfo(): CacheAccountInfo = prefManager.accountInfo.get()

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

    private companion object {
        const val ATTENDANCE_MAX_ATTEMPTS = 2
        const val ATTENDANCE_RETRY_DELAY_MS = 300L
        const val ALREADY_ATTENDED_CODE = 10001
        const val ALREADY_ATTENDED_MESSAGE = "\u4eca\u65e5\u5df2\u7b7e\u5230"

        val ALREADY_ATTENDED_MESSAGE_MARKERS = listOf(
            "\u8bf7\u52ff\u91cd\u590d\u7b7e\u5230",
            "\u4eca\u65e5\u5df2\u7b7e\u5230",
            "\u5df2\u7ecf\u7b7e\u5230",
        )
    }
}
