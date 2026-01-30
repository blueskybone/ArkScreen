package com.blueskybone.arkscreen.viewmodel

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.blueskybone.arkscreen.APP
import com.blueskybone.arkscreen.AppUpdateInfo
import com.blueskybone.arkscreen.network.BiliVideo
import com.blueskybone.arkscreen.network.announceUrl
import com.blueskybone.arkscreen.network.getVideoList
import com.blueskybone.arkscreen.playerinfo.cache.ApCache
import com.blueskybone.arkscreen.preference.PrefManager
import com.blueskybone.arkscreen.repository.AccountRepository
import com.blueskybone.arkscreen.room.Account
import com.blueskybone.arkscreen.room.AccountEf
import com.blueskybone.arkscreen.room.AccountGc
import com.blueskybone.arkscreen.room.AccountSk
import com.blueskybone.arkscreen.room.ArkDatabase
import com.blueskybone.arkscreen.room.Link
import com.blueskybone.arkscreen.task.attendance.doSklandAttendance
import com.blueskybone.arkscreen.util.TimeUtils.getCurrentTs
import com.fasterxml.jackson.databind.ObjectMapper
import com.hjq.toast.Toaster
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.koin.java.KoinJavaComponent.getKoin
import timber.log.Timber
import java.net.URL

/**
 *   Created by blueskybone
 *   Date: 2025/1/4
 */
class BaseModel : ViewModel() {
    private val prefManager: PrefManager by getKoin().inject()
    private val resp: AccountRepository by getKoin().inject()

    val accountSkList: LiveData<List<AccountSk>> = resp.allSkAccounts
    val accountGcList: LiveData<List<AccountGc>> = resp.allGcAccounts
    val accountEfList: LiveData<List<AccountEf>> = resp.allEfAccounts


    private val database = ArkDatabase.getDatabase(APP)
    private val linkDao = database.getLinkDao()

    private val _links = MutableLiveData<List<Link>>()
    val links: LiveData<List<Link>> get() = _links

    private val _currentAccount = MutableLiveData<AccountSk?>()
    val currentAccount: LiveData<AccountSk?> get() = _currentAccount

    private val _currentAccountGc = MutableLiveData<AccountGc?>()
    val currentAccountGc: LiveData<AccountGc?> get() = _currentAccountGc

    private val _appUpdateInfo = MutableLiveData<AppUpdateInfo.UpdateInfo>()
    val appUpdateInfo: LiveData<AppUpdateInfo.UpdateInfo> get() = _appUpdateInfo

    private val _apCache = MutableLiveData<ApCache>()
    val apCache: LiveData<ApCache> get() = _apCache

    private val _announce = MutableLiveData<String>()
    val announce: LiveData<String> get() = _announce

    private val _biliVideo = MutableLiveData<List<BiliVideo>>()
    val biliVideo: LiveData<List<BiliVideo>> get() = _biliVideo

    init {
        initialize()
        checkAppUpdate()
        getBiliVideoList()
        insertLinkData()
        checkAnnounce()
    }

    private fun loadApCache() {
        executeAsync {
            _apCache.postValue(prefManager.apCache.get())
        }
    }

    private var job: Job? = null
    fun startAttendance(context: Context) {
        job?.cancel()
        job = viewModelScope.launch(Dispatchers.IO) {
            val prefManager: PrefManager by getKoin().inject()
            try {
                doSklandAttendance(context)
                prefManager.lastAttendanceTs.set(getCurrentTs())
            } catch (e: Exception) {
                Toaster.show(e.message)
                Timber.e("startAttendance fault: " + e.message)
            }
        }
    }

    private fun insertLinkData() {
        executeAsync {
            if (!prefManager.insertLink.get()) {
                linkDao.insert(
                    Link(
                        title = "PRTS",
                        url = "https://prts.wiki/w/",
                        icon = "https://prts.wiki/public/favicon.ico"
                    )
                )
                prefManager.insertLink.set(true)
            }
        }
    }

    private fun checkAppUpdate() {
        if (!prefManager.autoUpdateApp.get()) return
        executeAsync {
            _appUpdateInfo.postValue(AppUpdateInfo.remoteInfo())
        }
    }

    private fun getBiliVideoList() {
        executeAsync {
            val list = getVideoList()
            _biliVideo.postValue(list)
        }
    }

    private fun checkAnnounce() {
        if (!prefManager.showHomeAnnounce.get()) return
        executeAsync {
            _announce.postValue(getAnnounce())
        }
    }

    private fun initialize() {
        viewModelScope.launch {
//            _accountSkList.value = accountSkDao.getAll()
//            _accountGcList.value = accountGcDao.getAll()
            _links.value = linkDao.getAll().map { it.copy() }
            getDefaultAccountSk()
            getDefaultAccountGc()
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
            _currentAccountGc.postValue(account)
        }
    }

    private fun getDefaultAccountGc() {
        executeAsync {
            val accountGc = prefManager.baseAccountGc.get()
            if (accountGc.uid == "") {
                _currentAccountGc.postValue(null)
            } else {
                _currentAccountGc.postValue(accountGc)
            }
        }
    }

    fun accountSkLogin(token: String, dId: String) {
        executeAsync {
            val accountCnt = resp.syncAccountsByToken(token, dId)
            Toaster.show("登录成功：导入${accountCnt}条账号")
        }
    }

    fun accountGcLogin(token: String, channelMasterId: Int, akUserCenter: String, xrToken: String) {
        executeAsync {
            resp.loginGachaAccount(token, channelMasterId, akUserCenter, xrToken)
            Toaster.show("登录成功，已导入卡池账号")
        }
    }

    fun accountSkLoginByPassword(phone: String, password: String) {
        executeAsync {
            val accountCnt = resp.syncAccountsByPhone(phone, password)
            Toaster.show("登录成功：导入${accountCnt}条账号")
        }
    }

    fun deleteAccount(account: Account) {
        executeAsync {
            resp.deleteAccount(account)
        }
    }

    fun insertLink(link: Link) {
        executeAsync {
            link.icon = parseHtmlForIcon(link.url) ?: ""
            linkDao.insert(link)
            _links.postValue(linkDao.getAll())
        }
    }

    private suspend fun parseHtmlForIcon(url: String): String? = withContext(Dispatchers.IO) {
        try {
            val html = URL(url).readText()
            Regex("""<link.*?rel=(["'])(?:icon|shortcut icon)\1.*?href=(["'])(.*?)\2""")
                .find(html)
                ?.groupValues?.get(3)
                ?.let { iconPath ->
                    if (iconPath.startsWith("http")) iconPath
                    else URL(URL(url), iconPath).toString()
                }
        } catch (e: Exception) {
            null
        }
    }

    fun updateLink(link: Link) {
        executeAsync {
            linkDao.update(link.id, link.title, link.url, link.icon)
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
        viewModelScope.launch(Dispatchers.IO) {
            try {
                function()
            } catch (e: Exception) {
                Toaster.show(e.message)
                e.printStackTrace()
            }
        }
    }

    private suspend fun getAnnounce(): String {
        val client = OkHttpClient()
        val request = Request.Builder().url(announceUrl).build()
        return withContext(Dispatchers.IO) {
            client.newCall(request).execute().use { response ->
                response.body?.string().let { json ->
                    val content = ObjectMapper().readTree(json).at("/content")
                    content.asText()
                } ?: "Empty response"
            }
        }
    }
}