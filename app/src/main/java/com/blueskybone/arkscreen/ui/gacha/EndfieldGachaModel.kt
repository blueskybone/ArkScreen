package com.blueskybone.arkscreen.ui.gacha

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.blueskybone.arkscreen.data.local.room.EndfieldGachaRecord
import com.blueskybone.arkscreen.data.local.room.EndfieldGachaPool
import com.blueskybone.arkscreen.data.repository.EndfieldGachaRepository
import com.blueskybone.arkscreen.domain.model.account.AccountEf
import com.blueskybone.arkscreen.domain.repository.AccountRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class EndfieldGachaState(
    val accounts: List<AccountEf> = emptyList(),
    val selectedUid: String? = null,
    val records: List<EndfieldGachaRecord> = emptyList(),
    val pools: List<EndfieldGachaPool> = emptyList(),
    val loading: Boolean = false,
    val progress: String = "",
    val error: String? = null,
)

class EndfieldGachaModel(
    private val accounts: AccountRepository,
    private val repository: EndfieldGachaRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(EndfieldGachaState())
    val state = _state.asStateFlow()
    private var observeJob: Job? = null
    private var observePoolsJob: Job? = null
    private var syncJob: Job? = null

    init {
        viewModelScope.launch {
            combine(accounts.observeEfAcc(), accounts.observeCurrentEfAcc()) { available, current ->
                available to current
            }.collect { (available, current) ->
                val selected = available.firstOrNull { it.uid == current?.uid }
                    ?: available.firstOrNull { it.uid == _state.value.selectedUid }
                    ?: available.firstOrNull()
                _state.update { it.copy(accounts = available) }
                if (selected?.uid != _state.value.selectedUid) select(selected?.uid)
            }
        }
    }

    fun select(uid: String?) {
        _state.value.accounts.firstOrNull { it.uid == uid }?.let(accounts::setCurrentAccountEf)
        syncJob?.cancel()
        observeJob?.cancel()
        observePoolsJob?.cancel()
        _state.update {
            it.copy(selectedUid = uid, records = emptyList(), pools = emptyList(), loading = false,
                progress = "", error = null)
        }
        if (uid == null) return
        observeJob = viewModelScope.launch {
            repository.observe(uid).collect { records ->
                _state.update { it.copy(records = records) }
            }
        }
        observePoolsJob = viewModelScope.launch {
            repository.observePools(uid).collect { pools ->
                _state.update { it.copy(pools = pools) }
            }
        }
        refresh()
    }

    fun refresh() {
        val account = _state.value.accounts.firstOrNull { it.uid == _state.value.selectedUid }
            ?: return
        if (_state.value.loading) return
        syncJob = viewModelScope.launch {
            _state.update { it.copy(loading = true, progress = "正在授权…", error = null) }
            try {
                repository.sync(account) { message ->
                    _state.update { it.copy(progress = message) }
                }
                _state.update { it.copy(loading = false, progress = "同步完成") }
            } catch (error: Exception) {
                _state.update {
                    it.copy(loading = false, progress = "", error = error.message ?: "同步失败")
                }
            }
        }
    }
}
