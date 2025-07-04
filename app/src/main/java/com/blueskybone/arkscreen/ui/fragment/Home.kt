package com.blueskybone.arkscreen.ui.fragment


import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Rect
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.blueskybone.arkscreen.R
import com.blueskybone.arkscreen.common.MenuDialog
import com.blueskybone.arkscreen.databinding.CardApCacheBinding
import com.blueskybone.arkscreen.databinding.ChipRoundBinding
import com.blueskybone.arkscreen.databinding.DialogInfoBinding
import com.blueskybone.arkscreen.databinding.DialogInputBinding
import com.blueskybone.arkscreen.databinding.FragmentHomeBinding
import com.blueskybone.arkscreen.network.BiliVideo
import com.blueskybone.arkscreen.playerinfo.ApCache
import com.blueskybone.arkscreen.preference.PrefManager
import com.blueskybone.arkscreen.room.Link
import com.blueskybone.arkscreen.ui.activity.CharAssets
import com.blueskybone.arkscreen.ui.activity.LoginWeb
import com.blueskybone.arkscreen.ui.activity.RealTimeActivity
import com.blueskybone.arkscreen.ui.activity.RecruitActivity
import com.blueskybone.arkscreen.ui.activity.WebViewActivity
import com.blueskybone.arkscreen.ui.bindinginfo.Attendance
import com.blueskybone.arkscreen.ui.bindinginfo.FuncChipInfo
import com.blueskybone.arkscreen.ui.bindinginfo.GachaStat
import com.blueskybone.arkscreen.ui.bindinginfo.OpeAssets
import com.blueskybone.arkscreen.ui.bindinginfo.RecruitCal
import com.blueskybone.arkscreen.ui.recyclerview.ItemListener
import com.blueskybone.arkscreen.ui.recyclerview.LinkGridAdapter
import com.blueskybone.arkscreen.ui.recyclerview.viewpager.ImagePagerAdapter
import com.blueskybone.arkscreen.util.TimeUtils.getCurrentTs
import com.blueskybone.arkscreen.util.TimeUtils.getLastUpdateStr
import com.blueskybone.arkscreen.util.TimeUtils.getRemainTimeStr
import com.blueskybone.arkscreen.viewmodel.BaseModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.hjq.toast.Toaster
import org.koin.android.ext.android.getKoin


/**
 *   Created by blueskybone
 *   Date: 2024/12/30
 */
class Home : Fragment() {

    private val prefManager: PrefManager by getKoin().inject()
    private val model: BaseModel by activityViewModels()
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private var adapter: LinkGridAdapter? = null
    private var adapterBanner: ImagePagerAdapter? = null
    private lateinit var activityResultLauncher: ActivityResultLauncher<Intent>

    private val adapterListener = object : ItemListener {
        override fun onClick(position: Int) {
            adapter?.currentList?.get(position)?.let { value ->
                try {
                    val url = value.url
                    if (prefManager.useInnerWeb.get()) {
                        val intent = Intent(requireContext(), WebViewActivity::class.java)
                        intent.putExtra("url", url)
                        startActivity(intent)
                    } else {
                        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                    }
                } catch (e: Exception) {
                    Toaster.show(getString(R.string.illegal_url))
                    e.printStackTrace()
                }
            }
        }
        override fun onLongClick(position: Int) {
            adapter?.currentList?.get(position)?.let { value ->
                MenuDialog(requireContext())
                    .add(R.string.edit) { displayEditDialog(value.copy()) }
                    .add(R.string.delete) { confirmDeletion(value) }
                    .show()
            }
        }
    }

    private val bannerListener = object : ItemListener {
        override fun onClick(position: Int) {
            adapterBanner?.getItem(position)?.let { value ->
                try {
                    val url = "bilibili://video/${value.bvid}"
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                    intent.setPackage("tv.danmaku.bili")
                    startActivity(intent)
                } catch (e: java.lang.Exception) {
                    val bilibili = "https://bilibili.com/video/${value.bvid}"
                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(bilibili)))
                }
            }
        }
        override fun onLongClick(position: Int) {

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater)
        adapter = LinkGridAdapter(adapterListener)
        initialize()
        setupBinding()
        setupObserver()
        return binding.root
    }

    private fun initialize() {
        activityResultLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            // 处理返回结果
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                // 解析返回的数据
                val token = data?.getStringExtra("token")
                val dId = data?.getStringExtra("dId")
                if (token != null && dId != null) {
                    Toaster.show(getString(R.string.getting_info))
                    model.accountSkLogin(token, dId)
                } else {
                    Toaster.show("null")
                }
            }
        }
    }

    private fun setupObserver() {
        model.currentAccount.observe(viewLifecycleOwner) { value ->
            if (value == null) binding.CurrentAccount.text = getString(R.string.no_login)
            else binding.CurrentAccount.text = value.nickName
        }
        model.announce.observe(viewLifecycleOwner) { value ->
            binding.Announce.text = value
        }
        model.links.observe(viewLifecycleOwner) { value ->
            adapter?.submitList(value)
            if (value.isEmpty()) {
                binding.LinkPreference.visibility = View.GONE
            } else {
                binding.LinkPreference.visibility = View.VISIBLE
            }
        }
        model.biliVideo.observe(viewLifecycleOwner) { value ->
            adapterBanner = ImagePagerAdapter(bannerListener, value)
            binding.TitleBanner.adapter = adapterBanner
            setupIndicators(value)
            binding.TitleBanner.isUserInputEnabled = true
        }
    }


    private fun setupBinding() {
        binding.RealTimeData.setOnClickListener {
            startActivity(Intent(requireContext(), RealTimeActivity::class.java))
        }

        binding.RefreshGame.setOnClickListener{
            //
        }

        binding.CurrentAccount.setOnClickListener {
            model.checkAnnounce()
            model.accountSkList.value!!.let {
                if (it.isEmpty()) {
                    MenuDialog(requireContext())
                        .add(getString(R.string.import_cookie)) {
                            displayLoginDialog()
                        }
                        .add(R.string.web_login) {
                            val intent =
                                LoginWeb.startIntent(
                                    requireContext(),
                                    LoginWeb.Companion.LoginType.SKLAND
                                )
                            activityResultLauncher.launch(intent)
                        }
                        .show()

                } else {
                    val menuDialog = MenuDialog(requireContext())
                    for (account in model.accountSkList.value!!) {
                        menuDialog.add(account.nickName) {
                            model.setDefaultAccountSk(account)
                        }
                    }
                    menuDialog.show()
                }
            }
        }

        binding.Recruit.setOnClickListener {
            startActivity(Intent(requireContext(), RecruitActivity::class.java))
        }
        binding.TitleImage.setOnClickListener {
            if (prefManager.baseAccountSk.get().official)
                openAnotherApp("com.hypergryph.arknights")
            else
                openAnotherApp("com.hypergryph.arknights.bilibili")
        }

        binding.RecruitCalc.setup(RecruitCal)
        binding.OpeAssets.setup(OpeAssets)
        binding.GachaStat.setup(GachaStat)
        binding.Attendance.setup(Attendance)

        binding.RecruitCalc.Layout.setOnClickListener {
            startActivity(Intent(requireContext(), RecruitActivity::class.java))
        }
        binding.OpeAssets.Layout.setOnClickListener {
            startActivity(Intent(requireContext(), CharAssets::class.java))
        }
        binding.GachaStat.Layout.setOnClickListener {
            Toaster.show("施工中...")
        }
        binding.AddLink.setOnClickListener {
            onAddButtonClick()
        }
        binding.Attendance.Layout.setOnClickListener {
            Toaster.show("签到开始，可在通知栏查看进度")
            model.startAttendance(requireContext())
            //displayAttendanceDialog()
        }

        binding.ExLinks.layoutManager = GridLayoutManager(requireContext(), 4)
        binding.ExLinks.adapter = adapter
        binding.ExLinks.addItemDecoration(object : RecyclerView.ItemDecoration() {
            override fun getItemOffsets(
                outRect: Rect,
                view: View,
                parent: RecyclerView,
                state: RecyclerView.State
            ) {
                super.getItemOffsets(outRect, view, parent, state)
                val params = view.layoutParams as RecyclerView.LayoutParams
                params.width = parent.width / 4
                view.layoutParams = params
            }
        })

//        CoroutineScope(Dispatchers.IO).launch {
//            try {
//                val path = getSpaceTitleImageUrl()
//                binding.TitleImage.load(path) {
//                    crossfade(true)
//                    crossfade(300)
//                }
//            } catch (e: Exception) {
//                e.printStackTrace()
//            }
//        }

        model.apCache.observe(viewLifecycleOwner) { value ->
            if (!value.isnull) {
                binding.ApCacheCard.bind(value)
            }
        }

        if (!prefManager.showHomeAnnounce.get()) {
            binding.AnnounceTitle.visibility = View.GONE
            binding.AnnounceCard.visibility = View.GONE
        }
    }

    private fun displayLoginDialog() {
        val dialogBinding = DialogInputBinding.inflate(layoutInflater)
        dialogBinding.EditText2.visibility = View.GONE
        dialogBinding.EditText1.hint = getString(R.string.import_cookie)
        MaterialAlertDialogBuilder(requireContext())
            .setView(dialogBinding.root)
            .setTitle(R.string.import_cookie)
            .setNegativeButton(R.string.cancel, null)
            .setPositiveButton(R.string.import_cookie) { _, _ ->
                val str = dialogBinding.EditText1.text.toString()
                val list = str.split("@")
                if (list.size == 2) {
                    try {
                        Toaster.show(getString(R.string.getting_info))
                        model.accountSkLogin(list[0], list[1])
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                } else {
                    Toaster.show(getString(R.string.wrong_format))
                }
            }.show()
    }

    @SuppressLint("QueryPermissionsNeeded")
    private fun openAnotherApp(packageName: String) {
        val packageManager = requireActivity().packageManager
        val launchIntent = packageManager.getLaunchIntentForPackage(packageName)
        if (launchIntent != null) {
            startActivity(launchIntent)
        } else {
            Toaster.show("未检测到游戏安装")
        }
    }

    private fun CardApCacheBinding.bind(value: ApCache) {
        val now = getCurrentTs()
        val lastSyncStr = getLastUpdateStr(now - value.lastSyncTs).let {
            if (it.isEmpty()) "刚刚" else "${it}前"
        }


        this.LastSync.text = getString(R.string.last_sync_time, lastSyncStr)
        if (value.current >= value.max) {
            this.Current.text = value.current.toString()
            this.RestTime.text = getString(R.string.recovered)
        } else if (now > value.recoverTime) {
            this.Current.text = value.max.toString()
            this.RestTime.text = getString(R.string.recovered)
        } else {
            val currentStr =
                (value.max - ((value.recoverTime - now).toInt() / (60 * 6) + 1)).toString()
            this.Current.text = currentStr
            this.RestTime.text = getRemainTimeStr(value.recoverTime - now)
        }
        val maxText = "/" + value.max.toString()
        this.Max.text = maxText
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    private fun displayEditDialog(value: Link) {
        val dialogBinding = DialogInputBinding.inflate(layoutInflater)
        dialogBinding.EditText1.setText(value.title)
        dialogBinding.EditText2.setText(value.url)
        MaterialAlertDialogBuilder(requireContext())
            .setView(dialogBinding.root)
            .setTitle(R.string.edit_site)
            .setNegativeButton(R.string.cancel, null)
            .setPositiveButton(R.string.save) { _, _ ->
                val title = dialogBinding.EditText1.text.toString().trim()
                val url = dialogBinding.EditText2.text.toString().trim()
                if (title.isNotEmpty() && url.isNotEmpty()) {
                    value.title = title
                    value.url = url
                    model.updateLink(value)
                }
            }.show()
    }

    private fun confirmDeletion(value: Link) {
        MaterialAlertDialogBuilder(requireContext())
            .setMessage(R.string.confirm_delete)
            .setPositiveButton(R.string.delete) { _, _ -> model.deleteLink(value) }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun onAddButtonClick() {
        val dialogBinding = DialogInputBinding.inflate(layoutInflater)
        dialogBinding.EditText2.setText(R.string.prefix)
        MaterialAlertDialogBuilder(requireContext())
            .setView(dialogBinding.root)
            .setTitle(R.string.add_site)
            .setNegativeButton(R.string.cancel, null)
            .setPositiveButton(R.string.save) { _, _ ->
                val title = dialogBinding.EditText1.text.toString().trim()
                val url = dialogBinding.EditText2.text.toString().trim()
                if (title.isNotEmpty() && url.isNotEmpty()) {
                    model.insertLink(Link(title = title, url = url))
                }
            }.show()
    }

    private fun displayAttendanceDialog() {
        val dialogBinding = DialogInfoBinding.inflate(layoutInflater)
        MaterialAlertDialogBuilder(requireContext())
            .setView(dialogBinding.root)
            .setTitle(R.string.attendance_click)
            .setPositiveButton(R.string.confirm, null).show()

        model.runAttendance()
        model.attendanceLog.observe(viewLifecycleOwner) {
            dialogBinding.Text.text = it
        }
    }

    private fun ChipRoundBinding.setup(funcChipInfo: FuncChipInfo) {
        Icon.setImageResource(funcChipInfo.icon)
        Title.setText(funcChipInfo.title)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupIndicators(imageList:List<BiliVideo>) {
        val indicatorLayout =binding.BannerIdc
        imageList.forEach { _ ->
            val indicator = ImageView(requireContext()).apply {
                setImageResource(R.drawable.dot_unselected)
                layoutParams = LinearLayout.LayoutParams(20.dp, 20.dp).apply {
                    setMargins(8.dp, 0, 8.dp, 0)
                }
            }
            indicatorLayout.addView(indicator)
        }

        // 同步指示器与手动滑动
        binding.TitleBanner.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                updateIndicators(position % imageList.size)
            }
        })
    }

    private fun updateIndicators(position: Int) {
        val indicatorLayout = binding.BannerIdc
        for (i in 0 until indicatorLayout.childCount) {
            (indicatorLayout.getChildAt(i) as ImageView).setImageResource(
                if (i == position) R.drawable.dot_selected else R.drawable.dot_unselected
            )
        }
    }

    private val Int.dp: Int get() = (this * resources.displayMetrics.density).toInt()
}