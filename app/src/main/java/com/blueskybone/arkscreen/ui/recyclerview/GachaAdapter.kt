package com.blueskybone.arkscreen.ui.recyclerview

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.blueskybone.arkscreen.R
import com.blueskybone.arkscreen.databinding.ItemGachaCardBinding
import com.blueskybone.arkscreen.room.Gachas

/**
 *   Created by blueskybone
 *   Date: 2025/2/3
 */
class GachaAdapter(private val context: Context) : ListAdapter<Gachas, GachaAdapter.GachasVH>(
    DiffCallback
) {

    private object DiffCallback : DiffUtil.ItemCallback<Gachas>() {
        override fun areItemsTheSame(oldItem: Gachas, newItem: Gachas): Boolean {
            return oldItem.pool == newItem.pool
        }

        override fun areContentsTheSame(oldItem: Gachas, newItem: Gachas): Boolean {
            return (oldItem.count == newItem.count)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GachasVH {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemGachaCardBinding.inflate(inflater, parent, false)
        return GachasVH(binding)
    }

    override fun onBindViewHolder(holder: GachasVH, position: Int) {
        holder.bind(getItem(position))
    }

    inner class GachasVH(private val binding: ItemGachaCardBinding) :
        RecyclerView.ViewHolder(binding.root) {

        private val innerRecyclerView = binding.RecyclerView
        private val adapter = RecordsAdapter(context)

        init {
            innerRecyclerView.layoutManager = LinearLayoutManager(itemView.context)
            innerRecyclerView.adapter = adapter
        }

        fun bind(item: Gachas) {
            adapter.submitList(item.data)
            binding.RecordsCount.text = context.getString(R.string.gacha_count,item.count)
            binding.PoolName.text = item.pool
            binding.Fes.visibility = if (item.isFes) View.VISIBLE else View.GONE
        }
    }


}