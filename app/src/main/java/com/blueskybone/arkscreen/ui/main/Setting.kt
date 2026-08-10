package com.blueskybone.arkscreen.ui.main

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.blueskybone.arkscreen.R
import com.blueskybone.arkscreen.data.local.pref.SettingPrefManager
import com.blueskybone.arkscreen.databinding.DialogDonateBinding
import com.blueskybone.arkscreen.databinding.FragmentSettingBinding
import com.blueskybone.arkscreen.platform.theme.AppThemeController
import com.blueskybone.arkscreen.domain.service.ServerTimeCalibrator
import com.blueskybone.arkscreen.ui.common.LogManagerActivity
import com.blueskybone.arkscreen.ui.license.OpenSourceLicensesActivity
import com.blueskybone.arkscreen.ui.common.bindinginfo.AppTheme
import com.blueskybone.arkscreen.ui.common.bindinginfo.CheckUpdate
import com.blueskybone.arkscreen.ui.common.bindinginfo.GroupChat
import com.blueskybone.arkscreen.ui.common.bindinginfo.TimeCorrection
import com.blueskybone.arkscreen.ui.common.bindinginfo.UseInnerWeb
import com.blueskybone.arkscreen.ui.common.view.MenuDialog
import com.blueskybone.arkscreen.ui.main.common.PreferenceBinder
import com.blueskybone.arkscreen.util.copyToClipboard
import com.github.mikephil.charting.BuildConfig
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.hjq.toast.Toaster
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.getKoin
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import timber.log.Timber

/**
 *   Created by blueskybone
 *   Date: 2025/1/4
 */
class Setting : Fragment() {
    private var _binding: FragmentSettingBinding? = null
    private val model: MainModel by activityViewModel()
    private val binding get() = _binding!!
    private val prefManager: SettingPrefManager by getKoin().inject()
    private val appThemeController: AppThemeController by getKoin().inject()
    private val serverTimeCalibrator: ServerTimeCalibrator by getKoin().inject()

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

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    private fun setUpBinding() {
        PreferenceBinder.bindSwitch(
            binding = binding.AutoUpdateApp,
            icon = R.drawable.ic_refresh,
            text = R.string.auto_check_update,
            pref = prefManager.autoUpdateApp
        )

        PreferenceBinder.bindSwitch(
            binding = binding.ShowHomeAnnounce,
            icon = R.drawable.ic_megaphone,
            text = R.string.show_home_announce,
            pref = prefManager.showHomeAnnounce
        )

        PreferenceBinder.bindSubSwitch(
            binding = binding.UseInnerWeb,
            icon = R.drawable.ic_link,
            textInfo = UseInnerWeb,
            pref = prefManager.useInnerWeb,
            onCall = null,
            offCall = null
        )

        PreferenceBinder.bindSubSwitch(
            binding = binding.TimeCorrect,
            icon = R.drawable.ic_delay,
            textInfo = TimeCorrection,
            pref = prefManager.timeCorrect,
            onCall = { recordTimeCorrect() },
            offCall = null
        )

        PreferenceBinder.bindPreference(
            binding = binding.AppTheme,
            context = requireContext(),
            icon = R.drawable.ic_palette,
            listInfo = AppTheme,
            pref = prefManager.appTheme,
            onClick = appThemeController::applySavedTheme
        )

        PreferenceBinder.bindPreferenceText(
            binding = binding.CheckUpdate,
            icon = R.drawable.ic_update,
            textInfo = CheckUpdate
        )

        PreferenceBinder.bindPreferenceText(
            binding = binding.GroupChat,
            icon = R.drawable.ic_group,
            textInfo = GroupChat
        )

        PreferenceBinder.bindPreferenceText(
            binding = binding.CheckLogs,
            icon = R.drawable.ic_log,
            text = R.string.log_manager
        )

        PreferenceBinder.bindPreferenceText(
            binding = binding.OpenSourceLicense,
            icon = R.drawable.ic_license,
            text = R.string.open_license
        )

        PreferenceBinder.bindPreferenceText(
            binding = binding.FeedBack,
            icon = R.drawable.ic_feedback,
            text = R.string.send_feedback
        )

        PreferenceBinder.bindPreferenceText(
            binding = binding.Donate,
            icon = R.drawable.ic_favorite,
            text = R.string.donate
        )

        binding.CheckUpdate.Layout.setOnClickListener {
            model.checkAppUpdate(showNoUpdate = true)
        }

        binding.OpenSourceLicense.Layout.setOnClickListener {
            startActivity(Intent(requireContext(), OpenSourceLicensesActivity::class.java))
        }

        binding.GroupChat.Layout.setOnClickListener {
            val groupId = "924153470"
            try {
                val url =
                    "mqqapi://card/show_pslcard?src_type=internal&card_type=group&uin=$groupId"
                val intent = Intent(Intent.ACTION_VIEW, url.toUri())
                intent.setPackage("com.tencent.mobileqq")
                startActivity(intent)
            } catch (e: Exception) {
                copyToClipboard(requireContext(), groupId)
                Toaster.show(getString(R.string.copied_qq_group_num))
            }
        }

        binding.CheckLogs.Layout.setOnClickListener {
            startActivity(Intent(requireContext(), LogManagerActivity::class.java))
        }

        binding.FeedBack.Layout.setOnClickListener {
            MenuDialog(requireContext()).add("github") {
                val github = "https://github.com/blueskybone/ArkScreen/issues"
                startActivity(Intent(Intent.ACTION_VIEW, github.toUri()))
            }.add("bilibili") {
                val biliUid = "13957147"
                try {
                    val url = "bilibili://space/$biliUid"
                    val intent = Intent(Intent.ACTION_VIEW, url.toUri())
                    intent.setPackage("tv.danmaku.bili")
                    startActivity(intent)
                } catch (e: java.lang.Exception) {
                    val bilibili = "https://space.bilibili.com/$biliUid"
                    startActivity(Intent(Intent.ACTION_VIEW, bilibili.toUri()))
                }
            }.add("QQ") {
                try {
                    val qqNumber = "1980463469"
                    val url = "mqqapi://card/show_pslcard?src_type=internal&version=1&uin=$qqNumber"
                    val intent = Intent(Intent.ACTION_VIEW, url.toUri())
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
                val intent = Intent(Intent.ACTION_VIEW, "bilibili://article/$cvId".toUri())
                startActivity(intent)
            } catch (e: Exception) {
                val intent =
                    Intent(Intent.ACTION_VIEW, "https://www.bilibili.com/read/cv$cvId".toUri())
                startActivity(intent)
            }
        }
        binding.Donate.Layout.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.donate)
                .setView(DialogDonateBinding.inflate(layoutInflater).root)
                .setNegativeButton(R.string.cancel, null)
                .setNeutralButton(R.string.donated) { _, _ -> Toaster.show(getString(R.string.thank_for_donate)) }
                .setPositiveButton(R.string.save_code) { _, _ ->
                    val context = requireContext().applicationContext
                    viewLifecycleOwner.lifecycleScope.launch {
                        withContext(Dispatchers.IO) {
                            saveDrawableToGallery(context, R.drawable.wechat)
                            saveDrawableToGallery(context, R.drawable.zfb)
                        }
                        Toaster.show("已保存到本地")
                    }
                }.show()
        }
    }

    private fun recordTimeCorrect() {
        viewLifecycleOwner.lifecycleScope.launch {
            serverTimeCalibrator.calibrate()
                .onSuccess { offsetSeconds ->
                    val signedOffset = if (offsetSeconds >= 0) "+$offsetSeconds" else "$offsetSeconds"
                    Toaster.show("时间校准完成：$signedOffset 秒")
                }
                .onFailure { error ->
                    prefManager.timeCorrect.set(false)
                    binding.TimeCorrect.Switch.isChecked = false
                    Toaster.show(error.message ?: "时间校准失败")
                    Timber.w("Server time calibration failed: %s", error.message)
                }
        }
    }

}
