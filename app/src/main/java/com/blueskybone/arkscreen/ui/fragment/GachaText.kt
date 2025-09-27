package com.blueskybone.arkscreen.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.RecyclerView
import com.blueskybone.arkscreen.databinding.FragmentGachaTextBinding
import com.blueskybone.arkscreen.ui.model.GachaInfo
import com.blueskybone.arkscreen.ui.recyclerview.GachaTextAdapter
import com.blueskybone.arkscreen.viewmodel.BaseModel
import com.blueskybone.arkscreen.viewmodel.GachaModel

class GachaText : Fragment() {
    private val model : GachaModel by activityViewModels()

    private lateinit var adapter: GachaTextAdapter

    private var _binding: FragmentGachaTextBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {

        adapter = GachaTextAdapter(requireContext(), 100)
        _binding = FragmentGachaTextBinding.inflate(inflater)
        setupBinding()
        setUpObserver()
        setupListener()
        return binding.root
    }

    private fun setupBinding() {
        binding.RecyclerView.adapter = adapter
    }

    private fun setUpObserver() {
        model.gachaRecordsCount.observe(requireActivity()) { value ->
            adapter.refreshData(value)
            binding.RecyclerView.scrollToPosition(0)
        }

        model.gachaInfoList.observe(requireActivity()) { value ->
            setupSpinner(value)
        }
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

    private fun setupSpinner(gachaInfo: List<GachaInfo>) {
        val dataText = gachaInfo.map { item -> item.poolName }
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,  // 默认布局
            dataText
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)  // 下拉项布局

        // 设置适配器
        val spinner: Spinner = binding.Spinner
        spinner.adapter = adapter

        // 设置选择监听器
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val info = gachaInfo[position]
                model.postPoolGachaList(info.poolId)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }
        // 设置默认选择（可选）
        // 选择第一项,并触发监听器
        spinner.setSelection(0)
    }
}