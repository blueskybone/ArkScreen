package com.blueskybone.arkscreen.ui.fragment

import android.graphics.Rect
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.blueskybone.arkscreen.R
import com.blueskybone.arkscreen.common.FlowRadioGroup
import com.blueskybone.arkscreen.common.getFlowRadioGroup
import com.blueskybone.arkscreen.common.profImageButton
import com.blueskybone.arkscreen.common.tagButton
import com.blueskybone.arkscreen.databinding.FragmentCharBinding
import com.blueskybone.arkscreen.ui.recyclerview.CharAdapter
import com.blueskybone.arkscreen.ui.recyclerview.ItemListener
import com.blueskybone.arkscreen.viewmodel.CharModel


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

    private val profList =
        listOf("PIONEER", "WARRIOR", "TANK", "SNIPER", "CASTER", "MEDIC", "SUPPORT", "SPECIAL")
    private val profListIcon = listOf(
        R.drawable.icon_pioneer,
        R.drawable.icon_warrior,
        R.drawable.icon_tank,
        R.drawable.icon_sniper,
        R.drawable.icon_caster,
        R.drawable.icon_medic,
        R.drawable.icon_support,
        R.drawable.icon_special
    )
    private val levelList = listOf("精零", "精一", "精二")
    private val rarityList = listOf("1~3★", "4★", "5★", "6★")

    private lateinit var profRadioGroup: FlowRadioGroup
    private lateinit var levelRadioGroup: FlowRadioGroup
    private lateinit var rarityRadioGroup: FlowRadioGroup

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        adapter = CharAdapter(requireContext(), 24)
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
        profRadioGroup = getFlowRadioGroup(requireContext())
        for ((idx, prof) in profList.withIndex()) {
            val but = profImageButton(requireContext(), profListIcon[idx], prof)
            profRadioGroup.addView(but)
        }
        linearLayout.addView(profRadioGroup)
        levelRadioGroup = getFlowRadioGroup(requireContext())
        for (level in levelList) {
            val but = tagButton(requireContext(), level)
            levelRadioGroup.addView(but)
        }
        linearLayout.addView(levelRadioGroup)
        rarityRadioGroup = getFlowRadioGroup(requireContext())
        for (rarity in rarityList) {
            val but = tagButton(requireContext(), rarity)
            rarityRadioGroup.addView(but)
        }
        linearLayout.addView(rarityRadioGroup)
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
                binding.FrameDialog.visibility = View.VISIBLE
                binding.ButtonLayout.requestFocus()
            } else {
                binding.ButtonLayout.visibility = View.GONE
                binding.FrameDialog.visibility = View.GONE

                //TODO: model.sublime.filter
                submitFilter()
            }
        }

        binding.ButtonLayout.setOnClickListener {
            // 空实现，拦截点击，防止点击事件穿透
        }
        binding.FrameDialog.setOnClickListener {
            binding.ButtonLayout.visibility = View.GONE
            binding.FrameDialog.visibility = View.GONE

            //TODO: model.sublime.filter
            submitFilter()
        }

        model.charsList.observe(viewLifecycleOwner) { value ->
            adapter.refreshData(value)
            binding.RecyclerView.scrollToPosition(0)
        }
    }

    private fun submitFilter() {

        val id1 = profRadioGroup.getCheckedRadioButtonId()
        val filter1 = if (id1 != -1) {
            (profRadioGroup.findViewById<View>(id1) as? ImageButton)?.contentDescription?.toString() ?: "ALL"
        } else "ALL"

        val id2 = levelRadioGroup.getCheckedRadioButtonId()
        val filter2 = if (id2 != -1) {
          (levelRadioGroup.findViewById<View>(id2) as? Button)?.text?.toString() ?: "ALL"
        } else "ALL"

        val id3 = rarityRadioGroup.getCheckedRadioButtonId()
        val filter3 = if (id3 != -1) {
            (rarityRadioGroup.findViewById<View>(id3) as? Button)?.text?.toString() ?: "ALL"
        } else "ALL"

        model.applyFilterNew(filter1, filter2, filter3)
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