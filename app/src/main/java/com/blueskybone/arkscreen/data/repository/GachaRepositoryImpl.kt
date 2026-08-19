package com.blueskybone.arkscreen.data.repository

import com.blueskybone.arkscreen.data.local.room.AccountGc
import com.blueskybone.arkscreen.data.local.room.Gacha
import com.blueskybone.arkscreen.data.local.room.dao.GachaDao
import com.blueskybone.arkscreen.data.network.ApiService
import com.blueskybone.arkscreen.data.network.auth.HeaderProvider
import com.blueskybone.arkscreen.data.network.model.GachaResponse
import com.blueskybone.arkscreen.data.network.safeApiCall
import com.blueskybone.arkscreen.data.repository.mapper.AccountMapper
import com.blueskybone.arkscreen.data.repository.mapper.GachaMapper
import com.blueskybone.arkscreen.data.common.repositoryResultOf
import com.blueskybone.arkscreen.domain.model.account.Account
import com.blueskybone.arkscreen.domain.model.gacha.Record
import com.blueskybone.arkscreen.domain.repository.GachaRepository
import com.blueskybone.arkscreen.domain.repository.GachaImportResult
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import com.blueskybone.arkscreen.domain.model.account.AccountGc as DomainAccGc

/**
 * Created by blueskybone
 * Date: 2026/1/29
 */

class GachaRepositoryImpl(
    private val gachaDao: GachaDao,
    private val api: ApiService,
    private val headerProvider: HeaderProvider,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) : GachaRepository {

    private fun String.toCate(): String = when {
        startsWith("LIMITED") || startsWith("LINKAGE") || startsWith("ATTAIN") -> "LIMITED"
        startsWith("CLASSIC") || startsWith("FESCLASSIC") -> "CLASSIC"
        startsWith("SINGLE") || startsWith("DOUBLE") || startsWith("SPECIAL") ||
            startsWith("NORM") -> "NORMAL"
        else -> "UN"
    }

    private fun AccountGc.toHeaders() = headerProvider.createAkHeader(
        akUserCenter, token, xrToken
    )

    suspend fun fetchFirstPageRecords(account: AccountGc, cate: String, size: Int = 10) =
        safeApiCall(
            call = { api.getGachaRecords(account.uid, cate, size, account.toHeaders()) },
            errorMessage = "获取第一页寻访记录失败"
        )

    suspend fun fetchMorePageRecords(
        account: AccountGc,
        cate: String,
        pos: Int,
        ts: Long,
        size: Int = 10
    ): GachaResponse {
        return safeApiCall(
            call = {
                api.getGachaRecordsMore(
                    account.uid,
                    cate,
                    pos,
                    ts,
                    size,
                    account.toHeaders()
                )
            },
            errorMessage = "获取更多寻访记录失败"
        )
    }

    override fun observeRecords(uid: String): Flow<List<Record>> {
        return gachaDao.getFLowByUid(uid)
            .map { gachas ->
                gachas.map { gacha ->
                    GachaMapper.toDomain(gacha)
                }
            }.flowOn(dispatcher)
    }

    override suspend fun importRecords(
        account: DomainAccGc,
        records: List<Record>
    ): Result<GachaImportResult> = repositoryResultOf {
        withContext(dispatcher) {
            val importGachas = records.map { record ->
                GachaMapper.toEntity(account.uid, record)
            }
            val inserted = gachaDao.importIgnoringConflicts(importGachas)
            GachaImportResult(total = importGachas.size, inserted = inserted)
        }
    }

    override suspend fun deleteRecords(account: DomainAccGc): Result<Unit> = repositoryResultOf {
        gachaDao.deleteByUid(account.uid)
    }

    override suspend fun deleteRecordsByUid(uid: String): Result<Unit> = repositoryResultOf {
        gachaDao.deleteByUid(uid)
    }

    override suspend fun fixGachaCate(): Result<Unit> = repositoryResultOf {
        val gachaList = gachaDao.getByCate("UN")
        val updatedRecords = gachaList.map { gachaEntity ->
            gachaEntity.copy(
                poolCate = gachaEntity.poolId.toCate(),  // 尝试修复卡池
            )
        }
        gachaDao.updateGachas(updatedRecords)
    }

    override suspend fun syncRecords(account: DomainAccGc): Result<Unit> = repositoryResultOf {
        withContext(Dispatchers.IO) {
            val acc = AccountMapper.toEntity(account)
            val localRecords = gachaDao.getByUid(acc.uid)
            //获取最后一条历史时间避免每次pull全部数据
            val lastTs = localRecords.maxOfOrNull { it.ts } ?: 0L
            //获取卡池名称
            val cateList = fetchGachaCate(acc)
            //然后去请求远端
            val records = mutableListOf<Gacha>()
            cateList.forEach { cate ->
                var resp = fetchFirstPageRecords(acc, cate)
                while (true) {
                    val list = resp.data.list.map {
                        Gacha(
                            poolId = it.poolId,
                            poolCate = it.poolId.toCate(),
                            uid = acc.uid,
                            ts = it.gachaTs,
                            pool = it.poolName,
                            charName = it.charName,
                            charId = it.charId,
                            rarity = it.rarity,
                            isNew = it.isNew,
                            pos = it.pos
                        )
                    }
                    records.addAll(list)
                    if (list.isEmpty()) break
                    if (list.last().ts < lastTs) break
                    if (!resp.data.hasMore) break
                    resp = fetchMorePageRecords(acc, cate, list.last().pos, list.last().ts)
                }
            }
            records.toList()
            gachaDao.insert(records)
        }
    }

    override suspend fun correctUnCateRecord(account: Account): Result<Int> {
        return repositoryResultOf {
            val records = gachaDao.getByUid(account.uid)
            val corrected = records
                .filter { it.poolCate == "UN" }
                .mapNotNull { record ->
                    record.poolId.toCate()
                        .takeIf { it != "UN" }
                        ?.let { record.copy(poolCate = it) }
                }
            if (corrected.isNotEmpty()) {
                gachaDao.updateGachas(corrected)
            }
            corrected.size
        }
    }

    private suspend fun fetchGachaCate(account: AccountGc): List<String> {
        val resp = safeApiCall(
            call = {
                val headers = headerProvider.createAkHeader(
                    account.akUserCenter,
                    account.token,
                    account.xrToken
                )
                api.getGachaCate(account.uid, headers)
            },
            errorMessage = "获取卡池类型失败"
        )
        return resp.data.map { it.id }
    }
}
