package com.blueskybone.arkscreen.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.blueskybone.arkscreen.DataUiState
import com.blueskybone.arkscreen.network.NetWorkTask.Companion.getGameInfoConnectionTaskTest
import com.blueskybone.arkscreen.network.avatarUrl
import com.blueskybone.arkscreen.playerinfo.ApCache
import com.blueskybone.arkscreen.playerinfo.LaborCache
import com.blueskybone.arkscreen.playerinfo.RealTimeData
import com.blueskybone.arkscreen.playerinfo.RealTimeUi
import com.blueskybone.arkscreen.playerinfo.geneRealTimeData
import com.blueskybone.arkscreen.preference.PrefManager
import com.blueskybone.arkscreen.room.AccountSk
import com.blueskybone.arkscreen.util.TimeUtils.getCurrentTs
import com.blueskybone.arkscreen.util.TimeUtils.getDayNum
import com.blueskybone.arkscreen.util.TimeUtils.getRemainTimeStr
import com.blueskybone.arkscreen.util.TimeUtils.getTimeStr
import com.hjq.toast.Toaster
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.java.KoinJavaComponent.getKoin
import java.net.URLEncoder

/**
 *   Created by blueskybone
 *   Date: 2025/1/17
 */


class RealTimeModel : ViewModel() {
    private val prefManager: PrefManager by getKoin().inject()

    private val _uiState = MutableLiveData<DataUiState>()
    val uiState: LiveData<DataUiState> get() = _uiState
    var realTimeUi: RealTimeUi? = null


    init {
        viewModelScope.launch {
            _uiState.value = DataUiState.Loading("LOADING...")
            withContext(Dispatchers.IO) {
                loadRealTimeData()
            }
        }
    }

    private suspend fun loadRealTimeData() {
        val accountSk = prefManager.baseAccountSk.get()
        //not a good practice. for null/empty situation, use a simple flag,
        //add 'isnull' to AccountSk
        if (accountSk.uid == "") {
            _uiState.postValue(DataUiState.Error("未登录"))
            return
        }
        try {
            val realTimeData = getRealTimeData(accountSk) ?: return
            //store ap to cache
            val apCache = ApCache(
                realTimeData.currentTs,
                realTimeData.apInfo.remainSecs,
                realTimeData.apInfo.recoverTime,
                realTimeData.apInfo.max,
                realTimeData.apInfo.current,
                false
            )
            val laborCache = LaborCache(
                realTimeData.currentTs,
                realTimeData.labor.remainSecs,
                realTimeData.labor.max,
                realTimeData.labor.current,
                false
            )
            prefManager.apCache.set(apCache)
            prefManager.laborCache.set(laborCache)

            realTimeUi = processData(realTimeData, accountSk.official)
            _uiState.postValue(DataUiState.Success(""))
        } catch (e: Exception) {
            _uiState.postValue(DataUiState.Error(e.message ?: "LOADING FAILED"))
        }
    }

    private fun processData(data: RealTimeData, official: Boolean): RealTimeUi {
        val realTimeUi = RealTimeUi()
        realTimeUi.level = "Lv" + data.playerStatus.level
        realTimeUi.avatarUrl = when (data.avatar.type) {
            "ASSISTANT" -> {
                val skinUrl = URLEncoder.encode(data.avatar.id, "UTF-8")
                "$avatarUrl$skinUrl.png"
            }

            else -> data.avatar.url
        }

        realTimeUi.official = official
        realTimeUi.apNow = data.apInfo.current.toString()
        realTimeUi.apMax = "/" + data.apInfo.max
        getRemainTimeStr(data.apInfo.remainSecs).let {
            if (it == "") realTimeUi.apResTime = "已恢复"
            else realTimeUi.apResTime = it
        }
        realTimeUi.nickName = data.playerStatus.nickname
        realTimeUi.lastLogin = "上次登录 " +
                when (getDayNum(getCurrentTs()) - getDayNum(data.playerStatus.lastOnlineTs)) {
                    0L -> "今天"
                    1L -> "昨天"
                    else -> getTimeStr(data.playerStatus.lastOnlineTs * 1000, "yyyy-MM-dd")
                }

        //recruit
        realTimeUi.recruit.value = "${data.recruits.complete}/${data.recruits.max}"
        realTimeUi.recruit.time = if (data.recruits.remainSecs == -1L) {
            "已完成招募"
        } else {
            getRemainTimeStr(data.recruits.remainSecs)
        }

        //recruitRefresh
        if (data.hire.isNull) {
            realTimeUi.recruitRefresh.value = "暂无数据"
            realTimeUi.recruitRefresh.time = ""
        } else {
            realTimeUi.recruitRefresh.value = "${data.hire.count}/3"
            realTimeUi.recruitRefresh.time = if (data.hire.remainSecs == -1L) {
                "已完成刷新"
            } else {
                getRemainTimeStr(data.hire.remainSecs)
            }
        }

        //labor
        realTimeUi.labor.value = "${data.labor.current}/${data.labor.max}"
        realTimeUi.labor.time = if (data.labor.remainSecs == -1L) {
            ""
        } else {
            getRemainTimeStr(data.labor.remainSecs)
        }
        //meeting
        if (data.meeting.isNull) {
            realTimeUi.meeting.value = "暂无数据"
        } else {
            realTimeUi.meeting.value = "${data.meeting.current}/7"
            realTimeUi.meeting.time = if (data.meeting.remainSecs == -1L) {
                "收集完成"
            } else {
                getRemainTimeStr(data.meeting.remainSecs)
            }
        }

        //base
        realTimeUi.manufacture.value = "${data.manufactures.current}/${data.manufactures.max}"
        realTimeUi.trading.value = "${data.tradings.current}/${data.tradings.max}"
        realTimeUi.dormitories.value = "${data.dormitories.current}/${data.dormitories.max}"
        realTimeUi.tired.value = "${data.tired.current}"

        //train
        if (data.train.isNull) {
            realTimeUi.train.value = "暂无数据"
        } else {
            realTimeUi.train.value = if (data.train.traineeIsNull) {
                "空闲中"
            } else {
                data.train.trainee
            }
            realTimeUi.train.time = when (data.train.remainSecs) {
                -1L -> "空闲中"
                0L -> "专精完成"
                else -> getRemainTimeStr(data.train.remainSecs)
            }
        }
        realTimeUi.campaign.value = "${data.routine.campaignCurrent}/${data.routine.campaignTotal}"
        //show change
        if (data.train.changeTimeLogos != -1L) {
            realTimeUi.displayChange = true
            realTimeUi.logosChange.text =
                "逻各斯换班时间：${getTimeStr(data.train.changeTimeLogos * 1000)}"
            realTimeUi.logosChange.display = true
        }

        if (data.train.changeTimeIrene != -1L) {
            realTimeUi.displayChange = true
            realTimeUi.ireneChange.text =
                "艾丽妮换班时间：${getTimeStr(data.train.changeTimeIrene * 1000)}"
            realTimeUi.ireneChange.display = true
        }
        return realTimeUi
    }

    private suspend fun getRealTimeData(account: AccountSk): RealTimeData? {

        try {
            val response = getGameInfoConnectionTaskTest(account)
            if (!response.isSuccessful) throw Exception("!response.isSuccessful")
            response.body() ?: throw Exception("response empty")
            return geneRealTimeData(response.body()!!)
        } catch (e: Exception) {
            e.printStackTrace()
            Toaster.show(e.message)
            return null
        }

    }
}