package com.blueskybone.arkscreen.ui.main

import android.Manifest
import android.os.Build
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.blueskybone.arkscreen.R
import com.blueskybone.arkscreen.data.local.pref.SettingPrefManager
import com.blueskybone.arkscreen.domain.repository.AccountRepository
import com.blueskybone.arkscreen.domain.usecase.realtime.GetRealTimeUseCase
import com.blueskybone.arkscreen.platform.notification.RecoveryReminderController
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.switchmaterial.SwitchMaterial
import com.hjq.toast.Toaster
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class RecoveryNotificationSettingsActivity : AppCompatActivity() {
    private val settings: SettingPrefManager by inject()
    private val reminders: RecoveryReminderController by inject()
    private val accounts: AccountRepository by inject()
    private val getRealTime: GetRealTimeUseCase by inject()
    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (!granted) Toaster.show(getString(R.string.recovery_notifications_permission))
    }

    private lateinit var enabled: SwitchMaterial
    private lateinit var apFull: SwitchMaterial
    private lateinit var apEarly: SwitchMaterial
    private lateinit var trainDone: SwitchMaterial
    private lateinit var trainShift: SwitchMaterial
    private lateinit var labor: SwitchMaterial
    private lateinit var meeting: SwitchMaterial
    private lateinit var recruit: SwitchMaterial
    private lateinit var refresh: SwitchMaterial
    private lateinit var apLeadButton: MaterialButton
    private lateinit var shiftLeadButton: MaterialButton
    private var apLead = 30
    private var shiftLead = 30

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val root = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        val toolbar = MaterialToolbar(this).apply {
            title = getString(R.string.recovery_notifications)
            setNavigationIcon(androidx.appcompat.R.drawable.abc_ic_ab_back_material)
            setNavigationOnClickListener { finish() }
        }
        root.addView(toolbar)
        val scroll = ScrollView(this).apply { isFillViewport = true }
        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            val horizontal = dp(20)
            setPadding(horizontal, dp(12), horizontal, dp(24))
        }
        scroll.addView(content)
        root.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))
        setContentView(root)

        content.addView(TextView(this).apply {
            setText(R.string.recovery_notifications_experimental)
            setTextColor(ContextCompat.getColor(this@RecoveryNotificationSettingsActivity,
                R.color.inline_chip_text))
            background = ContextCompat.getDrawable(this@RecoveryNotificationSettingsActivity,
                R.drawable.bg_inline_chip)
            backgroundTintList = android.content.res.ColorStateList.valueOf(
                ContextCompat.getColor(this@RecoveryNotificationSettingsActivity,
                    R.color.inline_chip_background))
            textSize = 13f
            setPadding(dp(8), dp(6), dp(8), dp(6))
        })
        enabled = addSwitch(content, R.string.recovery_notifications_enable,
            settings.recoveryNotificationsEnabled.get())
        addSection(content, R.string.recovery_notifications_ap_full)
        apFull = addSwitch(content, R.string.recovery_notifications_ap_full,
            settings.notifyApFull.get())
        apEarly = addSwitch(content, R.string.recovery_notifications_ap_early,
            settings.notifyApEarly.get())
        apLead = settings.notifyApEarlyMinutes.get()
        apLeadButton = addLeadButton(content, apLead) {
            chooseLead(intArrayOf(5, 10, 15, 30, 60, 120), apLead) {
                apLead = it
                apLeadButton.text = getString(R.string.recovery_notifications_advance, it)
            }
        }
        addSection(content, R.string.recovery_notifications_training_done)
        trainDone = addSwitch(content, R.string.recovery_notifications_training_done,
            settings.notifyTrainingComplete.get())
        trainShift = addSwitch(content, R.string.recovery_notifications_training_shift,
            settings.notifyTrainingShift.get())
        shiftLead = settings.notifyTrainingShiftMinutes.get()
        shiftLeadButton = addLeadButton(content, shiftLead) {
            chooseLead(intArrayOf(0, 5, 10, 15, 30, 60), shiftLead) {
                shiftLead = it
                shiftLeadButton.text = getString(R.string.recovery_notifications_advance, it)
            }
        }
        addSection(content, R.string.recovery_notifications_labor)
        labor = addSwitch(content, R.string.recovery_notifications_labor,
            settings.notifyLaborFull.get())
        meeting = addSwitch(content, R.string.recovery_notifications_meeting,
            settings.notifyMeetingComplete.get())
        recruit = addSwitch(content, R.string.recovery_notifications_recruit,
            settings.notifyRecruitComplete.get())
        refresh = addSwitch(content, R.string.recovery_notifications_refresh,
            settings.notifyRecruitRefresh.get())

        val save = MaterialButton(this).apply {
            setText(R.string.save)
            setOnClickListener { saveSettings() }
        }
        content.addView(save, LinearLayout.LayoutParams(-1, dp(48)).apply { topMargin = dp(20) })
    }

    private fun saveSettings() {
        settings.recoveryNotificationsEnabled.set(enabled.isChecked)
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
        if (enabled.isChecked) {
            if (Build.VERSION.SDK_INT >= 33 && ContextCompat.checkSelfPermission(
                    this, Manifest.permission.POST_NOTIFICATIONS
                ) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
            lifecycleScope.launch {
                accounts.observeCurrentSkAcc().first()?.let { account ->
                    getRealTime(account).onFailure {
                        Toaster.show(getString(R.string.recovery_notifications_sync_failed))
                    }
                }
            }
        }
    }

    private fun addSection(parent: LinearLayout, title: Int) {
        parent.addView(TextView(this).apply {
            setText(title)
            textSize = 14f
            setPadding(0, dp(16), 0, dp(4))
            setTextColor(ContextCompat.getColor(this@RecoveryNotificationSettingsActivity,
                R.color.inline_chip_text))
        })
    }

    private fun addSwitch(parent: LinearLayout, title: Int, checked: Boolean): SwitchMaterial =
        SwitchMaterial(this).apply {
            setText(title)
            isChecked = checked
            minimumHeight = dp(48)
            parent.addView(this, LinearLayout.LayoutParams(-1, -2))
        }

    private fun addLeadButton(parent: LinearLayout, minutes: Int, onClick: () -> Unit): MaterialButton =
        MaterialButton(this, null, com.google.android.material.R.attr.materialButtonOutlinedStyle).apply {
            text = getString(R.string.recovery_notifications_advance, minutes)
            setOnClickListener { onClick() }
            parent.addView(this, LinearLayout.LayoutParams(-1, -2))
        }

    private fun chooseLead(options: IntArray, selected: Int, onChosen: (Int) -> Unit) {
        MaterialAlertDialogBuilder(this)
            .setSingleChoiceItems(options.map {
                getString(R.string.recovery_notifications_advance, it)
            }.toTypedArray(), options.indexOf(selected)) { dialog, index ->
                onChosen(options[index])
                dialog.dismiss()
            }
            .show()
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()
}
