package com.blueskybone.arkscreen.ui.recruit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.blueskybone.arkscreen.domain.model.ConfigType
import com.blueskybone.arkscreen.domain.repository.ResourceRepository
import com.blueskybone.arkscreen.domain.usecase.recruit.CalcResultUseCase
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException

/**
 * Created by blueskybone
 * Date: 2026/3/19
 */
class RecruitModel(
    private val repo: ResourceRepository,
    private val calcResultUseCase: CalcResultUseCase
) : ViewModel() {

    //UI 状态
    private val _uiState = MutableStateFlow(RecruitUiState())
    val uiState: StateFlow<RecruitUiState> = _uiState

    //标签选中状态
    private val selectedTagsFlow = MutableStateFlow<List<String>>(emptyList())

    init {
        observeTags()
        loadData()
    }


    @OptIn(FlowPreview::class)
    private fun observeTags() {
        execute {
            selectedTagsFlow
                .debounce(300) // ✅ 防抖
                .distinctUntilChanged()
                .collect { tags ->
                    calculate(tags)
                }
        }
    }


    fun toggleTag(tag: String) {
        val state = _uiState.value

        val newTags = if (tag in state.selectedTags) {
            state.selectedTags - tag
        } else {
            if (state.selectedTags.size >= 6) return
            state.selectedTags + tag
        }

        _uiState.update {
            it.copy(selectedTags = newTags)
        }

        // 不直接算，而是发给 Flow
        selectedTagsFlow.value = newTags
    }
    private fun loadData() {
        execute {
            val current = _uiState.value

            _uiState.value = current.copy(loading = true)

            repo.syncResource(ConfigType.RECRUIT_DB)

            repo.getRecruitDb()
                .onSuccess { db ->
                    _uiState.value = current.copy(
                        loading = false,
                        update = db.update.date,
                        newOpe = db.newOpe.name
                    )
                }
                .onFailure {
                    _uiState.value = current.copy(
                        loading = false,
                        error = "json解析错误"
                    )
                }
        }
    }

    private fun calculate(tags: List<String>) {
        execute {
            calcResultUseCase(tags)
                .onSuccess { result ->
                    _uiState.value = _uiState.value.copy(result = result)
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        error = error.message ?: "发生未知错误"
                    )
                }
        }
    }

    fun reset() {
        _uiState.value = _uiState.value.copy(
            selectedTags = emptyList(),
            result = emptyList()
        )
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