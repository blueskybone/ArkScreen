package com.blueskybone.arkscreen.ui.main

import android.Manifest
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.net.toUri
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.blueskybone.arkscreen.R
import com.blueskybone.arkscreen.data.local.pref.SettingPrefManager
import com.blueskybone.arkscreen.data.local.pref.InnerPrefManager
import com.blueskybone.arkscreen.platform.schedule.AttendanceAlarmController
import com.blueskybone.arkscreen.databinding.DialogTimepickerBinding
import com.blueskybone.arkscreen.databinding.FragmentDashboardBinding
import com.blueskybone.arkscreen.ui.common.bindinginfo.BackAutoAtd
import com.blueskybone.arkscreen.ui.common.bindinginfo.FloatWindowAppearance
import com.blueskybone.arkscreen.ui.common.bindinginfo.NotifyPermission
import com.blueskybone.arkscreen.ui.common.bindinginfo.OpenAutoStartSettings
import com.blueskybone.arkscreen.ui.common.bindinginfo.OverlayPermission
import com.blueskybone.arkscreen.ui.common.bindinginfo.PowerSavingMode
import com.blueskybone.arkscreen.ui.common.bindinginfo.RecruitMode
import com.blueskybone.arkscreen.ui.common.bindinginfo.ScDelay
import com.blueskybone.arkscreen.ui.common.bindinginfo.ScreenshotDelay
import com.blueskybone.arkscreen.ui.common.bindinginfo.SetAtdTime
import com.blueskybone.arkscreen.ui.common.bindinginfo.WidgetUpdateFreq
import com.blueskybone.arkscreen.ui.common.view.PreferenceDialog
import com.blueskybone.arkscreen.ui.main.common.PreferenceBinder
import com.blueskybone.arkscreen.ui.widget.WidgetThemeActivity
import com.blueskybone.arkscreen.ui.widget.Widget1
import com.blueskybone.arkscreen.ui.widget.Widget2
import com.blueskybone.arkscreen.ui.widget.Widget3
import com.blueskybone.arkscreen.ui.widget.Widget4
import com.blueskybone.arkscreen.ui.widget.WidgetWorkScheduler
import com.blueskybone.arkscreen.platform.time.TimeUtils
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.hjq.toast.Toaster
import org.koin.android.ext.android.getKoin
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import java.util.Locale
import java.time.Instant
import java.time.ZoneId
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

    private fun setUpBinding() {

        PreferenceBinder.bindPreferenceValue(
            binding = binding.RecruitMode,
            context = requireContext(),
            icon = R.drawable.ic_result_display,
            listInfo = RecruitMode,
            pref = prefManager.recruitMode,
            onClick = null
        )

        PreferenceBinder.bindPreference(
            binding = binding.FloatWindowAppearance,
            context = requireContext(),
            icon = null,
            listInfo = FloatWindowAppearance,
            pref = prefManager.floatWindowAppearance,
            onClick = null
        )


        PreferenceBinder.bindSubValue(
            binding = binding.ScreenShotDelay,
            context = requireContext(),
            icon = R.drawable.ic_delay,
            textInfo = ScreenshotDelay,
            listInfo = ScDelay,
            pref = prefManager.screenShotDelay,
            onClick = null
        )


        binding.WidgetAppearance.apply {
            Title.text = getString(R.string.widget_appearance)
            Icon.setImageResource(R.drawable.ic_widget_appearance)
        }
        binding.WidgetAppearance.Layout.setOnClickListener {
            startActivity(Intent(requireContext(), WidgetThemeActivity::class.java))
        }

        PreferenceBinder.bindPreference(
            binding = binding.WidgetRefresh,
            context = requireContext(),
            icon = R.drawable.ic_refresh,
            listInfo = WidgetUpdateFreq,
            pref = prefManager.widgetUpdateFreq,
            onClick = { WidgetWorkScheduler.ensureScheduled(requireContext()) },
        )


        PreferenceBinder.bindPreferenceText(
            binding = binding.OverlayPermission,
            icon = null,
            textInfo = OverlayPermission
        )

        PreferenceBinder.bindPreferenceText(
            binding = binding.NotifyPermission,
            icon = null,
            textInfo = NotifyPermission
        )

        PreferenceBinder.bindSubSwitch(
            binding = binding.PowerSavingMode,
            icon = R.drawable.ic_data_saver,
            textInfo = PowerSavingMode,
            pref = prefManager.powerSavingMode,
            onCall = null,
            offCall = null
        )

        PreferenceBinder.bindPreferenceText(
            binding = binding.OpenAutoStartSettings,
            icon = null,
            textInfo = OpenAutoStartSettings
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

        PreferenceBinder.bindSubSwitch(
            binding = binding.BackAutoAtd,
            icon = R.drawable.ic_check,
            textInfo = BackAutoAtd,
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

        binding.WidgetInfo.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.account_info)
                .setMessage(R.string.widget_info)
                .setNegativeButton(R.string.confirm, null)
                .show()
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
                binding.RecruitStatus.renderStatus(
                    R.string.status_overlay_required,
                    StatusTone.ERROR,
                )
            !notificationGranted ->
                binding.RecruitStatus.renderStatus(
                    R.string.status_notification_recommended,
                    StatusTone.WARNING,
                )
            else ->
                binding.RecruitStatus.renderStatus(R.string.status_ready, StatusTone.SUCCESS)
        }

        val powerManager =
            requireContext().getSystemService(Context.POWER_SERVICE) as PowerManager
        when {
            !hasInstalledWidget() ->
                binding.WidgetStatus.renderStatus(
                    R.string.status_widget_not_added,
                    StatusTone.NEUTRAL,
                )
            !powerManager.isIgnoringBatteryOptimizations(requireContext().packageName) ->
                binding.WidgetStatus.renderStatus(R.string.status_restricted, StatusTone.WARNING)
            else -> binding.WidgetStatus.renderStatus(R.string.status_ready, StatusTone.SUCCESS)
        }

        when {
            !prefManager.backAutoAtd.get() ->
                binding.AttendanceStatus.renderStatus(
                    R.string.status_disabled,
                    StatusTone.NEUTRAL,
                )
            !notificationGranted ->
                binding.AttendanceStatus.renderStatus(
                    R.string.status_permission_required,
                    StatusTone.ERROR,
                )
            hasAttendedToday() ->
                binding.AttendanceStatus.renderStatus(
                    R.string.status_attended_today,
                    StatusTone.SUCCESS,
                )
            else ->
                binding.AttendanceStatus.renderStatus(
                    R.string.status_enabled,
                    StatusTone.SUCCESS,
                )
        }
    }

    private enum class StatusTone { SUCCESS, WARNING, ERROR, NEUTRAL }

    private enum class FunctionGroup(val storedValue: Int) {
        RECRUIT(1),
        WIDGET(2),
        ATTENDANCE(4),
    }

    private fun setUpAccordion() {
        binding.RecruitHeader.setOnClickListener { toggleGroup(FunctionGroup.RECRUIT) }
        binding.WidgetHeader.setOnClickListener { toggleGroup(FunctionGroup.WIDGET) }
        binding.AttendanceHeader.setOnClickListener { toggleGroup(FunctionGroup.ATTENDANCE) }

        val stored = innerPrefManager.functionExpandedGroup.get()
        val initial = if (stored == -1) {
            (firstGroupNeedingAttention() ?: FunctionGroup.RECRUIT).storedValue
        } else stored
        renderExpandedGroups(initial, persist = stored == -1)
    }

    private fun toggleGroup(group: FunctionGroup) {
        val current = innerPrefManager.functionExpandedGroup.get().coerceAtLeast(0)
        renderExpandedGroups(current xor group.storedValue)
    }

    private fun renderExpandedGroups(expandedGroups: Int, persist: Boolean = true) {
        fun isExpanded(group: FunctionGroup) =
            expandedGroups and group.storedValue != 0

        binding.RecruitContent.visibility =
            if (isExpanded(FunctionGroup.RECRUIT)) View.VISIBLE else View.GONE
        binding.WidgetContent.visibility =
            if (isExpanded(FunctionGroup.WIDGET)) View.VISIBLE else View.GONE
        binding.AttendanceContent.visibility =
            if (isExpanded(FunctionGroup.ATTENDANCE)) View.VISIBLE else View.GONE

        binding.RecruitExpand.animateToGroupState(isExpanded(FunctionGroup.RECRUIT))
        binding.WidgetExpand.animateToGroupState(isExpanded(FunctionGroup.WIDGET))
        binding.AttendanceExpand.animateToGroupState(isExpanded(FunctionGroup.ATTENDANCE))

        if (persist) innerPrefManager.functionExpandedGroup.set(expandedGroups)
    }

    private fun View.animateToGroupState(expanded: Boolean) {
        animate().rotation(if (expanded) 90f else 0f).setDuration(160L).start()
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

    private fun TextView.renderStatus(textRes: Int, tone: StatusTone) {
        val (container, content) = when (tone) {
            StatusTone.SUCCESS ->
                R.color.status_success_container to R.color.status_success_content
            StatusTone.WARNING ->
                R.color.status_warning_container to R.color.status_warning_content
            StatusTone.ERROR ->
                R.color.status_error_container to R.color.status_error_content
            StatusTone.NEUTRAL ->
                R.color.status_neutral_container to R.color.status_neutral_content
        }
        setText(textRes)
        backgroundTintList = ColorStateList.valueOf(
            ContextCompat.getColor(requireContext(), container)
        )
        setTextColor(ContextCompat.getColor(requireContext(), content))
    }

    private fun hasInstalledWidget(): Boolean {
        val manager = AppWidgetManager.getInstance(requireContext())
        return listOf(Widget1::class.java, Widget2::class.java, Widget3::class.java, Widget4::class.java)
            .any { provider ->
                manager.getAppWidgetIds(ComponentName(requireContext(), provider)).isNotEmpty()
            }
    }

    private fun hasAttendedToday(): Boolean {
        val timestamp = innerPrefManager.lastAttendanceTs.get()
        if (timestamp <= 0) return false
        val zone = ZoneId.of("Asia/Shanghai")
        return Instant.ofEpochSecond(timestamp).atZone(zone).toLocalDate() ==
            Instant.now().atZone(zone).toLocalDate()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    private fun timePickerBinding() {
        binding.SetAtdTime.Icon.setImageResource(R.drawable.ic_clock)
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
