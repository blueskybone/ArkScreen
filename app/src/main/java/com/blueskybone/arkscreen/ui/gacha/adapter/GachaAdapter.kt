package com.blueskybone.arkscreen.ui.gacha.adapter

import android.content.Context
import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.core.content.ContextCompat
import coil.load
import com.blueskybone.arkscreen.R
import com.blueskybone.arkscreen.data.network.avatarUrl
import com.blueskybone.arkscreen.databinding.ItemGachaCardBinding
import com.blueskybone.arkscreen.databinding.ItemGachaRecordsBinding
import com.blueskybone.arkscreen.ui.gacha.model.GachaPool
import com.blueskybone.arkscreen.ui.gacha.model.Record
import com.blueskybone.arkscreen.platform.time.TimeUtils.getTimeStr
import com.google.android.material.color.MaterialColors
import java.net.URLEncoder

/**
 * 每个卡池直接渲染为一张卡片，避免再嵌套一层 RecyclerView。
 */
class GachaAdapter(
    private val context: Context,
    private val onExpandedChange: (String, Boolean) -> Unit = { _, _ -> },
) : ListAdapter<GachaPool, GachaAdapter.PoolViewHolder>(DiffCallback) {

    private val expandedPoolIds = mutableSetOf<String>()

    private object DiffCallback : DiffUtil.ItemCallback<GachaPool>() {
        override fun areItemsTheSame(oldItem: GachaPool, newItem: GachaPool): Boolean =
            oldItem.poolId == newItem.poolId

        override fun areContentsTheSame(oldItem: GachaPool, newItem: GachaPool): Boolean =
            oldItem == newItem
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PoolViewHolder {
        val binding = ItemGachaCardBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false,
        )
        return PoolViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PoolViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class PoolViewHolder(
        private val binding: ItemGachaCardBinding,
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(pool: GachaPool) {
            binding.PoolName.text = pool.poolName
            binding.RecordsCount.text = pool.totalCount.toString()
            binding.Fes.visibility = if (pool.isFes) View.VISIBLE else View.GONE

            binding.RecordsContainer.removeAllViews()
            val isExpanded = pool.poolId in expandedPoolIds
            val visibleRecords = if (isExpanded) {
                pool.hitRecords
            } else {
                pool.hitRecords.take(COLLAPSED_RECORD_COUNT)
            }
            visibleRecords.forEach { record ->
                val recordBinding = ItemGachaRecordsBinding.inflate(
                    LayoutInflater.from(binding.RecordsContainer.context),
                    binding.RecordsContainer,
                    false,
                )
                bindRecord(recordBinding, record)
                binding.RecordsContainer.addView(recordBinding.root)
            }

            binding.EmptyState.visibility =
                if (pool.hitRecords.isEmpty()) View.VISIBLE else View.GONE

            val canExpand = pool.hitRecords.size > COLLAPSED_RECORD_COUNT
            binding.ExpandHint.visibility = if (canExpand) View.VISIBLE else View.GONE
            if (canExpand) {
                binding.ExpandHint.text = if (isExpanded) {
                    context.getString(R.string.collapse_gacha_records)
                } else {
                    context.getString(R.string.expand_gacha_records, pool.hitRecords.size)
                }
                binding.root.setOnClickListener {
                    if (!expandedPoolIds.add(pool.poolId)) {
                        expandedPoolIds.remove(pool.poolId)
                    }
                    onExpandedChange(pool.poolId, pool.poolId in expandedPoolIds)
                    val position = bindingAdapterPosition
                    if (position != RecyclerView.NO_POSITION) notifyItemChanged(position)
                }
            } else {
                binding.root.setOnClickListener(null)
            }
        }

        private fun bindRecord(binding: ItemGachaRecordsBinding, record: Record) {
            binding.GachaCount.text = context.getString(R.string.gacha_count, record.count)
            binding.ProgressBar.progress = record.count
            binding.ProgressBar.progressTintList = ColorStateList.valueOf(
                progressColor(record.count, binding.root)
            )
            binding.GainDate.text = getTimeStr(record.ts, "yyyy-MM-dd")
            binding.IsNew.visibility = if (record.isNew) View.VISIBLE else View.GONE

            val skinUrl = URLEncoder.encode("${record.charId}#1.png", "UTF-8")
            binding.Avatar.load("$avatarUrl$skinUrl")
        }

        private fun progressColor(count: Int, view: View): Int = when {
            count > HIGH_PITY_THRESHOLD -> ContextCompat.getColor(context, R.color.rare_6)
            count > MEDIUM_PITY_THRESHOLD -> ContextCompat.getColor(context, R.color.rare_5)
            else -> MaterialColors.getColor(
                view,
                com.google.android.material.R.attr.colorPrimary,
            )
        }
    }

    fun restoreExpandedPools(poolIds: Set<String>) {
        if (expandedPoolIds == poolIds) return
        expandedPoolIds.clear()
        expandedPoolIds.addAll(poolIds)
        notifyDataSetChanged()
    }

    private companion object {
        const val COLLAPSED_RECORD_COUNT = 3
        const val MEDIUM_PITY_THRESHOLD = 40
        const val HIGH_PITY_THRESHOLD = 50
    }
}
