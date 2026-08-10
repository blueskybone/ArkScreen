package com.blueskybone.arkscreen.ui.gacha

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.blueskybone.arkscreen.domain.model.account.Account
import com.blueskybone.arkscreen.domain.model.account.AccountGc
import com.blueskybone.arkscreen.domain.model.gacha.Record
import com.blueskybone.arkscreen.domain.repository.AccountRepository
import com.blueskybone.arkscreen.domain.repository.GachaRepository
import com.blueskybone.arkscreen.domain.usecase.gacha.SyncRecordsUseCase
import com.blueskybone.arkscreen.ui.gacha.model.GachaUiMapper
import com.blueskybone.arkscreen.ui.UiStatus
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException

/**
 * Created by blueskybone
 * Date: 2026/3/19
 */
class GachaModel(
    private val repo: GachaRepository,
    private val repoAcc: AccountRepository,
    private val syncRecordsUseCase: SyncRecordsUseCase
) : ViewModel() {

    private val currentGcFlow = repoAcc.observeCurrentGcAcc() // 当前账号

    @OptIn(ExperimentalCoroutinesApi::class)
    private val recordsFlow: Flow<List<Record>> = currentGcFlow // 当前账号的寻访记录
        .flatMapLatest { account ->
            val uid = account?.uid.orEmpty()
            if (uid.isBlank()) {
                flowOf(emptyList())
            } else {
                repo.observeRecords(uid)
            }
        }

    private val _uiState = MutableStateFlow(GachaUiState())
    val uiState: StateFlow<GachaUiState> = _uiState

    private val _event = Channel<GachaEvent>(Channel.BUFFERED)
    val event = _event.receiveAsFlow()

    private fun observeAccounts() {
        viewModelScope.launch {
            combine(repoAcc.observeGcAcc(), currentGcFlow) { accounts, current ->
                accounts to current
            }.collect { (accounts, current) ->
                _uiState.update {
                    it.copy(
                        accList = accounts,
                        currAccount = current,
                        status = if (current == null) {
                            UiStatus.Empty("请在卡池账号管理中添加账号")
                        } else it.status,
                    )
                }
            }
        }
    }

    private fun observeRecords() {
        viewModelScope.launch {
            recordsFlow.collect { records ->
                _uiState.update {
                    val snapshot = GachaUiMapper.map(records)
                    val selectedPoolStillExists = snapshot.gachaPoolStats.any { pool ->
                        pool.poolId == it.selectedPoolId
                    }
                    it.copy(
                        gachaUiSnapshot = snapshot,
                        selectedPoolId = if (selectedPoolStillExists) it.selectedPoolId else "ALL",
                    )
                }
            }
        }
    }

    init {
        observeAccounts()
        observeRecords()
        observeAccountSync()
    }

    private fun observeAccountSync() {
        viewModelScope.launch {
            currentGcFlow
                .distinctUntilChangedBy { it?.uid }
                .collectLatest { account ->
                    if (account == null) {
                        _uiState.update { it.copy(status = UiStatus.Empty("请在卡池账号管理中添加账号")) }
                        return@collectLatest
                    }

                    _uiState.update { it.copy(status = UiStatus.Loading("正在同步寻访记录")) }
                    syncRecordsUseCase(account).fold(
                        onSuccess = {
                            _uiState.update { it.copy(status = UiStatus.Success()) }
                        },
                        onFailure = { error ->
                            val message = error.message ?: "同步寻访记录失败"
                            _uiState.update { it.copy(status = UiStatus.Error(message)) }
                            _event.send(GachaEvent.ShowError(message))
                        },
                    )
                }
        }
    }

    fun deleteRecords() {
        execute {
            val account = requireCurrentAccount() ?: return@execute
            _uiState.update { it.copy(status = UiStatus.Loading()) }
            repo.deleteRecords(account).fold(
                onSuccess = { _uiState.update { it.copy(status = UiStatus.Success()) } },
                onFailure = { error -> reportError(error.message ?: "删除寻访记录失败") },
            )
        }
    }

    fun correctUnCateRecord() {
        execute {
            val account = requireCurrentAccount() ?: return@execute
            _uiState.update { it.copy(status = UiStatus.Loading()) }
            repo.correctUnCateRecord(account).fold(
                onSuccess = { _uiState.update { it.copy(status = UiStatus.Success()) } },
                onFailure = { error -> reportError(error.message ?: "修复卡池分类失败") },
            )
        }
    }

    fun exportFileBaseName(): String =
        "${_uiState.value.currAccount?.uid ?: "gacha"}_gacha_records"

    fun checkoutAccount(account: Account) {
        execute {
            if (account is AccountGc) {
                repoAcc.setCurrentAccountGc(account)
                    .onFailure { error ->
                        val message = error.message ?: "切换账号失败"
                        reportError(message)
                    }
            }
        }
    }

    fun selectPool(poolId: String) {
        _uiState.update { state ->
            if (state.selectedPoolId == poolId) state else state.copy(selectedPoolId = poolId)
        }
    }

    private fun requireCurrentAccount(): AccountGc? {
        val account = _uiState.value.currAccount
        if (account == null) {
            _uiState.update { it.copy(status = UiStatus.Empty("请在卡池账号管理中添加账号")) }
            _event.trySend(GachaEvent.ShowMessage("请在卡池账号管理中添加账号"))
        }
        return account
    }

    private fun execute(block: suspend () -> Unit) {
        viewModelScope.launch {
            try {
                block()
            } catch (_: CancellationException) {
            } catch (error: Exception) {
                reportError(error.message ?: "操作失败")
            }
        }
    }

    private suspend fun reportError(message: String) {
        _uiState.update { it.copy(status = UiStatus.Error(message)) }
        _event.send(GachaEvent.ShowError(message))
    }

}
