package com.blueskybone.arkscreen.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.blueskybone.arkscreen.DataUiState
import com.blueskybone.arkscreen.R
import com.blueskybone.arkscreen.databinding.FragmentGachaBinding
import com.blueskybone.arkscreen.preference.PrefManager
import com.blueskybone.arkscreen.ui.recyclerview.GachaAdapter
import com.blueskybone.arkscreen.viewmodel.GachaModel
import org.koin.android.ext.android.getKoin

class Gacha : Fragment() {
    private val prefManager: PrefManager by getKoin().inject()
    private val model: GachaModel by viewModels()
    private var adapter: GachaAdapter? = null

    private var _binding: FragmentGachaBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGachaBinding.inflate(inflater)
        setupBinding()
        setUpObserver()
        return binding.root
    }

    private fun setupBinding() {
        adapter = GachaAdapter(requireContext())
        binding.RecyclerView.adapter = adapter
    }

    private fun setUpObserver() {
        model.gachaData.observe(requireActivity()) { value ->
            adapter?.submitList(value)
        }
        model.uiState.observe(requireActivity()) { value ->
            when (value) {
                is DataUiState.Success -> displayView()
                else -> {}
            }
        }
    }

    private fun displayView() {
        val account = prefManager.baseAccountGc.get()
        binding.NickName.text = account.nickName
        if (account.official) binding.Icon.setImageResource(R.drawable.hg_icon_80x80)
        else binding.Icon.setImageResource(R.drawable.bili_icon_75x71)

        binding.CountSum.text = getString(R.string.gacha_count, model.finalCountSum)
        binding.Rarity6.text = model.rarity6Count.toString()
        binding.AverageCount.text =
            if (model.rarity6Count == 0) "-" else getString(
                R.string.gacha_count,
                model.finalCountSum / model.rarity6Count
            )
        binding.NormalCount.text = getString(R.string.gacha_count, model.poolCountNormal)
        binding.FesCount.text = getString(R.string.gacha_count, model.poolCountFes)
        binding.CoreCount.text = getString(R.string.gacha_count, model.poolCountCore)
        binding.DateRange.text = getString(R.string.date_range, model.dateRange)
    }
}