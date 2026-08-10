package com.blueskybone.arkscreen.ui.gacha.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.blueskybone.arkscreen.R
import com.blueskybone.arkscreen.databinding.ItemGachaRecordsTxtBinding
import com.blueskybone.arkscreen.ui.character.rarityColorMap
import com.blueskybone.arkscreen.ui.common.adapter.paging.PagingAdapter
import com.blueskybone.arkscreen.ui.gacha.model.Record
import com.blueskybone.arkscreen.platform.time.TimeUtils

class GachaTextAdapter(
    private val context: Context,
    override val PAGE_SIZE: Int,
) : PagingAdapter<Record, RecyclerView.ViewHolder>() {

    override fun areItemsTheSame(oldItem: Record, newItem: Record): Boolean =
        oldItem.id == newItem.id

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val binding = ItemGachaRecordsTxtBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false,
        )
        return GachaViewHolder(binding)
    }

    override fun bindViewHolder(holder: RecyclerView.ViewHolder, item: Record) {
        (holder as GachaViewHolder).bind(item)
    }

    inner class GachaViewHolder(
        private val binding: ItemGachaRecordsTxtBinding,
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Record) {
            val rarity = item.rare + 1
            binding.OperatorName.text = item.name
            binding.Rarity.text = context.getString(R.string.gacha_rarity, rarity)
            binding.PoolInfo.text = context.getString(
                R.string.gacha_record_pool_info,
                item.gachaPool,
                item.count,
            )
            binding.PoolCount.text = context.getString(
                R.string.current_pool_gacha_count,
                item.gachaCount,
            )
            binding.GainTime.text = TimeUtils.getTimeStr(item.ts, "yyyy-MM-dd HH:mm:ss")
            binding.IsNew.visibility = if (item.isNew) View.VISIBLE else View.GONE

            val colorId = rarityColorMap[rarity] ?: R.color.red
            val rarityColor = ContextCompat.getColor(context, colorId)
            binding.OperatorName.setTextColor(rarityColor)
            binding.Rarity.setTextColor(rarityColor)
        }
    }
}
