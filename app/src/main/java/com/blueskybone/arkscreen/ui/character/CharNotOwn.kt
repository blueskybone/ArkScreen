package com.blueskybone.arkscreen.ui.character

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.blueskybone.arkscreen.R
import com.blueskybone.arkscreen.databinding.FragmentCharNotOwnBinding
import com.blueskybone.arkscreen.ui.character.adapter.CharMissFlowAdapter
import org.koin.androidx.viewmodel.ext.android.activityViewModel

/**
 *   Created by blueskybone
 *   Date: 2025/1/19
 */

class CharNotOwn : Fragment() {

    private val model: CharModel by activityViewModel()
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
        setupObserver()
        return binding.root
    }

    private fun setupBinding() {
        model.charsNotOwnList.observe(viewLifecycleOwner) { value ->
            adapterFlow.submitList(value)
        }
    }

    private fun setupObserver(){
        model.update.observe(viewLifecycleOwner){update->
            binding.Update.text = getString(R.string.last_update, update)
        }
    }

    override fun onDestroyView() {
        binding.FlowLayout.removeAllViews()
        _binding = null
        super.onDestroyView()
    }
}
