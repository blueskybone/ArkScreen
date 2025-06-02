package com.blueskybone.arkscreen.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.blueskybone.arkscreen.APP
import com.blueskybone.arkscreen.AppUpdate
import com.blueskybone.arkscreen.network.NetWorkTask.Companion.createAccountList
import com.blueskybone.arkscreen.network.NetWorkTask.Companion.createGachaAccount
import com.blueskybone.arkscreen.network.NetWorkTask.Companion.sklandAttendance
import com.blueskybone.arkscreen.network.announceUrl
import com.blueskybone.arkscreen.preference.PrefManager
import com.blueskybone.arkscreen.room.AccountGc
import com.blueskybone.arkscreen.room.AccountSk
import com.blueskybone.arkscreen.room.ApCache
import com.blueskybone.arkscreen.room.ArkDatabase
import com.blueskybone.arkscreen.room.Link
import com.fasterxml.jackson.databind.ObjectMapper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.koin.java.KoinJavaComponent.getKoin

/**
 *   Created by blueskybone
 *   Date: 2025/1/4
 */
class BaseModel : ViewModel() {
    private val prefManager: PrefManager by getKoin().inject()

    private val database = ArkDatabase.getDatabase(APP)
    private val accountSkDao = database.getAccountSkDao()
    private val accountGcDao = database.getAccountGcDao()
    private val linkDao = database.getLinkDao()

    private val _accountSkList = MutableLiveData<List<AccountSk>>()
    val accountSkList: LiveData<List<AccountSk>> get() = _accountSkList

    private val _links = MutableLiveData<List<Link>>()
    val links: LiveData<List<Link>> get() = _links

    private val _currentAccount = MutableLiveData<AccountSk?>()
    val currentAccount: LiveData<AccountSk?> get() = _currentAccount

    private val _accountGcList = MutableLiveData<List<AccountGc>>()
    val accountGcList: LiveData<List<AccountGc>> get() = _accountGcList

    private val _appUpdateInfo = MutableLiveData<AppUpdate.AppUpdateInfo>()
    val appUpdateInfo: LiveData<AppUpdate.AppUpdateInfo> get() = _appUpdateInfo

    private val _apCache = MutableLiveData<ApCache>()
    val apCache: LiveData<ApCache> get() = _apCache

    private val _testText = MutableLiveData<String>()
    val testText: LiveData<String> get() = _testText

    private val _announce = MutableLiveData<String>()
    val announce: LiveData<String> get() = _announce

    init {
        initialize()
        checkAppUpdate()
        insertLinkData()
        checkAnnounce()
    }

    private fun loadApCache() {
        executeAsync {
            _apCache.postValue(prefManager.apCache.get())
        }
    }

    private var job: Job? = null
    fun testTextRun() {
        job?.cancel()
        job = viewModelScope.launch(Dispatchers.IO) {
            val builder = StringBuilder()
            accountSkList.value?.let { list ->
                builder.append("开始任务：\n")
                for (account in list) {
                    if (account.uid != "") {
                        builder.append(account.nickName).append(" 签到中...\n")
                        _testText.postValue(builder.toString())
                        builder.append(attendance(account)).append("\n")
                        _testText.postValue(builder.toString())
                    }
                }
                builder.append("签到完成。\n")
                _testText.postValue(builder.toString())
            }
        }
    }

    private suspend fun attendance(account: AccountSk): String {
        return try {
            "签到结果：" + sklandAttendance(account)
        } catch (e: Exception) {
            e.message ?: "什么都没有发生喵"
        }
    }

    private fun insertLinkData() {
        executeAsync {
            if (!prefManager.insertLink.get()) {
                linkDao.insert(Link(title = "PRTS", url = "https://prts.wiki/w/"))
                prefManager.insertLink.set(true)
            }
        }

    }

    private fun checkAppUpdate() {
        if (!prefManager.autoUpdateApp.get()) return
        executeAsync {
            try {
                val info = AppUpdate.getUpdateInfo()
                _appUpdateInfo.postValue(info)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun checkAnnounce() {
        if(!prefManager.showHomeAnnounce.get())return
        executeAsync {
            try {
                val info = getAnnounce()
                _announce.postValue(info)
            } catch (e: Exception) {
                e.printStackTrace()
                _announce.postValue(e.message)
            }
        }
    }

    private fun initialize() {
        viewModelScope.launch {
            _accountSkList.value = accountSkDao.getAll()
            _accountGcList.value = accountGcDao.getAll()
            _links.value = linkDao.getAll()
            getDefaultAccountSk()
            loadApCache()
        }
    }

    fun reloadData() {
        initialize()
    }

    private fun getDefaultAccountSk() {
        executeAsync {
            val accountSk = prefManager.baseAccountSk.get()
            if (accountSk.uid == "") {
                _currentAccount.postValue(null)
            } else {
                _currentAccount.postValue(accountSk)
            }
        }
    }

    fun setDefaultAccountSk(account: AccountSk) {
        executeAsync {
            prefManager.baseAccountSk.set(account)
            _currentAccount.postValue(account)
        }
    }

    fun setDefaultAccountGc(account: AccountGc) {
        executeAsync {
            prefManager.baseAccountGc.set(account)
        }
    }

    fun accountSkLogin(token: String, dId: String) {
        executeAsync {
            try {
                val list = createAccountList(token, dId)
                accountSkDao.insert(list)
                _accountSkList.postValue(accountSkDao.getAll())
                if(prefManager.baseAccountSk.get().uid=="")
                    prefManager.baseAccountSk.set(list[0])
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun accountGcLogin(token: String, channelMasterId: Int) {
        executeAsync {
            try {
                val account = createGachaAccount(channelMasterId, token)?:return@executeAsync
                accountGcDao.insert(account)
                _accountGcList.postValue(accountGcDao.getAll())
                if(prefManager.baseAccountGc.get().uid=="")
                    prefManager.baseAccountGc.set(account)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun deleteAccountSk(account: AccountSk) {
        executeAsync {
//            val list = _accountSkList.value?.toMutableList()
//            list?.remove(account)
//            val _list = list ?: ArrayList()
//            _accountSkList.postValue(_list)
            accountSkDao.delete(account.id)
            _accountSkList.postValue(accountSkDao.getAll())
        }
    }

    fun deleteAccountGc(account: AccountGc) {
        executeAsync {
//            val list = _accountSkList.value?.toMutableList()
//            list?.remove(account)
//            val _list = list ?: ArrayList()
//            _accountSkList.postValue(_list)
            accountGcDao.delete(account.id)
            _accountGcList.postValue(accountGcDao.getAll())
        }
    }

    fun insertLink(link: Link) {
        executeAsync {
            linkDao.insert(link)
            _links.postValue(linkDao.getAll())
        }
    }

    fun updateLink(link: Link) {
        executeAsync {
            linkDao.update(link.id, link.title, link.url)
            _links.postValue(linkDao.getAll())
        }
    }

    fun deleteLink(link: Link) {
        executeAsync {
            linkDao.delete(link.id)
            val list = _links.value?.toMutableList()
            list?.remove(link)
            val newList = list ?: ArrayList()
            _links.postValue(newList)
//            _links.postValue(linkDao.getAll())
        }
    }

    private fun executeAsync(function: suspend () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) { function() }
    }

    private suspend fun getAnnounce():String{
        val client = OkHttpClient()
        val request = Request.Builder().url(announceUrl).build()
        return withContext(Dispatchers.IO) {
            client.newCall(request).execute().use { response ->
                response.body?.string().let{json ->
                    val content = ObjectMapper().readTree(json).at("/content")
                    content.asText()
                } ?: "Empty response"
            }
        }
    }
}