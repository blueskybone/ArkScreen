package com.blueskybone.arkscreen.fragment

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.SwitchCompat
import androidx.fragment.app.Fragment
import com.blueskybone.arkscreen.APP
import com.blueskybone.arkscreen.R
import com.blueskybone.arkscreen.bindinginfo.AppTheme
import com.blueskybone.arkscreen.bindinginfo.CheckUpdate
import com.blueskybone.arkscreen.bindinginfo.GroupChat
import com.blueskybone.arkscreen.bindinginfo.ListInfo
import com.blueskybone.arkscreen.bindinginfo.SeekBarInfo
import com.blueskybone.arkscreen.bindinginfo.TextInfo
import com.blueskybone.arkscreen.bindinginfo.UseInnerWeb
import com.blueskybone.arkscreen.common.MenuDialog
import com.blueskybone.arkscreen.AppUpdateInfo
import com.blueskybone.arkscreen.databinding.FragmentSettingBinding
import com.blueskybone.arkscreen.databinding.PreferenceBinding
import com.blueskybone.arkscreen.databinding.PreferenceSeekbarBinding
import com.blueskybone.arkscreen.databinding.PreferenceSwitchBinding
import com.blueskybone.arkscreen.preference.PrefManager
import com.blueskybone.arkscreen.preference.preference.Preference
import com.blueskybone.arkscreen.util.copyToClipboard
import com.blueskybone.arkscreen.util.getAppVersionName
import com.blueskybone.arkscreen.util.saveDrawableToGallery
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.hjq.toast.Toaster
import io.noties.markwon.Markwon
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
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
//        bindSwitchView(binding.AutoAttendance, prefManager.autoAttendance)
//        binding.RecruitMode.setUp(RecruitMode, prefManager.recruitMode, null)
//        binding.FloatWindowAppearance.setUp(
//            FloatWindowAppearance,
//            prefManager.floatWindowAppearance, null
//        )
//
//        binding.ScreenShotDelay.setUp(ScreenshotDelay, prefManager.screenShotDelay)
//        binding.TurnOffBatteryOptimization.setUp(TurnOffBatteryOptimization)
//        binding.WidgetAppearance.setUp(WidgetAppearance, prefManager.widgetAppearance, null)
//        binding.WidgetAlpha.setUp(WidgetAlpha, prefManager.widgetAlpha)
//        binding.WidgetContentSize.setUp(WidgetSize, prefManager.widgetContentSize, null)


//        binding.WidgetRefresh.setUp(WidgetRefresh)

//        bindSwitchView(binding.RealTimePageAttendance, prefManager.realTimePageAttendance)
//        bindSwitchView(binding.RealTimePageShowStarter, prefManager.realTimePageShowStarter)
        bindSwitchView(binding.AutoUpdateApp, prefManager.autoUpdateApp)
//        bindSwitchView(binding.TimeCorrect, prefManager.timeCorrect)
//        bindSwitchView(binding.DebugMode, prefManager.debugMode)
        bindSwitchView(binding.ShowHomeAnnounce, prefManager.showHomeAnnounce)

//        binding.OverlayPermission.setUp(OverlayPermission)
//        binding.OpenAutoStartSettings.setUp(OpenAutoStartSettings)
//        binding.PowerSavingMode.setUp(PowerSavingMode, prefManager.powerSavingMode, null, null)
//        binding.BackAutoAtd.setUp(
//            BackAutoAtd,
//            prefManager.backAutoAtd,
//            { APP.setDailyAlarm() },
//            { APP.cancelDailyAlarm() }
//        )
//
//        binding.OverlayPermission.Layout.setOnClickListener {
//            (activity as MainActivity?)?.jumpToOverlayPermission()
//        }
//        binding.OpenAutoStartSettings.Layout.setOnClickListener {
//            openAutoStartSettings(requireContext())
//        }
//        binding.TurnOffBatteryOptimization.Layout.setOnClickListener {
//            (activity as MainActivity?)?.requestIgnoreBatteryOptimizations()
//        }
//        binding.WidgetRefresh.Layout.setOnClickListener {
//            val intent = Intent(APP, WidgetReceiver::class.java)
//            intent.action = WidgetReceiver.MANUAL_UPDATE
//            intent.putExtra("msg", "组件刷新中...")
//            APP.sendBroadcast(intent)
//        }
//        binding.About.setOnClickListener {
//            startActivity(Intent(requireContext(), AboutActivity::class.java))
//        }

//        timePickerBinding()

        binding.UseInnerWeb.setUp(UseInnerWeb, prefManager.useInnerWeb, null, null)
        binding.AppTheme.setUp(AppTheme, prefManager.appTheme) {
            Toaster.show("重启应用生效")
//            requireActivity().recreate()
        }



        binding.CheckUpdate.setUp(CheckUpdate)
        binding.GroupChat.setUp(GroupChat)

        binding.CheckUpdate.Layout.setOnClickListener {
            CoroutineScope(Dispatchers.IO).launch {
                val info = AppUpdateInfo.remoteInfo()
                getAppVersionName(APP).let {
                    if (it < info.version.toString())
                        Handler(Looper.getMainLooper()).post {
                            MaterialAlertDialogBuilder(APP)
                                .setTitle(info.version.toString())
                                .setMessage(info.content)
                                .setNegativeButton(R.string.cancel, null)
                                .setPositiveButton(getString(R.string.download)) { _, _ ->
                                    try {
                                        startActivity(
                                            Intent(
                                                Intent.ACTION_VIEW,
                                                Uri.parse(info.link)
                                            )
                                        )
                                    } catch (e: Exception) {
                                        Toaster.show(getString(R.string.illegal_url))
                                        e.printStackTrace()
                                    }
                                }.show()
                        }
                    else Toaster.show(getString(R.string.newest_update))
                }
            }
        }
        binding.OpenSourceLicense.setOnClickListener {
            val textView = TextView(requireContext()).apply {
                setPadding(80, 80, 80, 80) // 设置padding
            }
            val markwon = Markwon.create(requireContext())
            markwon.setMarkdown(textView, getString(R.string.open_license_content))
            MaterialAlertDialogBuilder(requireContext())
                .setView(textView)
                .show()
        }

        binding.GroupChat.Layout.setOnClickListener {
            val groupId = "924153470"
            try{
                val url = "mqqapi://card/show_pslcard?src_type=internal&card_type=group&uin=$groupId"
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                intent.setPackage("com.tencent.mobileqq")
                startActivity(intent)
            }catch (e:Exception){
                copyToClipboard(requireContext(), groupId)
                Toaster.show(getString(R.string.copied_qq_group_num))
            }
        }
        binding.FeedBack.setOnClickListener {
            MenuDialog(requireContext()).add("github") {
                val github = "https://github.com/blueskybone/ArkScreen/issues"
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(github)))
            }.add("bilibili") {
                val biliUid = "13957147"
                try {
                    val url = "bilibili://space/$biliUid"
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                    intent.setPackage("tv.danmaku.bili")
                    startActivity(intent)
                } catch (e: java.lang.Exception) {
                    val bilibili = "https://space.bilibili.com/$biliUid"
                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(bilibili)))
                }
            }.add("QQ") {
                try {
                    val qqNumber = "1980463469"
                    val url = "mqqapi://card/show_pslcard?src_type=internal&version=1&uin=$qqNumber"
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                    intent.setPackage("com.tencent.mobileqq")
                    startActivity(intent)
                } catch (e: java.lang.Exception) {
                    Toaster.show(getString(R.string.qq_not_install))
                }
            }.show()
        }
        binding.Manual.setOnClickListener {
            val cvId = "40623349"
            try {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("bilibili://article/$cvId"))
                startActivity(intent)
            } catch (e: Exception) {
                val intent =
                    Intent(Intent.ACTION_VIEW, Uri.parse("https://www.bilibili.com/read/cv$cvId"))
                startActivity(intent)
            }
        }
        binding.Donate.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.donate)
                .setMessage(R.string.donate_msg)
                .setNegativeButton(R.string.cancel, null)
                .setNeutralButton(R.string.donated) { _, _ -> Toaster.show(getString(R.string.thank_for_donate)) }
                .setPositiveButton(R.string.save_code) { _, _ ->
                    saveDrawableToGallery(requireContext(), R.drawable.wechat)
                    saveDrawableToGallery(requireContext(), R.drawable.zfb)
                    Toaster.show("已保存到本地")
                }.show()
        }
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

//    private fun timePickerBinding() {
//        binding.SetAtdTime.Title.setText(SetAtdTime.title)
//        val hour = prefManager.alarmAtdHour.get()
//        val min = prefManager.alarmAtdMin.get()
//        binding.SetAtdTime.Value.text =
//            getString(R.string.auto_attendance_time, getDigitalString(hour, min))
//        binding.SetAtdTime.Layout.setOnClickListener {
//            val dialogBinding = DialogTimepickerBinding.inflate(layoutInflater).apply {
//                TimePicker.hour = prefManager.alarmAtdHour.get()
//                TimePicker.minute = prefManager.alarmAtdMin.get()
//            }
//            MaterialAlertDialogBuilder(requireContext())
//                .setView(dialogBinding.root)
//                .setTitle(R.string.set_auto_attendance_time)
//                .setNegativeButton(R.string.cancel, null)
//                .setPositiveButton(R.string.confirm) { _, _ ->
//                    val newHour = dialogBinding.TimePicker.hour
//                    val newMin = dialogBinding.TimePicker.minute
//                    prefManager.alarmAtdHour.set(newHour)
//                    prefManager.alarmAtdMin.set(newMin)
//                    binding.SetAtdTime.Value.text =
//                        getString(R.string.auto_attendance_time, getDigitalString(newHour, newMin))
//                    APP.cancelDailyAlarm()
//                    if (prefManager.backAutoAtd.get()) APP.setDailyAlarm()
//                }.show()
//        }
//    }

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