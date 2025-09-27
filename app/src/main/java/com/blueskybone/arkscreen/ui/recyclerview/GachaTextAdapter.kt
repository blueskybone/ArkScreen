package com.blueskybone.arkscreen.ui.recyclerview

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.blueskybone.arkscreen.R
import com.blueskybone.arkscreen.databinding.ItemGachaRecordsTxtBinding
import com.blueskybone.arkscreen.playerinfo.rarityColorMap
import com.blueskybone.arkscreen.room.GachaWithNum
import com.blueskybone.arkscreen.ui.recyclerview.paging.PagingAdapter
import com.blueskybone.arkscreen.util.TimeUtils

/**
 *   Created by blueskybone
 *   Date: 2025/9/21
 */

class GachaTextAdapter(private val context: Context, override val PAGE_SIZE: Int) :
    PagingAdapter<GachaWithNum, RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val context = context
        val inflater = LayoutInflater.from(context)
        val binding = ItemGachaRecordsTxtBinding.inflate(inflater, parent, false)
        return GachaVH(binding)
    }

    override fun bindViewHolder(holder: RecyclerView.ViewHolder, item: GachaWithNum) {
        (holder as GachaVH) .bind(item)
    }

    inner class GachaVH(private val binding: ItemGachaRecordsTxtBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: GachaWithNum) {
            val timeStr = TimeUtils.getTimeStr(item.gacha.ts, "YY-MM-dd HH:mm:ss")
            binding.RecordsText.text = "[$timeStr][${item.numSum}][${item.gacha.pool}] ${item.gacha.charName} (${item.numCount})"
            binding.IsNew.visibility = if (item.gacha.isNew) View.VISIBLE else View.GONE
            val colorId = rarityColorMap[item.gacha.rarity + 1] ?: R.color.red
            val color = ContextCompat.getColor(context, colorId)
            binding.RecordsText.setTextColor(color)
        }
    }
}