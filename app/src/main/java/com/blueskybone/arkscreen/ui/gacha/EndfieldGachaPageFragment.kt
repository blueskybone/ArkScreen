package com.blueskybone.arkscreen.ui.gacha

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.RecyclerView
import com.blueskybone.arkscreen.R
import com.blueskybone.arkscreen.data.local.room.EndfieldGachaRecord
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.combine
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import java.util.Locale

class EndfieldGachaPageFragment : Fragment() {
    private val model: EndfieldGachaModel by activityViewModel()
    private val recordAdapter = RecordAdapter()
    private val poolAdapter = EndfieldPoolAdapter()
    private var selectedPool: String? = null
    private var sixOnly = false
    private var newOnly = false
    private var filterScope: String? = null
    private var accountPopup: androidx.appcompat.widget.ListPopupWindow? = null
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, state: Bundle?): View =
        inflater.inflate(when (requireArguments().getInt("page")) {
            1 -> R.layout.fragment_endfield_gacha_report
            2 -> R.layout.fragment_endfield_gacha_records
            else -> R.layout.fragment_endfield_gacha_pools
        }, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        selectedPool = savedInstanceState?.getString("rawPool") ?: selectedPool
        sixOnly = savedInstanceState?.getBoolean("rawSix") ?: sixOnly
        newOnly = savedInstanceState?.getBoolean("rawNew") ?: newOnly
        filterScope = savedInstanceState?.getString("rawScope") ?: filterScope
        fun rerender() = render(view, model.state.value, (requireActivity() as EndfieldGachaActivity).category.value)
        view.findViewById<com.google.android.material.chip.Chip>(R.id.FilterSixStar)?.apply {
            isChecked = sixOnly
            setOnCheckedChangeListener { _, checked -> sixOnly = checked; rerender() }
        }
        view.findViewById<com.google.android.material.chip.Chip>(R.id.FilterNew)?.apply {
            isChecked = newOnly
            setOnCheckedChangeListener { _, checked -> newOnly = checked; rerender() }
        }
        view.findViewById<RecyclerView>(R.id.Pools)?.adapter = poolAdapter
        view.findViewById<RecyclerView>(R.id.RecyclerView)?.adapter = recordAdapter
        view.findViewById<View>(R.id.AccountHeader)?.setOnClickListener { chooseAccount(it) }
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                combine(model.state, (requireActivity() as EndfieldGachaActivity).category) { state, type -> state to type }
                    .collect { (state, type) -> render(view, state, type) }
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
    private fun render(root: View, state: EndfieldGachaState, type: String) {
        fun text(id: Int) = root.findViewById<android.widget.TextView>(id)
        val account = state.accounts.firstOrNull { it.uid == state.selectedUid }
        text(R.id.CurrentAccount)?.text = account?.nickName ?: getString(R.string.endfield_choose_account)
        text(R.id.Server)?.apply {
            visibility = if (account == null) View.GONE else View.VISIBLE
            setText(if (account?.official == true) R.string.official_server else R.string.bilibili_server)
        }
        root.findViewById<android.widget.ImageView>(R.id.Avatar)?.setImageResource(when(account?.official) {
            true -> R.drawable.hg_icon_80x80; false -> R.drawable.ic_account_bilibili; null -> R.drawable.ic_user
        })
        root.findViewById<View>(R.id.Content)?.visibility = if (state.loading) View.GONE else View.VISIBLE
        root.findViewById<com.blueskybone.arkscreen.ui.common.view.StarterLoadingView>(R.id.Loading)?.apply {
            setMessage(state.progress.ifBlank { getString(R.string.syncing) })
            visibility = if (state.loading) View.VISIBLE else View.GONE
        }
        root.findViewById<View>(R.id.GachaTextFrame)?.visibility = if (state.loading) View.GONE else View.VISIBLE
        root.findViewById<View>(R.id.Report)?.visibility = if (state.loading) View.GONE else View.VISIBLE
        val records = state.records.filter { it.type == type && !EndfieldPityCalculator.isGift(it) }
            .sortedWith(compareByDescending<EndfieldGachaRecord> { it.gachaTs.toLongOrNull() ?: 0L }
                .thenByDescending { it.seqId.length }.thenByDescending { it.seqId })
        root.findViewById<com.google.android.material.textfield.MaterialAutoCompleteTextView>(R.id.PoolDropdown)?.let { dropdown ->
            val scope = "${state.selectedUid}:$type"
            if (filterScope != scope) { selectedPool = null; filterScope = scope }
            val pools = records.distinctBy { it.poolId }.map { it.poolId to it.poolName.ifBlank { it.poolId } }
            if (selectedPool != null && pools.none { it.first == selectedPool }) selectedPool = null
            dropdown.setAdapter(android.widget.ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, listOf("全部卡池") + pools.map { it.second }))
            dropdown.setText(pools.firstOrNull { it.first == selectedPool }?.second ?: "全部卡池", false)
            dropdown.setOnItemClickListener { _, _, position, _ ->
                selectedPool = pools.getOrNull(position - 1)?.first
                render(root, model.state.value, (requireActivity() as EndfieldGachaActivity).category.value)
            }
            val filtered = rawRows(records).filter { row ->
                (selectedPool == null || row.record.poolId == selectedPool) &&
                    (!sixOnly || row.record.rarity == 6) && (!newOnly || row.record.isNew)
            }
            recordAdapter.submitList(filtered)
            text(R.id.ResultCount)?.text = getString(R.string.gacha_result_count, filtered.size)
        }
        val six = records.count { it.rarity == 6 }
        val five = records.count { it.rarity == 5 }
        text(R.id.Status)?.text = when {
            state.accounts.isEmpty() -> getString(R.string.endfield_chars_no_account)
            state.error != null -> state.error
            state.loading -> state.progress
            records.isEmpty() -> getString(R.string.endfield_gacha_empty)
            else -> getString(R.string.endfield_gacha_summary, records.size, six, five)
        }
        text(R.id.TotalCount)?.text = "${records.size} 次"
        text(R.id.SixCount)?.text = "$six"
        text(R.id.AverageCount)?.text = if (six == 0) "-" else "${records.size / six} 次"
        text(R.id.Status)?.visibility = if (!state.loading && (state.error != null || records.isEmpty())) View.VISIBLE else View.GONE
        text(R.id.Subtitle)?.text = if (records.isEmpty()) getString(R.string.endfield_gacha_empty) else
            getString(R.string.date_range, "${formatGachaTime(records.last().gachaTs, "yyyy-MM-dd")} ~ ${formatGachaTime(records.first().gachaTs, "yyyy-MM-dd")}")
        poolAdapter.setAccount(state.selectedUid)
        val progress = EndfieldPityCalculator.calculate(state.records, state.pools, type)
        poolAdapter.submitList(progress)
        val paidHits = progress.flatMap { it.hits }.filterNot { it.isFree }
        fun metric(id: Int, title: String, value: String) {
            val row = root.findViewById<View>(id) ?: return
            row.findViewById<android.widget.TextView>(R.id.Title).text = title
            row.findViewById<android.widget.TextView>(R.id.Value).text = value
        }
        fun rate(count: Int) = if (records.isEmpty()) "—" else String.format(Locale.getDefault(), "%.1f%%", count * 100.0 / records.size)
        metric(R.id.ReportPools, "卡池数", progress.size.toString())
        metric(R.id.ReportPaid, "非免费次数", records.count { !it.isFree }.toString())
        metric(R.id.ReportFree, "免费次数", records.count { it.isFree }.toString())
        metric(R.id.ReportSix, "六星数量 / 占比", "$six / ${rate(six)}")
        metric(R.id.ReportFive, "五星数量 / 占比", "$five / ${rate(five)}")
        metric(R.id.ReportAverage, "六星平均出货水位", if (paidHits.isEmpty()) "—" else String.format(Locale.getDefault(), "%.1f 次", paidHits.map { it.pity }.average()))
        metric(R.id.ReportBest, "最低出货水位", paidHits.minOfOrNull { it.pity }?.let { "$it 次" } ?: "—")
        metric(R.id.ReportWorst, "最高出货水位", paidHits.maxOfOrNull { it.pity }?.let { "$it 次" } ?: "—")
        text(R.id.AverageCount)?.text = if (paidHits.isEmpty()) "—" else String.format(Locale.getDefault(), "%.1f 次", paidHits.map { it.pity }.average())
    }
    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString("rawPool", selectedPool)
        outState.putString("rawScope", filterScope)
        outState.putBoolean("rawSix", sixOnly)
        outState.putBoolean("rawNew", newOnly)
        super.onSaveInstanceState(outState)
    }
    override fun onDestroyView() {
        accountPopup?.dismiss()
        view?.findViewById<RecyclerView>(R.id.Pools)?.adapter = null
        view?.findViewById<RecyclerView>(R.id.RecyclerView)?.adapter = null
        super.onDestroyView()
    }
}
