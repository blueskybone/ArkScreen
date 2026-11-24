package com.blueskybone.arkscreen.ui.recruit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.blueskybone.arkscreen.data.local.pref.SettingPrefManager
import com.blueskybone.arkscreen.domain.model.ConfigType
import com.blueskybone.arkscreen.domain.repository.GameResourceRepository
import com.blueskybone.arkscreen.domain.service.RecruitDatabaseProvider
import com.blueskybone.arkscreen.domain.model.ResourceSyncStatus
import com.blueskybone.arkscreen.domain.usecase.recruit.CalcResultUseCase
import com.blueskybone.arkscreen.ui.UiStatus
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException
import timber.log.Timber

/**
 * Created by blueskybone
 * Date: 2026/3/19
 */
class RecruitModel(
    private val repo: GameResourceRepository,
    private val databaseProvider: RecruitDatabaseProvider,
    private val calcResultUseCase: CalcResultUseCase,
    private val settings: SettingPrefManager,
) : ViewModel() {

    //UI 状态
    private val initialHideLowRarity = settings.recruitHideLowRarity.get()
    private val _uiState = MutableStateFlow(
        RecruitUiState(hideLowRarity = initialHideLowRarity)
    )
    val uiState: StateFlow<RecruitUiState> = _uiState

    private val _event = Channel<RecruitEvent>(Channel.BUFFERED)
    val event = _event.receiveAsFlow()

    //标签选中状态
    private val selectedTagsFlow = MutableStateFlow<List<String>>(emptyList())
    private val hideLowRarityFlow = MutableStateFlow(initialHideLowRarity)

    //允许最大选择tag数目
    private val maxSelectTagNum = 6

    init {
        observeTags()
        loadData()
    }


    @OptIn(FlowPreview::class)
    private fun observeTags() {
        execute {
            combine(selectedTagsFlow, hideLowRarityFlow) { tags, hideLowRarity ->
                tags to hideLowRarity
            }
                .debounce(500L)
                .distinctUntilChanged()
                .collect { (tags, hideLowRarity) ->
                    calculate(tags, hideLowRarity)
                }
        }
    }


    fun toggleTag(tag: String) {
        val state = _uiState.value

        val newTags = if (tag in state.selectedTags) {
            state.selectedTags - tag
        } else {
            if (state.selectedTags.size >= maxSelectTagNum) {
                _event.trySend(RecruitEvent.SelectionLimitReached)
                return
            }
            state.selectedTags + tag
        }

        _uiState.update {
            it.copy(selectedTags = newTags)
        }

        // 不直接算，而是发给 Flow
        selectedTagsFlow.value = newTags
    }

    fun setHideLowRarity(hidden: Boolean) {
        if (_uiState.value.hideLowRarity == hidden) return
        settings.recruitHideLowRarity.set(hidden)
        _uiState.update { it.copy(hideLowRarity = hidden) }
        hideLowRarityFlow.value = hidden
    }

    private fun loadData() {
        execute {
            _uiState.update { it.copy(status = UiStatus.Loading()) }

            repo.syncResource(ConfigType.RECRUIT_DB).collect { status ->
                when (status) {
                    is ResourceSyncStatus.Updated -> databaseProvider.clearCache()
                    // 在线更新失败时仍可继续使用安装包内置或本地缓存的公招数据。
                    is ResourceSyncStatus.Failed -> {
                        Timber.tag("Recruit").w(status.throwable, "Recruit database refresh failed; using local data")
                    }
                    else -> Unit
                }
            }

            repo.getRecruitDb()
                .onSuccess { db ->
                    _uiState.update { it.copy(
                        status = UiStatus.Success(),
                        update = db.update.date,
                        newOpe = db.newOpe.name
                    ) }
                }
                .onFailure { error ->
                    Timber.tag("Recruit").e(error, "Recruit database parsing failed")
                    _uiState.update { it.copy(status = UiStatus.Error("json解析错误")) }
                    _event.send(RecruitEvent.ShowError("公招数据解析失败"))
                }
        }
    }

    private fun calculate(tags: List<String>, hideLowRarity: Boolean) {
        execute {
            calcResultUseCase(tags, filter = hideLowRarity)
                .onSuccess { result ->
                    _uiState.value = _uiState.value.copy(result = result)
                }
                .onFailure { error ->
                    Timber.tag("Recruit").e(error, "Recruit calculation failed")
                    _uiState.update { it.copy(status = UiStatus.Error(error.message ?: "发生未知错误")) }
                    _event.send(RecruitEvent.ShowError(error.message ?: "公招计算失败"))
                }
        }
    }

    fun reset() {
        _uiState.value = _uiState.value.copy(
            selectedTags = emptyList(),
            result = emptyList()
        )
        selectedTagsFlow.value = emptyList()
    }

    private fun execute(block: suspend () -> Unit) {
        viewModelScope.launch {
            try {
                block()
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                val message = error.message ?: "公招数据加载失败"
                _uiState.update { it.copy(status = UiStatus.Error(message)) }
                _event.send(RecruitEvent.ShowError(message))
            }
        }
    }
}
