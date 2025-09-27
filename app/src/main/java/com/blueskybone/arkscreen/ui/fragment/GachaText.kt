package com.blueskybone.arkscreen.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.RecyclerView
import com.blueskybone.arkscreen.databinding.FragmentGachaTextBinding
import com.blueskybone.arkscreen.ui.recyclerview.GachaTextAdapter
import com.blueskybone.arkscreen.viewmodel.GachaModel

class GachaText : Fragment() {
    private val model: GachaModel by viewModels()

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
}