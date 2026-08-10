package com.blueskybone.arkscreen.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.blueskybone.arkscreen.domain.model.account.AccountSk
import com.blueskybone.arkscreen.domain.model.link.Link
import com.blueskybone.arkscreen.domain.model.link.LinkUrl
import com.blueskybone.arkscreen.domain.repository.AccountRepository
import com.blueskybone.arkscreen.domain.repository.HomeContentRepository
import com.blueskybone.arkscreen.domain.repository.LinkRepository
import com.blueskybone.arkscreen.domain.repository.RemoteConfigRepository
import com.blueskybone.arkscreen.domain.repository.SklandRepository
import com.blueskybone.arkscreen.domain.service.LinkMetadataResolver
import com.blueskybone.arkscreen.domain.model.DownloadStatus
import com.blueskybone.arkscreen.domain.usecase.appupdate.CheckAppUpdateUseCase
import com.blueskybone.arkscreen.domain.usecase.appupdate.StartAppUpdateDownloadUseCase
import com.blueskybone.arkscreen.domain.usecase.account.SyncAccountSkUseCase
import com.blueskybone.arkscreen.data.local.pref.InnerPrefManager
import com.blueskybone.arkscreen.ui.account.model.AccountItemUiModel
import com.blueskybone.arkscreen.ui.UiStatus
import com.blueskybone.arkscreen.ui.common.userFacingError
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.retryWhen
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.Job
import kotlin.coroutines.coroutineContext
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * Created by blueskybone
 * Date: 2026/3/19
 */
class MainModel(
    private val repoAcc: AccountRepository,
    private val homeContentRepository: HomeContentRepository,
    private val remoteConfigRepository: RemoteConfigRepository,
    private val linkRepository: LinkRepository,
    private val linkMetadataResolver: LinkMetadataResolver,
    private val repoSkland: SklandRepository,
    private val checkUpdateUseCase: CheckAppUpdateUseCase,
    private val startAppUpdateDownloadUseCase: StartAppUpdateDownloadUseCase,
    private val syncAccountSkUseCase: SyncAccountSkUseCase,
    private val innerPrefManager: InnerPrefManager,
) : ViewModel() {

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState

    // Events are consumed once by MainActivity; durable screen data stays in uiState.
    private val _event = Channel<MainEvent>(Channel.BUFFERED)
    val event = _event.receiveAsFlow()
    private val linkIconJobs = mutableMapOf<Long, Job>()
    private var appDownloadJob: Job? = null

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
        initializeDefaultLink()
        observeAccounts()
        observeLinks()
        refreshHomeData()
        resumeAppDownload()
    }

    private fun initializeDefaultLink() {
        if (innerPrefManager.insertLink.get()) return
        viewModelScope.launch {
            val existingLinks = linkRepository.observeLinks().first()
            if (existingLinks.isNotEmpty()) {
                innerPrefManager.insertLink.set(true)
                return@launch
            }

            val defaultLink = Link(
                id = null,
                title = "PRTS",
                url = "https://prts.wiki/w/",
                icon = "https://prts.wiki/public/favicon.ico",
            )
            linkRepository.insertLink(defaultLink)
                .onSuccess { innerPrefManager.insertLink.set(true) }
                .onFailure { error ->
                    _event.send(MainEvent.ShowError(error.message ?: "默认链接初始化失败"))
                }
        }
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
            linkRepository.observeLinks()
                .retryWhen { error, attempt ->
                    if (attempt == 0L) {
                        _event.send(MainEvent.ShowError(error.message ?: "链接列表加载失败"))
                    }
                    delay(
                        LINK_RETRY_DELAY_MS * (attempt + 1).coerceAtMost(
                            LINK_RETRY_MAX_DELAY_MS / LINK_RETRY_DELAY_MS
                        )
                    )
                    true
                }
                .collect { list ->
                    _uiState.update { it.copy(links = list) }
                }
        }
    }

    fun refreshHomeData() {
        fetchAnnounce()
        fetchBiliVideos()
        fetchAppRemoteConfig()
        loadApCache()
    }

    private fun fetchAppRemoteConfig() {
        viewModelScope.launch {
            remoteConfigRepository.fetchAppConfig()
                .onSuccess { config ->
                    _uiState.update { it.copy(appRemoteConfig = config) }
                }
                .onFailure { error ->
                    Timber.w(error, "远端应用配置加载失败，使用内置配置")
                }
        }
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
                    _event.send(
                        MainEvent.ShowError(userFacingError(error.message ?: "视频加载失败"))
                    )
                }
        }
    }

    fun loadApCache() {
        viewModelScope.launch {
            val cache = repoSkland.getApCache()
            val cacheAccountInfo = repoSkland.getCacheAccountInfo()
            _uiState.update {
                it.copy(
                    apCache = cache,
                    cacheAccountInfo = cacheAccountInfo.takeIf { owner -> owner.uid.isNotBlank() },
                )
            }
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
                            _uiState.update {
                                it.copy(
                                    pendingUpdate = PendingAppUpdate(
                                    version = check.version,
                                    versionCode = check.versionCode,
                                    changelog = check.content,
                                    url = check.link
                                )
                                )
                            }
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
            val normalizedUrl = normalizeLinkUrl(url) ?: return@launch
            val link = Link(id = null, title = title.trim(), url = normalizedUrl)
            linkRepository.insertLink(link)
                .onSuccess { id ->
                    refreshLinkIcon(link.copy(id = id))
                }
                .onFailure { error ->
                    _event.send(MainEvent.ShowError(error.message ?: "添加链接失败"))
                }
        }
    }

    fun updateLink(link: Link, title: String, url: String) {
        viewModelScope.launch {
            val normalizedUrl = normalizeLinkUrl(url) ?: return@launch
            val updated = link.copy(title = title.trim(), url = normalizedUrl)
            linkRepository.updateLink(updated)
                .onSuccess {
                    if (normalizedUrl != link.url || link.icon.isBlank()) {
                        refreshLinkIcon(updated)
                    }
                }
                .onFailure { error ->
                    _event.send(MainEvent.ShowError(error.message ?: "更新链接失败"))
                }
        }
    }

    fun deleteLink(link: Link) {
        viewModelScope.launch {
            link.id?.let { linkIconJobs.remove(it)?.cancel() }
            linkRepository.deleteLink(link)
                .onFailure { error ->
                    _event.send(MainEvent.ShowError(error.message ?: "删除链接失败"))
                }
        }
    }

    private suspend fun normalizeLinkUrl(url: String): String? {
        return LinkUrl.normalize(url).getOrElse { error ->
            _event.send(MainEvent.ShowError(error.message ?: "网址格式错误"))
            null
        }
    }

    private fun refreshLinkIcon(link: Link) {
        val id = link.id ?: return
        linkIconJobs.remove(id)?.cancel()
        linkIconJobs[id] = viewModelScope.launch {
            try {
                linkMetadataResolver.resolveIcon(link.url)
                    .getOrNull()
                    ?.takeIf(String::isNotBlank)
                    ?.let { icon ->
                        linkRepository.updateLink(link.copy(icon = icon))
                    }
            } finally {
                val currentJob = coroutineContext[Job]
                if (linkIconJobs[id] == currentJob) {
                    linkIconJobs.remove(id)
                }
            }
        }
    }

    fun dismissPendingUpdate() {
        _uiState.update { it.copy(pendingUpdate = null) }
    }

    fun downloadApp(url: String, expectedVersionCode: Long) {
        dismissPendingUpdate()
        if (appDownloadJob?.isActive == true) return
        collectDownload(startAppUpdateDownloadUseCase(url, expectedVersionCode))
    }

    fun acknowledgeDownloadResult() {
        _uiState.update { it.copy(downloadState = AppDownloadUiState.Idle) }
    }

    private fun resumeAppDownload() {
        startAppUpdateDownloadUseCase.resume()?.let(::collectDownload)
    }

    private fun collectDownload(statuses: kotlinx.coroutines.flow.Flow<DownloadStatus>) {
        if (appDownloadJob?.isActive == true) return
        appDownloadJob = viewModelScope.launch {
            try {
                statuses.collect { status ->
                    _uiState.update { state ->
                        state.copy(
                            downloadState = when (status) {
                                DownloadStatus.Started -> AppDownloadUiState.Downloading(null)
                                is DownloadStatus.Progress -> AppDownloadUiState.Downloading(
                                    status.percent.takeIf { status.totalBytes > 0L }
                                )
                                is DownloadStatus.Success ->
                                    AppDownloadUiState.Completed(status.filePath)
                                is DownloadStatus.Failed -> AppDownloadUiState.Failed(
                                    status.throwable.message ?: "下载更新失败"
                                )
                            }
                        )
                    }
                }
            } finally {
                appDownloadJob = null
            }
        }
    }

    private companion object {
        const val LINK_RETRY_DELAY_MS = 1_000L
        const val LINK_RETRY_MAX_DELAY_MS = 30_000L
    }
}
