package com.blueskybone.arkscreen.ui.recruit.adapter

import android.view.ViewGroup
import android.widget.Button
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.blueskybone.arkscreen.ui.common.view.tagButton
import com.blueskybone.arkscreen.ui.recruit.TagItem

class TagAdapter(
    private val onClick: (String) -> Unit
) : ListAdapter<TagItem, TagAdapter.TagViewHolder>(DiffCallback()) {

    private val selectedSet = mutableSetOf<String>()

    fun updateSelected(newSelected: List<String>) {
        val oldSet = selectedSet.toSet()
        selectedSet.clear()
        selectedSet.addAll(newSelected)

        currentList.forEachIndexed { index, item ->
            val oldSelected = item.name in oldSet
            val newSelectedState = item.name in selectedSet
            if (oldSelected != newSelectedState) {
                notifyItemChanged(index)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TagViewHolder {
        val button = tagButton(parent.context, "")
        return TagViewHolder(button)
    }

    override fun onBindViewHolder(holder: TagViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class TagViewHolder(
        private val button: Button
    ) : RecyclerView.ViewHolder(button) {

        fun bind(item: TagItem) {
            button.text = item.name
            button.isSelected = item.name in selectedSet
            button.setOnClickListener { onClick(item.name) }
        }
    }

    private class DiffCallback : DiffUtil.ItemCallback<TagItem>() {
        override fun areItemsTheSame(oldItem: TagItem, newItem: TagItem): Boolean {
            return oldItem.name == newItem.name
        }

        override fun areContentsTheSame(oldItem: TagItem, newItem: TagItem): Boolean {
            return oldItem == newItem
        }
    }
}