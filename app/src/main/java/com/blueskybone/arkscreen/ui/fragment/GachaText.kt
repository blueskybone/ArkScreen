package com.blueskybone.arkscreen.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.blueskybone.arkscreen.databinding.FragmentGachaTextBinding
import com.blueskybone.arkscreen.ui.recyclerview.GachaTextAdapter
import com.blueskybone.arkscreen.viewmodel.GachaModel

class GachaText: Fragment() {
    private val model: GachaModel by viewModels()
    private var adapter: GachaTextAdapter? = null

    private var _binding: FragmentGachaTextBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGachaTextBinding.inflate(inflater)
        setupBinding()
        setUpObserver()
        return binding.root
    }

    private fun setupBinding() {
        adapter = GachaTextAdapter(requireContext())
        binding.RecyclerView.adapter = adapter
    }

    private fun setUpObserver() {
        model.gachaRecords.observe(requireActivity()) { value ->
            adapter?.submitList(value)
        }
    }
}