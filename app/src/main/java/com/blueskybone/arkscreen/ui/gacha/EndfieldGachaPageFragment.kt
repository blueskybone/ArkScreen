package com.blueskybone.arkscreen.ui.gacha

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.RecyclerView
import com.blueskybone.arkscreen.R
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.combine
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import java.util.Locale

class EndfieldGachaPageFragment : Fragment() {
    private val model: EndfieldGachaViewModel by activityViewModel()
    private val recordAdapter = RecordAdapter()
    private val poolAdapter = EndfieldPoolAdapter()
    private val page get() = requireArguments().getInt("page")
    private var renderedData: EndfieldGachaPageData? = null
    private var accountPopup: androidx.appcompat.widget.ListPopupWindow? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, state: Bundle?): View =
        inflater.inflate(when (page) {
            1 -> R.layout.fragment_endfield_gacha_report
            2 -> R.layout.fragment_endfield_gacha_records
            else -> R.layout.fragment_endfield_gacha_pools
        }, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        if (page == 2 && savedInstanceState != null) {
            model.setRecordFilter(EndfieldRecordFilter(
                savedInstanceState.getString("rawScope"), savedInstanceState.getString("rawPool"),
                savedInstanceState.getBoolean("rawSix"), savedInstanceState.getBoolean("rawNew"),
            ))
        }
        view.findViewById<com.google.android.material.chip.Chip>(R.id.FilterSixStar)?.apply {
            isChecked = model.recordFilter.value.sixOnly
            setOnCheckedChangeListener { _, checked ->
                model.setRecordFilter(model.recordFilter.value.copy(sixOnly = checked))
            }
        }
        view.findViewById<com.google.android.material.chip.Chip>(R.id.FilterNew)?.apply {
            isChecked = model.recordFilter.value.newOnly
            setOnCheckedChangeListener { _, checked ->
                model.setRecordFilter(model.recordFilter.value.copy(newOnly = checked))
            }
        }
        view.findViewById<RecyclerView>(R.id.Pools)?.adapter = poolAdapter
        view.findViewById<RecyclerView>(R.id.RecyclerView)?.adapter = recordAdapter
        view.findViewById<View>(R.id.AccountHeader)?.setOnClickListener { chooseAccount(it) }
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                combine(model.state, model.pageData, model.category) { state, data, type -> Triple(state, data, type) }
                    .collect { (state, data, type) -> render(view, state, data, type) }
            }
        }
    }

    private fun chooseAccount(anchor: View) {
        val state = model.state.value
        if (state.accounts.isEmpty()) {
            startActivity(android.content.Intent(requireContext(), com.blueskybone.arkscreen.ui.account.AccountMngActivity::class.java))
            return
        }
        accountPopup?.dismiss()
        accountPopup = com.blueskybone.arkscreen.ui.account.common.showAccountChoices(anchor, state.accounts, state.selectedUid) { model.select(it.uid) }
    }

    private fun render(root: View, state: EndfieldGachaUiState, data: EndfieldGachaPageData?, type: String) {
        val summary = data?.summary
        // A background calculation may still belong to the previous account/category/snapshot.
        val ready = summary != null && summary.input.uid == state.selectedUid && summary.input.type == type &&
            summary.input.records == state.records && summary.input.pools == state.pools
        val loading = state.loading || !ready
        listOf(R.id.Content, R.id.GachaTextFrame, R.id.Report).forEach { id ->
            root.findViewById<View>(id)?.visibility = if (loading) View.GONE else View.VISIBLE
        }
        root.findViewById<com.blueskybone.arkscreen.ui.common.view.StarterLoadingView>(R.id.Loading)?.apply {
            setMessage(if (state.loading) state.progress.ifBlank { getString(R.string.syncing) } else getString(R.string.status_gacha_preparing))
            visibility = if (loading) View.VISIBLE else View.GONE
        }
        root.findViewById<TextView>(R.id.Status)?.apply {
            text = when {
                state.accounts.isEmpty() -> getString(R.string.endfield_chars_no_account)
                state.error != null -> state.error
                else -> getString(R.string.endfield_gacha_empty)
            }
            visibility = if (!loading && (state.error != null || summary?.records.isNullOrEmpty())) View.VISIBLE else View.GONE
        }
        if (page == 0) renderAccount(root, state)
        if (!ready || data == null || data === renderedData) return
        val previous = renderedData
        renderedData = data
        if (page != 2 && previous?.summary === data.summary) return
        when (page) {
            0 -> renderPools(root, data.summary)
            1 -> renderReport(root, data.summary)
            2 -> renderRecords(root, data)
        }
    }

    private fun renderAccount(root: View, state: EndfieldGachaUiState) {
        val account = state.accounts.firstOrNull { it.uid == state.selectedUid }
        root.findViewById<TextView>(R.id.CurrentAccount).text = account?.nickName ?: getString(R.string.endfield_choose_account)
        root.findViewById<TextView>(R.id.Server).apply {
            visibility = if (account == null) View.GONE else View.VISIBLE
            setText(if (account?.official == true) R.string.official_server else R.string.bilibili_server)
        }
        root.findViewById<android.widget.ImageView>(R.id.Avatar).setImageResource(when (account?.official) {
            true -> R.drawable.hg_icon_80x80
            false -> R.drawable.ic_account_bilibili
            null -> R.drawable.ic_user
        })
    }

    private fun average(data: EndfieldGachaPresentation) = data.average?.let {
        getString(R.string.gacha_average_times, it)
    } ?: "—"

    private fun renderPools(root: View, data: EndfieldGachaPresentation) {
        root.findViewById<TextView>(R.id.TotalCount).text = getString(R.string.gacha_times_count, data.records.size)
        root.findViewById<TextView>(R.id.SixCount).text = data.six.toString()
        root.findViewById<TextView>(R.id.AverageCount).text = average(data)
        root.findViewById<TextView>(R.id.Subtitle).text = if (data.records.isEmpty()) getString(R.string.endfield_gacha_empty) else
            getString(R.string.date_range, "${formatGachaTime(data.records.last().gachaTs, "yyyy-MM-dd")} ~ ${formatGachaTime(data.records.first().gachaTs, "yyyy-MM-dd")}")
        poolAdapter.setAccount(data.input.uid)
        poolAdapter.submitList(data.pools)
    }

    private fun renderReport(root: View, data: EndfieldGachaPresentation) {
        fun metric(id: Int, title: String, value: String) {
            val row = root.findViewById<View>(id)
            row.findViewById<TextView>(R.id.Title).text = title
            row.findViewById<TextView>(R.id.Value).text = value
        }
        fun rate(count: Int) = if (data.records.isEmpty()) "—" else String.format(Locale.getDefault(), "%.1f%%", count * 100.0 / data.records.size)
        metric(R.id.ReportPools, getString(R.string.gacha_report_pool_count), data.pools.size.toString())
        metric(R.id.ReportPaid, getString(R.string.gacha_report_paid), (data.records.size - data.free).toString())
        metric(R.id.ReportFree, getString(R.string.gacha_report_free), data.free.toString())
        metric(R.id.ReportSix, getString(R.string.gacha_report_six_share), "${data.six} / ${rate(data.six)}")
        metric(R.id.ReportFive, getString(R.string.gacha_report_five_share), "${data.five} / ${rate(data.five)}")
        metric(R.id.ReportAverage, getString(R.string.gacha_report_average_pity), average(data))
        metric(R.id.ReportBest, getString(R.string.gacha_report_best_pity), data.best?.let { getString(R.string.gacha_times_count, it) } ?: "—")
        metric(R.id.ReportWorst, getString(R.string.gacha_report_worst_pity), data.worst?.let { getString(R.string.gacha_times_count, it) } ?: "—")
    }

    private fun renderRecords(root: View, data: EndfieldGachaPageData) {
        val summary = data.summary
        root.findViewById<com.google.android.material.textfield.MaterialAutoCompleteTextView>(R.id.PoolDropdown).apply {
            setAdapter(android.widget.ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, listOf(getString(R.string.gacha_all_pools)) + summary.choices.map { it.second }))
            setText(summary.choices.firstOrNull { it.first == summary.selectedPool(data.filter) }?.second ?: getString(R.string.gacha_all_pools), false)
            setOnItemClickListener { _, _, position, _ ->
                model.setRecordFilter(model.recordFilter.value.copy(scope = summary.scope, pool = summary.choices.getOrNull(position - 1)?.first))
            }
        }
        recordAdapter.submitList(data.rows)
        root.findViewById<TextView>(R.id.ResultCount).text = getString(R.string.gacha_result_count, data.rows.size)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        if (page == 2) model.recordFilter.value.let {
            outState.putString("rawPool", it.pool)
            outState.putString("rawScope", it.scope)
            outState.putBoolean("rawSix", it.sixOnly)
            outState.putBoolean("rawNew", it.newOnly)
        }
        super.onSaveInstanceState(outState)
    }

    override fun onDestroyView() {
        accountPopup?.dismiss()
        accountPopup = null
        renderedData = null
        view?.findViewById<RecyclerView>(R.id.Pools)?.adapter = null
        view?.findViewById<RecyclerView>(R.id.RecyclerView)?.adapter = null
        super.onDestroyView()
    }
}
