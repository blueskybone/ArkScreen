package com.blueskybone.arkscreen.ui.gacha

import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.RecyclerView
import com.blueskybone.arkscreen.R
import com.blueskybone.arkscreen.databinding.FragmentGachaTextBinding
import com.blueskybone.arkscreen.ui.gacha.adapter.GachaTextAdapter
import com.blueskybone.arkscreen.ui.gacha.model.GachaPoolStats
import com.blueskybone.arkscreen.ui.gacha.model.Record
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.activityViewModel

class GachaTextFragment : Fragment() {

    private val model: GachaModel by activityViewModel()
    private lateinit var adapter: GachaTextAdapter
    private var _binding: FragmentGachaTextBinding? = null
    private val binding get() = _binding!!

    private var records: List<Record> = emptyList()
    private var pools: List<GachaPoolStats> = emptyList()
    private var selectedPoolId: String = "ALL"
    private var pendingScrollState: Parcelable? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        adapter = GachaTextAdapter(requireContext(), PAGE_SIZE)
        pendingScrollState = savedInstanceState?.getParcelable(KEY_SCROLL_STATE)
        _binding = FragmentGachaTextBinding.inflate(inflater, container, false)
        setupBinding()
        collectUiState()
        return binding.root
    }

    private fun setupBinding() {
        binding.RecyclerView.adapter = adapter
        binding.RecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (!recyclerView.canScrollVertically(1) && adapter.hasMore) {
                    recyclerView.post { adapter.loadMoreData() }
                }
            }
        })

        binding.PoolDropdown.setOnItemClickListener { _, _, position, _ ->
            pools.getOrNull(position)?.let { pool ->
                model.selectPool(pool.poolId)
            }
        }
        binding.FilterSixStar.setOnCheckedChangeListener { _, _ ->
            updateFilters()
        }
        binding.FilterNew.setOnCheckedChangeListener { _, _ ->
            updateFilters()
        }
    }

    private fun collectUiState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                model.uiState.collect { state ->
                    state.gachaUiSnapshot?.let { snapshot ->
                        records = snapshot.records
                        pools = snapshot.gachaPoolStats
                        selectedPoolId = state.selectedPoolId
                        if (binding.FilterSixStar.isChecked != state.filterSixStar) {
                            binding.FilterSixStar.isChecked = state.filterSixStar
                        }
                        if (binding.FilterNew.isChecked != state.filterNew) {
                            binding.FilterNew.isChecked = state.filterNew
                        }
                        renderPoolDropdown()
                        applyFilters()
                    }
                }
            }
        }
    }

    private fun updateFilters() {
        model.setRawDataFilters(
            sixStarOnly = binding.FilterSixStar.isChecked,
            newOnly = binding.FilterNew.isChecked,
        )
        applyFilters()
    }

    private fun renderPoolDropdown() {
        if (pools.isEmpty()) {
            binding.PoolDropdown.setAdapter(null)
            binding.PoolDropdown.setText("", false)
            return
        }

        binding.PoolDropdown.setAdapter(
            ArrayAdapter(
                requireContext(),
                android.R.layout.simple_list_item_1,
                pools.map { it.poolName },
            )
        )
        val selectedPool = pools.firstOrNull { it.poolId == selectedPoolId } ?: pools.first()
        binding.PoolDropdown.setText(selectedPool.poolName, false)
    }

    private fun applyFilters() {
        var filtered = if (selectedPoolId == "ALL") {
            records
        } else {
            records.filter { it.poolId == selectedPoolId }
        }
        if (binding.FilterSixStar.isChecked) {
            filtered = filtered.filter { it.rare == SIX_STAR_RARITY }
        }
        if (binding.FilterNew.isChecked) {
            filtered = filtered.filter { it.isNew }
        }

        binding.ResultCount.text = getString(R.string.gacha_result_count, filtered.size)
        adapter.refreshData(filtered)
        pendingScrollState?.let { state ->
            binding.RecyclerView.layoutManager?.onRestoreInstanceState(state)
            pendingScrollState = null
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putParcelable(
            KEY_SCROLL_STATE,
            binding.RecyclerView.layoutManager?.onSaveInstanceState(),
        )
        super.onSaveInstanceState(outState)
    }

    override fun onDestroyView() {
        binding.RecyclerView.adapter = null
        records = emptyList()
        pools = emptyList()
        _binding = null
        super.onDestroyView()
    }

    private companion object {
        const val PAGE_SIZE = 100
        const val SIX_STAR_RARITY = 5
        const val KEY_SCROLL_STATE = "gacha_text_scroll_state"
    }
}
