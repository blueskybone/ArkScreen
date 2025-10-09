package com.blueskybone.arkscreen.ui.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.RecyclerView
import com.blueskybone.arkscreen.DataUiState
import com.blueskybone.arkscreen.R
import com.blueskybone.arkscreen.databinding.FragmentGachaBinding
import com.blueskybone.arkscreen.databinding.PopupAccountBinding
import com.blueskybone.arkscreen.preference.PrefManager
import com.blueskybone.arkscreen.room.AccountGc
import com.blueskybone.arkscreen.ui.recyclerview.AccountAdapter
import com.blueskybone.arkscreen.ui.recyclerview.GachaAdapter
import com.blueskybone.arkscreen.ui.recyclerview.ItemListener
import com.blueskybone.arkscreen.viewmodel.BaseModel
import com.blueskybone.arkscreen.viewmodel.GachaModel
import org.koin.android.ext.android.getKoin

class Gacha : Fragment() {

//    private val model: GachaModel by viewModels()
//    private val modelBase: BaseModel by viewModels()
    private val model : GachaModel by activityViewModels()
    private val modelBase: BaseModel by activityViewModels()
    private var adapter: GachaAdapter? = null

    private var accountPopup: PopupWindow? = null
    private var adapterAccount: AccountAdapter? = null
    private var _binding: FragmentGachaBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGachaBinding.inflate(inflater)
        adapterAccount = AccountAdapter(requireContext(), adapterGcListener)
        setupBinding()
        setUpObserver()
        return binding.root
    }

    private val adapterGcListener = object : ItemListener {
        @SuppressLint("NotifyDataSetChanged")
        override fun onClick(position: Int) {
            adapterAccount?.currentList?.get(position)?.let { value ->
                modelBase.setDefaultAccountGc(value as AccountGc)
                model.initialize()
                adapterAccount?.notifyDataSetChanged()
                accountPopup?.dismiss()
            }
        }

        override fun onLongClick(position: Int) {
        }
    }

    private fun setupBinding() {
        adapter = GachaAdapter(requireContext())
        binding.RecyclerView.adapter = adapter
        binding.RecyclerView.apply {
            // 设置固定大小，优化性能
            setHasFixedSize(true)

            // 禁用滚动
            overScrollMode = RecyclerView.OVER_SCROLL_NEVER
            isNestedScrollingEnabled = false
        }
        binding.Exchange.setOnClickListener { view ->
            showAccountPopup(view)
        }
    }

    private fun setUpObserver() {
        model.gachaData.observe(requireActivity()) { value ->
            adapter?.submitList(value)
        }
        model.uiState.observe(requireActivity()) { value ->
            when (value) {
                is DataUiState.Success -> displayView()
                else -> {}
            }
        }

        modelBase.accountGcList.observe(viewLifecycleOwner) { value ->
            adapterAccount?.submitList(value)
        }

        modelBase.currentAccountGc.observe(viewLifecycleOwner) { value ->
            if(value==null) return@observe
            binding.NickName.text = value.nickName
            if (value.official) binding.Icon.setImageResource(R.drawable.hg_icon_80x80)
            else binding.Icon.setImageResource(R.drawable.bili_icon_75x71)
        }
    }

    private fun displayView() {

        binding.CountSum.text = getString(R.string.gacha_count, model.finalCountSum)
        binding.Rarity6.text = model.rarity6Count.toString()
        binding.AverageCount.text =
            if (model.rarity6Count == 0) "-" else getString(
                R.string.gacha_count,
                model.finalCountSum / model.rarity6Count
            )
        binding.NormalCount.text = getString(R.string.gacha_count, model.poolCountNormal)
        binding.FesCount.text = getString(R.string.gacha_count, model.poolCountFes)
        binding.CoreCount.text = getString(R.string.gacha_count, model.poolCountCore)
        binding.DateRange.text = getString(R.string.date_range, model.dateRange)
    }

    private fun showAccountPopup(anchor: View) {
        if (accountPopup?.isShowing == true) return
        val activity = requireActivity()
        // 初始化弹窗
        val binding = PopupAccountBinding.inflate(LayoutInflater.from(requireContext()))
        val popupView = binding.root  // 获取根布局
        binding.lvAccount.adapter = adapterAccount

        accountPopup = PopupWindow(
            popupView,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            true
        ).apply {
            setBackgroundDrawable(
                ContextCompat.getDrawable(
                    requireContext(),
                    android.R.color.transparent
                )
            )
            isOutsideTouchable = true
            animationStyle = R.style.PopupDownAnim
            setOnDismissListener {
                activity.window?.attributes = activity.window?.attributes?.apply {
                    this.alpha = 1.0f
                }
            }
        }
        // 显示弹窗前调整背景透明度
        activity.window?.attributes = activity.window?.attributes?.apply {
            alpha = 0.7f
        }
        // 计算弹窗位置，显示在锚点下方
        val location = IntArray(2)
        anchor.getLocationOnScreen(location)
        val x = location[0]
        val y = location[1] + anchor.height

        // 显示弹窗，可根据需要调整x和y的偏移量
        accountPopup?.showAtLocation(anchor, Gravity.NO_GRAVITY, x, y)
    }
}