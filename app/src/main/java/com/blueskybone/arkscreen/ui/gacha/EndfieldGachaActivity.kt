package com.blueskybone.arkscreen.ui.gacha

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.blueskybone.arkscreen.R
import com.blueskybone.arkscreen.data.local.room.EndfieldGachaRecord
import com.blueskybone.arkscreen.databinding.ActivityEndfieldGachaBinding
import com.blueskybone.arkscreen.databinding.ItemEndfieldGachaBinding
import com.blueskybone.arkscreen.databinding.ItemEndfieldGachaPoolBinding
import com.blueskybone.arkscreen.databinding.ItemEndfieldGachaHitBinding
import com.blueskybone.arkscreen.ui.account.AccountMngActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class EndfieldGachaActivity : AppCompatActivity() {
    private val model: EndfieldGachaModel by viewModel()
    private lateinit var binding: ActivityEndfieldGachaBinding
    private val adapter = RecordAdapter()
    private val poolAdapter = EndfieldPoolAdapter()
    private var current = EndfieldGachaState()
    private var type = "char"
    private var rawMode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme_Endfield)
        super.onCreate(savedInstanceState)
        binding = ActivityEndfieldGachaBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.Toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.Records.adapter = adapter
        binding.Pools.adapter = poolAdapter
        binding.CharButton.isCheckable = true
        binding.WeaponButton.isCheckable = true
        binding.AccountButton.setOnClickListener { chooseAccount() }
        binding.RefreshButton.setOnClickListener { model.refresh() }
        binding.CharButton.setOnClickListener { type = "char"; render(current) }
        binding.WeaponButton.setOnClickListener { type = "weapon"; render(current) }
        binding.ModeButton.setOnClickListener {
            rawMode = !rawMode
            render(current)
        }
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) { model.state.collect(::render) }
        }
    }

    private fun render(state: EndfieldGachaState) {
        current = state
        binding.AccountButton.text = state.accounts.firstOrNull { it.uid == state.selectedUid }
            ?.nickName ?: getString(R.string.endfield_choose_account)
        binding.RefreshButton.isEnabled = state.selectedUid != null && !state.loading
        binding.Loading.visibility = if (state.loading) View.VISIBLE else View.GONE
        binding.CharButton.isChecked = type == "char"
        binding.WeaponButton.isChecked = type == "weapon"
        binding.Pools.visibility = if (rawMode) View.GONE else View.VISIBLE
        binding.Records.visibility = if (rawMode) View.VISIBLE else View.GONE
        binding.ModeButton.setText(
            if (rawMode) R.string.endfield_gacha_pool_overview else R.string.endfield_gacha_raw_records
        )
        val records = state.records.filter { it.type == type && !EndfieldPityCalculator.isGift(it) }
            .sortedWith(compareByDescending<EndfieldGachaRecord> { it.gachaTs.toLongOrNull() ?: 0L }
                .thenByDescending { it.seqId.length }.thenByDescending { it.seqId })
        adapter.submitList(records)
        val six = records.count { it.rarity == 6 }
        val five = records.count { it.rarity == 5 }
        binding.Status.text = when {
            state.accounts.isEmpty() -> getString(R.string.endfield_chars_no_account)
            state.error != null -> state.error
            state.loading -> state.progress
            records.isEmpty() -> getString(R.string.endfield_gacha_empty)
            else -> getString(R.string.endfield_gacha_summary, records.size, six, five)
        }
        binding.TotalCount.text = "总计\n${records.size} 次"
        binding.SixCount.text = "6★\n$six"
        binding.AverageCount.text = "平均 6★\n${if (six == 0) "-" else "${records.size / six} 次"}"
        val progress = EndfieldPityCalculator.calculate(state.records, state.pools, type)
        poolAdapter.submitList(progress)
    }

    private fun chooseAccount() {
        if (current.accounts.isEmpty()) {
            startActivity(Intent(this, AccountMngActivity::class.java))
            return
        }
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.endfield_choose_account)
            .setSingleChoiceItems(
                current.accounts.map { it.nickName }.toTypedArray(),
                current.accounts.indexOfFirst { it.uid == current.selectedUid },
            ) { dialog, index ->
                model.select(current.accounts[index].uid)
                dialog.dismiss()
            }.show()
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}

private class EndfieldPoolAdapter : ListAdapter<EndfieldPoolProgress, EndfieldPoolAdapter.Holder>(
    object : DiffUtil.ItemCallback<EndfieldPoolProgress>() {
        override fun areItemsTheSame(a: EndfieldPoolProgress, b: EndfieldPoolProgress) =
            a.key == b.key
        override fun areContentsTheSame(a: EndfieldPoolProgress, b: EndfieldPoolProgress) = a == b
    },
) {
    private val expanded = mutableSetOf<String>()

    class Holder(val binding: ItemEndfieldGachaPoolBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder = Holder(
        ItemEndfieldGachaPoolBinding.inflate(LayoutInflater.from(parent.context), parent, false),
    )

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val pool = getItem(position)
        val binding = holder.binding
        binding.PoolName.text = pool.name
        binding.Count.text = "${pool.total} 次"
        binding.Counts.text = buildString {
            append("6★ ").append(pool.six).append("  ·  5★ ").append(pool.five)
            if (pool.free > 0) append("  ·  免费 ").append(pool.free)
        }
        binding.Pity.text = buildString {
            pool.smallMax?.let { max ->
                append("6★ 保底 ").append(pool.smallCount).append("/").append(max)
            }
            pool.bigMax?.let { max ->
                if (isNotEmpty()) append("   ·   ")
                append("UP ")
                if (pool.bigSatisfied) append("已获取")
                else append(pool.bigCount ?: 0).append("/").append(max)
                append(if (pool.bigUsesOfficialCount) "（官方累计）" else "（本地记录）")
            }
        }
        binding.Pity.visibility = if (binding.Pity.text.isEmpty()) View.GONE else View.VISIBLE
        binding.Hits.removeAllViews()
        val visible = if (pool.key in expanded) pool.hits else pool.hits.take(3)
        visible.forEach { hit ->
            val row = ItemEndfieldGachaHitBinding.inflate(
                LayoutInflater.from(binding.Hits.context), binding.Hits, false,
            )
            row.Name.text = hit.name + if (hit.isNew) " · N" else ""
            row.Progress.max = pool.smallMax ?: 80
            row.Progress.progress = hit.pity.coerceAtMost(row.Progress.max)
            row.Count.text = if (hit.isFree) "免费" else "${hit.pity} 次"
            binding.Hits.addView(row.root)
        }
        binding.Empty.visibility = if (pool.hits.isEmpty()) View.VISIBLE else View.GONE
        val canExpand = pool.hits.size > 3
        binding.ExpandHint.visibility = if (canExpand) View.VISIBLE else View.GONE
        if (canExpand) {
            binding.ExpandHint.text = if (pool.key in expanded) {
                binding.root.context.getString(R.string.collapse_gacha_records)
            } else {
                binding.root.context.getString(R.string.expand_gacha_records, pool.hits.size)
            }
            binding.root.setOnClickListener {
                if (!expanded.add(pool.key)) expanded.remove(pool.key)
                val index = holder.bindingAdapterPosition
                if (index != RecyclerView.NO_POSITION) notifyItemChanged(index)
            }
        } else binding.root.setOnClickListener(null)
    }
}

private class RecordAdapter : ListAdapter<EndfieldGachaRecord, RecordAdapter.Holder>(
    object : DiffUtil.ItemCallback<EndfieldGachaRecord>() {
        override fun areItemsTheSame(a: EndfieldGachaRecord, b: EndfieldGachaRecord) =
            a.roleId == b.roleId && a.type == b.type && a.seqId == b.seqId
        override fun areContentsTheSame(a: EndfieldGachaRecord, b: EndfieldGachaRecord) = a == b
    },
) {
    class Holder(val binding: ItemEndfieldGachaBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder = Holder(
        ItemEndfieldGachaBinding.inflate(LayoutInflater.from(parent.context), parent, false),
    )

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val record = getItem(position)
        holder.binding.Name.text = record.itemName.ifBlank { record.itemId.ifBlank { "未知物品" } }
        holder.binding.Pool.text = record.poolName.ifBlank { record.poolType }
        holder.binding.Detail.text = buildString {
            append(record.rarity).append("★")
            if (record.isNew) append(" · 新获得")
            if (record.isFree) append(" · 免费")
            if (record.kind.startsWith("gift")) append(" · 奖励")
            if (record.gachaTs.isNotBlank()) append(" · ").append(formatTime(record.gachaTs))
        }
        holder.binding.Name.setTextColor(
            com.google.android.material.color.MaterialColors.getColor(
                holder.binding.Name,
                if (record.rarity >= 6) com.google.android.material.R.attr.colorPrimary
                else com.google.android.material.R.attr.colorOnSurface,
            ),
        )
    }

    private fun formatTime(raw: String): String {
        val timestamp = raw.toLongOrNull() ?: return raw
        val millis = if (timestamp < 10_000_000_000L) timestamp * 1000 else timestamp
        return SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date(millis))
    }
}
