package com.blueskybone.arkscreen.ui.main

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.blueskybone.arkscreen.R
import com.blueskybone.arkscreen.data.local.pref.SettingPrefManager
import com.blueskybone.arkscreen.databinding.FragmentSettingBinding
import com.blueskybone.arkscreen.platform.theme.AppThemeController
import com.blueskybone.arkscreen.domain.service.ServerTimeCalibrator
import com.blueskybone.arkscreen.ui.common.LogManagerActivity
import com.blueskybone.arkscreen.ui.common.bindinginfo.AppTheme
import com.blueskybone.arkscreen.ui.common.bindinginfo.TimeCorrection
import com.blueskybone.arkscreen.ui.main.common.PreferenceBinder
import com.hjq.toast.Toaster
import kotlinx.coroutines.launch
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
        styleSettingRows()
        return binding.root
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    private fun styleSettingRows() {
        fun dp(value: Int) = (value * resources.displayMetrics.density).toInt()
        val primary = androidx.core.content.ContextCompat.getColor(requireContext(), R.color.function_text_primary)
        val secondary = androidx.core.content.ContextCompat.getColor(requireContext(), R.color.function_text_secondary)
        val rows = listOf(
            binding.AppTheme.root, binding.ShowHomeAnnounce.root, binding.AutoUpdateApp.root,
            binding.UseInnerWeb.root, binding.TimeCorrect.root, binding.CheckLogs.root, binding.AboutEntry.root,
        )
        rows.forEach { row ->
            val detail = row === binding.TimeCorrect.root
            val hasSwitch = row.findViewById<View>(R.id.Switch) != null
            if (!detail) row.minimumHeight = dp(56)
            val verticalPadding = if (detail) 2 else if (hasSwitch) 4 else 10
            row.setPaddingRelative(dp(20), dp(verticalPadding), dp(20), dp(verticalPadding))
            row.background = null
            (row as? android.widget.LinearLayout)?.gravity = android.view.Gravity.CENTER_VERTICAL
            row.findViewById<android.widget.ImageView>(R.id.Icon)?.apply {
                layoutParams = layoutParams.apply { width = dp(20); height = dp(20) }
                imageTintList = android.content.res.ColorStateList.valueOf(secondary)
            }
            row.findViewById<android.widget.TextView>(R.id.Title)?.apply {
                textSize = 16f
                typeface = android.graphics.Typeface.create("sans-serif", android.graphics.Typeface.NORMAL)
                setTextColor(primary)
            }
            row.findViewById<android.widget.TextView>(R.id.Value)?.apply {
                textSize = if (detail) 13f else 14f
                setTextColor(secondary)
            }
        }
        (binding.AboutEntry.root as ViewGroup).let { row ->
            (row.getChildAt(row.childCount - 1) as? android.widget.ImageView)?.apply {
                setImageResource(R.drawable.ic_function_chevron)
                imageTintList = android.content.res.ColorStateList.valueOf(secondary)
            }
        }
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

        PreferenceBinder.bindSwitch(
            binding = binding.UseInnerWeb,
            icon = R.drawable.ic_link,
            text = R.string.use_inner_web,
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

        PreferenceBinder.bindPreferenceValue(
            binding = binding.AppTheme,
            context = requireContext(),
            icon = R.drawable.ic_theme_mode,
            listInfo = AppTheme,
            pref = prefManager.appTheme,
            onClick = appThemeController::applySavedTheme
        )

        PreferenceBinder.bindPreferenceText(
            binding = binding.CheckLogs,
            icon = R.drawable.ic_log,
            text = R.string.log_manager
        )

        binding.AboutEntry.Title.setText(R.string.about_page_title)
        binding.AboutEntry.Icon.setImageResource(R.drawable.ic_info)
        binding.AboutEntry.Layout.setOnClickListener {
            if (parentFragmentManager.findFragmentByTag("about_page") == null) {
                AboutFragment().show(parentFragmentManager, "about_page")
            }
        }
        binding.CheckLogs.Layout.setOnClickListener {
            startActivity(Intent(requireContext(), LogManagerActivity::class.java))
        }

    }

    private fun recordTimeCorrect() {
        viewLifecycleOwner.lifecycleScope.launch {
            serverTimeCalibrator.calibrate()
                .onSuccess { offsetSeconds ->
                    val signedOffset = if (offsetSeconds >= 0) "+$offsetSeconds" else "$offsetSeconds"
                    Toaster.show(getString(R.string.time_calibration_completed, signedOffset))
                }
                .onFailure { error ->
                    prefManager.timeCorrect.set(false)
                    binding.TimeCorrect.Switch.isChecked = false
                    Toaster.show(error.message ?: getString(R.string.time_calibration_failed))
                    Timber.w("Server time calibration failed: %s", error.message)
                }
        }
    }

}
