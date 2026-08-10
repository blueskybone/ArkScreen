package com.blueskybone.arkscreen.ui.recruit

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.RecyclerView
import com.blueskybone.arkscreen.R
import com.blueskybone.arkscreen.ui.recruit.adapter.ResultAdapter
import com.blueskybone.arkscreen.ui.recruit.adapter.TagAdapter
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.hjq.toast.Toaster
import kotlinx.coroutines.launch

class RecruitFragment : Fragment(R.layout.fragment_recruit) {

    private val viewModel: RecruitModel by viewModels()

    private lateinit var tagAdapter: TagAdapter
    private lateinit var resultAdapter: ResultAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupTagRecycler(view)
        setupResultRecycler(view)
        observeState(view)

        view.findViewById<View>(R.id.btnReset).setOnClickListener {
            viewModel.reset()
        }
    }

    private fun setupTagRecycler(view: View) {
        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerTags)

        tagAdapter = TagAdapter(
            onClick = { tag ->
                viewModel.toggleTag(tag)
            }
        )

        recyclerView.layoutManager = FlexboxLayoutManager(requireContext()).apply {
            flexDirection = FlexDirection.ROW
            flexWrap = FlexWrap.WRAP
        }
        recyclerView.adapter = tagAdapter

        tagAdapter.submitList(RecruitTagSource.buildAllTags())
    }

    private fun setupResultRecycler(view: View) {
        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerResult)

        resultAdapter = ResultAdapter(
            onOperatorClick = { operator ->
                OperatorDialog(requireContext()).show(operator)
            }
        )

        recyclerView.adapter = resultAdapter
    }

    private fun observeState(view: View) {
        val loadingView = view.findViewById<View>(R.id.loadingContainer)
        val contentView = view.findViewById<View>(R.id.contentContainer)
        val updateText = view.findViewById<TextView>(R.id.tvUpdate)
        val newOpeText = view.findViewById<TextView>(R.id.tvNewOpe)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    loadingView.isVisible = state.loading
                    contentView.isVisible = !state.loading

                    updateText.text = getString(R.string.last_update, state.update)
                    newOpeText.text = state.newOpe.joinToString(" / ")

                    tagAdapter.updateSelected(state.selectedTags)
                    resultAdapter.submitList(state.result)

                    state.error?.let {
                        Toaster.show(it)
                    }
                }
            }
        }
    }
}