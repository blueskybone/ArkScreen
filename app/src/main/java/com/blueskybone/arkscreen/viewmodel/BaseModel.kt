package com.blueskybone.arkscreen.viewmodel

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.blueskybone.arkscreen.APP
import com.blueskybone.arkscreen.AppUpdateInfo
import com.blueskybone.arkscreen.network.BiliVideo
import com.blueskybone.arkscreen.network.NetWorkTask
import com.blueskybone.arkscreen.network.NetWorkTask.Companion.createAccountList
import com.blueskybone.arkscreen.network.NetWorkTask.Companion.createGachaAccount
import com.blueskybone.arkscreen.network.NetWorkTask.Companion.sklandAttendance
import com.blueskybone.arkscreen.network.announceUrl
import com.blueskybone.arkscreen.network.getVideoList
import com.blueskybone.arkscreen.playerinfo.ApCache
import com.blueskybone.arkscreen.preference.PrefManager
import com.blueskybone.arkscreen.room.AccountGc
import com.blueskybone.arkscreen.room.AccountSk
import com.blueskybone.arkscreen.room.ArkDatabase
import com.blueskybone.arkscreen.room.Link
import com.blueskybone.arkscreen.util.TimeUtils.getCurrentTs
import com.blueskybone.arkscreen.util.updateNotification
import com.fasterxml.jackson.databind.ObjectMapper
import kotlinx.coroutines.CoroutineScope
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

    private val _appUpdateInfo = MutableLiveData<AppUpdateInfo.UpdateInfo>()
    val appUpdateInfo: LiveData<AppUpdateInfo.UpdateInfo> get() = _appUpdateInfo

    private val _apCache = MutableLiveData<ApCache>()
    val apCache: LiveData<ApCache> get() = _apCache

    private val _attendanceLog = MutableLiveData<String>()
    val attendanceLog: LiveData<String> get() = _attendanceLog

    private val _announce = MutableLiveData<String>()
    val announce: LiveData<String> get() = _announce

    private val _biliVideo = MutableLiveData<List<BiliVideo>>()
    val biliVideo: LiveData<List<BiliVideo>> get() = _biliVideo


//    private val biliwbi: WbiParams

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
    fun runAttendance() {
        job?.cancel()
        job = viewModelScope.launch(Dispatchers.IO) {
            val builder = StringBuilder()
            accountSkList.value?.let { list ->
                builder.append("开始任务：\n")
                for (account in list) {
                    if (account.uid != "") {
                        builder.append(account.nickName).append(" 签到中...\n")
                        _attendanceLog.postValue(builder.toString())
                        builder.append(attendance(account)).append("\n")
                        _attendanceLog.postValue(builder.toString())
                    }
                }
                builder.append("签到完成。\n")
                _attendanceLog.postValue(builder.toString())
            }
        }
    }


    fun startAttendance(context: Context) {
        val prefManager: PrefManager by getKoin().inject()
        val database = ArkDatabase.getDatabase(APP)
        val accountSkDao = database.getAccountSkDao()
        CoroutineScope(Dispatchers.IO).launch {
            val accountList = accountSkDao.getAll()
            val channelId = "atd_notify_channel"
            val channelName = "签到通知"
            for ((idx, account) in accountList.withIndex()) {
                updateNotification(
                    context,
                    "正在签到中 (${idx + 1}/ ${accountList.size})",
                    account.nickName,
                    channelId,
                    channelName
                )
                val msg = sklandAttendance(account)
                Timber.i(msg)
                updateNotification(
                    context,
                    "正在签到中 (${idx + 1}/ ${accountList.size})",
                    account.nickName + ":" + msg,
                    channelId,
                    channelName
                )
                Thread.sleep(500)
            }
            updateNotification(
                context,
                "签到完成 (${accountList.size}/ ${accountList.size})",
                "",
                channelId,
                channelName
            )
        }
        prefManager.lastAttendanceTs.set(getCurrentTs())
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
            try {
                val info = AppUpdateInfo.remoteInfo()
                _appUpdateInfo.postValue(info)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun getBiliVideoList() {
        executeAsync {
            try {
                val list = getVideoList()
                println("bili video list.size : ${list.size}")
                _biliVideo.postValue(list)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun checkAnnounce() {
        if (!prefManager.showHomeAnnounce.get()) return
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
            _links.value = linkDao.getAll().map { it.copy() }
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
                Timber.i("createAccountList end.")
                accountSkDao.insert(list)
                _accountSkList.postValue(accountSkDao.getAll())
                if (prefManager.baseAccountSk.get().uid == "")
                    prefManager.baseAccountSk.set(list[0])
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun accountGcLogin(token: String, channelMasterId: Int) {
        executeAsync {
            try {
                val account = createGachaAccount(channelMasterId, token) ?: return@executeAsync
                accountGcDao.insert(account)
                _accountGcList.postValue(accountGcDao.getAll())
                if (prefManager.baseAccountGc.get().uid == "")
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
            link.icon = parseHtmlForIcon(link.url) ?: ""
            linkDao.insert(link)
            _links.postValue(linkDao.getAll())
//            val icon = parseHtmlForIcon(link.url) ?: ""
//            linkDao.update(link.id, link.title, link.url, icon)
//            _links.postValue(linkDao.getAll())
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
        } finally {

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
        viewModelScope.launch(Dispatchers.IO) { function() }
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