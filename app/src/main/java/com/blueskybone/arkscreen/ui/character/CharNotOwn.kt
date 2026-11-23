package com.blueskybone.arkscreen.ui.character

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.blueskybone.arkscreen.R
import com.blueskybone.arkscreen.databinding.FragmentCharNotOwnBinding
import com.blueskybone.arkscreen.ui.character.adapter.CharMissFlowAdapter
import com.blueskybone.arkscreen.ui.character.layout.calculateGridHorizontalPadding
import com.blueskybone.arkscreen.util.dpToPx
import com.nex3z.flowlayout.FlowLayout
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import kotlin.math.roundToInt

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
        binding.FlowLayout.addOnLayoutChangeListener { view, left, _, right, _, oldLeft, _, oldRight, _ ->
            if (right - left != oldRight - oldLeft) {
                alignGrid(view as FlowLayout)
            }
        }
        setupBinding()
        setupObserver()
        return binding.root
    }

    private fun setupBinding() {
        model.charsNotOwnList.observe(viewLifecycleOwner) { value ->
            adapterFlow.submitList(value)
            binding.FlowLayout.post {
                _binding?.let { alignGrid(it.FlowLayout) }
            }
        }
    }

    private fun alignGrid(flowLayout: FlowLayout) {
        val firstChild = flowLayout.getChildAt(0) ?: return
        val cellWidth = firstChild.measuredWidth.takeIf { it > 0 } ?: return
        val minPadding = dpToPx(requireContext(), MIN_HORIZONTAL_PADDING_DP).roundToInt()
        val horizontalPadding = calculateGridHorizontalPadding(
            containerWidth = flowLayout.width,
            cellWidth = cellWidth,
            minPadding = minPadding,
        )

        if (flowLayout.paddingLeft != horizontalPadding ||
            flowLayout.paddingRight != horizontalPadding
        ) {
            flowLayout.setPadding(
                horizontalPadding,
                flowLayout.paddingTop,
                horizontalPadding,
                flowLayout.paddingBottom,
            )
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

    private companion object {
        const val MIN_HORIZONTAL_PADDING_DP = 12F
    }
}
