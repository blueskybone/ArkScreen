package com.blueskybone.arkscreen.ui.widget

import com.blueskybone.arkscreen.R
import com.blueskybone.arkscreen.ui.common.UiStrings
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.blueskybone.arkscreen.domain.model.widget.WidgetConfig
import com.blueskybone.arkscreen.domain.model.cache.RealtimeCacheSnapshot
import com.blueskybone.arkscreen.domain.model.attendance.AccountAttendanceState
import com.blueskybone.arkscreen.domain.model.account.AccountType
import com.blueskybone.arkscreen.domain.repository.AccountRepository
import com.blueskybone.arkscreen.domain.repository.AttendanceStateRepository
import com.blueskybone.arkscreen.domain.repository.SklandRepository
import com.blueskybone.arkscreen.domain.repository.WidgetConfigRepository
import com.blueskybone.arkscreen.domain.usecase.widget.WidgetAccountResolver
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class WidgetEditorUiState(
    val original: WidgetConfig? = null,
    val draft: WidgetConfig? = null,
    val loading: Boolean = false,
    val saving: Boolean = false,
) {
    val dirty: Boolean get() = original != draft
}

sealed interface WidgetEditorEvent {
    data class Saved(val widgetId: Int) : WidgetEditorEvent
    data class Error(val message: String) : WidgetEditorEvent
}

data class WidgetPreviewData(val cache: RealtimeCacheSnapshot, val attendance: AccountAttendanceState?)

class WidgetEditorViewModel internal constructor(
    private val configs: WidgetConfigRepository,
    private val accountRepository: AccountRepository,
    private val skland: SklandRepository,
    private val attendance: AttendanceStateRepository,
    private val texts: UiStrings,
    scope: CoroutineScope?,
) : ViewModel() {
    constructor(configs: WidgetConfigRepository, accounts: AccountRepository, skland: SklandRepository,
                attendance: AttendanceStateRepository, texts: UiStrings) : this(configs, accounts, skland, attendance, texts, null)
    private val modelScope = scope ?: viewModelScope
    private val _state = MutableStateFlow(WidgetEditorUiState())
    val state = _state.asStateFlow()
    private val events = Channel<WidgetEditorEvent>(Channel.BUFFERED)
    val event = events.receiveAsFlow()
    val accounts = accountRepository.observeSkAcc()
        .stateIn(modelScope, SharingStarted.Eagerly, emptyList())
    private var loadJob: Job? = null
    private var generation = 0L

    fun load(id: Int, restore: (WidgetConfig) -> WidgetConfig = { it }) {
        if (_state.value.draft?.appWidgetId == id) return
        val request = ++generation
        loadJob?.cancel()
        _state.update { it.copy(loading = true) }
        loadJob = modelScope.launch {
            try {
                val config = configs.load(id)
                if (request != generation) return@launch
                _state.update { it.copy(original = config, draft = restore(config), loading = false) }
            } catch (e: CancellationException) { throw e }
            catch (e: Exception) {
                if (request != generation) return@launch
                _state.update { it.copy(loading = false) }
                events.send(WidgetEditorEvent.Error(texts.get(R.string.error_load_retry)))
            }
        }
    }

    fun updateDraft(value: WidgetConfig?) {
        if (value == null) {
            ++generation
            loadJob?.cancel()
            _state.update { it.copy(original = null, draft = null, loading = false) }
        } else _state.update { it.copy(draft = value) }
    }

    fun save() {
        val snapshot = _state.value
        val config = snapshot.draft ?: return
        if (snapshot.saving || snapshot.loading) return
        _state.update { it.copy(saving = true) }
        modelScope.launch {
            try {
                configs.save(config)
                _state.update {
                    if (it.draft?.appWidgetId == config.appWidgetId) it.copy(original = config) else it
                }
                events.send(WidgetEditorEvent.Saved(config.appWidgetId))
            } catch (e: CancellationException) { throw e }
            catch (e: Exception) { events.send(WidgetEditorEvent.Error(texts.get(R.string.error_save_retry))) }
            finally { _state.update { it.copy(saving = false) } }
        }
    }

    suspend fun preview(config: WidgetConfig): WidgetPreviewData {
        val account = WidgetAccountResolver.resolve(config, accountRepository.observeSkAcc().first(),
            accountRepository.observeCurrentSkAcc().first())
        return WidgetPreviewData(
            account?.let { skland.getRealtimeCache(it) } ?: RealtimeCacheSnapshot(),
            account?.let { attendance.get(AccountType.SK, it.uid) },
        )
    }
}
