package com.blueskybone.arkscreen.ui.realtime.model

import com.blueskybone.arkscreen.data.network.avatarUrl
import com.blueskybone.arkscreen.domain.model.realtime.RealTimeData
import com.blueskybone.arkscreen.util.TimeUtils.getCurrentTs
import com.blueskybone.arkscreen.util.TimeUtils.getDayNum
import com.blueskybone.arkscreen.util.TimeUtils.getRemainTimeStr
import com.blueskybone.arkscreen.util.TimeUtils.getTimeStr
import java.net.URLEncoder

/**
 * Created by blueskybone
 * Date: 2026/3/19
 */
object RealTimeMapper {
    fun toUi(data: RealTimeData): RealTimeUi {
        val realTimeUi = RealTimeUi()
        realTimeUi.level = "Lv" + data.playerStatus.level
        realTimeUi.avatarUrl = when (data.avatar.type) {
            "ASSISTANT" -> {
                val skinUrl = URLEncoder.encode(data.avatar.id, "UTF-8")
                "$avatarUrl$skinUrl.png"
            }

            else -> data.avatar.url
        }

        realTimeUi.apNow = data.apInfo.current.toString()
        realTimeUi.apMax = "/" + data.apInfo.max
        getRemainTimeStr(data.apInfo.remainSecs).let {
            if (it == "") realTimeUi.apResTime = "已恢复"
            else realTimeUi.apResTime = it
        }
        realTimeUi.nickName = data.playerStatus.nickname
        realTimeUi.lastLogin = "上次登录 " +
                when (getDayNum(getCurrentTs()) - getDayNum(data.playerStatus.lastOnlineTs)) {
                    0L -> "今天 " + getTimeStr(data.playerStatus.lastOnlineTs * 1000, "HH:mm")
                    1L -> "昨天 "
                    else -> getTimeStr(data.playerStatus.lastOnlineTs * 1000, "yyyy-MM-dd")
                }

        //公开招募
        realTimeUi.recruit.value = "${data.recruits.complete}/${data.recruits.max}"
        realTimeUi.recruit.time = if (data.recruits.remainSecs == -1L) {
            "已完成招募"
        } else {
            getRemainTimeStr(data.recruits.remainSecs)
        }
        realTimeUi.recruit.notify = data.recruits.complete > 0

        //公招刷新
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
        realTimeUi.recruitRefresh.notify = data.hire.count > 0

        //无人机
        realTimeUi.labor.value = "${data.labor.current}/${data.labor.max}"
        realTimeUi.labor.time = if (data.labor.remainSecs == -1L) {
            ""
        } else {
            getRemainTimeStr(data.labor.remainSecs)
        }
        realTimeUi.labor.notify = (data.labor.current == data.labor.max)

        //会客室
        if (data.meeting.isNull) {
            realTimeUi.meeting.value = "暂无数据"
        } else {
            realTimeUi.meeting.value = "${data.meeting.current}/7"
            realTimeUi.meeting.time = if (data.meeting.status == 0) {
                "空闲中"
            } else if (data.meeting.remainSecs == -1L) {
                "交流完成"
            } else {
                getRemainTimeStr(data.meeting.remainSecs)
            }
        }
        realTimeUi.meeting.notify = realTimeUi.meeting.time == "交流完成"

        //基建
        realTimeUi.manufacture.value = "${data.manufactures.current}/${data.manufactures.max}"
        realTimeUi.trading.value = "${data.tradings.current}/${data.tradings.max}"
        realTimeUi.dormitories.value = "${data.dormitories.current}/${data.dormitories.max}"
        realTimeUi.tired.value = "${data.tired.current}"

        realTimeUi.manufacture.notify = (data.manufactures.current == data.manufactures.max)
        realTimeUi.trading.notify = (data.tradings.current == data.tradings.max)
        realTimeUi.tired.notify = data.tired.current > 0

        //训练室
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
        realTimeUi.train.notify = realTimeUi.train.time == "专精完成"

        //训练室换班
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
}