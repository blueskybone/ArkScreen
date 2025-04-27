package com.blueskybone.arkscreen.fragment

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SwitchCompat
import androidx.fragment.app.Fragment
import com.blueskybone.arkscreen.APP
import com.blueskybone.arkscreen.R
import com.blueskybone.arkscreen.activity.AboutActivity
import com.blueskybone.arkscreen.activity.MainActivity
import com.blueskybone.arkscreen.bindinginfo.AppTheme
import com.blueskybone.arkscreen.bindinginfo.BackAutoAtd
import com.blueskybone.arkscreen.bindinginfo.FloatWindowAppearance
import com.blueskybone.arkscreen.bindinginfo.ListInfo
import com.blueskybone.arkscreen.bindinginfo.OpenAutoStartSettings
import com.blueskybone.arkscreen.bindinginfo.OverlayPermission
import com.blueskybone.arkscreen.bindinginfo.PowerSavingMode
import com.blueskybone.arkscreen.bindinginfo.RecruitMode
import com.blueskybone.arkscreen.bindinginfo.ScreenshotDelay
import com.blueskybone.arkscreen.bindinginfo.SeekBarInfo
import com.blueskybone.arkscreen.bindinginfo.SetAtdTime
import com.blueskybone.arkscreen.bindinginfo.TextInfo
import com.blueskybone.arkscreen.bindinginfo.TurnOffBatteryOptimization
import com.blueskybone.arkscreen.bindinginfo.UseInnerWeb
import com.blueskybone.arkscreen.bindinginfo.WidgetAlpha
import com.blueskybone.arkscreen.bindinginfo.WidgetAppearance
import com.blueskybone.arkscreen.bindinginfo.WidgetRefresh
import com.blueskybone.arkscreen.bindinginfo.WidgetSize
import com.blueskybone.arkscreen.databinding.DialogTimepickerBinding
import com.blueskybone.arkscreen.databinding.FragmentSettingBinding
import com.blueskybone.arkscreen.databinding.PreferenceBinding
import com.blueskybone.arkscreen.databinding.PreferenceSeekbarBinding
import com.blueskybone.arkscreen.databinding.PreferenceSwitchBinding
import com.blueskybone.arkscreen.preference.PrefManager
import com.blueskybone.arkscreen.preference.preference.Preference
import com.blueskybone.arkscreen.receiver.WidgetReceiver
import com.blueskybone.arkscreen.util.TimeUtils.getDigitalString
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.hjq.toast.Toaster
import org.koin.android.ext.android.getKoin
import java.util.Locale

/**
 *   Created by blueskybone
 *   Date: 2025/1/4
 */
class Setting : Fragment() {
    private var _binding: FragmentSettingBinding? = null
    private val binding get() = _binding!!
    private val prefManager: PrefManager by getKoin().inject()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)
        _binding = FragmentSettingBinding.inflate(inflater)
        setUpBinding()
        return binding.root
    }

    override fun onDestroy() {
        _binding = null
        super.onDestroy()
    }

    private fun setUpBinding() {
        bindSwitchView(binding.AutoAttendance, prefManager.autoAttendance)
        binding.RecruitMode.setUp(RecruitMode, prefManager.recruitMode, null)
        binding.FloatWindowAppearance.setUp(
            FloatWindowAppearance,
            prefManager.floatWindowAppearance, null
        )

        binding.ScreenShotDelay.setUp(ScreenshotDelay, prefManager.screenShotDelay)
        binding.TurnOffBatteryOptimization.setUp(TurnOffBatteryOptimization)
        binding.WidgetAppearance.setUp(WidgetAppearance, prefManager.widgetAppearance, null)
        binding.WidgetAlpha.setUp(WidgetAlpha, prefManager.widgetAlpha)
        binding.WidgetContentSize.setUp(WidgetSize, prefManager.widgetContentSize, null)

        binding.AppTheme.setUp(AppTheme, prefManager.appTheme) {
            Toaster.show("重启应用生效")
//            requireActivity().recreate()
        }
        binding.WidgetRefresh.setUp(WidgetRefresh)

//        bindSwitchView(binding.RealTimePageAttendance, prefManager.realTimePageAttendance)
//        bindSwitchView(binding.RealTimePageShowStarter, prefManager.realTimePageShowStarter)
        bindSwitchView(binding.AutoUpdateApp, prefManager.autoUpdateApp)
//        bindSwitchView(binding.TimeCorrect, prefManager.timeCorrect)
//        bindSwitchView(binding.DebugMode, prefManager.debugMode)
        bindSwitchView(binding.ShowHomeAnnounce, prefManager.showHomeAnnounce)

        binding.OverlayPermission.setUp(OverlayPermission)
        binding.OpenAutoStartSettings.setUp(OpenAutoStartSettings)
        binding.PowerSavingMode.setUp(PowerSavingMode, prefManager.powerSavingMode, null, null)
        binding.UseInnerWeb.setUp(UseInnerWeb, prefManager.useInnerWeb, null, null)
        binding.BackAutoAtd.setUp(
            BackAutoAtd,
            prefManager.backAutoAtd,
            { APP.setDailyAlarm() },
            { APP.cancelDailyAlarm() }
        )

        binding.OverlayPermission.Layout.setOnClickListener {
            (activity as MainActivity?)?.jumpToOverlayPermission()
        }
        binding.OpenAutoStartSettings.Layout.setOnClickListener {
            openAutoStartSettings(requireContext())
        }
        binding.TurnOffBatteryOptimization.Layout.setOnClickListener {
            (activity as MainActivity?)?.requestIgnoreBatteryOptimizations()
        }
        binding.WidgetRefresh.Layout.setOnClickListener {
            val intent = Intent(APP, WidgetReceiver::class.java)
            intent.action = WidgetReceiver.MANUAL_UPDATE
            intent.putExtra("msg", "组件刷新中...")
            APP.sendBroadcast(intent)
        }
        binding.About.setOnClickListener {
            startActivity(Intent(requireContext(), AboutActivity::class.java))
        }

        binding.Manual.setOnClickListener {
            val url = "https://www.bilibili.com/opus/1031126823526727688"
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        }
        timePickerBinding()
    }


    private fun bindSwitchView(switch: SwitchCompat, pref: Preference<Boolean>) {
        switch.isChecked = pref.get()
        switch.setOnCheckedChangeListener { _, isChecked -> pref.set(isChecked) }
    }


    private fun PreferenceSeekbarBinding.setUp(
        seekBarInfo: SeekBarInfo, pref: Preference<Int>
    ) {
        Title.setText(seekBarInfo.title)

        Slider.valueTo = seekBarInfo.max.toFloat()
        Slider.valueFrom = seekBarInfo.min.toFloat()

        Slider.value = pref.get().toFloat()
        Slider.stepSize = seekBarInfo.step.toFloat()
        Slider.addOnChangeListener { _, value, _ ->
            pref.set(value.toInt())
        }
    }

    private fun PreferenceSwitchBinding.setUp(
        textInfo: TextInfo,
        pref: Preference<Boolean>,
        onCall: (() -> Unit)?,
        offCall: (() -> Unit)?,
    ) {
        Title.setText(textInfo.title)
        Value.setText(textInfo.subTitle)
        this.Switch.isChecked = pref.get()
        this.Switch.setOnCheckedChangeListener { _, isChecked ->
            pref.set(isChecked)
            if (isChecked) onCall?.invoke()
            else offCall?.invoke()
        }
    }

    private fun PreferenceBinding.setUp(textInfo: TextInfo) {
        Title.setText(textInfo.title)
        Value.setText(textInfo.subTitle)
    }

    private fun PreferenceBinding.setUp(
        listInfo: ListInfo,
        pref: Preference<String>,
        onClick: (() -> Unit)?
    ) {
        Title.setText(listInfo.title)
        val entries = listInfo.getEntries(requireContext())
        val entryValues = listInfo.getEntryValues()
        var checked = entryValues.indexOf(pref.get())
        val displayValue = entries[checked]
        Value.text = displayValue
        root.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle(listInfo.title)
                .setSingleChoiceItems(entries, checked) { dialog, which ->
                    dialog.cancel()
                    pref.set(entryValues[which])
                    Value.text = entries[which]
                    checked = which
                    onClick?.invoke()
                }
                .setNegativeButton(R.string.cancel, null)
                .show()
        }
    }

    private fun timePickerBinding() {
        binding.SetAtdTime.Title.setText(SetAtdTime.title)
        val hour = prefManager.alarmAtdHour.get()
        val min = prefManager.alarmAtdMin.get()
        binding.SetAtdTime.Value.text =
            getString(R.string.auto_attendance_time, getDigitalString(hour, min))
        binding.SetAtdTime.Layout.setOnClickListener {
            val dialogBinding = DialogTimepickerBinding.inflate(layoutInflater).apply {
                TimePicker.hour = prefManager.alarmAtdHour.get()
                TimePicker.minute = prefManager.alarmAtdMin.get()
            }
            MaterialAlertDialogBuilder(requireContext())
                .setView(dialogBinding.root)
                .setTitle(R.string.set_auto_attendance_time)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.confirm) { _, _ ->
                    val newHour = dialogBinding.TimePicker.hour
                    val newMin = dialogBinding.TimePicker.minute
                    prefManager.alarmAtdHour.set(newHour)
                    prefManager.alarmAtdMin.set(newMin)
                    binding.SetAtdTime.Value.text =
                        getString(R.string.auto_attendance_time, getDigitalString(newHour, newMin))
                    APP.cancelDailyAlarm()
                    if (prefManager.backAutoAtd.get()) APP.setDailyAlarm()
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