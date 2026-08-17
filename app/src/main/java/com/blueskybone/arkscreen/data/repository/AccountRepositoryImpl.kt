package com.blueskybone.arkscreen.data.repository

import com.blueskybone.arkscreen.data.local.pref.InnerPrefManager
import com.blueskybone.arkscreen.data.local.room.dao.AccountEfDao
import com.blueskybone.arkscreen.data.local.room.dao.AccountGcDao
import com.blueskybone.arkscreen.data.local.room.dao.AccountSkDao
import com.blueskybone.arkscreen.data.network.ApiService
import com.blueskybone.arkscreen.data.network.auth.HeaderProvider
import com.blueskybone.arkscreen.data.network.auth.SklandAuthRemoteDataSource
import com.blueskybone.arkscreen.data.network.model.BasicInfoResponse
import com.blueskybone.arkscreen.data.network.model.BindingResponse
import com.blueskybone.arkscreen.data.network.safeApiCall
import com.blueskybone.arkscreen.data.repository.mapper.AccountMapper
import com.blueskybone.arkscreen.data.repository.mapper.AccountMapper.toEfEntities
import com.blueskybone.arkscreen.data.repository.mapper.AccountMapper.toGcEntities
import com.blueskybone.arkscreen.data.repository.mapper.AccountMapper.toSkEntities
import com.blueskybone.arkscreen.data.repository.utils.AccountCodec
import com.blueskybone.arkscreen.data.common.repositoryResult
import com.blueskybone.arkscreen.data.common.repositoryResultOf
import com.blueskybone.arkscreen.domain.model.account.AccountType
import com.blueskybone.arkscreen.domain.repository.AccountRepository
import com.blueskybone.arkscreen.data.repository.utils.generateDId
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.retryWhen
import kotlinx.coroutines.withContext
import timber.log.Timber
import com.blueskybone.arkscreen.domain.model.account.Account as DomainAcc
import com.blueskybone.arkscreen.domain.model.account.AccountEf as DomainAccEf
import com.blueskybone.arkscreen.domain.model.account.AccountGc as DomainAccGc
import com.blueskybone.arkscreen.domain.model.account.AccountSk as DomainAccSk

class AccountRepositoryImpl(
    private val accountSkDao: AccountSkDao,
    private val accountGcDao: AccountGcDao,
    private val accountEfDao: AccountEfDao,
    private val api: ApiService,
    private val apiAk : ApiService,
    private val headerProvider: HeaderProvider,
    private val authRemoteDataSource: SklandAuthRemoteDataSource,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
    private val preference: InnerPrefManager
) : AccountRepository {

    private val currentSkAccIdFlow: Flow<String?> = preference.currentAccountSkUid.flow()
    private val currentGcAccIdFlow: Flow<String?> = preference.currentAccountGcUid.flow()

    private suspend fun fetchPlayerBinding(
        cred: String,
        credToken: String,
        dId: String
    ): BindingResponse {
        return safeApiCall(
            call = {
                val headers = headerProvider.createSignHeaders(
                    "/api/v1/game/player/binding",
                    cred,
                    credToken,
                    "",
                    dId
                )
                api.getPlayerBinding(headers)
            },
            errorMessage = "获取角色列表失败"
        )
    }

    /* 卡池账号基础信息相关接口 */
    suspend fun fetchBasicInfo(
        token: String,
        akUserCenter: String,
        xrToken: String
    ): BasicInfoResponse {
        return safeApiCall(
            call = {
                val headers = headerProvider.createAkHeader(akUserCenter, token, xrToken)
                apiAk.getBasicInfo("", "", "", headers)
            },
            errorMessage = "获取官网账号信息失败"
        )
    }

    private suspend fun handleBindingResponse(
        body: BindingResponse,
        token: String,
        dId: String
    ): Int {
        var cnt = 0
        if (body.code != 0) throw Exception(body.message)
        body.data.list.forEach { item ->
            when (item.appCode) {
                "arknights" -> {
                    val list = item.toSkEntities(token, dId)
                    accountSkDao.upsert(list)
                    cnt += list.size
                }

                "endfield" -> {
                    val list = item.toEfEntities(token, dId)
                    accountEfDao.upsert(list)
                    cnt += list.size
                }
            }
        }
        return cnt
    }

    private suspend fun handleBindingResponse(
        body: BasicInfoResponse,
        token: String,
        akUserCenter: String,
        xrToken: String,
        channelMasterId: Int
    ): Int {
        val cnt = 1
        if (body.code != 0) throw Exception(body.msg)
        val account = body.data.toGcEntities(
            token,
            channelMasterId = channelMasterId,
            akUserCenter = akUserCenter,
            xrToken = xrToken,
        )
        accountGcDao.upsert(account)
        return cnt
    }

    override fun observeSkAcc(): Flow<List<DomainAccSk>> {
        //拿到数据库的acc，一个mapper传递回去
        return accountSkDao.getAllFlowData()
            .map { accounts ->
                accounts.map { account ->
                    AccountMapper.toDomain(account)
                }
            }
            .flowOn(dispatcher)
    }

    override fun observeGcAcc(): Flow<List<DomainAccGc>> {
        return accountGcDao.getAllFlowData()
            .map { accounts ->
                accounts.map { account ->
                    AccountMapper.toDomain(account)
                }
            }
            .flowOn(dispatcher)
    }

    override fun observeEfAcc(): Flow<List<DomainAccEf>> {
        return accountEfDao.getAllFlowData()
            .map { accounts ->
                accounts.map { account ->
                    AccountMapper.toDomain(account)
                }
            }
            .flowOn(dispatcher)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun observeCurrentSkAcc(): Flow<DomainAccSk?> {
        return currentSkAccIdFlow.flatMapLatest { accId ->
            when {
                accId.isNullOrEmpty() -> flowOf(null)
                else -> accountSkDao.getAccountFlowByUid(accId)
                    .map { entity ->
                        entity?.let { AccountMapper.toDomain(it) }
                    }
            }
        }.retryWhen { error, attempt ->
            Timber.e(error, "Failed to observe current Skland account; retrying")
            delay(accountFlowRetryDelay(attempt))
            true
        }.flowOn(Dispatchers.IO) // 确保数据库操作在 IO 线程
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun observeCurrentGcAcc(): Flow<DomainAccGc?> {
        return currentGcAccIdFlow.flatMapLatest { accId ->
            when {
                accId.isNullOrEmpty() -> flowOf(null)
                else -> accountGcDao.getAccountFlowByUid(accId)
                    .map { entity ->
                        entity?.let { AccountMapper.toDomain(it) }
                    }
            }
        }.retryWhen { error, attempt ->
            Timber.e(error, "Failed to observe current gacha account; retrying")
            delay(accountFlowRetryDelay(attempt))
            true
        }.flowOn(Dispatchers.IO) // 确保数据库操作在 IO 线程
    }

    /**
     * 密码登录森空岛账号
     * 参考 ZOOT 项目的登录流程
     */
    override suspend fun loginByPhonePassword(
        phone: String,
        code: String
    ): Result<Int> = repositoryResultOf {
        withContext(Dispatchers.IO) {
            val dId = generateDId()
            val token = authRemoteDataSource.loginByPassword(phone, code, dId)
            val credInfo = authRemoteDataSource.fetchCredential(token, dId)
            val bindingResp = fetchPlayerBinding(credInfo.cred, credInfo.token, dId)
            handleBindingResponse(bindingResp, token, dId)
        }
    }

    override suspend fun loginByToken(token: String, dId: String?): Result<Int> = repositoryResultOf {
        withContext(Dispatchers.IO) {
            val resolvedDId = dId?.takeIf(String::isNotBlank) ?: generateDId()
            val credInfo =
                authRemoteDataSource.fetchCredential(token, resolvedDId)
            val bindingResp =
                fetchPlayerBinding(credInfo.cred, credInfo.token, resolvedDId)
            handleBindingResponse(bindingResp, token, resolvedDId)
        }
    }

    override suspend fun loginOfficialWeb(
        token: String,
        akUserCenter: String,
        xrToken: String,
        channelMasterId: Int
    ): Result<Int> = repositoryResultOf {
        withContext(Dispatchers.IO) {
            val resp = fetchBasicInfo(token, akUserCenter, xrToken)
            handleBindingResponse(resp, token, akUserCenter, xrToken, channelMasterId)
        }
    }

    override suspend fun loadAccountFromCookie(
        accountType: AccountType,
        cookieStr: String
    ): Result<Int> {
        return when (accountType) {
            AccountType.SK -> {
                val credential = AccountCodec.decodeSk(cookieStr)
                loginByToken(credential.token)
            }

            AccountType.GC -> {
                val credential = AccountCodec.decodeGc(cookieStr)
                loginOfficialWeb(
                    credential.token,
                    credential.akUserCenter,
                    credential.xrToken,
                    credential.channelMasterId
                )
            }

            AccountType.EF -> {
                val credential = AccountCodec.decodeEf(cookieStr)
                loginByToken(credential.token)
            }
        }
    }


    override fun accountCookieEncode(account: DomainAcc): String {
        return when (account) {
            is DomainAccSk -> AccountCodec.encodeSk(account)
            is DomainAccGc -> AccountCodec.encodeGc(account)
            is DomainAccEf -> AccountCodec.encodeEf(account)
            else -> throw IllegalArgumentException("错误的账号类型")
        }
    }


    override fun setCurrentAccountGc(account: DomainAccGc): Result<Unit> = repositoryResult {
        preference.currentAccountGcUid.set(account.uid)
    }


    override fun setCurrentAccountSk(account: DomainAccSk): Result<Unit> = repositoryResult {
        preference.currentAccountSkUid.set(account.uid)
    }

    private fun accountFlowRetryDelay(attempt: Long): Long =
        ACCOUNT_FLOW_RETRY_DELAY_MS * (attempt + 1).coerceAtMost(
            ACCOUNT_FLOW_MAX_RETRY_DELAY_MS / ACCOUNT_FLOW_RETRY_DELAY_MS
        )

    override suspend fun deleteAccount(account: DomainAcc): Result<Unit> = repositoryResultOf {
        //先识别一下类型。然后根据类型去删对应表单。
        when (account) {
            is DomainAccSk -> accountSkDao.deleteByUid(account.uid)
            is DomainAccGc -> accountGcDao.deleteByUid(account.uid)
            is DomainAccEf -> accountEfDao.deleteByUid(account.uid)
        }
    }

    private companion object {
        const val ACCOUNT_FLOW_RETRY_DELAY_MS = 1_000L
        const val ACCOUNT_FLOW_MAX_RETRY_DELAY_MS = 30_000L
    }
}
