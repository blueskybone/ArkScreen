package com.blueskybone.arkscreen.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.blueskybone.arkscreen.domain.model.account.AccountSk
import com.blueskybone.arkscreen.domain.model.link.Link
import com.blueskybone.arkscreen.domain.repository.AccountRepository
import com.blueskybone.arkscreen.domain.repository.ResourceRepository
import com.blueskybone.arkscreen.domain.repository.SklandRepository
import com.blueskybone.arkscreen.domain.usecase.CheckUpdateUseCase
import com.blueskybone.arkscreen.domain.usecase.account.SyncAccountSkUseCase
import com.blueskybone.arkscreen.domain.usecase.attendance.GetAttdResultUseCase
import com.blueskybone.arkscreen.ui.account.model.AccountItemUiModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.URL

/**
 * Created by blueskybone
 * Date: 2026/3/19
 */
//class MainModel(
//    private val repoAcc: AccountRepository,
//    private val repoRes: ResourceRepository,
//    private val repoSkland: SklandRepository,
//    private val getAttdResultUseCase: GetAttdResultUseCase,
//    private val checkUpdateUseCase: CheckUpdateUseCase,
//    private val syncAccountSkUseCase: SyncAccountSkUseCase,
//) : ViewModel() {
//
//    private val _uiState = MutableLiveData<UiState>(UiState.Idle)
//    val uiState: LiveData<UiState> = _uiState  // 暴露 LiveData
//    val accountSkList: LiveData<List<AccountSk>> = repoAcc.observeSkAcc()
//        .catch { e -> emit(emptyList()) }
//        .asLiveData()
//
//    val accountEfList: LiveData<List<AccountEf>> = repoAcc.observeEfAcc()
//        .catch { e -> emit(emptyList()) }
//        .asLiveData()
//
//    val currentAccountSk: LiveData<AccountSk?> = repoAcc.observeCurrentSkAcc()
//        .catch { e -> emit(null) }
//        .asLiveData()
//
//    private val _apCache = MutableLiveData<ApCache>()
//    val apCache: LiveData<ApCache> get() = _apCache
//
//    sealed class UiEvent {
//        data class ShowUpdateDialog(
//            val version: String,
//            val changelog: String,
//            val url: String,
//        ) : UiEvent()
//
//        object ShowNoUpdate : UiEvent()
//        data class ShowError(val message: String) : UiEvent()
//        data class ShowToast(val message: String) : UiEvent()
//    }
//
//    // 一次性事件
//    private val _uiEvent = MutableLiveData<Event<UiEvent>>()
//    val uiEvent: LiveData<Event<UiEvent>> = _uiEvent
//
//    private val _announce = MutableLiveData<String>()
//    val announce: LiveData<String> get() = _announce
//
//    private val _biliVideo = MutableLiveData<List<BiliVideo>>()
//    val biliVideo: LiveData<List<BiliVideo>> get() = _biliVideo
//
//    val links: LiveData<List<Link>> = repoRes.observeLinks()
//        .catch { e -> emit(emptyList()) }
//        .asLiveData()
//
//    init {
//        getBiliVideoList()
//        fetchAnnounce()
//        fetchAppUpdate()
//        loadApCache()
//    }
//
//    private fun fetchAnnounce() {
//        execute {
//            _announce.value = "加载公告中..."
//            repoRes.fetchAnnounce().onSuccess { content ->
//                _announce.value = content
//            }.onFailure { error ->
//                _announce.value = error.message
//            }
//        }
//    }
//
//    //考虑一下Link的id重复问题
//    fun insertLink(title: String, url: String) {
//        execute {
//            val icon = parseHtmlForIcon(url) ?: ""
//            repoRes.insertLink(Link(id = null, title = title, url = url, icon = icon))
//                .onFailure { error ->
//                    _uiEvent.value = Event(UiEvent.ShowError(error.message.toString()))
//                }
//        }
//    }
//
//    private suspend fun parseHtmlForIcon(url: String): String? = withContext(Dispatchers.IO) {
//        try {
//            val html = URL(url).readText()
//            Regex("""<link.*?rel=(["'])(?:icon|shortcut icon)\1.*?href=(["'])(.*?)\2""")
//                .find(html)
//                ?.groupValues?.get(3)
//                ?.let { iconPath ->
//                    if (iconPath.startsWith("http")) iconPath
//                    else URL(URL(url), iconPath).toString()
//                }
//        } catch (e: Exception) {
//            null
//        }
//    }
//
//    fun deleteLink(link: Link) {
//        execute {
//            repoRes.deleteLink(link).onFailure { error ->
//                _uiEvent.value = Event(UiEvent.ShowError(error.message.toString()))
//            }
//        }
//    }
//
//    fun updateLink(link: Link, title: String, url: String) {
//        execute {
//            val icon = parseHtmlForIcon(url) ?: ""
//            repoRes.updateLink(Link(id = link.id, title = title, url = url, icon = icon)).onFailure { error ->
//                _uiEvent.value = Event(UiEvent.ShowError(error.message.toString()))
//            }
//        }
//    }
//
//    fun setDefaultAccountSk(account: AccountSk) {
//        execute {
//            repoAcc.setCurrentAccountSk(account)
//        }
//    }
//
//    //TODO:签到:看看怎么跟通知联动
//    fun startAttendance() {
//        execute {
//            accountSkList.value?.forEach { acc ->
//                getAttdResultUseCase(acc).onSuccess { result ->
//                    //result目前就只是单个账号签到之后的结果，是一个string
//                }.onFailure { error ->
//                }
//            }
//            accountEfList.value?.forEach { acc ->
//                getAttdResultUseCase(acc).onSuccess { result ->
//
//                }.onFailure { error ->
//                }
//            }
//        }
//    }
//
//    fun fetchAppUpdate() {
//        execute {
//            val result = checkUpdateUseCase()
//            result.onSuccess { check ->
//                when (check) {
//                    is CheckUpdateUseCase.CheckResult.Update -> {
//                        _uiEvent.value = Event(
//                            UiEvent.ShowUpdateDialog(
//                                version = check.info.version,
//                                changelog = check.info.content,
//                                url = check.info.link
//                            )
//                        )
//                    }
//                    is CheckUpdateUseCase.CheckResult.NoUpdate -> {
//                        _uiEvent.value = Event(UiEvent.ShowNoUpdate)
//                    }
//                }
//            }.onFailure { error ->
//                _uiEvent.value = Event(UiEvent.ShowError(error.message.toString()))
//            }
//        }
//    }
//
//    fun downloadApp() {
//
//    }
//
//    fun loadApCache() {
//        execute {
//            val apCache = repoSkland.getApCache()
//            _apCache.value = apCache
//        }
//    }
//
//
//    private fun getBiliVideoList() {
//        execute {
//            repoRes.getBiliVideo().onSuccess { list ->
//                _biliVideo.value = list
//            }
//        }
//    }
//
//    fun loginSklandByToken(token: String) {
//        execute {
//            _uiState.value = UiState.Loading
//            val way = SyncAccountSkUseCase.LoginWay.Token(token)
//            val result = syncAccountSkUseCase(way)
//            _uiState.value = result.fold(
//                onSuccess = { cnt ->
//                    UiState.Success("成功导入${cnt}条账号")
//                },
//                onFailure = { error ->
//                    UiState.Error(error.message ?: "登录失败")
//                }
//            )
//        }
//    }
//
//    fun loginSklandByPhonePassword(phone: String, password: String) {
//        execute {
//            _uiState.value = UiState.Loading
//            val way = SyncAccountSkUseCase.LoginWay.PhoneAndPassword(phone, password)
//            val result = syncAccountSkUseCase(way)
//            _uiState.value = result.fold(
//                onSuccess = { cnt ->
//                    UiState.Success("成功导入${cnt}条账号")
//                },
//                onFailure = { error ->
//                    UiState.Error(error.message ?: "登录失败")
//                }
//            )
//        }
//    }
//
//    fun loginSklandByCookie(cookie: String) {
//        execute {
//            _uiState.value = UiState.Loading
//            val way = SyncAccountSkUseCase.LoginWay.Cookie(cookie)
//            val result = syncAccountSkUseCase(way)
//            _uiState.value = result.fold(
//                onSuccess = { cnt ->
//                    UiState.Success("成功导入${cnt}条账号")
//                },
//                onFailure = { error ->
//                    UiState.Error(error.message ?: "登录失败")
//                }
//            )
//        }
//    }
//
//
//    private fun execute(function: suspend () -> Unit) {
//        viewModelScope.launch {
//            try {
//                function()
//            } catch (e: CancellationException) {
//            }
//        }
//    }
//}


class MainModel(
    private val repoAcc: AccountRepository,
    private val repoRes: ResourceRepository,
    private val repoSkland: SklandRepository,
    private val getAttdResultUseCase: GetAttdResultUseCase,
    private val checkUpdateUseCase: CheckUpdateUseCase,
    private val syncAccountSkUseCase: SyncAccountSkUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState

    private val _event = MutableSharedFlow<MainEvent>()
    val event: SharedFlow<MainEvent> = _event

    private val skListFlow = repoAcc.observeSkAcc()
        .catch { emit(emptyList()) }
    private val currentSkFlow = repoAcc.observeCurrentSkAcc()

    val accountSkUiList: StateFlow<List<AccountItemUiModel>> =
        combine(skListFlow, currentSkFlow) { list, current ->
            list.map { account ->
                AccountItemUiModel(
                    account = account,
                    isDefault = account.uid == current?.uid
                )
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        observeAccounts()
        observeLinks()
        refreshHomeData()
    }

    private fun observeAccounts() {
        viewModelScope.launch {
            repoAcc.observeSkAcc().collect { list ->
                _uiState.update { it.copy(accountSkList = list) }
            }
        }

        viewModelScope.launch {
            repoAcc.observeEfAcc().collect { list ->
                _uiState.update { it.copy(accountEfList = list) }
            }
        }

        viewModelScope.launch {
            repoAcc.observeCurrentSkAcc().collect { account ->
                _uiState.update { it.copy(currentAccountSk = account) }
            }
        }
    }

    private fun observeLinks() {
        viewModelScope.launch {
            repoRes.observeLinks().collect { list ->
                _uiState.update { it.copy(links = list) }
            }
        }
    }

    fun refreshHomeData() {
        fetchAnnounce()
        fetchBiliVideos()
        loadApCache()
    }


    fun setDefaultAccountSk(account: AccountSk) {
        viewModelScope.launch {
            repoAcc.setCurrentAccountSk(account)
        }
    }

    fun loginSkland(way: SyncAccountSkUseCase.LoginWay) {
        viewModelScope.launch {
            _uiState.update { it.copy(loginState = ActionState.Loading) }

            syncAccountSkUseCase(way)
                .onSuccess { cnt ->
                    _uiState.update {
                        it.copy(loginState = ActionState.Success("成功导入${cnt}条账号"))
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(loginState = ActionState.Error(error.message ?: "登录失败"))
                    }
                }
        }
    }

    fun fetchAnnounce() {
        viewModelScope.launch {
            _uiState.update { it.copy(loading = true) }

            repoRes.fetchAnnounce()
                .onSuccess { content ->
                    _uiState.update {
                        it.copy(
                            loading = false,
                            announce = content
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            loading = false,
                            announce = error.message ?: "公告加载失败"
                        )
                    }
                }
        }
    }

    fun fetchBiliVideos() {
        viewModelScope.launch {
            repoRes.getBiliVideo()
                .onSuccess { list ->
                    _uiState.update { it.copy(biliVideos = list) }
                }
                .onFailure { error ->
                    _event.emit(MainEvent.ShowError(error.message ?: "视频加载失败"))
                }
        }
    }

    fun loadApCache() {
        viewModelScope.launch {
            val cache = repoSkland.getApCache()
            _uiState.update { it.copy(apCache = cache) }
        }
    }

    fun startAttendance() {
        viewModelScope.launch {
            val state = _uiState.value
            _uiState.update { it.copy(attendanceState = ActionState.Loading) }

            val skResults = state.accountSkList.map { acc ->
                getAttdResultUseCase(acc)
            }
            val efResults = state.accountEfList.map { acc ->
                getAttdResultUseCase(acc)
            }

            val all = skResults + efResults
            val failed = all.filter { it.isFailure }

            if (failed.isEmpty()) {
                _uiState.update {
                    it.copy(attendanceState = ActionState.Success("签到完成"))
                }
            } else {
                _uiState.update {
                    it.copy(attendanceState = ActionState.Error("部分账号签到失败"))
                }
            }
        }
    }

    fun checkAppUpdate(showNoUpdate: Boolean = false) {
        viewModelScope.launch {
            checkUpdateUseCase()
                .onSuccess { check ->
                    when (check) {
                        is CheckUpdateUseCase.CheckResult.Update -> {
                            _event.emit(
                                MainEvent.ShowUpdateDialog(
                                    version = check.info.version,
                                    changelog = check.info.content,
                                    url = check.info.link
                                )
                            )
                        }

                        is CheckUpdateUseCase.CheckResult.NoUpdate -> {
                            if (showNoUpdate) {
                                _event.emit(MainEvent.ShowToast("当前已是最新版本"))
                            }
                        }
                    }
                }
                .onFailure { error ->
                    _event.emit(MainEvent.ShowError(error.message ?: "检查更新失败"))
                }
        }
    }

    private suspend fun resolveIcon(url: String): String {
        return parseHtmlForIcon(url).orEmpty()
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

    fun addLink(title: String, url: String) {
        viewModelScope.launch {
            val icon = resolveIcon(url)
            repoRes.insertLink(Link(id = null, title = title, url = url, icon = icon))
                .onFailure { error ->
                    _event.emit(MainEvent.ShowError(error.message ?: "添加链接失败"))
                }
        }
    }

    fun updateLink(link: Link, title: String, url: String) {
        viewModelScope.launch {
            val icon = resolveIcon(url)
            repoRes.updateLink(link.copy(title = title, url = url, icon = icon))
                .onFailure { error ->
                    _event.emit(MainEvent.ShowError(error.message ?: "更新链接失败"))
                }
        }
    }

    fun deleteLink(link: Link) {
        viewModelScope.launch {
            repoRes.deleteLink(link)
                .onFailure { error ->
                    _event.emit(MainEvent.ShowError(error.message ?: "删除链接失败"))
                }
        }
    }

    fun downloadApp(url: String) {
        viewModelScope.launch {
            _event.emit(MainEvent.StartAppDownload(url))
        }
    }
}