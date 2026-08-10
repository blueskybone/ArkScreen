package com.blueskybone.arkscreen.ui.main


import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Rect
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
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.blueskybone.arkscreen.R
import com.blueskybone.arkscreen.data.local.pref.SettingPrefManager
import com.blueskybone.arkscreen.databinding.CardApCacheBinding
import com.blueskybone.arkscreen.databinding.ChipRoundBinding
import com.blueskybone.arkscreen.databinding.FragmentHomeBinding
import com.blueskybone.arkscreen.domain.model.account.Account
import com.blueskybone.arkscreen.domain.model.account.AccountSk
import com.blueskybone.arkscreen.domain.model.cache.ApCache
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
import com.blueskybone.arkscreen.ui.common.bindinginfo.UserManual
import com.blueskybone.arkscreen.ui.common.view.MenuDialog
import com.blueskybone.arkscreen.ui.gacha.GachaActivity
import com.blueskybone.arkscreen.ui.main.common.HomeBannerController
import com.blueskybone.arkscreen.ui.main.common.HomeDialogController
import com.blueskybone.arkscreen.ui.realtime.RealTimeActivity
import com.blueskybone.arkscreen.ui.recruit.RecruitActivity
import com.blueskybone.arkscreen.platform.schedule.AttendanceWorkScheduler
import com.blueskybone.arkscreen.util.TimeUtils.getCurrentTs
import com.blueskybone.arkscreen.util.TimeUtils.getLastUpdateStr
import com.blueskybone.arkscreen.util.TimeUtils.getRemainTimeStr
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
        if (!granted) Toaster.show("未授予通知权限，签到仍会继续")
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
        // RealTimeActivity refreshes the persisted snapshot. Reload it whenever Home becomes
        // visible again so the card reflects the latest successful realtime request.
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
                if (!token.isNullOrBlank()) {
                    Toaster.show(getString(R.string.getting_info))
                    viewModel.loginSkland(
                        SyncAccountSkUseCase.LoginWay.Token(token)
                    )
                } else {
                    Toaster.show("登录结果为空")
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
            currentAccount?.nickName ?: getString(R.string.no_login)
        binding.ApCacheCard.AccountName.text = currentAccount?.nickName.orEmpty()
        binding.ApCacheCard.AccountName.visibility =
            if (currentAccount == null) View.GONE else View.VISIBLE
        binding.ApCacheCard.Server.visibility =
            if (currentAccount == null) View.GONE else View.VISIBLE
        currentAccount?.let { account ->
            binding.ApCacheCard.Server.setText(
                if (account.official) R.string.official_server else R.string.bilibili_server
            )
        }

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
        binding.LinkPreference.visibility =
            if (state.links.isEmpty()) View.GONE else View.VISIBLE
    }

    private fun renderBanner(state: MainUiState) {
        val videos = state.biliVideos
        if (videos.isEmpty()) return
        bannerController.submitList(videos)
    }

    private fun renderApCache(state: MainUiState) {
        val value = state.apCache ?: return
        if (!value.isnull) {
            binding.ApCacheCard.bind(value)
        }
    }

    private fun setupBinding() {
        binding.RealTimeData.setOnClickListener {
            startActivity(Intent(requireContext(), RealTimeActivity::class.java))
        }

        binding.CurrentAccount.setOnClickListener { view ->
            if (!viewModel.hasSklandAccounts()) {
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
            } else {
                dialogController.showAccountPopup(view)

            }
        }

        binding.RecruitCalc.setup(RecruitCal)
        binding.OpeAssets.setup(OpeAssets)
        binding.GachaStat.setup(GachaStat)
        binding.Attendance.setup(Attendance)
        binding.AccountManager.setup(AccountManager)
        binding.GameStarter.setup(GameStarter)
        binding.UserManual.setup(UserManual)

        binding.RecruitCalc.Layout.setOnClickListener {
            startActivity(Intent(requireContext(), RecruitActivity::class.java))
        }
        binding.OpeAssets.Layout.setOnClickListener {
            startActivity(Intent(requireContext(), CharAssets::class.java))
        }
        binding.GachaStat.Layout.setOnClickListener {
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
            this.requireContext().launchApp(packageName) { Toaster.show("未检测到游戏安装") }
        }
        binding.UserManual.Layout.setOnClickListener {
            val cvId = "40623349"
            try {
                startActivity(Intent(Intent.ACTION_VIEW, "bilibili://article/$cvId".toUri()))
            } catch (_: Exception) {
                startActivity(
                    Intent(Intent.ACTION_VIEW, "https://www.bilibili.com/read/cv$cvId".toUri())
                )
            }
        }

        binding.AddLink.setOnClickListener {
            dialogController.showAddLinkDialog()
        }

        binding.Attendance.Layout.setOnClickListener {
            startManualAttendance()
        }

        binding.ExLinks.layoutManager = GridLayoutManager(requireContext(), 4)
        binding.ExLinks.adapter = linkAdapter
        if (binding.ExLinks.itemDecorationCount == 0) {
            binding.ExLinks.addItemDecoration(object : RecyclerView.ItemDecoration() {
                override fun getItemOffsets(
                    outRect: Rect,
                    view: View,
                    parent: RecyclerView,
                    state: RecyclerView.State
                ) {
                    val params = view.layoutParams as RecyclerView.LayoutParams
                    params.width = parent.width / 4
                    view.layoutParams = params
                }
            })
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
        AttendanceWorkScheduler.enqueue(
            context = requireContext(),
            force = true,
            allowRepeatToday = true,
        )
        Toaster.show("签到开始，可在通知栏查看进度")
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
        val now = getCurrentTs()
        val lastSyncStr = getLastUpdateStr(now - value.lastSyncTs).let {
            if (it.isEmpty()) "刚刚" else "${it}前"
        }

        LastSync.text = lastSyncStr

        if (value.current >= value.max || now > value.recoverTime) {
            Current.text = value.max.toString()
            RestTime.text = getString(R.string.recovered)
        } else {
            val currentStr =
                (value.max - ((value.recoverTime - now).toInt() / (60 * 6) + 1)).toString()
            Current.text = currentStr
            RestTime.text = getRemainTimeStr(value.recoverTime - now)
        }

        Max.text = "/${value.max}"
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

}
