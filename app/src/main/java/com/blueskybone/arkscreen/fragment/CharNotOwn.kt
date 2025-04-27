package com.blueskybone.arkscreen.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.RecyclerView
import com.blueskybone.arkscreen.databinding.FragmentCharNotOwnBinding
import com.blueskybone.arkscreen.recyclerview.CharNotOwnAdapter
import com.blueskybone.arkscreen.viewmodel.CharModel

/**
 *   Created by blueskybone
 *   Date: 2025/1/19
 */

class CharNotOwn : Fragment() {

    private val model: CharModel by activityViewModels()
    private var _binding: FragmentCharNotOwnBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: CharNotOwnAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCharNotOwnBinding.inflate(inflater)

        adapter = CharNotOwnAdapter(requireContext(), 20)
        binding.RecyclerView.adapter = adapter
        setupBinding()
        setupListener()
        return binding.root
    }

    private fun setupBinding() {
        model.charsNotOwnList.observe(viewLifecycleOwner) { value ->
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

    override fun onDestroy() {
        _binding = null
        super.onDestroy()
    }
}