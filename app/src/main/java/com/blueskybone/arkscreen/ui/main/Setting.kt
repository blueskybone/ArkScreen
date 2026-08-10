package com.blueskybone.arkscreen.ui.main

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.blueskybone.arkscreen.R
import com.blueskybone.arkscreen.core.logger.FileLoggingInterceptor
import com.blueskybone.arkscreen.core.logger.FileLoggingTree
import com.blueskybone.arkscreen.data.local.pref.PrefManager
import com.blueskybone.arkscreen.databinding.DialogDonateBinding
import com.blueskybone.arkscreen.databinding.FragmentSettingBinding
import com.blueskybone.arkscreen.ui.common.LogActivity
import com.blueskybone.arkscreen.ui.common.bindinginfo.AppTheme
import com.blueskybone.arkscreen.ui.common.bindinginfo.CheckUpdate
import com.blueskybone.arkscreen.ui.common.bindinginfo.GroupChat
import com.blueskybone.arkscreen.ui.common.bindinginfo.TimeCorrection
import com.blueskybone.arkscreen.ui.common.bindinginfo.UseInnerWeb
import com.blueskybone.arkscreen.ui.common.view.MenuDialog
import com.blueskybone.arkscreen.ui.main.common.PreferenceBinder
import com.blueskybone.arkscreen.util.copyToClipboard
import com.blueskybone.arkscreen.util.getScreenInfo
import com.blueskybone.arkscreen.util.saveDrawableToGallery
import com.github.mikephil.charting.BuildConfig
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.hjq.toast.Toaster
import io.noties.markwon.Markwon
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.android.ext.android.getKoin
import timber.log.Timber
import java.io.File

/**
 *   Created by blueskybone
 *   Date: 2025/1/4
 */
class Setting : Fragment() {
    private var _binding: FragmentSettingBinding? = null
    private val model: MainModel by activityViewModels()
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
            onClick = {
                Toaster.show("重启应用生效")
            }
        )

        PreferenceBinder.bindPreferenceText(
            binding = binding.CheckUpdate,
            icon = R.drawable.ic_update,
            textInfo = CheckUpdate
        )
        binding.CheckUpdate.Value.text = BuildConfig.VERSION_NAME

        PreferenceBinder.bindPreferenceText(
            binding = binding.GroupChat,
            icon = R.drawable.ic_group,
            textInfo = GroupChat
        )

        binding.CheckUpdate.Layout.setOnClickListener {
            model.checkAppUpdate()
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

        binding.CheckLogs.setOnClickListener {
            checkScreenInfo(requireContext())
            val combinedFiles = mutableListOf<File>()
            FileLoggingTree.logDir.listFiles()?.let {
                combinedFiles.addAll(it)
            }
            FileLoggingInterceptor.logDir.listFiles()?.let {
                combinedFiles.addAll(it)
            }
            val menuDialog = MenuDialog(requireContext())
            for (file in combinedFiles) {
                menuDialog.add(file.name) {
                    val intent = Intent(requireContext(), LogActivity::class.java)
                    intent.putExtra("log_filepath", file.absoluteFile.toString())
                    startActivity(intent)
                }
            }
            menuDialog.show()
        }

        binding.CleanLogs.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setMessage(R.string.confirm_delete)
                .setPositiveButton(R.string.delete) { _, _ ->
                    FileLoggingTree.logDir.listFiles()?.forEach { file ->
                        file.delete()
                    }
                    FileLoggingInterceptor.logDir.listFiles()?.forEach { file ->
                        file.delete()
                    }
                    Toaster.show("已清除日志")
                }
                .setNegativeButton(R.string.cancel, null)
                .show()
        }

        binding.FeedBack.setOnClickListener {
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
        binding.Donate.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.donate)
                .setView(DialogDonateBinding.inflate(layoutInflater).root)
                .setNegativeButton(R.string.cancel, null)
                .setNeutralButton(R.string.donated) { _, _ -> Toaster.show(getString(R.string.thank_for_donate)) }
                .setPositiveButton(R.string.save_code) { _, _ ->
                    CoroutineScope(Dispatchers.IO).launch {
                        saveDrawableToGallery(requireContext(), R.drawable.wechat)
                        saveDrawableToGallery(requireContext(), R.drawable.zfb)
                        Toaster.show("已保存到本地")
                    }
                }.show()
        }
    }

    private fun recordTimeCorrect() {
//        CoroutineScope(Dispatchers.IO).launch {
//            try {
//                val subTs = getSklandServerTs() - System.currentTimeMillis() / 1000
//                prefManager.timeCorrectSec.set(subTs)
//                Toaster.show("delay: $subTs s")
//            } catch (e: Exception) {
//                Toaster.show(e.message)
//                Timber.e(e.printStackTrace().toString())
//            }
//        }
    }

    private fun checkScreenInfo(context: Context) {
        Timber.i(getScreenInfo(context))
    }
}