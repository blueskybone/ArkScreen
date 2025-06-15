package com.blueskybone.arkscreen.fragment


import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Rect
import android.icu.text.CaseMap.Title
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.blueskybone.arkscreen.R
import com.blueskybone.arkscreen.activity.AboutActivity
import com.blueskybone.arkscreen.activity.AccountSk
import com.blueskybone.arkscreen.activity.CharAssets
import com.blueskybone.arkscreen.activity.RealTimeActivity
import com.blueskybone.arkscreen.activity.RecruitActivity
import com.blueskybone.arkscreen.activity.WebViewActivity
import com.blueskybone.arkscreen.bindinginfo.Attendance
import com.blueskybone.arkscreen.bindinginfo.FuncChipInfo
import com.blueskybone.arkscreen.bindinginfo.GachaStat
import com.blueskybone.arkscreen.bindinginfo.OpeAssets
import com.blueskybone.arkscreen.bindinginfo.RecruitCal
import com.blueskybone.arkscreen.common.MenuDialog
import com.blueskybone.arkscreen.databinding.CardApCacheBinding
import com.blueskybone.arkscreen.databinding.ChipRoundBinding
import com.blueskybone.arkscreen.databinding.DialogInfoBinding
import com.blueskybone.arkscreen.databinding.DialogInputBinding
import com.blueskybone.arkscreen.databinding.FragmentHomeBinding
import com.blueskybone.arkscreen.network.NetWorkUtils
import com.blueskybone.arkscreen.preference.PrefManager
import com.blueskybone.arkscreen.recyclerview.ItemListener
import com.blueskybone.arkscreen.recyclerview.LinkGridAdapter
import com.blueskybone.arkscreen.recyclerview.LinkHomeAdapter
import com.blueskybone.arkscreen.room.ApCache
import com.blueskybone.arkscreen.room.Link
import com.blueskybone.arkscreen.util.TimeUtils.getCurrentTs
import com.blueskybone.arkscreen.util.TimeUtils.getLastUpdateStr
import com.blueskybone.arkscreen.util.TimeUtils.getRemainTimeStr
import com.blueskybone.arkscreen.util.saveDrawableToGallery
import com.blueskybone.arkscreen.viewmodel.BaseModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.hjq.toast.Toaster
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.getKoin
import timber.log.Timber
import java.net.URL


/**
 *   Created by blueskybone
 *   Date: 2024/12/30
 */
class Home : Fragment(), ItemListener {

    private val prefManager: PrefManager by getKoin().inject()
    private val model: BaseModel by activityViewModels()
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private var adapter: LinkHomeAdapter? = null
    private var adapter_new: LinkGridAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        print("这这对吗")
        _binding = FragmentHomeBinding.inflate(inflater)
        adapter = LinkHomeAdapter(this)
        adapter_new = LinkGridAdapter(this)
        setupBinding()
        setupObserver()
        return binding.root
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
            adapter_new?.submitList(value)
            if (value.isEmpty()) {
                binding.LinkPreference.visibility = View.GONE
            } else {
                binding.LinkPreference.visibility = View.VISIBLE
            }
        }
    }


    private fun setupBinding() {
        binding.AboutText.setOnClickListener {
            startActivity(Intent(requireContext(), AboutActivity::class.java))
        }
        binding.RealTimeData.setOnClickListener {
            startActivity(Intent(requireContext(), RealTimeActivity::class.java))
        }

        binding.CurrentAccount.setOnClickListener {
            model.checkAnnounce()
            model.accountSkList.value!!.let {
                if (it.isEmpty()) {
                    startActivity(Intent(requireContext(), AccountSk::class.java))
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
            displayAttendanceDialog()
        }

        binding.ExLinks.layoutManager = GridLayoutManager(requireContext(), 4)
        binding.ExLinks.adapter = adapter_new
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

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val path = NetWorkUtils.getSpaceTitleImageUrl()
                print("try get space title image url")
//                val path = getSpaceTitleImageUrl()
                print(path)
                binding.TitleImage.load(path) {
                    crossfade(true)
                    crossfade(300)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

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
        val lastSyncStr = getLastUpdateStr(now - value.lastSyncTs)
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

    override fun onClick(position: Int) {
        adapter_new?.currentList?.get(position)?.let { value ->
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
        adapter_new?.currentList?.get(position)?.let { value ->
            MenuDialog(requireContext())
                .add(R.string.edit) { displayEditDialog(value.copy()) }
                .add(R.string.delete) { confirmDeletion(value) }
                .show()
        }
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
}