package com.blueskybone.arkscreen.ui.main

import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import com.blueskybone.arkscreen.domain.repository.AccountRepository
import com.blueskybone.arkscreen.domain.usecase.realtime.GetRealTimeUseCase
import com.blueskybone.arkscreen.platform.notification.RecoveryReminderController
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import android.Manifest
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.transition.AutoTransition
import android.transition.TransitionManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.blueskybone.arkscreen.R
import com.blueskybone.arkscreen.data.local.pref.SettingPrefManager
import com.blueskybone.arkscreen.data.local.pref.InnerPrefManager
import com.blueskybone.arkscreen.platform.schedule.AttendanceAlarmController
import com.blueskybone.arkscreen.databinding.DialogTimepickerBinding
import com.blueskybone.arkscreen.databinding.FragmentDashboardBinding
import com.blueskybone.arkscreen.ui.common.bindinginfo.RecruitMode
import com.blueskybone.arkscreen.ui.common.bindinginfo.ScDelay
import com.blueskybone.arkscreen.ui.common.bindinginfo.SetAtdTime
import com.blueskybone.arkscreen.ui.common.bindinginfo.WidgetUpdateFreq
import com.blueskybone.arkscreen.ui.common.view.PreferenceDialog
import com.blueskybone.arkscreen.ui.common.view.InfoStatusTone
import com.blueskybone.arkscreen.ui.common.view.attendedToday
import com.blueskybone.arkscreen.ui.common.view.renderInfoStatus
import com.blueskybone.arkscreen.ui.main.common.PreferenceBinder
import com.blueskybone.arkscreen.ui.widget.WidgetTemplateLabActivity
import com.blueskybone.arkscreen.ui.widget.WidgetNext1
import com.blueskybone.arkscreen.ui.widget.WidgetNext2
import com.blueskybone.arkscreen.ui.widget.WidgetNext3
import com.blueskybone.arkscreen.ui.widget.WidgetNext4
import com.blueskybone.arkscreen.ui.widget.WidgetWorkScheduler
import com.blueskybone.arkscreen.platform.time.TimeUtils
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.hjq.toast.Toaster
import org.koin.android.ext.android.getKoin
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import java.util.Locale
import timber.log.Timber

/**
 *   Created by blueskybone
 *   Date: 2024/12/31
 */
class Function : Fragment() {
    private var _binding: FragmentDashboardBinding? = null
    private val prefManager: SettingPrefManager by getKoin().inject()
    private val innerPrefManager: InnerPrefManager by getKoin().inject()
    private val alarmController: AttendanceAlarmController by getKoin().inject()
    private val reminders: RecoveryReminderController by getKoin().inject()
    private val accounts: AccountRepository by getKoin().inject()
    private val getRealTime: GetRealTimeUseCase by getKoin().inject()
    private val notificationPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (!granted) Toaster.show(getString(R.string.recovery_notifications_permission))
    }
    private val model: MainModel by activityViewModel()

    private val binding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpBinding()
        renderGroupStatus()
        setUpAccordion()
    }

    override fun onResume() {
        super.onResume()
        if (_binding != null) renderGroupStatus()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setUpBinding() {

        PreferenceBinder.bindPreferenceValue(
            binding = binding.RecruitMode,
            context = requireContext(),
            icon = R.drawable.ic_function_result,
            listInfo = RecruitMode,
            pref = prefManager.recruitMode,
            onClick = null
        )

        PreferenceBinder.bindPreferenceValue(
            binding = binding.ScreenShotDelay,
            context = requireContext(),
            icon = R.drawable.ic_function_delay,
            listInfo = ScDelay,
            pref = prefManager.screenShotDelay,
            onClick = null
        )


        binding.WidgetAppearance.apply {
            Title.text = getString(R.string.widget_appearance)
            Icon.setImageResource(R.drawable.ic_function_widget)
        }
        binding.WidgetAppearance.Layout.setOnClickListener {
            startActivity(Intent(requireContext(), WidgetTemplateLabActivity::class.java))
        }
        PreferenceBinder.bindSwitch(binding.RecoveryEnabled, R.drawable.ic_recovery_bell,
            R.string.recovery_push_notifications, prefManager.recoveryNotificationsEnabled,
            onCall = {
                if (Build.VERSION.SDK_INT >= 33 && ContextCompat.checkSelfPermission(requireContext(),
                        Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                    notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
                lifecycleScope.launch {
                    accounts.observeCurrentSkAcc().first()?.let { account ->
                        getRealTime(account).onFailure { Toaster.show(getString(R.string.recovery_notifications_sync_failed)) }
                    }
                }
            },
            offCall = { reminders.cancelAll() },
        )
        binding.RecoveryEnabled.root.setOnClickListener { binding.RecoveryEnabled.Switch.toggle() }
        binding.RecoverySettings.Title.setText(R.string.recovery_settings_entry)
        binding.RecoverySettings.Icon.setImageResource(R.drawable.ic_recovery_settings)
        binding.RecoveryPermissions.Title.setText(R.string.permission_requirements)
        binding.RecoveryPermissions.Icon.setImageResource(R.drawable.ic_function_permission)
        (binding.RecoveryPermissions.root as? android.view.ViewGroup)
            ?.getChildAt(1)?.visibility = View.GONE
        binding.RecoveryPermissions.root.setOnClickListener {
            PreferenceDialog(requireContext()).add(R.string.notify_permission, R.string.notify_permission_detail) {
                (activity as? MainActivity)?.openNotificationSettings(requireContext())
            }.show()
        }
        binding.RecoverySettings.root.setOnClickListener {
            startActivity(Intent(requireContext(), RecoveryNotificationSettingsActivity::class.java))
        }

        PreferenceBinder.bindPreferenceValue(
            binding = binding.WidgetRefresh,
            context = requireContext(),
            icon = R.drawable.ic_function_refresh,
            listInfo = WidgetUpdateFreq,
            pref = prefManager.widgetUpdateFreq,
            onClick = { WidgetWorkScheduler.ensureScheduled(requireContext()) },
        )


        PreferenceBinder.bindSwitch(
            binding = binding.PowerSavingMode,
            icon = R.drawable.ic_function_saver,
            text = R.string.power_saving_mode,
            pref = prefManager.powerSavingMode,
            onCall = null,
            offCall = null
        )

        binding.RecruitVideo.setOnClickListener {
            val bvid = model.uiState.value.appRemoteConfig.recruitDemoBvid
            try {
                val url = "bilibili://video/$bvid"
                val intent = Intent(Intent.ACTION_VIEW, url.toUri())
                intent.setPackage("tv.danmaku.bili")
                startActivity(intent)
            } catch (e: Exception) {
                val bilibili = "https://bilibili.com/video/$bvid"
                startActivity(Intent(Intent.ACTION_VIEW, bilibili.toUri()))
            }
        }

        PreferenceBinder.bindSwitch(
            binding = binding.BackAutoAtd,
            icon = R.drawable.ic_function_check,
            text = R.string.back_auto_attendance,
            pref = prefManager.backAutoAtd,
            onCall = {
                alarmController.schedule()
                renderGroupStatus()
            },
            offCall = {
                alarmController.cancel()
                renderGroupStatus()
            },
        )

        binding.OverlayPermissionChip.setOnClickListener {
            PreferenceDialog(requireContext()).add(
                R.string.overlay_permission,
                R.string.overlay_permission_detail
            ) {
                (activity as MainActivity?)?.jumpToPermission(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
            }.add(R.string.notify_permission, R.string.notify_permission_detail) {
                (activity as MainActivity?)?.openNotificationSettings(requireContext())
            }.show()
        }

        binding.WidgetPermission.setOnClickListener {
            PreferenceDialog(requireContext()).add(
                R.string.turn_off_battery_optimization,
                R.string.turn_off_battery_optimization_detail
            ) {
                (activity as MainActivity?)?.requestIgnoreBatteryOptimizations()
            }.show()
        }

        binding.AttdPermission.setOnClickListener {
            PreferenceDialog(requireContext()).add(
                R.string.open_auto_start_settings,
                R.string.open_auto_start_settings_detail
            ) {
                openAutoStartSettings(requireContext())
            }.add(R.string.notify_permission, R.string.notify_permission_detail) {
                (activity as MainActivity?)?.openNotificationSettings(requireContext())
            }.show()
        }

        timePickerBinding()
    }

    private fun renderGroupStatus() {
        val notificationGranted =
            Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.POST_NOTIFICATIONS,
                ) == PackageManager.PERMISSION_GRANTED

        when {
            !Settings.canDrawOverlays(requireContext()) ->
                binding.RecruitStatus.renderInfoStatus(
                    R.string.status_overlay_required,
                    InfoStatusTone.ERROR,
                )
            !notificationGranted ->
                binding.RecruitStatus.renderInfoStatus(
                    R.string.status_notification_recommended,
                    InfoStatusTone.WARNING,
                )
            else ->
                binding.RecruitStatus.renderInfoStatus(R.string.status_ready, InfoStatusTone.SUCCESS)
        }

        val powerManager =
            requireContext().getSystemService(Context.POWER_SERVICE) as PowerManager
        when {
            !hasInstalledWidget() ->
                binding.WidgetStatus.renderInfoStatus(
                    R.string.status_widget_not_added,
                    InfoStatusTone.NEUTRAL,
                )
            !powerManager.isIgnoringBatteryOptimizations(requireContext().packageName) ->
                binding.WidgetStatus.renderInfoStatus(R.string.status_restricted, InfoStatusTone.WARNING)
            else -> binding.WidgetStatus.renderInfoStatus(R.string.status_ready, InfoStatusTone.SUCCESS)
        }

        when {
            !prefManager.backAutoAtd.get() ->
                binding.AttendanceStatus.renderInfoStatus(
                    R.string.status_disabled,
                    InfoStatusTone.NEUTRAL,
                )
            !notificationGranted ->
                binding.AttendanceStatus.renderInfoStatus(
                    R.string.status_permission_required,
                    InfoStatusTone.ERROR,
                )
            hasAttendedToday() ->
                binding.AttendanceStatus.renderInfoStatus(
                    R.string.status_attended_today,
                    InfoStatusTone.SUCCESS,
                )
            else ->
                binding.AttendanceStatus.renderInfoStatus(
                    R.string.status_not_attended_today,
                    InfoStatusTone.WARNING,
                )
        }
    }

    private enum class FunctionGroup(val storedValue: Int) {
        RECRUIT(1),
        WIDGET(2),
        ATTENDANCE(4),
        RECOVERY(8),
    }

    private fun setUpAccordion() {
        binding.RecruitHeader.setOnClickListener { toggleGroup(FunctionGroup.RECRUIT) }
        binding.WidgetHeader.setOnClickListener { toggleGroup(FunctionGroup.WIDGET) }
        binding.AttendanceHeader.setOnClickListener { toggleGroup(FunctionGroup.ATTENDANCE) }

        binding.RecoveryHeader.setOnClickListener { toggleGroup(FunctionGroup.RECOVERY) }

        val stored = innerPrefManager.functionExpandedGroup.get()
        val initial = if (stored == -1) {
            (firstGroupNeedingAttention() ?: FunctionGroup.RECRUIT).storedValue
        } else stored
        renderExpandedGroups(initial, persist = stored == -1)
    }

    private fun toggleGroup(group: FunctionGroup) {
        val current = innerPrefManager.functionExpandedGroup.get().coerceAtLeast(0)
        renderExpandedGroups(current xor group.storedValue, animate = true)
    }

    private fun renderExpandedGroups(
        expandedGroups: Int,
        persist: Boolean = true,
        animate: Boolean = false,
    ) {
        fun isExpanded(group: FunctionGroup) =
            expandedGroups and group.storedValue != 0

        if (animate) {
            TransitionManager.endTransitions(binding.root)
            TransitionManager.beginDelayedTransition(
                binding.root,
                AutoTransition().apply {
                    ordering = android.transition.TransitionSet.ORDERING_TOGETHER
                    duration = 140L
                },
            )
        }

        binding.RecruitContent.visibility =
            if (isExpanded(FunctionGroup.RECRUIT)) View.VISIBLE else View.GONE
        binding.WidgetContent.visibility =
            if (isExpanded(FunctionGroup.WIDGET)) View.VISIBLE else View.GONE
        binding.AttendanceContent.visibility =
            if (isExpanded(FunctionGroup.ATTENDANCE)) View.VISIBLE else View.GONE

        binding.RecoveryContent.visibility = if (isExpanded(FunctionGroup.RECOVERY)) View.VISIBLE else View.GONE
        binding.RecoveryExpand.animateToGroupState(isExpanded(FunctionGroup.RECOVERY), animate)

        binding.RecruitExpand.animateToGroupState(isExpanded(FunctionGroup.RECRUIT), animate)
        binding.WidgetExpand.animateToGroupState(isExpanded(FunctionGroup.WIDGET), animate)
        binding.AttendanceExpand.animateToGroupState(isExpanded(FunctionGroup.ATTENDANCE), animate)

        if (persist) innerPrefManager.functionExpandedGroup.set(expandedGroups)
    }

    private fun View.animateToGroupState(expanded: Boolean, animated: Boolean) {
        animate().cancel()
        if (animated) {
            animate().rotation(if (expanded) 90f else 0f).setDuration(140L).start()
        } else {
            rotation = if (expanded) 90f else 0f
        }
    }

    private fun firstGroupNeedingAttention(): FunctionGroup? {
        val notificationGranted =
            Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.POST_NOTIFICATIONS,
                ) == PackageManager.PERMISSION_GRANTED
        if (!Settings.canDrawOverlays(requireContext()) || !notificationGranted) {
            return FunctionGroup.RECRUIT
        }

        val powerManager =
            requireContext().getSystemService(Context.POWER_SERVICE) as PowerManager
        if (hasInstalledWidget() &&
            !powerManager.isIgnoringBatteryOptimizations(requireContext().packageName)
        ) {
            return FunctionGroup.WIDGET
        }
        if (prefManager.backAutoAtd.get() && !notificationGranted) {
            return FunctionGroup.ATTENDANCE
        }
        return null
    }

    private fun hasInstalledWidget(): Boolean {
        val manager = AppWidgetManager.getInstance(requireContext())
        return listOf(
            WidgetNext1::class.java,
            WidgetNext2::class.java,
            WidgetNext3::class.java,
            WidgetNext4::class.java,
        )
            .any { provider ->
                manager.getAppWidgetIds(ComponentName(requireContext(), provider)).isNotEmpty()
            }
    }

    private fun hasAttendedToday(): Boolean {
        return attendedToday(innerPrefManager.lastAttendanceTs.get())
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    private fun timePickerBinding() {
        binding.SetAtdTime.Icon.setImageResource(R.drawable.ic_function_clock)
        binding.SetAtdTime.Title.setText(SetAtdTime.title)
        binding.SetAtdTime.SubTitle.setText(SetAtdTime.subTitle)
        val hour = prefManager.alarmAtdHour.get()
        val min = prefManager.alarmAtdMin.get()
        binding.SetAtdTime.Value.text =
            TimeUtils.getDigitalString(hour, min)
        binding.SetAtdTime.Layout.setOnClickListener {
            val dialogBinding = DialogTimepickerBinding.inflate(layoutInflater).apply {
                TimePicker.hour = prefManager.alarmAtdHour.get()
                TimePicker.minute = prefManager.alarmAtdMin.get()
            }
            MaterialAlertDialogBuilder(requireContext())
                .setView(dialogBinding.root)
                .setTitle(R.string.attendance_time)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.confirm) { _, _ ->
                    val newHour = dialogBinding.TimePicker.hour
                    val newMin = dialogBinding.TimePicker.minute
                    prefManager.alarmAtdHour.set(newHour)
                    prefManager.alarmAtdMin.set(newMin)
                    binding.SetAtdTime.Value.text =
                        getString(
                            R.string.auto_attendance_time,
                            TimeUtils.getDigitalString(newHour, newMin)
                        )
                    alarmController.cancel()
                    if (prefManager.backAutoAtd.get()) alarmController.schedule()
                }.show()
        }
    }

    private fun openAutoStartSettings(context: Context) {
        try {
            val intent = Intent()
            val manufacturer = Build.MANUFACTURER.lowercase(Locale.ROOT)
            when {
                manufacturer.contains("xiaomi") -> {
                    intent.component = ComponentName(
                        "com.miui.securitycenter",
                        "com.miui.permcenter.autostart.AutoStartManagementActivity"
                    )
                }

                manufacturer.contains("oppo") -> {
                    intent.component = ComponentName(
                        "com.coloros.safecenter",
                        "com.coloros.safecenter.permission.startup.StartupAppListActivity"
                    )
                }

                manufacturer.contains("vivo") -> {
                    intent.component = ComponentName(
                        "com.vivo.permissionmanager",
                        "com.vivo.permissionmanager.activity.BgStartUpManagerActivity"
                    )
                }

                manufacturer.contains("honor") -> {
                    intent.component = ComponentName(
                        "com.huawei.systemmanager",
                        "com.huawei.systemmanager.optimize.process.ProtectActivity"
                    )
                }

                else -> {
                    Toaster.show(context.getString(R.string.open_auto_start_manually))
                    return
                }
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Timber.w(e, "Failed to open auto-start settings")
            Toaster.show(context.getString(R.string.open_auto_start_failed))
        }
    }
}
