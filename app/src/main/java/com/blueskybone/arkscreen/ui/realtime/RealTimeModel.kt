package com.blueskybone.arkscreen.ui.realtime

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.blueskybone.arkscreen.domain.repository.AccountRepository
import com.blueskybone.arkscreen.domain.usecase.realtime.GetRealTimeUseCase
import com.blueskybone.arkscreen.ui.realtime.model.RealTimeMapper
import com.blueskybone.arkscreen.ui.realtime.model.RealTimeUi
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

sealed interface RealTimeScreenState {
    data object Loading : RealTimeScreenState
    data object Empty : RealTimeScreenState
    data class Content(val data: RealTimeUi) : RealTimeScreenState
    data class Error(val message: String?) : RealTimeScreenState
}

class RealTimeModel(
    private val getRealTimeUseCase: GetRealTimeUseCase,
    private val repo: AccountRepository,
) : ViewModel() {

    private val _state = MutableLiveData<RealTimeScreenState>(RealTimeScreenState.Loading)
    val state: LiveData<RealTimeScreenState> get() = _state

    private var loadJob: Job? = null

    init {
        load()
    }

    private fun load() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            _state.value = RealTimeScreenState.Loading
            try {
                val account = repo.observeCurrentSkAcc().first()
                if (account == null) {
                    _state.value = RealTimeScreenState.Empty
                    return@launch
                }
                getRealTimeUseCase(account)
                    .onSuccess { data ->
                        _state.value = RealTimeScreenState.Content(
                            RealTimeMapper.toUi(data, account.official)
                        )
                    }
                    .onFailure { error ->
                        _state.value = RealTimeScreenState.Error(error.message)
                    }
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                _state.value = RealTimeScreenState.Error(error.message)
            }
        }
    }
}
