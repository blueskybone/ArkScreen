package com.blueskybone.arkscreen.ui.main


import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.net.toUri
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.widget.ImageViewCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.blueskybone.arkscreen.R
import com.blueskybone.arkscreen.data.local.pref.SettingPrefManager
import com.blueskybone.arkscreen.databinding.CardApCacheBinding
import com.blueskybone.arkscreen.databinding.ChipRoundBinding
import com.blueskybone.arkscreen.databinding.FragmentHomeBinding
import com.blueskybone.arkscreen.domain.model.account.Account
import com.blueskybone.arkscreen.domain.model.account.AccountSk
import com.blueskybone.arkscreen.domain.model.cache.ApCache
import com.blueskybone.arkscreen.domain.service.AppClock
import com.blueskybone.arkscreen.domain.usecase.account.SyncAccountSkUseCase
import com.blueskybone.arkscreen.ui.account.AccountMngActivity
import com.blueskybone.arkscreen.ui.account.LoginWeb
import com.blueskybone.arkscreen.ui.account.adapter.AccountAdapter
import com.blueskybone.arkscreen.ui.account.model.AccountItemAction
import com.blueskybone.arkscreen.ui.character.CharAssets
import com.blueskybone.arkscreen.ui.common.adapter.ItemListener
import com.blueskybone.arkscreen.ui.common.bindinginfo.AccountManager
import com.blueskybone.arkscreen.ui.common.bindinginfo.Attendance
import com.blueskybone.arkscreen.ui.common.bindinginfo.FuncChipInfo
import com.blueskybone.arkscreen.ui.common.bindinginfo.GachaStat
import com.blueskybone.arkscreen.ui.common.bindinginfo.GameStarter
import com.blueskybone.arkscreen.ui.common.bindinginfo.OpeAssets
import com.blueskybone.arkscreen.ui.common.bindinginfo.RecruitCal
import com.blueskybone.arkscreen.ui.common.view.MenuDialog
import com.blueskybone.arkscreen.ui.gacha.GachaActivity
import com.blueskybone.arkscreen.ui.main.common.HomeBannerController
import com.blueskybone.arkscreen.ui.main.common.HomeDialogController
import com.blueskybone.arkscreen.ui.realtime.RealTimeActivity
import com.blueskybone.arkscreen.ui.recruit.RecruitActivity
import com.blueskybone.arkscreen.platform.schedule.AttendanceWorkScheduler
import com.blueskybone.arkscreen.platform.time.TimeUtils.getCurrentTs
import com.blueskybone.arkscreen.platform.time.TimeUtils.getLastUpdateStr
import com.blueskybone.arkscreen.platform.time.TimeUtils.getRemainTimeStr
import com.blueskybone.arkscreen.util.launchApp
import com.blueskybone.arkscreen.ui.common.openLink
import com.hjq.toast.Toaster
import kotlinx.coroutines.launch
import org.koin.android.ext.android.getKoin
import org.koin.androidx.viewmodel.ext.android.activityViewModel


/**
 *   Created by blueskybone
 *   Date: 2024/12/30
 */
class Home : Fragment() {

    private val prefManager: SettingPrefManager by getKoin().inject()
    private val appClock: AppClock by getKoin().inject()
    private val viewModel: MainModel by activityViewModel()
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var linkAdapter: LinkGridAdapter
    private lateinit var accountAdapter: AccountAdapter
    private lateinit var dialogController: HomeDialogController
    private lateinit var activityResultLauncher: ActivityResultLauncher<Intent>
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
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initLaunchers()
        initAdapters()
        initController()
        setupBinding()
        collectUiState()
    }

    override fun onResume() {
        super.onResume()
        // 实时数据页会更新持久化快照；首页每次重新可见时都应读取一次，
        // 这样卡片能展示最近一次成功请求的数据。
        viewModel.loadApCache()
    }

    private fun initController() {
        bannerController = HomeBannerController(
            fragment = this,
            viewPager = binding.TitleBanner,
            indicatorLayout = binding.BannerIdc
        )
        dialogController = HomeDialogController(
            fragment = this,
            viewModel = viewModel,
            accountAdapter = accountAdapter,
            onLaunchWebLogin = {
                val intent = LoginWeb.startIntent(
                    requireContext(),
                    LoginWeb.Companion.LoginType.SKLAND
                )
                activityResultLauncher.launch(intent)
            }
        )
    }

    private fun initLaunchers() {
        activityResultLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data = result.data
                val token = data?.getStringExtra("token")
                val dId = data?.getStringExtra("dId")
                if (!token.isNullOrBlank() && !dId.isNullOrBlank()) {
                    Toaster.show(getString(R.string.getting_info))
                    viewModel.loginSkland(
                        SyncAccountSkUseCase.LoginWay.Token(token, dId)
                    )
                } else {
                    Toaster.show(getString(R.string.login_result_empty))
                }
            }
        }
    }

    private fun initAdapters() {
        linkAdapter = LinkGridAdapter(linkListener)
        accountAdapter = AccountAdapter(createItemAction())
    }


    private fun createItemAction(): AccountItemAction {
        return object : AccountItemAction {
            override fun onClick(account: Account) {
                viewModel.setDefaultAccountSk(account as AccountSk)
                dialogController.dismissPopup()
                Toaster.show(getString(R.string.set_default_account, account.nickName))
            }

            override fun onLongClick(account: Account) {
            }
        }
    }

    private fun collectUiState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    renderAccount(state)
                    renderAnnounce(state)
                    renderLinks(state)
                    renderBanner(state)
                    renderApCache(state)
                }
            }
        }
    }

    private fun renderAccount(state: MainUiState) {
        val currentAccount = state.currentAccountSk
        binding.CurrentAccount.text =
            currentAccount?.nickName ?: getString(R.string.login_account)
        binding.LoginGuide.visibility =
            if (currentAccount == null) View.VISIBLE else View.GONE
        accountAdapter.submitList(state.accountSkList)
    }

    private fun renderAnnounce(state: MainUiState) {
        binding.Announce.text = state.announce
        val showAnnounce = prefManager.showHomeAnnounce.get()
        binding.AnnounceTitle.visibility = if (showAnnounce) View.VISIBLE else View.GONE
        binding.AnnounceCard.visibility = if (showAnnounce) View.VISIBLE else View.GONE
    }

    private fun renderLinks(state: MainUiState) {
        linkAdapter.submitList(state.links)
        binding.LinkCard.visibility =
            if (state.links.isEmpty()) View.GONE else View.VISIBLE
    }

    private fun renderBanner(state: MainUiState) {
        val videos = state.biliVideos
        binding.BannerCard.visibility = View.VISIBLE
        if (videos.isEmpty()) return
        bannerController.submitList(videos)
    }

    private fun renderApCache(state: MainUiState) {
        val card = binding.ApCacheCard
        val cacheOwner = state.cacheAccountInfo
        card.AccountName.text = cacheOwner?.nickname.orEmpty()
        card.AccountName.visibility = if (cacheOwner == null) View.GONE else View.VISIBLE
        card.Server.visibility = if (cacheOwner == null) View.GONE else View.VISIBLE
        cacheOwner?.let { owner ->
            card.Server.setText(
                if (owner.official) R.string.official_server else R.string.bilibili_server
            )
        }
        val account = state.currentAccountSk
        val value = state.apCache
        when {
            account == null -> {
                card.RestTime.setText(R.string.home_realtime_login_required)
                card.LastSync.setText(R.string.home_realtime_login_hint)
                card.Current.visibility = View.GONE
                card.Max.visibility = View.GONE
                card.SanityProgress.visibility = View.GONE
                card.Freshness.visibility = View.GONE
            }
            value == null || value.isnull -> {
                card.RestTime.setText(R.string.home_realtime_not_synced)
                card.LastSync.setText(R.string.home_realtime_sync_hint)
                card.Current.visibility = View.GONE
                card.Max.visibility = View.GONE
                card.SanityProgress.visibility = View.GONE
                card.Freshness.visibility = View.GONE
            }
            else -> {
                card.Current.visibility = View.VISIBLE
                card.Max.visibility = View.VISIBLE
                card.bind(value)
            }
        }
    }

    private fun setupBinding() {
        binding.RealTimeData.setOnClickListener {
            startActivity(Intent(requireContext(), RealTimeActivity::class.java))
        }

        binding.CurrentAccount.setOnClickListener { view ->
            if (!viewModel.hasSklandAccounts()) {
                showSklandLoginMenu()
            } else {
                dialogController.showAccountPopup(view)

            }
        }

        binding.LoginNow.setOnClickListener {
            showSklandLoginMenu()
        }

        binding.RecruitCalc.setup(RecruitCal)
        binding.Attendance.setup(Attendance)
        binding.AccountManager.setup(AccountManager)
        binding.GameStarter.setup(GameStarter)

        binding.RecruitCalc.Layout.setOnClickListener {
            startActivity(Intent(requireContext(), RecruitActivity::class.java))
        }
        binding.CoreAssets.apply {
            Icon.setImageResource(OpeAssets.icon)
            ImageViewCompat.setImageTintList(
                Icon,
                android.content.res.ColorStateList.valueOf(
                    com.google.android.material.color.MaterialColors.getColor(
                        Icon,
                        com.google.android.material.R.attr.colorPrimary,
                    )
                ),
            )
            ViewCompat.setBackgroundTintList(
                Icon,
                ContextCompat.getColorStateList(requireContext(), R.color.sec_con),
            )
            Title.setText(OpeAssets.title)
            Subtitle.setText(R.string.assets_entry_summary)
        }
        binding.CoreGacha.apply {
            Icon.setImageResource(GachaStat.icon)
            ImageViewCompat.setImageTintList(
                Icon,
                android.content.res.ColorStateList.valueOf(
                    com.google.android.material.color.MaterialColors.getColor(
                        Icon,
                        com.google.android.material.R.attr.colorPrimary,
                    )
                ),
            )
            ViewCompat.setBackgroundTintList(
                Icon,
                ContextCompat.getColorStateList(requireContext(), R.color.sec_con),
            )
            Title.setText(GachaStat.title)
            Subtitle.setText(R.string.gacha_entry_summary)
        }

        binding.CoreAssets.Layout.setOnClickListener {
            startActivity(Intent(requireContext(), CharAssets::class.java))
        }
        binding.CoreGacha.Layout.setOnClickListener {
            startActivity(Intent(requireContext(), GachaActivity::class.java))
        }
        binding.AccountManager.Layout.setOnClickListener {
            startActivity(Intent(requireContext(), AccountMngActivity::class.java))
        }
        binding.GameStarter.Layout.setOnClickListener {
            val packageName = viewModel.currentGamePackageName()
            if (packageName == null) {
                Toaster.show(getString(R.string.no_login))
                return@setOnClickListener
            }
            this.requireContext().launchApp(packageName) {
                Toaster.show(getString(R.string.game_not_found))
            }
        }
        binding.AddLink.setOnClickListener {
            dialogController.showAddLinkDialog()
        }

        binding.Attendance.Layout.setOnClickListener {
            startManualAttendance()
        }

        binding.ExLinks.adapter = linkAdapter
    }

    private fun showSklandLoginMenu() {
        MenuDialog(requireContext())
            .add(getString(R.string.import_cookie)) {
                dialogController.showCookieLoginDialog()
            }
            .add(R.string.web_login) {
                dialogController.launchWebLogin()
            }
            .add(R.string.password_login) {
                dialogController.showPasswordLoginDialog()
            }
            .show()
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
        AttendanceWorkScheduler.enqueue(
            context = requireContext(),
            allowRepeatToday = true,
        )
        Toaster.show(getString(R.string.attendance_started))
    }

    private val linkListener = object : ItemListener {
        override fun onClick(position: Int) {
            linkAdapter.currentList.getOrNull(position)?.let { value ->
                try {
                    openLink(requireContext(), value.url, prefManager)
                } catch (e: Exception) {
                    Toaster.show(getString(R.string.illegal_url))
                }
            }
        }

        override fun onLongClick(position: Int) {
            linkAdapter.currentList.getOrNull(position)?.let { value ->
                MenuDialog(requireContext())
                    .add(R.string.edit) { dialogController.showEditLinkDialog(value.copy()) }
                    .add(R.string.delete) { dialogController.showDeleteConfirm(value.copy()) }
                    .show()
            }
        }
    }

    private fun CardApCacheBinding.bind(value: ApCache) {
        val now = getCurrentTs(appClock)
        val cacheAge = (now - value.lastSyncTs).coerceAtLeast(0)
        val lastSyncStr = getLastUpdateStr(cacheAge).let {
            if (it.isEmpty()) "刚刚" else "${it}前"
        }

        LastSync.text = lastSyncStr

        val current = when {
            value.current >= value.max -> value.current
            now >= value.recoverTime -> value.max
            else -> value.max - ((value.recoverTime - now).toInt() / (60 * 6) + 1)
        }.coerceAtLeast(0)

        Current.text = current.toString()
        RestTime.text = if (current >= value.max) {
            getString(R.string.home_sanity_full)
        } else {
            getString(
                R.string.home_sanity_recovery,
                getRemainTimeStr(value.recoverTime - now),
            )
        }

        Max.text = "/${value.max}"
        SanityProgress.visibility = View.VISIBLE
        SanityProgress.max = value.max.coerceAtLeast(1)
        SanityProgress.setProgressCompat(current.coerceAtMost(SanityProgress.max), true)

        val stale = cacheAge >= CACHE_STALE_SECONDS
        Freshness.visibility = if (stale) View.VISIBLE else View.GONE
        if (stale) {
            Freshness.backgroundTintList = android.content.res.ColorStateList.valueOf(
                ContextCompat.getColor(requireContext(), R.color.status_warning_container)
            )
            Freshness.setTextColor(
                ContextCompat.getColor(requireContext(), R.color.status_warning_content)
            )
        }
    }

    private fun ChipRoundBinding.setup(funcChipInfo: FuncChipInfo) {
        Icon.setImageResource(funcChipInfo.icon)
        ImageViewCompat.setImageTintList(
            Icon,
            ContextCompat.getColorStateList(requireContext(), funcChipInfo.colors.icon),
        )
        ViewCompat.setBackgroundTintList(
            Icon,
            ContextCompat.getColorStateList(requireContext(), funcChipInfo.colors.container),
        )
        Title.setText(funcChipInfo.title)
    }


    override fun onDestroyView() {
        bannerController.release()
        dialogController.release()
        binding.ExLinks.adapter = null
        _binding = null
        super.onDestroyView()
    }

    private companion object {
        const val CACHE_STALE_SECONDS = 24 * 60 * 60L
    }

}
