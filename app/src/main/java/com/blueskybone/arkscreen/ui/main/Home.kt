package com.blueskybone.arkscreen.ui.main


import android.app.Activity
import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.blueskybone.arkscreen.R
import com.blueskybone.arkscreen.data.local.pref.PrefManager
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
import com.blueskybone.arkscreen.legacy.deprecated.RecruitActivity
import com.blueskybone.arkscreen.util.TimeUtils.getCurrentTs
import com.blueskybone.arkscreen.util.TimeUtils.getLastUpdateStr
import com.blueskybone.arkscreen.util.TimeUtils.getRemainTimeStr
import com.blueskybone.arkscreen.util.launchApp
import com.hjq.toast.Toaster
import kotlinx.coroutines.launch
import org.koin.android.ext.android.getKoin


/**
 *   Created by blueskybone
 *   Date: 2024/12/30
 */
//class Home : Fragment() {
//
//    private val prefManager: PrefManager by getKoin().inject()
//    private val model: MainModel by activityViewModels()
//    private var _binding: FragmentHomeBinding? = null
//    private val binding get() = _binding!!
//
//    private var adapter: LinkGridAdapter? = null
//    private var adapterBanner: ImagePagerAdapter? = null
//    private var adapterAccount: AccountAdapter? = null
//
//    private var autoScrollJob: Job? = null
//    private val scrollDelay = 5000L
//    private var accountPopup: PopupWindow? = null
//
//    private lateinit var activityResultLauncher: ActivityResultLauncher<Intent>
//
//
//    private val adapterListener = object : ItemListener {
//        override fun onClick(position: Int) {
//            adapter?.currentList?.get(position)?.let { value ->
//                try {
//                    val url = value.url
////                    openLink(requireContext(), url, prefManager)
//
//                } catch (e: Exception) {
//                    Toaster.show(getString(R.string.illegal_url))
//                    e.printStackTrace()
//                }
//            }
//        }
//
//        override fun onLongClick(position: Int) {
//            adapter?.currentList?.get(position)?.let { value ->
//                MenuDialog(requireContext())
//                    .add(R.string.edit) { displayEditDialog(value.copy()) }
//                    .add(R.string.delete) { confirmDeletion(value.copy()) }
//                    .show()
//            }
//        }
//    }
//
//    private val bannerListener = object : ItemListener {
//        override fun onClick(position: Int) {
//            adapterBanner?.getItem(position)?.let { value ->
//                try {
//                    val url = "bilibili://video/${value.bVid}"
//                    val intent = Intent(Intent.ACTION_VIEW, url.toUri())
//                    intent.setPackage("tv.danmaku.bili")
//                    startActivity(intent)
//                } catch (_: Exception) {
//                    val biliHome = "https://bilibili.com/video/${value.bVid}"
//                    startActivity(Intent(Intent.ACTION_VIEW, biliHome.toUri()))
//                }
//            }
//        }
//
//        override fun onLongClick(position: Int) {
//
//        }
//    }
//
//    override fun onCreateView(
//        inflater: LayoutInflater,
//        container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View {
//        _binding = FragmentHomeBinding.inflate(inflater)
//        adapter = LinkGridAdapter(adapterListener)
//        adapterAccount = AccountAdapter(requireContext(), adapterSkListener)
//
//        initialize()
//        setupBinding()
//        setupObserver()
//        return binding.root
//    }
//
//    private fun initialize() {
//        activityResultLauncher = registerForActivityResult(
//            ActivityResultContracts.StartActivityForResult()
//        ) { result ->
//            // 处理返回结果
//            if (result.resultCode == Activity.RESULT_OK) {
//                val data: Intent? = result.data
//                // 解析返回的数据
//                val token = data?.getStringExtra("token")
//                val dId = data?.getStringExtra("dId")
//                if (token != null && dId != null) {
//                    Toaster.show(getString(R.string.getting_info))
////                    model.accountSkLogin(token, dId)
//                } else {
//                    Toaster.show("null")
//                }
//            }
//        }
//    }
//
//    private fun setupObserver() {
//        model.currentAccountSk.observe(viewLifecycleOwner) { value ->
//            if (value == null) binding.CurrentAccount.text = getString(R.string.no_login)
//            else binding.CurrentAccount.text = value.nickName
//        }
//        model.announce.observe(viewLifecycleOwner) { value ->
//            binding.Announce.text = value
//        }
//        model.links.observe(viewLifecycleOwner) { value ->
//            adapter?.submitList(value)
//            if (value.isEmpty()) {
//                binding.LinkPreference.visibility = View.GONE
//            } else {
//                binding.LinkPreference.visibility = View.VISIBLE
//            }
//        }
//        model.biliVideo.observe(viewLifecycleOwner) { value ->
//            adapterBanner = ImagePagerAdapter(bannerListener, value)
//            binding.TitleBanner.adapter = adapterBanner
//            setupIndicators(value)
//            startAutoScroll()
//            binding.TitleBanner.isUserInputEnabled = true
//        }
//
//        model.accountSkList.observe(viewLifecycleOwner) { value ->
//            adapterAccount?.submitList(value)
//        }
//    }
//
//    private fun startAutoScroll() {
//        autoScrollJob = lifecycleScope.launch {
//            while (true) {
//                delay(scrollDelay)
//                withContext(Dispatchers.Main) {
//                    val nextItem =
//                        (binding.TitleBanner.currentItem + 1) % (binding.TitleBanner.adapter?.itemCount
//                            ?: 1)
//                    binding.TitleBanner.setCurrentItem(nextItem, true)
//                }
//            }
//        }
//    }
//
//    private fun setupBinding() {
//        binding.RealTimeData.setOnClickListener {
//            startActivity(Intent(requireContext(), RealTimeActivity::class.java))
//        }
//
//        binding.RefreshGame.setOnClickListener {
//
//        }
//
//        binding.CurrentAccount.setOnClickListener { view ->
//            // ?这谁写的雷霆代码
//            model.accountSkList.value!!.let {
//                if (it.isEmpty()) {
//                    //登入
//                    MenuDialog(requireContext())
//                        .add(getString(R.string.import_cookie)) {
//                            displayLoginDialog()
//                        }
//                        .add(R.string.web_login) {
//                            val intent =
//                                LoginWeb.startIntent(
//                                    requireContext(),
//                                    LoginWeb.Companion.LoginType.SKLAND
//                                )
//                            activityResultLauncher.launch(intent)
//                        }
//                        .add(R.string.password_login) {
//                            displayPasswordLoginDialog()
//                        }
//                        .show()
//
//                } else {
//                    //切换账号
////                    val menuDialog = MenuDialog(requireContext())
////                    for (account in model.accountSkList.value!!) {
////                        menuDialog.add(account.nickName) {
////                            model.setDefaultAccountSk(account)
////                        }
////                    }
////                    menuDialog.show()
//                    showAccountPopup(view)
//                }
//            }
//        }
//        binding.RecruitCalc.setup(RecruitCal)
//        binding.OpeAssets.setup(OpeAssets)
//        binding.GachaStat.setup(GachaStat)
//        binding.Attendance.setup(Attendance)
//        binding.AccountManager.setup(AccountManager)
//        binding.GameStarter.setup(GameStarter)
//        binding.UserManual.setup(UserManual)
//
//        binding.RecruitCalc.Layout.setOnClickListener {
//            startActivity(Intent(requireContext(), RecruitActivity::class.java))
//        }
//        binding.OpeAssets.Layout.setOnClickListener {
//            startActivity(Intent(requireContext(), CharAssets::class.java))
//        }
//        binding.GachaStat.Layout.setOnClickListener {
//            startActivity(Intent(requireContext(), GachaActivity::class.java))
//        }
//        binding.AccountManager.Layout.setOnClickListener {
//            startActivity(Intent(requireContext(), AccountMngActivity::class.java))
//        }
//        binding.GameStarter.Layout.setOnClickListener {
//            val currAcc = model.currentAccountSk.value
//            if (currAcc != null && currAcc.official) {
//                openAnotherApp("com.hypergryph.arknights")
//            } else {
//                openAnotherApp("com.hypergryph.arknights.bilibili")
//            }
//        }
//        binding.UserManual.Layout.setOnClickListener {
//            val cvId = "40623349"
//            try {
//                val intent = Intent(Intent.ACTION_VIEW, "bilibili://article/$cvId".toUri())
//                startActivity(intent)
//            } catch (e: Exception) {
//                val intent =
//                    Intent(Intent.ACTION_VIEW, "https://www.bilibili.com/read/cv$cvId".toUri())
//                startActivity(intent)
//            }
//        }
//        binding.AddLink.setOnClickListener {
//            onAddButtonClick()
//        }
//        binding.Attendance.Layout.setOnClickListener {
//            Toaster.show("签到开始，可在通知栏查看进度")
//            model.startAttendance() //TODO:在通知栏显示更新
//        }
//
//        binding.ExLinks.layoutManager = GridLayoutManager(requireContext(), 4)
//        binding.ExLinks.adapter = adapter
//        binding.ExLinks.addItemDecoration(object : RecyclerView.ItemDecoration() {
//            override fun getItemOffsets(
//                outRect: Rect,
//                view: View,
//                parent: RecyclerView,
//                state: RecyclerView.State
//            ) {
//                super.getItemOffsets(outRect, view, parent, state)
//                val params = view.layoutParams as RecyclerView.LayoutParams
//                params.width = parent.width / 4
//                view.layoutParams = params
//            }
//        })
//
//        model.apCache.observe(viewLifecycleOwner) { value ->
//            if (!value.isnull) {
//                binding.ApCacheCard.bind(value)
//            }
//        }
//
//        if (!prefManager.showHomeAnnounce.get()) {
//            binding.AnnounceTitle.visibility = View.GONE
//            binding.AnnounceCard.visibility = View.GONE
//        }
//    }
//
//    private val adapterSkListener = object : ItemListener {
//        @SuppressLint("NotifyDataSetChanged")
//        override fun onClick(position: Int) {
//            adapterAccount?.currentList?.get(position)?.let { value ->
//                model.setDefaultAccountSk(value as AccountSk)
//                adapterAccount?.notifyDataSetChanged()
//                accountPopup?.dismiss()
//            }
//        }
//
//        override fun onLongClick(position: Int) {
//        }
//    }
//
//    private fun showAccountPopup(anchor: View) {
//        if (accountPopup?.isShowing == true) return
//        val activity = requireActivity()
//        // 初始化弹窗
//        val binding = PopupAccountBinding.inflate(LayoutInflater.from(requireContext()))
//        val popupView = binding.root  // 获取根布局
//        binding.lvAccount.adapter = adapterAccount
//
//        accountPopup = PopupWindow(
//            popupView,
//            ViewGroup.LayoutParams.WRAP_CONTENT,
//            ViewGroup.LayoutParams.WRAP_CONTENT,
//            true
//        ).apply {
//            setBackgroundDrawable(
//                ContextCompat.getDrawable(
//                    requireContext(),
//                    android.R.color.transparent
//                )
//            )
//            isOutsideTouchable = true
//            animationStyle = R.style.PopupDownAnim
//            setOnDismissListener {
//                activity.window?.attributes = activity.window?.attributes?.apply {
//                    this.alpha = 1.0f
//                }
//            }
//        }
//        // 显示弹窗前调整背景透明度
//        activity.window?.attributes = activity.window?.attributes?.apply {
//            alpha = 0.7f
//        }
//        // 计算弹窗位置，显示在锚点下方
//        val location = IntArray(2)
//        anchor.getLocationOnScreen(location)
//        val x = location[0]
//        val y = location[1] + anchor.height
//
//        // 显示弹窗，可根据需要调整x和y的偏移量
//        accountPopup?.showAtLocation(anchor, Gravity.NO_GRAVITY, x, y)
//    }
//
//    private fun displayLoginDialog() {
//
//        //TODO:?怎么cookie变成这样了
//        val dialogBinding = DialogInputBinding.inflate(layoutInflater)
//        dialogBinding.EditText2.visibility = View.GONE
//        dialogBinding.EditText1.hint = getString(R.string.import_cookie)
//        MaterialAlertDialogBuilder(requireContext())
//            .setView(dialogBinding.root)
//            .setTitle(R.string.import_cookie)
//            .setNegativeButton(R.string.cancel, null)
//            .setPositiveButton(R.string.import_cookie) { _, _ ->
//                val str = dialogBinding.EditText1.text.toString()
//                val list = str.split("@")
//                if (list.size == 2) {
//                    try {
//                        Toaster.show(getString(R.string.getting_info))
//                        model.loginSklandByToken(list[0])
//                    } catch (e: Exception) {
//                        e.printStackTrace()
//                    }
//                } else {
//                    Toaster.show(getString(R.string.wrong_format))
//                }
//            }.show()
//    }
//
//    /**
//     * 密码登录对话框
//     */
//    private fun displayPasswordLoginDialog() {
//        val dialogBinding = DialogInputBinding.inflate(layoutInflater)
//        dialogBinding.EditText1.hint = getString(R.string.phone_number)
//        dialogBinding.EditText2.visibility = View.VISIBLE
//        dialogBinding.EditText2.hint = getString(R.string.password)
//        dialogBinding.EditText2.inputType =
//            InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
//
//        MaterialAlertDialogBuilder(requireContext())
//            .setView(dialogBinding.root)
//            .setTitle(R.string.password_login)
//            .setNegativeButton(R.string.cancel, null)
//            .setPositiveButton(R.string.login) { _, _ ->
//                val phone = dialogBinding.EditText1.text.toString()
//                val password = dialogBinding.EditText2.text.toString()
//
//                if (phone.isEmpty() || password.isEmpty()) {
//                    Toaster.show("请输入手机号和密码")
//                    return@setPositiveButton
//                }
//
//                Toaster.show(getString(R.string.logging_in))
//
//                lifecycleScope.launch(Dispatchers.IO) {
//                    try {
//                        model.loginSklandByPhonePassword(phone, password)
//                    } catch (e: Exception) {
//                        withContext(Dispatchers.Main) {
//                            Toaster.show("登录失败：${e.message}")
//                        }
//                    }
//                }
//            }
//            .show()
//    }
//
//    private fun CardApCacheBinding.bind(value: ApCache) {
//        val now = getCurrentTs()
//        val lastSyncStr = getLastUpdateStr(now - value.lastSyncTs).let {
//            if (it.isEmpty()) "刚刚" else "${it}前"
//        }
//
//
//        this.LastSync.text = getString(R.string.last_sync_time, lastSyncStr)
//        if (value.current >= value.max) {
//            this.Current.text = value.current.toString()
//            this.RestTime.text = getString(R.string.recovered)
//        } else if (now > value.recoverTime) {
//            this.Current.text = value.max.toString()
//            this.RestTime.text = getString(R.string.recovered)
//        } else {
//            val currentStr =
//                (value.max - ((value.recoverTime - now).toInt() / (60 * 6) + 1)).toString()
//            this.Current.text = currentStr
//            this.RestTime.text = getRemainTimeStr(value.recoverTime - now)
//        }
//        val maxText = "/" + value.max.toString()
//        this.Max.text = maxText
//    }
//
//    override fun onDestroy() {
//        super.onDestroy()
//        _binding = null
//    }
//
//    private fun displayEditDialog(value: Link) {
//        val dialogBinding = DialogInputBinding.inflate(layoutInflater)
//        dialogBinding.EditText1.setText(value.title)
//        dialogBinding.EditText2.setText(value.url)
//        MaterialAlertDialogBuilder(requireContext())
//            .setView(dialogBinding.root)
//            .setTitle(R.string.edit_site)
//            .setNegativeButton(R.string.cancel, null)
//            .setPositiveButton(R.string.save) { _, _ ->
//                val title = dialogBinding.EditText1.text.toString().trim()
//                val url = dialogBinding.EditText2.text.toString().trim()
//                if (title.isNotEmpty() && url.isNotEmpty()) {
//                    model.updateLink(value, title, url)
//                }
//            }.show()
//    }
//
//    private fun confirmDeletion(value: Link) {
//        MaterialAlertDialogBuilder(requireContext())
//            .setMessage(R.string.confirm_delete)
//            .setPositiveButton(R.string.delete) { _, _ -> model.deleteLink(value) }
//            .setNegativeButton(R.string.cancel, null)
//            .show()
//    }
//
//    private fun onAddButtonClick() {
//        val dialogBinding = DialogInputBinding.inflate(layoutInflater)
//        dialogBinding.EditText2.setText(R.string.prefix)
//        MaterialAlertDialogBuilder(requireContext())
//            .setView(dialogBinding.root)
//            .setTitle(R.string.add_site)
//            .setNegativeButton(R.string.cancel, null)
//            .setPositiveButton(R.string.save) { _, _ ->
//                val title = dialogBinding.EditText1.text.toString().trim()
//                val url = dialogBinding.EditText2.text.toString().trim()
//                if (title.isNotEmpty() && url.isNotEmpty()) {
//                    model.insertLink(title = title, url = url)
//                }
//            }.show()
//    }
//
//    private fun ChipRoundBinding.setup(funcChipInfo: FuncChipInfo) {
//        Icon.setImageResource(funcChipInfo.icon)
//        Title.setText(funcChipInfo.title)
//    }
//
//    @SuppressLint("ClickableViewAccessibility")
//    private fun setupIndicators(imageList: List<BiliVideo>) {
//        val indicatorLayout = binding.BannerIdc
//        imageList.forEach { _ ->
//            val indicator = ImageView(requireContext()).apply {
//                setImageResource(R.drawable.dot_unselected)
//                layoutParams = LinearLayout.LayoutParams(20.dp, 20.dp).apply {
//                    setMargins(8.dp, 0, 8.dp, 0)
//                }
//            }
//            indicatorLayout.addView(indicator)
//        }
//
//        // 同步指示器与手动滑动
//        binding.TitleBanner.registerOnPageChangeCallback(object :
//            ViewPager2.OnPageChangeCallback() {
//            override fun onPageSelected(position: Int) {
//                updateIndicators(position % imageList.size)
//            }
//        })
//    }
//
//    //TODO:??
//    private fun updateIndicators(position: Int) {
//        val indicatorLayout = binding.BannerIdc
//        for (i in 0 until indicatorLayout.childCount) {
//            (indicatorLayout.getChildAt(i) as ImageView).setImageResource(
//                if (i == position) R.drawable.dot_selected else R.drawable.dot_unselected
//            )
//        }
//    }
//
//    private val Int.dp: Int get() = (this * resources.displayMetrics.density).toInt()
//
//    @SuppressLint("QueryPermissionsNeeded")
//    private fun openAnotherApp(packageName: String) {
//        val packageManager = requireActivity().packageManager
//        val launchIntent = packageManager.getLaunchIntentForPackage(packageName)
//        if (launchIntent != null) {
//            startActivity(launchIntent)
//        } else {
//            Toaster.show("未检测到游戏安装")
//        }
//    }
//}

class Home : Fragment() {

    private val prefManager: PrefManager by getKoin().inject()
    private val viewModel: MainModel by activityViewModels()
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var linkAdapter: LinkGridAdapter
    private lateinit var accountAdapter: AccountAdapter
    private lateinit var dialogController: HomeDialogController
    private lateinit var activityResultLauncher: ActivityResultLauncher<Intent>
    private lateinit var bannerController: HomeBannerController

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

    private fun initController() {
        // 初始化 banner 控制器
        bannerController = HomeBannerController(
            fragment = this,
            viewPager = binding.TitleBanner,
            indicatorLayout = binding.BannerIdc
        )
        //初始化 dialog 控制器
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

                if (token != null && dId != null) {
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
                    renderLoginState(state)
                    renderAttendanceState(state)
                }
            }
        }
    }

    private fun renderAccount(state: MainUiState) {
        binding.CurrentAccount.text =
            state.currentAccountSk?.nickName ?: getString(R.string.no_login)

        accountAdapter.submitList(state.accountSkList) //TODO:
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

    private fun renderLoginState(state: MainUiState) {
        when (val loginState = state.loginState) {
            is ActionState.Success -> {
//                loginState.message?.let { Toaster.show(it) }
            }

            is ActionState.Error -> {
//                Toaster.show(loginState.message)
            }

            else -> Unit
        }
    }

    private fun renderAttendanceState(state: MainUiState) {
        when (val attendanceState = state.attendanceState) {
            is ActionState.Success -> {
                attendanceState.message?.let { Toaster.show(it) }
            }

            is ActionState.Error -> {
                Toaster.show(attendanceState.message)
            }

            else -> Unit
        }
    }

    private fun setupBinding() {
        binding.RealTimeData.setOnClickListener {
            startActivity(Intent(requireContext(), RealTimeActivity::class.java))
        }

        binding.CurrentAccount.setOnClickListener { view ->
            val accountList = viewModel.uiState.value.accountSkList
            if (accountList.isEmpty()) {
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
            val currAcc = viewModel.uiState.value.currentAccountSk
            if (currAcc != null && currAcc.official) {
                this.requireContext()
                    .launchApp("com.hypergryph.arknights") { Toaster.show("未检测到官服游戏安装") }
            } else {
                this.requireContext()
                    .launchApp("com.hypergryph.arknights.bilibili") { Toaster.show("未检测到b服游戏安装") }
            }
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
            Toaster.show("签到开始，可在通知栏查看进度")
            viewModel.startAttendance()
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

    private val linkListener = object : ItemListener {
        override fun onClick(position: Int) {
            linkAdapter.currentList[position]?.let { value ->
                try {
                    // openLink(requireContext(), value.url, prefManager)
                } catch (e: Exception) {
                    Toaster.show(getString(R.string.illegal_url))
                }
            }
        }

        override fun onLongClick(position: Int) {
            linkAdapter.currentList[position]?.let { value ->
                MenuDialog(requireContext())
                    .add(R.string.edit) { dialogController.showEditLinkDialog(value.copy()) }
                    .add(R.string.delete) { dialogController.showDeleteConfirm(value.copy()) }
                    .show()
            }
        }
    }

    private val accountListener = object : ItemListener {
        override fun onClick(position: Int) {
            accountAdapter.currentList[position]?.let { value ->
                viewModel.setDefaultAccountSk(value as AccountSk)
                dialogController.dismissPopup()
            }
        }

        override fun onLongClick(position: Int) = Unit
    }

    private fun CardApCacheBinding.bind(value: ApCache) {
        val now = getCurrentTs()
        val lastSyncStr = getLastUpdateStr(now - value.lastSyncTs).let {
            if (it.isEmpty()) "刚刚" else "${it}前"
        }

        LastSync.text = getString(R.string.last_sync_time, lastSyncStr)

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
        Title.setText(funcChipInfo.title)
    }


    override fun onDestroyView() {
        bannerController.release()
        dialogController.release()
        binding.ExLinks.adapter = null
        _binding = null
        _binding = null
        super.onDestroyView()
    }

}