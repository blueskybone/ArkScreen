package com.blueskybone.arkscreen.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.blueskybone.arkscreen.DataUiState
import com.blueskybone.arkscreen.RecruitDb
import com.blueskybone.arkscreen.task.recruit.RecruitManager
import com.blueskybone.arkscreen.task.recruit.RecruitManager.RecruitResult
import com.blueskybone.arkscreen.util.getEleCombination
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 *   Created by blueskybone
 *   Date: 2025/1/22
 */
class RecruitModel : ViewModel() {

    private lateinit var recruitManager: RecruitManager

    private val _result = MutableLiveData<List<RecruitResult>>()
    val result: LiveData<List<RecruitResult>> get() = _result

    private val _uiState = MutableLiveData<DataUiState>()
    val uiState: LiveData<DataUiState> get() = _uiState

    private val _update = MutableLiveData<String>()
    val update: LiveData<String> get() = _update

    init {
        viewModelScope.launch {
            _uiState.value = DataUiState.Loading("LOADING...")
            withContext(Dispatchers.IO) {
                try{
                    RecruitDb.updateFile()
                    _update.postValue(RecruitDb.updateTime())
                    recruitManager = RecruitManager.instance
                    _uiState.postValue(DataUiState.Success(""))
                }catch (e: Exception) {
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
            val recruitResultList = mutableListOf<RecruitResult>()
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