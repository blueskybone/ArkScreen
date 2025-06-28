package com.blueskybone.arkscreen.ui.recyclerview

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.blueskybone.arkscreen.APP
import com.blueskybone.arkscreen.R
import com.blueskybone.arkscreen.databinding.ItemLinkBinding
import com.blueskybone.arkscreen.databinding.ItemLinkRoundBinding
import com.blueskybone.arkscreen.room.Link

/**
 *   Created by blueskybone
 *   Date: 2025/6/15
 */

class LinkGridAdapter(private val listener: ItemListener) :
    ListAdapter<Link, LinkGridAdapter.LinkVH>(DiffCallback) {

    private object DiffCallback : DiffUtil.ItemCallback<Link>() {
        override fun areItemsTheSame(oldItem: Link, newItem: Link): Boolean {
            return oldItem.id == newItem.id
        }
        override fun areContentsTheSame(oldItem: Link, newItem: Link): Boolean {
            return (oldItem.title == newItem.title && oldItem.url == newItem.url && oldItem.icon == newItem.icon)
        }
    }

    inner class LinkVH(private val binding: ItemLinkRoundBinding, listener: ItemListener) :
        RecyclerView.ViewHolder(binding.root) {
        init {
            binding.root.setOnLongClickListener {
                listener.onLongClick(bindingAdapterPosition)
                true
            }
            binding.root.setOnClickListener {
                listener.onClick(bindingAdapterPosition)
            }
        }

        fun bind(item: Link) {
            binding.Title.text = item.title
            binding.Icon.load(item.icon) {
                error(R.drawable.ic_default_trans)               // 加载失败的占位图
                crossfade(true)                     // 淡入淡出效果
                crossfade(300)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LinkVH {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemLinkRoundBinding.inflate(inflater, parent, false)
        return LinkVH(binding, listener)
    }

    override fun onBindViewHolder(holder: LinkVH, position: Int) {
        holder.bind(getItem(position))
    }
}
