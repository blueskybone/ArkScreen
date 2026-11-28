package com.blueskybone.arkscreen.ui.main

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import android.util.DisplayMetrics
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import com.blueskybone.arkscreen.R
import com.blueskybone.arkscreen.data.local.pref.SettingPrefManager
import com.blueskybone.arkscreen.databinding.DialogDonateBinding
import com.blueskybone.arkscreen.databinding.FragmentAboutBinding
import com.blueskybone.arkscreen.domain.model.AppVersion
import com.blueskybone.arkscreen.platform.theme.AppThemeController
import com.blueskybone.arkscreen.domain.service.ServerTimeCalibrator
import com.blueskybone.arkscreen.ui.common.LogManagerActivity
import com.blueskybone.arkscreen.ui.license.OpenSourceLicensesActivity
import com.blueskybone.arkscreen.ui.common.bindinginfo.TimeCorrection
import com.blueskybone.arkscreen.ui.common.view.MenuDialog
import com.blueskybone.arkscreen.ui.main.common.PreferenceBinder
import com.blueskybone.arkscreen.util.copyToClipboard
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.hjq.toast.Toaster
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.getKoin
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import timber.log.Timber

/** Full-screen about page shares the host update flow instead of duplicating downloads. */
class AboutFragment : DialogFragment() {
    private var _binding: FragmentAboutBinding? = null
    private val binding get() = _binding!!
    private val model: MainViewModel by activityViewModel()
    private val appVersion: AppVersion by getKoin().inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.AppTheme_About)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAboutBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.apply {
            setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            setBackgroundDrawableResource(R.color.app_background)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.Toolbar.setNavigationOnClickListener { dismiss() }
        binding.Version.text = appVersion.name
        setUpBinding()
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    private fun buildDeviceInfo(): String {
        val activity = requireActivity()
        val metrics = resources.displayMetrics
        val configuration = resources.configuration
        val windowManager = activity.windowManager
        val decorView = activity.window.decorView
        val screenWidth: Int
        val screenHeight: Int
        val windowWidth: Int
        val windowHeight: Int
        val insets: WindowInsetsCompat?
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val screenBounds = windowManager.maximumWindowMetrics.bounds
            val windowMetrics = windowManager.currentWindowMetrics
            screenWidth = screenBounds.width()
            screenHeight = screenBounds.height()
            windowWidth = windowMetrics.bounds.width()
            windowHeight = windowMetrics.bounds.height()
            insets = WindowInsetsCompat.toWindowInsetsCompat(windowMetrics.windowInsets)
        } else {
            val screenMetrics = DisplayMetrics()
            @Suppress("DEPRECATION")
            windowManager.defaultDisplay.getRealMetrics(screenMetrics)
            screenWidth = screenMetrics.widthPixels
            screenHeight = screenMetrics.heightPixels
            windowWidth = decorView.width
            windowHeight = decorView.height
            insets = ViewCompat.getRootWindowInsets(decorView)
        }
        fun insetInfo(type: Int): String {
            val edges = insets?.getInsetsIgnoringVisibility(type)
                ?: return getString(R.string.about_diagnostics_unavailable)
            return getString(
                R.string.about_diagnostics_inset_edges,
                edges.left, edges.top, edges.right, edges.bottom,
            )
        }
        val orientation = when (configuration.orientation) {
            Configuration.ORIENTATION_LANDSCAPE -> R.string.about_diagnostics_landscape
            Configuration.ORIENTATION_PORTRAIT -> R.string.about_diagnostics_portrait
            else -> R.string.about_diagnostics_unavailable
        }
        return listOf(
            "ArkScreen ${appVersion.name} (${appVersion.versionCode})",
            "Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})",
            "${Build.MANUFACTURER} ${Build.MODEL}",
            getString(R.string.about_diagnostics_screen, screenWidth, screenHeight),
            getString(R.string.about_diagnostics_window, windowWidth, windowHeight,
                windowWidth / metrics.density, windowHeight / metrics.density),
            getString(R.string.about_diagnostics_density, metrics.densityDpi),
            getString(R.string.about_diagnostics_font_scale, configuration.fontScale),
            getString(R.string.about_diagnostics_orientation, getString(orientation)),
            getString(R.string.about_diagnostics_status_bar, insetInfo(WindowInsetsCompat.Type.statusBars())),
            getString(R.string.about_diagnostics_navigation_bar, insetInfo(WindowInsetsCompat.Type.navigationBars())),
            getString(R.string.about_diagnostics_cutout, insetInfo(WindowInsetsCompat.Type.displayCutout())),
        ).joinToString("\n")
    }

    private fun openUrl(url: String) {
        runCatching { startActivity(Intent(Intent.ACTION_VIEW, url.toUri())) }
            .onFailure { Toaster.show(getString(R.string.about_open_failed)) }
    }

    private fun setUpBinding() {
        val entries = listOf(
            Triple(binding.Manual, R.string.manual, R.drawable.ic_manual_book),
            Triple(binding.FeedBack, R.string.send_feedback, R.drawable.ic_feedback),
            Triple(binding.GroupChat, R.string.about_community, R.drawable.ic_group),
            Triple(binding.Diagnostics, R.string.about_diagnostics, R.drawable.ic_function_result),
            Triple(binding.Source, R.string.about_source, R.drawable.ic_link),
            Triple(binding.OpenSourceLicense, R.string.open_license, R.drawable.ic_license),
            Triple(binding.Donate, R.string.donate, R.drawable.ic_favorite),
        )
        val primary = androidx.core.content.ContextCompat.getColor(requireContext(), R.color.app_text_primary)
        val secondary = androidx.core.content.ContextCompat.getColorStateList(requireContext(), R.color.app_text_secondary)
        fun dp(n: Int) = (n * resources.displayMetrics.density).toInt()
        entries.forEach { (item, title, icon) ->
            item.Title.setText(title)
            item.Title.textSize = 16f
            item.Title.typeface = android.graphics.Typeface.DEFAULT
            item.Title.setTextColor(primary)
            item.Icon.setImageResource(icon)
            item.Icon.imageTintList = secondary
            item.Icon.layoutParams = item.Icon.layoutParams.apply { width = dp(20); height = dp(20) }
            item.root.minimumHeight = dp(56)
            item.root.setPaddingRelative(dp(20), dp(10), dp(20), dp(10))
            item.root.background = null
            (item.root as ViewGroup).let { row ->
                (row.getChildAt(row.childCount - 1) as? android.widget.ImageView)?.apply {
                    setImageResource(R.drawable.ic_function_chevron)
                    visibility = if (item == binding.Manual || item == binding.OpenSourceLicense) View.VISIBLE else View.GONE
                    imageTintList = secondary
                }
            }
        }
        binding.CheckUpdate.setOnClickListener { model.checkAppUpdate(showNoUpdate = true) }
        binding.Source.Layout.setOnClickListener { openUrl("https://github.com/blueskybone/ArkScreen") }
        binding.Diagnostics.Layout.setOnClickListener {
            val info = buildDeviceInfo()
            MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.about_diagnostics)
                .setMessage(getString(R.string.about_diagnostics_note) + "\n\n" + info)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.copy) { _, _ ->
                    copyToClipboard(requireContext(), info)
                    Toaster.show(getString(R.string.about_diagnostics_copied))
                }.show()
        }
        binding.OpenSourceLicense.Layout.setOnClickListener {
            startActivity(Intent(requireContext(), OpenSourceLicensesActivity::class.java))
        }

        binding.GroupChat.Layout.setOnClickListener {
            val groupId = model.uiState.value.appRemoteConfig.qqGroupId
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
        binding.Manual.Layout.setOnClickListener {
            val cvId = model.uiState.value.appRemoteConfig.manualCvId
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
                            saveDrawableToGallery(context, R.drawable.donation_qr_wechat)
                            saveDrawableToGallery(context, R.drawable.donation_qr_alipay)
                        }
                        Toaster.show(getString(R.string.saved_to_local))
                    }
                }.show()
        }
    }

}
