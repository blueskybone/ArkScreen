package com.blueskybone.arkscreen.ui.gacha

import com.blueskybone.arkscreen.R
import com.blueskybone.arkscreen.ui.common.UiStrings
import com.blueskybone.arkscreen.ui.UiStatus
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.blueskybone.arkscreen.domain.model.gacha.EndfieldGachaRecord
import com.blueskybone.arkscreen.domain.model.gacha.EndfieldGachaPool
import com.blueskybone.arkscreen.domain.repository.EndfieldGachaRepository
import com.blueskybone.arkscreen.domain.model.account.AccountEf
import com.blueskybone.arkscreen.domain.repository.AccountRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class EndfieldGachaUiState(
    val accounts: List<AccountEf> = emptyList(),
    val selectedUid: String? = null,
    val records: List<EndfieldGachaRecord> = emptyList(),
    val pools: List<EndfieldGachaPool> = emptyList(),
    val status: UiStatus = UiStatus.Idle,
) {
    val loading: Boolean get() = status is UiStatus.Loading
    val error: String? get() = (status as? UiStatus.Error)?.message
    val progress: String get() = when (val current = status) {
        is UiStatus.Loading -> current.message.orEmpty()
        is UiStatus.Success -> current.message.orEmpty()
        else -> ""
    }
}

class EndfieldGachaViewModel internal constructor(
    private val accounts: AccountRepository,
    private val repository: EndfieldGachaRepository,
    private val texts: UiStrings,
    scope: CoroutineScope?,
) : ViewModel() {
    constructor(accounts: AccountRepository, repository: EndfieldGachaRepository, texts: UiStrings) : this(accounts, repository, texts, null)

    private val modelScope = scope ?: viewModelScope
    // Updated on the main thread; callbacks from IO threads are marshalled onto this scope.
    private var selectionVersion = 0L
    private var requestVersion = 0L
    private val _state = MutableStateFlow(EndfieldGachaUiState())
    val state = _state.asStateFlow()
    private var observeJob: Job? = null
    private var observePoolsJob: Job? = null
    private var syncJob: Job? = null

    private val _category = MutableStateFlow("char")
    val category = _category.asStateFlow()
    private val _recordFilter = MutableStateFlow(EndfieldRecordFilter())
    internal val recordFilter = _recordFilter.asStateFlow()

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    private val presentation = combine(
        state.map { Triple(it.selectedUid, it.records, it.pools) }.distinctUntilChanged(), category,
    ) { (uid, records, pools), type -> EndfieldGachaInput(uid, type, records, pools) }
        .distinctUntilChanged()
        .mapLatest { input -> withContext(Dispatchers.Default) { buildEndfieldGachaPresentation(input) } }
        .stateIn(modelScope, SharingStarted.Eagerly, null)

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    internal val pageData = combine(presentation, recordFilter) { data, filter -> data to filter }
        .mapLatest { (data, filter) ->
            data?.let { withContext(Dispatchers.Default) {
                EndfieldGachaPageData(it, filter, it.filterRows(filter))
            } }
        }.stateIn(modelScope, SharingStarted.Eagerly, null)

    fun selectCategory(type: String) {
        require(type == "char" || type == "weapon")
        if (_category.value == type) return
        _recordFilter.update { it.copy(pool = null, scope = null) }
        _category.value = type
    }

    internal fun setRecordFilter(filter: EndfieldRecordFilter) { _recordFilter.value = filter }

    init {
        modelScope.launch {
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
        if (uid == _state.value.selectedUid) return
        val account = _state.value.accounts.firstOrNull { it.uid == uid }
        if (uid != null && account == null) return
        _recordFilter.update { it.copy(pool = null, scope = null) }
        val selection = ++selectionVersion
        ++requestVersion
        account?.let(accounts::setCurrentAccountEf)
        syncJob?.cancel()
        observeJob?.cancel()
        observePoolsJob?.cancel()
        _state.update {
            it.copy(selectedUid = uid, records = emptyList(), pools = emptyList(), status = UiStatus.Idle)
        }
        if (uid == null) return
        observeJob = modelScope.launch {
            repository.observe(uid).collect { records ->
                if (selection == selectionVersion) _state.update { it.copy(records = records) }
            }
        }
        observePoolsJob = modelScope.launch {
            repository.observePools(uid).collect { pools ->
                if (selection == selectionVersion) _state.update { it.copy(pools = pools) }
            }
        }
        refresh()
    }

    fun refresh() {
        val account = _state.value.accounts.firstOrNull { it.uid == _state.value.selectedUid }
            ?: return
        if (_state.value.loading) return
        val request = ++requestVersion
        _state.update { it.copy(status = UiStatus.Loading(texts.get(R.string.status_authorizing))) }
        syncJob = modelScope.launch {
            try {
                repository.sync(account) { message ->
                    modelScope.launch {
                        if (request == requestVersion) _state.update { it.copy(status = UiStatus.Loading(message)) }
                    }
                }
                if (request == requestVersion) {
                    ++requestVersion // Reject any queued progress callbacks after completion.
                    _state.update { it.copy(status = UiStatus.Success(texts.get(R.string.status_sync_complete))) }
                }
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                if (request != requestVersion) return@launch
                ++requestVersion
                _state.update {
                    it.copy(status = UiStatus.Error(error.message ?: texts.get(R.string.error_sync_failed)))
                }
            }
        }
    }
}
