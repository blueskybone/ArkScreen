package com.blueskybone.arkscreen.ui.realtime

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.blueskybone.arkscreen.domain.model.account.AccountSk
import com.blueskybone.arkscreen.domain.repository.AccountRepository
import com.blueskybone.arkscreen.domain.usecase.realtime.GetRealTimeUseCase
import com.blueskybone.arkscreen.ui.UiState
import com.blueskybone.arkscreen.ui.realtime.model.RealTimeMapper
import com.blueskybone.arkscreen.ui.realtime.model.RealTimeUi
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException

/**
 * Created by blueskybone
 * Date: 2026/3/19
 */
class RealTimeModel(
    private val getRealTimeUseCase: GetRealTimeUseCase,
    private val repo: AccountRepository
) : ViewModel() {

    private val _uiState = MutableLiveData<UiState>()
    val uiState: LiveData<UiState> get() = _uiState

    private val _realTimeUi = MutableLiveData<RealTimeUi>()
    val realTimeUi: LiveData<RealTimeUi> get() = _realTimeUi



    init {
        _uiState.value = UiState.Loading
        execute {
            repo.getCurrentAccountSk().onSuccess {acc ->
                if (acc == null)
                    _uiState.value = UiState.Warning("请先在 首页-账号管理 添加游戏账号")
                else {
                    loadRealTimeData(acc)
                }
            }
        }
    }

    private fun loadRealTimeData(account: AccountSk) {
        execute {
            getRealTimeUseCase(account).onSuccess { data ->
                _realTimeUi.value = RealTimeMapper.toUi(data)
            }.onFailure {
                _uiState.value = UiState.Error(it.message ?: "加载失败：请查看日志")
            }
        }
    }

    private fun execute(function: suspend () -> Unit) {
        viewModelScope.launch {
            try {
                function()
            } catch (e: CancellationException) {
                _uiState.value = UiState.Cancelled
            }
        }
    }
}