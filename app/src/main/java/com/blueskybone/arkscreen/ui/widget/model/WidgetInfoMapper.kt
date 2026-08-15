package com.blueskybone.arkscreen.ui.widget.model

import android.content.Context
import com.blueskybone.arkscreen.R
import com.blueskybone.arkscreen.data.local.pref.CachePrefManager
import com.blueskybone.arkscreen.data.local.pref.SettingPrefManager
import com.blueskybone.arkscreen.domain.model.attendance.AccountAttendanceState
import com.blueskybone.arkscreen.domain.service.AppClock
import com.blueskybone.arkscreen.platform.time.TimeUtils

/** 将当前缓存快照映射为可直接展示的桌面组件信息。 */
class WidgetInfoMapper(
    private val context: Context,
    private val cache: CachePrefManager,
    private val settings: SettingPrefManager,
    private val appClock: AppClock,
) {
    fun mapAll(
        now: Long = TimeUtils.getCurrentTs(appClock),
        attendanceState: AccountAttendanceState? = null,
    ): List<WidgetInfoItem> = listOf(
        mapSanity(now),
        mapDrone(now),
        mapRecruitment(now),
        mapRecruitmentRefresh(now),
        mapTraining(now),
        mapMeeting(now),
        mapAttendance(now, attendanceState),
    )

    private fun mapSanity(now: Long): WidgetInfoItem {
        val source = cache.apCache.get()
        if (source.isnull) return unavailable(WidgetInfoType.SANITY, R.string.ap)
        val current = when {
            source.current >= source.max -> source.current.toLong()
            now >= source.recoverTime -> source.max.toLong()
            else -> source.max.toLong() -
                ((source.recoverTime - now + AP_SECONDS - 1) / AP_SECONDS)
        }.coerceAtLeast(source.current.toLong())
        val full = current >= source.max
        return item(
            type = WidgetInfoType.SANITY,
            titleRes = R.string.ap,
            value = "$current / ${source.max}",
            restTime = if (full) completed() else "${remaining(source.recoverTime - now)}后回满",
            state = if (full) WidgetInfoState.ATTENTION else WidgetInfoState.NORMAL,
        )
    }

    private fun mapDrone(now: Long): WidgetInfoItem {
        val source = cache.laborCache.get()
        if (source.isnull) return unavailable(WidgetInfoType.DRONE, R.string.labor)
        val remainingSeconds = (source.remainSec - now + source.lastSyncTs).coerceAtLeast(0L)
        val current = if (source.remainSec <= 0 || remainingSeconds == 0L) {
            source.max
        } else {
            val elapsed = (now - source.lastSyncTs).coerceAtLeast(0L)
            val gained = elapsed * (source.max - source.current) / source.remainSec
            (source.current + gained).toInt().coerceIn(source.current, source.max)
        }
        val full = current >= source.max
        return item(
            type = WidgetInfoType.DRONE,
            titleRes = R.string.labor,
            value = "$current / ${source.max}",
            restTime = if (full) completed() else "${remaining(remainingSeconds)}后回满",
            state = if (full) WidgetInfoState.ATTENTION else WidgetInfoState.NORMAL,
        )
    }

    private fun mapRecruitment(now: Long): WidgetInfoItem {
        val source = cache.recruitCache.get()
        if (source.isNull) return unavailable(WidgetInfoType.RECRUITMENT, R.string.recruit)
        val completedCount = when {
            source.completeTime == -1L -> source.complete
            now >= source.completeTime -> source.complete + 1
            else -> source.complete
        }.coerceAtMost(source.max)
        val hasPendingCompletion = source.completeTime > now
        return item(
            type = WidgetInfoType.RECRUITMENT,
            titleRes = R.string.recruit,
            value = "$completedCount / ${source.max}",
            restTime = when {
                completedCount >= source.max -> completed()
                hasPendingCompletion -> "${remaining(source.completeTime - now)}后完成"
                else -> null
            },
            state = if (completedCount > 0) WidgetInfoState.COMPLETED else WidgetInfoState.NORMAL,
        )
    }

    private fun mapRecruitmentRefresh(now: Long): WidgetInfoItem {
        val source = cache.refreshCache.get()
        if (source.isNull) {
            return unavailable(WidgetInfoType.RECRUITMENT_REFRESH, R.string.recruit_refresh)
        }
        val count = when {
            source.completeTime == -1L -> source.count
            now >= source.completeTime -> source.count + 1
            else -> source.count
        }.coerceAtMost(source.max)
        return item(
            type = WidgetInfoType.RECRUITMENT_REFRESH,
            titleRes = R.string.recruit_refresh,
            value = "$count / ${source.max}",
            restTime = when {
                count >= source.max -> completed()
                source.completeTime > now -> "${remaining(source.completeTime - now)}后恢复"
                else -> null
            },
            state = if (count >= source.max) WidgetInfoState.COMPLETED else WidgetInfoState.NORMAL,
        )
    }

    private fun mapTraining(now: Long): WidgetInfoItem {
        val source = cache.trainCache.get()
        if (source.isnull) return unavailable(WidgetInfoType.TRAINING, R.string.train)
        return when {
            source.status == -1L -> item(
                WidgetInfoType.TRAINING,
                R.string.train,
                value = context.getString(R.string.widget_idle),
                restTime = null,
                state = WidgetInfoState.IDLE,
            )
            source.status == 0L || now >= source.completeTime -> item(
                WidgetInfoType.TRAINING,
                R.string.train,
                value = source.trainee,
                restTime = completed(),
                state = WidgetInfoState.COMPLETED,
            )
            source.status == 1L -> item(
                WidgetInfoType.TRAINING,
                R.string.train,
                value = source.trainee,
                restTime = "${remaining(source.completeTime - now)}后完成",
                state = WidgetInfoState.NORMAL,
            )
            else -> error(WidgetInfoType.TRAINING, R.string.train)
        }
    }

    private fun mapMeeting(now: Long): WidgetInfoItem {
        val source = cache.meetCache.get()
        if (source.isnull) return unavailable(WidgetInfoType.MEETING, R.string.meeting)
        return when {
            source.stats == 0 -> item(
                WidgetInfoType.MEETING,
                R.string.meeting,
                value = context.getString(R.string.widget_meeting_idle),
                restTime = null,
                state = WidgetInfoState.IDLE,
            )
            source.stats == 2 || now >= source.completeTime -> item(
                WidgetInfoType.MEETING,
                R.string.meeting,
                value = context.getString(R.string.widget_meeting_ended),
                restTime = null,
                state = WidgetInfoState.COMPLETED,
            )
            source.stats == 1 -> item(
                WidgetInfoType.MEETING,
                R.string.meeting,
                value = context.getString(R.string.widget_meeting_sharing),
                restTime = context.getString(
                    R.string.widget_meeting_ends_after,
                    remaining(source.completeTime - now),
                ),
                state = WidgetInfoState.NORMAL,
            )
            else -> error(WidgetInfoType.MEETING, R.string.meeting)
        }
    }

    private fun mapAttendance(
        now: Long,
        accountState: AccountAttendanceState?,
    ): WidgetInfoItem {
        val enabled = settings.backAutoAtd.get()
        val attendedToday = accountState?.lastSuccessTs?.let(TimeUtils::getDayNum) ==
            TimeUtils.getDayNum(now)
        val failedToday = accountState?.lastAttemptTs?.let(TimeUtils::getDayNum) ==
            TimeUtils.getDayNum(now) && accountState.lastError != null
        return when {
            !enabled -> item(
                WidgetInfoType.ATTENDANCE,
                R.string.auto_attendance,
                value = context.getString(R.string.widget_attendance_disabled),
                restTime = null,
                state = WidgetInfoState.DISABLED,
            )
            attendedToday -> item(
                WidgetInfoType.ATTENDANCE,
                R.string.auto_attendance,
                value = context.getString(R.string.widget_attended),
                restTime = null,
                state = WidgetInfoState.COMPLETED,
            )
            failedToday -> item(
                WidgetInfoType.ATTENDANCE,
                R.string.auto_attendance,
                value = context.getString(R.string.widget_attendance_failed),
                restTime = null,
                state = WidgetInfoState.ERROR,
            )
            else -> item(
                WidgetInfoType.ATTENDANCE,
                R.string.auto_attendance,
                value = context.getString(R.string.widget_attendance_pending),
                restTime = null,
                state = WidgetInfoState.NORMAL,
            )
        }
    }

    private fun unavailable(type: WidgetInfoType, titleRes: Int) = item(
        type = type,
        titleRes = titleRes,
        value = context.getString(R.string.widget_no_data),
        restTime = null,
        state = WidgetInfoState.UNAVAILABLE,
    )

    private fun error(type: WidgetInfoType, titleRes: Int) = item(
        type = type,
        titleRes = titleRes,
        value = context.getString(R.string.widget_status_error),
        restTime = null,
        state = WidgetInfoState.ERROR,
    )

    private fun item(
        type: WidgetInfoType,
        titleRes: Int,
        value: String?,
        restTime: String?,
        state: WidgetInfoState,
    ) = WidgetInfoItem(
        type = type,
        title = context.getString(titleRes),
        value = value,
        restTime = restTime,
        state = state,
    )

    private fun completed() = context.getString(R.string.widget_completed)

    private fun remaining(seconds: Long): String =
        TimeUtils.getRemainTimeMinStr(seconds.coerceAtLeast(0L))

    private companion object {
        const val AP_SECONDS = 6 * 60L
    }
}
