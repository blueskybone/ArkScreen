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
import com.blueskybone.arkscreen.domain.model.account.Account
import com.blueskybone.arkscreen.domain.model.account.AccountEf
import com.blueskybone.arkscreen.databinding.ChipRoundBinding
import com.blueskybone.arkscreen.databinding.CardApCacheBinding
import com.blueskybone.arkscreen.databinding.FragmentEndfieldHomeBinding
import com.blueskybone.arkscreen.databinding.HomeCoreTileBinding
import com.blueskybone.arkscreen.platform.schedule.AttendanceWorkScheduler
import com.blueskybone.arkscreen.ui.account.AccountMngActivity
import com.blueskybone.arkscreen.ui.account.adapter.AccountAdapter
import com.blueskybone.arkscreen.ui.account.common.AccountPickerPopup
import com.blueskybone.arkscreen.ui.account.model.AccountItemAction
import com.blueskybone.arkscreen.ui.character.EndfieldCharsActivity
import com.blueskybone.arkscreen.ui.gacha.EndfieldGachaActivity
import com.blueskybone.arkscreen.ui.common.bindinginfo.AccountManager
import com.blueskybone.arkscreen.ui.common.bindinginfo.Attendance
import com.blueskybone.arkscreen.ui.common.bindinginfo.FuncChipInfo
import com.blueskybone.arkscreen.ui.common.bindinginfo.GachaStat
import com.blueskybone.arkscreen.ui.common.bindinginfo.GameStarter
import com.blueskybone.arkscreen.ui.common.bindinginfo.OpeAssets
import com.blueskybone.arkscreen.ui.main.common.HomeBannerController
import com.blueskybone.arkscreen.ui.realtime.EndfieldRealtimeActivity
import com.blueskybone.arkscreen.util.launchApp
import com.blueskybone.arkscreen.platform.time.TimeUtils.getLastUpdateStr
import com.blueskybone.arkscreen.platform.time.TimeUtils.getRemainTimeStr
import com.hjq.toast.Toaster
import kotlinx.coroutines.launch
import org.koin.android.ext.android.getKoin
import org.koin.androidx.viewmodel.ext.android.activityViewModel

/** 终末地首页入口；详情和数据源接入后再为三张卡片添加内容与跳转。 */
class EndfieldHome : Fragment() {
    private companion object { const val CACHE_STALE_SECONDS = 24 * 60 * 60L }
    private val prefManager: SettingPrefManager by getKoin().inject()
    private val viewModel: MainModel by activityViewModel()
    private var _binding: FragmentEndfieldHomeBinding? = null
    private val binding get() = requireNotNull(_binding)
    private lateinit var bannerController: HomeBannerController
    private lateinit var accountAdapter: AccountAdapter
    private var accountPopup: AccountPickerPopup? = null
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
        accountAdapter = AccountAdapter(object : AccountItemAction {
            override fun onClick(account: Account) {
                viewModel.setDefaultAccountEf(account as AccountEf)
                accountPopup?.dismiss()
                Toaster.show(getString(R.string.set_default_account, account.nickName))
            }

            override fun onLongClick(account: Account) = Unit
        })
        (requireActivity() as MainActivity).bindHomeGameControls(
            binding.GameSelector,
            binding.SwitchGame,
            binding.DefaultStartup,
        )
        binding.CurrentAccount.setOnClickListener { anchor ->
            if (viewModel.uiState.value.accountEfList.isEmpty()) {
                startActivity(Intent(requireContext(), AccountMngActivity::class.java))
            } else if (accountPopup?.isShowing != true) {
                accountPopup = AccountPickerPopup(requireActivity(), accountAdapter).also {
                    it.show(anchor)
                }
            }
        }
        binding.CoreAssets.setup(OpeAssets)
        binding.CoreAssets.Subtitle.setText(R.string.endfield_chars_entry)
        binding.CoreAssets.Layout.isClickable = true
        binding.CoreAssets.Layout.isFocusable = true
        binding.CoreAssets.Layout.setOnClickListener {
            startActivity(Intent(requireContext(), EndfieldCharsActivity::class.java))
        }
        binding.RealTimeData.setOnClickListener {
            startActivity(Intent(requireContext(), EndfieldRealtimeActivity::class.java))
        }
        binding.CoreGacha.setup(GachaStat)
        binding.CoreGacha.Subtitle.setText(R.string.endfield_gacha_title)
        binding.CoreGacha.Layout.isClickable = true
        binding.CoreGacha.Layout.isFocusable = true
        binding.CoreGacha.Layout.setOnClickListener {
            startActivity(Intent(requireContext(), EndfieldGachaActivity::class.java))
        }
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
                    binding.CurrentAccount.text = state.currentAccountEf?.nickName
                        ?: state.accountEfList.firstOrNull()?.account?.nickName
                        ?: getString(R.string.login_account)
                    accountAdapter.submitList(state.accountEfList)
                    renderStaminaCache(state)
                    val showAnnounce = prefManager.showHomeAnnounce.get()
                    binding.AnnounceTitle.visibility = if (showAnnounce) View.VISIBLE else View.GONE
                    binding.AnnounceCard.visibility = if (showAnnounce) View.VISIBLE else View.GONE
                }
            }
        }
        viewModel.fetchBiliVideos(HomeGame.ENDFIELD)
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadEndfieldStaminaCache()
    }

    private fun renderStaminaCache(state: MainUiState) {
        val card: CardApCacheBinding = binding.EfStaminaCard
        card.Current.setTextColor(ContextCompat.getColor(requireContext(), R.color.endfield_on_surface_accent))
        card.SanityProgress.setIndicatorColor(
            ContextCompat.getColor(requireContext(), R.color.endfield_primary),
        )
        card.SanityProgress.trackColor =
            ContextCompat.getColor(requireContext(), R.color.endfield_progress_track)
        val account = state.currentAccountEf
            ?: state.accountEfList.firstOrNull()?.account as? AccountEf
        card.AccountName.text = account?.nickName ?: getString(R.string.endfield_stamina)
        card.Server.setText(R.string.endfield_stamina)
        card.Server.visibility = if (account == null) View.GONE else View.VISIBLE
        val cache = state.endfieldStaminaCache?.takeIf { it.isValid && it.uid == account?.uid }
        if (account == null || cache == null) {
            card.RestTime.setText(if (account == null) R.string.home_realtime_login_required
                else R.string.home_realtime_not_synced)
            card.LastSync.setText(if (account == null) R.string.home_realtime_login_hint
                else R.string.home_realtime_sync_hint)
            card.Current.visibility = View.GONE
            card.Max.visibility = View.GONE
            card.SanityProgress.visibility = View.GONE
            card.Freshness.visibility = View.GONE
            return
        }
        val now = System.currentTimeMillis() / 1000
        val age = (now - cache.sampledAt).coerceAtLeast(0)
        card.LastSync.text = getLastUpdateStr(age).let { if (it.isEmpty()) "刚刚" else "${it}前" }
        card.Current.visibility = View.VISIBLE
        card.Max.visibility = View.VISIBLE
        val current = if (cache.fullAt > 0 && now >= cache.fullAt) cache.maximum else cache.current
        card.Current.text = current.toString()
        card.Max.text = "/${cache.maximum}"
        card.RestTime.text = if (current >= cache.maximum || cache.fullAt <= 0) {
            getString(R.string.home_sanity_full)
        } else {
            getString(R.string.home_sanity_recovery,
                getRemainTimeStr((cache.fullAt - now).coerceAtLeast(0)))
        }
        card.SanityProgress.visibility = View.VISIBLE
        card.SanityProgress.max = cache.maximum.coerceAtLeast(1).coerceAtMost(Int.MAX_VALUE.toLong()).toInt()
        card.SanityProgress.progress = current.coerceIn(0, card.SanityProgress.max.toLong()).toInt()
        card.Freshness.visibility = if (age >= CACHE_STALE_SECONDS) View.VISIBLE else View.GONE
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
            ContextCompat.getColorStateList(requireContext(), R.color.endfield_on_surface_accent),
        )
        ViewCompat.setBackgroundTintList(
            Icon,
            ContextCompat.getColorStateList(requireContext(), R.color.endfield_accent_container),
        )
        Title.setText(info.title)
        Subtitle.setText(R.string.endfield_entry_pending)
        Layout.isClickable = false
        Layout.isFocusable = false
    }

    override fun onDestroyView() {
        accountPopup?.dismiss()
        accountPopup = null
        bannerController.release()
        _binding = null
        super.onDestroyView()
    }
}
