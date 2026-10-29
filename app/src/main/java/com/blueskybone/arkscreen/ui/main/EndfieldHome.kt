package com.blueskybone.arkscreen.ui.main

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.widget.ImageViewCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.blueskybone.arkscreen.R
import com.blueskybone.arkscreen.data.local.pref.SettingPrefManager
import com.blueskybone.arkscreen.databinding.ChipRoundBinding
import com.blueskybone.arkscreen.databinding.FragmentEndfieldHomeBinding
import com.blueskybone.arkscreen.databinding.HomeCoreTileBinding
import com.blueskybone.arkscreen.platform.schedule.AttendanceWorkScheduler
import com.blueskybone.arkscreen.ui.account.AccountMngActivity
import com.blueskybone.arkscreen.ui.common.bindinginfo.AccountManager
import com.blueskybone.arkscreen.ui.common.bindinginfo.Attendance
import com.blueskybone.arkscreen.ui.common.bindinginfo.FuncChipInfo
import com.blueskybone.arkscreen.ui.common.bindinginfo.GachaStat
import com.blueskybone.arkscreen.ui.common.bindinginfo.GameStarter
import com.blueskybone.arkscreen.ui.common.bindinginfo.OpeAssets
import com.blueskybone.arkscreen.ui.main.common.HomeBannerController
import com.blueskybone.arkscreen.util.launchApp
import com.hjq.toast.Toaster
import com.google.android.material.color.MaterialColors
import kotlinx.coroutines.launch
import org.koin.android.ext.android.getKoin
import org.koin.androidx.viewmodel.ext.android.activityViewModel

/** 终末地首页入口；详情和数据源接入后再为三张卡片添加内容与跳转。 */
class EndfieldHome : Fragment() {
    private val prefManager: SettingPrefManager by getKoin().inject()
    private val viewModel: MainModel by activityViewModel()
    private var _binding: FragmentEndfieldHomeBinding? = null
    private val binding get() = requireNotNull(_binding)
    private lateinit var bannerController: HomeBannerController
    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (!granted) {
            Toaster.show(getString(R.string.notification_permission_denied_attendance_continues))
        }
        enqueueManualAttendance()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentEndfieldHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        bannerController = HomeBannerController(this, binding.TitleBanner, binding.BannerIdc)
        binding.GameSelector.text = getString(
            R.string.game_selector_label,
            getString(R.string.game_endfield),
        )
        binding.GameSelector.setOnClickListener {
            (requireActivity() as MainActivity).showGameSelector()
        }
        binding.CoreAssets.setup(OpeAssets)
        binding.CoreGacha.setup(GachaStat)
        binding.GameStarter.setup(GameStarter)
        binding.Attendance.setup(Attendance)
        binding.AccountManager.setup(AccountManager)
        binding.GameStarter.Layout.setOnClickListener {
            requireContext().launchApp("com.hypergryph.endfield") {
                Toaster.show(getString(R.string.game_not_found))
            }
        }
        binding.Attendance.Layout.setOnClickListener { startManualAttendance() }
        binding.AccountManager.Layout.setOnClickListener {
            startActivity(Intent(requireContext(), AccountMngActivity::class.java))
        }
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    val videos = state.endfieldBiliVideos
                    binding.BannerCard.visibility = if (videos.isEmpty()) View.GONE else View.VISIBLE
                    bannerController.submitList(videos)
                    binding.Announce.text = state.announce
                    val showAnnounce = prefManager.showHomeAnnounce.get()
                    binding.AnnounceTitle.visibility = if (showAnnounce) View.VISIBLE else View.GONE
                    binding.AnnounceCard.visibility = if (showAnnounce) View.VISIBLE else View.GONE
                }
            }
        }
        viewModel.fetchBiliVideos(HomeGame.ENDFIELD)
    }

    private fun startManualAttendance() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.POST_NOTIFICATIONS,
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            return
        }
        enqueueManualAttendance()
    }

    private fun enqueueManualAttendance() {
        AttendanceWorkScheduler.enqueue(requireContext(), allowRepeatToday = true)
        Toaster.show(getString(R.string.attendance_started))
    }

    private fun ChipRoundBinding.setup(info: FuncChipInfo) {
        Icon.setImageResource(info.icon)
        ImageViewCompat.setImageTintList(
            Icon,
            ContextCompat.getColorStateList(requireContext(), info.colors.icon),
        )
        ViewCompat.setBackgroundTintList(
            Icon,
            ContextCompat.getColorStateList(requireContext(), info.colors.container),
        )
        Title.setText(info.title)
    }

    private fun HomeCoreTileBinding.setup(info: FuncChipInfo) {
        Icon.setImageResource(info.icon)
        ImageViewCompat.setImageTintList(
            Icon,
            android.content.res.ColorStateList.valueOf(
                MaterialColors.getColor(Icon, com.google.android.material.R.attr.colorPrimary),
            ),
        )
        ViewCompat.setBackgroundTintList(
            Icon,
            ContextCompat.getColorStateList(requireContext(), R.color.sec_con),
        )
        Title.setText(info.title)
        Subtitle.setText(R.string.endfield_entry_pending)
        Layout.isClickable = false
        Layout.isFocusable = false
    }

    override fun onDestroyView() {
        bannerController.release()
        _binding = null
        super.onDestroyView()
    }
}
