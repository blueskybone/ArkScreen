package com.blueskybone.arkscreen.ui.gacha.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.blueskybone.arkscreen.databinding.ItemGachaCardBinding
import com.blueskybone.arkscreen.ui.gacha.model.GachaPool
import com.blueskybone.arkscreen.ui.recyclerview.RecordsAdapter

/**
 *   Created by blueskybone
 *   Date: 2025/2/3
 */
class GachaAdapter(private val context: Context) : ListAdapter<GachaPool, GachaAdapter.GachasVH>(
    DiffCallback
) {

    private object DiffCallback : DiffUtil.ItemCallback<GachaPool>() {
        override fun areItemsTheSame(oldItem: GachaPool, newItem: GachaPool): Boolean {
            return oldItem.poolId == newItem.poolId
        }

        override fun areContentsTheSame(oldItem: GachaPool, newItem: GachaPool): Boolean {
            return (oldItem.poolName == newItem.poolName)
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

        fun bind(item: GachaPool) {
            adapter.submitList(item.records)
            binding.RecordsCount.text = item.records.size.toString()
            binding.PoolName.text = item.poolName
            binding.Fes.visibility = if (item.isFes) View.VISIBLE else View.GONE
        }
    }
}