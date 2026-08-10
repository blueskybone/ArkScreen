package com.blueskybone.arkscreen.ui.realtime.model

import com.blueskybone.arkscreen.R
import com.blueskybone.arkscreen.data.network.avatarUrl
import com.blueskybone.arkscreen.domain.model.realtime.RealTimeData
import com.blueskybone.arkscreen.util.TimeUtils.getCurrentTs
import com.blueskybone.arkscreen.util.TimeUtils.getDayNum
import com.blueskybone.arkscreen.util.TimeUtils.getRemainTimeStr
import com.blueskybone.arkscreen.util.TimeUtils.getTimeStr
import java.net.URLEncoder

object RealTimeMapper {

    fun toUi(data: RealTimeData, official: Boolean): RealTimeUi {
        val avatar = when (data.avatar.type) {
            "ASSISTANT" -> {
                val skinUrl = URLEncoder.encode(data.avatar.id, "UTF-8")
                "$avatarUrl$skinUrl.png"
            }
            else -> data.avatar.url
        }

        val lastLoginValue = when (
            getDayNum(getCurrentTs()) - getDayNum(data.playerStatus.lastOnlineTs)
        ) {
            0L -> UiText.Resource(
                R.string.realtime_last_login_today,
                listOf(getTimeStr(data.playerStatus.lastOnlineTs * 1000, "HH:mm")),
            )
            1L -> UiText.Resource(R.string.realtime_last_login_yesterday)
            else -> UiText.Resource(
                R.string.realtime_last_login_date,
                listOf(getTimeStr(data.playerStatus.lastOnlineTs * 1000, "yyyy-MM-dd")),
            )
        }

        val recruit = RealTimeUi.PairInfo(
            value = raw("${data.recruits.complete}/${data.recruits.max}"),
            time = if (data.recruits.remainSecs == -1L) {
                res(R.string.realtime_recruit_complete)
            } else {
                remain(data.recruits.remainSecs)
            },
            notify = data.recruits.complete > 0,
        )

        val recruitRefresh = if (data.hire.isNull) {
            RealTimeUi.PairInfo(value = res(R.string.realtime_no_data))
        } else {
            RealTimeUi.PairInfo(
                value = raw("${data.hire.count}/3"),
                time = if (data.hire.remainSecs == -1L) {
                    res(R.string.realtime_refresh_complete)
                } else {
                    remain(data.hire.remainSecs)
                },
                notify = data.hire.count > 0,
            )
        }

        val labor = RealTimeUi.PairInfo(
            value = raw("${data.labor.current}/${data.labor.max}"),
            time = if (data.labor.remainSecs == -1L) raw("") else remain(data.labor.remainSecs),
            notify = data.labor.current == data.labor.max,
        )

        val meeting = if (data.meeting.isNull) {
            RealTimeUi.PairInfo(value = res(R.string.realtime_no_data))
        } else {
            val time = when {
                data.meeting.status == 0 -> res(R.string.realtime_idle)
                data.meeting.remainSecs == -1L -> res(R.string.realtime_meeting_complete)
                else -> remain(data.meeting.remainSecs)
            }
            RealTimeUi.PairInfo(
                value = raw("${data.meeting.current}/7"),
                time = time,
                notify = data.meeting.status != 0 && data.meeting.remainSecs == -1L,
            )
        }

        val train = if (data.train.isNull) {
            RealTimeUi.PairInfo(value = res(R.string.realtime_no_data))
        } else {
            RealTimeUi.PairInfo(
                value = if (data.train.traineeIsNull) {
                    res(R.string.realtime_idle)
                } else {
                    raw(data.train.trainee)
                },
                time = when (data.train.remainSecs) {
                    -1L -> res(R.string.realtime_idle)
                    0L -> res(R.string.realtime_training_complete)
                    else -> remain(data.train.remainSecs)
                },
                notify = data.train.remainSecs == 0L,
            )
        }

        return RealTimeUi(
            nickName = data.playerStatus.nickname,
            lastLogin = lastLoginValue,
            level = data.playerStatus.level,
            avatarUrl = avatar,
            apMax = data.apInfo.max.coerceAtLeast(0),
            apNow = data.apInfo.current.coerceAtLeast(0),
            apFullTime = if (data.apInfo.recoverTime <= 0L) {
                res(R.string.realtime_ap_full_recovered)
            } else {
                UiText.Resource(
                    R.string.realtime_ap_full_at,
                    listOf(getTimeStr(data.apInfo.recoverTime * 1000, "MM-dd HH:mm")),
                )
            },
            apResTime = if (data.apInfo.remainSecs <= 0L) {
                res(R.string.realtime_ap_remaining_recovered)
            } else {
                UiText.Resource(
                    R.string.realtime_ap_remaining,
                    listOf(getRemainTimeStr(data.apInfo.remainSecs)),
                )
            },
            recruit = recruit,
            recruitRefresh = recruitRefresh,
            labor = labor,
            meeting = meeting,
            manufacture = RealTimeUi.PairInfo(
                value = raw("${data.manufactures.current}/${data.manufactures.max}"),
                notify = data.manufactures.current == data.manufactures.max,
            ),
            trading = RealTimeUi.PairInfo(
                value = raw("${data.tradings.current}/${data.tradings.max}"),
                notify = data.tradings.current == data.tradings.max,
            ),
            dormitories = RealTimeUi.PairInfo(
                value = raw("${data.dormitories.current}/${data.dormitories.max}"),
            ),
            tired = RealTimeUi.PairInfo(
                value = raw(data.tired.current.toString()),
                notify = data.tired.current > 0,
            ),
            train = train,
            campaign = RealTimeUi.PairInfo(
                value = raw("${data.routine.campaignCurrent}/${data.routine.campaignTotal}"),
            ),
            logosChange = data.train.changeTimeLogos.takeIf { it != -1L }?.let {
                RealTimeUi.TrainChange(
                    text = UiText.Resource(
                        R.string.realtime_logos_shift,
                        listOf(getTimeStr(it * 1000)),
                    ),
                    timeStamp = it,
                )
            },
            ireneChange = data.train.changeTimeIrene.takeIf { it != -1L }?.let {
                RealTimeUi.TrainChange(
                    text = UiText.Resource(
                        R.string.realtime_irene_shift,
                        listOf(getTimeStr(it * 1000)),
                    ),
                    timeStamp = it,
                )
            },
            official = official,
        )
    }

    private fun raw(value: String) = UiText.Raw(value)
    private fun res(id: Int) = UiText.Resource(id)
    private fun remain(seconds: Long) = raw(getRemainTimeStr(seconds.coerceAtLeast(0L)))
}
