package com.blueskybone.arkscreen.ui.fragment

import android.annotation.SuppressLint
import android.app.Activity
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
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.SwitchCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.blueskybone.arkscreen.APP
import com.blueskybone.arkscreen.R
import com.blueskybone.arkscreen.common.MenuDialog
import com.blueskybone.arkscreen.databinding.DialogInputBinding
import com.blueskybone.arkscreen.databinding.DialogTimepickerBinding
import com.blueskybone.arkscreen.databinding.FragmentDashboardBinding
import com.blueskybone.arkscreen.databinding.PreferenceBinding
import com.blueskybone.arkscreen.databinding.PreferenceSeekbarBinding
import com.blueskybone.arkscreen.databinding.PreferenceSwitchBinding
import com.blueskybone.arkscreen.preference.PrefManager
import com.blueskybone.arkscreen.preference.preference.Preference
import com.blueskybone.arkscreen.room.Account
import com.blueskybone.arkscreen.ui.activity.LoginWeb
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
import com.blueskybone.arkscreen.ui.bindinginfo.ScreenshotDelay
import com.blueskybone.arkscreen.ui.bindinginfo.SeekBarInfo
import com.blueskybone.arkscreen.ui.bindinginfo.SetAtdTime
import com.blueskybone.arkscreen.ui.bindinginfo.TextInfo
import com.blueskybone.arkscreen.ui.bindinginfo.TurnOffBatteryOptimization
import com.blueskybone.arkscreen.ui.bindinginfo.WidgetUpdateFreq
import com.blueskybone.arkscreen.ui.recyclerview.AccountAdapter
import com.blueskybone.arkscreen.ui.recyclerview.ItemListener
import com.blueskybone.arkscreen.util.TimeUtils
import com.blueskybone.arkscreen.util.copyToClipboard
import com.blueskybone.arkscreen.viewmodel.BaseModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.hjq.toast.Toaster
import org.koin.android.ext.android.getKoin
import java.util.Locale

/**
 *   Created by blueskybone
 *   Date: 2024/12/31
 */
class Function : Fragment(), ItemListener {
    private val model: BaseModel by activityViewModels()
    private var _binding: FragmentDashboardBinding? = null
    private val prefManager: PrefManager by getKoin().inject()
    private var adapter: AccountAdapter? = null
    private lateinit var activityResultLauncher: ActivityResultLauncher<Intent>

    private val binding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)
        _binding = FragmentDashboardBinding.inflate(inflater)
        initialize()
        setUpBinding()
        return binding.root
    }

    private fun initialize() {
        adapter = AccountAdapter(requireContext(), this)
        binding.RecyclerView.adapter = adapter
        model.accountSkList.observe(viewLifecycleOwner) { value ->
            adapter?.submitList(value as List<Account>?)
        }
        activityResultLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            // 处理返回结果
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                // 解析返回的数据
                val token = data?.getStringExtra("token")
                val dId = data?.getStringExtra("dId")
                if (token != null && dId != null) {
                    Toaster.show(getString(R.string.getting_info))
                    model.accountSkLogin(token, dId)
                } else {
                    Toaster.show("null")
                }
            }
        }
    }

    private fun setUpBinding() {

        binding.AddAccount.setOnClickListener {
            MenuDialog(requireContext())
                .add(getString(R.string.import_cookie)) {
                    displayLoginDialog()
                }
                .add(R.string.web_login) {
                    val intent =
                        LoginWeb.startIntent(requireContext(), LoginWeb.Companion.LoginType.SKLAND)
                    activityResultLauncher.launch(intent)
                }
                .show()
        }

        bindSwitchView(binding.AutoAttendance, prefManager.autoAttendance)
        binding.RecruitMode.setUp(RecruitMode, prefManager.recruitMode, null)
        binding.FloatWindowAppearance.setUp(
            FloatWindowAppearance,
            prefManager.floatWindowAppearance, null
        )

        binding.ScreenShotDelay.setUp(ScreenshotDelay, prefManager.screenShotDelay)
        binding.TurnOffBatteryOptimization.setUp(TurnOffBatteryOptimization)
        binding.WidgetAppearance.setOnClickListener {
            startActivity(Intent(requireContext(), WidgetThemeActivity::class.java))
        }
//        binding.WidgetAlpha.setUp(WidgetAlpha, prefManager.widgetAlpha)
//        binding.WidgetContentSize.setUp(WidgetSize, prefManager.widgetContentSize, null)
        binding.WidgetRefresh.setUp(WidgetUpdateFreq, prefManager.widgetUpdateFreq, null)

        binding.OverlayPermission.setUp(OverlayPermission)
        binding.NotifyPermission.setUp(NotifyPermission)
        binding.PowerSavingMode.setUp(PowerSavingMode, prefManager.powerSavingMode, null, null)
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

        binding.TurnOffBatteryOptimization.Layout.setOnClickListener {
            (activity as MainActivity?)?.requestIgnoreBatteryOptimizations()
        }
//        binding.WidgetRefresh.Layout.setOnClickListener {
//            val intent = Intent(APP, WidgetReceiver::class.java)
//            intent.action = WidgetReceiver.MANUAL_UPDATE
//            intent.putExtra("msg", "组件刷新中...")
//            APP.sendBroadcast(intent)
//        }
        binding.BackAutoAtd.setUp(
            BackAutoAtd,
            prefManager.backAutoAtd,
            { APP.setDailyAlarm() },
            { APP.cancelDailyAlarm() }
        )
        binding.OpenAutoStartSettings.Layout.setOnClickListener {
            openAutoStartSettings(requireContext())
        }
        binding.OverlayPermission.Layout.setOnClickListener {
            (activity as MainActivity?)?.jumpToPermission(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
        }
        binding.NotifyPermission.Layout.setOnClickListener {
            (activity as MainActivity?)?.openNotificationSettings(requireContext())
        }
        timePickerBinding()
    }

    private fun displayLoginDialog() {
        val dialogBinding = DialogInputBinding.inflate(layoutInflater)
        dialogBinding.EditText2.visibility = View.GONE
        dialogBinding.EditText1.hint = getString(R.string.import_cookie)
        MaterialAlertDialogBuilder(requireContext())
            .setView(dialogBinding.root)
            .setTitle(R.string.import_cookie)
            .setNegativeButton(R.string.cancel, null)
            .setPositiveButton(R.string.import_cookie) { _, _ ->
                val str = dialogBinding.EditText1.text.toString()
                val list = str.split("@")
                if (list.size == 2) {
                    try {
                        Toaster.show(getString(R.string.getting_info))
                        model.accountSkLogin(list[0], list[1])
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                } else {
                    Toaster.show(getString(R.string.wrong_format))
                }
            }.show()
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

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    private fun timePickerBinding() {
        binding.SetAtdTime.Title.setText(SetAtdTime.title)
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
                .setTitle(R.string.set_auto_attendance_time)
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

    @SuppressLint("NotifyDataSetChanged")
    //TODO: 强制刷新影响性能，看能不能改数据结构，用Observe的方法改变视图
    override fun onClick(position: Int) {
        adapter?.currentList?.get(position)?.let { value ->
            model.setDefaultAccountSk(value as com.blueskybone.arkscreen.room.AccountSk)
            Toaster.show(getString(R.string.set_default_account, value.nickName))
            adapter?.notifyDataSetChanged()
        }
    }

    override fun onLongClick(position: Int) {
        adapter?.currentList?.get(position)?.let { value ->
            MenuDialog(requireContext())
                .add(getString(R.string.export_cookie)) {
                    displayExportDialog("${value.token}@${(value as com.blueskybone.arkscreen.room.AccountSk).dId}")
                }
                .add(R.string.delete) { confirmDeletion(value as com.blueskybone.arkscreen.room.AccountSk) }
                .show()
        }
    }

    private fun displayExportDialog(key: String) {
        val dialogBinding = DialogInputBinding.inflate(layoutInflater)
        dialogBinding.EditText2.visibility = View.GONE
        dialogBinding.EditText1.setText(key)
        MaterialAlertDialogBuilder(requireContext())
            .setView(dialogBinding.root)
            .setTitle(R.string.export_cookie)
            .setNegativeButton(R.string.cancel, null)
            .setPositiveButton(R.string.copy) { _, _ ->
                copyToClipboard(requireContext(), key)
            }.show()
    }

    private fun confirmDeletion(value: com.blueskybone.arkscreen.room.AccountSk) {
        MaterialAlertDialogBuilder(requireContext())
            .setMessage(R.string.confirm_delete)
            .setPositiveButton(R.string.delete) { _, _ -> model.deleteAccountSk(value) }
            .setNegativeButton(R.string.cancel, null)
            .show()
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