package com.blueskybone.arkscreen.ui.gacha

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.RecyclerView
import com.blueskybone.arkscreen.databinding.FragmentGachaTextBinding
import com.blueskybone.arkscreen.ui.gacha.adapter.GachaTextAdapter
import com.blueskybone.arkscreen.ui.gacha.model.Record
import kotlinx.coroutines.launch

class GachaTextFragment : Fragment() {
    private val model : GachaModel by activityViewModels()
    private lateinit var adapter: GachaTextAdapter
    private var _binding: FragmentGachaTextBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {

        adapter = GachaTextAdapter(requireContext(), 100)
        _binding = FragmentGachaTextBinding.inflate(inflater, container, false)
        setupBinding()
        setupListener()
        collectUiState()
        return binding.root
    }

    private fun setupBinding() {
        binding.RecyclerView.adapter = adapter
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

    private fun setupSpinner(recordList: List<Record>, poolNames: List<String>) {
        if (poolNames.isEmpty()) return
        val spinnerAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,  // 默认布局
            poolNames   //卡池名称作为数据
        )
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)  // 下拉项布局

        // 设置适配器
        val spinner: Spinner = binding.Spinner
        spinner.adapter = spinnerAdapter

        // 设置选择监听器
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val poolName = poolNames[position]
                val filteredRecords = recordList.filter { it.gachaPool == poolName }
                adapter.refreshData(filteredRecords)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }
        // 设置默认选择（可选），选择第一项,并触发监听器
        spinner.setSelection(0)
    }

    private fun collectUiState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                model.uiState.collect { state ->
                    renderState(state)
                    state.gachaUiSnapshot?.let{
                        setupSpinner(it.records, it.gachaPoolStats.map { item -> item.poolName })
                    }
                }
            }
        }
    }

    private fun renderState(gachaUiState: GachaUiState){
        if(gachaUiState.error != null){
            binding.root
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.RecyclerView.adapter = null
        _binding = null
    }
}