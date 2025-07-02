package com.blueskybone.arkscreen.ui.fragment

import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.PopupWindow
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.blueskybone.arkscreen.R
import com.blueskybone.arkscreen.common.getFlowLayout
import com.blueskybone.arkscreen.common.setTagLayout
import com.blueskybone.arkscreen.databinding.ChipCardBinding
import com.blueskybone.arkscreen.databinding.FragmentCharBinding
import com.blueskybone.arkscreen.ui.recyclerview.CharAdapter
import com.blueskybone.arkscreen.ui.recyclerview.ItemListener
import com.blueskybone.arkscreen.util.dpToPx
import com.blueskybone.arkscreen.util.getColorFromAttr
import com.blueskybone.arkscreen.viewmodel.CharModel
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.nex3z.flowlayout.FlowLayout


/**
 *   Created by blueskybone
 *   Date: 2025/1/19
 */

class CharOwn : Fragment(), ItemListener {

    private val model: CharModel by activityViewModels()
    private var _binding: FragmentCharBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: CharAdapter
//    private lateinit var adapter_new: CharGridAdapter


    private val profList = listOf("先锋", "近卫", "狙击", "重装", "术师", "医疗", "辅助", "特种")
    private val levelList = listOf("精英二", "精英一", "精英零")
    private val rarityList = listOf("6★", "5★", "4★", "1~3★")

    private val layoutList = listOf(profList, levelList, rarityList)

    private val buttonViewList: MutableList<Button> = ArrayList()
    private val flowLayoutList: MutableList<FlowLayout> = ArrayList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        adapter = CharAdapter(requireContext(), 20)
//        adapter_new = CharGridAdapter(requireContext(), 20)
        _binding = FragmentCharBinding.inflate(inflater)

        setupBinding()
        setButtonLayout()
        setupListener()
        return binding.root
    }


    private fun setButtonLayout() {
        val linearLayout = binding.ButtonLayout
        linearLayout.removeAllViews()
        for (buttonList in layoutList) {
            val flowLayout = getFlowLayout(requireContext())
            for (button in buttonList) {
                val buttonView = tagButton(button)
                buttonViewList.add(buttonView)
                flowLayout.addView(buttonView)
            }
            flowLayoutList.add(flowLayout)
        }
        for (flowLayout in flowLayoutList) {
            linearLayout.addView(flowLayout)
        }
    }

    private fun tagButton(text: String): Button {
        val button = Button(requireContext())
        button.setTagLayout(text)
        button.setBackgroundResource(R.drawable.button_tag)
        button.setOnClickListener {
            button.isSelected = !button.isSelected
//            try {
//                val tags = ArrayList<String>()
//                for (buttonView in buttonViewList) {
//                    if (buttonView.isSelected) {
//                        tags.add(buttonView.text.toString())
//                    }
//                }
//                model.startCalculate(tags)
//            } catch (e: Exception) {
//                e.printStackTrace()
//            }
        }
        return button
    }

    private fun setupListener() {
        val rv = binding.RecyclerView
        rv.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (!recyclerView.canScrollVertically(1)) {
                    recyclerView.post { adapter.loadMoreData() }
                }
            }
        })
    }

    private fun setupBinding() {

        binding.RecyclerView.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.RecyclerView.adapter = adapter

//        binding.RecyclerView.adapter = adapter_new
        binding.RecyclerView.addItemDecoration(object : RecyclerView.ItemDecoration() {
            override fun getItemOffsets(
                outRect: Rect,
                view: View,
                parent: RecyclerView,
                state: RecyclerView.State
            ) {
                super.getItemOffsets(outRect, view, parent, state)
                val params = view.layoutParams as RecyclerView.LayoutParams
                params.width = parent.width / 2
                view.layoutParams = params
            }
        })
        binding.Filter.setOnClickListener {
            if (binding.ButtonLayout.visibility == View.GONE) {
                binding.ButtonLayout.visibility = View.VISIBLE
                binding.FrameDialog.visibility =  View.VISIBLE
                binding.ButtonLayout.requestFocus()
            } else {
                binding.ButtonLayout.visibility = View.GONE
                binding.FrameDialog.visibility =  View.GONE
            }
        }


        // 保留原有的焦点监听作为备用
        binding.ButtonLayout.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus && binding.ButtonLayout.visibility == View.VISIBLE) {
                binding.ButtonLayout.visibility = View.GONE
                binding.FrameDialog.visibility =  View.GONE
            }
        }

        binding.ButtonLayout.setOnClickListener {
            // 空实现，拦截点击，防止点击事件穿透
        }
        binding.FrameDialog.setOnClickListener{
            binding.ButtonLayout.visibility = View.GONE
            binding.FrameDialog.visibility =  View.GONE
        }

        model.charsList.observe(viewLifecycleOwner) { value ->
            adapter.refreshData(value)
            binding.RecyclerView.scrollToPosition(0)
        }
    }

//    private fun displayPopupWindow(filter: CharModel.Filter) {
//    }

//    private fun ChipCardBinding.setup(filter: CharModel.Filter, value: String) {
//        val checked = filter.getEntryValues().indexOf(value)
//        val entries = filter.getEntries(requireContext())
//        Value.text = entries[checked]
//        root.setOnClickListener {
//            displayPopupWindow(filter)
//        }
//    }

    override fun onDestroy() {
        _binding = null
        super.onDestroy()
    }

    override fun onClick(position: Int) {

    }

    override fun onLongClick(position: Int) {

    }
}