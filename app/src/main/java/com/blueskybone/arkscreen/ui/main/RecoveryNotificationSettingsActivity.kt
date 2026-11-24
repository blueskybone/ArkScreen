package com.blueskybone.arkscreen.ui.main

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.ListPopupWindow
import androidx.lifecycle.lifecycleScope
import com.blueskybone.arkscreen.R
import com.blueskybone.arkscreen.data.local.pref.SettingPrefManager
import com.blueskybone.arkscreen.domain.repository.AccountRepository
import com.blueskybone.arkscreen.domain.usecase.realtime.GetRealTimeUseCase
import com.blueskybone.arkscreen.platform.notification.RecoveryReminderController
import com.blueskybone.arkscreen.ui.common.showChoicePopup
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.materialswitch.MaterialSwitch
import com.hjq.toast.Toaster
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class RecoveryNotificationSettingsActivity : AppCompatActivity() {
    private val settings: SettingPrefManager by inject()
    private val reminders: RecoveryReminderController by inject()
    private val accounts: AccountRepository by inject()
    private val getRealTime: GetRealTimeUseCase by inject()
    private lateinit var apFull: MaterialSwitch
    private lateinit var apEarly: MaterialSwitch
    private lateinit var trainDone: MaterialSwitch
    private lateinit var trainShift: MaterialSwitch
    private lateinit var labor: MaterialSwitch
    private lateinit var meeting: MaterialSwitch
    private lateinit var recruit: MaterialSwitch
    private lateinit var refresh: MaterialSwitch
    private lateinit var apLeadRow: View
    private lateinit var shiftLeadRow: View
    private var choicePopup: ListPopupWindow? = null
    private var apLead = 30
    private var shiftLead = 30

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recovery_notification_settings)
        findViewById<com.google.android.material.appbar.MaterialToolbar>(R.id.Toolbar)
            .setNavigationOnClickListener { finish() }

        apFull = findViewById(R.id.ApFull)
        apEarly = findViewById(R.id.ApEarly)
        trainDone = findViewById(R.id.TrainDone)
        trainShift = findViewById(R.id.TrainShift)
        labor = findViewById(R.id.Labor)
        meeting = findViewById(R.id.Meeting)
        recruit = findViewById(R.id.Recruit)
        refresh = findViewById(R.id.Refresh)
        apLeadRow = findViewById(R.id.ApLead)
        shiftLeadRow = findViewById(R.id.ShiftLead)
        setupLeadRow(apLeadRow)
        setupLeadRow(shiftLeadRow)

        apFull.isChecked = settings.notifyApFull.get()
        apEarly.isChecked = settings.notifyApEarly.get()
        trainDone.isChecked = settings.notifyTrainingComplete.get()
        trainShift.isChecked = settings.notifyTrainingShift.get()
        labor.isChecked = settings.notifyLaborFull.get()
        meeting.isChecked = settings.notifyMeetingComplete.get()
        recruit.isChecked = settings.notifyRecruitComplete.get()
        refresh.isChecked = settings.notifyRecruitRefresh.get()

        apLead = settings.notifyApEarlyMinutes.get()
        renderLeadValue(apLeadRow, apLead)
        apLeadRow.setOnClickListener {
            chooseLead(apLeadRow, intArrayOf(5, 10, 15, 30, 60, 120), apLead) {
                apLead = it
                renderLeadValue(apLeadRow, it)
            }
        }
        shiftLead = settings.notifyTrainingShiftMinutes.get()
        renderLeadValue(shiftLeadRow, shiftLead)
        shiftLeadRow.setOnClickListener {
            chooseLead(shiftLeadRow, intArrayOf(0, 5, 10, 15, 30, 60), shiftLead) {
                shiftLead = it
                renderLeadValue(shiftLeadRow, it)
            }
        }

        fun updateControls() {
            setLeadEnabled(apLeadRow, apEarly.isChecked)
            setLeadEnabled(shiftLeadRow, trainShift.isChecked)
        }
        apEarly.setOnCheckedChangeListener { _, _ -> updateControls() }
        trainShift.setOnCheckedChangeListener { _, _ -> updateControls() }
        updateControls()

        findViewById<FloatingActionButton>(R.id.Save).setOnClickListener { saveSettings() }
    }

    private fun saveSettings() {
        settings.notifyApFull.set(apFull.isChecked)
        settings.notifyApEarly.set(apEarly.isChecked)
        settings.notifyApEarlyMinutes.set(apLead)
        settings.notifyTrainingComplete.set(trainDone.isChecked)
        settings.notifyTrainingShift.set(trainShift.isChecked)
        settings.notifyTrainingShiftMinutes.set(shiftLead)
        settings.notifyLaborFull.set(labor.isChecked)
        settings.notifyMeetingComplete.set(meeting.isChecked)
        settings.notifyRecruitComplete.set(recruit.isChecked)
        settings.notifyRecruitRefresh.set(refresh.isChecked)
        reminders.cancelAll()
        Toaster.show(getString(R.string.recovery_notifications_saved))
        if (settings.recoveryNotificationsEnabled.get()) {
            lifecycleScope.launch {
                accounts.observeCurrentSkAcc().first()?.let { account ->
                    getRealTime(account).onFailure {
                        Toaster.show(getString(R.string.recovery_notifications_sync_failed))
                    }
                }
            }
        }
    }

    private fun setupLeadRow(row: View) {
        row.findViewById<TextView>(R.id.Title).setText(R.string.recovery_notifications_lead_time)
        row.findViewById<ImageView>(R.id.Icon).setImageResource(R.drawable.ic_function_clock)
    }

    private fun renderLeadValue(row: View, minutes: Int) {
        row.findViewById<TextView>(R.id.Value).text =
            if (minutes == 0) getString(R.string.recovery_notifications_on_completion)
            else getString(R.string.recovery_notifications_minutes, minutes)
    }

    private fun setLeadEnabled(row: View, enabled: Boolean) {
        row.isEnabled = enabled
        row.alpha = if (enabled) 1f else 0.5f
    }

    private fun chooseLead(anchor: View, options: IntArray, selected: Int, onChosen: (Int) -> Unit) {
        choicePopup?.dismiss()
        choicePopup = showChoicePopup(
            anchor = anchor,
            entries = options.map { minutes ->
                if (minutes == 0) getString(R.string.recovery_notifications_on_completion)
                else getString(R.string.recovery_notifications_minutes, minutes)
            },
            selected = options.indexOf(selected),
        ) { index -> onChosen(options[index]) }
    }

    override fun onDestroy() {
        choicePopup?.dismiss()
        super.onDestroy()
    }
}
