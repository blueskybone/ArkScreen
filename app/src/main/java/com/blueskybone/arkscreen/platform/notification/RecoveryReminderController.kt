package com.blueskybone.arkscreen.platform.notification

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.blueskybone.arkscreen.R
import com.blueskybone.arkscreen.data.local.pref.InnerPrefManager
import com.blueskybone.arkscreen.data.local.pref.SettingPrefManager
import com.blueskybone.arkscreen.domain.model.account.AccountSk
import com.blueskybone.arkscreen.domain.model.realtime.RealTimeData
import com.blueskybone.arkscreen.domain.repository.SklandRepository
import com.blueskybone.arkscreen.domain.service.RecoveryReminderScheduler
import com.blueskybone.arkscreen.ui.main.MainActivity
import kotlinx.coroutines.CancellationException
import org.json.JSONArray
import org.json.JSONObject
import timber.log.Timber

/** One alarm per event key. A new network snapshot replaces every outstanding alarm. */
class RecoveryReminderController(
    context: Context,
    private val settings: SettingPrefManager,
    private val innerPrefs: InnerPrefManager,
    private val skland: SklandRepository,
) : RecoveryReminderScheduler {
    private val appContext = context.applicationContext
    private val alarms = appContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    private val notifications = appContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    private val storage = appContext.getSharedPreferences("recovery_reminders", Context.MODE_PRIVATE)

    override suspend fun onRealtimeSynced(account: AccountSk, data: RealTimeData) {
        if (!settings.recoveryNotificationsEnabled.get()) {
            cancelAll()
            return
        }
        if (innerPrefs.currentAccountSkUid.get() != account.uid) return
        val now = System.currentTimeMillis() / 1000
        val name = data.playerStatus.nickname.ifBlank { account.nickName }
        val events = mutableListOf<Event>()
        fun add(key: String, at: Long, message: String, expiresAt: Long = Long.MAX_VALUE) {
            if (at > now) events += Event(key, account.uid, data.currentTs, at, name, message,
                expiresAt)
        }

        val ap = data.apInfo
        if (ap.max > 0 && ap.current in 0 until ap.max && ap.recoverTime > now) {
            if (settings.notifyApFull.get()) {
                add("ap_full", ap.recoverTime, "$name 理智已满")
            }
            if (settings.notifyApEarly.get()) {
                val lead = settings.notifyApEarlyMinutes.get().coerceIn(1, 180)
                add("ap_early", ap.recoverTime - lead * 60L,
                    "$name 理智即将溢出：${lead}min", ap.recoverTime)
            }
        }

        val train = data.train
        if (!train.isNull && !train.traineeIsNull && train.status == 1L) {
            if (settings.notifyTrainingComplete.get()) {
                add("train_done", train.completeTime,
                    "$name ${train.trainee.ifBlank { "干员" }} 专精已完成")
            }
            if (settings.notifyTrainingShift.get() &&
                train.trainee != "艾丽妮" && train.trainee != "逻各斯") {
                val lead = settings.notifyTrainingShiftMinutes.get().coerceIn(0, 180)
                val candidates = listOf(
                    Triple("艾丽妮", "train_irene", train.changeTimeIrene),
                    Triple("逻各斯", "train_logos", train.changeTimeLogos),
                ).filter { (operator, _, at) ->
                    at > now && when (operator) {
                        "艾丽妮" -> train.profession == "WARRIOR" || train.profession == "SNIPER"
                        else -> train.profession == "CASTER" || train.profession == "SUPPORT"
                    }
                }
                if (candidates.isNotEmpty()) {
                    // A failed asset lookup must not create an unverified shift reminder.
                    val owned = runCatching { skland.fetchCharAssets(account).getOrNull() }
                        .onFailure { if (it is CancellationException) throw it }
                        .getOrNull()?.operators.orEmpty()
                        .filter { it.evolvePhase >= 2 }
                        .map { it.name }
                        .toSet()
                    candidates.forEach { (operator, key, at) ->
                        if (operator in owned && train.trainer != operator) {
                            add(key, at - lead * 60L,
                                "$name ${train.trainee.ifBlank { "干员" }} 可由${operator}换班",
                                train.completeTime)
                        }
                    }
                }
            }
        }

        val labor = data.labor
        if (settings.notifyLaborFull.get() && !labor.isNull &&
            labor.max > 0 && labor.current in 0 until labor.max) {
            add("labor", labor.recoverTime, "$name 无人机已满")
        }
        val meeting = data.meeting
        if (settings.notifyMeetingComplete.get() && !meeting.isNull && meeting.status == 1) {
            add("meeting", meeting.completeTime, "$name 会客室交流结束")
        }
        val recruit = data.recruits
        if (settings.notifyRecruitComplete.get() && !recruit.isNull) {
            recruit.pendingCompleteTimes.groupBy { it / 60 }.values
                .map { it.min() }.take(4).forEachIndexed { index, at ->
                add("recruit_$index", at, "$name 公开招募已完成")
            }
        }
        val hire = data.hire
        if (settings.notifyRecruitRefresh.get() && !hire.isNull && hire.count < hire.max) {
            add("refresh", hire.completeTime, "$name 公开招募可刷新")
        }
        replace(account.uid, data.currentTs, events)
    }

    @Synchronized
    private fun replace(uid: String, sampledAt: Long, events: List<Event>) {
        if (!settings.recoveryNotificationsEnabled.get() ||
            innerPrefs.currentAccountSkUid.get() != uid) return
        val previous = readEvents()
        if (storage.getString(KEY_LAST_UID, null) == uid &&
            storage.getLong(KEY_LAST_SYNC, -1L) > sampledAt) return
        previous.forEach { cancelAlarm(it.key) }
        storage.edit().putString(KEY_EVENTS, encode(events))
            .putString(KEY_LAST_UID, uid).putLong(KEY_LAST_SYNC, sampledAt).apply()
        events.forEach(::scheduleAlarm)
        Timber.tag("RecoveryReminder").i("Scheduled %d reminders for current account", events.size)
    }

    @Synchronized
    fun onAccountChanged(uid: String?) {
        if (!settings.recoveryNotificationsEnabled.get() ||
            readEvents().any { it.uid != uid }) cancelAll()
    }

    @Synchronized
    fun cancelAll() {
        readEvents().forEach { cancelAlarm(it.key) }
        storage.edit().remove(KEY_EVENTS).remove(KEY_LAST_UID).remove(KEY_LAST_SYNC).apply()
    }

    @Synchronized
    fun restoreAfterBoot() {
        val currentUid = innerPrefs.currentAccountSkUid.get()
        if (!settings.recoveryNotificationsEnabled.get() || currentUid.isBlank()) {
            cancelAll()
            return
        }
        readEvents().filter { it.uid == currentUid &&
            it.at > System.currentTimeMillis() / 1000 &&
            it.expiresAt > System.currentTimeMillis() / 1000 }
            .also { valid ->
                storage.edit().putString(KEY_EVENTS, encode(valid)).apply()
                valid.forEach(::scheduleAlarm)
            }
    }

    @Synchronized
    fun onAlarm(key: String, uid: String, at: Long) {
        val events = readEvents()
        val event = events.firstOrNull { it.key == key && it.uid == uid && it.at == at } ?: return
        if (System.currentTimeMillis() / 1000 < event.at) return
        storage.edit().putString(KEY_EVENTS, encode(events - event)).apply()
        if (!settings.recoveryNotificationsEnabled.get() ||
            innerPrefs.currentAccountSkUid.get() != uid) return
        if (System.currentTimeMillis() / 1000 >= event.expiresAt) return
        if (Build.VERSION.SDK_INT >= 33 && ContextCompat.checkSelfPermission(
                appContext, Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED) return
        if (!NotificationManagerCompat.from(appContext).areNotificationsEnabled()) return
        notifications.createNotificationChannel(NotificationChannel(
            CHANNEL_ID, appContext.getString(R.string.recovery_notification_channel),
            NotificationManager.IMPORTANCE_DEFAULT,
        ))
        val openApp = PendingIntent.getActivity(
            appContext, 0, Intent(appContext, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val notification = NotificationCompat.Builder(appContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(appContext.getString(R.string.recovery_notification_title))
            .setContentText(event.message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(event.message))
            .setContentIntent(openApp)
            .setAutoCancel(true)
            .build()
        notifications.notify(key.hashCode(), notification)
    }

    private fun scheduleAlarm(event: Event) {
        alarms.setAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP, event.at * 1000,
            pendingIntent(event.key, event.uid, event.at),
        )
    }

    private fun cancelAlarm(key: String) {
        val intent = Intent(appContext, RecoveryReminderReceiver::class.java).apply {
            action = ACTION_FIRE
            data = android.net.Uri.parse("arkscreen://recovery/$key")
        }
        PendingIntent.getBroadcast(appContext, key.hashCode(), intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE)?.let {
            alarms.cancel(it)
            it.cancel()
        }
    }

    private fun pendingIntent(key: String, uid: String, at: Long): PendingIntent =
        PendingIntent.getBroadcast(
            appContext, key.hashCode(),
            Intent(appContext, RecoveryReminderReceiver::class.java).apply {
                action = ACTION_FIRE
                data = android.net.Uri.parse("arkscreen://recovery/$key")
                putExtra(EXTRA_KEY, key)
                putExtra(EXTRA_UID, uid)
                putExtra(EXTRA_AT, at)
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

    private fun readEvents(): List<Event> = runCatching {
        val array = JSONArray(storage.getString(KEY_EVENTS, "[]"))
        (0 until array.length()).map { index ->
            val item = array.getJSONObject(index)
            Event(item.getString("key"), item.getString("uid"), item.getLong("sampledAt"),
                item.getLong("at"), item.getString("name"), item.getString("message"),
                item.optLong("expiresAt", Long.MAX_VALUE))
        }
    }.getOrDefault(emptyList())

    private fun encode(events: List<Event>): String = JSONArray().apply {
        events.forEach { event ->
            put(JSONObject().apply {
                put("key", event.key)
                put("uid", event.uid)
                put("sampledAt", event.sampledAt)
                put("at", event.at)
                put("name", event.name)
                put("message", event.message)
                put("expiresAt", event.expiresAt)
            })
        }
    }.toString()

    private data class Event(
        val key: String,
        val uid: String,
        val sampledAt: Long,
        val at: Long,
        val name: String,
        val message: String,
        val expiresAt: Long,
    )

    companion object {
        const val ACTION_FIRE = "com.blueskybone.arkscreen.RECOVERY_REMINDER"
        const val EXTRA_KEY = "key"
        const val EXTRA_UID = "uid"
        const val EXTRA_AT = "at"
        private const val KEY_EVENTS = "events"
        private const val KEY_LAST_UID = "last_uid"
        private const val KEY_LAST_SYNC = "last_sync"
        private const val CHANNEL_ID = "recovery_reminders"
    }
}
