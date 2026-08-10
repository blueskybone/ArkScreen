package com.blueskybone.arkscreen.ui.fragment

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SwitchCompat
import androidx.fragment.app.Fragment
import com.blueskybone.arkscreen.APP
import com.blueskybone.arkscreen.R
import com.blueskybone.arkscreen.common.PreferenceDialog
import com.blueskybone.arkscreen.databinding.DialogTimepickerBinding
import com.blueskybone.arkscreen.databinding.FragmentDashboardBinding
import com.blueskybone.arkscreen.databinding.PreferenceBinding
import com.blueskybone.arkscreen.databinding.PreferenceSeekbarBinding
import com.blueskybone.arkscreen.databinding.PreferenceSubSwitchBinding
import com.blueskybone.arkscreen.databinding.PreferenceSubValueBinding
import com.blueskybone.arkscreen.databinding.PreferenceSwitchBinding
import com.blueskybone.arkscreen.databinding.PreferenceValueBinding
import com.blueskybone.arkscreen.preference.PrefManager
import com.blueskybone.arkscreen.preference.preference.Preference
import com.blueskybone.arkscreen.ui.activity.MainActivity
import com.blueskybone.arkscreen.ui.activity.WidgetThemeActivity
import com.blueskybone.arkscreen.ui.bindinginfo.BackAutoAtd
import com.blueskybone.arkscreen.ui.bindinginfo.FloatWindowAppearance
import com.blueskybone.arkscreen.ui.bindinginfo.ListInfo
import com.blueskybone.arkscreen.ui.bindinginfo.NotifyPermission
import com.blueskybone.arkscreen.ui.bindinginfo.OpenAutoStartSettings
import com.blueskybone.arkscreen.ui.bindinginfo.OverlayPermission
import com.blueskybone.arkscreen.ui.bindinginfo.PowerSavingMode
import com.blueskybone.arkscreen.ui.bindinginfo.RecruitMode
import com.blueskybone.arkscreen.ui.bindinginfo.ScDelay
import com.blueskybone.arkscreen.ui.bindinginfo.ScreenshotDelay
//import com.blueskybone.arkscreen.ui.bindinginfo.ScreenshotDelay
import com.blueskybone.arkscreen.ui.bindinginfo.SeekBarInfo
import com.blueskybone.arkscreen.ui.bindinginfo.SetAtdTime
import com.blueskybone.arkscreen.ui.bindinginfo.TextInfo
import com.blueskybone.arkscreen.ui.bindinginfo.TurnOffBatteryOptimization
import com.blueskybone.arkscreen.ui.bindinginfo.WidgetUpdateFreq
import com.blueskybone.arkscreen.util.TimeUtils
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.hjq.toast.Toaster
import org.koin.android.ext.android.getKoin
import timber.log.Timber
import java.util.Locale

/**
 *   Created by blueskybone
 *   Date: 2024/12/31
 */
class Function : Fragment() {
    private var _binding: FragmentDashboardBinding? = null
    private val prefManager: PrefManager by getKoin().inject()

    private val binding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
//        _binding = FragmentDashboardBinding.inflate(inflater)
//        setUpBinding()
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // 确保 context 不为空再执行
        if (_binding != null && context != null) {
            try{
                setUpBinding()
            }catch (e:Exception){
                Timber.e(e.message)
            }

        }
    }

    private fun setUpBinding() {
//        bindSwitchView(binding.AutoAttendance, prefManager.autoAttendance)
        binding.AutoAttendance.setUp(
            R.drawable.ic_skland,
            R.string.auto_attendance,
            prefManager.autoAttendance
        )
        binding.RecruitMode.setUp(R.drawable.ic_filter, RecruitMode, prefManager.recruitMode, null)
        binding.FloatWindowAppearance.setUp(
            null,
            FloatWindowAppearance,
            prefManager.floatWindowAppearance, null
        )

        binding.ScreenShotDelay.setUp(
            R.drawable.ic_delay,
            ScreenshotDelay,
            ScDelay,
            prefManager.screenShotDelay,
            null
        )
        binding.TurnOffBatteryOptimization.setUp(TurnOffBatteryOptimization)
        binding.WidgetAppearance.apply {
            this.Title.text = getString(R.string.widget_appearance)
            this.Icon.setImageResource(R.drawable.ic_palette)
        }
        binding.WidgetAppearance.Layout.setOnClickListener {
            startActivity(Intent(requireContext(), WidgetThemeActivity::class.java))
        }
        binding.WidgetRefresh.setUp(null, WidgetUpdateFreq, prefManager.widgetUpdateFreq, null)

        binding.OverlayPermission.setUp(OverlayPermission)
        binding.NotifyPermission.setUp(NotifyPermission)
        binding.PowerSavingMode.setUp(
            R.drawable.ic_battery,
            PowerSavingMode,
            prefManager.powerSavingMode,
            null,
            null
        )
        binding.OpenAutoStartSettings.setUp(OpenAutoStartSettings)
        binding.RecruitVideo.setOnClickListener {
            val bvid = "BV1624y1q7Cv"
            try {
                val url = "bilibili://video/$bvid"
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                intent.setPackage("tv.danmaku.bili")
                startActivity(intent)
            } catch (e: java.lang.Exception) {
                val bilibili = "https://bilibili.com/video/$bvid"
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(bilibili)))
            }
        }


        binding.BackAutoAtd.setUp(
            R.drawable.ic_check,
            BackAutoAtd,
            prefManager.backAutoAtd,
            { APP.setDailyAlarm() },
            { APP.cancelDailyAlarm() }
        )

        binding.OverlayPermissionChip.setOnClickListener{
            PreferenceDialog(requireContext()).add(R.string.overlay_permission, R.string.confirm){
                (activity as MainActivity?)?.jumpToPermission(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
            }.add(R.string.notify_permission, R.string.notify_permission_detail){
                (activity as MainActivity?)?.openNotificationSettings(requireContext())
            }.show()
        }

        binding.WidgetPermission.setOnClickListener{
            PreferenceDialog(requireContext()).add(R.string.turn_off_battery_optimization, R.string.turn_off_battery_optimization_detail){
                (activity as MainActivity?)?.requestIgnoreBatteryOptimizations()
            }.show()
        }

        binding.AttdPermission.setOnClickListener {
            PreferenceDialog(requireContext()).add(R.string.open_auto_start_settings, R.string.open_auto_start_settings_detail){
                openAutoStartSettings(requireContext())
            }.show()
        }

//        binding.OpenAutoStartSettings.Layout.setOnClickListener {
//            openAutoStartSettings(requireContext())
//        }
//        binding.OverlayPermission.Layout.setOnClickListener {
//            (activity as MainActivity?)?.jumpToPermission(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
//        }
//        binding.NotifyPermission.Layout.setOnClickListener {
//            (activity as MainActivity?)?.openNotificationSettings(requireContext())
//        }
        binding.WidgetInfo.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.account_info)
                .setMessage(R.string.widget_info)
                .setNegativeButton(R.string.confirm, null)
                .show()
        }
        timePickerBinding()
    }

    private fun bindSwitchView(switch: SwitchCompat, pref: Preference<Boolean>) {
        switch.isChecked = pref.get()
        switch.setOnCheckedChangeListener { _, isChecked -> pref.set(isChecked) }
    }

    private fun PreferenceSwitchBinding.setUp(
        icon: Int?,
        text: Int,
        pref: Preference<Boolean>
    ) {
        Switch.isChecked = pref.get()
        Switch.setOnCheckedChangeListener { _, isChecked -> pref.set(isChecked) }
        if (icon == null) {
            Icon.visibility = View.GONE
        } else {
            Icon.setImageResource(icon)
        }
        Title.setText(text)
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

    private fun PreferenceSubValueBinding.setUp(
        icon: Int?,
        textInfo: TextInfo,
        listInfo: ListInfo,
        pref: Preference<String>,
        onClick: (() -> Unit)?
    ) {
        if (icon == null) {
            Icon.visibility = View.GONE
        } else Icon.setImageResource(icon)
        Title.setText(textInfo.title)
        SubTitle.setText(textInfo.subTitle)
        val entries = listInfo.getEntries(requireContext())
        val entryValues = listInfo.getEntryValues()

        var checked = entryValues.indexOf(pref.get()).coerceAtLeast(0)
        val displayValue = if (checked < entries.size) entries[checked] else "未知"
        Value.text = displayValue
//
//        var checked = entryValues.indexOf(pref.get())
//        val displayValue = entries[checked]
//        Value.text = displayValue
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

    private fun PreferenceSubSwitchBinding.setUp(
        icon: Int?,
        textInfo: TextInfo,
        pref: Preference<Boolean>,
        onCall: (() -> Unit)?,
        offCall: (() -> Unit)?,
    ) {
        if (icon == null) {
            Icon.visibility = View.GONE
        } else Icon.setImageResource(icon)
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
        icon: Int?,
        listInfo: ListInfo,
        pref: Preference<String>,
        onClick: (() -> Unit)?
    ) {
        if (icon == null) {
            Icon.visibility = View.GONE
        } else Icon.setImageResource(icon)
        Title.setText(listInfo.title)
        Title.setText(listInfo.title)
        val entries = listInfo.getEntries(requireContext())



        val entryValues = listInfo.getEntryValues()
//        var checked = entryValues.indexOf(pref.get())
//        val displayValue = entries[checked]
//        Value.text = displayValue
        var checked = entryValues.indexOf(pref.get()).coerceAtLeast(0)
        val displayValue = if (checked < entries.size) entries[checked] else "未知"
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

    private fun PreferenceValueBinding.setUp(
        icon: Int?,
        listInfo: ListInfo,
        pref: Preference<String>,
        onClick: (() -> Unit)?
    ) {
//        val context = context ?: return // 防御 Context 为空
        if (icon == null) {
            Icon.visibility = View.GONE
        } else Icon.setImageResource(icon)
        Title.setText(listInfo.title)
        val entries = listInfo.getEntries(requireContext())



        val entryValues = listInfo.getEntryValues()
//        var checked = entryValues.indexOf(pref.get())

        var checked = entryValues.indexOf(pref.get()).coerceAtLeast(0)
        val displayValue = if (checked < entries.size) entries[checked] else "未知"
//        Value.text = displayValue

        if (checked == -1 || checked >= entries.size) {
            checked = 0 // 或者给个默认值
        }

        if (entries.isNotEmpty()) {
            Value.text = entries[checked]
        }

//        val displayValue = entries[checked]
//        Value.text = displayValue
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