package com.blueskybone.arkscreen.ui.recruit.adapter

import android.view.ViewGroup
import android.content.res.ColorStateList
import androidx.core.content.ContextCompat
import com.blueskybone.arkscreen.R
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
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
        val container = android.view.LayoutInflater.from(parent.context)
            .inflate(R.layout.item_recruit_select_tag, parent, false)
        return TagViewHolder(container)
    }

    override fun onBindViewHolder(holder: TagViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class TagViewHolder(
        private val container: android.view.View
    ) : RecyclerView.ViewHolder(container) {
        private val button = container.findViewById<android.widget.TextView>(R.id.tagLabel)

        fun bind(item: TagItem) {
            button.text = item.name
            val selected = item.name in selectedSet
            button.isSelected = selected
            val context = button.context
            val background = if (selected) com.google.android.material.color.MaterialColors.getColor(
                button, R.attr.capsuleAccentContainer
            ) else ContextCompat.getColor(context, R.color.recruit_tag_surface)
            val foreground = if (selected) com.google.android.material.color.MaterialColors.getColor(
                button, R.attr.capsuleAccentContent
            ) else ContextCompat.getColor(context, R.color.home_text_primary)
            button.backgroundTintList = ColorStateList.valueOf(background)
            button.setTextColor(foreground)
            container.isSelected = selected
            container.contentDescription = item.name
            container.setOnClickListener { onClick(item.name) }
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
