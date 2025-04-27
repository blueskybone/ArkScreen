package com.blueskybone.arkscreen.recyclerview

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.blueskybone.arkscreen.databinding.ItemLinkBinding
import com.blueskybone.arkscreen.room.Link

/**
 *   Created by blueskybone
 *   Date: 2025/1/8
 */
class LinkAdapter(private val listener: ItemListener) :
    ListAdapter<Link, LinkAdapter.LinkVH>(DiffCallback) {

    private object DiffCallback : DiffUtil.ItemCallback<Link>() {
        override fun areItemsTheSame(oldItem: Link, newItem: Link): Boolean {
            return oldItem.id == newItem.id
        }
        override fun areContentsTheSame(oldItem: Link, newItem: Link): Boolean {
            return (oldItem.url == newItem.url && oldItem.title == newItem.title)
        }
    }

    inner class LinkVH(private val binding: ItemLinkBinding, listener: ItemListener) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnLongClickListener {
                listener.onLongClick(bindingAdapterPosition)
                true
            }
        }

        fun bind(item: Link) {
            binding.Title.text = item.title
            binding.Value.text = item.url
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LinkVH {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemLinkBinding.inflate(inflater, parent, false)
        return LinkVH(binding, listener)
    }

    override fun onBindViewHolder(holder: LinkVH, position: Int) {
        holder.bind(getItem(position))
    }
}