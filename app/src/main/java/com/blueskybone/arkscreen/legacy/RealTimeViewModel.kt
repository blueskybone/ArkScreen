package com.blueskybone.arkscreen.legacy

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.blueskybone.arkscreen.data.local.pref.PrefManager
import com.blueskybone.arkscreen.ui.DataUiState
import com.blueskybone.arkscreen.ui.realtime.model.RealTimeUi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.java.KoinJavaComponent

/**
 *   Created by blueskybone
 *   Date: 2025/1/17
 */

@Deprecated("重构结束删除")
class RealTimeViewModel : ViewModel() {
    private val prefManager: PrefManager by KoinJavaComponent.getKoin().inject()

    private val _uiState = MutableLiveData<DataUiState>()
    val uiState: LiveData<DataUiState> get() = _uiState
    var realTimeUi: RealTimeUi? = null


    init {
        viewModelScope.launch {
            _uiState.value = DataUiState.Loading("加载中...")
            withContext(Dispatchers.IO) {
//                loadRealTimeData()
            }
        }
    }

//    private suspend fun loadRealTimeData() {
//        val accountSk = prefManager.baseAccountSk.get()
//        if (accountSk.uid == "") {
//            _uiState.postValue(DataUiState.Error("请先在 账号管理 添加游戏账号"))
//            return
//        }
//        try {
//            val realTimeData = getRealTimeData(accountSk) ?: return
//            setCaches(prefManager, realTimeData)
//            realTimeUi = processData(realTimeData, accountSk.official)
//            _uiState.postValue(DataUiState.Success(""))
//        } catch (e: Exception) {
//            _uiState.postValue(DataUiState.Error("加载失败: ${e.message }"))
//        }
//    }
}