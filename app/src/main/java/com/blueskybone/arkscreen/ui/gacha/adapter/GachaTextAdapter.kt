package com.blueskybone.arkscreen.ui.gacha.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.blueskybone.arkscreen.R
import com.blueskybone.arkscreen.databinding.ItemGachaRecordsTxtBinding
import com.blueskybone.arkscreen.domain.model.operator.rarityColorMap
import com.blueskybone.arkscreen.ui.common.adapter.paging.PagingAdapter
import com.blueskybone.arkscreen.ui.gacha.model.Record
import com.blueskybone.arkscreen.util.TimeUtils

/**
 *   Created by blueskybone
 *   Date: 2025/9/21
 */
/*
* 寻访详情adapter,全部使用textView
* */
class GachaTextAdapter(private val context: Context, override val PAGE_SIZE: Int) :
    PagingAdapter<Record, RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val context = context
        val inflater = LayoutInflater.from(context)
        val binding = ItemGachaRecordsTxtBinding.inflate(inflater, parent, false)
        return GachaVH(binding)
    }

    override fun bindViewHolder(holder: RecyclerView.ViewHolder, item: Record) {
        (holder as GachaVH) .bind(item)
    }

    inner class GachaVH(private val binding: ItemGachaRecordsTxtBinding) :
        RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("SetTextI18n")
        fun bind(item: Record) {
            val timeStr = TimeUtils.getTimeStr(item.ts, "YY-MM-dd HH:mm:ss")
            binding.RecordsText.text = "[$timeStr][${item.count}][${item.id}] ${item.name} (${item.count})"
            binding.IsNew.visibility = if (item.isNew) View.VISIBLE else View.GONE
            val colorId = rarityColorMap[item.count + 1] ?: R.color.red
            val color = ContextCompat.getColor(context, colorId)
            binding.RecordsText.setTextColor(color)
        }
    }
}