package com.blueskybone.arkscreen.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.blueskybone.arkscreen.domain.model.account.AccountSk
import com.blueskybone.arkscreen.domain.model.link.Link
import com.blueskybone.arkscreen.domain.repository.AccountRepository
import com.blueskybone.arkscreen.domain.repository.HomeContentRepository
import com.blueskybone.arkscreen.domain.repository.LinkRepository
import com.blueskybone.arkscreen.domain.repository.SklandRepository
import com.blueskybone.arkscreen.domain.model.DownloadStatus
import com.blueskybone.arkscreen.domain.usecase.appupdate.CheckAppUpdateUseCase
import com.blueskybone.arkscreen.domain.usecase.appupdate.StartAppUpdateDownloadUseCase
import com.blueskybone.arkscreen.domain.usecase.account.SyncAccountSkUseCase
import com.blueskybone.arkscreen.ui.account.model.AccountItemUiModel
import com.blueskybone.arkscreen.ui.UiStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

/**
 * Created by blueskybone
 * Date: 2026/3/19
 */
class MainModel(
    private val repoAcc: AccountRepository,
    private val homeContentRepository: HomeContentRepository,
    private val linkRepository: LinkRepository,
    private val repoSkland: SklandRepository,
    private val checkUpdateUseCase: CheckAppUpdateUseCase,
    private val startAppUpdateDownloadUseCase: StartAppUpdateDownloadUseCase,
    private val syncAccountSkUseCase: SyncAccountSkUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState

    // Events are consumed once by MainActivity; durable screen data stays in uiState.
    private val _event = Channel<MainEvent>(Channel.BUFFERED)
    val event = _event.receiveAsFlow()

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
            combine(
                repoAcc.observeSkAcc(),
                repoAcc.observeEfAcc(),
                repoAcc.observeCurrentSkAcc(),
            ) { skAccounts, efAccounts, current ->
                Triple(skAccounts, efAccounts, current)
            }.collect { (skAccounts, efAccounts, current) ->
                val skItems = skAccounts.map { account ->
                    AccountItemUiModel(account, account.uid == current?.uid)
                }
                val efItems = efAccounts.map { account -> AccountItemUiModel(account, false) }
                _uiState.update {
                    it.copy(
                        accountSkList = skItems,
                        accountEfList = efItems,
                        currentAccountSk = current,
                    )
                }
            }
        }
    }

    private fun observeLinks() {
        viewModelScope.launch {
            linkRepository.observeLinks().collect { list ->
                _uiState.update { it.copy(links = list) }
            }
        }
    }

    fun refreshHomeData() {
        fetchAnnounce()
        fetchBiliVideos()
        loadApCache()
    }

    fun hasSklandAccounts(): Boolean = _uiState.value.accountSkList.isNotEmpty()

    fun currentGamePackageName(): String? = _uiState.value.currentAccountSk?.let { account ->
        if (account.official) "com.hypergryph.arknights"
        else "com.hypergryph.arknights.bilibili"
    }


    fun setDefaultAccountSk(account: AccountSk) {
        viewModelScope.launch {
            repoAcc.setCurrentAccountSk(account)
                .onFailure { error ->
                    _event.send(MainEvent.ShowError(error.message ?: "切换账号失败"))
                }
        }
    }

    fun loginSkland(way: SyncAccountSkUseCase.LoginWay) {
        viewModelScope.launch {
            _uiState.update { it.copy(loginStatus = UiStatus.Loading()) }

            syncAccountSkUseCase(way)
                .onSuccess { cnt ->
                    _uiState.update {
                        it.copy(loginStatus = UiStatus.Success("成功导入${cnt}条账号"))
                    }
                    _event.send(MainEvent.ShowToast("成功导入${cnt}条账号"))
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(loginStatus = UiStatus.Error(error.message ?: "登录失败"))
                    }
                    _event.send(MainEvent.ShowError(error.message ?: "登录失败"))
                }
        }
    }

    fun fetchAnnounce() {
        viewModelScope.launch {
            _uiState.update { it.copy(status = UiStatus.Loading()) }

            homeContentRepository.fetchAnnouncement()
                .onSuccess { content ->
                    _uiState.update {
                        it.copy(
                            status = UiStatus.Success(),
                            announce = content
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            status = UiStatus.Error(error.message ?: "公告加载失败"),
                            announce = error.message ?: "公告加载失败"
                        )
                    }
                }
        }
    }

    fun fetchBiliVideos() {
        viewModelScope.launch {
            homeContentRepository.fetchBiliVideos()
                .onSuccess { list ->
                    _uiState.update { it.copy(biliVideos = list) }
                }
                .onFailure { error ->
                    _event.send(MainEvent.ShowError(error.message ?: "视频加载失败"))
                }
        }
    }

    fun loadApCache() {
        viewModelScope.launch {
            val cache = repoSkland.getApCache()
            _uiState.update { it.copy(apCache = cache) }
        }
    }

    fun checkAppUpdate(showNoUpdate: Boolean = false) {
        viewModelScope.launch {
            checkUpdateUseCase()
                .onSuccess { check ->
                    when (check) {
                        null -> {
                            if (showNoUpdate) {
                                _event.send(MainEvent.ShowToast("当前已是最新版本"))
                            }
                        }

                        else -> {
                            _event.send(
                                MainEvent.ShowUpdateDialog(
                                    version = check.version,
                                    changelog = check.content,
                                    url = check.link
                                )
                            )
                        }
                    }
                }
                .onFailure { error ->
                    _event.send(MainEvent.ShowError(error.message ?: "检查更新失败"))
                }
        }
    }

    fun addLink(title: String, url: String) {
        viewModelScope.launch {
            val icon = linkRepository.resolveIcon(url).getOrDefault("")
            linkRepository.insertLink(Link(id = null, title = title, url = url, icon = icon))
                .onFailure { error ->
                    _event.send(MainEvent.ShowError(error.message ?: "添加链接失败"))
                }
        }
    }

    fun updateLink(link: Link, title: String, url: String) {
        viewModelScope.launch {
            val icon = linkRepository.resolveIcon(url).getOrDefault("")
            linkRepository.updateLink(link.copy(title = title, url = url, icon = icon))
                .onFailure { error ->
                    _event.send(MainEvent.ShowError(error.message ?: "更新链接失败"))
                }
        }
    }

    fun deleteLink(link: Link) {
        viewModelScope.launch {
            linkRepository.deleteLink(link)
                .onFailure { error ->
                    _event.send(MainEvent.ShowError(error.message ?: "删除链接失败"))
                }
        }
    }

    fun downloadApp(url: String) {
        viewModelScope.launch {
            startAppUpdateDownloadUseCase(url).collect { status ->
                when (status) {
                    DownloadStatus.Started -> _event.send(MainEvent.DownloadStarted)
                    is DownloadStatus.Progress -> {
                        _event.send(MainEvent.DownloadProgress(status.percent))
                    }
                    is DownloadStatus.Success -> {
                        _event.send(MainEvent.DownloadCompleted(status.filePath))
                    }
                    is DownloadStatus.Failed -> {
                        _event.send(
                            MainEvent.DownloadFailed(
                                status.throwable.message ?: "下载更新失败"
                            )
                        )
                    }
                }
            }
        }
    }
}
