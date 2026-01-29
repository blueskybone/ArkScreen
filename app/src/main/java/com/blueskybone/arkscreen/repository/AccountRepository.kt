package com.blueskybone.arkscreen.repository

import androidx.lifecycle.LiveData
import com.blueskybone.arkscreen.network.NetWorkTask.Companion.getSklandUserBinding
import com.blueskybone.arkscreen.network.RetrofitUtils.Companion.getBasicInfo
import com.blueskybone.arkscreen.network.RetrofitUtils.Companion.getErrorMessage
import com.blueskybone.arkscreen.network.RetrofitUtils.Companion.loginByPassword
import com.blueskybone.arkscreen.network.model.BasicInfoData
import com.blueskybone.arkscreen.network.model.BasicInfoResponse
import com.blueskybone.arkscreen.network.model.BindingItem
import com.blueskybone.arkscreen.network.model.BindingResponse
import com.blueskybone.arkscreen.preference.PrefManager
import com.blueskybone.arkscreen.room.Account
import com.blueskybone.arkscreen.room.AccountEf
import com.blueskybone.arkscreen.room.AccountGc
import com.blueskybone.arkscreen.room.AccountSk
import com.blueskybone.arkscreen.room.dao.AccountEfDao
import com.blueskybone.arkscreen.room.dao.AccountGcDao
import com.blueskybone.arkscreen.room.dao.AccountSkDao
import com.blueskybone.arkscreen.util.generateDId
import retrofit2.Response

class AccountRepository(
    private val accountSkDao: AccountSkDao,
    private val accountGcDao: AccountGcDao,
    private val accountEfDao: AccountEfDao,
    private val prefManager: PrefManager
) {

    val allSkAccounts: LiveData<List<AccountSk>> = accountSkDao.getAllLiveData()
    val allGcAccounts: LiveData<List<AccountGc>> = accountGcDao.getAllLiveData()
    val allEfAccounts: LiveData<List<AccountEf>> = accountEfDao.getAllLiveData()

    /*
    * 使用token登录森空岛，获取账号列表并存入数据库
    * */
    suspend fun syncAccountsByToken(token: String, dId: String): Int {
        try {

            val bindingResp = safeApiCall(
                call = { getSklandUserBinding(token, dId) },
                errorMessage = "获取账号列表失败"
            )
            return handleBindingResponse(bindingResp, token, dId)
        } catch (e: Exception) {
            throw Exception("登录失败：${e.message}")
        }
    }

    /**
     * 密码登录森空岛账号
     * 参考 ZOOT 项目的登录流程
     */
    suspend fun syncAccountsByPhone(phone: String, password: String): Int {
        try {
            val dId = generateDId()
            val loginData = safeApiCall(
                call = { loginByPassword(phone, password, dId) },
                errorMessage = "登录验证失败"
            )
            val token = loginData.data?.token ?: throw Exception("账号密码错误")
            val bindingResp = safeApiCall(
                call = { getSklandUserBinding(token, dId) },
                errorMessage = "获取账号列表失败"
            )
            return handleBindingResponse(bindingResp, token, dId)
        } catch (e: Exception) {
            throw Exception("登录失败：${e.message}")
        }
    }


    suspend fun loginGachaAccount(
        token: String,
        channelMasterId: Int,
        akUserCenter: String,
        xrToken: String
    ) {
        val infoResp = safeApiCall(
            call = { getBasicInfo(token, akUserCenter, xrToken) },
            errorMessage = "获取账号失败"
        )
        handleBasicResponse(infoResp, token, channelMasterId, akUserCenter, xrToken)
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
                    accountSkDao.insert(list)
                    cnt += list.size
                    if (prefManager.baseAccountSk.get().uid == "")
                        prefManager.baseAccountSk.set(list[0])
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

    private suspend fun handleBasicResponse(
        body: BasicInfoResponse,
        token: String, channelMasterId: Int, akUserCenter: String, xrToken: String
    ) {
        val account = body.data.toGcEntities(token, channelMasterId, akUserCenter, xrToken)
        accountGcDao.insert(account)
        if (prefManager.baseAccountGc.get().uid == "")
            prefManager.baseAccountGc.set(account)
    }


    private fun BindingItem.toSkEntities(token: String, dId: String): List<AccountSk> {
        return this.bindingList.map { user ->
            AccountSk(
                token = token, dId = dId,
                nickName = user.nickName,
                channelMasterId = user.channelMasterId,
                uid = user.uid,
                official = user.isOfficial
            )
        }
    }

    private fun BindingItem.toEfEntities(token: String, dId: String): List<AccountEf> {
        return this.bindingList.flatMap { user ->
            user.roles.map { role ->
                AccountEf(
                    token = token, dId = dId,
                    nickName = role.nickname,
                    channelMasterId = user.channelMasterId,
                    uid = user.uid, roleId = role.roleId,
                    serverId = role.serverId,
                    official = user.isOfficial
                )
            }
        }
    }

    private fun BasicInfoData.toGcEntities(
        token: String,
        channelMasterId: Int,
        akUserCenter: String,
        xrToken: String
    ): AccountGc {
        return this.let { item ->
            AccountGc(
                uid = item.uid,
                nickName = item.name,
                channelMasterId = item.channelId,
                token = token,
                official = channelMasterId == 1,
                akUserCenter = akUserCenter,
                xrToken = xrToken
            )
        }
    }

    suspend fun deleteAccount(account: Account) {
        when (account) {
            is AccountSk -> accountSkDao.delete(account.id)
            is AccountGc -> accountGcDao.delete(account.id)
            is AccountEf -> accountEfDao.delete(account.id)
        }
    }

    /**
     * 封装统一的网络请求处理逻辑
     * @param call 挂起函数，返回 Retrofit 的 Response
     * @param errorMessage 业务层定义的错误前缀
     * TODO： 扔到一个外部工具类里
     */
    private suspend fun <T> safeApiCall(
        call: suspend () -> Response<T>,
        errorMessage: String = "请求失败"
    ): T {
        val response = try {
            call()
        } catch (e: Exception) {
            throw Exception("$errorMessage: 网络连接异常")
        }

        if (response.isSuccessful) {
            // 成功，返回 Body，如果 Body 为空，抛出异常
            return response.body() ?: throw Exception("$errorMessage: 返回数据为空")
        } else {
            // HTTP 报错（401, 500等）
            throw Exception("$errorMessage: ${response.getErrorMessage()}")
        }
    }
}