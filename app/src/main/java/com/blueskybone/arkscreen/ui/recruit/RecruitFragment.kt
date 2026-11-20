package com.blueskybone.arkscreen.ui.recruit

import android.os.Bundle
import org.koin.android.ext.android.inject
import android.view.View
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.blueskybone.arkscreen.R
import com.blueskybone.arkscreen.domain.model.recruit.RecruitResult
import com.blueskybone.arkscreen.ui.UiStatus
import com.blueskybone.arkscreen.ui.recruit.adapter.ResultAdapter
import com.blueskybone.arkscreen.ui.recruit.adapter.TagAdapter
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexboxLayout
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.hjq.toast.Toaster
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class RecruitFragment : Fragment(R.layout.fragment_recruit) {

    private val viewModel: RecruitModel by viewModel()
    private val databaseProvider: com.blueskybone.arkscreen.domain.service.RecruitDatabaseProvider by inject()

    private val tagAdapters = mutableListOf<TagAdapter>()
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
        val recyclerIds = listOf(
            R.id.recyclerTagsPrimary,
            R.id.recyclerTagsProfession,
            R.id.recyclerTagsPosition,
            R.id.recyclerTagsAbility,
        )
        val tagGroups = listOf(
            RecruitTagSource.buttonList1,
            RecruitTagSource.buttonList2,
            RecruitTagSource.buttonList3,
            RecruitTagSource.buttonList4,
        )

        recyclerIds.zip(tagGroups).forEachIndexed { group, (recyclerId, tags) ->
            val recyclerView = view.findViewById<RecyclerView>(recyclerId)
            val adapter = TagAdapter(viewModel::toggleTag)
            recyclerView.layoutManager = FlexboxLayoutManager(requireContext()).apply {
                flexDirection = FlexDirection.ROW
                flexWrap = FlexWrap.WRAP
            }
            recyclerView.adapter = adapter
            adapter.submitList(tags.map { TagItem(name = it, group = group) })
            tagAdapters += adapter
        }
    }

    private fun setupResultRecycler(view: View) {
        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerResult)

        resultAdapter = ResultAdapter(
            onOperatorClick = { operator ->
                OperatorDialog(requireContext()).show(operator)
            }
        )

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = resultAdapter
    }

    private fun observeState(view: View) {
        val loadingView = view.findViewById<View>(R.id.loadingContainer)
        val contentView = view.findViewById<View>(R.id.contentContainer)
        val updateText = view.findViewById<TextView>(R.id.tvUpdate)
        val metadataChips = view.findViewById<FlexboxLayout>(R.id.recruitMetadataChips)
        var displayedNewOperators = emptyList<String>()
        var displayedResults = emptyList<RecruitResult>()

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.uiState.collect { state ->
                        val loading = state.status is UiStatus.Loading
                        loadingView.isVisible = loading
                        contentView.isVisible = !loading
                        updateText.text = getString(
                            R.string.last_update,
                            state.update.ifBlank { "—" },
                        )
                        if (state.newOpe != displayedNewOperators) {
                            displayedNewOperators = state.newOpe
                            renderNewOperatorChips(metadataChips, state.newOpe)
                        }
                        view.findViewById<View>(R.id.btnReset).isEnabled = state.selectedTags.isNotEmpty()
                        view.findViewById<TextView>(R.id.resultEmpty).apply {
                            isVisible = state.result.isEmpty()
                            text = when {
                                state.status is UiStatus.Error -> (state.status as UiStatus.Error).message
                                state.selectedTags.isEmpty() -> getString(R.string.recruit_select_hint)
                                else -> getString(R.string.recruit_no_match)
                            }
                        }
                        tagAdapters.forEach { it.updateSelected(state.selectedTags) }
                        if (state.result != displayedResults) {
                            displayedResults = state.result

                            resultAdapter.submitList(state.result)
                        }
                    }
                }
                launch {
                    viewModel.event.collect { event ->
                        when (event) {
                            is RecruitEvent.ShowError -> Toaster.show(event.message)
                        }
                    }
                }
            }
        }
    }

    private fun renderNewOperatorChips(group: FlexboxLayout, operators: List<String>) {
        if (group.childCount > 1) {
            group.removeViews(1, group.childCount - 1)
        }
        operators.forEach { operator ->
            val chip = layoutInflater.inflate(
                R.layout.item_recruit_info,
                group,
                false,
            ) as TextView
            chip.text = operator
            chip.isClickable = true
            chip.isFocusable = true
            chip.setOnClickListener {
                viewLifecycleOwner.lifecycleScope.launch {
                    databaseProvider.getDatabase().getOrNull()?.let { db ->
                        val operators = db.operatorList.map { com.blueskybone.arkscreen.domain.model.recruit.RecruitOpe(it.name, it.star, it.tag, it.skin) } +
                            db.operatorHighList.map { com.blueskybone.arkscreen.domain.model.recruit.RecruitOpe(it.name, it.star, it.tag, it.skin) } +
                            db.operatorLowList.map { com.blueskybone.arkscreen.domain.model.recruit.RecruitOpe(it.name, it.star, it.tag, it.skin) } +
                            db.operatorRobotList.map { com.blueskybone.arkscreen.domain.model.recruit.RecruitOpe(it.name, it.star, it.tag, it.skin) }
                        operators.firstOrNull { it.name == operator }?.let { OperatorDialog(requireContext()).show(it) }
                    }
                }
            }
            group.addView(chip)
        }
    }

    override fun onDestroyView() {
        listOf(
            R.id.recyclerTagsPrimary,
            R.id.recyclerTagsProfession,
            R.id.recyclerTagsPosition,
            R.id.recyclerTagsAbility,
        ).forEach { id -> view?.findViewById<RecyclerView>(id)?.adapter = null }
        view?.findViewById<RecyclerView>(R.id.recyclerResult)?.adapter = null
        tagAdapters.clear()
        super.onDestroyView()
    }
}
