package com.blueskybone.arkscreen.ui.account


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.blueskybone.arkscreen.domain.model.account.Account
import com.blueskybone.arkscreen.domain.model.account.AccountEf
import com.blueskybone.arkscreen.domain.model.account.AccountGc
import com.blueskybone.arkscreen.domain.model.account.AccountSk
import com.blueskybone.arkscreen.domain.repository.AccountRepository
import com.blueskybone.arkscreen.domain.usecase.account.SyncAccountGcUseCase
import com.blueskybone.arkscreen.domain.usecase.account.SyncAccountSkUseCase
import com.blueskybone.arkscreen.ui.account.model.AccountItemUiModel
import com.blueskybone.arkscreen.ui.UiStatus
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException

class AccountModel(
    private val repo: AccountRepository,
    private val syncAccountSkUseCase: SyncAccountSkUseCase,
    private val syncAccountGcUseCase: SyncAccountGcUseCase,
) : ViewModel() {

    private val _event = MutableSharedFlow<UiEvent>(extraBufferCapacity = 1)
    val event: SharedFlow<UiEvent> = _event

    private val _operationStatus = MutableStateFlow<UiStatus>(UiStatus.Idle)
    val operationStatus: StateFlow<UiStatus> = _operationStatus

    private val skListFlow = repo.observeSkAcc()
        .catch { emit(emptyList()) }

    private val gcListFlow = repo.observeGcAcc()
        .catch { emit(emptyList()) }

    private val efListFlow = repo.observeEfAcc()
        .catch { emit(emptyList()) }

    private val currentSkFlow = repo.observeCurrentSkAcc()
    private val currentGcFlow = repo.observeCurrentGcAcc()

    val accountSkUiList: StateFlow<List<AccountItemUiModel>> =
        combine(skListFlow, currentSkFlow) { list, current ->
            list.map { account ->
                AccountItemUiModel(
                    account = account,
                    isDefault = account.uid == current?.uid
                )
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val accountGcUiList: StateFlow<List<AccountItemUiModel>> =
        combine(gcListFlow, currentGcFlow) { list, current ->
            list.map { account ->
                AccountItemUiModel(
                    account = account,
                    isDefault = account.uid == current?.uid
                )
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val accountEfUiList: StateFlow<List<AccountItemUiModel>> =
        efListFlow.map { list ->
            list.map { account ->
                AccountItemUiModel(
                    account = account,
                    isDefault = false
                )
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun loginSklandByPhonePassword(phone: String, password: String) {
        execute {
            val way = SyncAccountSkUseCase.LoginWay.PhoneAndPassword(phone, password)
            val result = syncAccountSkUseCase(way)
            result.fold(
                onSuccess = { cnt ->
                    _event.emit(UiEvent.ShowToast("成功导入${cnt}条账号"))
                },
                onFailure = { error ->
                    _event.emit(UiEvent.ShowError(error.message ?: "登录失败"))
                }
            )
        }
    }

    fun loginSklandByToken(token: String, dId: String? = null) {
        execute {
            val way = SyncAccountSkUseCase.LoginWay.Token(token, dId)
            val result = syncAccountSkUseCase(way)
            result.fold(
                onSuccess = { cnt ->
                    _event.emit(UiEvent.ShowToast("成功导入${cnt}条账号"))
                },
                onFailure = { error ->
                    _event.emit(UiEvent.ShowError(error.message ?: "登录失败"))
                }
            )
        }
    }

    fun loginSklandByCookie(cookie: String) {
        execute {
            val way = SyncAccountSkUseCase.LoginWay.Cookie(cookie)
            val result = syncAccountSkUseCase(way)
            result.fold(
                onSuccess = { cnt ->
                    _event.emit(UiEvent.ShowToast("成功导入${cnt}条账号"))
                },
                onFailure = { error ->
                    _event.emit(UiEvent.ShowError(error.message ?: "登录失败"))
                }
            )
        }
    }

    fun loginGameByToken(
        token: String,
        akUserCenter: String,
        xrToken: String,
        channelMasterId: Int
    ) {
        execute {
            val way = SyncAccountGcUseCase.LoginWay.Token(
                token, akUserCenter, xrToken, channelMasterId
            )
            val result = syncAccountGcUseCase(way)
            result.fold(
                onSuccess = { cnt ->
                    _event.emit(UiEvent.ShowToast("成功导入${cnt}条账号"))
                },
                onFailure = { error ->
                    _event.emit(UiEvent.ShowError(error.message ?: "登录失败"))
                }
            )
        }
    }

    fun loginGameByCookie(cookie: String) {
        execute {
            val way = SyncAccountGcUseCase.LoginWay.Cookie(cookie)
            val result = syncAccountGcUseCase(way)
            result.fold(
                onSuccess = { cnt ->
                    _event.emit(UiEvent.ShowToast("成功导入${cnt}条账号"))
                },
                onFailure = { error ->
                    _event.emit(UiEvent.ShowError(error.message ?: "登录失败"))
                }
            )
        }
    }

    fun setDefaultAccount(account: Account) {
        execute {
            when (account) {
                is AccountSk -> repo.setCurrentAccountSk(account)
                is AccountGc -> repo.setCurrentAccountGc(account)
                is AccountEf -> Unit
            }
        }
    }

    fun deleteAccount(account: Account) {
        execute {
            repo.deleteAccount(account)
        }
    }

    private fun execute(block: suspend () -> Unit) {
        viewModelScope.launch {
            _operationStatus.value = UiStatus.Loading()
            try {
                block()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _event.emit(UiEvent.ShowError(e.message ?: "发生未知错误"))
            } finally {
                _operationStatus.value = UiStatus.Idle
            }
        }
    }

    fun generateAccountCookie(account: Account): Result<String> =
        runCatching { repo.accountCookieEncode(account) }
}
