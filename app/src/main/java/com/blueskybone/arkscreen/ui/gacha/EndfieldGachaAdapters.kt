package com.blueskybone.arkscreen.ui.gacha

import com.blueskybone.arkscreen.domain.model.gacha.EndfieldRecordProgress
import com.blueskybone.arkscreen.domain.model.gacha.EndfieldPoolProgress
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.blueskybone.arkscreen.R
import com.blueskybone.arkscreen.databinding.ItemGachaRecordsTxtBinding
import com.blueskybone.arkscreen.databinding.ItemEndfieldGachaPoolBinding
import com.blueskybone.arkscreen.databinding.ItemEndfieldGachaHitBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

internal class EndfieldPoolAdapter : ListAdapter<EndfieldPoolProgress, EndfieldPoolAdapter.Holder>(
    object : DiffUtil.ItemCallback<EndfieldPoolProgress>() {
        override fun areItemsTheSame(a: EndfieldPoolProgress, b: EndfieldPoolProgress) =
            a.key == b.key
        override fun areContentsTheSame(a: EndfieldPoolProgress, b: EndfieldPoolProgress) = a == b
    },
) {
    private val expanded = mutableSetOf<String>()
    private var accountUid: String? = null
    fun setAccount(uid: String?) {
        if (accountUid != uid) { expanded.clear(); accountUid = uid }
    }

    class Holder(val binding: ItemEndfieldGachaPoolBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder = Holder(
        ItemEndfieldGachaPoolBinding.inflate(LayoutInflater.from(parent.context), parent, false),
    )

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val pool = getItem(position)
        val binding = holder.binding
        binding.PoolName.text = pool.name
        binding.Count.text = binding.root.context.getString(R.string.gacha_times_count, pool.total)
        binding.Pity.root.text = binding.root.context.getString(R.string.gacha_current_pity, pool.smallCount)
        binding.Pity.root.isClickable = false
        binding.Hits.removeAllViews()
        val visible = if (pool.key in expanded) pool.hits else pool.hits.take(3)
        visible.forEach { hit ->
            val row = ItemEndfieldGachaHitBinding.inflate(
                LayoutInflater.from(binding.Hits.context), binding.Hits, false,
            )
            row.Date.text = formatGachaTime(hit.gachaTs, "MM-dd")
            row.Name.text = hit.name
            row.IsNew.visibility = if (hit.isNew) View.VISIBLE else View.GONE
            row.Progress.max = pool.smallMax ?: 80
            row.Progress.progress = hit.pity.coerceAtMost(row.Progress.max)
            row.Progress.setIndicatorColor(androidx.core.content.ContextCompat.getColor(
                row.root.context, gachaPullColor(hit.pity),
            ))
            row.Count.text = if (hit.isFree) binding.root.context.getString(R.string.gacha_free) else binding.root.context.getString(R.string.gacha_times_count, hit.pity)
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

internal class RecordAdapter : ListAdapter<EndfieldRecordProgress, RecordAdapter.Holder>(
    object : DiffUtil.ItemCallback<EndfieldRecordProgress>() {
        override fun areItemsTheSame(a: EndfieldRecordProgress, b: EndfieldRecordProgress) =
            a.record.roleId == b.record.roleId && a.record.type == b.record.type && a.record.seqId == b.record.seqId
        override fun areContentsTheSame(a: EndfieldRecordProgress, b: EndfieldRecordProgress) = a == b
    },
) {
    class Holder(val binding: ItemGachaRecordsTxtBinding) : RecyclerView.ViewHolder(binding.root)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = Holder(
        ItemGachaRecordsTxtBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    override fun onBindViewHolder(holder: Holder, position: Int) {
        val row = getItem(position)
        val record = row.record
        val binding = holder.binding
        val context = binding.root.context
        binding.OperatorName.text = record.itemName.ifBlank { record.itemId.ifBlank { binding.root.context.getString(R.string.unknown_item) } }
        val pool = record.poolName.ifBlank { record.poolType }
        binding.PoolInfo.text = if (record.isFree) binding.root.context.getString(R.string.gacha_free_pool, pool) else context.getString(R.string.gacha_record_pool_info, pool, row.pity)
        binding.Rarity.text = context.getString(R.string.gacha_rarity, record.rarity)
        binding.GainTime.text = formatGachaTime(record.gachaTs, "yyyy-MM-dd HH:mm:ss")
        binding.PoolCount.text = context.getString(R.string.current_pool_gacha_count, row.total)
        binding.IsNew.visibility = if (record.isNew) View.VISIBLE else View.GONE
        val color = androidx.core.content.ContextCompat.getColor(context,
            com.blueskybone.arkscreen.ui.character.rarityColorMap[record.rarity] ?: R.color.red)
        binding.OperatorName.setTextColor(color)
        binding.Rarity.setTextColor(color)
    }
}

internal fun formatGachaTime(raw: String, pattern: String): String {
    val timestamp = raw.toLongOrNull() ?: return raw
    val millis = if (timestamp < 10_000_000_000L) timestamp * 1000 else timestamp
    return SimpleDateFormat(pattern, Locale.getDefault()).format(Date(millis))
}
