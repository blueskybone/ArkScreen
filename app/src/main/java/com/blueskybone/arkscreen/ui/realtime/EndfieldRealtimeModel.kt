package com.blueskybone.arkscreen.ui.realtime

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.blueskybone.arkscreen.domain.model.account.AccountEf
import com.blueskybone.arkscreen.domain.model.realtime.EndfieldRealtimeSnapshot
import com.blueskybone.arkscreen.domain.repository.AccountRepository
import com.blueskybone.arkscreen.domain.repository.SklandRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class EndfieldRealtimeUiState(
    val accounts: List<AccountEf> = emptyList(),
    val selectedUid: String? = null,
    val snapshot: EndfieldRealtimeSnapshot? = null,
    val loading: Boolean = false,
    val error: String? = null,
)

class EndfieldRealtimeModel(
    private val accounts: AccountRepository,
    private val skland: SklandRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(EndfieldRealtimeUiState())
    val uiState = _uiState.asStateFlow()
    private var loadJob: Job? = null
    private var requestVersion = 0

    init {
        viewModelScope.launch {
            combine(accounts.observeEfAcc(), accounts.observeCurrentEfAcc()) { available, current ->
                available to current
            }.collect { (available, current) ->
                val previous = _uiState.value.accounts.firstOrNull {
                    it.uid == _uiState.value.selectedUid
                }
                val selected = available.firstOrNull { it.uid == current?.uid }
                    ?: available.firstOrNull { it.uid == _uiState.value.selectedUid }
                    ?: available.firstOrNull()
                val changed = selected != previous || selected?.uid != _uiState.value.selectedUid
                _uiState.update { it.copy(accounts = available, selectedUid = selected?.uid) }
                if (changed) load(selected)
            }
        }
    }

    fun select(uid: String) {
        val account = _uiState.value.accounts.firstOrNull { it.uid == uid } ?: return
        if (uid == _uiState.value.selectedUid) return
        accounts.setCurrentAccountEf(account)
        _uiState.update { it.copy(selectedUid = uid) }
        load(account)
    }

    fun refresh() {
        load(_uiState.value.accounts.firstOrNull { it.uid == _uiState.value.selectedUid })
    }

    private fun load(account: AccountEf?) {
        val version = ++requestVersion
        loadJob?.cancel()
        if (account == null) {
            _uiState.update { it.copy(snapshot = null, loading = false, error = null) }
            return
        }
        loadJob = viewModelScope.launch {
            _uiState.update { it.copy(snapshot = null, loading = true, error = null) }
            skland.fetchEndfieldRealtime(account)
                .onSuccess { snapshot ->
                    if (version != requestVersion) return@onSuccess
                    _uiState.update { it.copy(snapshot = snapshot, loading = false) }
                }
                .onFailure { error ->
                    if (version != requestVersion) return@onFailure
                    _uiState.update {
                        it.copy(loading = false, error = error.message ?: "加载失败")
                    }
                }
        }
    }
}
