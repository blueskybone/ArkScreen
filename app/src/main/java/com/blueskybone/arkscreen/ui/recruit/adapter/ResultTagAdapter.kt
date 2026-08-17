package com.blueskybone.arkscreen.ui.recruit.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.blueskybone.arkscreen.R

class ResultTagAdapter :
    ListAdapter<String, ResultTagAdapter.ResultTagViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ResultTagViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_result_tag, parent, false)
        return ResultTagViewHolder(view as TextView)
    }

    override fun onBindViewHolder(holder: ResultTagViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ResultTagViewHolder(
        private val textView: TextView
    ) : RecyclerView.ViewHolder(textView) {

        fun bind(text: String) {
            textView.text = text
        }
    }

    private class DiffCallback : DiffUtil.ItemCallback<String>() {
        override fun areItemsTheSame(oldItem: String, newItem: String): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: String, newItem: String): Boolean {
            return oldItem == newItem
        }
    }
}