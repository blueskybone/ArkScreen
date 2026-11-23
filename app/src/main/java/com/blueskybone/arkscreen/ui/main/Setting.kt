package com.blueskybone.arkscreen.ui.main

import android.content.Intent
import com.google.android.material.dialog.MaterialAlertDialogBuilder
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

        binding.CheckLogs.Title.setText(R.string.log_manager)
        binding.CheckLogs.Icon.setImageResource(R.drawable.ic_log)

        binding.DataInfo.Title.setText(R.string.about_data_info)
        binding.DataInfo.Icon.setImageResource(R.drawable.ic_function_permission)
        binding.DataInfo.Layout.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.about_data_info)
                .setMessage(R.string.about_data_description)
                .setPositiveButton(R.string.confirm, null).show()
        }

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
