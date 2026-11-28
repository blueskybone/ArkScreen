package com.blueskybone.arkscreen.ui.character

import com.blueskybone.arkscreen.R
import com.blueskybone.arkscreen.ui.common.UiStrings
import com.blueskybone.arkscreen.data.local.pref.SettingPrefManager
import com.blueskybone.arkscreen.ui.UiStatus
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.blueskybone.arkscreen.domain.model.operator.endfield.EndfieldCharacter
import com.blueskybone.arkscreen.domain.model.account.AccountEf
import com.blueskybone.arkscreen.domain.repository.AccountRepository
import com.blueskybone.arkscreen.domain.repository.SklandRepository
import com.blueskybone.arkscreen.domain.model.ConfigType
import com.blueskybone.arkscreen.domain.model.operator.endfield.EndfieldOperatorInfo
import com.blueskybone.arkscreen.domain.repository.GameResourceRepository
import com.blueskybone.arkscreen.domain.usecase.operator.EndfieldOperatorCatalog
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.lastOrNull
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class EndfieldCharUiState(
    val accounts: List<AccountEf> = emptyList(),
    val selectedUid: String? = null,
    val characters: List<EndfieldCharacter> = emptyList(),
    val visibleCharacters: List<EndfieldCharacter> = emptyList(),
    val filters: EndfieldCharFilters = EndfieldCharFilters(),
    val catalog: List<EndfieldOperatorInfo> = emptyList(),
    val catalogError: String? = null,
    val showMissing: Boolean = false,
    val avatarUrl: String? = null,
    val status: UiStatus = UiStatus.Idle,
    val grid: Boolean = false,
) {
    val missingCharacters: List<EndfieldOperatorInfo> get() =
        if (status is UiStatus.Success) EndfieldOperatorCatalog.missing(catalog, characters) else emptyList()
    val visibleMissing: List<EndfieldOperatorInfo> get() = EndfieldCharList.visibleMissing(missingCharacters, filters)
    val filterOptions: List<List<String>> get() = (0..2).map { kind ->
        (characters.mapNotNull { EndfieldCharList.value(it, kind) } +
            catalog.map { EndfieldCharList.value(it, kind) }).distinct().sortedDescending()
    }
    val loading: Boolean get() = status is UiStatus.Loading
    val error: String? get() = (status as? UiStatus.Error)?.message
}

class EndfieldCharViewModel(
    private val accounts: AccountRepository,
    private val skland: SklandRepository,
    private val settings: SettingPrefManager,
    private val texts: UiStrings,
    private val resources: GameResourceRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(EndfieldCharUiState(grid = settings.endfieldAssetsGrid.get()))
    val uiState = _uiState.asStateFlow()
    private var loadJob: Job? = null
    private var requestVersion = 0

    fun setShowMissing(show: Boolean) {
        _uiState.update { it.copy(showMissing = show) }
    }

    fun toggleViewType() {
        settings.endfieldAssetsGrid.set(!settings.endfieldAssetsGrid.get())
    }

    init {
        viewModelScope.launch {
            settings.endfieldAssetsGrid.flow().collect { grid ->
                _uiState.update { it.copy(grid = grid) }
            }
        }
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

    fun setFilters(filters: EndfieldCharFilters) {
        _uiState.update { state ->
            if (state.filters == filters) state else state.copy(
                filters = filters,
                visibleCharacters = EndfieldCharList.visible(state.characters, filters),
            )
        }
    }

    private fun load(account: AccountEf?) {
        val version = ++requestVersion
        loadJob?.cancel()
        if (account == null) {
            _uiState.update { it.copy(avatarUrl = null, characters = emptyList(), visibleCharacters = emptyList(),
                catalog = emptyList(), catalogError = null, status = UiStatus.Idle) }
            return
        }
        loadJob = viewModelScope.launch {
            _uiState.update { it.copy(avatarUrl = null, characters = emptyList(), visibleCharacters = emptyList(),
                catalog = emptyList(), catalogError = null, status = UiStatus.Loading()) }
            val catalogRequest = async {
                resources.syncResource(ConfigType.ENDFIELD_CHAR_CATALOG).lastOrNull()
                resources.getEndfieldCharCatalog()
            }
            val assets = skland.fetchEndfieldCharacters(account)
            val catalogResult = catalogRequest.await()
            assets
                .onSuccess { (characters, avatarUrl) ->
                    if (version != requestVersion) return@onSuccess
                    _uiState.update {
                        it.copy(
                            avatarUrl = avatarUrl,
                            characters = characters,
                            visibleCharacters = EndfieldCharList.visible(characters, it.filters),
                            catalog = catalogResult.getOrNull().orEmpty(),
                            catalogError = catalogResult.exceptionOrNull()?.let { texts.get(R.string.endfield_catalog_failed) },
                            status = UiStatus.Success(),
                        )
                    }
                }
                .onFailure { error ->
                    if (version != requestVersion) return@onFailure
                    _uiState.update { it.copy(status = UiStatus.Error(error.message ?: texts.get(R.string.error_load_failed))) }
                }
        }
    }
}
