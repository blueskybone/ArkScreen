package com.blueskybone.arkscreen.ui.main

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import com.blueskybone.arkscreen.R
import com.blueskybone.arkscreen.data.local.pref.SettingPrefManager
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
import com.blueskybone.arkscreen.ui.common.view.PreferenceDialog
import com.blueskybone.arkscreen.ui.main.common.PreferenceBinder
import com.blueskybone.arkscreen.ui.widget.WidgetThemeActivity
import com.blueskybone.arkscreen.util.TimeUtils
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.hjq.toast.Toaster
import org.koin.android.ext.android.getKoin
import java.util.Locale

/**
 *   Created by blueskybone
 *   Date: 2024/12/31
 */
class Function : Fragment() {
    private var _binding: FragmentDashboardBinding? = null
    private val prefManager: SettingPrefManager by getKoin().inject()
    private val alarmController: AttendanceAlarmController by getKoin().inject()

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
    }

    private fun setUpBinding() {

        PreferenceBinder.bindSwitch(
            binding = binding.AutoAttendance,
            icon = R.drawable.ic_skland,
            text = R.string.auto_attendance,
            pref = prefManager.autoAttendance
        )

        PreferenceBinder.bindPreferenceValue(
            binding = binding.RecruitMode,
            context = requireContext(),
            icon = R.drawable.ic_filter,
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
            Icon.setImageResource(R.drawable.ic_palette)
        }
        binding.WidgetAppearance.Layout.setOnClickListener {
            startActivity(Intent(requireContext(), WidgetThemeActivity::class.java))
        }


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
            icon = R.drawable.ic_battery,
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
            val bvid = "BV1624y1q7Cv"
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
            onCall = alarmController::schedule,
            offCall = alarmController::cancel,
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
                    Toaster.show("请手动在设置中找到自启动设置")
                    return
                }
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
            Toaster.show("无法打开自启动设置页面")
        }
    }
}
