package com.blueskybone.arkscreen.ui.main

import com.blueskybone.arkscreen.ui.account.common.showAccountChoices
import androidx.appcompat.widget.ListPopupWindow
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
import androidx.core.widget.ImageViewCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.blueskybone.arkscreen.R
import com.blueskybone.arkscreen.data.local.pref.SettingPrefManager
import com.blueskybone.arkscreen.domain.model.account.AccountEf
import com.blueskybone.arkscreen.domain.model.account.AccountType
import com.blueskybone.arkscreen.domain.repository.AttendanceStateRepository
import com.blueskybone.arkscreen.ui.common.view.InfoStatusTone
import com.blueskybone.arkscreen.ui.common.view.attendedToday
import com.blueskybone.arkscreen.ui.common.view.renderInfoStatus
import com.blueskybone.arkscreen.databinding.ChipRoundBinding
import com.blueskybone.arkscreen.databinding.CardApCacheBinding
import com.blueskybone.arkscreen.databinding.FragmentEndfieldHomeBinding
import com.blueskybone.arkscreen.databinding.HomeCoreTileBinding
import com.blueskybone.arkscreen.platform.schedule.AttendanceWorkScheduler
import com.blueskybone.arkscreen.ui.account.AccountMngActivity
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
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.catch
import java.time.LocalDate
import java.time.ZoneId
import org.koin.android.ext.android.getKoin
import org.koin.androidx.viewmodel.ext.android.activityViewModel

/** 终末地首页入口；详情和数据源接入后再为三张卡片添加内容与跳转。 */
class EndfieldHome : Fragment() {
    private companion object { const val CACHE_STALE_SECONDS = 24 * 60 * 60L }
    private val prefManager: SettingPrefManager by getKoin().inject()
    private val attendanceStates: AttendanceStateRepository by getKoin().inject()
    private val viewModel: MainViewModel by activityViewModel()
    private var _binding: FragmentEndfieldHomeBinding? = null
    private val binding get() = requireNotNull(_binding)
    private var accountPopup: ListPopupWindow? = null
    private lateinit var bannerController: HomeBannerController
    private var attendanceKey: String? = null
    private var attendanceJob: Job? = null
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
        (requireActivity() as MainActivity).bindHomeGameControls(
            binding.GameHeader.GameSelector,
            binding.GameHeader.SwitchGame,
            binding.GameHeader.DefaultStartup,
        )
        binding.AccountSelector.root.setOnClickListener { anchor ->
            val state = viewModel.uiState.value
            val accounts = state.accountEfList.map { it.account as AccountEf }
            if (accounts.isEmpty()) {
                startActivity(Intent(requireContext(), AccountMngActivity::class.java))
            } else {
                accountPopup = showAccountChoices(anchor, accounts, effectiveAccount(state)?.uid) { account ->
                    viewModel.setDefaultAccountEf(account)
                    Toaster.show(getString(R.string.set_default_account, account.nickName))
                }
            }
        }
        binding.EfStaminaCard.apply {
            Current.setTextColor(ContextCompat.getColor(requireContext(), R.color.endfield_on_surface_accent))
            SanityProgress.setIndicatorColor(ContextCompat.getColor(requireContext(), R.color.endfield_primary))
            SanityProgress.trackColor = ContextCompat.getColor(requireContext(), R.color.endfield_progress_track)
        }
        binding.CoreGroup.CoreAssets.setup(OpeAssets)
        binding.CoreGroup.CoreAssets.Layout.isClickable = true
        binding.CoreGroup.CoreAssets.Layout.isFocusable = true
        binding.CoreGroup.CoreAssets.Layout.setOnClickListener {
            startActivity(Intent(requireContext(), EndfieldCharsActivity::class.java))
        }
        binding.RealTimeData.setOnClickListener {
            startActivity(Intent(requireContext(), EndfieldRealtimeActivity::class.java))
        }
        binding.CoreGroup.CoreGacha.setup(GachaStat)
        binding.CoreGroup.CoreGacha.Layout.isClickable = true
        binding.CoreGroup.CoreGacha.Layout.isFocusable = true
        binding.CoreGroup.CoreGacha.Layout.setOnClickListener {
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
                    val showBanner = prefManager.showHomeBanner.get()
                    binding.BannerCard.visibility = if (showBanner) View.VISIBLE else View.GONE
                    if (showBanner) bannerController.submitList(videos)
                    binding.Announce.text = state.announce
                    val account = state.currentAccountEf ?: state.accountEfList.firstOrNull()?.account
                    binding.AccountSelector.CurrentAccount.text = account?.nickName ?: getString(R.string.login_account)
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
        binding.BannerCard.visibility = if (prefManager.showHomeBanner.get()) View.VISIBLE else View.GONE
        viewModel.loadEndfieldStaminaCache()
        val state = viewModel.uiState.value
        updateAttendanceStatus(effectiveAccount(state))
    }

    private fun renderStaminaCache(state: MainUiState) {
        val card: CardApCacheBinding = binding.EfStaminaCard
        val account = effectiveAccount(state)
        updateAttendanceStatus(account)
        val cache = state.endfieldStaminaCache?.takeIf { it.isValid && it.uid == account?.uid }
        if (account == null || cache == null) {
            card.RestTime.setText(if (account == null) R.string.home_realtime_login_required
                else R.string.home_realtime_not_synced)
            card.LastSync.setText(if (account == null) R.string.home_realtime_login_hint
                else R.string.home_realtime_sync_hint)
            card.Current.text = "—"
            card.Max.visibility = View.GONE
            card.SanityProgress.visibility = View.GONE
            card.Freshness.visibility = View.GONE
            return
        }
        val now = System.currentTimeMillis() / 1000
        val age = (now - cache.sampledAt).coerceAtLeast(0)
        val lastSync = getLastUpdateStr(age).let { if (it.isEmpty()) getString(R.string.time_just_now) else getString(R.string.time_ago, it) }
        card.LastSync.text = getString(R.string.home_updated_at, lastSync)

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
        card.SanityProgress.max = cache.maximum.coerceIn(1L, Int.MAX_VALUE.toLong()).toInt()
        card.SanityProgress.progress = current.coerceIn(0, card.SanityProgress.max.toLong()).toInt()
        card.Freshness.visibility = if (age >= CACHE_STALE_SECONDS) View.VISIBLE else View.GONE
    }

    private fun effectiveAccount(state: MainUiState): AccountEf? = state.currentAccountEf
        ?: state.accountEfList.firstOrNull()?.account as? AccountEf

    private fun updateAttendanceStatus(account: AccountEf?) {
        val chip = binding.AccountSelector.AttendanceStatus
        if (account == null) {
            attendanceJob?.cancel()
            attendanceKey = null
            chip.visibility = View.GONE
            return
        }
        val key = "${account.uid}:${LocalDate.now(ZoneId.of("Asia/Shanghai"))}"
        if (attendanceKey == key && attendanceJob?.isActive == true) return
        attendanceKey = key
        attendanceJob?.cancel()
        chip.visibility = View.GONE
        attendanceJob = viewLifecycleOwner.lifecycleScope.launch {
            attendanceStates.observe(AccountType.EF, account.uid)
                .catch { emit(null) }
                .collect { state ->
                    if (_binding == null || effectiveAccount(viewModel.uiState.value)?.uid != account.uid) return@collect
                    val signed = attendedToday(state?.lastSuccessTs ?: 0L)
                    chip.renderInfoStatus(
                        if (signed) R.string.status_attended_today else R.string.status_not_attended_today,
                        if (signed) InfoStatusTone.SUCCESS else InfoStatusTone.WARNING,
                    )
                    chip.visibility = View.VISIBLE
                }
        }
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
        Title.setText(info.title)
    }

    private fun HomeCoreTileBinding.setup(info: FuncChipInfo) {
        Icon.setImageResource(info.icon)
        ImageViewCompat.setImageTintList(
            Icon,
            ContextCompat.getColorStateList(requireContext(), R.color.endfield_on_surface_accent),
        )
        Title.setText(info.title)
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
