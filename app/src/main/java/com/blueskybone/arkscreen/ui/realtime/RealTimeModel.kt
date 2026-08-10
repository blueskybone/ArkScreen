package com.blueskybone.arkscreen.ui.realtime

import android.annotation.SuppressLint
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.blueskybone.arkscreen.domain.AppError
import com.blueskybone.arkscreen.domain.repository.AccountRepository
import com.blueskybone.arkscreen.domain.model.account.AccountSk
import com.blueskybone.arkscreen.domain.usecase.account.SyncAccountSkUseCase
import com.blueskybone.arkscreen.domain.usecase.realtime.GetRealTimeUseCase
import com.blueskybone.arkscreen.domain.service.AppClock
import com.blueskybone.arkscreen.platform.widget.WidgetUpdateDispatcher
import com.blueskybone.arkscreen.ui.realtime.model.RealTimeMapper
import com.blueskybone.arkscreen.ui.realtime.model.RealTimeUi
import com.fasterxml.jackson.core.JsonProcessingException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.launch
import retrofit2.HttpException
import timber.log.Timber
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

enum class RealTimeFailureKind {
    NETWORK,
    TIMEOUT,
    AUTH_EXPIRED,
    OTHER,
}

sealed interface RealTimeScreenState {
    data object Loading : RealTimeScreenState
    data object Empty : RealTimeScreenState
    data class Content(val data: RealTimeUi, val syncedAt: Long) : RealTimeScreenState
    data class Error(val kind: RealTimeFailureKind) : RealTimeScreenState
}

class RealTimeModel(
    private val getRealTimeUseCase: GetRealTimeUseCase,
    private val repo: AccountRepository,
    private val widgetUpdates: WidgetUpdateDispatcher,
    private val syncAccountSkUseCase: SyncAccountSkUseCase,
    private val appClock: AppClock,
) : ViewModel() {

    private val _state = MutableLiveData<RealTimeScreenState>(RealTimeScreenState.Loading)
    val state: LiveData<RealTimeScreenState> get() = _state

    private var loadJob: Job? = null
    private var lastContent: RealTimeScreenState.Content? = null
    private var currentUid: String? = null
    private var syncing = false
    private val _syncingState = MutableLiveData(false)
    val syncingState: LiveData<Boolean> get() = _syncingState
    private val _event = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val event = _event.asSharedFlow()

    init {
        viewModelScope.launch {
            repo.observeCurrentSkAcc()
                .distinctUntilChangedBy { it?.uid }
                .collectLatest { account ->
                    val accountChanged = currentUid != account?.uid
                    currentUid = account?.uid
                    if (accountChanged) lastContent = null
                    loadAccount(account, accountChanged)
                }
        }
    }

    fun refresh() {
        if (syncing) return
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            loadAccount(repo.observeCurrentSkAcc().first(), accountChanged = false)
        }
    }

    fun reauthenticate(token: String, dId: String) {
        if (syncing) return
        viewModelScope.launch {
            syncAccountSkUseCase(SyncAccountSkUseCase.LoginWay.Token(token, dId))
                .onSuccess {
                    _event.emit("登录成功，正在重新同步")
                    refresh()
                }
                .onFailure { error ->
                    _event.emit(com.blueskybone.arkscreen.ui.common.userFacingError(error.message ?: "登录失败"))
                }
        }
    }

    private suspend fun loadAccount(account: AccountSk?, accountChanged: Boolean) {
        if (syncing) return
        syncing = true
        _syncingState.value = true
        if (lastContent == null || accountChanged) _state.value = RealTimeScreenState.Loading
        try {
            if (account == null) {
                _state.value = RealTimeScreenState.Empty
                return
            }
            getRealTimeUseCase(account)
                .onSuccess { data ->
                    widgetUpdates.renderAll()
                    val content = RealTimeScreenState.Content(
                        RealTimeMapper.toUi(
                            data,
                            account.official,
                            appClock.currentEpochSeconds(),
                        ),
                        System.currentTimeMillis(),
                    )
                    lastContent = content
                    _state.value = content
                }
                .onFailure { error -> handleFailure(error) }
        } catch (error: CancellationException) {
            throw error
        } catch (error: Exception) {
            handleFailure(error)
        } finally {
            syncing = false
            _syncingState.value = false
        }
    }

    @SuppressLint("NullSafeMutableLiveData")
    private suspend fun handleFailure(error: Throwable) {
        Timber.e(error, "Failed to load real-time data")
        val kind = classifyFailure(error)
        val userMessage = when (kind) {
            RealTimeFailureKind.NETWORK -> "网络连接失败，请检查网络后重试"
            RealTimeFailureKind.TIMEOUT -> "请求超时，请稍后重试"
            RealTimeFailureKind.AUTH_EXPIRED -> "登录状态已过期，请重新登录"
            RealTimeFailureKind.OTHER -> "加载失败，请查看日志"
        }
        val cached = lastContent
        if (cached != null) {
            _state.value = cached
            _event.emit(userMessage)
        } else {
            _state.value = RealTimeScreenState.Error(kind)
        }
    }

    private fun classifyFailure(error: Throwable): RealTimeFailureKind {
        val causes = generateSequence(error) { it.cause }.toList()
        return when {
            causes.any { it is AppError.AuthExpired } ||
                causes.filterIsInstance<HttpException>().any { it.code() == 401 || it.code() == 403 } ->
                RealTimeFailureKind.AUTH_EXPIRED

            causes.any { it is AppError.Timeout || it is SocketTimeoutException } ->
                RealTimeFailureKind.TIMEOUT

            causes.any {
                it is AppError.NetworkUnavailable ||
                    it is UnknownHostException ||
                    it is ConnectException ||
                    it is IOException
            } -> RealTimeFailureKind.NETWORK

            causes.any { it is AppError.DataParse || it is JsonProcessingException } ->
                RealTimeFailureKind.OTHER

            causes.any(::looksLikeAuthExpired) -> RealTimeFailureKind.AUTH_EXPIRED

            else -> RealTimeFailureKind.OTHER
        }
    }

    private fun looksLikeAuthExpired(error: Throwable): Boolean {
        val message = error.message.orEmpty().lowercase()
        return listOf(
            " 401", " 403", "token", "grant", "cred",
            "凭证", "登录过期", "登录状态已过期", "授权失败",
        ).any(message::contains)
    }
}
