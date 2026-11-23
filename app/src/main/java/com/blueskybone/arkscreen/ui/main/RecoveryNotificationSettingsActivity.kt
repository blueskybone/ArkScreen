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
    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (!granted) Toaster.show(getString(R.string.recovery_notifications_permission))
    }

    private lateinit var enabled: MaterialSwitch
    private lateinit var apFull: MaterialSwitch
    private lateinit var apEarly: MaterialSwitch
    private lateinit var trainDone: MaterialSwitch
    private lateinit var trainShift: MaterialSwitch
    private lateinit var labor: MaterialSwitch
    private lateinit var meeting: MaterialSwitch
    private lateinit var recruit: MaterialSwitch
    private lateinit var refresh: MaterialSwitch
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
        val general = addCard(content, R.string.recovery_notifications)
        enabled = addSwitch(general, R.string.recovery_notifications_enable,
            settings.recoveryNotificationsEnabled.get())
        val sanity = addCard(content, R.string.realtime_sanity)
        apFull = addSwitch(sanity, R.string.recovery_notifications_ap_full,
            settings.notifyApFull.get())
        apEarly = addSwitch(sanity, R.string.recovery_notifications_ap_early,
            settings.notifyApEarly.get())
        apLead = settings.notifyApEarlyMinutes.get()
        apLeadButton = addLeadButton(sanity, apLead) {
            chooseLead(intArrayOf(5, 10, 15, 30, 60, 120), apLead) {
                apLead = it
                apLeadButton.text = getString(R.string.recovery_notifications_advance, it)
            }
        }
        val training = addCard(content, R.string.train)
        trainDone = addSwitch(training, R.string.recovery_notifications_training_done,
            settings.notifyTrainingComplete.get())
        trainShift = addSwitch(training, R.string.recovery_notifications_training_shift,
            settings.notifyTrainingShift.get())
        shiftLead = settings.notifyTrainingShiftMinutes.get()
        shiftLeadButton = addLeadButton(training, shiftLead) {
            chooseLead(intArrayOf(0, 5, 10, 15, 30, 60), shiftLead) {
                shiftLead = it
                shiftLeadButton.text = getString(R.string.recovery_notifications_advance, it)
            }
        }
        val other = addCard(content, R.string.recovery_other_events)
        labor = addSwitch(other, R.string.recovery_notifications_labor,
            settings.notifyLaborFull.get())
        meeting = addSwitch(other, R.string.recovery_notifications_meeting,
            settings.notifyMeetingComplete.get())
        recruit = addSwitch(other, R.string.recovery_notifications_recruit,
            settings.notifyRecruitComplete.get())
        refresh = addSwitch(other, R.string.recovery_notifications_refresh,
            settings.notifyRecruitRefresh.get())

        fun updateControls() {
            listOf(apFull, apEarly, trainDone, trainShift, labor, meeting, recruit, refresh)
                .forEach { it.isEnabled = enabled.isChecked }
            apLeadButton.isEnabled = enabled.isChecked && apEarly.isChecked
            shiftLeadButton.isEnabled = enabled.isChecked && trainShift.isChecked
        }
        enabled.setOnCheckedChangeListener { _, _ -> updateControls() }
        apEarly.setOnCheckedChangeListener { _, _ -> updateControls() }
        trainShift.setOnCheckedChangeListener { _, _ -> updateControls() }
        updateControls()

        val save = (layoutInflater.inflate(R.layout.item_recovery_action, content, false) as MaterialButton).apply {
            setText(R.string.save)
            setOnClickListener { saveSettings() }
        }
        content.addView(save, LinearLayout.LayoutParams(-2, -2).apply { topMargin = dp(16); gravity = android.view.Gravity.END })
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

    private fun addCard(parent: LinearLayout, title: Int): LinearLayout {
        val card = layoutInflater.inflate(R.layout.item_recovery_settings_card, parent, false) as com.google.android.material.card.MaterialCardView
        parent.addView(card)
        val body = card.getChildAt(0) as LinearLayout
        body.addView(TextView(this).apply {
            setText(title)
            setTextAppearance(com.google.android.material.R.style.TextAppearance_MaterialComponents_Subtitle1)
            setTypeface(typeface, android.graphics.Typeface.BOLD)
            setPadding(0, 0, 0, dp(4))
        })
        return body
    }

    private fun addSwitch(parent: LinearLayout, title: Int, checked: Boolean): MaterialSwitch =
        MaterialSwitch(this).apply {
            setText(title)
            isChecked = checked
            minimumHeight = dp(48)
            textSize = 14f
            setPadding(0, dp(4), 0, dp(4))
            parent.addView(this, LinearLayout.LayoutParams(-1, -2))
        }

    private fun addLeadButton(parent: LinearLayout, minutes: Int, onClick: () -> Unit): MaterialButton =
        (layoutInflater.inflate(R.layout.item_recovery_action, parent, false) as MaterialButton).apply {
            text = getString(R.string.recovery_notifications_advance, minutes)
            setOnClickListener { onClick() }
            parent.addView(this, LinearLayout.LayoutParams(-2, -2).apply { gravity = android.view.Gravity.END })
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
