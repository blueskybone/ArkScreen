package com.blueskybone.arkscreen.ui.fragment

import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.blueskybone.arkscreen.R
import com.blueskybone.arkscreen.databinding.ChipCardBinding
import com.blueskybone.arkscreen.databinding.FragmentCharBinding
import com.blueskybone.arkscreen.ui.recyclerview.CharAdapter
import com.blueskybone.arkscreen.ui.recyclerview.CharGridAdapter
import com.blueskybone.arkscreen.ui.recyclerview.ItemListener
import com.blueskybone.arkscreen.util.dpToPx
import com.blueskybone.arkscreen.util.getColorFromAttr
import com.blueskybone.arkscreen.viewmodel.CharModel
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup


/**
 *   Created by blueskybone
 *   Date: 2025/1/19
 */

class CharOwn : Fragment(), ItemListener {

    private val model: CharModel by activityViewModels()
    private var _binding: FragmentCharBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: CharAdapter
    private lateinit var adapter_new: CharGridAdapter


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        adapter = CharAdapter(requireContext(), 20)
        adapter_new = CharGridAdapter(requireContext(), 20)
        _binding = FragmentCharBinding.inflate(inflater)

        setupBinding()
        setupListener()
        return binding.root
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

        model.filterProf.observe(viewLifecycleOwner) { value ->
            binding.Profession.setup(CharModel.ProfFilter, value)
        }
        model.filterRarity.observe(viewLifecycleOwner) { value ->
            binding.Rarity.setup(CharModel.RarityFilter, value)
        }
        model.filterLevel.observe(viewLifecycleOwner) { value ->
            binding.Level.setup(CharModel.LevelFilter, value)
        }
        model.charsList.observe(viewLifecycleOwner) { value ->
            adapter.refreshData(value)
            binding.RecyclerView.scrollToPosition(0)
        }
    }

    private fun displayPopupWindow(filter: CharModel.Filter) {
        val popupWindow = PopupWindow(context)
        val view = layoutInflater.inflate(R.layout.pop_filter, null)
        val chipGroup = view.findViewById<ChipGroup>(R.id.FilterChipGroup)
        val context = requireContext()
        val entries = filter.getEntries(context)
        for (item in entries) {
            val chip = Chip(context)
            chip.text = item
            chip.setTextColor(getColorFromAttr(context, androidx.appcompat.R.attr.colorPrimary))
            chip.chipBackgroundColor = ColorStateList.valueOf(
                getColorFromAttr(
                    context, com.google.android.material.R.attr.colorBackgroundFloating
                )
            )
            chip.chipStrokeWidth = dpToPx(context, 1F)
            chip.chipStrokeColor = (ColorStateList.valueOf(
                getColorFromAttr(
                    context, androidx.appcompat.R.attr.colorPrimary
                )
            ))
            chip.setOnClickListener {
                model.setFilter(filter, item, context)
                if (popupWindow.isShowing) {
                    popupWindow.dismiss()
                }
            }
            chipGroup.addView(chip)
        }
        popupWindow.contentView = view
        popupWindow.isFocusable = true
        popupWindow.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        popupWindow.isOutsideTouchable = true
//        popupWindow.setOnDismissListener {
//
//        }
        popupWindow.showAsDropDown(binding.FilterBar)

        binding.FilterBar.setOnClickListener {
            if (popupWindow.isShowing) {
                popupWindow.dismiss()
            }
        }
    }

    private fun ChipCardBinding.setup(filter: CharModel.Filter, value: String) {
        val checked = filter.getEntryValues().indexOf(value)
        val entries = filter.getEntries(requireContext())
        Value.text = entries[checked]
        root.setOnClickListener {
            displayPopupWindow(filter)
        }
    }

    override fun onDestroy() {
        _binding = null
        super.onDestroy()
    }

    override fun onClick(position: Int) {

    }

    override fun onLongClick(position: Int) {

    }
}