package com.blueskybone.arkscreen.ui.recyclerview

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.blueskybone.arkscreen.R
import com.blueskybone.arkscreen.databinding.ItemAccountBinding
import com.blueskybone.arkscreen.databinding.ItemGachaRecordsTxtBinding
import com.blueskybone.arkscreen.playerinfo.Gachas
import com.blueskybone.arkscreen.playerinfo.rarityColorMap
import com.blueskybone.arkscreen.preference.PrefManager
import com.blueskybone.arkscreen.room.Account
import com.blueskybone.arkscreen.room.AccountGc
import com.blueskybone.arkscreen.room.AccountSk
import com.blueskybone.arkscreen.room.Gacha
import com.blueskybone.arkscreen.util.TimeUtils
import org.koin.java.KoinJavaComponent.getKoin

/**
 *   Created by blueskybone
 *   Date: 2025/9/21
 */
class GachaTextAdapter(private val context: Context) :
    ListAdapter<Gacha, GachaTextAdapter.GachaVH>(DiffCallback) {

    private object DiffCallback : DiffUtil.ItemCallback<Gacha>() {
        override fun areItemsTheSame(oldItem: Gacha, newItem: Gacha): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Gacha, newItem: Gacha): Boolean {
            return oldItem.id == newItem.id
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GachaVH {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemGachaRecordsTxtBinding.inflate(inflater, parent, false)
        return GachaVH(binding)
    }

    override fun onBindViewHolder(holder: GachaVH, position: Int) {
        holder.bind(getItem(position))
    }
    inner class GachaVH(private val binding: ItemGachaRecordsTxtBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Gacha) {
            binding.RecordsText.text = "[${TimeUtils.getTimeStr(item.ts, "YY-MM-dd HH:mm:ss")}] [${item.pool}] ${item.charName}"
            binding.IsNew.visibility = if(item.isNew) View.VISIBLE else View.GONE
            val colorId = rarityColorMap[item.rarity + 1] ?: R.color.red
            val color = ContextCompat.getColor(context, colorId)
            binding.RecordsText.setTextColor(color)
        }
    }
}