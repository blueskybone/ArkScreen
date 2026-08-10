package com.blueskybone.arkscreen.data.repository

import com.blueskybone.arkscreen.data.local.pref.InnerPrefManager
import com.blueskybone.arkscreen.data.local.room.dao.AccountEfDao
import com.blueskybone.arkscreen.data.local.room.dao.AccountGcDao
import com.blueskybone.arkscreen.data.local.room.dao.AccountSkDao
import com.blueskybone.arkscreen.data.network.ApiService
import com.blueskybone.arkscreen.data.network.auth.HeaderProvider
import com.blueskybone.arkscreen.data.network.model.BasicInfoResponse
import com.blueskybone.arkscreen.data.network.model.BindingResponse
import com.blueskybone.arkscreen.data.network.safeApiCall
import com.blueskybone.arkscreen.data.repository.mapper.AccountMapper
import com.blueskybone.arkscreen.data.repository.mapper.AccountMapper.toEfEntities
import com.blueskybone.arkscreen.data.repository.mapper.AccountMapper.toGcEntities
import com.blueskybone.arkscreen.data.repository.mapper.AccountMapper.toSkEntities
import com.blueskybone.arkscreen.data.repository.utils.AccountCodec
import com.blueskybone.arkscreen.data.repository.utils.fetchCredInfo
import com.blueskybone.arkscreen.data.repository.utils.fetchToken
import com.blueskybone.arkscreen.data.repository.utils.safeResultSync
import com.blueskybone.arkscreen.data.repository.utils.safeResultNormal
import com.blueskybone.arkscreen.domain.model.account.AccountType
import com.blueskybone.arkscreen.domain.repository.AccountRepository
import com.blueskybone.arkscreen.util.generateDId
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import com.blueskybone.arkscreen.domain.model.account.Account as DomainAcc
import com.blueskybone.arkscreen.domain.model.account.AccountEf as DomainAccEf
import com.blueskybone.arkscreen.domain.model.account.AccountGc as DomainAccGc
import com.blueskybone.arkscreen.domain.model.account.AccountSk as DomainAccSk

class AccountRepositoryImpl(
    private val accountSkDao: AccountSkDao,
    private val accountGcDao: AccountGcDao,
    private val accountEfDao: AccountEfDao,
    private val api: ApiService,
    private val apiAk: ApiService,
    private val headerProvider: HeaderProvider = HeaderProvider,
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
                    //TODO:写一个Update方法，以后不允许登录多个账号了，只能更新当前的账号
                    accountSkDao.insert(list)
                    cnt += list.size
                    //TODO：放到domain部分
//                if (prefManager.baseAccountSk.get().uid == "")
//                    prefManager.baseAccountSk.set(list[0])
                }

                "endfield" -> {
                    val list = item.toEfEntities(token, dId)
                    accountEfDao.insert(list)
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
        accountGcDao.insert(account)
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
        }.catch { e ->
            emit(null)
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
        }.catch { e ->
            emit(null)
        }.flowOn(Dispatchers.IO) // 确保数据库操作在 IO 线程
    }

    /**
     * 密码登录森空岛账号
     * 参考 ZOOT 项目的登录流程
     */
    override suspend fun loginByPhonePassword(
        phone: String,
        code: String
    ): Result<Int> = safeResultSync {
        withContext(Dispatchers.IO) {
            val dId = generateDId()
            val token = fetchToken(phone, code, dId, headerProvider, api)
            val credInfo = fetchCredInfo(token, dId, headerProvider, api)
            val bindingResp = fetchPlayerBinding(credInfo.cred, credInfo.token, dId)
            handleBindingResponse(bindingResp, token, dId)
        }
    }

    override suspend fun loginByToken(token: String): Result<Int> = safeResultSync {
        withContext(Dispatchers.IO) {
            val dId = generateDId()
            val credInfo = fetchCredInfo(token, dId, headerProvider, api)
            val bindingResp = fetchPlayerBinding(credInfo.cred, credInfo.token, dId)
            handleBindingResponse(bindingResp, token, dId)
        }
    }

    override suspend fun loginOfficialWeb(
        token: String,
        akUserCenter: String,
        xrToken: String,
        channelMasterId: Int
    ): Result<Int> = safeResultSync {
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


    override fun setCurrentAccountGc(account: DomainAccGc): Result<Unit> = safeResultNormal {
        preference.currentAccountGcUid.set(account.uid)
    }


    override fun setCurrentAccountSk(account: DomainAccSk): Result<Unit> = safeResultNormal {
        preference.currentAccountSkUid.set(account.uid)
    }

    override suspend fun deleteAccount(account: DomainAcc): Result<Unit> = safeResultSync {
        //先识别一下类型。然后根据类型去删对应表单。
        when (account) {
            is DomainAccSk -> accountSkDao.deleteByUid(account.uid)
            is DomainAccGc -> accountGcDao.deleteByUid(account.uid)
            is DomainAccEf -> accountEfDao.deleteByUid(account.uid)
        }
    }
}
