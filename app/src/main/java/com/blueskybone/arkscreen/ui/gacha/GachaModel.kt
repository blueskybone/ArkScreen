package com.blueskybone.arkscreen.ui.gacha

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.blueskybone.arkscreen.domain.model.account.Account
import com.blueskybone.arkscreen.domain.model.account.AccountEf
import com.blueskybone.arkscreen.domain.model.account.AccountGc
import com.blueskybone.arkscreen.domain.model.account.AccountSk
import com.blueskybone.arkscreen.domain.model.gacha.Record
import com.blueskybone.arkscreen.domain.repository.AccountRepository
import com.blueskybone.arkscreen.domain.repository.GachaRepository
import com.blueskybone.arkscreen.domain.usecase.gacha.SyncRecordsUseCase
import com.blueskybone.arkscreen.ui.account.model.AccountItemUiModel
import com.blueskybone.arkscreen.ui.gacha.model.GachaUiMapper
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
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

    private val gcListFlow = repoAcc.observeGcAcc()     // 账号列表（用于切换）
        .catch { emit(emptyList()) }

    val accountGcUiList: StateFlow<List<AccountItemUiModel>> =
        combine(gcListFlow, currentGcFlow) { list, current ->
            list.map { account ->
                AccountItemUiModel(
                    account = account,
                    isDefault = account.uid == current?.uid
                )
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private fun observeAccounts() {
        viewModelScope.launch {
            repoAcc.observeGcAcc().collect { list ->
                _uiState.update { it.copy(accList = list) }
            }
        }

        viewModelScope.launch {
            repoAcc.observeCurrentGcAcc().collect { account ->
                _uiState.update { it.copy(currAccount = account) }
            }
        }
    }

    init {
        observeAccounts()
        syncRecords()
    }

    private fun syncRecords() {
        execute {
            _uiState.update { it.copy(loading = true) }
            currentGcFlow.collect { acc ->
                if (acc == null)
                    _uiState.update { it.copy(warning = "请在 卡池账号管理 添加账号") }
                syncRecordsUseCase(acc!!)
                    .onSuccess {
                        _uiState.update { it.copy(loading = false) }
                    }.onFailure { error ->
                        _uiState.update { it.copy(error = error.message) }
                    }
            }
            recordsFlow.collect { list ->
                _uiState.update { it.copy(gachaUiSnapshot = GachaUiMapper.map(list)) }
            }
        }
    }

    fun deleteRecords() {
        execute {
            _uiState.update { it.copy(loading = true) }
            currentGcFlow.collect { acc ->
                if (acc == null)
                    _uiState.update { it.copy(warning = "请在 卡池账号管理 添加账号") }
                repo.deleteRecords(acc!!).onSuccess {
                    _uiState.update { it.copy(loading = false) }
                }.onFailure { error ->
                    _uiState.update { it.copy(error = error.message) }
                }
            }
            recordsFlow.collect { list ->
                _uiState.update { it.copy(gachaUiSnapshot = GachaUiMapper.map(list)) }
            }
        }

    }

    fun correctUnCateRecord() {
        execute {
            _uiState.update { it.copy(loading = true) }
            currentGcFlow.collect { acc ->
                if (acc == null)
                    _uiState.update { it.copy(warning = "请在 卡池账号管理 添加账号") }
                repo.correctUnCateRecord(acc!!).onSuccess {
                    _uiState.update { it.copy(loading = false) }
                }.onFailure { error ->
                    _uiState.update { it.copy(error = error.message) }
                }
            }
            recordsFlow.collect { list ->
                _uiState.update { it.copy(gachaUiSnapshot = GachaUiMapper.map(list)) }
            }
        }
    }

    fun checkoutAccount(account: Account) {
        execute {
            when (account) {
                is AccountSk -> repoAcc.setCurrentAccountSk(account)
                is AccountGc -> repoAcc.setCurrentAccountGc(account)
                is AccountEf -> Unit
            }
            syncRecords()
        }
    }

    private fun execute(block: suspend () -> Unit) {
        viewModelScope.launch {
            try {
                block()
            } catch (_: CancellationException) {
            }
        }
    }

}