package com.blueskybone.arkscreen.fragment


import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import coil.load
import com.blueskybone.arkscreen.R
import com.blueskybone.arkscreen.activity.AboutActivity
import com.blueskybone.arkscreen.activity.AccountSk
import com.blueskybone.arkscreen.activity.RealTimeActivity
import com.blueskybone.arkscreen.activity.WebViewActivity
import com.blueskybone.arkscreen.common.MenuDialog
import com.blueskybone.arkscreen.databinding.CardApCacheBinding
import com.blueskybone.arkscreen.databinding.FragmentHomeBinding
import com.blueskybone.arkscreen.network.NetWorkUtils
import com.blueskybone.arkscreen.preference.PrefManager
import com.blueskybone.arkscreen.recyclerview.ItemListener
import com.blueskybone.arkscreen.recyclerview.LinkHomeAdapter
import com.blueskybone.arkscreen.room.ApCache
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
import org.koin.android.ext.android.getKoin


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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater)
        adapter = LinkHomeAdapter(this)
        setupBinding()
        setupObserver()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }


    private fun setupObserver() {
        model.currentAccount.observe(viewLifecycleOwner) { value ->
            if (value == null) binding.CurrentAccount.text = getString(R.string.no_login)
            else binding.CurrentAccount.text = value.nickName
        }
        model.announce.observe(viewLifecycleOwner) { value ->
            binding.Announce.text = value
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

        binding.Donate.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.donate)
                .setMessage(R.string.donate_msg)
                .setNegativeButton(R.string.cancel, null)
                .setNeutralButton(R.string.donated) { _, _ -> Toaster.show(getString(R.string.thank_for_donate)) }
                .setPositiveButton(R.string.save_code) { _, _ ->
                    saveDrawableToGallery(requireContext(), R.drawable.wechat)
                    saveDrawableToGallery(requireContext(), R.drawable.zfb)
                    Toaster.show("已保存到本地")
                }.show()
        }

        binding.Manual.setOnClickListener {
            val cvId = "40623349"
            try {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("bilibili://article/$cvId"))
                startActivity(intent)
            } catch (e: Exception) {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.bilibili.com/read/cv$cvId"))
                startActivity(intent)
            }
        }

        binding.ExLinks.adapter = adapter
        model.links.observe(viewLifecycleOwner) { value ->
            adapter?.submitList(value)
            if (value.isEmpty()) {
                binding.LinkPreference.visibility = View.GONE
            } else {
                binding.LinkPreference.visibility = View.VISIBLE
            }
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val path = NetWorkUtils.getSpaceTitleImageUrl()
                binding.TitleImage.load(path)
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

    private fun CardApCacheBinding.bind(value: ApCache) {
        val maxText = "/" + value.max.toString()
        this.Max.text = maxText
        val currentTs = getCurrentTs()
        val passTime = currentTs - value.lastUpdateTs
        val passSyncTime = currentTs - value.lastSyncTs
        val lastSyncStr = getLastUpdateStr(passSyncTime)
        this.LastSync.text = getString(R.string.last_sync_time, lastSyncStr)
        if (value.current >= value.max) {
            this.Current.text = value.current.toString()
            this.RestTime.text = getString(R.string.recovered)
        } else if (value.remainSec < passTime) {
            this.Current.text = value.max.toString()
            this.RestTime.text = getString(R.string.recovered)
        } else {
            val currentStr = (value.max - ((value.recoverTime - currentTs).toInt() / (60 * 6) + 1)).toString()
//            val currentStr = (passTime.toInt() / (60 * 6) + value.current).toString()
            this.Current.text = currentStr
            this.RestTime.text = getRemainTimeStr(value.remainSec - passTime)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

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

    }
}