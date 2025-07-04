package com.blueskybone.arkscreen.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.blueskybone.arkscreen.databinding.FragmentCharNotOwnBinding
import com.blueskybone.arkscreen.ui.recyclerview.CharMissFlowAdapter
import com.blueskybone.arkscreen.viewmodel.CharModel

/**
 *   Created by blueskybone
 *   Date: 2025/1/19
 */

class CharNotOwn : Fragment() {

    private val model: CharModel by activityViewModels()
    private var _binding: FragmentCharNotOwnBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapterFlow: CharMissFlowAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCharNotOwnBinding.inflate(inflater)
        adapterFlow = CharMissFlowAdapter(requireContext(), binding.FlowLayout)
        setupBinding()
        return binding.root
    }

    private fun setupBinding() {
        model.charsNotOwnList.observe(viewLifecycleOwner) { value ->
            adapterFlow.submitList(value)
        }
    }

    override fun onDestroy() {
        _binding = null
        super.onDestroy()
    }
}