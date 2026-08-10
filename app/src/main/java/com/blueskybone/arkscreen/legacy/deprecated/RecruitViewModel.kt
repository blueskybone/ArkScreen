package com.blueskybone.arkscreen.legacy.deprecated

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.blueskybone.arkscreen.legacy.RecruitDb
import com.blueskybone.arkscreen.legacy.RecruitManager
import com.blueskybone.arkscreen.ui.DataUiState
import com.blueskybone.arkscreen.util.getEleCombination
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 *   Created by blueskybone
 *   Date: 2025/1/22
 */
class RecruitViewModel : ViewModel() {

    private lateinit var recruitManager: RecruitManager

    private val _result = MutableLiveData<List<RecruitManager.RecruitResult>>()
    val result: LiveData<List<RecruitManager.RecruitResult>> get() = _result

    private val _uiState = MutableLiveData<DataUiState>()
    val uiState: LiveData<DataUiState> get() = _uiState

    private val _update = MutableLiveData<String>()
    val update: LiveData<String> get() = _update

    private val _newOpe = MutableLiveData<List<String>>()
    val newOpe: LiveData<List<String>> get() = _newOpe

    init {
        viewModelScope.launch {
            _uiState.value = DataUiState.Loading("加载中...")
            withContext(Dispatchers.IO) {
                try {
                    recruitManager = RecruitManager.Companion.instance
                    _update.postValue(RecruitDb.updateTime())
                    _newOpe.postValue(RecruitDb.newOpes())
                    _uiState.postValue(DataUiState.Success(""))
                } catch (e: Exception) {
                    e.printStackTrace()
                    _uiState.postValue(DataUiState.Error(e.message ?: "Unknown error"))
                    return@withContext
                }
            }
            reset()
        }
    }

    fun startCalculate(tags: List<String>) {
        viewModelScope.launch {
            val tagsList = getEleCombination(tags)
            val recruitResultList = mutableListOf<RecruitManager.RecruitResult>()
            for (tagsCom in tagsList) {
                val recruitResult = recruitManager.getRecruitResult(tagsCom, false)
                if (recruitResult.operators.isNotEmpty()) {
                    recruitResult.sort()
                    recruitResultList.add(recruitResult)
                }
            }
            val finalList = recruitResultList.toList().sorted()
            _result.postValue(finalList)
        }
    }

    fun reset() {
        _result.value = ArrayList()
    }

}