package com.blueskybone.arkscreen.ui.widget

import com.blueskybone.arkscreen.data.local.pref.CachePrefManager
import com.blueskybone.arkscreen.domain.service.AppClock
import com.blueskybone.arkscreen.platform.time.TimeUtils
import com.blueskybone.arkscreen.platform.time.TimeUtils.getCurrentTs
import timber.log.Timber

data class WidgetContentText(
    val primary: String,
    val secondary: String,
)

/** 将缓存的游戏状态转换为紧凑型桌面组件共用的统一文本模型。 */
class WidgetContentFormatter(
    private val cache: CachePrefManager,
    private val appClock: AppClock,
) {
    fun format(content: String, compact: Boolean = false): WidgetContentText {
        val now = getCurrentTs(appClock)
        return when (content) {
            "ap" -> {
                val value = cache.apCache.get()
                if (value.isnull) return WidgetContentText("暂无数据", "")
                val current = when {
                    value.current >= value.max -> value.current.toLong()
                    now >= value.recoverTime -> value.max.toLong()
                    else -> value.max.toLong() -
                        ((value.recoverTime - now + AP_SECONDS - 1) / AP_SECONDS)
                }.coerceAtLeast(value.current.toLong())
                WidgetContentText(
                    primary = if (compact) "$current" else "$current / ${value.max}",
                    secondary = if (compact) "${value.max}" else remaining(value.recoverTime - now),
                )
            }

            "labor" -> {
                val value = cache.laborCache.get()
                if (value.isnull) return WidgetContentText("暂无数据", "")
                val remaining = (value.remainSec - now + value.lastSyncTs).coerceAtLeast(0)
                val current = if (value.remainSec <= 0 || remaining == 0L) {
                    value.max
                } else {
                    val elapsed = (now - value.lastSyncTs).coerceAtLeast(0)
                    val gained = elapsed * (value.max - value.current) / value.remainSec
                    (value.current + gained).toInt().coerceIn(value.current, value.max)
                }
                WidgetContentText(
                    primary = if (compact) "$current" else "$current / ${value.max}",
                    secondary = if (compact) "${value.max}" else remaining(remaining),
                )
            }

            "recruit" -> {
                val value = cache.recruitCache.get()
                if (value.isNull) return WidgetContentText("暂无数据", "")
                val completed = when {
                    value.completeTime == -1L -> value.complete
                    now >= value.completeTime -> value.complete + 1
                    else -> value.complete
                }.coerceAtMost(value.max)
                WidgetContentText(
                    primary = if (compact) "$completed" else "$completed / ${value.max}",
                    secondary = if (compact) {
                        if (value.completeTime > now) {
                            remaining(value.completeTime - now)
                        } else {
                            "${value.max}"
                        }
                    } else {
                        if (value.completeTime > now) remaining(value.completeTime - now) else ""
                    },
                )
            }

            "refresh" -> {
                val value = cache.refreshCache.get()
                if (value.isNull) return WidgetContentText("暂无数据", "")
                val count = when {
                    value.completeTime == -1L -> value.count
                    now >= value.completeTime -> value.count + 1
                    else -> value.count
                }.coerceAtMost(value.max)
                WidgetContentText(
                    primary = if (compact) "$count" else "$count / ${value.max}",
                    secondary = if (compact) {
                        if (value.completeTime > now) {
                            remaining(value.completeTime - now)
                        } else {
                            "${value.max}"
                        }
                    } else {
                        if (value.completeTime > now) remaining(value.completeTime - now) else ""
                    },
                )
            }

            "train" -> {
                val value = cache.trainCache.get()
                when {
                    value.isnull -> WidgetContentText("暂无数据", "")
                    value.status == -1L -> WidgetContentText("空闲中", "")
                    value.status == 0L || now >= value.completeTime ->
                        WidgetContentText(value.trainee, "已完成")
                    value.status == 1L ->
                        WidgetContentText(value.trainee, remaining(value.completeTime - now))
                    else -> invalidStatus("训练室", value.status)
                }
            }

            "meet" -> {
                val value = cache.meetCache.get()
                when {
                    value.isnull -> WidgetContentText("暂无数据", "")
                    value.stats == 0 -> WidgetContentText("空闲中", "")
                    value.stats == 2 || now >= value.completeTime ->
                        WidgetContentText("交流完成", "已完成")
                    value.stats == 1 ->
                        WidgetContentText("交流中", remaining(value.completeTime - now))
                    else -> invalidStatus("会客室", value.stats)
                }
            }

            else -> {
                Timber.e("未知 Widget 内容类型：%s", content)
                WidgetContentText("暂无数据", "")
            }
        }
    }

    private fun remaining(seconds: Long): String =
        if (seconds <= 0) "已完成" else TimeUtils.getRemainTimeMinStr(seconds)

    private fun invalidStatus(source: String, status: Any): WidgetContentText {
        Timber.e("%s状态错误：%s", source, status)
        return WidgetContentText("状态异常", "")
    }

    private companion object {
        const val AP_SECONDS = 6 * 60L
    }
}
